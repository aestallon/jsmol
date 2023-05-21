/*
 * Copyright 2023 Szabolcs Bazil Papp
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
