package cz.slama.android.gtd.utils;

import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Drugnanov on 12.4.2015.
 */
public class ActivityUtils {

  public static String getStringFromExtras(Intent intent, Bundle savedInstanceState, String stringName) {
    String newString = null;
    Bundle extras;
    if (savedInstanceState == null) {
      extras = intent.getExtras();
      if (extras == null) {
        newString = null;
      }
      else {
        newString = extras.getString(stringName);
      }
    }
    else {
      newString = (String) savedInstanceState.getSerializable(stringName);
    }
    return newString;
  }

  public static String getStringFromExtras(Intent intent, String stringName) {
    String newString = null;
    Bundle extras;
    extras = intent.getExtras();
    if (extras == null) {
      newString = null;
    }
    else {
      newString = extras.getString(stringName);
    }
    return newString;
  }

  public enum CallbackTypes {
    SUCCESS,
    CANCEL,
    ERROR
  }
}
