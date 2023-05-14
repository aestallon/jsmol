package hu.aestallon.jsmol.marshaller;

public class TypeConversionException extends IllegalArgumentException {

  private static final String MSG = "Cannot convert from %s to %s!";

  private final Class<?> from;
  private final Class<?> to;

  public TypeConversionException(Class<?> from, Class<?> to) {
    super(MSG.formatted(from.getSimpleName(), to.getSimpleName()));
    this.from = from;
    this.to = to;
  }
  
}
