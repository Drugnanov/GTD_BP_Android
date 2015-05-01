package cz.slama.android.gtd.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import butterknife.InjectView;
import com.facebook.*;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.github.kevinsawicki.wishlist.Toaster;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import cz.slama.android.gtd.R;
import cz.slama.android.gtd.core.BootstrapService;
import cz.slama.android.gtd.events.NetworkErrorEvent;
import cz.slama.android.gtd.events.RestAdapterErrorEvent;
import cz.slama.android.gtd.events.UnAuthorizedErrorEvent;
import cz.slama.android.gtd.model.Task;
import cz.slama.android.gtd.persistence.ShrPrefUtils;

import javax.inject.Inject;
import java.util.Arrays;

import static cz.slama.android.gtd.core.Constants.Extra.TASK;

public class FacebookActivity extends BootstrapActivity {

  @Inject
  BootstrapService bootstrapService;
  @Inject
  protected cz.slama.android.gtd.model.api.Filter filter;

  @InjectView(R.id.b_facebook_login_button)
  LoginButton facebookLoginButton;

  AccessToken accessToken;

  CallbackManager callbackManager;

  Task taskItem = null;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.facebook_login);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);

    taskItem = (Task) getIntent().getExtras().getSerializable(TASK);

    callbackManager = CallbackManager.Factory.create();
    facebookLoginButton.setPublishPermissions(Arrays.asList("publish_actions"));
    LoginManager.getInstance().registerCallback(callbackManager,
        new FacebookCallback<LoginResult>() {
          @Override
          public void onSuccess(LoginResult loginResult) {
            successLogin(loginResult);
          }

          @Override
          public void onCancel() {
            cancelLogin();
          }

          @Override
          public void onError(FacebookException exception) {
            errorLogin(exception);
          }
        });
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

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    callbackManager.onActivityResult(requestCode, resultCode, data);
  }

  private void errorLogin(FacebookException exception) {
    Toaster.showLong(this, R.string.facebook_error_facebook_exception);
    return;
  }

  private void cancelLogin() {
    System.out.println("whats going on?");
  }

  private void successLogin(LoginResult loginResult) {
    String errorText = "";
    if (loginResult.getRecentlyDeniedPermissions().size() > 0) {
      errorText = getString(R.string.facebook_error_denied_permissions,
          org.apache.commons.lang.StringUtils.join(loginResult.getRecentlyDeniedPermissions(), ","));
      LoginManager.getInstance().logOut();
      Toaster.showLong(this, errorText);
      return;
    }
    try {
      accessToken = AccessToken.getCurrentAccessToken();
    }
    catch (Exception e) {
      errorText = getString(R.string.facebook_error_cannot_obtain_token);
      Toaster.showLong(this, errorText);
      return;
    }
    try {
      ShrPrefUtils.saveFacebookToken(accessToken.getToken());
    }
    catch (Exception e) {
      errorText = getString(R.string.facebook_error_cannot_save_token);
      Toaster.showLong(this, errorText);
      return;
    }
    startActivity(new Intent(this, TaskActivity.class).putExtra(TASK, taskItem));
  }

  private boolean logoutFromFacebook() {
    try {
      LoginManager.getInstance().logOut();
    }
    catch (Exception e) {
      return false;
    }
    ShrPrefUtils.removeFacebookToken();
    return true;
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
    Toaster.showLong(FacebookActivity.this, R.string.message_bad_credentials);
  }

  @Subscribe
  public void onNetworkErrorEvent(NetworkErrorEvent networkErrorEvent) {
    Toaster.showLong(FacebookActivity.this, R.string.message_bad_network);
  }

  @Subscribe
  public void onRestAdapterErrorEvent(RestAdapterErrorEvent restAdapterErrorEvent) {
    Toaster.showLong(FacebookActivity.this, R.string.message_bad_restRequest);
  }

  public void showErrorText(String errorText) {
    Toaster.showLong(FacebookActivity.this, errorText);
  }
}
