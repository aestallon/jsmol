package hu.aestallon.jsmol.json;

import java.util.Map;
import java.util.stream.Collectors;

public final class JsonObject extends WrappedValue<Map<String, JsonValue>> {
  public JsonObject(Map<String, JsonValue> m) {super(m);}

  @Override
  public String toString() {
    return value().entrySet().stream()
        .map(e -> new StringBuilder()
            .append("\"")
            .append(e.getKey())
            .append("\":")
            .append(e.getValue()))
        .collect(Collectors.joining(",", "{", "}"));
  }
}
