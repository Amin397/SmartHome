package com.mimik.smarthome.edgeSDK;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class MdsProvider {
    private static final String TAG = "MdsProvider";

    private static final String API_URL = "http://127.0.0.1:8083/mds/v1/";
    private static final Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .create();
    private static MdsProvider mMdsProviderInstance = null;
    private MdsService mLocalService = null;
    private String mLocalDeviceId = null;

    public MdsProvider() {
        //
    }

    public static MdsProvider instance() {
        if (mMdsProviderInstance == null) {
            mMdsProviderInstance = new MdsProvider();
        }
        return mMdsProviderInstance;
    }

    public MdsService localService() {
        if (mLocalService == null) {
            final HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            final OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();

            mLocalService = retrofit.create(MdsService.class);
        }
        return mLocalService;
    }

    public void requestDeviceId() {
        Call<NodesPing> call = localService().getNodes(true);
        call.enqueue(new Callback<NodesPing>() {
            @Override
            public void onResponse(Call<NodesPing> call, Response<NodesPing> response) {
                NodesPing localNode = response.body();

                if (localNode != null) {
                    mLocalDeviceId = localNode.getData().getId();
                }
            }

            @Override
            public void onFailure(Call<NodesPing> call, Throwable t) {

            }
        });
    }

    public String getDeviceId() {
        if (mLocalDeviceId == null) {
            requestDeviceId();
        }
        return mLocalDeviceId;
    }

    interface MdsService {
        @GET("nodes")
        Call<NodesPing> getNodes(@Query("ping") Boolean doPing);
    }
}
