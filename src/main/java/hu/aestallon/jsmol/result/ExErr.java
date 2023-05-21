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
package hu.aestallon.jsmol.result;

public class ExErr<T, X extends Exception> extends Err<T, X> {

  public static <T, X extends Exception> ExErr<T, X> of(X x) {
    return new ExErr<>(x);
  }

  public ExErr(X x) {
    super(x);
  }

  @Override
  public T unwrap() {
    throw new IllegalStateException(super.x);
  }
}
