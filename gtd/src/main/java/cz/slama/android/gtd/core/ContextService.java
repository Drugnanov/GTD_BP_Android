package cz.slama.android.gtd.core;

import cz.slama.android.gtd.model.ContextGtd;
import retrofit.client.Response;
import retrofit.http.*;

import java.util.List;


public interface ContextService {
    @GET(Constants.Http.URL_CONTEXTS_FRAG)
    List<ContextGtd> getAllContexts();

    @POST(Constants.Http.URL_CONTEXTS_FRAG)
    ContextGtd createContext(@Body ContextGtd context);

    @GET(Constants.Http.URL_CONTEXT_FRAG)
    ContextGtd getContext(@Path(Constants.Http.PARAM_CONTEXT_ID) Integer taskId);

    @PUT(Constants.Http.URL_CONTEXT_FRAG)
    ContextGtd updateContext(@Path(Constants.Http.PARAM_CONTEXT_ID) Integer taskId, @Body ContextGtd context);

    @DELETE(Constants.Http.URL_CONTEXT_FRAG)
    Response deleteContext(@Path(Constants.Http.PARAM_CONTEXT_ID) Integer taskId);
}
