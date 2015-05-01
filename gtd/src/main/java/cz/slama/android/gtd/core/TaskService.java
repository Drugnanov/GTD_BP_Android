package cz.slama.android.gtd.core;

import cz.slama.android.gtd.model.TaskFacebookPublish;
import cz.slama.android.gtd.model.Task;
import cz.slama.android.gtd.model.TaskGooglePublish;
import retrofit.client.Response;
import retrofit.http.*;

import java.util.List;


public interface TaskService {

    @GET(Constants.Http.URL_TASKS_FRAG)
    List<Task> getAllTasks();

    @POST(Constants.Http.URL_TASKS_FRAG)
    Task createTask(@Body Task task);

    @GET(Constants.Http.URL_TASK_FRAG)
    Task getTask(@Path(Constants.Http.PARAM_TASK_ID) Integer taskId);

    @PUT(Constants.Http.URL_TASK_FRAG)
    Task updateTask(@Path(Constants.Http.PARAM_TASK_ID) Integer taskId, @Body Task task);

    @DELETE(Constants.Http.URL_TASK_FRAG)
    Response deleteTask(@Path(Constants.Http.PARAM_TASK_ID) Integer taskId);

    @POST(Constants.Http.URL_TASK_FACEBOOK_FRAG)
    Response publishTaskToFacebook(@Path(Constants.Http.PARAM_TASK_ID) Integer taskId, @Body TaskFacebookPublish facebookPublish);

    @POST(Constants.Http.URL_TASK_GOOGLE_FRAG)
    Response publishTaskToGoogle(@Path(Constants.Http.PARAM_TASK_ID) Integer taskId, @Body TaskGooglePublish googlePublish);
}
