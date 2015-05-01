package cz.slama.android.gtd.utils;

import android.widget.Spinner;
import cz.slama.android.gtd.model.ContextGtd;
import cz.slama.android.gtd.model.Project;
import cz.slama.android.gtd.model.interfaces.IObjectTitle;

import java.util.List;

/**
 * Created by Drugnanov on 29.3.2015.
 */
public class SpinnerUtils {

  public static List<ContextGtd> addEmptyContextItem(List<ContextGtd> contextGtds) {
    ContextGtd contextGtd = new ContextGtd();
    contextGtd.setTitle(" ");
    if (!contextGtds.contains(contextGtd)) {
      contextGtds.add(0, contextGtd);
    }
    return contextGtds;
  }

  public static List<Project> addEmptyProjectItem(List<Project> projects) {
    Project projectEmpty = new Project();
    projectEmpty.setTitle(" ");
    if (!projects.contains(projectEmpty)) {
      projects.add(0, projectEmpty);
    }
    return projects;
  }

  public static boolean isEmptyObject(IObjectTitle object) {
    if (object == null) {
      return false;
    }
    if (" ".equals(object.getTitle())){
      return true;
    }
    return false;
  }

  private static void universalSpinnerSet(Spinner spinner, List<Object> objectList, Object o) {
    if (o != null) {
      int index = objectList.indexOf(o);
      if (index >= 0) {
        spinner.setSelection(index);
        return;
      }
    }
    spinner.setSelection(0);
  }
}
