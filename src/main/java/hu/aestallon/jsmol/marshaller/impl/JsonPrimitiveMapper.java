package hu.aestallon.jsmol.marshaller.impl;

import hu.aestallon.jsmol.json.JsonBoolean;
import hu.aestallon.jsmol.json.JsonNull;
import hu.aestallon.jsmol.json.JsonNumber;
import hu.aestallon.jsmol.json.JsonString;
import hu.aestallon.jsmol.json.JsonValue;
import hu.aestallon.jsmol.json.WrappedValue;
import hu.aestallon.jsmol.marshaller.JsonTypeMapper;
import hu.aestallon.jsmol.marshaller.TypeConversionException;
import hu.aestallon.jsmol.result.ExErr;
import hu.aestallon.jsmol.result.Ok;
import hu.aestallon.jsmol.result.Result;

import java.util.function.Function;

public final class JsonPrimitiveMapper<T, W extends WrappedValue<? super T>>
    implements JsonTypeMapper<T> {

  static final JsonPrimitiveMapper<String, JsonString> STRING_MAPPER =
      new JsonPrimitiveMapper<>(
          JsonString::new,
          WrappedValue::value,
          String.class, JsonString.class);

  static final JsonPrimitiveMapper<Integer, JsonNumber> INT_MAPPER =
      new JsonPrimitiveMapper<>(
          JsonNumber::new,
          n -> n.value().intValue(),
          Integer.class, JsonNumber.class);

  static final JsonPrimitiveMapper<Long, JsonNumber> LONG_MAPPER =
      new JsonPrimitiveMapper<>(
          JsonNumber::new,
          n -> n.value().longValue(),
          Long.class, JsonNumber.class);

  static final JsonPrimitiveMapper<Float, JsonNumber> FLOAT_MAPPER =
      new JsonPrimitiveMapper<>(
          JsonNumber::new,
          n -> n.value().floatValue(),
          Float.class, JsonNumber.class);

  static final JsonPrimitiveMapper<Double, JsonNumber> DOUBLE_MAPPER =
      new JsonPrimitiveMapper<>(
          JsonNumber::new,
          n -> n.value().doubleValue(),
          Double.class, JsonNumber.class);

  static final JsonPrimitiveMapper<Boolean, JsonBoolean> BOOLEAN_MAPPER =
      new JsonPrimitiveMapper<>(
          JsonBoolean::new,
          WrappedValue::value,
          Boolean.class, JsonBoolean.class);

  private final Function<T, W> constructor;
  private final Function<W, T> extractor;
  private final Class<T>       type;
  private final Class<W>       wrapperClass;

  public JsonPrimitiveMapper(Function<T, W> constructor, Function<W, T> extractor,
                             Class<T> type, Class<W> wrapperClass) {
    this.constructor = constructor;
    this.extractor = extractor;
    this.type = type;
    this.wrapperClass = wrapperClass;
  }

  @Override
  public Result<JsonValue> marshall(T t) {
    return (t != null)
        ? new Ok<>(constructor.apply(t))
        : new Ok<>(JsonNull.INSTANCE);
  }

  @Override
  public Result<T> unmarshall(JsonValue json) {
    if (json instanceof JsonNull) {
      return new Ok<>(null);
    }
    if (wrapperClass.isInstance(json)) {
      return new Ok<>(extractor.apply(wrapperClass.cast(json)));
    }
    return new ExErr<>(new TypeConversionException(json.getClass(), type));
  }
}
