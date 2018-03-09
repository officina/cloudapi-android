package cc.officina.cloudapilib.retrofit;

import java.util.List;

import rx.Observable;

/**
 * Created on 16/04/16.
 *
 * @author Umberto Marini
 */
public interface NetworkClientInterface {

    // location
    Observable<List<String>> locations(double latitude, double longitude, String query, int limit, int radius);
}
