package hu.aestallon.jsmol.parser;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Marker interface for value types interpreted in JavaScript Object Notation (JSON).
 *
 * @author Szabolcs Bazil Papp
 */
public interface JsonValue {

  abstract class WrappedValue<T> implements JsonValue {
    T value;

    protected WrappedValue(T value) {
      this.value = value;
    }

    public T value() {return value;}
  }

  class JsonNull implements JsonValue {
    @Override
    public String toString() {
      return "null";
    }
  }

  class JsonNumber extends WrappedValue<Number> {
    public JsonNumber(Number n) {super(n);}

    @Override
    public String toString() {
      return "number(%s)".formatted(value.toString());
    }
  }

  class JsonBoolean extends WrappedValue<Boolean> {
    public JsonBoolean(boolean b) {super(b);}

    @Override
    public String toString() {
      return "boolean(%s)".formatted(value.toString());
    }
  }

  class JsonString extends WrappedValue<String> {
    public JsonString(String s) {super(s);}

    @Override
    public String toString() {
      return "string(\"%s\")".formatted(value);
    }

  }

  class JsonArray extends WrappedValue<List<JsonValue>> {
    public JsonArray(List<JsonValue> l) {super(l);}

    @Override
    public String toString() {
      return value().stream()
          .map(Objects::toString)
          .collect(Collectors.joining(",\n", "array([\n", "])"));
    }
  }

  class JsonObject extends WrappedValue<Map<String, JsonValue>> {
    public JsonObject(Map<String, JsonValue> m) {super(m);}

    @Override
    public String toString() {
      return value().entrySet().stream()
          .map(e -> e.getKey() + " : " + e.getValue().toString())
          .collect(Collectors.joining(",\n", "object({\n", "})"));
    }
  }
}
