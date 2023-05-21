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

import hu.aestallon.jsmol.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public interface Result<T> {

  @FunctionalInterface
  interface CheckedSupplier<T> {
    T get() throws Exception;
  }

  @FunctionalInterface
  interface CheckedFunction<T, U> {
    U apply(T t) throws Exception;

  }

  static <T> Result<T> of(CheckedSupplier<T> s) {
    try {
      return Ok.of(s.get());
    } catch (Exception t) {
      return new ExErr<>(t);
    }
  }

  @SuppressWarnings("unchecked")
  private static <E> Result<List<E>> invertList(List<Result<E>> c) {
    return (c.stream().anyMatch(Result::isErr))
        ? (Result<List<E>>) c.stream().filter(Result::isErr).findFirst().orElseThrow()
        : Ok.of(c.stream().map(Result::unwrap).toList());
  }

  static <E> Collector<Result<E>, ?, Result<List<E>>> toList() {
    return Collectors.collectingAndThen(Collectors.toList(), Result::invertList);
  }

  @SuppressWarnings("unchecked")
  private static <K, V> Result<Map<K, V>> invertMap(Map<K, Result<V>> m) {
    return (m.values().stream().anyMatch(Result::isErr))
        ? (Result<Map<K, V>>) m.values().stream()
            .filter(Result::isErr)
            .findFirst()
            .orElseThrow()
        : Ok.of(m.entrySet().stream()
            .map(Pair::ofEntries)
            .map(Pair.onB(Result::unwrap))
            .collect(Pair.toMap()));
  }

  static <K, E> Collector<Pair<K, Result<E>>, ?, Result<Map<K, E>>> ofMap() {
    return Collectors.collectingAndThen(Pair.toMap(), Result::invertMap);
  }

  static <K, E> Collector<Map.Entry<K, Result<E>>, ?, Result<Map<K, E>>> ofEntries() {
    return Collectors.collectingAndThen(
        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue),
        Result::invertMap);
  }

  Result<T> or(Supplier<Result<T>> s);

  <U> Result<U> map(CheckedFunction<T, U> f);

  <U> Result<U> flatMap(Function<T, Result<U>> f);

  Optional<T> toOptional();

  T unwrap();

  boolean isOk();

  boolean isErr();

  void ifOk(Consumer<T> c);

  void errMatch(Consumer<Object> c);

}
