package hu.aestallon.jsmol.marshaller.impl;

import hu.aestallon.jsmol.json.JsonNull;
import hu.aestallon.jsmol.json.JsonObject;
import hu.aestallon.jsmol.json.JsonValue;
import hu.aestallon.jsmol.marshaller.JsonMarshaller;
import hu.aestallon.jsmol.marshaller.JsonTypeMapper;
import hu.aestallon.jsmol.marshaller.JsonUnmarshaller;
import hu.aestallon.jsmol.marshaller.TypeConversionException;
import hu.aestallon.jsmol.result.ExErr;
import hu.aestallon.jsmol.result.Ok;
import hu.aestallon.jsmol.result.Result;
import hu.aestallon.jsmol.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ObjectMapper<T> implements JsonTypeMapper<T> {

  private final Map<String, Function<T, Result<JsonValue>>> getters = new HashMap<>();
  private final Map<String, BiConsumer<T, JsonValue>>       setters = new HashMap<>();
  private final Supplier<T>                                 typeConstructor;

  ObjectMapper(Supplier<T> typeConstructor) {this.typeConstructor = typeConstructor;}

  <P> ObjectMapper<T> bind(String name,
                           JsonTypeMapper<P> typeMapper,
                           Function<T, ? extends P> getter,
                           BiConsumer<T, P> setter) {
    return this.bind(name, typeMapper, getter, typeMapper, setter);
  }

  <P> ObjectMapper<T> bind(String name, JsonMarshaller<P> marshaller,
                           Function<T, ? extends P> getter,
                           JsonUnmarshaller<P> unmarshaller,
                           BiConsumer<T, P> setter) {
    this.getters.put(name, t -> marshaller.marshall(getter.apply(t)));
    this.setters.put(name, (t, json) -> unmarshaller
        .unmarshall(json)
        .ifOk(p -> setter.accept(t, p)));
    return this;
  }

  @SuppressWarnings("unchecked, rawtypes")
  ObjectMapper<T> bindRaw(String name, JsonTypeMapper typeMapper,
                          Function getter,
                          BiConsumer setter) {
    return this.bind(name, typeMapper, getter, setter);
  }

  @SuppressWarnings("unchecked, rawtypes")
  ObjectMapper<T> bindRaw(String name, JsonMarshaller marshaller,
                          Function getter,
                          JsonUnmarshaller unmarshaller,
                          BiConsumer setter) {
    return this.bind(name, marshaller, getter, unmarshaller, setter);
  }

  @Override
  public Result<JsonValue> marshall(T t) {
    if (t == null) {
      return new Ok<>(JsonNull.INSTANCE);
    }
    return getters.entrySet().stream()
        .map(Pair::ofEntries)
        .map(Pair.onB(getter -> getter.apply(t)))
        .collect(Result.ofMap())
        .map(JsonObject::new);
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
