package hu.aestallon.jsmol.json;

import java.util.Objects;

public sealed class WrappedValue<T>
    implements JsonValue
    permits JsonArray, JsonBoolean, JsonNumber, JsonObject, JsonString {

  private final T value;

  protected WrappedValue(T value) {
    this.value = Objects.requireNonNull(value);
  }

  public T value() {return value;}
}
