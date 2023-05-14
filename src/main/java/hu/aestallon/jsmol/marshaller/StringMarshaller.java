package hu.aestallon.jsmol.marshaller;

import hu.aestallon.jsmol.json.JsonNull;
import hu.aestallon.jsmol.json.JsonString;
import hu.aestallon.jsmol.json.JsonValue;
import hu.aestallon.jsmol.result.ExErr;
import hu.aestallon.jsmol.result.Ok;
import hu.aestallon.jsmol.result.Result;

public class StringMarshaller implements JsonMarshaller<String> {
  @Override
  public Result<JsonValue> marshall(String s) {
    return (s != null)
        ? new Ok<>(new JsonString(s))
        : new Ok<>(new JsonNull());
  }

  @Override
  public Result<String> unmarshall(JsonValue json) {
    if (json instanceof JsonNull) {
      return new Ok<>(null);
    }
    if (json instanceof JsonString jsonString) {
      return new Ok<>(jsonString.value());
    }
    return new ExErr<>(new TypeConversionException(json.getClass(), String.class));
  }
}
