package hu.aestallon.jsmol.marshaller;

import hu.aestallon.jsmol.json.JsonArray;
import hu.aestallon.jsmol.json.JsonNull;
import hu.aestallon.jsmol.json.JsonValue;
import hu.aestallon.jsmol.result.ExErr;
import hu.aestallon.jsmol.result.Ok;
import hu.aestallon.jsmol.result.Result;

import java.util.Collections;
import java.util.List;

public class ArrayMarshaller<T> implements JsonMarshaller<List<T>> {

  private final ObjectMarshaller<T> objectMarshaller;

  public ArrayMarshaller(ObjectMarshaller<T> objectMarshaller) {
    this.objectMarshaller = objectMarshaller;
  }

  @Override
  public Result<JsonValue> marshall(List<T> ts) {
    if (ts == null) {
      return new Ok<>(new JsonNull());
    }
    if (ts.isEmpty()) {
      return new Ok<>(new JsonArray(Collections.emptyList()));
    }
    List<Result<JsonValue>> list = ts.stream()
        .map(objectMarshaller::marshall)
        .toList();
    if (list.stream().anyMatch(r -> !r.isOk())) {
      return list.stream()
          .filter(r -> !r.isOk())
          .findFirst()
          .orElseThrow();
    }
    return new Ok<>(new JsonArray(list.stream()
        .map(Result::unwrap)
        .toList()));
  }

  @Override
  public Result<List<T>> unmarshall(JsonValue json) {
    if (json instanceof JsonNull) {
      return new Ok<>(null);
    }
    if (json instanceof JsonArray array) {
      List<Result<T>> list = array.value().stream()
          .map(objectMarshaller::unmarshall)
          .toList();
      if (list.stream().anyMatch(r -> !r.isOk())) {
        @SuppressWarnings("unchecked")
        Result<List<T>> err = (Result<List<T>>) list.stream()
            .filter(r -> !r.isOk())
            .findFirst()
            .orElseThrow();
        return err;
      }
      return new Ok<>(list.stream().map(Result::unwrap).toList());
    }
    return new ExErr<>(new TypeConversionException(json.getClass(), List.class));
  }
}
