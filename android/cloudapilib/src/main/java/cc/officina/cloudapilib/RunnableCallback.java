package cc.officina.cloudapilib;

/**
 * Created by riccardogazzea on 06/06/16.
 */
public interface RunnableCallback {
    void success(int statusCode, Object responseObject);

    void failure(int statusCode, Exception e);
}
