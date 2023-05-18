package hu.aestallon.jsmol.marshaller;

import hu.aestallon.jsmol.json.JsonValue;
import hu.aestallon.jsmol.result.Result;

public interface JsonMarshaller<T> {

  Result<JsonValue> marshall(T t);

}
