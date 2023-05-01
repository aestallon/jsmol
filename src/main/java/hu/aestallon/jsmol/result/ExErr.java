package hu.aestallon.jsmol.result;

public class ExErr<T, X extends Exception> extends Err<T, X> {
  public ExErr(X x) {
    super(x);
  }
}
