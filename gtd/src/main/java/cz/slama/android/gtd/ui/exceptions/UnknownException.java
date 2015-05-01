package cz.slama.android.gtd.ui.exceptions;

import retrofit.client.Response;

/**
 * Created by Drugnanov on 23.3.2015.
 */
public class UnknownException extends Throwable {

  private Response response;

  public UnknownException(Response pResponse) {
    this.response = pResponse;
  }

  public Response getResponse() {
    return response;
  }

  public void setResponse(Response response) {
    this.response = response;
  }
}
