package hu.aestallon.jsmol.json;

public class JsonString extends WrappedValue<String> {
  public JsonString(String s) {super(s);}

  @Override
  public String toString() {
    return "\"%s\"".formatted(value());
  }

}
