package cz.slama.android.gtd.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import cz.slama.android.gtd.ui.MainActivity;

/**
 * Created by Drugnanov on 27.3.2015.
 */
public class ApplicationUtils {

  public static Context getApplicationContext() {
    return getMainActivity().getApplicationContext();
  }


  public static boolean isNetworkAvailable() {
    ConnectivityManager connMgr = (ConnectivityManager)
        getMainActivity().getSystemService(ApplicationUtils.getApplicationContext().CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    Boolean connected = false;
    if (networkInfo != null && networkInfo.isConnected()) {
      connected = true;
    }
    else {
      connected = false;
    }
    return connected;
  }

  public static Context getMainActivity() {
    return MainActivity.mainActivity;
  }
}
