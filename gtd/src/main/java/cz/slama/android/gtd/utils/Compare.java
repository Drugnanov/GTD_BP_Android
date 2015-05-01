package cz.slama.android.gtd.utils;

import cz.slama.android.gtd.R;
import cz.slama.android.gtd.ui.exceptions.ConflictException;
import cz.slama.android.gtd.ui.exceptions.NotFoundException;
import cz.slama.android.gtd.ui.exceptions.UnauthorizedException;
import cz.slama.android.gtd.ui.exceptions.UnknownException;

/**
 * Created by Drugnanov on 22.3.2015.
 */
public class Compare extends ContextClass {

  public static boolean isNullOrEmpty(String string){
    if (string == null || "".equals(string)){
      return true;
    }
    return false;
  }

  public static String checkException(Throwable error){
    CheckContext();
    String errorText = "";
    if (error instanceof NotFoundException) {
      errorText = _context.getString(R.string.error_not_found_user);
    }
    else if (error instanceof UnauthorizedException) {
      errorText = _context.getString(R.string.error_wrong_password);
    }
    else if (error instanceof ConflictException) {
      errorText = _context.getString(R.string.error_conflict);
    }
    else if (error instanceof UnknownException) {
      errorText = _context.getString(R.string.error_unknown);
    }
    else {
      errorText = _context.getString(R.string.error_unknown);
    }
    return errorText;
  }
}
