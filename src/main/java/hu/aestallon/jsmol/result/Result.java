package hu.aestallon.jsmol.result;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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

  Result<T> or(Supplier<Result<T>> s);

  <U> Result<U> map(CheckedFunction<T, U> f);

  <U> Result<U> flatMap(Function<T, Result<U>> f);

  Optional<T> toOptional();

  T unwrap();

  boolean isOk();

  void ifPresent(Consumer<T> c);

  void errMatch(Consumer<Object> c);

}
