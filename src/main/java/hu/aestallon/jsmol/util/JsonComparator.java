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
package hu.aestallon.jsmol.util;

import hu.aestallon.jsmol.json.JsonArray;
import hu.aestallon.jsmol.json.JsonComplex;
import hu.aestallon.jsmol.json.JsonObject;
import hu.aestallon.jsmol.json.JsonValue;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Facilitates the comparison of two JSON values.
 *
 * <p>
 * The
 *
 * @author Szabolcs Bazil Papp
 */
public class JsonComparator {

  public JsonValue compare(JsonValue left, JsonValue right) {
    if (right == null || Objects.equals(left, right)) {
      // right just isn't there (because it has been deleted, or the value was never present), or
      // the value is unchanged => we can exclude it from our result:
      return null;
    }
    if (left == null) {
      // right is present, but left isn't => the entirety of the right value is a change:
      return right;
    }

    // both left and right are present, but they differ => let's find the smallest hierarchical
    // alteration:
    return switch (left) {
      // left is some complex type:
      case JsonComplex<?> complex -> switch (complex) {
        case JsonArray arrLeft -> {
          if (right instanceof JsonArray arrRight) {
            List<JsonValue> resultValues = arrRight.iter()
                .filter(json -> !arrLeft.contains(json))
                .map(json -> this.compare(null, json))
                .filter(Objects::nonNull)
                .toList();
            yield new JsonArray(resultValues);
          } else {
            throw new JsonTypeMismatchException(left, right);
          }
        }
        case JsonObject objLeft -> {
          if (right instanceof JsonObject objRight) {
            Map<String, JsonValue> resultValues = objRight.iter()
                .map(pair -> Pair.of(pair.a(), this.compare(objLeft.get(pair.a()), pair.b())))
                .filter(pair -> pair.b() != null)
                .collect(Pair.toMap());
            yield new JsonObject(resultValues);
          } else {
            throw new JsonTypeMismatchException(left, right);
          }
        }
      };
      // left is some primitive type:
      default -> switch (right) {
        case JsonComplex<?> __ -> throw new JsonTypeMismatchException(left, right);
        default -> {
          // they are both primitives and the value changed => return the right value:
          if (Objects.equals(left.getClass(), right.getClass())) {yield right;}
          // they are both primitive and the type changed => throw:
          throw new JsonTypeMismatchException(left, right);
        }
      };
    };
  }

  public static final class JsonTypeMismatchException extends IllegalStateException {
    JsonTypeMismatchException(JsonValue left, JsonValue right) {
      super("%s is a(n) %s, while %s is a(n) %s. Cannot compare!".formatted(
          left, left.getClass().getSimpleName(),
          right, right.getClass().getSimpleName()));
    }
  }

}
