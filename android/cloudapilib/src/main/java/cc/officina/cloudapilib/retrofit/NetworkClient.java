package cc.officina.cloudapilib.retrofit;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cc.officina.cloudapilib.BuildConfig;
import cc.officina.cloudapilib.CloudApi;
import cc.officina.cloudapilib.model.Location;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

/**
 * Created on 09/11/16.
 *
 * @author Umberto Marini
 */
public class NetworkClient implements NetworkClientInterface {

    private static final long READ_TIMEOUT_IN_SECONDS = 30;
    private static final long CONNECTION_TIMEOUT_IN_SECONDS = 30;
    private Retrofit mRetrofit;

    private static OkHttpClient.Builder makeBaseOkHttpClient() {
        try {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();

            // logging feature (add logging as last interceptor)
            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                builder.addInterceptor(logging);
            }

            // add connection timeout
                builder.readTimeout(READ_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
                builder.connectTimeout(CONNECTION_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);

            // add interceptor to add common request headers
                builder.addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();

                        Request request = original.newBuilder()
                                .header(getAuthenticationHeaderName(), getAuthenticationHeaderValue())
                                .method(original.method(), original.body())
                                .build();

                        return chain.proceed(request);
                    }
                });

            // set ConnectionSpec to OkHttpClient
            ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
                    .cipherSuites(
                            // TLS 1.2
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,
                            CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
                            CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA256,
                            CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA,
                            // TLS 1.1
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
                            CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA,
                            // TLS 1.0
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
                            CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA
                    )
                    .build();
            builder.connectionSpecs(Collections.singletonList(spec));

            return builder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static OkHttpClient.Builder makeOkHttpClient() {
        return makeBaseOkHttpClient();
    }

    public NetworkClient(final String apiBaseUrl) {
        OkHttpClient.Builder builder = makeOkHttpClient();
        OkHttpClient client = builder.build();
        mRetrofit = new Retrofit.Builder()
                .baseUrl(apiBaseUrl)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(GsonHelper.createGsonInstance()))
                .client(client)
                .build();
    }

    @Override
    public Observable<List<Location>> locations(double latitude, double longitude, String query, int limit, int radius) {
        return mRetrofit.create(NetworkApiInterface.class).locations(latitude, longitude, query, limit, radius);
    }

    private static String getAuthenticationHeaderName() {
        final CloudApi.AuthenticationType authenticationType = CloudApi.getInstance().getAuthenticationType();
        if (CloudApi.AuthenticationType.Facebook.equals(authenticationType)) {
            return NetworkApiInterface.AUTHORIZATION_HEADER_NAME_FACEBOOK;
        } else if (CloudApi.AuthenticationType.Standard.equals(authenticationType)) {
            return NetworkApiInterface.AUTHORIZATION_HEADER_NAME_STANDARD;
        } else if (CloudApi.AuthenticationType.Oauth2.equals(authenticationType)) {
            return NetworkApiInterface.AUTHORIZATION_HEADER_NAME_OAUTH2;
        } else {
            return NetworkApiInterface.AUTHORIZATION_HEADER_NAME_DEFAULT;
        }
    }

    private static String getAuthenticationHeaderValue() {
        return CloudApi.getInstance().getAuthorizationToken();
    }
}