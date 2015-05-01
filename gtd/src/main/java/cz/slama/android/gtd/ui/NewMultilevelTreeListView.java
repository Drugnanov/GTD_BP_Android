package cz.slama.android.gtd.ui;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.*;
import android.widget.*;
import butterknife.Views;
import com.github.kevinsawicki.wishlist.Toaster;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import cz.slama.android.gtd.BootstrapServiceProvider;
import cz.slama.android.gtd.Injector;
import cz.slama.android.gtd.R;
import cz.slama.android.gtd.authenticator.LogoutService;
import cz.slama.android.gtd.core.BootstrapService;
import cz.slama.android.gtd.events.*;
import cz.slama.android.gtd.model.Project;
import cz.slama.android.gtd.model.Task;
import cz.slama.android.gtd.model.api.Filter;
import cz.slama.android.gtd.model.interfaces.IObjectTitle;
import cz.slama.android.gtd.model.util.HierarchyUtils;
import cz.slama.android.gtd.model.util.ProjectUtils;
import cz.slama.android.gtd.model.util.TaskUtils;
import cz.slama.android.gtd.ui.adapter.model.Group;
import cz.slama.android.gtd.ui.reloading.ReloadStatus;
import cz.slama.android.gtd.utils.util.SafeAsyncTask;
import retrofit.RetrofitError;

import javax.inject.Inject;

import static cz.slama.android.gtd.core.Constants.Extra.PROJECT_ITEM;
import static cz.slama.android.gtd.core.Constants.Extra.TASK;

public class NewMultilevelTreeListView extends Fragment {

  @Inject
  protected BootstrapServiceProvider serviceProvider;
  @Inject
  protected LogoutService logoutService;
  @Inject
  protected Filter filter;
  @Inject
  Bus bus;

  /**
   * Progress bar
   */
  protected ProgressBar progressBar;

  NewListAdapter adapter;
  ListView mainList;
  ArrayList<Group> groups = new ArrayList<Group>();

  View view;

  private SafeAsyncTask<Boolean> actionLoadDataTask;
  private Timestamp hierarchyReloaded = null;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Injector.inject(this);
    loadData();
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    view = inflater.inflate(R.layout.treelist, container, false);
    Views.inject(this, view);

    mainList = (ListView) view.findViewById(R.id.currentpending_list);
    mainList.setDividerHeight(1);

    return view;
  }

  @Override
  public void onViewCreated(final View view, final Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    progressBar = (ProgressBar) view.findViewById(R.id.pb_hierarchy_loading);
  }

  @Override
  public void onCreateOptionsMenu(final Menu optionsMenu, final MenuInflater inflater) {
    inflater.inflate(R.menu.bootstrap, optionsMenu);
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    if (!isUsable()) {
      return false;
    }
    switch (item.getItemId()) {
      case R.id.refresh:
        loadData();
        return true;
      case R.id.logout:
        logout();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  protected boolean isUsable() {
    return getActivity() != null;
  }

  protected LogoutService getLogoutService() {
    return logoutService;
  }

  private void logout() {
    getLogoutService().logout(new Runnable() {
      @Override
      public void run() {
        // Calling a refresh will force the service to look for a logged in user
        // and when it finds none the user will be requested to log in again.
        loadData();
      }
    });
  }


  @Override
  public void onResume() {
    super.onResume();
    if (ReloadStatus.isTaskToReload() || ReloadStatus.isProjectToReload() || ReloadStatus.isHierarchyReload()
        || Filter.filterChanged(hierarchyReloaded)) {
      loadData();
    }
    else{
      populateList();
    }
    bus.register(this);
  }

  @Override
  public void onPause() {
    super.onPause();
    bus.unregister(this);
  }

  private ActionBarActivity getActionBarActivity() {
    return ((ActionBarActivity) getActivity());
  }

  @Override
  public void onDestroyView() {
    adapter = null;
    progressBar = null;
    super.onDestroyView();
  }

  public void loadData() {
    //cant run more than once at a time
    if (actionLoadDataTask != null) {
      return;
    }
    getActionBarActivity().setSupportProgressBarIndeterminateVisibility(true);
    actionLoadDataTask = new SafeAsyncTask<Boolean>() {
      public Boolean call() throws Exception {
        if (getActivity() != null) {
          BootstrapService bootstrapService = serviceProvider.getService(getActivity());
          if (ReloadStatus.isTaskToReload()) {
            TaskUtils.setTaskList(bootstrapService.getAllTasks());
            ReloadStatus.setTaskToReload(false);
          }
          if (ReloadStatus.isProjectToReload()) {
            ProjectUtils.setProjectList(bootstrapService.getAllProjects());
            ReloadStatus.setProjectToReload(false);
          }
        }
        return true;
      }

      @Override
      protected void onException(final Exception e) throws RuntimeException {
        // Retrofit Errors are handled inside of the {
        if (!(e instanceof RetrofitError)) {
          final Throwable cause = e.getCause() != null ? e.getCause() : e;
          if (cause != null) {
            Toaster.showLong(getActionBarActivity(), getString(R.string.label_something_wrong));
          }
        }
        actionLoadDataTask = null;
      }

      @Override
      public void onSuccess(final Boolean authSuccess) {
        actionLoadDataTask = null;
        loadDataHierarchy();
      }

      @Override
      protected void onFinally() throws RuntimeException {
        actionLoadDataTask = null;
        getActionBarActivity().setSupportProgressBarIndeterminateVisibility(false);
      }
    };
    actionLoadDataTask.execute();
  }

  private void loadDataHierarchy() {
    if (ReloadStatus.isHierarchyReload() || groups.size() == 0) {
      this.groups.clear();
      HierarchyUtils.getHierarchyList(filter);
      this.groups.addAll(HierarchyUtils.getParentChildren(0));
      ReloadStatus.setHierarchyReload(false);
      hierarchyReloaded = new Timestamp(new Date().getTime());
    }
    populateList();
  }

  public void populateList() {
    try {
      if (adapter == null) {
        adapter = new NewListAdapter(getActivity(),
            R.id.row_cell_text_multilevel, this.groups, this);
        mainList.setAdapter(adapter);
      }
      else {
        adapter.notifyDataSetChanged();
      }
    }
    catch (Exception e) {
      showCommonErrorMessage();
    }
  }

  public void cellButtonClick(View v) {
    try {
      Button b = (Button) v;
      int index = (Integer) b.getTag();
      if (b.getText().toString().equals("+")) {
        b.setText("-");
        groups.get(index).isOpened = true;
        Group group = groups.get(index);
        List<Group> groupList = HierarchyUtils.getChildren(group);
        int addIndex = index + 1;
        for (Group groupChild : groupList) {
          groups.add(addIndex++, groupChild);
        }
      }
      else {
        b.setText("+");
        groups.get(index).isOpened = false;
        Group group = groups.get(index);
        removeChilds(index, group);
      }
      adapter.notifyDataSetChanged();
    }
    catch (Exception e) {
      adapter.notifyDataSetChanged();
      showCommonErrorMessage();
    }
  }

  private void showCommonErrorMessage() {
    Toaster.showLong(getActionBarActivity(), getString(R.string.label_something_wrong));
  }

  public void removeChilds(int index, Group group) {
    try {
      List<Group> groupList = HierarchyUtils.getChildren(group);
      int removeindex = index + 1;
      for (Group groupChild : groupList) {
        if (groups.get(removeindex).isOpened) {
          groups.get(removeindex).setOpened(false);
          removeChilds(removeindex, groupChild);
        }
        groups.remove(removeindex);
      }
    }
    catch (Exception e) {
      Log.d("Errro=", "" + e.getMessage());
    }
  }

  public void cellTextClick(View v) {
    try {
      TextView b = (TextView) v;
      int index = (Integer) b.getTag();
      Group group = groups.get(index);
      IObjectTitle objectTitle = group.getItem();
      if (objectTitle instanceof Task) {
        startActivity(new Intent(getActivity(), TaskActivity.class).putExtra(TASK, (Task) objectTitle));
      }
      if (objectTitle instanceof Project) {
        startActivity(new Intent(getActivity(), ProjectActivity.class).putExtra(PROJECT_ITEM, (Project) objectTitle));
      }
    }
    catch (Exception e) {
      showCommonErrorMessage();
    }
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

  @Subscribe
  public void onBadRequestErrorEvent(BadRequestErrorEvent badRequestErrorEvent) {
    Toaster.showLong(getActivity(), R.string.message_bad_restRequest);
  }

  @Subscribe
  public void onAlreadyReportedErrorEvent(AlreadyReportedErrorEvent alreadyReportedErrorEvent) {
    Toaster.showLong(getActivity(), R.string.message_bad_post_already_reported);
  }


}
