package cz.slama.android.gtd.exceptions;

/**
 * Created by Drugnanov on 28.3.2015.
 */
public class ModelException extends Exception {

  public ModelException() {
  }

  public ModelException(String message) {
    super(message);
  }

  public ModelException(String message, Throwable cause) {
    super(message, cause);
  }

  public ModelException(Throwable cause) {
    super(cause);
  }
}
