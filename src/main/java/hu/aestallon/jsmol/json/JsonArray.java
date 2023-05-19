package hu.aestallon.jsmol.json;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class JsonArray extends WrappedValue<List<JsonValue>> {
  public JsonArray(List<JsonValue> l) {super(l);}

  @Override
  public String toString() {
    return value().stream()
        .map(Objects::toString)
        .collect(Collectors.joining(",", "[", "]"));
  }
}
