package cz.slama.android.gtd.exceptions;

/**
 * Created by Drugnanov on 28.3.2015.
 */
public class GtdException extends Exception {

  public GtdException() {
  }

  public GtdException(String message) {
    super(message);
  }

  public GtdException(String message, Throwable cause) {
    super(message, cause);
  }

  public GtdException(Throwable cause) {
    super(cause);
  }
}
