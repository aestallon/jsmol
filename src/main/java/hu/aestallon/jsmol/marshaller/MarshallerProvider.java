package hu.aestallon.jsmol.marshaller;

import java.util.Optional;

public interface MarshallerProvider {

  <T> Optional<JsonMarshaller<T>> provide(Class<T> type);

  Optional<JsonMarshaller<?>> provide(String typeName);

}
