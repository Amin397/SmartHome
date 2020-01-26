package com.mimik.smarthome.userinterface.common;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.mimik.smarthome.BuildConfig;
import com.mimik.smarthome.R;
import com.mimik.smarthome.edgeSDK.CustomPeerConnectionObserver;
import com.mimik.smarthome.edgeSDK.CustomSdpObserver;
import com.mimik.smarthome.edgeSDK.Device;
import com.mimik.smarthome.edgeSDK.DriveFile;
import com.mimik.smarthome.edgeSDK.DriveProvider;
import com.mimik.smarthome.edgeSDK.McmData;
import com.mimik.smarthome.edgeSDK.McmProvider;
import com.mimik.smarthome.edgeSDK.MsgProvider;
import com.mimik.smarthome.edgeSDK.SignalMsg;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
//import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class ChatActivity extends Activity implements View.OnClickListener, MsgProvider.MsgChatListener {
    private static final String TAG = "ChatActivity";

    private final String WS_SERVER_URL = "ws://127.0.0.1:8083/ws/"+ BuildConfig.CLIENT_ID+"/msg/v1";
    private MsgProvider mMsgProvider;

    PeerConnectionFactory peerConnectionFactory;
    MediaConstraints audioConstraints;
    MediaConstraints videoConstraints;
    MediaConstraints sdpConstraints;
    VideoSource videoSource;
    VideoTrack localVideoTrack;
    AudioSource audioSource;
    AudioTrack localAudioTrack;

    SurfaceViewRenderer localVideoView;
    SurfaceViewRenderer remoteVideoView;
//    VideoRenderer localRenderer;
//    VideoRenderer remoteRenderer;
    VideoCapturer videoCapturerAndroid;

    ImageButton hangup;
    PeerConnection localPeer;
    EglBase rootEglBase;

    boolean gotUserMedia;
    //prevent video from freezing after sending media due to issues with the resume call
    boolean resume = false;
    List<PeerConnection.IceServer> peerIceServers = new ArrayList<>();

    private Device mLocalDevice = null;
    private Device mShareDevice = null;
    private Device mRemoteDevice = null;
    private MsgProvider.CallerStatus mCallStatus;
    private String mEdgeAccessToken;
    private String mDriveToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Keep the screen on while the chat activity is running
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mMsgProvider = MsgProvider.instance(WS_SERVER_URL, ChatActivity.this);
        mMsgProvider.connect(null, ChatActivity.this, null);

        initViews();
        initVideos();
        getIceServers();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();

        if (intent.hasExtra("token")) {
            mEdgeAccessToken = (String) intent.getSerializableExtra("token");
        }
        if (intent.hasExtra("localDevice")) {
            mLocalDevice = (Device) intent.getSerializableExtra("localDevice");
        }
        if (intent.hasExtra("shareDevice")) {
            mShareDevice = (Device) intent.getSerializableExtra("shareDevice");
        }
        if (intent.hasExtra("remoteDevice")) {
            mRemoteDevice = (Device) intent.getSerializableExtra("remoteDevice");
        }
        if (intent.hasExtra("status")) {
            mCallStatus = (MsgProvider.CallerStatus) intent.getSerializableExtra("status");
        }
//        if (mEdgeAccessToken != null && mShareDevice != null && mDriveToken == null) {
//            new GetDriveToken().execute();
//        }

        //only start the app if this is the first time establishing a connection
        //prevents video freezing issue when selecting media to send
        if (!resume) {
            start();
        }
    }

    @Override
    public void onBackPressed() {
        try {
            videoCapturerAndroid.stopCapture();
            videoCapturerAndroid.dispose();
            finish();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    private void initViews() {
        hangup = findViewById(R.id.end_call);
        localVideoView = findViewById(R.id.local_gl_surface_view);
        remoteVideoView = findViewById(R.id.remote_gl_surface_view);
        hangup.setOnClickListener(this);
    }

    private void initVideos() {
        rootEglBase = EglBase.create();
        localVideoView.init(rootEglBase.getEglBaseContext(), null);
        remoteVideoView.init(rootEglBase.getEglBaseContext(), null);
        localVideoView.setZOrderMediaOverlay(true);
        remoteVideoView.setZOrderMediaOverlay(false);
    }

    private void getIceServers() {
//        PeerConnection.IceServer peerIceServer1 = PeerConnection.IceServer.builder("stun:52.33.181.13:3478?transport=udp")
//                .createIceServer();
//        peerIceServers.add(peerIceServer1);

        PeerConnection.IceServer peerIceServer2 = PeerConnection.IceServer.builder("turn:52.33.181.13:3478?transport=udp")
                .setUsername("user1")
                .setPassword("password1")
                .createIceServer();
        peerIceServers.add(peerIceServer2);
    }

    public void start() {
        //Initialize PeerConnectionFactory globals.
        PeerConnectionFactory.InitializationOptions initializationOptions =
                PeerConnectionFactory.InitializationOptions.builder(this)
//                        .setEnableVideoHwAcceleration(true)
                        .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
                        .createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);

        //Create a new PeerConnectionFactory instance - using Hardware encoder and decoder.

//        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
//        DefaultVideoEncoderFactory defaultVideoEncoderFactory = new DefaultVideoEncoderFactory(
//                rootEglBase.getEglBaseContext(),  /* enableIntelVp8Encoder */true,  /* enableH264HighProfile */true);
//        DefaultVideoDecoderFactory defaultVideoDecoderFactory = new DefaultVideoDecoderFactory(rootEglBase.getEglBaseContext());
////        peerConnectionFactory = new PeerConnectionFactory(options, defaultVideoEncoderFactory, defaultVideoDecoderFactory);
//        peerConnectionFactory = PeerConnectionFactory.builder().setOptions(options).createPeerConnectionFactory();

        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        peerConnectionFactory = PeerConnectionFactory
                .builder()
                .setVideoDecoderFactory(new DefaultVideoDecoderFactory(rootEglBase.getEglBaseContext()))
                .setVideoEncoderFactory(new DefaultVideoEncoderFactory(rootEglBase.getEglBaseContext(), true, true))
                .setOptions(options)
                .createPeerConnectionFactory();


        //Now create a VideoCapturer instance.
        videoCapturerAndroid = createCameraCapturer(new Camera1Enumerator(false));


        //Create MediaConstraints - Will be useful for specifying video and audio constraints.
        audioConstraints = new MediaConstraints();
        videoConstraints = new MediaConstraints();
        sdpConstraints = new MediaConstraints();

        //Create a VideoSource instance
        if (videoCapturerAndroid != null) {
//            videoSource = peerConnectionFactory.createVideoSource(videoCapturerAndroid);
            videoSource = peerConnectionFactory.createVideoSource(true);
        }

        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create(Thread.currentThread().getName(), rootEglBase.getEglBaseContext());
        videoCapturerAndroid.initialize(surfaceTextureHelper, localVideoView.getContext(), videoSource.getCapturerObserver());

        localVideoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);

        //create an AudioSource instance
        audioSource = peerConnectionFactory.createAudioSource(audioConstraints);
        localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);


        if (videoCapturerAndroid != null) {
            videoCapturerAndroid.startCapture(1024, 720, 30);
        }
        localVideoView.setVisibility(View.VISIBLE);
//        //create a videoRenderer based on SurfaceViewRenderer instance
//        localRenderer = new VideoRenderer(localVideoView);
//        // And finally, with our VideoRenderer ready, we
//        // can add our renderer to the VideoTrack.
//        localVideoTrack.addRenderer(localRenderer);

        localVideoTrack.addSink(localVideoView);


        localVideoView.setMirror(true);
        localVideoView.setEnableHardwareScaler(true);
        remoteVideoView.setMirror(true);

        gotUserMedia = true;
        if (mCallStatus == MsgProvider.CallerStatus.INITIATOR) {
//            onTryToStart();
//            callInitiated();
        } else if (mCallStatus == MsgProvider.CallerStatus.PARTICIPANT) {
            onTryToStart();
            onCallJoined();
        }
    }


    /**
     * This method will be called directly by the app when it is the initiator and has got the local media
     * or when the remote peer sends a message through socket that it is ready to transmit AV data
     */
    @Override
    public void onTryToStart() {
        runOnUiThread(() -> {
            if (localVideoTrack != null) {
                createPeerConnection();
                if (mCallStatus == MsgProvider.CallerStatus.INITIATOR) {
                    doCall();
                }
            }
        });
    }


    /**
     * Creating the local peerconnection instance
     */
    private void createPeerConnection() {
        if (localPeer != null) {
            return;
        }
        Log.d(TAG, "createPeerConnection: ");
        PeerConnection.RTCConfiguration rtcConfig =
                new PeerConnection.RTCConfiguration(peerIceServers);
        // TCP candidates are only useful when connecting to a server that supports
        // ICE-TCP.
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        // Use ECDSA encryption.
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;
        localPeer = peerConnectionFactory.createPeerConnection(rtcConfig, new CustomPeerConnectionObserver("localPeerCreation") {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
//                showToast("Received iceCandidate");
                super.onIceCandidate(iceCandidate);
                onIceCandidateReceived(iceCandidate);
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
//                showToast("Received Remote stream");
                super.onAddStream(mediaStream);
                gotRemoteStream(mediaStream);
            }
        });

        addStreamToLocalPeer();
    }

    /**
     * Adding the stream to the localpeer
     */
    private void addStreamToLocalPeer() {
        //creating local mediastream
        MediaStream stream = peerConnectionFactory.createLocalMediaStream("102");
        stream.addTrack(localAudioTrack);
        stream.addTrack(localVideoTrack);
        localPeer.addStream(stream);
    }

    /**
     * This method is called when the app is initiator - We generate the offer and send it over through socket
     * to remote peer
     */
    private void doCall() {
        CustomSdpObserver spdObserver = new CustomSdpObserver("localCreateOffer") {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                localPeer.setLocalDescription(new CustomSdpObserver("localSetLocalDesc"), sessionDescription);
                Log.d(TAG, "onCreateSuccess Signalling Client emit ");
                emitMessage(sessionDescription);
            }
        };
        localPeer.createOffer(spdObserver, sdpConstraints);
    }

    /**
     * Received remote peer's media stream. we will get the first video track and render it
     */
    private void gotRemoteStream(MediaStream stream) {
        Log.d(TAG, "gotRemoteStream Incoming remote stream " + stream.toString());
        //we have remote video stream. add to the renderer.
        final VideoTrack videoTrack = stream.videoTracks.get(0);
        runOnUiThread(() -> {
            try {
//                remoteRenderer = new VideoRenderer(remoteVideoView);
                remoteVideoView.setVisibility(View.VISIBLE);
//                videoTrack.addRenderer(remoteRenderer);
                videoTrack.addSink(remoteVideoView);
                localVideoView.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    /**
     * Received local ice candidate. Send it to remote peer through signalling for negotiation
     */
    public void onIceCandidateReceived(IceCandidate iceCandidate) {
        Log.d(TAG, "onIceCandidateReceived iceCandidate " + iceCandidate.toString());
        //we have received ice candidate. We can set it to the other peer.
        emitIceCandidate(iceCandidate);
    }

    /**
     * Signalling Callback - called when call Initiated - i.e. you are the initiator
     */
    @Override
    public void callInitiated() {
        Log.d(TAG, "You initiated a call. gotUserMedia:  " + gotUserMedia);
//        showToast("You initiated a call. gotUserMedia: " + gotUserMedia);
        if (gotUserMedia) {
            emitMessage("got user media");
        }
    }

    /**
     * SignallingCallback - called when  - you are a participant
     */
    public void onCallJoined() {
        Log.d(TAG, "You joined the room " + gotUserMedia);
//        showToast("You joined the room " + gotUserMedia);
        if (gotUserMedia) {
            emitMessage("got user media");
        }
    }

    @Override
    public void callAnswered() {
        Log.d(TAG, "Remote peer joined");
        showToast("Remote Peer Joined");
        if (mCallStatus == MsgProvider.CallerStatus.INITIATOR) {
            onTryToStart();
            callInitiated();
        }
    }

    @Override
    public void callIgnored(String msg) {
        Log.d(TAG, "Remote rejected the call");
        showToast("Remote " + msg + " rejected the call");
        runOnUiThread(this::hangup);
    }

    @Override
    public void onRemoteHangUp(String msg) {
        Log.d(TAG, "Remote peer hungup");
        showToast("Remote Peer hungup");
        runOnUiThread(this::hangup);
    }

    /**
     * SignallingCallback - Called when remote peer sends offer
     */
    @Override
    public void onOfferReceived(final JSONObject data) {
        Log.d(TAG,"onOfferReceived Offer " + data.toString());
//        showToast("Received Offer");
        runOnUiThread(() -> {
            if (mCallStatus == MsgProvider.CallerStatus.INITIATOR) {
                onTryToStart();
            }

            try {
                localPeer.setRemoteDescription(new CustomSdpObserver("localSetRemote"), new SessionDescription(SessionDescription.Type.OFFER, data.getString("sdp")));
                doAnswer();
                updateVideoViews(true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private void doAnswer() {
        Log.d(TAG,"doAnswer" + " local Answer ");
        localPeer.createAnswer(new CustomSdpObserver("localCreateAns") {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                localPeer.setLocalDescription(new CustomSdpObserver("localSetLocal"), sessionDescription);
                emitMessage(sessionDescription);
            }
        }, new MediaConstraints());
    }

    /**
     * SignallingCallback - Called when remote peer sends answer to your offer
     */

    @Override
    public void onAnswerReceived(JSONObject data) {
        Log.d(TAG,"onAnswerReceived Received Answer " + data.toString());
//        showToast("Received Answer");
        try {
            localPeer.setRemoteDescription(new CustomSdpObserver("localSetRemote"), new SessionDescription(SessionDescription.Type.fromCanonicalForm(data.getString("type").toLowerCase()), data.getString("sdp")));
            updateVideoViews(true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remote IceCandidate received
     */
    @Override
    public void onIceCandidateReceived(JSONObject data) {
        Log.d(TAG,"onIceCandidateReceived Received Candidate " +data.toString());
        try {
            localPeer.addIceCandidate(new IceCandidate(data.getString("id"), data.getInt("label"), data.getString("candidate")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void updateVideoViews(final boolean remoteVisible) {
        Log.d(TAG,"updateVideoViews" + " update ");
        runOnUiThread(() -> {
            ViewGroup.LayoutParams params = localVideoView.getLayoutParams();
            if (remoteVisible) {
                params.height = dpToPx(100);
                params.width = dpToPx(100);
            } else {
                params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            }
            localVideoView.setLayoutParams(params);
        });

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /**
             * Closing up - normal hangup
             */
            case R.id.end_call: {
                if (localPeer != null) {
                    SignalMsg msgBody = new SignalMsg();
                    msgBody.type = "bye";
                    msgBody.message = "bye";
                    mMsgProvider.postMessages(mRemoteDevice.url + "/" + BuildConfig.CLIENT_ID + "/msg/v1/", msgBody);
                }
                hangup();
                break;
            }

        }
    }

    private void hangup() {
        try {
            close();
            if (localPeer != null) {
                localPeer.close();
                localPeer.dispose();
                localPeer = null;
                updateVideoViews(false);
            }
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==317 && resultCode==RESULT_OK) {
            Uri selectedfile = data.getData();
            if (selectedfile != null) {
                new UploadAFile().execute(selectedfile);
            }
        }
    }

    private static String getMimeType(String url) {
        String type = "application/octet-stream";
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);

        if (extension.isEmpty()) {
            int filenamePos = url.lastIndexOf('/');
            String filename = 0 <= filenamePos ? url.substring(filenamePos + 1) : url;

            if (!filename.isEmpty()) {
                int dotPos = filename.lastIndexOf('.');
                if (0 <= dotPos) {
                    extension = filename.substring(dotPos + 1);
                }
            }
        }

        if (extension != null && !extension.isEmpty()) {
            extension = extension.toLowerCase();
            String typeFromExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (typeFromExtension != null) {
                type = typeFromExtension;
            }
        }

        return type;
    }

    @Override
    protected void onDestroy() {
        close();
        super.onDestroy();
    }

    /**
     * Util Methods
     */
    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public void showToast(final String msg) {
        runOnUiThread(() -> Toast.makeText(ChatActivity.this, msg, Toast.LENGTH_SHORT).show());
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Logging.d(TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Logging.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

//    @Override
//    public void onNewMsg() {
//        Log.d(TAG, "onNewMsg: ");
//        mMsgProvider.handleMessages("");
//    }

    private void emitMessage(String msg) {
        if (mMsgProvider.callStatus == MsgProvider.CallerStatus.UNKNOWN) {
            return;
        }
        SignalMsg msgBody = new SignalMsg();
        msgBody.type = "message";
        msgBody.message = msg;
        mMsgProvider.postMessages(mRemoteDevice.url+"/"+BuildConfig.CLIENT_ID+"/msg/v1/", msgBody);
    }

    public void emitMessage(SessionDescription message) {
        try {
            Log.d(TAG, "emitMessage() called with: message = [" + message + "]");
            JSONObject object = new JSONObject();
            object.put("type", message.type.canonicalForm());
            object.put("sdp", message.description);
            Log.d("emitMessage", object.toString());
            emitMessage(object.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void emitIceCandidate(IceCandidate iceCandidate) {
        try {
            JSONObject object = new JSONObject();
            object.put("type", "candidate");
            object.put("label", iceCandidate.sdpMLineIndex);
            object.put("id", iceCandidate.sdpMid);
            object.put("candidate", iceCandidate.sdp);
            emitMessage(object.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void close() {
        mDriveToken = null;
        if (localPeer != null) {
            SignalMsg msgBody = new SignalMsg();
            msgBody.type = "bye";
            msgBody.message = "bye";
            mMsgProvider.postMessages(mRemoteDevice.url + "/" + BuildConfig.CLIENT_ID + "/msg/v1/", msgBody);
            localPeer.close();
            localPeer.dispose();
            //Hangup crash occurred when trying to close/dispose the local peer for a second time in the hangup() function
            localPeer = null;
            try {
                if (videoCapturerAndroid != null) {
                    videoCapturerAndroid.stopCapture();
                    videoCapturerAndroid.dispose();
                }
                if (peerConnectionFactory != null) {
                    peerConnectionFactory.dispose();
                }
//                if (localRenderer != null) {
//                    localRenderer.dispose();
//                }
//                if (remoteRenderer != null) {
//                    remoteRenderer.dispose();
//                }
                if (localVideoView != null) {
                    localVideoView.release();
                    localVideoView = null;
                }
                if (remoteVideoView != null) {
                    remoteVideoView.release();
                    remoteVideoView = null;
                }
                if (rootEglBase != null) {
                    rootEglBase.release();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        mMsgProvider.cleanUp();
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private class GetDriveToken extends AsyncTask<Void, Void, McmData> {
        @Override
        protected McmData doInBackground(Void... voids) {
            McmData ret = null;
            try {
                Response<McmData> response =
                        McmProvider.getContainers(mShareDevice, mEdgeAccessToken).execute();
                if (response.isSuccessful() && response.body() != null) {
                    ret = response.body();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ret;
        }

        @Override
        protected void onPostExecute(final McmData mcmData) {
            if (mcmData != null) {
                for (McmData.Data data: mcmData.data) {
                    Log.d(TAG, "onPostExecute: " + data.name);
                    if (data.name.equalsIgnoreCase("drive-v1") && data.env != null && data.env.AUTHORIZATION_KEY != null) {
                        mDriveToken = data.env.AUTHORIZATION_KEY;
                    }
                }
            }
        }
    }

    private class UploadAFile extends AsyncTask<Uri, Void, Void> {
        @Override
        protected Void doInBackground(Uri... uris) {
            Uri selectedfile = uris[0];
            final int THUMBSIZE = 64;
            Bitmap thumbImage = null;
            String path = getPath(ChatActivity.this, selectedfile);
            File file = new File(path);

            thumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path),
                    THUMBSIZE, THUMBSIZE);

            DriveProvider driveProvider = DriveProvider.instance(mShareDevice, mDriveToken);
            DriveFile metadata = new DriveFile();
            metadata.name = file.getName();
            metadata.mimeType = getMimeType(path);
            if (metadata.mimeType == null) {
                showToast("Could not prepare the data for upload. Please try again.");
                return null;
            }

            driveProvider.sendFile(file, metadata, thumbImage);
            return null;
        }
    }
}
