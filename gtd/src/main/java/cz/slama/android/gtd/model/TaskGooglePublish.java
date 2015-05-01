package cz.slama.android.gtd.model;

/**
 * Created by Drugnanov on 25.4.2015.
 */
public class TaskGooglePublish {

  private String accessToken;

  private String userMessage;

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getUserMessage() {
    return userMessage;
  }

  public void setUserMessage(String userMessage) {
    this.userMessage = userMessage;
  }
}
