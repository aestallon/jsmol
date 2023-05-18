package hu.aestallon.jsmol.marshaller;

import hu.aestallon.jsmol.result.Result;

public interface JsonTypeMapperProvider {

  <T> Result<JsonTypeMapper<T>> provide(Class<T> type);

  <E> Result<ArrayMapper<E>> provideList(Class<E> elementType);

  <T> void register(Class<T> type, JsonTypeMapper<T> typeMapper);

}
