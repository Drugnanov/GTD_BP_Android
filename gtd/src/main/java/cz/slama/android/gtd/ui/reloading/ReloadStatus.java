package cz.slama.android.gtd.ui.reloading;

/**
 * Created by Drugnanov on 19.4.2015.
 */
public class ReloadStatus {
  private static boolean projectToReload = true;
  private static boolean taskToReload = true;
  private static boolean contextToReload = true;
  private static boolean personToReload = true;
  private static boolean hierarchyReload = true;

  public static boolean isProjectToReload() {
    return projectToReload;
  }

  public static void setProjectToReload(boolean projectToReload) {
    if (ReloadStatus.projectToReload && !projectToReload){
      ReloadStatus.hierarchyReload = true;
    }
    ReloadStatus.projectToReload = projectToReload;
  }

  public static boolean isTaskToReload() {
    return taskToReload;
  }

  public static void setTaskToReload(boolean taskToReload) {
    if (ReloadStatus.taskToReload && !taskToReload){
      ReloadStatus.hierarchyReload = true;
    }
    ReloadStatus.taskToReload = taskToReload;
  }

  public static boolean isContextToReload() {
    return contextToReload;
  }

  public static void setContextToReload(boolean contextToReload) {
    ReloadStatus.contextToReload = contextToReload;
  }

  public static boolean isPersonToReload() {
    return personToReload;
  }

  public static void setPersonToReload(boolean personToReload) {
    ReloadStatus.personToReload = personToReload;
  }

  public static boolean isHierarchyReload() {
    return hierarchyReload;
  }

  public static void setHierarchyReload(boolean hierarchyReload) {
    ReloadStatus.hierarchyReload = hierarchyReload;
  }
}
