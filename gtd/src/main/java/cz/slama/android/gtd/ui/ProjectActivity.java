package cz.slama.android.gtd.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.github.kevinsawicki.wishlist.Toaster;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import cz.slama.android.gtd.R;

import butterknife.InjectView;
import cz.slama.android.gtd.core.*;
import cz.slama.android.gtd.events.*;
import cz.slama.android.gtd.exceptions.GtdException;
import cz.slama.android.gtd.model.Note;
import cz.slama.android.gtd.model.Project;
import cz.slama.android.gtd.model.State;
import cz.slama.android.gtd.model.Task;
import cz.slama.android.gtd.model.interfaces.IObjectTitle;
import cz.slama.android.gtd.model.util.ProjectUtils;
import cz.slama.android.gtd.model.util.StateUtils;
import cz.slama.android.gtd.model.util.TaskUtils;
import cz.slama.android.gtd.ui.adapter.StableArrayAdapter;
import cz.slama.android.gtd.ui.reloading.ReloadStatus;
import cz.slama.android.gtd.utils.util.SafeAsyncTask;
import cz.slama.android.gtd.utils.*;
import retrofit.RetrofitError;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static cz.slama.android.gtd.core.Constants.Extra.PROJECT_ITEM;
import static cz.slama.android.gtd.core.Constants.Extra.TASK;
import static cz.slama.android.gtd.core.Constants.FormValidation.*;

public class ProjectActivity extends BootstrapActivity {

  @Inject
  BootstrapService bootstrapService;
  @Inject
  Bus bus;
  @Inject
  protected cz.slama.android.gtd.model.api.Filter filter;

  public enum ETypeOfAction {
    PROJECT_CREATE,
    PROJECT_UPDATE,
    PROJECT_DELETE
  }

  private ETypeOfAction actionType;
  private Project projectItem;

  @InjectView(R.id.et_project_name)
  protected EditText titleText;
  @InjectView(R.id.et_project_description)
  protected EditText descriptionText;
  @InjectView(R.id.et_project_note)
  protected EditText noteText;
  @InjectView(R.id.sp_project_parent)
  protected Spinner parentProjectSP;
  @InjectView(R.id.sp_project_state)
  protected Spinner stateSP;

  @InjectView(R.id.b_project_create)
  protected Button actionProjectButton;
  @InjectView(R.id.b_project_delete)
  protected Button deleteProjectButton;

  UniversalSpinAdapter stateAdapter;
  UniversalSpinAdapter projectAdapter;
  List<State> stateList;
  List<Project> projectList;
  Timestamp projectLoaded;

  private SafeAsyncTask<Boolean> actionTask;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.project);

    if (getIntent() != null && getIntent().getExtras() != null) {
      projectItem = (Project) getIntent().getExtras().getSerializable(PROJECT_ITEM);
      if (projectItem == null) {
        actionType = ETypeOfAction.PROJECT_CREATE;
        projectItem = new Project();
      }
      else {
        actionType = ETypeOfAction.PROJECT_UPDATE;
      }
    }
    else {
      actionType = ETypeOfAction.PROJECT_CREATE;
      projectItem = new Project();
    }

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);

    final ListView listview = (ListView) findViewById(R.id.project_parent_listview);
    setParentProject(listview);

    final ListView listview_child_project = (ListView) findViewById(R.id.project_child_projects_listview);
    setChildrenProjects(listview_child_project);

    final ListView listview_child_task = (ListView) findViewById(R.id.project_child_tasks_listview);
    setChildrenTasks(listview_child_task);
  }

  private void setParentProject(ListView listview) {
    List<IObjectTitle> parentProjects = getParentProjects();
    setListAdapter(listview, parentProjects);
  }

  private void setChildrenProjects(ListView listview) {
    List<Project> childrenProjects = getChildrenProjects(projectItem);
    setListAdapter(listview, childrenProjects);
  }

  private void setChildrenTasks(ListView listview) {
    List<Task> childrenTasks = getChildrenTasks(projectItem);
    setListAdapter(listview, childrenTasks);
  }

  private List<Task> getChildrenTasks(Project projectItem) {
    return TaskUtils.getTaskList(projectItem);
  }

  private List<Project> getChildrenProjects(Project projectItem) {
    return ProjectUtils.getProjectList(projectItem);
  }

  private List<IObjectTitle> getParentProjects() {
    ArrayList<IObjectTitle> list = new ArrayList<IObjectTitle>();
    if (projectItem != null) {
      Project project = projectItem.getProject();
      if (project != null) {
        list.add(project);
      }
    }
    return list;
  }

  private void setListAdapter(ListView listview, List<? extends IObjectTitle> childrenProjects) {
    final StableArrayAdapter adapter = new StableArrayAdapter(this,
        R.layout.project_item, childrenProjects);
    listview.setAdapter(adapter);
    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, final View view,
                              int position, long id) {
        Object item = parent.getAdapter().getItem(position);
        redirectToProject(item);
      }
    });
  }


  private void redirectToProject(Object item) {
    if (item instanceof Project) {
      startActivity(new Intent(this, ProjectActivity.class).putExtra(PROJECT_ITEM, (Project) item));
    }
    else if (item instanceof Task) {
      startActivity(new Intent(this, TaskActivity.class).putExtra(TASK, (Task) item));
    }
//    Toaster.showLong(ProjectActivity.this, "klik na project");
  }


  @Override
  protected void onPause() {
    super.onPause();
    bus.unregister(this);
  }

  @Override
  protected void onResume() {
    try {
      fillFragmentFromProject(projectItem);
      SetAction();
    }
    catch (GtdException e) {
      //e.printStackTrace();
    }
    super.onResume();
    bus.register(this);
  }


  public void showErrorText(String errorText) {
    Toaster.showLong(ProjectActivity.this, errorText);
  }

  public void handleDelete(View view) {
    actionType = ETypeOfAction.PROJECT_DELETE;
    handleAction(view);
  }

  public void handleAction(final View view) {
    //cant run more than once at a time
    if (actionTask != null) {
      return;
    }
    final Project project = getProjectFromForms();
    if (project == null) {
      return;
    }
    showProgress();
    actionTask = new SafeAsyncTask<Boolean>() {
      public Boolean call() throws Exception {
        Project projectResponse;
        switch (actionType) {
          case PROJECT_CREATE:
            projectResponse = bootstrapService.createProject(project);
            break;
          case PROJECT_UPDATE:
            projectResponse = bootstrapService.updateProject(project.getId(), project);
            break;
          case PROJECT_DELETE:
            bootstrapService.deleteProject(project.getId());
            break;
        }
        return true;
      }

      @Override
      protected void onException(final Exception e) throws RuntimeException {
        // Retrofit Errors are handled inside of the {
        if (!(e instanceof RetrofitError)) {
          final Throwable cause = e.getCause() != null ? e.getCause() : e;
          if (cause != null) {
            Toaster.showLong(ProjectActivity.this, getString(R.string.label_something_wrong));
          }
//          cause.getMessage()
        }
        actionTask = null;
      }

      @Override
      public void onSuccess(final Boolean authSuccess) {
        ReloadStatus.setProjectToReload(true);
        actionTask = null;
        goHome();
      }

      @Override
      protected void onFinally() throws RuntimeException {
        hideProgress();
        actionTask = null;
      }
    };
    actionTask.execute();
  }

  private void SetAction() {
    setActionButton(actionProjectButton, actionType);
  }

  private void setActionButton(Button taskActionBt, ETypeOfAction typeOfAction) {
    switch (typeOfAction) {
      case PROJECT_CREATE: {
        taskActionBt.setText(getString(R.string.project_create));
        deleteProjectButton.setVisibility(View.GONE);
        break;
      }
      case PROJECT_UPDATE: {
        taskActionBt.setText(getString(R.string.project_update));
        deleteProjectButton.setVisibility(View.VISIBLE);
        break;
      }
    }
  }

  private Project getProjectFromForms() {
    Project project = null;
    try {
      project = fillProjectFromFragmentForm();
    }
    catch (GtdException e) {
      showErrorText(getString(R.string.error_fill_from_form));
      project = null;
    }
    return project;
  }

  private Project fillProjectFromFragmentForm() throws GtdException {
    if (!checkForms()) {
      return null;
    }
    if (projectItem == null) {
      projectItem = new Project();
    }
    projectItem.setTitle(titleText.getText().toString());
    projectItem.setDescription(descriptionText.getText().toString());
    //state
    State stateSelected = (State) stateSP.getSelectedItem();
    projectItem.setState(stateSelected);
    //note
    if (Compare.isNullOrEmpty(noteText.getText().toString())) {
      projectItem.setNotes(null);
    }
    else {
      if (projectItem.getNotes() == null || projectItem.getNotes().size() < 1) {
        List<Note> noteList = new ArrayList<Note>();
        Note note = new Note();
        noteList.add(note);
        projectItem.setNotes(noteList);
      }
      Note note = projectItem.getNotes().get(0);
      note.setText(noteText.getText().toString());
    }
    //project
    Project projectSelected = (Project) parentProjectSP.getSelectedItem();
    if (SpinnerUtils.isEmptyObject(projectSelected)) {
      projectItem.setProject(null);
    }
    else {
      projectItem.setProject(projectSelected);
    }
    return projectItem;
  }

  private void fillFragmentFromProject(Project projectToFill) throws GtdException {
    if (projectToFill == null) {
      setTitle(getString(R.string.title_project_create));
      return;
    }
    setTitle(projectItem.getTitle());

    titleText.setText(projectToFill.getTitle());
    descriptionText.setText(projectToFill.getDescription());
    if (projectToFill.getNotes() != null && projectToFill.getNotes().size() > 0) {
      Note note = projectToFill.getNotes().get(0);
      noteText.setText(note.getText());
    }
    deleteProjectButton.setEnabled(!hasChildren(projectItem));

    stateReload(projectToFill.getState());
    parentProjectsReload(projectToFill.getProject());
  }

  private boolean hasChildren(Project project) {
    return (getChildrenProjects(projectItem).size() > 0 || getChildrenTasks(project).size() > 0);
  }

  private boolean checkForms() throws GtdException {
    boolean ok = true;

    ok = ValidateForm.checkForm(titleText, 0, PROJECT_MIN_LENGTH_TITLE, PROJECT_MAX_LENGTH_TITLE, ok);
    ok = ValidateForm.checkForm(descriptionText, null, null, PROJECT_MAX_LENGTH_DESCRIPTION, ok);

    State stateSelected = (State) stateSP.getSelectedItem();
    if (StateUtils.isEmptyState(stateSelected)) {
      ok = false;
    }
    return ok;
  }

  public void stateReload(State state) throws GtdException {
    if (this.stateList == null || stateAdapter == null) {
      List<State> statesList = StateUtils.getStatesArray(StateUtils.EStateTypes.PROJECT);
      this.stateList = statesList;
      State[] states = statesList.toArray(new State[statesList.size()]);
      stateAdapter = new UniversalSpinAdapter(this,
          R.layout.simple_spinner_item,
          states);
      stateAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
      stateSP.setAdapter(stateAdapter);
    }
    stateSpinnerSet(state);
  }

  private void stateSpinnerSet(State state) {
    if (stateList == null) {
      return;
    }
    if (state != null) {
      int index = stateList.indexOf(state);
      if (index >= 0) {
        stateSP.setSelection(index);
        return;
      }
    }
    if (this.stateList.size() > 0) {
      stateSP.setSelection(0);
    }
  }

  public void parentProjectsReload(Project parentProject) {
    if (this.projectList == null || projectAdapter == null
        || ProjectUtils.isNewData(projectLoaded)) {
      projectList = ProjectUtils.getProjectList(filter, projectItem);
      SpinnerUtils.addEmptyProjectItem(projectList);
      Project[] projects = projectList.toArray(new Project[projectList.size()]);
      projectAdapter = new UniversalSpinAdapter(this,
          R.layout.simple_spinner_item,
          projects);
      projectAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
      parentProjectSP.setAdapter(projectAdapter);
      projectLoaded = new Timestamp(new Date().getTime());
    }
    projectSpinnerSet(parentProject);
  }

  private void projectSpinnerSet(Project projectParent) {
    if (this.projectList == null) {
      return;
    }
    if (projectParent != null) {
      int index = this.projectList.indexOf(projectParent);
      if (index >= 0) {
        parentProjectSP.setSelection(index);
        return;
      }
    }
    if (this.projectList.size() > 0) {
      parentProjectSP.setSelection(0);
    }
  }

  /**
   * Hide progress dialog
   */
  @SuppressWarnings("deprecation")
  protected void hideProgress() {
    dismissDialog(0);
  }

  /**
   * Show progress dialog
   */
  @SuppressWarnings("deprecation")
  protected void showProgress() {
    showDialog(0);
  }

  @Subscribe
  public void onUnAuthorizedErrorEvent(UnAuthorizedErrorEvent unAuthorizedErrorEvent) {
    Toaster.showLong(ProjectActivity.this, R.string.message_bad_credentials);
  }

  @Subscribe
  public void onNetworkErrorEvent(NetworkErrorEvent networkErrorEvent) {
    Toaster.showLong(ProjectActivity.this, R.string.message_bad_network);
  }

  @Subscribe
  public void onRestAdapterErrorEvent(RestAdapterErrorEvent restAdapterErrorEvent) {
    Toaster.showLong(ProjectActivity.this, R.string.message_bad_restRequest);
  }

  @Subscribe
  public void onBadRequestErrorEvent(BadRequestErrorEvent badRequestErrorEvent) {
    Toaster.showLong(ProjectActivity.this, R.string.message_bad_restRequest);
  }

  @Subscribe
  public void onAlreadyReportedErrorEvent(AlreadyReportedErrorEvent alreadyReportedErrorEvent) {
    Toaster.showLong(ProjectActivity.this, R.string.message_bad_universal);
  }
}
