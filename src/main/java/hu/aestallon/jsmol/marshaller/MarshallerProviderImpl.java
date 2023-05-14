package hu.aestallon.jsmol.marshaller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static hu.aestallon.jsmol.marshaller.JsonMarshallers.OBJECT_MARSHALLER;
import static hu.aestallon.jsmol.marshaller.JsonMarshallers.STRING_MARSHALLER;

public class MarshallerProviderImpl implements MarshallerProvider {

  private final Map<Class<?>, JsonMarshaller<?>> marshallersByClass = new HashMap<>();

  public MarshallerProviderImpl() {
    this.marshallersByClass.putAll(Map.of(
        String.class, STRING_MARSHALLER,
        Object.class, OBJECT_MARSHALLER));
  }

  @Override
  public <T> Optional<JsonMarshaller<T>> provide(Class<T> type) {
    JsonMarshaller<T> jsonMarshaller = (JsonMarshaller<T>) this.marshallersByClass.get(type);
    if (jsonMarshaller != null) {
      return Optional.of(jsonMarshaller);
    }
    this.marshallersByClass.put(type, this.register(type));
    return Optional.ofNullable((JsonMarshaller<T>) this.marshallersByClass.get(type));
  }

  private <T> JsonMarshaller<T> register(Class<T> type) {
    if (type.isRecord()) {
      return (JsonMarshaller<T>) new RecordMarshallerCreator<>(
          (Class<? extends Record>) type,
          this).create();
    }
    return null;
  }

  @Override
  public Optional<JsonMarshaller<?>> provide(String typeName) {
    return Optional.empty();
  }
}
