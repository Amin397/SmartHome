package com.mimik.smarthome.edgeSDK;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mimik.smarthome.BuildConfig;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class SuperDriveProvider {

    public static final String API_URL = "http://127.0.0.1:8083/"+ BuildConfig.CLIENT_ID+"/superdrive/v1/";
    private static final Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .create();
    private static SuperDriveService mLocalService = null;

    public enum DeviceFilter {
        NETWORK,
        PROXIMITY,
        ACCOUNT
    }

    public static SuperDriveService localService(final String edgeAccessToken) {
        if (mLocalService == null) {
            final HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            final OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(final Chain chain) throws IOException {
                            Request newRequest = chain.request().newBuilder()
                                    .addHeader("Authorization", "Bearer " + edgeAccessToken)
                                    .build();
                            return chain.proceed(newRequest);
                        }
                    })
                    .readTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();

            mLocalService = retrofit.create(SuperDriveService.class);
        }
        return mLocalService;
    }

    // Get list of devices
    public static Call<DeviceList> getDevices(final DeviceFilter filter, final String edgeAccessToken) {
        String type;
        switch (filter) {
            case PROXIMITY:
                type = "proximity";
                break;
            case ACCOUNT:
                type = "account";
                break;
            case NETWORK:
            default:
                type = "nearby";
        }
        return localService(edgeAccessToken).getDevices(type, edgeAccessToken);
    }

    public static Call<DeviceList> getLocalDevices(final String edgeAccessToken) {
        return localService(edgeAccessToken).getLocalDevices(edgeAccessToken);
    }

    public static Call<Device> presenceCheck(final Device device, final String edgeAccessToken) {
        return localService(edgeAccessToken).nodes(device.id, edgeAccessToken);
    }

    interface SuperDriveService {
        // Get a list of nearby devices
        @GET("drives")
        Call<DeviceList> getDevices(@Query("type") String type, @Query("userAccessToken") String userToken);

        @GET("drives")
        Call<DeviceList> getLocalDevices(@Query("userAccessToken") String userToken);

        @GET("nodes/{nodeId}")
        Call<Device> nodes(@Path("nodeId") String nodeId, @Query("userAccessToken") String userToken);
    }
}