package cz.slama.android.gtd.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import butterknife.InjectView;
import com.github.kevinsawicki.wishlist.Toaster;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import cz.slama.android.gtd.R;
import cz.slama.android.gtd.core.BootstrapService;
import cz.slama.android.gtd.events.*;
import cz.slama.android.gtd.exceptions.GtdException;
import cz.slama.android.gtd.model.*;
import cz.slama.android.gtd.model.interfaces.IObjectTitle;
import cz.slama.android.gtd.model.util.ContextUtils;
import cz.slama.android.gtd.model.util.ProjectUtils;
import cz.slama.android.gtd.model.util.StateUtils;
import cz.slama.android.gtd.persistence.ShrPrefUtils;
import cz.slama.android.gtd.ui.adapter.StableArrayAdapter;
import cz.slama.android.gtd.ui.exceptions.TokenExpiredException;
import cz.slama.android.gtd.ui.reloading.ReloadStatus;
import cz.slama.android.gtd.utils.util.SafeAsyncTask;
import cz.slama.android.gtd.utils.*;
import retrofit.RetrofitError;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static cz.slama.android.gtd.core.Constants.Extra.PROJECT_ITEM;
import static cz.slama.android.gtd.core.Constants.Extra.TASK;
import static cz.slama.android.gtd.core.Constants.FormValidation.*;

public class TaskActivity extends BootstrapActivity implements AdapterView.OnItemSelectedListener {

  @Inject
  BootstrapService bootstrapService;
  @Inject
  Bus bus;
  @Inject
  protected cz.slama.android.gtd.model.api.Filter filter;

  public enum ETypeOfAction {
    TASK_CREATE,
    TASK_UPDATE,
    TASK_DELETE
  }

  private ETypeOfAction actionType;
  private Task taskItem;

  @InjectView(R.id.et_task_name)
  protected EditText titleText;
  @InjectView(R.id.et_task_description)
  protected EditText descriptionText;
  @InjectView(R.id.et_task_date_from)
  protected EditText dateFromText;
  @InjectView(R.id.et_task_date_to)
  protected EditText dateToText;
  @InjectView(R.id.et_task_note)
  protected EditText noteText;

  @InjectView(R.id.sp_task_parent)
  protected Spinner taskParentSP;
  @InjectView(R.id.sp_task_state)
  protected Spinner stateSP;
  @InjectView(R.id.sp_task_context)
  protected Spinner contextSP;

  @InjectView(R.id.b_task_create)
  protected Button actionTaskButton;
  @InjectView(R.id.b_task_delete)
  protected Button deleteTaskButton;
  @InjectView(R.id.b_task_fast_done)
  protected Button fastDoneButton;

  @InjectView(R.id.b_task_social_post_facebook)
  protected Button postFacebookButton;
  @InjectView(R.id.b_task_social_post_google)
  protected Button postGoogleButton;
  @InjectView(R.id.b_task_social_login_facebook)
  protected Button loginFacebookButton;
  @InjectView(R.id.b_task_social_login_google)
  protected Button loginGoogleButton;

  @InjectView(R.id.tv_task_date_from)
  protected TextView dateFromTV;
  @InjectView(R.id.tv_task_date_to)
  protected TextView dateToTV;

  UniversalSpinAdapter stateAdapter;
  UniversalSpinAdapter projectAdapter;
  UniversalSpinAdapter contextAdapter;
  List<State> stateList;
  List<Project> projectList;
  List<ContextGtd> contextList;
  Timestamp projectLoaded;
  Timestamp contextLoaded;

  private SafeAsyncTask<Boolean> actionTask;
  private SafeAsyncTask<Boolean> postFacebookTask;
  private SafeAsyncTask<Boolean> postGoogleTask;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.task);
    boolean postFacebook = false;
    boolean postGoogle = false;
    if (getIntent() != null && getIntent().getExtras() != null) {
      taskItem = (Task) getIntent().getExtras().getSerializable(TASK);
      if (taskItem == null) {
        actionType = ETypeOfAction.TASK_CREATE;
        taskItem = new Task();
      }
      else {
        actionType = ETypeOfAction.TASK_UPDATE;
        try {
          postGoogle = ShrPrefUtils.isGoogleToken();
          if (!StateUtils.isInCalendarState(taskItem.getState())) {
            postGoogle = false;
          }
        }
        catch (GtdException e) {
          postGoogle = false;
        }
      }
      postFacebook = FacebookUtils.isUserLogIn();
    }
    else {
      postFacebook = false;
      postGoogle = false;
      actionType = ETypeOfAction.TASK_CREATE;
      taskItem = new Task();
    }
    showPostButtons(postFacebook, postGoogle);
    showLoginButtons(true, true);

    final Activity activity = this;
    dateFromText.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        DialogBox.showDateTimePicker(v, activity);
        v.clearFocus();
      }
    });
    dateToText.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        DialogBox.showDateTimePicker(v, activity);
        v.clearFocus();
      }
    });
    stateSP.setOnItemSelectedListener(this);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);

    final ListView listview = (ListView) findViewById(R.id.task_parent_project_listview);

    List<IObjectTitle> projects = getParentProjects();
    final StableArrayAdapter adapter = new StableArrayAdapter(this,
        R.layout.project_item, projects);
    listview.setAdapter(adapter);
    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, final View view,
                              int position, long id) {
        Project item = (Project) parent.getAdapter().getItem(position);
        redirectToProject(item);
      }
    });
  }

  private void redirectToProject(Project item) {
    startActivity(new Intent(this, ProjectActivity.class).putExtra(PROJECT_ITEM, item));
//    Toaster.showLong(ProjectActivity.this, "klik na project");
  }

  private List<IObjectTitle> getParentProjects(){
    ArrayList<IObjectTitle> list = new ArrayList<IObjectTitle>();
    if (taskItem != null) {
      Project project = taskItem.getProject();
      if (project != null) {
        list.add(project);
      }
    }
    return list;
  }

  @Override
  protected void onPause() {
    super.onPause();
    bus.unregister(this);
  }

  @Override
  protected void onResume() {
    try {
      fillFragmentFromTask(taskItem);
      SetAction();
    }
    catch (GtdException e) {
//            e.printStackTrace();
    }
    super.onResume();
    bus.register(this);
  }


  public void showErrorText(String errorText) {
    Toaster.showLong(TaskActivity.this, errorText);
  }

  public void handleFacebook(View view) {
    final Task task = taskItem;
    startActivity(new Intent(this, FacebookActivity.class).putExtra(TASK, task));
  }

  public void handleGoogle(View view) {
    final Task task = taskItem;
    startActivity(new Intent(this, GoogleActivity.class).putExtra(TASK, task));
  }

  public void handlePostGoogle(View view) {
    //cant run more than once at a time
    if (postGoogleTask != null) {
      return;
    }
    if (taskItem == null || taskItem.getId() < 1) {
      Toaster.showLong(TaskActivity.this, getString(R.string.error_task_post_no_id));
      return;
    }
    showProgress();
    postGoogleTask = new SafeAsyncTask<Boolean>() {
      public Boolean call() throws Exception {
        TaskGooglePublish taskGooglePublish = new TaskGooglePublish();
        taskGooglePublish.setAccessToken(ShrPrefUtils.getGoogleToken());
        bootstrapService.publishTaskToGoogle(taskItem.getId(), taskGooglePublish);
        return true;
      }

      @Override
      protected void onException(final Exception e) throws RuntimeException {
        // Retrofit Errors are handled inside of the {
//        Toaster.showLong(TaskActivity.this, getString(R.string.label_something_wrong));
        if (!(e instanceof RetrofitError)) {
          if (e.getCause() != null && (e.getCause() instanceof TokenExpiredException)) {
            Toaster.showLong(TaskActivity.this, R.string.error_token_expired);
            ShrPrefUtils.removeGoogleToken();
            finish();
            startActivity(getIntent());
          }
          final Throwable cause = e.getCause() != null ? e.getCause() : e;
          if (cause != null) {
            Toaster.showLong(TaskActivity.this, getString(R.string.label_something_wrong));
          }
        }
        postGoogleTask = null;
      }

      @Override
      public void onSuccess(final Boolean authSuccess) {
        postGoogleTask = null;
        Toaster.showLong(TaskActivity.this, getString(R.string.task_post_google_success));
      }

      @Override
      protected void onFinally() throws RuntimeException {
        hideProgress();
        postGoogleTask = null;
      }
    };
    postGoogleTask.execute();
  }

  public void handlePostFacebook(View view) {
    //cant run more than once at a time
    if (postFacebookTask != null) {
      return;
    }
    if (taskItem == null || taskItem.getId() < 1) {
      Toaster.showLong(TaskActivity.this, getString(R.string.error_task_post_no_id));
      return;
    }
    showProgress();
    postFacebookTask = new SafeAsyncTask<Boolean>() {
      public Boolean call() throws Exception {
        TaskFacebookPublish taskFacebookPublish = new TaskFacebookPublish();
        taskFacebookPublish.setAccessToken(ShrPrefUtils.getFacebookToken());
        bootstrapService.publishTaskToFacebook(taskItem.getId(), taskFacebookPublish);
        return true;
      }

      @Override
      protected void onException(final Exception e) throws RuntimeException {
        // Retrofit Errors are handled inside of the {
//        Toaster.showLong(TaskActivity.this, getString(R.string.label_something_wrong));
        if (!(e instanceof RetrofitError)) {
          if (e.getCause() != null && (e.getCause() instanceof TokenExpiredException)) {
            Toaster.showLong(TaskActivity.this, R.string.error_token_expired);
            ShrPrefUtils.removeFacebookToken();
            finish();
            startActivity(getIntent());
          }
          final Throwable cause = e.getCause() != null ? e.getCause() : e;
          if (cause != null) {
            Toaster.showLong(TaskActivity.this, getString(R.string.label_something_wrong));
          }
        }
        postFacebookTask = null;
      }

      @Override
      public void onSuccess(final Boolean authSuccess) {
        postFacebookTask = null;
        Toaster.showLong(TaskActivity.this, getString(R.string.task_post_facebook_success));
      }

      @Override
      protected void onFinally() throws RuntimeException {
        hideProgress();
        postFacebookTask = null;
      }
    };
    postFacebookTask.execute();
  }

  public void handleDelete(View view) {
    actionType = ETypeOfAction.TASK_DELETE;
    handleAction(view);
  }

  public void handleAction(final View view) {
    //cant run more than once at a time
    if (actionTask != null) {
      return;
    }
    final Task task = getTaskFromForms();
    if (task == null) {
      return;
    }
    showProgress();
    actionTask = new SafeAsyncTask<Boolean>() {
      public Boolean call() throws Exception {
        cz.slama.android.gtd.model.Task taskResponse;
        switch (actionType) {
          case TASK_CREATE:
            taskResponse = bootstrapService.createTask(task);
            break;
          case TASK_UPDATE:
            taskResponse = bootstrapService.updateTask(task.getId(), task);
            break;
          case TASK_DELETE:
            bootstrapService.deleteTask(task.getId());
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
            Toaster.showLong(TaskActivity.this, getString(R.string.label_something_wrong));
          }
        }
        actionTask = null;
      }

      @Override
      public void onSuccess(final Boolean authSuccess) {
        ReloadStatus.setTaskToReload(true);
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

  public void handleFastDone(View view) {
    State state = null;
    try {
      state = StateUtils.getState(StateUtils.EStateTypes.TASK, StateUtils.EStates.TASK_DONE);
      stateReload(state);
      handleAction(view);
    }
    catch (GtdException e) {
      Toaster.showLong(TaskActivity.this, R.string.error_task_fast_done);
      return;
    }
  }

  private void SetAction() {
    setActionButton(actionTaskButton, actionType);
  }

  private void setActionButton(Button taskActionBt, ETypeOfAction typeOfAction) {
    switch (typeOfAction) {
      case TASK_CREATE: {
        taskActionBt.setText(getString(R.string.task_create));
        deleteTaskButton.setVisibility(View.GONE);
        fastDoneButton.setVisibility(View.GONE);
        break;
      }
      case TASK_UPDATE: {
        taskActionBt.setText(getString(R.string.task_update));
        deleteTaskButton.setVisibility(View.VISIBLE);
        try {
          if (!taskItem.getState().getCode()
              .equals(StateUtils.getState(StateUtils.EStateTypes.TASK, StateUtils.EStates.TASK_DONE).getCode())
              ) {
            fastDoneButton.setVisibility(View.VISIBLE);
          }
          else {
            fastDoneButton.setVisibility(View.GONE);
          }
        }
        catch (Throwable e) {
          fastDoneButton.setVisibility(View.GONE);
        }
        break;
      }
    }
  }

  private Task getTaskFromForms() {
    Task task = null;
    try {
      task = fillTaskFromFragmentForm();
    }
    catch (GtdException e) {
      showErrorText(getString(R.string.error_fill_from_form));
      task = null;
    }
    return task;
  }

  private Task fillTaskFromFragmentForm() throws GtdException {
    if (!checkForms()) {
      return null;
    }
    if (taskItem == null) {
      taskItem = new Task();
    }
    taskItem.setTitle(titleText.getText().toString());
    taskItem.setDescription(descriptionText.getText().toString());
    //state
    State stateSelected = (State) stateSP.getSelectedItem();
    taskItem.setState(stateSelected);
    if (StateUtils.isInCalendarState(stateSelected)) {
      if (taskItem.getCalendar() == null) {
        taskItem.setCalendar(new Calendar());
      }
      taskItem.getCalendar().setFrom(Converter.getMillisecondsFromString(dateFromText.getText().toString()));
      taskItem.getCalendar().setTo(Converter.getMillisecondsFromString(dateToText.getText().toString()));
    }
    //note
    if (Compare.isNullOrEmpty(noteText.getText().toString())) {
      taskItem.setNotes(null);
    }
    else {
      if (taskItem.getNotes() == null || taskItem.getNotes().size() < 1) {
        List<Note> noteList = new ArrayList<Note>();
        Note note = new Note();
        noteList.add(note);
        taskItem.setNotes(noteList);
      }
      Note note = taskItem.getNotes().get(0);
      note.setText(noteText.getText().toString());
    }
    //context
    ContextGtd contextSelected = (ContextGtd) contextSP.getSelectedItem();
    if (SpinnerUtils.isEmptyObject(contextSelected)) {
      taskItem.setContext(null);
    }
    else {
      taskItem.setContext(contextSelected);
    }
    //project
    Project projectSelected = (Project) taskParentSP.getSelectedItem();
    if (SpinnerUtils.isEmptyObject(projectSelected)) {
      taskItem.setProject(null);
    }
    else {
      taskItem.setProject(projectSelected);
    }
    return taskItem;
  }

  private void fillFragmentFromTask(Task taskToFill) throws GtdException {
    if (taskToFill == null) {
      setTitle(getString(R.string.title_task_create));
      return;
    }
    setTitle(taskToFill.getTitle());

    titleText.setText(taskToFill.getTitle());
    descriptionText.setText(taskToFill.getDescription());
    if (taskToFill.getCalendar() != null) {
      dateFromText.setText(Converter.getDateTimeStringFromMinutes(taskToFill.getCalendar().getFrom()));
      dateToText.setText(Converter.getDateTimeStringFromMinutes(taskToFill.getCalendar().getTo()));
    }
    if (taskToFill.getNotes() != null && taskToFill.getNotes().size() > 0) {
      Note note = taskToFill.getNotes().get(0);
      noteText.setText(note.getText());
    }
    stateReload(taskToFill.getState());
    parentProjectsReload(taskToFill.getProject());
    contextReload(taskToFill.getContext());
  }

  private boolean checkForms() throws GtdException {
    boolean ok = true;

    ok = ValidateForm.checkForm(titleText, 0, TASK_MIN_LENGTH_TITLE, TASK_MAX_LENGTH_TITLE, ok);
    ok = ValidateForm.checkForm(descriptionText, null, null, TASK_MAX_LENGTH_DESCRIPTION, ok);

    State stateSelected = (State) stateSP.getSelectedItem();
    if (StateUtils.isEmptyState(stateSelected)) {
      ok = false;
    }
    if (StateUtils.isInCalendarState(stateSelected)) {
      ok = ValidateForm.checkForm(dateFromText, 0, null, null, ok);
      ok = ValidateForm.checkForm(dateToText, 0, null, null, ok);

      try {
        Long dateFromLong = Converter.getMillisecondsFromString(dateFromText.getText().toString());
        Long dateToLong = Converter.getMillisecondsFromString(dateToText.getText().toString());
        if (dateFromLong < 0) {
          dateFromText.setError(getString(R.string.error_date_wrong_format));
          ok = false;
        }
        if (dateToLong < 0) {
          dateToText.setError(getString(R.string.error_date_wrong_format));
          ok = false;
        }
        if (ok && dateFromLong >= dateToLong) {
          dateFromText.setError(getString(R.string.error_date_bad_order));
          dateToText.setError(getString(R.string.error_date_bad_order));
          ok = false;
        }
      }
      catch (Throwable e) {
        dateFromText.setError(getString(R.string.error_date_something_wrong));
        ok = false;
      }
    }
    return ok;
  }

  public void stateReload(State state) throws GtdException {
    if (this.stateList == null || stateAdapter == null) {
      List<State> statesList = StateUtils.getStatesArray(StateUtils.EStateTypes.TASK);
      this.stateList = statesList;
      State[] states = statesList.toArray(new State[statesList.size()]);
      stateAdapter = new UniversalSpinAdapter(this,
          R.layout.simple_spinner_item,
          states);
//      stateAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
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
      projectList = ProjectUtils.getProjectList(filter);
      SpinnerUtils.addEmptyProjectItem(projectList);
      Project[] projects = projectList.toArray(new Project[projectList.size()]);
      projectAdapter = new UniversalSpinAdapter(this,
          R.layout.simple_spinner_item,
          projects);
//      projectAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
      taskParentSP.setAdapter(projectAdapter);
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
        taskParentSP.setSelection(index);
        return;
      }
    }
    if (this.projectList.size() > 0) {
      taskParentSP.setSelection(0);
    }
  }

  public void contextReload(ContextGtd context) {

    if (this.contextList == null || contextAdapter == null
        || ContextUtils.isNewData(contextLoaded)) {
      contextList = ContextUtils.getContextList();
      SpinnerUtils.addEmptyContextItem(contextList);
      ContextGtd[] contextGtds = contextList.toArray(new ContextGtd[contextList.size()]);
      contextAdapter = new UniversalSpinAdapter(this,
          R.layout.simple_spinner_item,
          contextGtds);
      contextSP.setAdapter(contextAdapter); // Set the custom adapter to the spinner
      contextLoaded = new Timestamp(new Date().getTime());
    }
    contextSpinnerSet(context);
  }

  private void contextSpinnerSet(ContextGtd contextGtd) {
    if (this.contextList == null) {
      return;
    }
    if (contextGtd != null) {
      int index = this.contextList.indexOf(contextGtd);
      if (index >= 0) {
        contextSP.setSelection(index);
        return;
      }
    }
    if (this.contextList.size() > 0) {
      contextSP.setSelection(0);
    }
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view,
                             int position, long id) {
    // Here you get the current item (a User object) that is selected by its position
    Object object = stateAdapter.getItem(position);
    if (!(object instanceof State))
      return;
    State state = (State) object;
    try {
      if (StateUtils.isInCalendarState(state)) {
        ShowCalendar(true);
      }
      else {
        ShowCalendar(false);
      }
    }
    catch (GtdException e) {
      showErrorText(getString(R.string.error_unknown));
    }
  }

  private void ShowCalendar(boolean show) {
    int visible = (show) ? View.VISIBLE : View.GONE;
    dateToText.setVisibility(visible);
    dateToTV.setVisibility(visible);
    dateFromText.setVisibility(visible);
    dateFromTV.setVisibility(visible);
  }

  private void showPostButtons(boolean postFacebook, boolean postGoogle) {
    postFacebookButton.setEnabled(postFacebook);
    postGoogleButton.setEnabled(postGoogle);
  }

  private void showLoginButtons(boolean loginFacebook, boolean loginGoogle) {
    loginFacebookButton.setEnabled(loginFacebook);
    loginGoogleButton.setEnabled(loginGoogle);
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
    // Another interface callback
  }


  @Subscribe
  public void onUnAuthorizedErrorEvent(UnAuthorizedErrorEvent unAuthorizedErrorEvent) {
    Toaster.showLong(TaskActivity.this, R.string.message_bad_credentials);
  }

  @Subscribe
  public void onNetworkErrorEvent(NetworkErrorEvent networkErrorEvent) {
    Toaster.showLong(TaskActivity.this, R.string.message_bad_network);
  }

  @Subscribe
  public void onRestAdapterErrorEvent(RestAdapterErrorEvent restAdapterErrorEvent) {
    Toaster.showLong(TaskActivity.this, R.string.message_bad_restRequest);
  }

  @Subscribe
  public void onBadRequestErrorEvent(BadRequestErrorEvent badRequestErrorEvent) {
    Toaster.showLong(TaskActivity.this, R.string.message_bad_restRequest);
  }

  @Subscribe
  public void onAlreadyReportedErrorEvent(AlreadyReportedErrorEvent alreadyReportedErrorEvent) {
    Toaster.showLong(TaskActivity.this, R.string.task_post_already_reported);
  }

}
