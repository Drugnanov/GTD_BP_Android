
package cz.slama.android.gtd.core;

import java.util.List;

import cz.slama.android.gtd.model.*;
import retrofit.RestAdapter;
import retrofit.client.Response;

/**
 * Bootstrap API service
 */
public class BootstrapService {

  private RestAdapter restAdapter;

  /**
   * Create bootstrap service
   * Default CTOR
   */
  public BootstrapService() {
  }

  /**
   * Create bootstrap service
   *
   * @param restAdapter The RestAdapter that allows HTTP Communication.
   */
  public BootstrapService(RestAdapter restAdapter) {
    this.restAdapter = restAdapter;
  }

  //used
  //**************
  private UserService getUserService() {
    return getRestAdapter().create(UserService.class);
  }

  private ProjectService getProjectsService() {
    return getRestAdapter().create(ProjectService.class);
  }

  private TaskService getTasksService() {
    return getRestAdapter().create(TaskService.class);
  }

  private ContextService getContextsService() {
    return getRestAdapter().create(ContextService.class);
  }
  //*****************

  private NewsService getNewsService() {
    return getRestAdapter().create(NewsService.class);
  }

  /**
   * Get all bootstrap News that exists on Parse.com
   */
  public List<News> getNews() {
    return getNewsService().getNews().getResults();
  }

  private CheckInService getCheckInService() {
    return getRestAdapter().create(CheckInService.class);
  }

  private RestAdapter getRestAdapter() {
    return restAdapter;
  }

  /**
   * Get all bootstrap Checkins that exists on Parse.com
   */
  public List<CheckIn> getCheckIns() {
    return getCheckInService().getCheckIns().getResults();
  }

  //********************************************************************
  //** NEW*************************************************************
  //*******************************************************************


  public List<Project> getAllProjects() {
    return getProjectsService().getAllProjects();
  }

  public Project createProject(Project project) {
    return getProjectsService().createProject(project);
  }

  public Project updateProject(int projectId, Project project) {
    return getProjectsService().updateProject(projectId, project);
  }

  public void deleteProject(int projectId) {
    Response response = getProjectsService().deleteProject(projectId);
  }

  public List<Task> getAllTasks() {
    return getTasksService().getAllTasks();
  }

  public Task createTask(Task task) {
    return getTasksService().createTask(task);
  }

  public Task updateTask(int taskId, Task task) {
    return getTasksService().updateTask(taskId, task);
  }

  public void deleteTask(int taskId) {
    Response response = getTasksService().deleteTask(taskId);
  }

  public void publishTaskToFacebook(int taskId, TaskFacebookPublish taskFacebookPublish) {
    Response response = getTasksService().publishTaskToFacebook(taskId, taskFacebookPublish);
  }

  public void publishTaskToGoogle(int taskId, TaskGooglePublish taskGooglePublish) {
    Response response = getTasksService().publishTaskToGoogle(taskId, taskGooglePublish);
  }


  public List<ContextGtd> getAllContexts() {
    return getContextsService().getAllContexts();
  }

  public ContextGtd createContext(ContextGtd contextGtd) {
    return getContextsService().createContext(contextGtd);
  }

  public ContextGtd updateContext(int contextId, ContextGtd context) {
    return getContextsService().updateContext(contextId, context);
  }

  public void deleteContext(int contextId) {
    Response response = getContextsService().deleteContext(contextId);
  }

  //****************************************
  //***************    PERSON
  //*****************************************
  public Person getPersonByToken() {
    return getUserService().getUsersByToken();
  }

  public Person updatePerson(int personId, Person person) {
    return getUserService().updatePerson(personId, person);
  }

  public PersonAuth authenticate(String email, String password) {
    return getUserService().authenticate(email, password);
  }

  public Person createPerson(PersonCreate personCreate) {
    return getUserService().createPerson(personCreate);
  }

}