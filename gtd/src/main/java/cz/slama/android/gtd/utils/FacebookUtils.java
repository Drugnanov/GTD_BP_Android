package cz.slama.android.gtd.utils;

import com.facebook.Profile;

/**
 * Created by Drugnanov on 25.4.2015.
 */
public class FacebookUtils {

  public static boolean isUserLogIn() {
    Profile profile = Profile.getCurrentProfile().getCurrentProfile();
    if (profile == null) {
      return false;
    }
    else{
      return true;
    }
  }
}
