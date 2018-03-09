package cc.officina.cloudapilib.retrofit;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created on 16/04/16.
 *
 * @author Umberto Marini
 */
public interface NetworkApiInterface {

    boolean PATH_VALUE_ENCODED = true;

    @Retention(SOURCE)
    @StringDef({
            AUTHORIZATION_HEADER_NAME_FACEBOOK,
            AUTHORIZATION_HEADER_NAME_STANDARD,
            AUTHORIZATION_HEADER_NAME_OAUTH2,
            AUTHORIZATION_HEADER_NAME_DEFAULT
    })
    public @interface AuthorizationHeader {
    }

    String AUTHORIZATION_HEADER_NAME_FACEBOOK = "X-Auth-Token";
    String AUTHORIZATION_HEADER_NAME_STANDARD = "X-Auth-Token";
    String AUTHORIZATION_HEADER_NAME_OAUTH2 = "Authorization";
    String AUTHORIZATION_HEADER_NAME_DEFAULT = "Authorization";

    // location

    @GET("api/locations")
    Observable<List<String>> locations(@Query(value = "latitude", encoded = PATH_VALUE_ENCODED) double latitude,
                                       @Query(value = "longitude", encoded = PATH_VALUE_ENCODED) double longitude,
                                       @Query(value = "query", encoded = PATH_VALUE_ENCODED) String query,
                                       @Query(value = "limit", encoded = PATH_VALUE_ENCODED) int limit,
                                       @Query(value = "radius", encoded = PATH_VALUE_ENCODED) int radius);
}
