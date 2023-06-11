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
package hu.aestallon.jsmol.json;

import hu.aestallon.jsmol.util.Pair;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class JsonObject
    extends WrappedValue<Map<String, JsonValue>>
    implements Map<String, JsonValue>, JsonComplex<Pair<String, JsonValue>> {

  public static final JsonObject EMPTY = new JsonObject(Collections.emptyMap());

  public JsonObject(Map<String, JsonValue> m) {super(m);}

  @Override
  public String toString() {
    return value().entrySet().stream()
        .map(e -> new StringBuilder()
            .append("\"")
            .append(e.getKey())
            .append("\":")
            .append(e.getValue()))
        .collect(Collectors.joining(",", "{", "}"));
  }

  @Override
  public Stream<Pair<String, JsonValue>> iter() {
    return super.value().entrySet().stream().map(Pair::ofEntries);
  }

  @Override
  public int size() {
    return super.value().size();
  }

  @Override
  public boolean isEmpty() {
    return super.value().isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return super.value().containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return super.value().containsValue(value);
  }

  @Override
  public JsonValue get(Object key) {
    return super.value().get(key);
  }

  @Override
  public JsonValue put(String key, JsonValue value) {
    return super.value().put(key, value);
  }

  @Override
  public JsonValue remove(Object key) {
    return super.value().remove(key);
  }

  @Override
  public void putAll(Map<? extends String, ? extends JsonValue> m) {
    super.value().putAll(m);
  }

  @Override
  public void clear() {
    super.value().clear();
  }

  @Override
  public Set<String> keySet() {
    return super.value().keySet();
  }

  @Override
  public Collection<JsonValue> values() {
    return super.value().values();
  }

  @Override
  public Set<Entry<String, JsonValue>> entrySet() {
    return super.value().entrySet();
  }
}
