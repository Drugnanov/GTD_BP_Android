package cz.slama.android.gtd.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.InjectView;
import com.github.kevinsawicki.wishlist.Toaster;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import cz.slama.android.gtd.R;
import cz.slama.android.gtd.core.BootstrapService;
import cz.slama.android.gtd.events.NetworkErrorEvent;
import cz.slama.android.gtd.events.RestAdapterErrorEvent;
import cz.slama.android.gtd.events.UnAuthorizedErrorEvent;
import cz.slama.android.gtd.exceptions.GtdException;
import cz.slama.android.gtd.model.ContextGtd;
import cz.slama.android.gtd.ui.reloading.ReloadStatus;
import cz.slama.android.gtd.utils.util.SafeAsyncTask;
import cz.slama.android.gtd.utils.ValidateForm;
import retrofit.RetrofitError;

import javax.inject.Inject;

import static cz.slama.android.gtd.core.Constants.Extra.CONTEXT;
import static cz.slama.android.gtd.core.Constants.FormValidation.CONTEXT_MAX_LENGTH_TITLE;
import static cz.slama.android.gtd.core.Constants.FormValidation.CONTEXT_MIN_LENGTH_TITLE;

public class ContextActivity extends BootstrapActivity{

  @Inject
  BootstrapService bootstrapService;
  @Inject
  Bus bus;

  public enum ETypeOfAction {
    CONTEXT_CREATE,
    CONTEXT_UPDATE,
    CONTEXT_DELETE
  }

  private ETypeOfAction actionType;
  private ContextGtd contextItem;

  @InjectView(R.id.et_context_title)
  protected EditText titleText;

  @InjectView(R.id.b_context_create)
  protected Button actionContextButton;
  @InjectView(R.id.b_context_delete)
  protected Button deleteContextButton;

  @InjectView(R.id.tv_context_title)
  protected TextView titleTV;

  private SafeAsyncTask<Boolean> actionTask;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.context);

    if (getIntent() != null && getIntent().getExtras() != null) {
      contextItem = (ContextGtd) getIntent().getExtras().getSerializable(CONTEXT);
      if (contextItem == null) {
        actionType = ETypeOfAction.CONTEXT_CREATE;
        contextItem = new ContextGtd();
      }
      else {
        actionType = ETypeOfAction.CONTEXT_UPDATE;
      }
    }
    else {
      actionType = ETypeOfAction.CONTEXT_CREATE;
      contextItem = new ContextGtd();
    }

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);
  }

  @Override
  protected void onPause() {
    super.onPause();
    bus.unregister(this);
  }

  @Override
  protected void onResume() {
    try {
      fillFragmentFromContext(contextItem);
      SetAction();
    }
    catch (GtdException e) {
//            e.printStackTrace();
    }
    super.onResume();
    bus.register(this);
  }


  @Subscribe
  public void onUnAuthorizedErrorEvent(UnAuthorizedErrorEvent unAuthorizedErrorEvent) {
    Toaster.showLong(ContextActivity.this, R.string.message_bad_credentials);
  }

  @Subscribe
  public void onNetworkErrorEvent(NetworkErrorEvent networkErrorEvent) {
    Toaster.showLong(ContextActivity.this, R.string.message_bad_network);
  }

  @Subscribe
  public void onRestAdapterErrorEvent(RestAdapterErrorEvent restAdapterErrorEvent) {
    Toaster.showLong(ContextActivity.this, R.string.message_bad_restRequest);
  }

  public void showErrorText(String errorText) {
    Toaster.showLong(ContextActivity.this, errorText);
  }

  public void handleDelete(View view) {
    actionType = ETypeOfAction.CONTEXT_DELETE;
    handleAction(view);
  }

  public void handleAction(final View view) {
    //cant run more than once at a time
    if (actionTask != null) {
      return;
    }
    final ContextGtd context = getContextFromForms();
    if (context == null) {
      return;
    }
    showProgress();
    actionTask = new SafeAsyncTask<Boolean>() {
      public Boolean call() throws Exception {
        ContextGtd contextResponse;
        switch (actionType) {
          case CONTEXT_CREATE:
            contextResponse = bootstrapService.createContext(context);
            break;
          case CONTEXT_UPDATE:
            contextResponse = bootstrapService.updateContext(context.getId(), context);
            break;
          case CONTEXT_DELETE:
            bootstrapService.deleteContext(context.getId());
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
            Toaster.showLong(ContextActivity.this, getString(R.string.label_something_wrong));
          }
        }
        actionTask = null;
      }

      @Override
      public void onSuccess(final Boolean authSuccess) {
        ReloadStatus.setContextToReload(true);
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
    setActionButton(actionContextButton, actionType);
  }

  private void setActionButton(Button taskActionBt, ETypeOfAction typeOfAction) {
    switch (typeOfAction) {
      case CONTEXT_CREATE: {
        taskActionBt.setText(getString(R.string.task_create));
        deleteContextButton.setVisibility(View.GONE);
        break;
      }
      case CONTEXT_UPDATE: {
        taskActionBt.setText(getString(R.string.task_update));
        deleteContextButton.setVisibility(View.VISIBLE);
        break;
      }
    }
  }

  private ContextGtd getContextFromForms() {
    ContextGtd context = null;
    try {
      context = fillContextFromFragmentForm();
    }
    catch (GtdException e) {
      showErrorText(getString(R.string.error_fill_from_form));
      context = null;
    }
    return context;
  }

  private ContextGtd fillContextFromFragmentForm() throws GtdException {
    if (!checkForms()) {
      return null;
    }
    if (contextItem == null) {
      contextItem = new ContextGtd();
    }
    contextItem.setTitle(titleText.getText().toString());
    return contextItem;
  }

  private void fillFragmentFromContext(ContextGtd contextToFill) throws GtdException {
    if (contextToFill == null) {
      setTitle(getString(R.string.title_context_create));
      return;
    }
    setTitle(contextToFill.getTitle());

    titleText.setText(contextToFill.getTitle());
  }

  private boolean checkForms() throws GtdException {
    boolean ok = true;

    ok = ValidateForm.checkForm(titleText, 0, CONTEXT_MIN_LENGTH_TITLE, CONTEXT_MAX_LENGTH_TITLE, ok);

    return ok;
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

}
