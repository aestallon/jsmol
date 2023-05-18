package hu.aestallon.jsmol.json;

import java.util.Objects;

public abstract class WrappedValue<T> implements JsonValue {
  private final T value;

  protected WrappedValue(T value) {
    this.value = Objects.requireNonNull(value);
  }

  public T value() {return value;}
}
