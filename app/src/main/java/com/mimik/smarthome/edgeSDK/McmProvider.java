package com.mimik.smarthome.edgeSDK;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class McmProvider {
    private static final String TAG = "McmProvider";
    private static final Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .create();


    private static McmService remoteService(Device device, String edgeToken) {
        Call<Device> callDev = SuperDriveProvider.presenceCheck(device, edgeToken);
        try {
            Device checkedDevice = callDev.execute().body();
            if (checkedDevice != null && checkedDevice.url != null) {
                final HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                final OkHttpClient client = new OkHttpClient.Builder()
                        .addInterceptor(logging)
                        .addInterceptor(chain -> {
                            Request request = chain.request().newBuilder()
                                    .header("Authorization", "Bearer " + edgeToken).build();
                            return chain.proceed(request);
                        })
                        .readTimeout(60, TimeUnit.SECONDS)
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .build();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(checkedDevice.url + "/mcm/v1/")
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .client(client)
                        .build();

                return retrofit.create(McmService.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Call<McmData> getContainers(Device device, String edgeToken) {
        return remoteService(device, edgeToken).getContainersData();
    }

    interface McmService {
        @GET("containers")
        Call<McmData> getContainersData();
    }
}
