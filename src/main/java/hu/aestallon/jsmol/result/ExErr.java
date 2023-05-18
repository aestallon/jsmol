package hu.aestallon.jsmol.result;

public class ExErr<T, X extends Exception> extends Err<T, X> {

  public static <T, X extends Exception> ExErr<T, X> of(X x) {
    return new ExErr<>(x);
  }

  public ExErr(X x) {
    super(x);
  }

  @Override
  public T unwrap() {
    throw new IllegalStateException(super.x);
  }
}
