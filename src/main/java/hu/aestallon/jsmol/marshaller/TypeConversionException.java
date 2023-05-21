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

public class TypeConversionException extends IllegalArgumentException {

  private static final String MSG = "Cannot convert from %s to %s!";

  private final Class<?> from;
  private final Class<?> to;

  public TypeConversionException(Class<?> from, Class<?> to) {
    super(MSG.formatted(from.getSimpleName(), to.getSimpleName()));
    this.from = from;
    this.to = to;
  }

  public Class<?> from() {
    return from;
  }

  public Class<?> to() {
    return to;
  }
}
