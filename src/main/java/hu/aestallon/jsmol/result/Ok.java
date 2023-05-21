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

public final class Ok<T> implements Result<T> {

  public static <T> Ok<T> of(T t) {
    return new Ok<>(t);
  }

  private final T t;

  public Ok(T t) {
    this.t = t;
  }

  @Override
  public Result<T> or(Supplier<Result<T>> s) {
    return this;
  }

  @Override
  public <U> Result<U> map(CheckedFunction<T, U> f) {
    try {
      return Ok.of(f.apply(t));
    } catch (Exception e) {
      return new ExErr<>(e);
    }
  }

  @Override
  public Optional<T> toOptional() {
    return Optional.of(t);
  }

  @Override
  public <U> Result<U> flatMap(Function<T, Result<U>> f) {
    return f.apply(t);
  }

  @Override
  public T unwrap() {
    return t;
  }

  @Override
  public void ifOk(Consumer<T> c) {
    c.accept(t);
  }

  @Override
  public boolean isOk() {
    return true;
  }

  @Override
  public boolean isErr() {
    return false;
  }

  @Override
  public void errMatch(Consumer<Object> c) {
    // NO OP
  }
}
