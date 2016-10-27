package cc.officina.cloudapilib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Base64;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.MalformedJsonException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by riccardogazzea on 06/06/16.
 */
public class CloudApi{
    private String hostName;
    //TODO mettere https
    private static CloudApi instance = null;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private AuthenticationType authenticationType;
    private Method authMethod;
    private OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).build();
    private String APP_PACKAGE;

    public Class getFirstActivity() {
        return firstActivity;
    }

    public void setFirstActivity(Class firstActivity) {
        this.firstActivity = firstActivity;
    }

    private Class firstActivity;
    private int ACCESS_MODE;

    protected CloudApi() {
        // Exists only to defeat instantiation.
    }
    public static CloudApi getInstance() {
        if(instance == null) {
            instance = new CloudApi();
            instance.authenticationType = AuthenticationType.Oauth2;
            instance.authMethod = Method.POST;
            instance.ACCESS_MODE = Context.MODE_PRIVATE;
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
        URL
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
        Register
    }

    public String getAPP_PACKAGE() {
        return APP_PACKAGE;
    }

    public void setAPP_PACKAGE(String APP_PACKAGE) {
        this.APP_PACKAGE = APP_PACKAGE;
    }

    public int getACCESS_MODE() {
        return ACCESS_MODE;
    }

    public void setACCESS_MODE(int ACCESS_MODE) {
        this.ACCESS_MODE = ACCESS_MODE;
    }

    public String getHostName(){
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

    private Request retrofitBuilder(Method method, final String endpoint,final Map<String, String> headers, final Map<String, Object> params, Context context, final ParameterEncoding parameterEncoding, FunOrigin funOrigin){
        if (hostName == null){
            hostName = settings.getString("hostName", "");
        }

        if (funOrigin == FunOrigin.Action){
            switch (authenticationType){
                case Facebook:
                    break;
                case Standard:
                    headers.put("X-Auth-Token", settings.getString("token", ""));
                    break;
                case Oauth2:
                    headers.put("Authorization", "Bearer "+ settings.getString("token", ""));
                    break;
                default:
                    break;
            }
        }
       Request request = new Request.Builder().url(getHostName()+endpoint).build();
        try{
            switch (parameterEncoding){
                            case JSON:
                                Gson gson = new Gson();
                                String json = gson.toJson(params);
                                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);

                                request  = request.newBuilder()
                                        .url(getHostName()+endpoint)
                                        .method(method.name(), requestBody)
                                        .build();
                                break;
                            case URL:
                                String url = getHostName()+getQueryParams(params, endpoint);

                                switch (method){
                                    case GET:
                                        request = request.newBuilder()
                                                .url(url)
                                                .get()
                                                .build();
                                        break;
                                    case DELETE:
                                        request = request.newBuilder()
                                                .url(url)
                                                .delete(RequestBody.create(MediaType.parse("text/plain"), ""))
                                                .build();
                                        break;
                                    case PUT:
                                        request = request.newBuilder()
                                                .url(url)
                                                .put(RequestBody.create(MediaType.parse("text/plain"), ""))
                                                .build();
                                        break;
                                    case POST:
                                        request = request.newBuilder()
                                                .url(url)
                                                .post(RequestBody.create(MediaType.parse("text/plain"), ""))
                                                .build();
                                        break;
                                    default:
                                        request = request.newBuilder()
                                                .url(url)
                                                .build();
                                        break;
                                }

                                break;
                            default:
                                break;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }

        if (headers != null){
            for (Map.Entry<String, String> entry : headers.entrySet()){
                request = request.newBuilder().addHeader(entry.getKey(), entry.getValue()).build();
            }
        }
        return request;
    }


    public void authenticate(String endpoint,
                             final Method method,
                             Map<String, String> headers,
                             Map<String, Object> params ,
                             final ParameterEncoding encoding,
                             final Context context,
                             String appPackage,
                             int settingsMode,
                             final RunnableCallback callback) {
        settings = configSharedPref(appPackage, settingsMode, context);
        editor = settings.edit();
        authMethod = method;
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "");
        Request request;
        switch (authenticationType){
            case Facebook:
                editor.putString("username", params.get("username").toString());
                editor.putString("password", params.get("token").toString());
                editor.putString("token", params.get("token").toString());

                request = retrofitBuilder(method,endpoint,headers, params,context, encoding, FunOrigin.Authentication);
                request = request.newBuilder().method(method.name(),requestBody).build();

                authenticateFb(callback, method, request);
                break;
            case Oauth2:
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
                request = retrofitBuilder(method, endpoint,headers, params,context,encoding, FunOrigin.Authentication);
                request = request.newBuilder().method(method.name(),requestBody).build();

                authenticateOauth2(callback, method,request);
                break;
            case Standard:
                try{
                    request = retrofitBuilder(method, endpoint, headers, params,context,encoding, FunOrigin.Authentication);
                    request = request.newBuilder().method(method.name(),requestBody).build();

                    authenticateStandard(callback, method, request);
                }catch (Exception e){
                    e.printStackTrace();
                }

                break;
            case None:
                break;
            default:
                break;
        }
        editor.putString("auth_type", authenticationType.toString());
        editor.putString("auth_endpoint", endpoint);
        editor.apply();
    }


    private void authenticateStandard(final RunnableCallback callback, Method method, final Request request){
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                try {
                    if (response.code() >= 200 && response.code() < 400){
                        JsonParser parser = new JsonParser();
                        JsonObject responseObj = parser.parse(response.body().string()).getAsJsonObject();
                        editor.putString("token", responseObj.get("token").getAsString());
                        editor.apply();
                        callback.success(response.code(),responseObj);
                    }else{
                        callback.failure(response.code(), new Exception(response.message()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    response.body().close();
                }


            }

            @Override
            public void onFailure(Call call, IOException e) {
                callback.failure(0, new Exception(e.getMessage()));
            }
        });
    }

    private void authenticateFb(final RunnableCallback callback, Method method, Request request){
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.failure(0, new Exception(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.code() >=200 && response.code() < 400){
                        JsonParser parser = new JsonParser();
                        JsonObject responseObj = parser.parse(response.body().string()).getAsJsonObject();
                        callback.success(response.code(), responseObj);
                    }else{
                        callback.failure(response.code(), new Exception(response.message()));
                    }
                }finally {
                    response.body().close();
                }
            }
        });
    }

    private void authenticateOauth2(final RunnableCallback callback, Method method, Request request){
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.failure(0, new Exception(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

             try {
                 if (response.code() >= 200 && response.code() < 400){
                     JsonParser parser = new JsonParser();
                     JsonObject responseObj = parser.parse(response.body().string()).getAsJsonObject();
                     editor.putString("token", responseObj.get("access_token").getAsString());
                     editor.putString("refresh_token", responseObj.get("refresh_token").getAsString());
                     editor.putBoolean("isAuth", true);
                     editor.apply();
                     callback.success(response.code(), responseObj);
                 }else{
                     callback.failure(response.code(), new Exception(response.message()));
                 }
             }catch(MalformedJsonException e){
                 callback.failure(500, e);
             }finally {
                 response.body().close();
             }
            }
        });
    }





    public void action(final String endpoint, final Method method, final Map<String, Object> parameters, final ParameterEncoding encoding, final Context context, final Map<String, String> headers, final String appPackage, final int configMode, final FunOrigin funOrigin, final RunnableCallback callback){
        settings = configSharedPref(appPackage, configMode, context);

        final Request request = retrofitBuilder(method, endpoint, headers,parameters,context,encoding, funOrigin);

        if (request != null){
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.failure(0, e);

                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {

                    try{
                        if (response.code() == 401){
                            // TODO: 20/10/16 bug in caso di reauth
                            Map<String,Object> params = new HashMap<>();
                            params.put("username", settings.getString("username",""));
                            params.put("password", settings.getString("password",""));
                            authenticate(settings.getString("auth_endpoint", ""), authMethod, new HashMap<String, String>(), params, ParameterEncoding.URL, context,appPackage, configMode, new RunnableCallback() {
                                @Override
                                public void success(int statusCode, Object responseObject) {
                                    action(endpoint, method, parameters, encoding, context, headers,appPackage,configMode, funOrigin,callback);
                                }

                                @Override
                                public void failure(int statusCode, Exception e) {
                                    if (statusCode == 400){
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
                        }else{
                            if (response.code() >= 200 && response.code() < 400) {
                                callback.success(response.code(), response.body().string());
                            }else{
                                callback.failure(response.code(), new Exception(response.message()));
                            }
                        }
                    }finally {
                        response.body().close();
                    }
                }
            });
        }

    }


    private String getQueryParams(Map<String, Object> params, String endpoint){
        if (params != null){
            String queryPath = "?";
            for (Map.Entry<String, Object> entry : params.entrySet()){
                queryPath = queryPath+entry.getKey()+"="+entry.getValue()+"&";
            }
            endpoint = endpoint+queryPath;
        }

        return endpoint;
    }

    public SharedPreferences configSharedPref(String appPackage, int mode, Context context){
        return context.getSharedPreferences(appPackage, mode);
    }
}

