package hu.aestallon.jsmol.result;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Err<T, X> implements Result<T> {

  private final X x;

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
    throw new IllegalStateException();
  }

  @Override
  public void ifOk(Consumer<T> c) {
    // NO OP
  }

  @Override
  public boolean isOk() {
    return false;
  }

  @Override
  public void errMatch(Consumer<Object> c) {
    c.accept(x);
  }
}
