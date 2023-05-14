package hu.aestallon.jsmol.marshaller;

import java.lang.ref.WeakReference;
import java.lang.reflect.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public final class RecordMarshallerCreator<T extends Record> {

  private final Class<T>                          recordClass;
  private final LinkedHashMap<String, Class<?>>   componentsByName = new LinkedHashMap<>();
  private final WeakReference<MarshallerProvider> marshallerProvider;

  public RecordMarshallerCreator(Class<T> recordClass, MarshallerProvider marshallerProvider) {
    this.recordClass = recordClass;
    this.marshallerProvider = new WeakReference<>(marshallerProvider);
  }

  public JsonMarshaller<T> create() {
    ObjectMarshaller<T> marshaller = null;
    try {
      marshaller = this.initMarshaller(recordClass);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    return marshaller;
  }


  private ObjectMarshaller<T> initMarshaller(Class<T> recordClass) throws ClassNotFoundException {
    RecordComponent[] recordComponents = recordClass.getRecordComponents();
    final ObjectMarshaller<T> marshaller = new ObjectMarshaller<>(() -> null);
    for (RecordComponent component : recordComponents) {
      Class<?> componentType = component.getType();
      Method componentAccessor = component.getAccessor();
      String componentName = component.getName();

      if (Objects.equals(componentType, List.class)) {
        Type componentGenericType = component.getGenericType();
        if (componentGenericType instanceof ParameterizedType parameterizedType) {
          String parameterTypeName = parameterizedType.getActualTypeArguments()[0].getTypeName();
          Class<?> parameterClass = Class.forName(parameterTypeName);
          marshallerProvider.get().provide(parameterClass)
              .ifPresent(elementMarshaller -> {
                marshaller.bindRaw(componentName,
                    new ArrayMarshaller((ObjectMarshaller) elementMarshaller),
                    t -> {
                      try {
                        return componentAccessor.invoke(t);
                      } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                      }
                    }, (t, unused) -> {});
              });
        }

      }
      marshallerProvider.get().provide(componentType)
          .ifPresent(
              componentMarshaller -> marshaller.bindRaw(componentName, componentMarshaller, t -> {
                try {
                  return componentAccessor.invoke(t);
                } catch (IllegalAccessException | InvocationTargetException e) {
                  throw new RuntimeException(e);
                }
              }, (t, unused) -> {}));
    }
    return marshaller;
  }


}
