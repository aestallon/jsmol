package hu.aestallon.jsmol.marshaller;

import hu.aestallon.jsmol.json.JsonArray;
import hu.aestallon.jsmol.json.JsonBoolean;
import hu.aestallon.jsmol.json.JsonNull;
import hu.aestallon.jsmol.json.JsonNumber;
import hu.aestallon.jsmol.json.JsonObject;
import hu.aestallon.jsmol.json.JsonString;
import hu.aestallon.jsmol.json.JsonValue;
import hu.aestallon.jsmol.json.WrappedValue;
import hu.aestallon.jsmol.result.ExErr;
import hu.aestallon.jsmol.result.Ok;
import hu.aestallon.jsmol.result.Result;
import hu.aestallon.jsmol.util.Pair;

import java.util.List;
import java.util.Map;

public class JsonValueMapper implements JsonTypeMapper<Object> {

  private final ArrayMapper<Object> arrayMapper;

  public JsonValueMapper() {
    this.arrayMapper = new ArrayMapper<>(this);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Result<JsonValue> marshall(Object o) {
    return switch (o) {
      case null -> Ok.of(JsonNull.INSTANCE);
      case Number n -> Ok.of(new JsonNumber(n));
      case String s -> Ok.of(new JsonString(s));
      case Boolean b -> Ok.of(new JsonBoolean(b));
      case List<?> l -> Result
          .of(() -> (List<Object>) l)
          .flatMap(arrayMapper::marshall);
      case Map<?, ?> m -> Result
          .of(() -> (Map<String, Object>) m)
          .flatMap(this::marshallObjectMap);
      default -> ExErr.of(new TypeConversionException(o.getClass(), JsonValue.class));
    };
  }

  private Result<JsonValue> marshallObjectMap(Map<String, Object> m) {
    return m.entrySet().stream()
        .map(Pair::ofEntries)
        .map(Pair.onB(this::marshall))
        .collect(Result.ofMap())
        .map(JsonObject::new);
  }

  @Override
  public Result<Object> unmarshall(JsonValue json) {
    return switch (json) {
      case JsonNull ignored -> Ok.of(null);
      case JsonArray arr -> arrayMapper
          .unmarshall(arr)
          .map(Object.class::cast);
      case JsonObject obj -> this.unmarshallObjectMap(obj)
          .map(Object.class::cast);
      case WrappedValue<?> w -> Ok.of(w.value());
    };
  }

  private Result<Map<String, Object>> unmarshallObjectMap(JsonObject obj) {
    return obj.value().entrySet().stream()
        .map(Pair::ofEntries)
        .map(Pair.onB(this::unmarshall))
        .collect(Result.ofMap());
  }
}
