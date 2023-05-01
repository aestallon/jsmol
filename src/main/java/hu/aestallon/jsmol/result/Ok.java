package hu.aestallon.jsmol.result;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Ok<T> implements Result<T> {

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
      return new Ok<>(f.apply(t));
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
  public void ifPresent(Consumer<T> c) {
    c.accept(t);
  }

  @Override
  public boolean isOk() {
    return true;
  }

  @Override
  public void errMatch(Consumer<Object> c) {
    // NO OP
  }
}