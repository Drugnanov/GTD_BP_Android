package cz.slama.android.gtd.core;


import static cz.slama.android.gtd.core.Constants.Http.HEADER_PARSE_APP_ID;
import static cz.slama.android.gtd.core.Constants.Http.HEADER_PARSE_REST_API_KEY;
import static cz.slama.android.gtd.core.Constants.Http.PARSE_APP_ID;
import static cz.slama.android.gtd.core.Constants.Http.PARSE_REST_API_KEY;

import cz.slama.android.gtd.persistence.ShrPrefUtils;
import cz.slama.android.gtd.utils.Compare;
import retrofit.RequestInterceptor;

public class RestAdapterRequestInterceptor implements RequestInterceptor {

    private UserAgentProvider userAgentProvider;

    public RestAdapterRequestInterceptor(UserAgentProvider userAgentProvider) {
        this.userAgentProvider = userAgentProvider;
    }

    @Override
    public void intercept(RequestFacade request) {

        // Add header to set content type of JSON
        request.addHeader("Content-Type", "application/json");

        // Add auth info for PARSE, normally this is where you'd add your auth info for this request (if needed).
//        request.addHeader(HEADER_PARSE_REST_API_KEY, PARSE_REST_API_KEY);
//        request.addHeader(HEADER_PARSE_APP_ID, PARSE_APP_ID);
        String token = ShrPrefUtils.getUserToken();
        boolean isToken = !Compare.isNullOrEmpty(token);
        if (isToken){
            request.addHeader(Constants.Http.HEADER_GTD_TOKEN, token);
        }
        // Add the user agent to the request.
        request.addHeader("User-Agent", userAgentProvider.get());

//        request.addHeader("token", userAgentProvider.get());
    }
}
