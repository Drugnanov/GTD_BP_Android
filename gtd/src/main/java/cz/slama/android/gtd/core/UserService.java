package cz.slama.android.gtd.core;

import cz.slama.android.gtd.model.Person;
import cz.slama.android.gtd.model.PersonAuth;
import cz.slama.android.gtd.model.PersonCreate;
import cz.slama.android.gtd.model.Task;
import retrofit.http.*;

/**
 * User service for connecting the the REST API and
 * getting the users.
 */
public interface UserService {

    @GET(Constants.Http.URL_AUTH_FRAG) // "/authenticate/{userName}"
    PersonAuth authenticate(@Path(Constants.Http.PARAM_USERNAME) String username, @Header(Constants.Http.PARAM_PASSWORD) String password);

    @POST(Constants.Http.URL_PERSONS_FRAG)
    Person createPerson(@Body PersonCreate personCreate);

    @GET(Constants.Http.URL_PERSON_BY_TOKEN_FRAG)
    Person getUsersByToken();

    @PUT(Constants.Http.URL_PERSON_FRAG)
    Person updatePerson(@Path(Constants.Http.PARAM_PERSON_ID) Integer personId, @Body Person person);



//    /**
//     * The {@link retrofit.http.Query} values will be transform into query string paramters
//     * via Retrofit
//     *
//     * @param email The users email
//     * @param password The users password
//     * @return A login response.
//     */
//    @GET(Constants.Http.URL_AUTH_FRAG)
//    User authenticate(@Query(Constants.Http.PARAM_USERNAME) String email,
//                               @Query(Constants.Http.PARAM_PASSWORD) String password);
}
