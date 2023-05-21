package hu.aestallon.jsmol.marshaller;

import hu.aestallon.jsmol.json.JsonArray;
import hu.aestallon.jsmol.json.JsonNull;
import hu.aestallon.jsmol.json.JsonValue;
import hu.aestallon.jsmol.result.ExErr;
import hu.aestallon.jsmol.result.Ok;
import hu.aestallon.jsmol.result.Result;

import java.util.Collections;
import java.util.List;

public class ArrayMapper<E> implements JsonTypeMapper<List<E>> {

  private final JsonTypeMapper<E> typeMapper;

  public ArrayMapper(JsonTypeMapper<E> typeMapper) {
    this.typeMapper = typeMapper;
  }

  @Override
  public Result<JsonValue> marshall(List<E> es) {
    if (es == null) {
      return Ok.of(JsonNull.INSTANCE);
    }
    if (es.isEmpty()) {
      return new Ok<>(new JsonArray(Collections.emptyList()));
    }
    return es.stream()
        .map(typeMapper::marshall)
        .collect(Result.toList())
        .map(JsonArray::new);
  }

  @Override
  public Result<List<E>> unmarshall(JsonValue json) {
    if (json instanceof JsonNull) {
      return new Ok<>(null);
    }
    if (json instanceof JsonArray array) {
      return array.value().stream()
          .map(typeMapper::unmarshall)
          .collect(Result.toList());
    }
    return new ExErr<>(new TypeConversionException(json.getClass(), List.class));
  }
}
