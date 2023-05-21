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

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Err<T, X> implements Result<T> {

  public static <T, X> Err<T, X> of(X x) {
    return new Err<>(x);
  }

  protected final X x;

  public Err(X x) {
    this.x = x;
  }

  @Override
  public Result<T> or(Supplier<Result<T>> s) {
    return s.get();
  }

  @Override
  public <U> Result<U> map(CheckedFunction<T, U> f) {
    return new Err<>(x);
  }


  @Override
  public <U> Result<U> flatMap(Function<T, Result<U>> f) {
    return new Err<>(x);
  }

  @Override
  public Optional<T> toOptional() {
    return Optional.empty();
  }

  @Override
  public T unwrap() {
    throw new IllegalStateException(x.toString());
  }

  @Override
  public void ifOk(Consumer<T> c) {
    // NO OP
  }

  @Override
  public final boolean isOk() {
    return false;
  }

  @Override
  public final boolean isErr() {
    return true;
  }

  @Override
  public void errMatch(Consumer<Object> c) {
    c.accept(x);
  }
}
