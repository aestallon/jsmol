package hu.aestallon.jsmol.json;

public final class JsonBoolean extends WrappedValue<Boolean> {
  public JsonBoolean(boolean b) {super(b);}

  @Override
  public String toString() {
    return value().toString();
  }
}
