package cz.slama.android.gtd.model.util;

import cz.slama.android.gtd.model.Project;
import cz.slama.android.gtd.model.Task;
import cz.slama.android.gtd.model.api.Filter;
import cz.slama.android.gtd.model.interfaces.IObjectTitle;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Drugnanov on 28.3.2015.
 */
public class TaskUtils {

  protected static List<Task> taskList = null;

  protected static Timestamp actualDataTime;

  public TaskUtils() {
  }

  public static void setTaskList(List<Task> taskList) {
    if (taskList == null) {
      taskList = new ArrayList<Task>();
    }
    TaskUtils.taskList = taskList;
    actualDataTime = new Timestamp(new Date().getTime());
  }

  public static List<Task> getTaskList(Filter filter) {
    if (taskList == null) {
      taskList = new ArrayList<Task>();
    }
    List<Task> tasks = new ArrayList<Task>();
    for (Task task : taskList) {
//      filter.contains(StateUtils.getStateEnum(StateUtils.EStateTypes.TASK, task.getState()));
      if (filter.contains(StateUtils.EStateTypes.TASK, task.getState())) {
        tasks.add(task);
      }
    }
    return tasks;
  }

  public static boolean isNewData(Timestamp taskLoaded) {
    if (actualDataTime == null || taskLoaded == null) {
      return true;
    }
    if (actualDataTime.after(taskLoaded)) {
      return true;
    }
    return false;
  }

  public static List<Task> getTaskList(Project projectItem) {
    List<Task> tasks = new ArrayList<Task>();
    if (projectItem == null) {
      return tasks;
    }
    for (Task task : taskList) {
      if (task.getProject() != null) {
        if (task.getProject().getId() == projectItem.getId()) {
          tasks.add(task);
        }
      }
    }
    return tasks;
  }

  public static List<IObjectTitle> getChildTaskList(Filter filter, IObjectTitle parentProject) {
    List<IObjectTitle> tasks = new ArrayList<IObjectTitle>();
    for (Task task : taskList) {
      if (filter.contains(StateUtils.EStateTypes.TASK, task.getState())) {
        if (parentProject == null && task.getProject() == null) {
          tasks.add(task);
        }
        else if (parentProject != null && task.getProject() != null &&
            task.getProject().getId() == parentProject.getId()){
          tasks.add(task);
        }
      }
    }
    return tasks;
  }
}
