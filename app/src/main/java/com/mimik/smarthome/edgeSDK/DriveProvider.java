package com.mimik.smarthome.edgeSDK;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public class DriveProvider {
    private static final String TAG = "DriveProvider";
    private static final Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    private static String mEdgeAccessToken;
    private static DriveProvider mDriveProviderInstance;
    private static Device mDevice;
    private static DriveService mRemoteService;
    public static String mRemoteUrl;

    public DriveProvider(Device device) {
        mDevice = device;
    }

    public static DriveProvider instance(Device device, String token) {
        if (token != null) {
            if (mEdgeAccessToken != null && !mEdgeAccessToken.equalsIgnoreCase(token)) {
                mRemoteUrl = null;
            }
            mEdgeAccessToken = token;
        }
        if (mDriveProviderInstance == null || !device.id.equalsIgnoreCase(mDevice.id)) {
            mDriveProviderInstance = new DriveProvider(device);
        }
        return mDriveProviderInstance;
    }

    private static DriveService remoteService() {
        Call<Device> callDev = SuperDriveProvider.presenceCheck(mDevice, mEdgeAccessToken);
        try {
            Device checkedDevice = callDev.execute().body();
            if (checkedDevice != null) {
                if (mRemoteService == null || (checkedDevice.url != null && !checkedDevice.url.equalsIgnoreCase(mRemoteUrl))) {
                    final HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                    final OkHttpClient client = new OkHttpClient.Builder()
//                            .addInterceptor(logging)
                            .addInterceptor(chain -> {
                                Request request = chain.request().newBuilder()
                                        .header("Authorization", "Bearer " + mEdgeAccessToken).build();
                                return chain.proceed(request);
                            })
                            .readTimeout(10, TimeUnit.MINUTES)
                            .connectTimeout(60, TimeUnit.SECONDS)
                            .build();

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(checkedDevice.url + "/drive/v1/")
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .client(client)
                            .build();

                    mRemoteService =  retrofit.create(DriveService.class);
                    mRemoteUrl = checkedDevice.url;
                }
                return mRemoteService;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mRemoteService;
    }

    public static Call<DriveFiles> getFiles() {
        return remoteService().filesList();
    }

    public void sendFile(File file, DriveFile metadata, Bitmap bitmap) {
        if (bitmap != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            boolean success = bitmap.compress(Bitmap.CompressFormat.JPEG, 75, out);

            if (success) {
                if (metadata.contentHints == null) {
                    metadata.contentHints = new ContentHints();
                }

                ContentHints hints = metadata.contentHints;
                hints.thumbnail = new ContentHints.Thumbnail();
                hints.thumbnail.image = Base64
                        .encodeToString(out.toByteArray(), Base64.NO_WRAP | Base64.NO_PADDING);
                Log.d(TAG, "thumbnail: " + hints.thumbnail.image);
                hints.thumbnail.mimeType = "image/jpeg";
            }
        }


        String json = new Gson().toJson(metadata);

        MultipartBody.Part partMetadata = MultipartBody.Part.createFormData("metadata", json);
        RequestBody newFile = MultipartBody.create(MediaType.parse(metadata.mimeType), file);
        MultipartBody.Part parFile = MultipartBody.Part.createFormData("file", file.getName(), newFile);

        DriveService service = remoteService();
        if (service != null) {
            Call<DriveFile> call = service.upload(partMetadata, parFile);
            call.enqueue(new Callback<DriveFile>() {
                @Override
                public void onResponse(Call<DriveFile> call, Response<DriveFile> response) {
                    //
                    if (response.body() != null) {
                        Log.d(TAG, "on Upload Response: " + response.body().toString());
                    }
                }

                @Override
                public void onFailure(Call<DriveFile> call, Throwable t) {
                    //
                    Log.e(TAG, "on Upload Failure: ", t);
                }
            });
        }
    }

    interface DriveService {
        @GET("files")
        Call<DriveFiles> filesList();

        @Multipart
        @POST("files")
        Call<DriveFile> upload(
                @Part MultipartBody.Part metadata,
                @Part MultipartBody.Part file);
    }
}
