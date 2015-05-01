package cz.slama.android.gtd.core;

import cz.slama.android.gtd.model.Project;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.*;

import java.util.List;


/**
 * Interface for defining the news service to communicate with Parse.com
 */
public interface ProjectService {

//    @GET(Constants.Http.URL_NEWS_FRAG)
//    NewsWrapper getNews();

    @GET(Constants.Http.URL_PROJECTS_FRAG)
    List<Project> getAllProjects();

    @POST(Constants.Http.URL_PROJECTS_FRAG)
    Project createProject(@Body Project project);

    @GET(Constants.Http.URL_PROJECT_FRAG)
    Project getProject(@Path(Constants.Http.PARAM_PROJECT_ID) Integer projectId);

    @PUT(Constants.Http.URL_PROJECT_FRAG)
    Project updateProject(@Path(Constants.Http.PARAM_PROJECT_ID) Integer projectId, @Body Project project);

    @DELETE(Constants.Http.URL_PROJECT_FRAG)
    Response deleteProject(@Path(Constants.Http.PARAM_PROJECT_ID) Integer projectId);


}
