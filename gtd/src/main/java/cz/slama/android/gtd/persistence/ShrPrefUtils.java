package cz.slama.android.gtd.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import cz.slama.android.gtd.model.api.Filter;
import cz.slama.android.gtd.model.util.StateUtils;
import cz.slama.android.gtd.utils.ApplicationUtils;
import cz.slama.android.gtd.utils.Compare;

/**
 * Created by Drugnanov on 22.3.2015.
 */

public class ShrPrefUtils {

  private static Context _context = null;
  private static final String PREFERENCES_FILE_NAME = "gtd.preferences";

  private static void setPreference(String prefNamem, String value) {
    CheckContext();
    _context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(prefNamem, value)
        .commit();
  }

  private static String getPreference(String prefName) {
    CheckContext();
    return _context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
        .getString(prefName, null);
  }

  private static void removePreference(String prefNamem) {
    CheckContext();
    SharedPreferences preferences = _context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
    preferences.edit().remove(prefNamem).commit();
  }

  private static void CheckContext() {
    if (_context == null) {
      _context = ApplicationUtils.getApplicationContext();
    }
  }

  public static void saveUserData(String username, String password, String token) {
    setPreference(ShrPrefEnum.GTD_API_AUTH_LOGIN_NAME.toString(), username);
    setPreference(ShrPrefEnum.GTD_API_AUTH_LOGIN_PASSWORD.toString(), password);
    setPreference(ShrPrefEnum.GTD_API_AUTH_TOKEN.toString(), token);
  }


  public static void removeLogin() {
    removePreference(ShrPrefEnum.GTD_API_AUTH_LOGIN_PASSWORD.toString());
    removePreference(ShrPrefEnum.GTD_API_AUTH_TOKEN.toString());
  }

  public static String getUserToken() {
    return getPreference(ShrPrefEnum.GTD_API_AUTH_TOKEN.toString());
  }

  public static Filter loadFilter(Filter filter) {
    boolean isFilterSaved = isPreferenceSet(ShrPrefEnum.GTD_API_FILTER.toString());
    if (!isFilterSaved) {
      return null;
    }
    for (StateUtils.EStates state : StateUtils.EStates.values()) {
      boolean isSet = isPreferenceSet(state.toString());
      if (isSet) {
        filter.addState(state);
      }
    }
    return filter;
  }

  public static void saveFilter(Filter filter) {
    for (StateUtils.EStates state : StateUtils.EStates.values()) {
      if (filter.contains(state)) {
        setPreference(state.toString(), "set");
      }
      else {
        removePreference(state.toString());
      }
    }
    setPreference(ShrPrefEnum.GTD_API_FILTER.toString(), "set");
  }

  private static boolean isPreferenceSet(String preferenceName) {
    String value = getPreference(preferenceName);
    if (Compare.isNullOrEmpty(value)) {
      return false;
    }
    return true;
  }

  //**************************  Facebook ***************************
  public static boolean isFacebookToken() {
    return isPreferenceSet(ShrPrefEnum.FACEBOOK_TOKEN.toString());
  }

  public static void saveFacebookToken(String token) {
    setPreference(ShrPrefEnum.FACEBOOK_TOKEN.toString(), token);
  }

  public static void removeFacebookToken() {
    removePreference(ShrPrefEnum.FACEBOOK_TOKEN.toString());
  }

  public static String getFacebookToken(){
    return getPreference(ShrPrefEnum.FACEBOOK_TOKEN.toString());
  }

  //**************************  Google ***************************
  public static boolean isGoogleToken() {
    return isPreferenceSet(ShrPrefEnum.GOOGLE_TOKEN.toString());
  }

  public static void saveGoogleToken(String token) {
    setPreference(ShrPrefEnum.GOOGLE_TOKEN.toString(), token);
  }

  public static void removeGoogleToken() {
    removePreference(ShrPrefEnum.GOOGLE_TOKEN.toString());
  }

  public static String getGoogleToken(){
    return getPreference(ShrPrefEnum.GOOGLE_TOKEN.toString());
  }
}
