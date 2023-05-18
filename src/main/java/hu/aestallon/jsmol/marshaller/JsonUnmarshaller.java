package hu.aestallon.jsmol.marshaller;

import hu.aestallon.jsmol.json.JsonValue;
import hu.aestallon.jsmol.result.Result;

public interface JsonUnmarshaller<T> {
  Result<T> unmarshall(JsonValue json);

}
