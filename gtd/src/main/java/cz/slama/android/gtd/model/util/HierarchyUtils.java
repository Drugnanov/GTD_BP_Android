package cz.slama.android.gtd.model.util;

import cz.slama.android.gtd.model.Project;
import cz.slama.android.gtd.model.api.Filter;
import cz.slama.android.gtd.model.interfaces.IObjectTitle;
import cz.slama.android.gtd.ui.adapter.model.Group;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Drugnanov on 28.3.2015.
 */
public class HierarchyUtils {

  protected static ArrayList<Group> hierarchyList = new ArrayList<Group>();

  protected static Timestamp actualDataTime;
  static int index = 0;

  public HierarchyUtils() {
  }

  public static ArrayList<Group> getHierarchyList(Filter filter) {
    hierarchyList.clear();
    HierarchyUtils.index = 0;
    getHierarchyList(filter, null, 0, 0);
    actualDataTime = new Timestamp(new Date().getTime());
    return hierarchyList;
  }

  public static void getHierarchyList(Filter filter, IObjectTitle parentProject, int level, int indexParentParent) {
    int levelNext = level + 1;
    int indexParent = getNextIndex();
    List<IObjectTitle> projects = ProjectUtils.getChildProjectList(filter, parentProject);
    ArrayList<Group> childTasks = getTasks(filter, parentProject, levelNext, indexParent);
    if (parentProject != null) {
      hierarchyList.add(
          new Group(parentProject, notEmpty(projects, childTasks) ? 1 : 0, level, indexParent, indexParentParent, true, "P:"));
    }
    for (IObjectTitle project : projects) {
      getHierarchyList(filter, project, levelNext, indexParent);
    }
    hierarchyList.addAll(childTasks);
  }

  private static boolean notEmpty(List<IObjectTitle> childProjects, ArrayList<Group> childTasks) {
    return (childProjects.size() > 0 || childTasks.size() > 0);
  }

  private static ArrayList<Group> getTasks(Filter filter, IObjectTitle parentProject, int level, int indexParent) {
    ArrayList<Group> groups = new ArrayList<Group>();
    List<IObjectTitle> childTasks = TaskUtils.getChildTaskList(filter, parentProject);
    for (IObjectTitle task : childTasks) {
      Group groupTask = new Group(task, 0, level, getNextIndex(), indexParent, false, "T:");
      groups.add(groupTask);
    }
    return groups;
  }

  private static int getNextIndex() {
    return HierarchyUtils.index++;
  }

  public static boolean isNewData(Timestamp projectLoaded) {
    if (actualDataTime == null || projectLoaded == null) {
      return true;
    }
    if (actualDataTime.after(projectLoaded)) {
      return true;
    }
    return false;
  }

  public static ArrayList<Group> getParentChildren(int parentId) {
    ArrayList<Group> groups = new ArrayList<Group>();
    for(Group group : hierarchyList){
      if (group.getIndexParent() == parentId) {
        groups.add(group);
      }
    }
    return groups;
  }

  public static List<Group> getChildren(Group groupParent) {
    ArrayList<Group> groups = new ArrayList<Group>();
    for(Group group : hierarchyList){
      if (group.getIndexParent() == groupParent.getIndex()) {
        groups.add(group);
      }
    }
    return groups;
  }
}
