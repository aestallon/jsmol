package hu.aestallon.jsmol.json;

public abstract class WrappedValue<T> implements JsonValue {
  private final T value;

  protected WrappedValue(T value) {
    this.value = value;
  }

  public T value() {return value;}
}
