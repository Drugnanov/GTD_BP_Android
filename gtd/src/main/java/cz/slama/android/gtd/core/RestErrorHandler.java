package cz.slama.android.gtd.core;

import cz.slama.android.gtd.events.*;
import com.squareup.otto.Bus;

import cz.slama.android.gtd.ui.exceptions.TokenExpiredException;
import cz.slama.android.gtd.ui.exceptions.UnauthorizedException;
import retrofit.ErrorHandler;
import retrofit.RetrofitError;

public class RestErrorHandler implements ErrorHandler {

  public static final int HTTP_NOT_FOUND = 404;
  public static final int HTTP_UNAUTHORIZED = 401;
  public static final int INVALID_LOGIN_PARAMETERS = 101;
  public static final int HTTP_BAD_REQUEST = 400;
  public static final int HTTP_ALREADY_REPORTED = 208;
  public static final int HTTP_TOKEN_EXPIRED = 412;

  private Bus bus;

  public RestErrorHandler(Bus bus) {
    this.bus = bus;
  }

  @Override
  public Throwable handleError(RetrofitError cause) {
    if (cause != null) {
      if (cause.isNetworkError()) {
        bus.post(new NetworkErrorEvent(cause));
      }
      else if (isUnAuthorized(cause)) {
        bus.post(new UnAuthorizedErrorEvent(cause));
      }
      else if (isBadRequest(cause)) {
        bus.post(new BadRequestErrorEvent(cause));
      }
      else if (isAlreadyReported(cause)) {
        bus.post(new AlreadyReportedErrorEvent(cause));
      }
      else {
        bus.post(new RestAdapterErrorEvent(cause));
      }
    }

    if (cause != null && cause.getResponse() != null
        && cause.getResponse().getStatus() == HTTP_UNAUTHORIZED) {
      return new UnauthorizedException(cause.getResponse());
    }
    if (cause != null && cause.getResponse() != null
        && isTokenExpired(cause)) {
      return new TokenExpiredException(cause.getResponse());
    }

    return cause;
  }

  /**
   * If a user passes an incorrect username/password combo in we could
   * get a unauthorized error back from the API. On parse.com this means
   * we get back a HTTP 404 with an error as JSON in the body as such:
   * <p/>
   * {
   * code: 101,
   * error: "invalid login parameters"
   * }
   * <p/>
   * }
   * <p/>
   * Therefore we need to check for the 101 and the 404.
   *
   * @param cause The initial error.
   * @return
   */
  private boolean isUnAuthorized(RetrofitError cause) {
    boolean authFailed = false;

    if (cause.getResponse().getStatus() == HTTP_NOT_FOUND) {
      final ApiError err = (ApiError) cause.getBodyAs(ApiError.class);
      if (err != null && err.getCode() == INVALID_LOGIN_PARAMETERS) {
        authFailed = true;
      }
    }
    if (cause.getResponse().getStatus() == HTTP_UNAUTHORIZED) {
      authFailed = true;
    }

    return authFailed;
  }

  /* Chech for the 400 as error from user side
      *
      * @param cause The initial error.
      * @return
      */
  private boolean isBadRequest(RetrofitError cause) {
    if (cause.getResponse().getStatus() == HTTP_BAD_REQUEST) {
      return true;
    }
    return false;
  }

  private boolean isAlreadyReported(RetrofitError cause) {
    if (cause.getResponse().getStatus() == HTTP_ALREADY_REPORTED) {
      return true;
    }
    return false;
  }

  private boolean isTokenExpired(RetrofitError cause) {
    if (cause.getResponse().getStatus() == HTTP_TOKEN_EXPIRED) {
      return true;
    }
    return false;
  }

}
