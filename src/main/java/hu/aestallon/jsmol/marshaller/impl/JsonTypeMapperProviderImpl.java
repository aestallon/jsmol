package hu.aestallon.jsmol.marshaller.impl;

import hu.aestallon.jsmol.marshaller.ArrayMapper;
import hu.aestallon.jsmol.marshaller.JsonTypeMapper;
import hu.aestallon.jsmol.marshaller.JsonTypeMapperProvider;
import hu.aestallon.jsmol.result.Ok;
import hu.aestallon.jsmol.result.Result;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JsonTypeMapperProviderImpl implements JsonTypeMapperProvider {

  private final Map<Class<?>, JsonTypeMapper<?>> typeMappersByClass  = new ConcurrentHashMap<>();
  private final Map<Class<?>, ArrayMapper<?>>    arrayMappersByClass = new ConcurrentHashMap<>();

  public JsonTypeMapperProviderImpl() {
    this.register(String.class, JsonPrimitiveMapper.STRING_MAPPER);
    this.register(Integer.class, JsonPrimitiveMapper.INT_MAPPER);
    this.register(Long.class, JsonPrimitiveMapper.LONG_MAPPER);
    this.register(Float.class, JsonPrimitiveMapper.FLOAT_MAPPER);
    this.register(Double.class, JsonPrimitiveMapper.DOUBLE_MAPPER);
    this.register(Boolean.class, JsonPrimitiveMapper.BOOLEAN_MAPPER);
    this.register(int.class, JsonPrimitiveMapper.INT_MAPPER);
    this.register(long.class, JsonPrimitiveMapper.LONG_MAPPER);
    this.register(float.class, JsonPrimitiveMapper.FLOAT_MAPPER);
    this.register(double.class, JsonPrimitiveMapper.DOUBLE_MAPPER);
    this.register(boolean.class, JsonPrimitiveMapper.BOOLEAN_MAPPER);
  }

  @Override
  public <T> Result<JsonTypeMapper<T>> provide(Class<T> type) {
    @SuppressWarnings("unchecked")
    JsonTypeMapper<T> typeMapper = (JsonTypeMapper<T>) this.typeMappersByClass.get(type);
    if (typeMapper != null) {
      return Ok.of(typeMapper);
    }
    Result<JsonTypeMapper<T>> r = JsonTypeMapperFactory.create(type, this);
    r.ifOk(mapper -> this.register(type, mapper));
    return r;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <E> Result<ArrayMapper<E>> provideList(Class<E> elementType) {
    ArrayMapper<E> arrayMapper = (ArrayMapper<E>) this.arrayMappersByClass.get(elementType);
    if (arrayMapper != null) {
      return Ok.of(arrayMapper);
    }
    return this.provide(elementType)
        .map(__ -> (ArrayMapper<E>) this.arrayMappersByClass.get(elementType));
  }

  @Override
  public <T> void register(Class<T> type, JsonTypeMapper<T> typeMapper) {
    this.typeMappersByClass.put(type, typeMapper);
    this.arrayMappersByClass.put(type, new ArrayMapper<>(typeMapper));
  }
}
