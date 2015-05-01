package cz.slama.android.gtd.ui;

import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import butterknife.InjectView;
import com.facebook.FacebookException;
import com.github.kevinsawicki.wishlist.Toaster;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import cz.slama.android.gtd.R;
import cz.slama.android.gtd.core.BootstrapService;
import cz.slama.android.gtd.events.*;
import cz.slama.android.gtd.model.Task;
import cz.slama.android.gtd.persistence.ShrPrefUtils;

import javax.inject.Inject;

import java.io.IOException;

import static cz.slama.android.gtd.core.Constants.Extra.TASK;

public class GoogleActivity extends BootstrapActivity implements
    GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks
    , View.OnClickListener {

  @Inject
  BootstrapService bootstrapService;
  @Inject
  Bus bus;
  @Inject
  protected cz.slama.android.gtd.model.api.Filter filter;

  private final static String CALENDAR_API_SCOPE = "https://www.googleapis.com/auth/plus.login https://www.googleapis.com/auth/calendar"; //https://www.googleapis.com/auth/calendar

  private final static String GOOGLE_CALENDAR_API_SCOPE = "oauth2:" + CALENDAR_API_SCOPE;

  private static String SCOPE_TO_USE = GOOGLE_CALENDAR_API_SCOPE;

  @InjectView(R.id.b_google_login_button)
  SignInButton googleLoginButton;
  @InjectView(R.id.b_google_logout_button)
  Button googleLogoutButton;
//  @InjectView(R.id.b_google_refresh_token_button)
//  Button googleRefreshButton;


  /* Request code used to invoke sign in user interactions. */
  private static final int RC_SIGN_IN = 0;
  private static final int REQUEST_CODE_TOKEN_AUTH = 2;

  /* Client used to interact with Google APIs. */
  private GoogleApiClient mGoogleApiClient;

  /**
   * True if the sign-in button was clicked.  When true, we know to resolve all
   * issues preventing sign-in without waiting.
   */
  private boolean mSignInClicked;

  /**
   * True if we are in the process of resolving a ConnectionResult
   */
  private boolean mIntentInProgress;

  Task taskItem = null;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.google_login);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);

    taskItem = (Task) getIntent().getExtras().getSerializable(TASK);

    mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(Plus.API)
        .addScope(Plus.SCOPE_PLUS_PROFILE)
        .build();

    googleLoginButton.setOnClickListener(this);
    processButtonsVisibility();
  }

  @Override
  public void onConnectionFailed(ConnectionResult result) {
    if (!mIntentInProgress) {
      if (mSignInClicked && result.hasResolution()) {
        // The user has already clicked 'sign-in' so we attempt to resolve all
        // errors until the user is signed in, or they cancel.
        try {
          result.startResolutionForResult(this, RC_SIGN_IN);
          mIntentInProgress = true;
        }
        catch (IntentSender.SendIntentException e) {
          // The intent was canceled before it was sent.  Return to the default
          // state and attempt to connect to get an updated ConnectionResult.
          mIntentInProgress = false;
          mGoogleApiClient.connect();
        }
      }
    }
    else {
      Toast.makeText(this, "Chyba!", Toast.LENGTH_LONG).show();
    }
  }

  @Override
  public void onClick(View view) {
    if (view.getId() == R.id.b_google_login_button && !mGoogleApiClient.isConnecting()) {
      if (mGoogleApiClient.isConnected()) {
        mGoogleApiClient.clearDefaultAccountAndReconnect();
      }
      else {
        mSignInClicked = true;
        mGoogleApiClient.connect();
      }
    }
    processButtonsVisibility();
  }

  private void processButtonsVisibility() {
    if (ShrPrefUtils.isGoogleToken()) {
      googleLoginButton.setVisibility(View.GONE);
      googleLogoutButton.setVisibility(View.VISIBLE);
//      googleRefreshButton.setVisibility(View.VISIBLE);
    }
    else {
      googleLoginButton.setVisibility(View.VISIBLE);
      googleLogoutButton.setVisibility(View.GONE);
//      googleRefreshButton.setVisibility(View.GONE);
    }
  }

  public void handleGoogleSignOut(View view) {
    obtainToken();
  }

  public void obtainToken() {
    AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
      @Override
      protected String doInBackground(Void... params) {
        String token = null;

        try {
          String accountName = Plus.AccountApi.getAccountName(mGoogleApiClient);
          //seems like we dont need offline token
//          Bundle bundle = new Bundle();
          //          bundle
          token = GoogleAuthUtil.getToken(
              GoogleActivity.this,
              accountName,
              SCOPE_TO_USE);
        }
        catch (IOException transientEx) {
          System.out.println("error");
          showErrorText(getString(R.string.label_something_wrong));
        }
        catch (UserRecoverableAuthException e) {
//          System.out.println("error");
          // Recover (with e.getIntent())
          Intent recover = e.getIntent();
          startActivityForResult(recover, REQUEST_CODE_TOKEN_AUTH);
          finish();
        }
        catch (GoogleAuthException authEx) {
          showErrorText(getString(R.string.label_something_wrong));
          // The call is not ever expected to succeed
          // assuming you have already verified that
          // Google Play services is installed.
        }
        return token;
      }

      @Override
      protected void onPostExecute(String s) {
        super.onPostExecute(s);
        processGoogleToken(s);
//        System.out.println("Token Value: " + s);
      }
    };
    task.execute();
  }

  private void processGoogleToken(String s) {
    String errorText = "";
    if (s == null || s.isEmpty()) {
      errorText = getString(R.string.google_error_cannot_obtain_token);
      Toaster.showLong(this, errorText);
      return;
    }
    try {
      ShrPrefUtils.saveGoogleToken(s);
    }
    catch (Exception e) {
      errorText = getString(R.string.google_error_cannot_save_token);
      Toaster.showLong(this, errorText);
      return;
    }
    startActivity(new Intent(this, TaskActivity.class).putExtra(TASK, taskItem));
  }

  private void signOutProcessed(Status status) {
    System.out.println("Jedeme");
  }

  @Override
  public void onConnected(Bundle connectionHint) {
    mSignInClicked = false;
    Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();
    obtainToken();
  }

  @Override
  protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
    if (requestCode == RC_SIGN_IN) {
      if (responseCode != RESULT_OK) {
        mSignInClicked = false;
      }
      mIntentInProgress = false;

      if (!mGoogleApiClient.isConnected()) {
        mGoogleApiClient.reconnect();
      }
    }
    if (requestCode == REQUEST_CODE_TOKEN_AUTH && responseCode == RESULT_OK) {
      Bundle extra = intent.getExtras();
      String oneTimeToken = extra.getString("authtoken");
    }
  }

  public void onConnectionSuspended(int cause) {
    mGoogleApiClient.connect();
  }

  /**
   * Called whenever this activity is pushed to the foreground, such as after
   * a call to onCreate().
   */
  @Override
  protected void onResume() {
    super.onResume();
    bus.register(this);
  }


  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      // This is the home button in the top left corner of the screen.
      case android.R.id.home:
        // Don't call finish! Because activity could have been started by an
        // outside activity and the home button would not operated as expected!
        startActivity(new Intent(this, TaskActivity.class).putExtra(TASK, taskItem));
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void errorLogin(FacebookException exception) {
    Toaster.showLong(this, R.string.google_error_google_exception);
    return;
  }

  @Override
  protected void onPause() {
    super.onPause();
    bus.unregister(this);
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
    Toaster.showLong(GoogleActivity.this, R.string.message_bad_credentials);
  }

  @Subscribe
  public void onNetworkErrorEvent(NetworkErrorEvent networkErrorEvent) {
    Toaster.showLong(GoogleActivity.this, R.string.message_bad_network);
  }

  @Subscribe
  public void onRestAdapterErrorEvent(RestAdapterErrorEvent restAdapterErrorEvent) {
    Toaster.showLong(GoogleActivity.this, R.string.message_bad_restRequest);
  }

  @Subscribe
  public void onAlreadyReportedErrorEvent(AlreadyReportedErrorEvent alreadyReportedErrorEvent) {
    Toaster.showLong(GoogleActivity.this, R.string.message_bad_restRequest);
  }

  @Subscribe
  public void onBadRequestErrorEvent(BadRequestErrorEvent badRequestErrorEvent) {
    Toaster.showLong(GoogleActivity.this, R.string.message_bad_restRequest);
  }

  public void showErrorText(String errorText) {
    Toaster.showLong(GoogleActivity.this, errorText);
  }

//  public void handleGoogleRefreshToken(View view) {
//    obtainToken();
//  }

  public void handleGoogleLogout(View view) {

    try {
      if (mGoogleApiClient.isConnected()) {
        Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
        Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient)
            .setResultCallback(new ResultCallback<Status>() {

              @Override
              public void onResult(Status status) {
                mGoogleApiClient.disconnect();
                logoutResult(status);
              }
            });
        Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
      }
    }
    catch (Exception e) {
      logoutResult(null);
      return;
    }
    finally {
      ShrPrefUtils.removeGoogleToken();
      processButtonsVisibility();
    }
    startActivity(new Intent(this, TaskActivity.class).putExtra(TASK, taskItem));
    finish();
  }

  private void logoutResult(Status status) {
    if (status == null || !status.isSuccess()) {
      Toaster.showLong(GoogleActivity.this, R.string.message_google_logout_failed);
    }
    else {
      Toaster.showLong(GoogleActivity.this, R.string.message_google_logout_complete);
    }
    processButtonsVisibility();
  }
}

//private final static String ANDROID_DEBUG_CLIENT_ID = "1062095970935-r9ldmn5ghfm9b33bc5u7epije7738o8n.apps.googleusercontent.com";
//private final static String ANDROID_CLIENT_ID = "1062095970935-bc3nrpesoo3nkrabhei7fvfcge4d7939.apps.googleusercontent.com";
//private final static String WEBAPP_CLIENT_ID =  "1062095970935-vct1p8o74com9ngu1d1m6o68o0db6oa7.apps.googleusercontent.com";
//private final static String SERVICE_CLIENT_ID =  "1062095970935-633se69qdifgu3j1a9qguq2ke66s4i92.apps.googleusercontent.com";

//  private final static String GOOGLE_CALENDAR_API_SCOPE = "oauth2:server:client_id:" + ANDROID_CLIENT_ID +
//      ":api_scope:https://www.googleapis.com/auth/plus.login https://www.googleapis.com/auth/calendar";

//private final static String GOOGLE_CALENDAR_API_SCOPE_SERVISE = "oauth2:server:client_id:" + SERVICE_CLIENT_ID + ":api_scope:" + CALENDAR_API_SCOPE;
//private final static String GOOGLE_CALENDAR_API_SCOPE_SERVER = "oauth2:server:client_id:" + ANDROID_DEBUG_CLIENT_ID + ":api_scope:" + CALENDAR_API_SCOPE;

//  private final static String GOOGLE_CALENDAR_API_SCOPE = "oauth2:https://www.googleapis.com/auth/plus.login"; //https://www.googleapis.com/auth/calendar
//private final static String WEB_CALENDAR_API_SCOPE = "audience:server:client_id:"+WEBAPP_CLIENT_ID+":api_scope:https://www.googleapis.com/auth/calendar";
//private final static String mScope = "oauth2:server:client_id:123456789-dgrgfdgfdgfdgngemhmtfko16f5tnobqphb6v.apps.googleusercontent.com:api_scope:https://www.googleapis.com/auth/plus.login";
