package cz.slama.android.gtd.ui;

import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import cz.slama.android.gtd.BootstrapServiceProvider;
import cz.slama.android.gtd.Injector;
import cz.slama.android.gtd.R;
import cz.slama.android.gtd.authenticator.LogoutService;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import cz.slama.android.gtd.core.BootstrapService;
import cz.slama.android.gtd.model.Task;
import cz.slama.android.gtd.model.api.Filter;
import cz.slama.android.gtd.model.util.TaskUtils;
import cz.slama.android.gtd.ui.exceptions.UnauthorizedException;
import cz.slama.android.gtd.ui.reloading.ReloadStatus;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import static cz.slama.android.gtd.core.Constants.Extra.TASK;
import static cz.slama.android.gtd.model.util.ContextUtils.setContextList;

public class TasksListFragment extends ItemListFragment<Task> {

  @Inject
  protected BootstrapServiceProvider serviceProvider;
  @Inject
  protected LogoutService logoutService;
  @Inject
  protected cz.slama.android.gtd.model.api.Filter filter;

  private Timestamp taskReloaded = null;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Injector.inject(this);
    setHasOptionsMenu(true);
  }

  @Override
  public void onActivityCreated(final Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    setEmptyText(R.string.no_tasks);
  }

  @Override
  protected void configureList(final Activity activity, final ListView listView) {
    super.configureList(activity, listView);

    listView.setFastScrollEnabled(true);
    listView.setDividerHeight(0);

    View view = activity.getLayoutInflater()
        .inflate(R.layout.task_list_item_labels, null);
    Button projectCreate = (Button) view.findViewById(R.id.b_task_create_show);
    projectCreate.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(getActivity(), TaskActivity.class));
      }
    });

    getListAdapter()
        .addHeader(view);
  }

  @Override
  protected LogoutService getLogoutService() {
    return logoutService;
  }

  @Override
  public Loader<List<Task>> onCreateLoader(final int id, final Bundle args) {
    final List<Task> initialItems = items;
    return new ThrowableLoader<List<Task>>(getActivity(), items) {
      @Override
      public List<Task> loadData() throws Exception {
        try {
          if (getActivity() != null) {
            if (ReloadStatus.isContextToReload()) {
              BootstrapService bootstrapService = serviceProvider.getService(getActivity());
              setContextList(bootstrapService.getAllContexts());
              ReloadStatus.setContextToReload(false);
            }
            if (ReloadStatus.isTaskToReload()) {
              TaskUtils.setTaskList(serviceProvider.getService(getActivity()).getAllTasks());
            }
          }
          else {
            TaskUtils.setTaskList(null);
          }
          ReloadStatus.setTaskToReload(false);
          taskReloaded = new Timestamp(new Date().getTime());
          return TaskUtils.getTaskList(filter);
        }
        catch (final OperationCanceledException e) {
          final Activity activity = getActivity();
          if (activity != null) {
            activity.finish();
          }
          return initialItems;
        }
      }
    };
  }

  @Override
  protected SingleTypeAdapter<Task> createAdapter(final List<Task> items) {
    return new TaskListAdapter(getActivity().getLayoutInflater(), items);
  }

  @Override
  public void onResume() {
    super.onResume();
    if ((ReloadStatus.isTaskToReload() || Filter.filterChanged(taskReloaded))
        && !isFirstLoad()) {
      forceRefresh();
    }
    firstLoad = false;
  }


  public void onListItemClick(final ListView l, final View v, final int position, final long id) {
    final Task task = ((Task) l.getItemAtPosition(position));
    startActivity(new Intent(getActivity(), TaskActivity.class).putExtra(TASK, task));
  }

  @Override
  public void onLoadFinished(final Loader<List<Task>> loader, final List<Task> items) {
    super.onLoadFinished(loader, items);
  }

  @Override
  protected int getErrorMessage(final Exception exception) {
    if (exception.getCause() instanceof UnauthorizedException){
      logoutUser();
    }
    return R.string.error_loading_tasks;
  }



}
