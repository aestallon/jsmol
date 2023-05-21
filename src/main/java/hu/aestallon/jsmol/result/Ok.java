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
