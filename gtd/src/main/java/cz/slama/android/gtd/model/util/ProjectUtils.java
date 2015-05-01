package cz.slama.android.gtd.model.util;

import cz.slama.android.gtd.model.Project;
import cz.slama.android.gtd.model.api.Filter;
import cz.slama.android.gtd.model.interfaces.IObjectTitle;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Drugnanov on 28.3.2015.
 */
public class ProjectUtils {

  protected static List<Project> projectList = null;

  protected static Timestamp actualDataTime;

  public ProjectUtils() {
  }

  public static void setProjectList(List<Project> projectList) {
    if (projectList == null) {
      projectList = new ArrayList<Project>();
    }
    ProjectUtils.projectList = projectList;
    actualDataTime = new Timestamp(new Date().getTime());
  }

  public static List<Project> getProjectList(Filter filter, Project projectItem) {
    if (projectList == null) {
      projectList = new ArrayList<Project>();
    }
    List<Project> projects = new ArrayList<Project>();
    for (Project project : projectList) {
      if (filter.contains(StateUtils.EStateTypes.PROJECT, project.getState())) {
        if (projectItem == null || !projectItem.equals(project)) {
          projects.add(project);
        }
      }
    }
    return projects;
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

  public static List<Project> getProjectList(Filter filter) {
    return getProjectList(filter, null);
  }

  public static List<Project> getProjectList(Project projectItem) {
    List<Project> projects = new ArrayList<Project>();
    if (projectItem == null) {
      return projects;
    }
    for (Project project : projectList) {
      if (project.getProject() != null){
        if (project.getProject().getId() == projectItem.getId()) {
          projects.add(project);
        }
      }
    }
    return projects;
  }

  public static List<IObjectTitle> getChildProjectList(Filter filter, IObjectTitle parentProject) {
    List<IObjectTitle> projects = new ArrayList<IObjectTitle>();
    for (Project project : projectList) {
      if (filter.contains(StateUtils.EStateTypes.PROJECT, project.getState())) {
        if (parentProject == null && project.getProject() == null) {
          projects.add(project);
        }
        else if (parentProject != null && project.getProject() != null &&
            project.getProject().getId() == parentProject.getId()){
          projects.add(project);
        }
      }
    }
    return projects;
  }
}
