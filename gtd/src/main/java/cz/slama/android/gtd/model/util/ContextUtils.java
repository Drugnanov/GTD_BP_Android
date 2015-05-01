package cz.slama.android.gtd.model.util;

import android.accounts.AccountsException;
import android.support.v4.app.FragmentActivity;
import cz.slama.android.gtd.BootstrapServiceProvider;
import cz.slama.android.gtd.core.BootstrapService;
import cz.slama.android.gtd.model.ContextGtd;
import cz.slama.android.gtd.model.Task;
import cz.slama.android.gtd.ui.reloading.ReloadStatus;

import javax.inject.Inject;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Drugnanov on 28.3.2015.
 */
public class ContextUtils {

  @Inject
  protected static BootstrapServiceProvider serviceProvider;

  protected static List<ContextGtd> contextList = null;

  protected static Timestamp actualDataTime;

  public ContextUtils() {
  }

  public static void setContextList(List<ContextGtd> contextList) {
    if (contextList == null) {
      contextList = new ArrayList<ContextGtd>();
    }
    ContextUtils.contextList = contextList;
    actualDataTime = new Timestamp(new Date().getTime());
  }

  public static List<ContextGtd> getContextList() {
    if (contextList == null){
      contextList = new ArrayList<ContextGtd>();
    }
    List<ContextGtd> contexts = new ArrayList<ContextGtd>();
    for(ContextGtd context : contextList){
      contexts.add(context);
    }
    return contexts;
  }

  public static boolean isNewData(Timestamp contextLoaded) {
    if (actualDataTime == null || contextLoaded == null){
      return true;
    }
    if (actualDataTime.after(contextLoaded)){
      return true;
    }
    return false;
  }

  public static void loadContextList(BootstrapService bootstrapService) throws IOException, AccountsException {
    if (bootstrapService == null){
      return;
    }
    if (ReloadStatus.isContextToReload()){
      setContextList(bootstrapService.getAllContexts());
    }
    ReloadStatus.setContextToReload(false);
  }
}
