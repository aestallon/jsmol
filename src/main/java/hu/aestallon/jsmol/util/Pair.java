package hu.aestallon.jsmol.util;

import java.util.Map;
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

  public static <A, B> Collector<Pair<A, B>, ?, Map<A, B>> toMap() {
    return Collectors.toMap(Pair::a, Pair::b);
  }


}
