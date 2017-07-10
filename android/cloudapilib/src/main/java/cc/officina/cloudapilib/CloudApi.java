package cc.officina.cloudapilib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.compat.BuildConfig;
import android.util.Base64;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.StringEntity;
import io.realm.Realm;

/**
 * Created by riccardogazzea on 06/06/16.
 */
public class CloudApi {
    private String hostName;
    private static CloudApi instance = null;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private AuthenticationType authenticationType;
    private Context context;
    private String settingsString;
    private AsyncHttpClient client = new AsyncHttpClient();

    public Class getFirstActivity() {
        return firstActivity;
    }

    public void setFirstActivity(Class firstActivity) {
        this.firstActivity = firstActivity;
    }

    private Class firstActivity;

    protected CloudApi() {
        // Exists only to defeat instantiation.
    }

    public static void setSettingsString(String settingsString) {
        instance.settingsString = settingsString;
        instance.settings = configSharedPref(instance.settingsString, instance.context);
    }

    public static CloudApi getInstance(Context context) {
        if(instance == null) {
            instance = new CloudApi();
            instance.authenticationType = AuthenticationType.Oauth2;
            instance.context = context;

            if (BuildConfig.DEBUG){
                instance.client.setProxy(System.getProperty("http.proxyHost"), Integer.parseInt(System.getProperty("http.proxyPort")));
            }
        }
        return instance;
    }

    public enum Method{
        GET,
        POST,
        PUT,
        DELETE
    }
    public enum ParameterEncoding{
        JSON,
        URL,
        RECOVER_PASSWORD
    }
    public enum AuthenticationType{

        Standard,
        Facebook,
        Oauth2,
        None
    }

    public enum FunOrigin{
        Action,
        Authentication,
    }

    String getHostName(){
        return this.hostName;
    }
    public void setHostName(String hostName){
        this.hostName = hostName;
    }

    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(AuthenticationType authenticationType) {
        this.authenticationType = authenticationType;
    }

    public void authenticate(String endpoint,
                             Map<String, String> headers,
                             Map<String, Object> params ,
                             final RunnableCallback callback) {
        editor = settings.edit();
        if (authenticationType == AuthenticationType.Oauth2){
            try{
                if (params != null){
                    if (params.get("grant_type").toString().equals("refresh_token")){
                        String authorization = "";
                        String clientId = params.get("client_id").toString();
                        String clientSecret = params.get("client_secret").toString();
                        byte[] data = (clientId+":"+clientSecret).getBytes();
                        authorization ="Basic "+ Base64.encodeToString(data, Base64.DEFAULT);
                        authorization = authorization.trim();
                        params.put("refresh_token", settings.getString("refresh_token", ""));
                        params.put("scope", settings.getString("scope", ""));
                        headers.put("Authorization", authorization);
                    }
                }else{
                    params = new HashMap<>();
                    params.put("grant_type", "refresh_token");
                    params.put("client_id", settings.getString("clientId",""));
                    params.put("client_secret", settings.getString("clientSecret",""));
                    params.put("refresh_token", settings.getString("refresh_token", ""));
                    params.put("scope", settings.getString("scope", ""));
                    String authorization = "";
                    String clientId = params.get("client_id").toString();
                    String clientSecret = params.get("client_secret").toString();
                    byte[] data = (clientId+":"+clientSecret).getBytes();
                    authorization ="Basic "+ Base64.encodeToString(data, Base64.DEFAULT);
                    authorization = authorization.trim();
                    headers.put("Authorization", authorization);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        final RequestParams requestParams = new RequestParams();
        if (params != null && !params.isEmpty()){
            for (Map.Entry<String, Object> entry : params.entrySet()){
                requestParams.put(entry.getKey(), entry.getValue());
            }
        }
        headers.put("Content-type", "application/x-www-form-urlencoded");
        buildHeaders(headers);
        client.post(getHostName() + endpoint, requestParams, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                if (statusCode >= 200 && statusCode < 400){
                    callback.success(statusCode, response);
                    if (authenticationType == AuthenticationType.Standard || authenticationType == AuthenticationType.Facebook){
                        try {
                            editor.putString("token", response.getString("token"));
                            editor.putBoolean("isAuth", true);
                            editor.apply();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else if(authenticationType == AuthenticationType.Oauth2){
                        try {
                            editor.putString("token", response.getString("access_token"));
                            editor.putString("refresh_token", response.getString("refresh_token"));
                            editor.putBoolean("isAuth", true);
                            editor.apply();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }else{
                    callback.failure(statusCode, response);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                callback.failure(statusCode, errorResponse);
            }
        });
        editor.putString("auth_type", authenticationType.toString());
        editor.putString("auth_endpoint", endpoint);
        editor.apply();
    }

    private void buildHeaders(Map<String, String> headers){
        if (headers != null && !headers.isEmpty()){
            client.removeAllHeaders();
            for (Map.Entry<String, String> entry : headers.entrySet()){
                headers.put(entry.getKey(), entry.getValue());
                client.addHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    private StringEntity buildParams(Map<String, Object> map){
        JSONObject object = new JSONObject(map);
        return new StringEntity(object.toString(), Charset.defaultCharset());
    };

    private void loginEaction(final String endpoint, final Method method, final Map<String, Object> parameters, final ParameterEncoding encoding, final Map<String, String> headers, final FunOrigin funOrigin, final RunnableCallback callback){
        Map<String,Object> params = new HashMap<>();
        params.put("username", settings.getString("login",""));
        params.put("password", settings.getString("password",""));
        authenticate(settings.getString("auth_endpoint", ""), new HashMap<String, String>(), params, new RunnableCallback() {
            @Override
            public void success(int statusCode, Object responseObject) {
                action(endpoint, method, parameters, encoding, headers, funOrigin,callback);
            }

            @Override
            public void failure(int statusCode, Object e) {
                if (statusCode == 401){
                    //se entro in questa casistica significa che le credenziali dell'account sono state cambiate
                    // nel server e non in locale quindi ho bisogno che l'utente riesegua il login
                    settings.edit().clear().apply();
                    Realm.init(context);
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    realm.deleteAll();
                    realm.commitTransaction();
                    ((Activity)context).finishAffinity();
                    context.startActivity(new Intent(context, getFirstActivity()));
                }
            }
        });
    }

    public void action(final String endpoint, final Method method, final Map<String, Object> parameters, final ParameterEncoding encoding, final Map<String, String> headers,final FunOrigin funOrigin, final RunnableCallback callback){
        TextHttpResponseHandler textHttpResponseHandler = new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] newHeaders, String responseString, Throwable throwable) {
                if (statusCode == 401){
                    loginEaction(endpoint, method, parameters, encoding, headers, funOrigin, callback);
                }else{
                    if (statusCode >= 200 && statusCode < 400) {
                        callback.success(statusCode, responseString);
                    }else{
                        callback.failure(statusCode, responseString);
                    }
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] newHeaders, String responseString) {
                if (statusCode == 401){
                    loginEaction(endpoint, method, parameters, encoding, headers, funOrigin, callback);
                }else{
                    if (statusCode >= 200 && statusCode < 400) {
                        callback.success(statusCode, responseString);
                    }else{
                        callback.failure(statusCode, responseString);
                    }
                }
            }
        };

        JsonHttpResponseHandler jsonHttpResponseHandler = new JsonHttpResponseHandler(){

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                if (statusCode >= 200 && statusCode < 400){
                    callback.success(statusCode, responseString);
                }else{
                    callback.failure(statusCode, responseString);
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                super.onSuccess(statusCode, headers, responseString);
                if (statusCode >= 200 && statusCode < 400){
                    callback.success(statusCode, responseString);
                }else{
                    callback.failure(statusCode, responseString);
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] newHeaders, JSONObject response) {
                super.onSuccess(statusCode, newHeaders, response);
                if (statusCode == 401){
                    loginEaction(endpoint, method, parameters, encoding, headers, funOrigin, callback);
                }else{
                    if (statusCode >= 200 && statusCode < 400) {
                        callback.success(statusCode, response);
                    }else{
                        callback.failure(statusCode, response);
                    }
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] newHeaders, JSONArray response) {
                super.onSuccess(statusCode, newHeaders, response);
                if (statusCode == 401){
                    loginEaction(endpoint, method, parameters, encoding, headers, funOrigin, callback);
                }else{
                    if (statusCode >= 200 && statusCode < 400) {
                        callback.success(statusCode, response);
                    }else{
                        callback.failure(statusCode, response);
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] newHeaders, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, newHeaders, throwable, errorResponse);
                if (statusCode == 401){
                    loginEaction(endpoint, method, parameters, encoding, headers, funOrigin, callback);
                }else{
                    if (statusCode >= 200 && statusCode < 400) {
                        callback.success(statusCode, errorResponse);
                    }else{
                        callback.failure(statusCode, errorResponse);
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] newHeaders, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, newHeaders, throwable, errorResponse);
                if (statusCode == 401){
                    loginEaction(endpoint, method, parameters, encoding, headers, funOrigin, callback);
                }else{
                    if (statusCode >= 200 && statusCode < 400) {
                        callback.success(statusCode, errorResponse);
                    }else{
                        callback.failure(statusCode, errorResponse);
                    }
                }
            }
        };
        if (headers != null){
            if (headers.get("Content-type") == null){
                headers.put("Content-type", "application/json");
            }
            switch (authenticationType){
                case Facebook:
                    headers.put("X-Auth-Token", settings.getString("token", ""));
                    break;
                case Standard:
                    headers.put("X-Auth-Token", settings.getString("token", ""));
                    break;
                case Oauth2:
                    //dovrebbe già avere l'header
                    headers.put("Authorization", "Bearer "+ settings.getString("token", ""));
                    break;
                default:
                    headers.put("Authorization", "");
                    break;
            }
        }
        buildHeaders(headers);
        switch (encoding){
            case JSON:
                StringEntity request = buildParams(parameters);
                switch (method){
                    case DELETE:
                        client.delete(context, getHostName() + endpoint, request, "application/json", jsonHttpResponseHandler);
                        break;
                    case GET:
                        client.get(context, getHostName() + endpoint, request, "application/json", jsonHttpResponseHandler);
                        break;
                    case POST:
                        client.post(context, getHostName() + endpoint, request, "application/json", jsonHttpResponseHandler);
                        break;
                    case PUT:
                        client.put(context, getHostName() + endpoint, request, "application/json", jsonHttpResponseHandler);
                        break;
                    default:
                        client.get(context, getHostName() + endpoint, request, "application/json", jsonHttpResponseHandler);
                        break;
                }
                break;
            case URL:
                String queryiedEndpoint = buildPath(endpoint, parameters);
                switch (method){
                    case DELETE:
                        client.delete(getHostName() + queryiedEndpoint, null, jsonHttpResponseHandler);
                        break;
                    case GET:
                        client.get(getHostName() + queryiedEndpoint, null, jsonHttpResponseHandler);
                        break;
                    case POST:
                        client.post(getHostName() + queryiedEndpoint, null, jsonHttpResponseHandler);
                        break;
                    case PUT:
                        client.put(getHostName() + queryiedEndpoint, null, jsonHttpResponseHandler);
                        break;
                    default:
                        client.get(getHostName() + queryiedEndpoint, null, jsonHttpResponseHandler);
                        break;
                }
                break;
            case RECOVER_PASSWORD:
                StringEntity entity = new StringEntity(parameters.get("email").toString(), ContentType.TEXT_PLAIN);
                client.removeAllHeaders();
                switch (method){
                    case DELETE:
                        client.delete(context, getHostName() + endpoint, entity, "text/plain", textHttpResponseHandler);
                        break;
                    case GET:
                        client.get(context, getHostName() + endpoint, entity, "text/plain", textHttpResponseHandler);
                        break;
                    case POST:
                        client.post(context, getHostName() + endpoint, entity, "text/plain", textHttpResponseHandler);
                        break;
                    case PUT:
                        client.put(context, getHostName() + endpoint, entity, "text/plain", textHttpResponseHandler);
                        break;
                    default:
                        client.get(context, getHostName() + endpoint, entity, "text/plain", textHttpResponseHandler);
                        break;
                }
                break;
        }
    }

    private String buildPath(String endpoint, Map<String, Object> params){
        endpoint = endpoint+"?";
        if (params != null && !params.isEmpty()){
            for (Map.Entry<String, Object> entry : params.entrySet()){
                endpoint = endpoint+entry.getKey()+"="+entry.getValue()+"&";
            }
        }
        return endpoint;
    }

    public static SharedPreferences configSharedPref(String appPackage, Context context){
        return context.getSharedPreferences(appPackage, Context.MODE_PRIVATE);
    }
}

