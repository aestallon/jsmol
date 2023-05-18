package hu.aestallon.jsmol.result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
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
      return new Ok<>(s.get());
    } catch (Exception t) {
      return new ExErr<>(t);
    }
  }

  @SuppressWarnings("unchecked")
  private static <E> Result<List<E>> invertList(List<Result<E>> c) {
    return (c.stream().anyMatch(Result::isErr))
        ? (Result<List<E>>) c.stream().filter(Result::isErr).findFirst().orElseThrow()
        : new Ok<>(c.stream().map(Result::unwrap).toList());
  }

  static <E> Collector<Result<E>, ?, Result<List<E>>> toList() {
    return Collectors.collectingAndThen(Collectors.toList(), Result::invertList);
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
