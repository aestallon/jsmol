package hu.aestallon.jsmol.marshaller;

import hu.aestallon.jsmol.json.JsonNull;
import hu.aestallon.jsmol.json.JsonObject;
import hu.aestallon.jsmol.json.JsonValue;
import hu.aestallon.jsmol.result.ExErr;
import hu.aestallon.jsmol.result.Ok;
import hu.aestallon.jsmol.result.Result;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public sealed class ObjectMarshaller<T>
    implements JsonMarshaller<T>
    permits ExtendedObjectMarshaller {

  private final Map<String, Function<T, Result<JsonValue>>> getters = new HashMap<>();
  private final Map<String, BiConsumer<T, JsonValue>>       setters = new HashMap<>();
  private final Supplier<T>                                 typeConstructor;

  public ObjectMarshaller(Supplier<T> typeConstructor) {this.typeConstructor = typeConstructor;}

  public ObjectMarshaller<T> bindString(String name, Function<T, String> getter,
                                        BiConsumer<T, String> setter) {
    return this.bind(name, JsonMarshallers.STRING_MARSHALLER, getter, setter);
  }

  public <P> ObjectMarshaller<T> bind(String name, JsonMarshaller<P> marshaller,
                                      Function<T, ? extends P> getter,
                                      BiConsumer<T, ? super P> setter) {
    this.getters.put(name, t -> marshaller.marshall(getter.apply(t)));
    this.setters.put(name, (t, json) -> marshaller
        .unmarshall(json)
        .ifOk(s -> setter.accept(t, s)));
    return this;
  }

  ObjectMarshaller<T> bindRaw(String name, JsonMarshaller marshaller,
                              Function getter,
                              BiConsumer setter) {
    return this.bind(name, marshaller, getter, setter);
  }

  @Override
  public Result<JsonValue> marshall(T t) {
    if (t == null) {
      return new Ok<>(new JsonNull());
    }
    Map<String, Result<JsonValue>> marshalledProperties = getters.entrySet().stream()
        .map(e -> {
          final String name = e.getKey();
          final Function<T, Result<JsonValue>> accessor = e.getValue();
          Result<JsonValue> marshalledResult = accessor.apply(t);

          return Map.entry(name, marshalledResult);
        })
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    if (marshalledProperties.values().stream().allMatch(Result::isOk)) {
      JsonObject jsonObject = new JsonObject(marshalledProperties
          .entrySet().stream()
          .map(e -> Map.entry(e.getKey(), e.getValue().unwrap()))
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
      return new Ok<>(jsonObject);
    }
    return marshalledProperties.values().stream()
        .filter(r -> !r.isOk())
        .findFirst()
        .orElseThrow();
  }

  @Override
  public Result<T> unmarshall(JsonValue json) {
    if (json instanceof JsonNull) {
      return new Ok<>(null);
    }
    final T t = typeConstructor.get();
    if (json instanceof JsonObject jsonObject) {

      this.setters.forEach(
          (property, setter) -> setter.accept(t, jsonObject.value().get(property)));
      return new Ok<>(t);
    }
    return new ExErr<>(new TypeConversionException(json.getClass(), t.getClass()));
  }

}
