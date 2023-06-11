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

import java.util.stream.Stream;

/**
 * Represents a complex JSON value (an object or an array).
 *
 * <p>
 * Complex values contain elements, and thus they are streamable.
 *
 * @param <T> the type representing the sub-elements of the complex JSON object; stands for
 *            JsonValue for arrays and Pair&lt;String, JsonValue&gt; for objects
 *
 * @author Szabolcs Bazil Papp
 */
public sealed interface JsonComplex<T> permits JsonArray, JsonObject {

  /**
   * Returns a sequential {@link Stream} with this complex {@code JSON} value as its source.
   * @return a sequential {@code Stream} over the elements in this complex {@code JSON} value
   */
  Stream<T> iter();

}
