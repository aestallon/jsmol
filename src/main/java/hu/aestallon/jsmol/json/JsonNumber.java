package hu.aestallon.jsmol.json;

public final class JsonNumber extends WrappedValue<Number> {
  public JsonNumber(Number n) {super(n);}

  @Override
  public String toString() {
    return value().toString();
  }
}
