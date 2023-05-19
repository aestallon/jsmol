package hu.aestallon.jsmol.json;

public final class JsonNull implements JsonValue {
  public static final JsonNull INSTANCE = new JsonNull();

  @Override
  public String toString() {
    return "null";
  }
}
