package cz.slama.android.gtd.ui;

import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import cz.slama.android.gtd.BootstrapServiceProvider;
import cz.slama.android.gtd.Injector;
import cz.slama.android.gtd.R;
import cz.slama.android.gtd.authenticator.LogoutService;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import cz.slama.android.gtd.core.BootstrapService;
import cz.slama.android.gtd.model.Project;
import cz.slama.android.gtd.model.api.Filter;
import cz.slama.android.gtd.model.util.ProjectUtils;
import cz.slama.android.gtd.model.util.TaskUtils;
import cz.slama.android.gtd.ui.exceptions.UnauthorizedException;
import cz.slama.android.gtd.ui.reloading.ReloadStatus;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import static cz.slama.android.gtd.core.Constants.Extra.PROJECT_ITEM;

public class ProjectsListFragment extends ItemListFragment<Project> {

  @Inject
  protected BootstrapServiceProvider serviceProvider;
  @Inject
  protected LogoutService logoutService;
  @Inject
  protected cz.slama.android.gtd.model.api.Filter filter;

  Timestamp projectReloaded = null;


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Injector.inject(this);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    setEmptyText(R.string.no_projects);
  }

  @Override
  protected void configureList(Activity activity, ListView listView) {
    super.configureList(activity, listView);

    listView.setFastScrollEnabled(true);
    listView.setDividerHeight(0);

    View view = activity.getLayoutInflater()
        .inflate(R.layout.projects_list_item_labels, null);
    Button projectCreate = (Button) view.findViewById(R.id.b_project_create_show);
    projectCreate.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(getActivity(), ProjectActivity.class));
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
  public void onDestroyView() {
    setListAdapter(null);
    super.onDestroyView();
  }

  @Override
  public Loader<List<Project>> onCreateLoader(int id, Bundle args) {
    final List<Project> initialItems = items;
    return new ThrowableLoader<List<Project>>(getActivity(), items) {

      @Override
      public List<Project> loadData() throws Exception {
        try {
          if (getActivity() != null) {
            if (ReloadStatus.isTaskToReload()) {
              BootstrapService bootstrapService = serviceProvider.getService(getActivity());
              TaskUtils.setTaskList(serviceProvider.getService(getActivity()).getAllTasks());
              ReloadStatus.setTaskToReload(false);
            }
            if (ReloadStatus.isProjectToReload()) {
              BootstrapService bootstrapService = serviceProvider.getService(getActivity());
              ProjectUtils.setProjectList(bootstrapService.getAllProjects());
            }
          }
          else {
            ProjectUtils.setProjectList(new ArrayList<Project>());
          }
          ReloadStatus.setProjectToReload(false);
          projectReloaded = new Timestamp(new Date().getTime());
          return ProjectUtils.getProjectList(filter);
        }
        catch (OperationCanceledException e) {
          Activity activity = getActivity();
          if (activity != null)
            activity.finish();
          return initialItems;
        }
      }
    };
  }

  @Override
  protected SingleTypeAdapter<Project> createAdapter(List<Project> items) {
    return new ProjectsListAdapter(getActivity().getLayoutInflater(), items);
  }

  @Override
  public void onStart() {
    super.onStart();
  }

  @Override
  public void onResume() {
    super.onResume();
    if ((ReloadStatus.isProjectToReload() || Filter.filterChanged(projectReloaded) || ReloadStatus.isTaskToReload())
        && !isFirstLoad()) {
      forceRefresh();
    }
    firstLoad = false;
  }

  public void onListItemClick(ListView l, View v, int position, long id) {
    Project project = ((Project) l.getItemAtPosition(position));
    startActivity(new Intent(getActivity(), ProjectActivity.class).putExtra(PROJECT_ITEM, project));
  }

  @Override
  protected int getErrorMessage(Exception exception) {
    if (exception.getCause() instanceof UnauthorizedException) {
      logoutUser();
    }
    return R.string.error_loading_projects;
  }

//  @Override
//  protected void userRefreshAction() {
//    ReloadStatus.projectToReload = true;
//  }
}
