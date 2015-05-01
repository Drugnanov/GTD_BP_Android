package cz.slama.android.gtd.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.*;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import butterknife.InjectView;
import butterknife.Views;
import com.github.kevinsawicki.wishlist.Toaster;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import cz.slama.android.gtd.BootstrapServiceProvider;
import cz.slama.android.gtd.Injector;
import cz.slama.android.gtd.R;
import cz.slama.android.gtd.authenticator.LogoutService;
import cz.slama.android.gtd.events.NetworkErrorEvent;
import cz.slama.android.gtd.events.RestAdapterErrorEvent;
import cz.slama.android.gtd.events.UnAuthorizedErrorEvent;
import cz.slama.android.gtd.model.api.Filter;
import cz.slama.android.gtd.model.util.StateUtils;
import cz.slama.android.gtd.persistence.ShrPrefUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class FilterFragment extends Fragment {

  @Inject
  protected BootstrapServiceProvider serviceProvider;
  @Inject
  protected LogoutService logoutService;
  @Inject
  Bus bus;
  @Inject
  protected Filter filter;

  @InjectView(R.id.cb_filter_project_show_active)
  protected CheckBox projectStateActiveCb;
  @InjectView(R.id.cb_filter_project_show_done)
  protected CheckBox projectStateDoneCb;

  @InjectView(R.id.cb_filter_task_show_active)
  protected CheckBox taskStateActiveCb;
  @InjectView(R.id.cb_filter_task_show_created)
  protected CheckBox taskStateCreatedCb;
  @InjectView(R.id.cb_filter_task_show_in_calendar)
  protected CheckBox taskStateInCalendarCb;
  @InjectView(R.id.cb_filter_task_show_done)
  protected CheckBox taskStateDoneCb;

  Activity activity = null;

  View view;
  boolean loaded = false;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    view = inflater.inflate(R.layout.filter, container, false);
    Views.inject(this, view);
    return view;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    projectStateActiveCb.setOnCheckedChangeListener(getOnCheckListener());
    projectStateDoneCb.setOnCheckedChangeListener(getOnCheckListener());
    taskStateActiveCb.setOnCheckedChangeListener(getOnCheckListener());
    taskStateCreatedCb.setOnCheckedChangeListener(getOnCheckListener());
    taskStateInCalendarCb.setOnCheckedChangeListener(getOnCheckListener());
    taskStateDoneCb.setOnCheckedChangeListener(getOnCheckListener());
    uncheckAllStates();
    for (StateUtils.EStates state : filter.getStatesToShow()) {
      setState(state);
    }
    loaded = true;
    super.onViewCreated(view, savedInstanceState);
  }

  protected CompoundButton.OnCheckedChangeListener getOnCheckListener(){
    return new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView,
                                   boolean isChecked) {
        changeAndSafeFilter();
      }
    };
  }

  private void changeAndSafeFilter() {
    if (!loaded) {
      return;
    }
    List<StateUtils.EStates> statesList = new ArrayList<StateUtils.EStates>();
    loadState(projectStateActiveCb, StateUtils.EStates.PROJECT_ACTIVE, statesList);
    loadState(projectStateDoneCb, StateUtils.EStates.PROJECT_DONE, statesList);
    loadState(taskStateActiveCb, StateUtils.EStates.TASK_ACTIVE, statesList);
    loadState(taskStateCreatedCb, StateUtils.EStates.TASK_CREATED, statesList);
    loadState(taskStateInCalendarCb, StateUtils.EStates.TASK_IN_CALENDAR, statesList);
    loadState(taskStateDoneCb, StateUtils.EStates.TASK_DONE, statesList);
    filter.setStatesToShow(statesList);
    filter.saveFilter();
  }

  private void loadState(CheckBox projectStateActiveCb, StateUtils.EStates state,
                         List<StateUtils.EStates> statesList) {
    if (projectStateActiveCb.isChecked()) {
      statesList.add(state);
    }
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Injector.inject(this);
    setHasOptionsMenu(true);
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.activity = activity;
  }

  @Override
  public void onPause() {
    super.onPause();
    bus.unregister(this);
  }

  @Override
  public void onResume() {
    super.onResume();
    bus.register(this);
  }

  private void uncheckAllStates() {
    taskStateActiveCb.setChecked(false);
    taskStateCreatedCb.setChecked(false);
    taskStateInCalendarCb.setChecked(false);
    taskStateDoneCb.setChecked(false);
    projectStateActiveCb.setChecked(false);
    projectStateDoneCb.setChecked(false);
  }

  private void setState(StateUtils.EStates state) {
    switch (state) {
      case TASK_CREATED:
        taskStateCreatedCb.setChecked(true);
        break;
      case TASK_ACTIVE:
        taskStateActiveCb.setChecked(true);
        break;
      case TASK_IN_CALENDAR:
        taskStateInCalendarCb.setChecked(true);
        break;
      case TASK_DONE:
        taskStateDoneCb.setChecked(true);
        break;
      case PROJECT_ACTIVE:
        projectStateActiveCb.setChecked(true);
        break;
      case PROJECT_DONE:
        projectStateDoneCb.setChecked(true);
        break;
    }
  }

  public void showErrorText(String errorText) {
    Toaster.showLong(activity, errorText);
  }

  @Override
  public void onCreateOptionsMenu(final Menu optionsMenu, final MenuInflater inflater) {
    inflater.inflate(R.menu.bootstrap, optionsMenu);
    MenuItem item = optionsMenu.findItem(R.id.logout);
    item.setVisible(false);
    MenuItem itemRefresh = optionsMenu.findItem(R.id.refresh);
    itemRefresh.setVisible(false);
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    if (!isUsable()) {
      return false;
    }
    switch (item.getItemId()) {
      case R.id.refresh:
        forceRefresh();
        return true;
      case R.id.logout:
        logout();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void logout() {
    getLogoutService().logout(new Runnable() {
      @Override
      public void run() {
        // Calling a refresh will force the service to look for a logged in user
        // and when it finds none the user will be requested to log in again.
        forceRefresh();
      }
    });
  }

  protected LogoutService getLogoutService() {
    return logoutService;
  }

  protected void forceRefresh() {
  }

  protected boolean isUsable() {
    return getActivity() != null;
  }

  private ActionBarActivity getActionBarActivity() {
    return ((ActionBarActivity) getActivity());
  }

  @Subscribe
  public void onUnAuthorizedErrorEvent(UnAuthorizedErrorEvent unAuthorizedErrorEvent) {
    Toaster.showLong(getActivity(), R.string.message_bad_credentials);
  }

  @Subscribe
  public void onNetworkErrorEvent(NetworkErrorEvent networkErrorEvent) {
    Toaster.showLong(getActivity(), R.string.message_bad_network);
  }

  @Subscribe
  public void onRestAdapterErrorEvent(RestAdapterErrorEvent restAdapterErrorEvent) {
    Toaster.showLong(getActivity(), R.string.message_bad_restRequest);
  }
//  @InjectView(R.id.tv_user_username)
//  protected TextView usernameTextView;
//  @InjectView(R.id.tv_user_password)
//  protected TextView passwordTextView;
//  @InjectView(R.id.tv_user_password_check)
//  protected TextView passwordCheckTextView;
//  @InjectView(R.id.tv_user_person_name)
//  protected TextView personNameTextView;
//  @InjectView(R.id.tv_user_person_surname)
//  protected TextView personSurnameTextView;
}
