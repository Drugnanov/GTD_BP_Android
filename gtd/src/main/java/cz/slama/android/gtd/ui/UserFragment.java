package cz.slama.android.gtd.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
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
import cz.slama.android.gtd.exceptions.GtdException;
import cz.slama.android.gtd.model.Person;
import cz.slama.android.gtd.model.util.PersonUtils;
import cz.slama.android.gtd.ui.reloading.ReloadStatus;
import cz.slama.android.gtd.utils.util.SafeAsyncTask;
import cz.slama.android.gtd.utils.ValidateForm;
import retrofit.RetrofitError;

import javax.inject.Inject;

import static cz.slama.android.gtd.core.Constants.FormValidation.*;

public class UserFragment extends Fragment {

  @Inject
  protected BootstrapServiceProvider serviceProvider;
  @Inject
  Bus bus;
  @Inject
  protected LogoutService logoutService;

  private Person userItem;

  @InjectView(R.id.et_user_username)
  protected EditText userNameText;
  @InjectView(R.id.et_user_password)
  protected EditText passwordText;
  @InjectView(R.id.et_user_password_check)
  protected EditText passwordCheckText;
  @InjectView(R.id.et_user_person_name)
  protected EditText personNameText;
  @InjectView(R.id.et_user_person_surname)
  protected EditText personSurnameText;

  //buttons
  @InjectView(R.id.b_user_update)
  protected Button updateButton;

  Activity activity = null;
  boolean loaded = false;

  private SafeAsyncTask<Boolean> actionTask;

  private final TextWatcher watcher = validationTextWatcher();

  View view;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    view = inflater.inflate(R.layout.user, container, false);
    //butterknife injection
    Views.inject(this, view);
    return view;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    loadData();
    userNameText.addTextChangedListener(watcher);
    passwordText.addTextChangedListener(watcher);
    passwordCheckText.addTextChangedListener(watcher);
    updateButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleAction(v);
      }
    });
    updateButton.setEnabled(false);
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

  public void loadData() {
    if (actionTask!=null){
      return;
    }
    getActionBarActivity().setSupportProgressBarIndeterminateVisibility(true);
    actionTask = new SafeAsyncTask<Boolean>() {
      public Boolean call() throws Exception {
        if (ReloadStatus.isPersonToReload()) {
          PersonUtils.setPerson(serviceProvider.getService(getActivity()).getPersonByToken());
          ReloadStatus.setPersonToReload(false);
        }
        return true;
      }

      @Override
      protected void onException(final Exception e) throws RuntimeException {
        // Retrofit Errors are handled inside of the {
        if (!(e instanceof RetrofitError)) {
          final Throwable cause = e.getCause() != null ? e.getCause() : e;
          if (cause != null) {
            Toaster.showLong(getActivity(), getString(R.string.label_something_wrong));
          }
        }
        actionTask = null;
      }

      @Override
      public void onSuccess(final Boolean authSuccess) {
        userItem = PersonUtils.getPerson();
        fillFragmentFromPerson(userItem);
      }

      @Override
      protected void onFinally() throws RuntimeException {
        getActionBarActivity().setSupportProgressBarIndeterminateVisibility(false);
        actionTask = null;
        loaded = true;
      }
    };
    actionTask.execute();
  }

  public void handleAction(final View view) {
    //cant run more than once at a time
    if (actionTask != null) {
      return;
    }
    final Person person = getPersonFromForms();
    if (person == null) {
      return;
    }
    getActionBarActivity().setSupportProgressBarIndeterminateVisibility(true);
    actionTask = new SafeAsyncTask<Boolean>() {
      public Boolean call() throws Exception {
        PersonUtils.setPerson(serviceProvider.getService(getActivity()).updatePerson(person.getId(), person));
        ReloadStatus.setPersonToReload(true);
        return true;
      }

      @Override
      protected void onException(final Exception e) throws RuntimeException {
        // Retrofit Errors are handled inside of the {
        if (!(e instanceof RetrofitError)) {
          final Throwable cause = e.getCause() != null ? e.getCause() : e;
          if (cause != null) {
            Toaster.showLong(getActivity(), getString(R.string.label_something_wrong));
          }
        }
        actionTask = null;
      }

      @Override
      public void onSuccess(final Boolean authSuccess) {
        userItem = PersonUtils.getPerson();
        actionTask = null;
        Toaster.showLong(getActivity(), getString(R.string.label_user_updated));
      }

      @Override
      protected void onFinally() throws RuntimeException {
        getActionBarActivity().setSupportProgressBarIndeterminateVisibility(false);
        actionTask = null;
      }
    };
    actionTask.execute();
  }

  private Person getPersonFromForms() {
    Person person = null;
    try {
      person = fillPersonFromFragmentForm();
    }
    catch (GtdException e) {
      showErrorText(getString(R.string.error_fill_from_form));
      person = null;
    }
    return person;
  }

  private Person fillPersonFromFragmentForm() throws GtdException {
    if (userItem == null) {
      throw new GtdException("No user item.");
    }
    if (!checkForms(true)) {
      return null;
    }
    userItem.setUsername(userNameText.getText().toString());
    userItem.setPassword(passwordText.getText().toString());
    userItem.setName(personNameText.getText().toString());
    userItem.setSurname(personSurnameText.getText().toString());
    return userItem;
  }

  private void fillFragmentFromPerson(Person personToFill) {
    try {
      fillFragFromPerson(personToFill);
    }
    catch (GtdException e) {
      showErrorText(getString(R.string.error_fill_from_object));
    }
  }

  private void fillFragFromPerson(Person personToFill) throws GtdException {
    if (personToFill == null) {
      return;
    }
    userNameText.setText(userItem.getUsername());
    personNameText.setText(userItem.getName());
    personSurnameText.setText(userItem.getSurname());
  }

  private boolean checkForms(boolean showMessage) {
    boolean ok = true;

    ok = ValidateForm.checkForm(userNameText, 0, PERSON_MIN_LENGTH_LOGIN, PERSON_MAX_LENGTH_LOGIN, ok, showMessage);
    ok = ValidateForm
        .checkForm(passwordText, 0, PERSON_MIN_LENGTH_PASSWORD, PERSON_MAX_LENGTH_PASSWORD, ok, showMessage);
    ok = ValidateForm
        .checkForm(passwordCheckText, 0, PERSON_MIN_LENGTH_PASSWORD, PERSON_MAX_LENGTH_PASSWORD, ok, showMessage);
    ok = ValidateForm.checkForm(personNameText, null, null, PERSON_MAX_LENGTH_FIRST_NAME, ok, showMessage);
    ok = ValidateForm.checkForm(personSurnameText, null, null, PERSON_MAX_LENGTH_LAST_NAME, ok, showMessage);
    if (ok) {
      if (!passwordText.getText().toString().equals(passwordCheckText.getText().toString())) {
        ok = false;
      }
      showPassAreSame(ok);
    }
    updateButton.setEnabled(ok);
    return ok;
  }

  private TextWatcher validationTextWatcher() {
    return new TextWatcherAdapter() {
      public void afterTextChanged(final Editable gitDirEditText) {
        if (loaded) {
          checkForms(false);
        }
      }
    };
  }

  private void showPassAreSame(boolean areSame) {
    setRequiredFiled(passwordText, areSame);
    setRequiredFiled(passwordCheckText, areSame);
  }

  private void setRequiredFiled(EditText field, boolean ok) {
    if (ok) {
      field.setBackground(getResources().getDrawable(R.drawable.edit_text_background_required_ok));
    }
    else {
      field.setBackground(getResources().getDrawable(R.drawable.edit_text_background_required));
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
    loadData();
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
}
