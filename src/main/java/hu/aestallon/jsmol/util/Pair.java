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

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public record Pair<A, B>(A a, B b) {

  public static <A, B> Pair<A, B> of(A a, B b) {
    return new Pair<>(a, b);
  }

  public static <A, B> Pair<A, B> ofEntries(Map.Entry<A, B> entry) {
    return Pair.of(entry.getKey(), entry.getValue());
  }

  public static <A, B, R> Function<Pair<A, B>, Pair<A, R>> onB(Function<B, R> f) {
    return pair -> Pair.of(pair.a, f.apply(pair.b));
  }

  public static <A, B, R> Function<Pair<A, B>, Pair<A, R>> onB(BiFunction<A, B, R> f) {
    return pair -> Pair.of(pair.a, f.apply(pair.a, pair.b));
  }

  public static <A, B, R> Function<Pair<A, B>, Pair<R, B>> onA(Function<A, R> f) {
    return pair -> Pair.of(f.apply(pair.a), pair.b);
  }

  public static <A, B> Collector<Pair<A, B>, ?, Map<A, B>> toMap() {
    return Collectors.toMap(Pair::a, Pair::b);
  }

}
