package com.mimik.smarthome.edgeSDK;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mimik.smarthome.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class MsgProvider {
    private static final String TAG = "MsgProvider";

    private static final String API_URL = "http://127.0.0.1:8083/"+ BuildConfig.CLIENT_ID+"/msg/v1/";
    private static final Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    private static String mLastMessageId;
    private String mEdgeAccessToken;

    public enum CallerStatus implements Serializable {
        UNKNOWN("Unknown", 0),
        INITIATOR("Initiator", 1),
        PARTICIPANT("Participant",2);

        private String stringValue;
        private int intValue;
        private CallerStatus(String toString, int value) {
            stringValue = toString;
            intValue = value;
        }

        @Override
        public String toString() {
            return stringValue;
        }
    }

    public CallerStatus callStatus = CallerStatus.UNKNOWN;

    private static MsgProvider mMsgProviderInstance;
    private static Context mContext;
    private WebSocket mWebSocket;
    private OkHttpClient mClient;
    private String mWSServerUrl;
    private Handler mMessageHandler;
    private MsgListener mListener;
    private MsgChatListener mChatListener;
    private MsgService mLocalService = null;
    private MsgService mRemoteService = null;
    private String mRemoteUrl = "";

    public interface MsgListener {
//        void onNewMsg();

        void onIncomingCall(Device device, Device shareDevice);
    }

    public interface MsgChatListener {
//        void onNewMsg();

        void onTryToStart();

        void callInitiated();

        void callAnswered();

        void callIgnored(String msg);

        void onRemoteHangUp(String msg);

        void onOfferReceived(JSONObject data);

        void onAnswerReceived(JSONObject data);

        void onIceCandidateReceived(JSONObject data);
    }

    private class SocketListener extends WebSocketListener {
        @Override
        public void onMessage(WebSocket webSocket, String text) {
            Message m = mMessageHandler.obtainMessage(0, text);
            mMessageHandler.sendMessage(m);
            Log.d(TAG, "onMessage: " + text);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            Log.e(TAG, "ws error:" + t.getLocalizedMessage());
//            disconnect();
        }
    }

    public MsgProvider(String wsUrl) {
        mClient = new OkHttpClient.Builder()
                .readTimeout(3,  TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        mWSServerUrl = wsUrl;
    }

    public static MsgProvider instance(String wsUrl, Context context) {
        if (mMsgProviderInstance == null) {
            mMsgProviderInstance = new MsgProvider(wsUrl);
            mContext = context;
            mLastMessageId = "";
        }
        return mMsgProviderInstance;
    }

    public void connect(MsgListener listener, MsgChatListener chatListener, String token) {
        Request request = new Request.Builder()
                .url(mWSServerUrl)
                .build();
        mWebSocket = mClient.newWebSocket(request, new SocketListener());
        if (listener != null) {
            mListener = listener;
        }
        if (chatListener != null) {
            mChatListener = chatListener;
        }
        if (token != null) {
            mEdgeAccessToken = token;
        }
        mMessageHandler = new Handler(msg -> {
//            if (mListener != null) {
//                mListener.onNewMsg();
//            }
//            if (mChatListener != null) {
//                mChatListener.onNewMsg();
//            }
            handleMessages(mLastMessageId);
            return true;
        });
    }

    public void disconnect() {
        mWebSocket.cancel();
        mListener = null;
        mChatListener = null;
        mMessageHandler.removeCallbacksAndMessages(null);
    }

//    public void sendMessage(String message) {
//        mWebSocket.send(message);
//    }

    public MsgService localService() {
        if (mLocalService == null) {
            final HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            final OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        Request newRequest = chain.request().newBuilder()
                                .addHeader("Content-Type", "application/json")
                                .build();
                        return chain.proceed(newRequest);
                    })
                    .readTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();

            mLocalService = retrofit.create(MsgService.class);
        }
        return mLocalService;
    }

    public MsgService remoteService(String url) {
        if (mRemoteService == null || !url.equalsIgnoreCase(mRemoteUrl)) {
            final HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            final OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        Request newRequest = chain.request().newBuilder()
                                .addHeader("Content-Type", "application/json")
                                .build();
                        return chain.proceed(newRequest);
                    })
                    .readTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();

            mRemoteService =  retrofit.create(MsgService.class);
            mRemoteUrl = url;
        }
        return mRemoteService;
    }

    public void postMessages(String url, SignalMsg msgBody) {
        Call<ResponseBody> call = remoteService(url).postMessage(msgBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                Log.d(TAG, "onResponse: " + response.body());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
            }
        });
    }

    private boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            // edited, to include @Arthur's comment
            // e.g. in case JSONArray is valid as well...
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    private void handleMessages(String afterId) {
        Call<SignalMsgList> call = localService().getMessages(afterId);
        Log.d(TAG, "handleMessages: " + afterId);
        call.enqueue(new Callback<SignalMsgList>() {
            @Override
            public void onResponse(Call<SignalMsgList> call, retrofit2.Response<SignalMsgList> response) {
                SignalMsgList messages = response.body();
                if (messages != null) {
                    for (SignalMsg message: messages.data) {
                        mLastMessageId = message.id;
                        Log.d(TAG, "onResponse: " + message.id);
                        if (message.type.equalsIgnoreCase("ring")) {
                            final Gson mGson = new GsonBuilder()
                                    .create();
                            Log.d(TAG, "######## RING: " + message.message);
                            RingDevices devices = mGson.fromJson(message.message, RingDevices.class);
                            Device device = devices.caller;

                            Call<Device> callDev = SuperDriveProvider.presenceCheck(device, mEdgeAccessToken);
                            callDev.enqueue(new Callback<Device>() {
                                @Override
                                public void onResponse(Call<Device> call, retrofit2.Response<Device> resp) {
                                    Device checkedDevice = null;
                                    if (resp.isSuccessful() && resp.body() != null) {
                                        checkedDevice = resp.body();
                                        if (checkedDevice != null) {
                                            if (mListener != null) {
                                                mListener.onIncomingCall(checkedDevice, devices.share);
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<Device> call, Throwable t) {

                                }
                            });
//                                retrofit2.Response<Device> resp =
//                                        SuperDriveProvider.presenceCheck(device, mEdgeAccessToken).execute();
//                                if (resp.isSuccessful() && resp.body() != null) {
//                                    checkedDevice = resp.body();
//                                    if (checkedDevice != null) {
//                                        if (mListener != null) {
//                                            mListener.onIncomingCall(checkedDevice);
//                                        }
//                                    }
//                                }


                        } else if (message.type.equalsIgnoreCase("hangup")) {
                            final Gson mGson = new GsonBuilder()
                                    .create();
                            Device device = mGson.fromJson(message.message, Device.class);
                            if (mChatListener != null) {
                                mChatListener.callIgnored(device.name);
                            }
                        } else if (message.type.equalsIgnoreCase("answer")) {
                            if (mChatListener != null) {
                                mChatListener.callAnswered();
                            }
                        } else if (message.type.equalsIgnoreCase("bye")) {
                            if (mChatListener != null) {
                                mChatListener.onRemoteHangUp(message.message);
                            }
                        } else if (message.type.equalsIgnoreCase("message")) {
                            if (callStatus == CallerStatus.UNKNOWN) {
                                return;
                            }
                            if (mChatListener != null) {
                                if (isJSONValid(message.message)) {
                                    try {
                                        JSONObject data =new JSONObject(message.message);
                                        Log.d(TAG, "Json Received :: " + data.toString());
                                        String type = data.getString("type");
                                        if (type.equalsIgnoreCase("offer")) {
                                            mChatListener.onOfferReceived(data);
                                        } else if (type.equalsIgnoreCase("answer")) {
                                            mChatListener.onAnswerReceived(data);
                                            partialCleanUp(); // Trying to ensure there are no ghost messages
                                        } else if (type.equalsIgnoreCase("candidate")) {
                                            mChatListener.onIceCandidateReceived(data);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Log.d(TAG, "String received :: " + message.message);
                                    String data = message.message;
                                    if (data.equalsIgnoreCase("got user media")) {
                                        if (callStatus != MsgProvider.CallerStatus.INITIATOR) {
                                            mChatListener.onTryToStart();
                                        }
                                    }
                                    if (data.equalsIgnoreCase("bye")) {
                                        mChatListener.onRemoteHangUp(data);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<SignalMsgList> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
            }
        });
    }

    public void partialCleanUp() {
        Call<SignalMsgList> call = localService().getMessages("");
        Log.d(TAG, "partialCleanUp: ");
        call.enqueue(new Callback<SignalMsgList>() {
            @Override
            public void onResponse(Call<SignalMsgList> call, retrofit2.Response<SignalMsgList> response) {
//                Log.d(TAG, "onResponse: " + response.body().toString());
                SignalMsgList messages = response.body();
                if (messages != null) {
                    Integer index = 0;
                    Handler handler = new Handler();
                    while (index < messages.data.size()) {
                        SignalMsg message = messages.data.get(index);
                        index++;
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                removeMessages(message.id);
                            }
                        }, 85 * index);
                    }
                }
            }

            @Override
            public void onFailure(Call<SignalMsgList> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
            }
        });
    }


    public void cleanUp() {
        callStatus = CallerStatus.UNKNOWN;
        mLastMessageId = "";
        Call<SignalMsgList> call = localService().getMessages("");
        Log.d(TAG, "cleanUp: ");
        call.enqueue(new Callback<SignalMsgList>() {
            @Override
            public void onResponse(Call<SignalMsgList> call, retrofit2.Response<SignalMsgList> response) {
//                Log.d(TAG, "onResponse: " + response.body().toString());
                SignalMsgList messages = response.body();
                if (messages != null) {
                    Integer index = 0;
                    Handler handler = new Handler();
                    while (index < messages.data.size()) {
                        SignalMsg message = messages.data.get(index);
                        index++;
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                removeMessages(message.id);
                            }
                        }, 85 * index);
                    }
                }
            }

            @Override
            public void onFailure(Call<SignalMsgList> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
            }
        });
    }

    private void removeMessages(String msgId) {
        Log.d(TAG, "removeMessages : " + msgId);
        Call<ResponseBody> call = localService().deleteMessages(msgId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                Log.d(TAG, "removeMessages onResponse: " + response.body());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "removeMessages onFailure: ", t);
            }
        });
    }

    interface MsgService {
        @POST("messages")
        Call<ResponseBody> postMessage(@Body SignalMsg msgBody);

        @GET("messages")
        Call<SignalMsgList> getMessages(@Query("after") String afterId);

        @DELETE("messages/{msgId}")
        Call<ResponseBody> deleteMessages(@Path("msgId") String msgId);
    }
}
