package cz.slama.android.gtd.authenticator;

import static android.R.layout.simple_dropdown_item_1line;
import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import static android.accounts.AccountManager.KEY_ACCOUNT_TYPE;
import static android.accounts.AccountManager.KEY_AUTHTOKEN;
import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;
import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_ENTER;
import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import cz.slama.android.gtd.Injector;
import cz.slama.android.gtd.R;
import cz.slama.android.gtd.R.id;
import cz.slama.android.gtd.R.layout;
import cz.slama.android.gtd.R.string;
import cz.slama.android.gtd.core.BootstrapService;
import cz.slama.android.gtd.core.Constants;
import cz.slama.android.gtd.events.BadRequestErrorEvent;
import cz.slama.android.gtd.events.NetworkErrorEvent;
import cz.slama.android.gtd.events.RestAdapterErrorEvent;
import cz.slama.android.gtd.events.UnAuthorizedErrorEvent;
import cz.slama.android.gtd.model.Person;
import cz.slama.android.gtd.model.PersonAuth;
import cz.slama.android.gtd.model.PersonCreate;
import cz.slama.android.gtd.persistence.ShrPrefUtils;
import cz.slama.android.gtd.ui.TextWatcherAdapter;
import cz.slama.android.gtd.ui.reloading.ReloadStatus;
import cz.slama.android.gtd.utils.util.Ln;
import cz.slama.android.gtd.utils.util.SafeAsyncTask;
import com.github.kevinsawicki.wishlist.Toaster;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import butterknife.InjectView;
import butterknife.Views;
import retrofit.RetrofitError;

/**
 * Activity to authenticate the user against an API (example API on Parse.com)
 */
public class BootstrapAuthenticatorActivity extends ActionBarAccountAuthenticatorActivity {

  /**
   * PARAM_CONFIRM_CREDENTIALS
   */
  public static final String PARAM_CONFIRM_CREDENTIALS = "confirmCredentials";

  /**
   * PARAM_PASSWORD
   */
  public static final String PARAM_PASSWORD = "password";

  /**
   * PARAM_USERNAME
   */
  public static final String PARAM_USERNAME = "username";

  /**
   * PARAM_AUTHTOKEN_TYPE
   */
  public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";

  enum AuthAction {
    signin, create
  }

  AuthAction authAction = AuthAction.signin;

  private AccountManager accountManager;

  @Inject
  BootstrapService bootstrapService;
  @Inject
  Bus bus;

  @InjectView(id.et_username)
  protected AutoCompleteTextView userNameText;
  @InjectView(id.et_password)
  protected EditText passwordText;
  @InjectView(id.et_password_check)
  protected EditText passwordCheckText;
  @InjectView(id.et_person_name)
  protected EditText personNameText;
  @InjectView(id.et_person_surname)
  protected EditText personSurnameText;

  @InjectView(id.tv_username)
  protected TextView usernameTextView;
  @InjectView(id.tv_password)
  protected TextView passwordTextView;
  @InjectView(id.tv_password_check)
  protected TextView passwordCheckTextView;
  @InjectView(id.tv_person_name)
  protected TextView personNameTextView;
  @InjectView(id.tv_person_surname)
  protected TextView personSurnameTextView;
  @InjectView(id.tv_required)
  protected TextView requiredTextView;

  //buttons
  @InjectView(id.b_signin)
  protected Button signInButton;
  @InjectView(id.b_show_create)
  protected Button showCreateButton;
  @InjectView(id.b_show_sign)
  protected Button showSignButton;
  @InjectView(id.b_create_account)
  protected Button createAccountButton;

  private final TextWatcher watcher = validationTextWatcher();

  private final TextWatcher watcherCreate = validationTextCreateeWatcher();

  private SafeAsyncTask<Boolean> authenticationTask;
  private SafeAsyncTask<Boolean> createAccountTask;
  private String authToken;
  private String authTokenType;

  /**
   * If set we are just checking that the user knows their credentials; this
   * doesn't cause the user's password to be changed on the device.
   */
  private Boolean confirmCredentials = false;

  private String username;
  private String password;
  private String personName;
  private String personSurname;
  private String token;

  /**
   * Was the original caller asking for an entirely new account?
   */
  protected boolean requestNewAccount = false;

  @Override
  public void onCreate(Bundle bundle) {
    super.onCreate(bundle);

    Injector.inject(this);

    accountManager = AccountManager.get(this);

    final Intent intent = getIntent();
    username = intent.getStringExtra(PARAM_USERNAME);
    authTokenType = intent.getStringExtra(PARAM_AUTHTOKEN_TYPE);
    confirmCredentials = intent.getBooleanExtra(PARAM_CONFIRM_CREDENTIALS, false);

    requestNewAccount = username == null;

    setContentView(layout.login_activity);

    Views.inject(this);

    setActionListeners(authAction);
//    final TextView signUpText = (TextView) findViewById(id.tv_signup);
//    signUpText.setMovementMethod(LinkMovementMethod.getInstance());
//    signUpText.setText(Html.fromHtml(getString(string.signup_link)));
  }

  private void setActionListeners(AuthAction authAction) {
    userNameText.setAdapter(new ArrayAdapter<String>(this,
        simple_dropdown_item_1line, userNamesAccounts()));

    if (authAction == AuthAction.signin) {
      passwordText.setOnKeyListener(new OnKeyListener() {

        public boolean onKey(final View v, final int keyCode, final KeyEvent event) {
          if (event != null && ACTION_DOWN == event.getAction()
              && keyCode == KEYCODE_ENTER && signInButton.isEnabled()) {
            //i can use enter to from password form to init login
            handleLogin(signInButton);
            return true;
          }
          return false;
        }
      });

      passwordText.setOnEditorActionListener(new OnEditorActionListener() {

        public boolean onEditorAction(final TextView v, final int actionId,
                                      final KeyEvent event) {
          if (actionId == IME_ACTION_DONE && signInButton.isEnabled()) {
            handleLogin(signInButton);
            return true;
          }
          return false;
        }
      });
      userNameText.addTextChangedListener(watcher);
      passwordText.addTextChangedListener(watcher);
    }
    else if (authAction == AuthAction.create) {
      userNameText.addTextChangedListener(watcherCreate);
      passwordText.addTextChangedListener(watcherCreate);
      passwordCheckText.addTextChangedListener(watcherCreate);
    }
  }

  private List<String> userNamesAccounts() {
    final Account[] accounts = accountManager.getAccountsByType(Constants.Auth.BOOTSTRAP_ACCOUNT_TYPE);
    final List<String> userNames = new ArrayList<String>(accounts.length);
    for (final Account account : accounts) {
      userNames.add(account.name);
    }
    return userNames;
  }

  private TextWatcher validationTextWatcher() {
    return new TextWatcherAdapter() {
      public void afterTextChanged(final Editable gitDirEditText) {
        updateUIWithValidation();
      }
    };
  }

  private TextWatcher validationTextCreateeWatcher() {
    return new TextWatcherAdapter() {
      public void afterTextChanged(final Editable gitDirEditText) {
        updateCreateUIWithValidation();
      }
    };
  }

  @Override
  protected void onResume() {
    super.onResume();
    bus.register(this);
    updateUIWithValidation();
  }

  @Override
  protected void onPause() {
    super.onPause();
    bus.unregister(this);
  }

  private void updateUIWithValidation() {
    boolean userNameOk = populated(userNameText);
    boolean passwordOk = populated(passwordText);
    setRequiredFiled(userNameText, userNameOk);
    setRequiredFiled(passwordText, passwordOk);
    final boolean populated = userNameOk && passwordOk;
    signInButton.setEnabled(populated);
  }

  private void updateCreateUIWithValidation() {
    boolean usernameOK = populated(userNameText);
    setRequiredFiled(userNameText, usernameOK);
    boolean populatedPass = populated(passwordText) || populated(passwordCheckText);
    if (populatedPass) {
      if (!passwordText.getText().toString().equals(passwordCheckText.getText().toString())) {
        populatedPass = false;
      }
      else {
        populatedPass = true;
      }
    }
    showPassAreSame(populatedPass);
    final boolean okForm = usernameOK && populatedPass;
    createAccountButton.setEnabled(okForm);
  }

  private void showPassAreSame(boolean areSame) {
    setRequiredFiled(passwordText, areSame);
    setRequiredFiled(passwordCheckText, areSame);
  }

  private void setRequiredFiled(EditText field, boolean ok) {
    if (ok) {
      //passwordText.setTextColor(getResources().getColor(R.color.edit_text_background_ok_start));
      field.setBackground(getResources().getDrawable(R.drawable.edit_text_background_required_ok));
    }
    else {
      field.setBackground(getResources().getDrawable(R.drawable.edit_text_background_required));
    }
  }

  private boolean populated(final EditText editText) {
    return editText.length() > 0;
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    final ProgressDialog dialog = new ProgressDialog(this);
    dialog.setMessage(getText(string.message_signing_in));
    dialog.setIndeterminate(true);
    dialog.setCancelable(true);
    dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
      public void onCancel(final DialogInterface dialog) {
        if (authenticationTask != null) {
          authenticationTask.cancel(true);
        }
      }
    });
    return dialog;
  }

  @Subscribe
  public void onUnAuthorizedErrorEvent(UnAuthorizedErrorEvent unAuthorizedErrorEvent) {
    Toaster.showLong(BootstrapAuthenticatorActivity.this, R.string.message_bad_credentials);
  }

  @Subscribe
  public void onNetworkErrorEvent(NetworkErrorEvent networkErrorEvent) {
    Toaster.showLong(BootstrapAuthenticatorActivity.this, R.string.message_bad_network);
  }

  @Subscribe
  public void onRestAdapterErrorEvent(RestAdapterErrorEvent restAdapterErrorEvent) {
    Toaster.showLong(BootstrapAuthenticatorActivity.this, R.string.message_bad_restRequest);
  }

  @Subscribe
  public void onBadRequestErrorEvent(BadRequestErrorEvent badRequestErrorEvent) {
    Toaster.showLong(BootstrapAuthenticatorActivity.this, R.string.message_bad_badRequest);
  }

  /**
   * Handles onClick event on the Submit button. Sends username/password to
   * the server for authentication.
   * <p/>
   * Specified by android:onClick="handleLogin" in the layout xml
   *
   * @param view
   */
  public void handleLogin(final View view) {
    //cant run more than once at a time
    if (authenticationTask != null) {
      return;
    }

    if (requestNewAccount) {
      username = userNameText.getText().toString();
    }

    password = passwordText.getText().toString();
    showProgress();

    authenticationTask = new SafeAsyncTask<Boolean>() {
      public Boolean call() throws Exception {

        final String query = String.format("%s=%s&%s=%s",
            PARAM_USERNAME, username, PARAM_PASSWORD, password);

        PersonAuth loginResponse = bootstrapService.authenticate(username, password);
        token = loginResponse.getToken();

        return true;
      }

      @Override
      protected void onException(final Exception e) throws RuntimeException {
        // Retrofit Errors are handled inside of the {
        if (!(e instanceof RetrofitError)) {
          final Throwable cause = e.getCause() != null ? e.getCause() : e;
          if (cause != null) {
            Toaster.showLong(BootstrapAuthenticatorActivity.this, cause.getMessage());
          }
        }
      }

      @Override
      public void onSuccess(final Boolean authSuccess) {
        onAuthenticationResult(authSuccess);
      }

      @Override
      protected void onFinally() throws RuntimeException {
        hideProgress();
        authenticationTask = null;
      }
    };
    authenticationTask.execute();
  }

  /**
   * Called when response is received from the server for confirm credentials
   * request. See onAuthenticationResult(). Sets the
   * AccountAuthenticatorResult which is sent back to the caller.
   *
   * @param result
   */
  protected void finishConfirmCredentials(final boolean result) {
    final Account account = new Account(username, Constants.Auth.BOOTSTRAP_ACCOUNT_TYPE);
    accountManager.setPassword(account, password);

    final Intent intent = new Intent();
    intent.putExtra(KEY_BOOLEAN_RESULT, result);
    setAccountAuthenticatorResult(intent.getExtras());
    setResult(RESULT_OK, intent);
    finish();
  }

  /**
   * Called when response is received from the server for authentication
   * request. See onAuthenticationResult(). Sets the
   * AccountAuthenticatorResult which is sent back to the caller. Also sets
   * the authToken in AccountManager for this account.
   */

  protected void finishLogin() {
    final Account account = new Account(username, Constants.Auth.BOOTSTRAP_ACCOUNT_TYPE);

    if (requestNewAccount) {
      accountManager.addAccountExplicitly(account, password, null);
    }
    else {
      accountManager.setPassword(account, password);
    }

    authToken = token;

    final Intent intent = new Intent();
    intent.putExtra(KEY_ACCOUNT_NAME, username);
    intent.putExtra(KEY_ACCOUNT_TYPE, Constants.Auth.BOOTSTRAP_ACCOUNT_TYPE);

    if (authTokenType != null
        && authTokenType.equals(Constants.Auth.AUTHTOKEN_TYPE)) {
      intent.putExtra(KEY_AUTHTOKEN, authToken);
    }

    setAccountAuthenticatorResult(intent.getExtras());
    setResult(RESULT_OK, intent);
    finish();
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

  /**
   * Called when the authentication process completes (see attemptLogin()).
   *
   * @param result
   */
  public void onAuthenticationResult(final boolean result) {
    if (result) {
      ShrPrefUtils.saveUserData(username, password, token);
      if (!confirmCredentials) {
        finishLogin();
      }
      else {
        finishConfirmCredentials(true);
      }
    }
    else {
      Ln.d("onAuthenticationResult: failed to authenticate");
      if (requestNewAccount) {
        Toaster.showLong(BootstrapAuthenticatorActivity.this,
            string.message_auth_failed_new_account);
      }
      else {
        Toaster.showLong(BootstrapAuthenticatorActivity.this,
            string.message_auth_failed);
      }
    }
  }

  public void handleCreateAccount(View view) {
    //cant run more than once at a time
    if (createAccountTask != null) {
      return;
    }

    username = userNameText.getText().toString();
    password = passwordText.getText().toString();
    personName = personNameText.getText().toString();
    personSurname = personSurnameText.getText().toString();
    final PersonCreate personCreate = new PersonCreate();
    personCreate.setUsername(username);
    personCreate.setPassword(password);
    personCreate.setName(personName);
    personCreate.setSurname(personSurname);
    showProgress();

    createAccountTask = new SafeAsyncTask<Boolean>() {
      public Boolean call() throws Exception {

        Person createAccountResponse = bootstrapService.createPerson(personCreate);
        token = createAccountResponse.getTokens().get(0).getSecurityToken();
        ReloadStatus.setPersonToReload(true);
        return true;
      }

      @Override
      protected void onException(final Exception e) throws RuntimeException {
        // Retrofit Errors are handled inside of the {
        if (!(e instanceof RetrofitError)) {
          final Throwable cause = e.getCause() != null ? e.getCause() : e;
          if (cause != null) {
            Toaster.showLong(BootstrapAuthenticatorActivity.this, cause.getMessage());
          }
        }
      }

      @Override
      public void onSuccess(final Boolean authSuccess) {
        onAuthenticationResult(authSuccess);
      }

      @Override
      protected void onFinally() throws RuntimeException {
        hideProgress();
        createAccountTask = null;
      }
    };
    createAccountTask.execute();
  }

  public void handleShowCreate(View view) {
    updateCreateUIWithValidation();
    passwordCheckTextView.setVisibility(View.VISIBLE);
    passwordCheckText.setVisibility(View.VISIBLE);
    personNameTextView.setVisibility(View.VISIBLE);
    personNameText.setVisibility(View.VISIBLE);
    personSurnameTextView.setVisibility(View.VISIBLE);
    personSurnameText.setVisibility(View.VISIBLE);
    createAccountButton.setVisibility(View.VISIBLE);
    showSignButton.setVisibility(View.VISIBLE);
    requiredTextView.setVisibility(View.VISIBLE);

    signInButton.setVisibility(View.GONE);
    showCreateButton.setVisibility(View.GONE);

    passwordTextView.setText(getString(string.label_password_create));
    usernameTextView.setText(getString(string.label_username_create));

    authAction = AuthAction.create;
    setActionListeners(authAction);
  }

  public void handleShowSignIn(View view) {
    updateUIWithValidation();
    passwordCheckTextView.setVisibility(View.GONE);
    passwordCheckText.setVisibility(View.GONE);
    personNameTextView.setVisibility(View.GONE);
    personNameText.setVisibility(View.GONE);
    personSurnameTextView.setVisibility(View.GONE);
    personSurnameText.setVisibility(View.GONE);
    createAccountButton.setVisibility(View.GONE);
    showSignButton.setVisibility(View.GONE);
    requiredTextView.setVisibility(View.GONE);

    signInButton.setVisibility(View.VISIBLE);
    showCreateButton.setVisibility(View.VISIBLE);

    passwordCheckTextView.setText(getString(string.label_password_check));
    usernameTextView.setText(getString(string.label_username));

    authAction = AuthAction.signin;
    setActionListeners(authAction);
  }
}
