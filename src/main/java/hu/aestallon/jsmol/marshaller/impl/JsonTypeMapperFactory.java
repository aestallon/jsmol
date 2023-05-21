package hu.aestallon.jsmol.marshaller.impl;

import hu.aestallon.jsmol.json.JsonNull;
import hu.aestallon.jsmol.json.JsonObject;
import hu.aestallon.jsmol.json.JsonValue;
import hu.aestallon.jsmol.marshaller.JsonMarshaller;
import hu.aestallon.jsmol.marshaller.JsonTypeMapper;
import hu.aestallon.jsmol.marshaller.JsonTypeMapperProvider;
import hu.aestallon.jsmol.marshaller.JsonUnmarshaller;
import hu.aestallon.jsmol.marshaller.TypeConversionException;
import hu.aestallon.jsmol.result.ExErr;
import hu.aestallon.jsmol.result.Ok;
import hu.aestallon.jsmol.result.Result;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

abstract sealed class JsonTypeMapperFactory<T> permits JsonTypeMapperFactory.RecordMapperFactory {

  static <T> Result<JsonTypeMapper<T>> create(Class<T> type, JsonTypeMapperProvider provider) {
    if (type.isRecord()) {
      return new RecordMapperFactory<>(type, provider).create();
    }

    return ExErr.of(new IllegalStateException("non-record type " + type.getSimpleName() + " is not"
                                              + " supported!"));
  }

  protected final Class<T>               type;
  protected final JsonTypeMapperProvider provider;

  protected JsonTypeMapperFactory(Class<T> type, JsonTypeMapperProvider provider) {
    this.type = type;
    this.provider = provider;
  }

  protected abstract Result<JsonTypeMapper<T>> create();

  static final class JsonTypeMapperImpl<T> implements JsonTypeMapper<T> {
    private final JsonMarshaller<T>   marshaller;
    private final JsonUnmarshaller<T> unmarshaller;

    JsonTypeMapperImpl(JsonMarshaller<T> marshaller, JsonUnmarshaller<T> unmarshaller) {
      this.marshaller = marshaller;
      this.unmarshaller = unmarshaller;
    }

    @Override
    public Result<JsonValue> marshall(T t) {
      return marshaller.marshall(t);
    }

    @Override
    public Result<T> unmarshall(JsonValue json) {
      return unmarshaller.unmarshall(json);
    }
  }

  static final class RecordMapperFactory<R> extends JsonTypeMapperFactory<R> {

    private final LinkedHashMap<String, RecordComponent> componentsByName = new LinkedHashMap<>();

    private RecordMapperFactory(Class<R> type, JsonTypeMapperProvider provider) {
      super(type, provider);
    }

    @Override
    protected Result<JsonTypeMapper<R>> create() {
      return this.createMarshaller()
          .flatMap(marshaller -> this.createUnmarshaller()
              .map(unmarshaller -> new JsonTypeMapperImpl<>(marshaller, unmarshaller)));
    }

    private Result<JsonMarshaller<R>> createMarshaller() {
      // get the components of the record:
      RecordComponent[] recordComponents = super.type.getRecordComponents();
      // construct the marshaller; as it will be used solely as a marshaller, we do not need an
      // actual instance supplier, we are going to bind the accessor methods only:
      final ObjectMapper<R> marshaller = new ObjectMapper<>(() -> null);

      for (RecordComponent component : recordComponents) {
        final Class<?> componentType = component.getType();
        final Method componentAccessor = component.getAccessor();
        final String componentName = component.getName();

        // memoize the component by its name to be available when constructing the unmarshaller:
        // TODO: Once annotations are checked, memoize by the declared JSON property name instead!
        this.componentsByName.put(componentName, component);

        if (Objects.equals(componentType, List.class)) {
          // Lists need special attention, we want to marshall them with an ArrayMapper
          Type componentGenericType = component.getGenericType();
          if (componentGenericType instanceof ParameterizedType parameterizedType) {
            String parameterTypeName = parameterizedType.getActualTypeArguments()[0].getTypeName();
            Class<?> parameterClass;
            try {
              parameterClass = Class.forName(parameterTypeName);
            } catch (ClassNotFoundException e) {
              return ExErr.of(e);
            }
            // we don't actually care what the result of this binding action is, the only
            // important thing is that it returns Ok. Even the #unwrap() in the functional
            // accessor would not cause a wild exception to be thrown as Result#map consumes any
            // exceptions and re-wraps them in an ExErr.
            @SuppressWarnings("rawtypes") final Result bindAction = provider
                .provideList(parameterClass)
                .map(arrayMapper -> marshaller.bindRaw(
                    componentName,
                    arrayMapper,
                    // TODO: Improve ObjectMapper#bindRaw to tolerate checked suppliers
                    t -> Result.of(() -> componentAccessor.invoke(t)).unwrap(),
                    (t, __) -> {}));
            // FIXME: we are technically allowed to do this, improve Result API to tolerate this
            //  usage:
            if (bindAction.isErr()) {return (Result<JsonMarshaller<R>>) bindAction;}
          }
        } else {
          // we do not support parameterized types at the moment.
          // Reasoning for the raw Result same as above:
          @SuppressWarnings("rawtypes") final Result bindAction = provider
              .provide(componentType)
              .map(componentMapper -> marshaller.bindRaw(
                  componentName,
                  componentMapper,
                  t -> Result.of(() -> componentAccessor.invoke(t)).unwrap(),
                  (t, __) -> {}));
          if (bindAction.isErr()) {return (Result<JsonMarshaller<R>>) bindAction;}
        }
      }
      return Ok.of(marshaller);
    }

    private Result<JsonUnmarshaller<R>> createUnmarshaller() {
      return Result
          .of(() -> super.type.getDeclaredConstructor(this.componentsByName
              .values().stream()
              .map(RecordComponent::getType)
              .toArray(Class<?>[]::new)))
          .map(this::createUnmarshaller);
    }

    private JsonUnmarshaller<R> createUnmarshaller(Constructor<R> constructor) {
      return json -> {
        if (json instanceof JsonNull) {
          return Ok.of(null);
        }
        if (json instanceof JsonObject jsonObject) {
          return this.componentsByName.entrySet().stream()
              .map(e -> {
                final String prop = e.getKey();
                final Class<?> javaType = e.getValue().getType();
                return (Result<?>) ((Objects.equals(List.class, javaType))
                    ? Optional
                        .ofNullable(jsonObject.value().get(prop))
                        .map(jsonVal -> Result.of(() -> e.getValue().getGenericType())
                            .map(ParameterizedType.class::cast)
                            .map(paramType -> paramType.getActualTypeArguments()[0])
                            .map(Type::getTypeName)
                            .map(Class::forName)
                            .flatMap(provider::provideList)
                            .flatMap(mapper -> mapper.unmarshall(jsonVal))
                            .map(List.class::cast))
                        .orElse(Ok.of(null))
                    : Optional
                        .ofNullable(jsonObject.value().get(prop))
                        .map(jsonVal -> provider
                            .provide(javaType)
                            .flatMap(mapper -> mapper.unmarshall(jsonVal)))
                        .orElse(Ok.of(null)));
              })
              .collect(Result.toList())
              .map(args -> args.toArray(Object[]::new))
              .map(constructor::newInstance);
        }
        return ExErr.of(new TypeConversionException(json.getClass(), super.type));
      };
    }
  }

}
