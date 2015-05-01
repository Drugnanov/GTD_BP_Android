package cz.slama.android.gtd.utils;

import android.content.Context;

/**
 * Created by Drugnanov on 28.3.2015.
 */
public class ContextClass {

  protected static Context _context = null;

  protected static void CheckContext() {
    if (_context == null){
      _context = ApplicationUtils.getApplicationContext();
    }
  }
}
