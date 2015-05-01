

package cz.slama.android.gtd.core;

/**
 * Bootstrap constants
 */
public final class Constants {

  private Constants() {}

  public static final class Auth {

    private Auth() {}

    /**
     * Account type id
     */
    public static final String BOOTSTRAP_ACCOUNT_TYPE = "cz.slama.android.gtd";

    /**
     * Account name
     */
    public static final String BOOTSTRAP_ACCOUNT_NAME = "gtd";

    /**
     * Provider id
     */
    public static final String BOOTSTRAP_PROVIDER_AUTHORITY = "cz.slama.android.gtd.sync";

    /**
     * Auth token type
     */
    public static final String AUTHTOKEN_TYPE = BOOTSTRAP_ACCOUNT_TYPE;
  }

  /**
   * All HTTP is done through a REST style API built for demonstration purposes on Parse.com
   * Thanks to the nice people at Parse for creating such a nice system for us to use for bootstrap!
   */
  public static final class Http {

    private Http() {}


    /**
     * Base URL for all requests
     */
    //localhost
    public static final String URL_BASE = "http://10.0.3.2:8080/gtd/api/v1";
    //production
//        public static final String URL_BASE = "http://188.166.107.248:8080/gtd/api/v1";

    /**
     * Authentication URL
     */
    public static final String URL_AUTH_FRAG = "/authenticate/{" + Constants.Http.PARAM_USERNAME + "}";

    /**
     * List Persons
     */
    public static final String URL_PERSONS_FRAG = "/persons";
    public static final String URL_PERSON_FRAG = URL_PERSONS_FRAG + "/{" + Constants.Http.PARAM_PERSON_ID + "}";
    public static final String URL_PERSON_BY_TOKEN_FRAG = URL_PERSONS_FRAG + "/auth";

    /**
     * List Projects
     */
    public static final String URL_PROJECTS_FRAG = "/projects";
    public static final String URL_PROJECT_FRAG = URL_PROJECTS_FRAG+"/{" + Constants.Http.PARAM_PROJECT_ID + "}";

    /**
     * List Tasks
     */
    public static final String URL_TASKS_FRAG = "/tasks";
    public static final String URL_TASK_FRAG = URL_TASKS_FRAG + "/{" + Constants.Http.PARAM_TASK_ID + "}";
    public static final String URL_TASK_FACEBOOK_FRAG = URL_TASK_FRAG + "/facebookPublish";
    public static final String URL_TASK_GOOGLE_FRAG = URL_TASK_FRAG + "/googlePublish";

    /**
     * List Contexts
     */
    public static final String URL_CONTEXTS_FRAG = "/contexts";
    public static final String URL_CONTEXT_FRAG = URL_CONTEXTS_FRAG + "/{" + Http.PARAM_CONTEXT_ID + "}";

    /**
     * List Users URL
     */
    public static final String URL_USERS_FRAG = "/1/users";
    public static final String URL_USERS = URL_BASE + URL_USERS_FRAG;


    /**
     * List News URL
     */
    public static final String URL_NEWS_FRAG = "/1/classes/News";
    public static final String URL_NEWS = URL_BASE + URL_NEWS_FRAG;


    /**
     * List Checkin's URL
     */
    public static final String URL_CHECKINS_FRAG = "/1/classes/Locations";
    public static final String URL_CHECKINS = URL_BASE + URL_CHECKINS_FRAG;

    /**
     * PARAMS for auth
     */
    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_PROJECT_ID = "projectId";
    public static final String PARAM_TASK_ID = "taskId";
    public static final String PARAM_CONTEXT_ID = "contextId";
    public static final String PARAM_PERSON_ID = "personId";


    public static final String PARSE_APP_ID = "zHb2bVia6kgilYRWWdmTiEJooYA17NnkBSUVsr4H";
    public static final String PARSE_REST_API_KEY = "N2kCY1T3t3Jfhf9zpJ5MCURn3b25UpACILhnf5u9";
    public static final String HEADER_PARSE_REST_API_KEY = "X-Parse-REST-API-Key";
    public static final String HEADER_PARSE_APP_ID = "X-Parse-Application-Id";
    public static final String HEADER_GTD_TOKEN = "token";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String SESSION_TOKEN = "sessionToken";
  }


  public static final class Extra {

    private Extra() {}

    public static final String PROJECT_ITEM = "project_item";

    public static final String TASK = "task";

    public static final String CONTEXT = "context";

    public enum DataTypeToReload {
      None,
      Project,
      Task,
      Context
    }

    public static final String DATA_TYPE = "data_type";

    public static final String NEWS_ITEM = "news_item";
  }

  public static final class Intent {

    private Intent() {}

    /**
     * Action prefix for all intents created
     */
    public static final String INTENT_PREFIX = "cz.slama.android.gtd.";
  }

  public static class Notification {

    private Notification() {
    }

    public static final int TIMER_NOTIFICATION_ID = 1000; // Why 1000? Why not? :)
  }

  public static class FormValidation {

    private FormValidation(){
    }

    public static final int PERSON_MAX_LENGTH_FIRST_NAME = 20;
    public static final int PERSON_MAX_LENGTH_LAST_NAME = 20;
    public static final int PERSON_MIN_LENGTH_LOGIN = 5;
    public static final int PERSON_MAX_LENGTH_LOGIN = 20;
    public static final int PERSON_MIN_LENGTH_PASSWORD = 5;
    public static final int PERSON_MAX_LENGTH_PASSWORD = 50;

    public static final int TASK_MIN_LENGTH_TITLE = 1;
    public static final int TASK_MAX_LENGTH_TITLE = 100;
    public static final int TASK_MAX_LENGTH_DESCRIPTION = 1000;

    public static final int PROJECT_MIN_LENGTH_TITLE = 1;
    public static final int PROJECT_MAX_LENGTH_TITLE = 100;
    public static final int PROJECT_MAX_LENGTH_DESCRIPTION = 1000;

    public static final int CONTEXT_MIN_LENGTH_TITLE = 2;
    public static final int CONTEXT_MAX_LENGTH_TITLE = 100;
  }
}


