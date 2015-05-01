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
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import cz.slama.android.gtd.BootstrapServiceProvider;
import cz.slama.android.gtd.Injector;
import cz.slama.android.gtd.R;
import cz.slama.android.gtd.authenticator.LogoutService;
import cz.slama.android.gtd.core.BootstrapService;
import cz.slama.android.gtd.model.ContextGtd;
import cz.slama.android.gtd.model.util.ContextUtils;
import cz.slama.android.gtd.ui.exceptions.UnauthorizedException;
import cz.slama.android.gtd.ui.reloading.ReloadStatus;

import javax.inject.Inject;
import java.util.List;

import static cz.slama.android.gtd.core.Constants.Extra.CONTEXT;

public class ContextsListFragment extends ItemListFragment<ContextGtd> {

  @Inject
  protected BootstrapServiceProvider serviceProvider;
  @Inject
  protected LogoutService logoutService;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Injector.inject(this);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    return super.onCreateView(inflater, container, savedInstanceState);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    setEmptyText(R.string.no_contexts);
  }

  @Override
  protected void configureList(final Activity activity, final ListView listView) {
    super.configureList(activity, listView);

    listView.setFastScrollEnabled(true);
    listView.setDividerHeight(0);

    View view = activity.getLayoutInflater()
        .inflate(R.layout.context_list_item_labels, null);
    Button projectCreate = (Button) view.findViewById(R.id.b_context_create_show);
    projectCreate.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(getActivity(), ContextActivity.class));
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
  public Loader<List<ContextGtd>> onCreateLoader(final int id, final Bundle args) {
    final List<ContextGtd> initialItems = items;
    return new ThrowableLoader<List<ContextGtd>>(getActivity(), items) {
      @Override
      public List<ContextGtd> loadData() throws Exception {
        try {
          BootstrapService bootstrapService = serviceProvider.getService(getActivity());
          if (ReloadStatus.isContextToReload()) {
            ContextUtils.setContextList(bootstrapService.getAllContexts());
          }
          ReloadStatus.setContextToReload(false);
          return ContextUtils.getContextList();
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
  protected SingleTypeAdapter<ContextGtd> createAdapter(List<ContextGtd> items) {
    return new ContextListAdapter(getActivity().getLayoutInflater(), items);
  }

  @Override
  public void onResume() {
    super.onResume();
    if ((ReloadStatus.isTaskToReload() || ReloadStatus.isContextToReload())
        && !isFirstLoad()) {
      forceRefresh();
    }
    firstLoad = false;
  }

  public void onListItemClick(ListView l, View v, int position, long id) {
    ContextGtd contextGtd = ((ContextGtd) l.getItemAtPosition(position));
    startActivity(new Intent(getActivity(), ContextActivity.class).putExtra(CONTEXT, contextGtd));
  }

  @Override
  public void onLoadFinished(final Loader<List<ContextGtd>> loader, final List<ContextGtd> items) {
    super.onLoadFinished(loader, items);
  }

  @Override
  protected int getErrorMessage(Exception exception) {
    if (exception.getCause() instanceof UnauthorizedException){
      logoutUser();
    }
    return R.string.error_loading_contexts;
  }
}
