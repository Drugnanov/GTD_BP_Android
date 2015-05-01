package cz.slama.android.gtd.utils;

import android.widget.EditText;
import cz.slama.android.gtd.R;

/**
 * Created by Drugnanov on 27.3.2015.
 */
public class ValidateForm {

//  private static Context context;
//
//  public static void Init(Context pContext) {
//    context = pContext;
//  }

  public static boolean checkForm(EditText usernameET, Integer emptyValue, Integer minLengthLogin,
                                  Integer maxLengthLogin,
                                  boolean ok) {
    return checkForm(usernameET, emptyValue, minLengthLogin,
        maxLengthLogin,
        ok, true);
  }

  public static boolean checkForm(EditText usernameET, Integer emptyValue, Integer minLengthLogin,
                                  Integer maxLengthLogin,
                                  boolean ok, boolean withErrorMessages ) {
    boolean errorEmpty = false;
    boolean errorMinLength = false;
    boolean errorMaxLength = false;
    if (emptyValue != null) {
      if (usernameET.getText().length() == emptyValue) {
        errorEmpty = true;
        ok = false;
      }
    }
    if (minLengthLogin != null) {
      if (usernameET.getText().length() < minLengthLogin) {
        errorMinLength = true;

        ok = false;
      }
    }
    if (maxLengthLogin != null) {
      if (usernameET.getText().length() > maxLengthLogin) {
        errorMaxLength = true;
        ok = false;
      }
    }
    if (withErrorMessages) {
      if (errorEmpty) {
        usernameET.setError(ApplicationUtils.getApplicationContext().getString(R.string.error_enter_value));
      }
      if (errorMinLength) {
        usernameET.setError(
            ApplicationUtils.getApplicationContext().getString(R.string.error_value_short) + " " + minLengthLogin);
      }
      if (errorMaxLength) {
        usernameET.setError(
            ApplicationUtils.getApplicationContext().getString(R.string.error_value_too_long) + " " + maxLengthLogin);
      }
    }
    return ok;
  }
}
