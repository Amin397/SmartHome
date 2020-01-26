package com.mimik.smarthome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mimik.edgeappops.EdgeAppOps;
import com.mimik.edgeappops.edgeservice.EdgeConfig;
import com.mimik.smarthome.userinterface.common.ChatActivity;
import com.mimik.smarthome.edgeSDK.Device;
import com.mimik.smarthome.edgeSDK.DeviceAdapter;
import com.mimik.smarthome.edgeSDK.DeviceList;
import com.mimik.smarthome.edgeSDK.MdsProvider;
import com.mimik.smarthome.edgeSDK.MsgProvider;
import com.mimik.smarthome.edgeSDK.SignalMsg;
import com.mimik.smarthome.edgeSDK.SuperDriveProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements DeviceAdapter.IDeviceSelectCallback, MsgProvider.MsgListener {
    private static final String TAG = "MainActivity";
    private static final int CHATTING = 167;
    private static final int RC_CAMERA_AND_RECORD_AUDIO = 153;

    private final String WS_SERVER_URL = "ws://127.0.0.1:8083/ws/"+BuildConfig.CLIENT_ID+"/msg/v1";
    private MsgProvider mMsgProvider;

    // User access tokens
    private String mEdgeAccessToken;
    private String mUserAccessToken;

    private List<AsyncTask> mTaskList;
    private List<Device> OtherDeviceList = new ArrayList<>();
    private Device mLocalDevice = null;
    private Device mRemoteDevice = null;
    private Device mShareDevice = null;
    Vibrator vibrator;

    EdgeAppOps mAppOps;

    ProgressBar mProgressBar;
    ProgressBar mCallBar;
    Button mScanButton;
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    ImageButton mAnswer;
    ImageButton mIgnore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar = findViewById(R.id.progressBar);
        mCallBar = findViewById(R.id.callProgress);
        mScanButton = findViewById(R.id.buttonScan);
        mRecyclerView = findViewById(R.id.recyclerView);
        mAnswer = findViewById(R.id.buttonAnswer);
        mIgnore = findViewById(R.id.buttonIgnore);

        //not visible until we start making a call
        mCallBar.setVisibility(View.INVISIBLE);

        mScanButton.setEnabled(false);
        mScanButton.setOnClickListener(view -> {
            mProgressBar.setVisibility(View.VISIBLE);
            new RefreshDeviceListTask().execute();
        });

        List<Device> mDeviceList = new ArrayList<>();

        mAdapter = new DeviceAdapter(mDeviceList);

        if (mAdapter instanceof DeviceAdapter) {
            ((DeviceAdapter) mAdapter).registerCallback(MainActivity.this);
        }

        mLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);

        mAnswer.setVisibility(View.INVISIBLE);


        mAnswer.setOnClickListener(view -> {

            mAnswer.setVisibility(View.INVISIBLE);
            mIgnore.setVisibility(View.INVISIBLE);
            vibrator.cancel();
            final Gson mGson = new GsonBuilder()
                    .create();

            SignalMsg msgBody = new SignalMsg();
            msgBody.type = "answer";
            msgBody.message = mGson.toJson(mLocalDevice);
            mMsgProvider.postMessages(mRemoteDevice.url+"/"+BuildConfig.CLIENT_ID+"/msg/v1/", msgBody);
            mMsgProvider.callStatus = MsgProvider.CallerStatus.PARTICIPANT;

            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            intent.putExtra("token", mEdgeAccessToken);
            intent.putExtra("localDevice", mLocalDevice);
            intent.putExtra("shareDevice", mShareDevice);
            intent.putExtra("remoteDevice", mRemoteDevice);
            intent.putExtra("status", mMsgProvider.callStatus);
            startActivityForResult(intent, CHATTING);
        });

        mIgnore.setVisibility(View.INVISIBLE);

        mIgnore.setOnClickListener(view -> {
            mAnswer.setVisibility(View.INVISIBLE);
            mIgnore.setVisibility(View.INVISIBLE);
            vibrator.cancel();
            final Gson mGson = new GsonBuilder()
                    .create();

            SignalMsg msgBody = new SignalMsg();
            msgBody.type = "hangup";
            msgBody.message = mGson.toJson(mLocalDevice);
            mMsgProvider.postMessages(mRemoteDevice.url+"/"+BuildConfig.CLIENT_ID+"/msg/v1/", msgBody);
            toast("Hanging up " + mRemoteDevice.name);
        });

        EdgeConfig config = new EdgeConfig();
        mAppOps = new EdgeAppOps(this, config);
        mTaskList = new ArrayList<>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProgressBar.setVisibility(View.INVISIBLE);
        Intent intent = getIntent();
        if (intent.hasExtra("edgeToken")) {
            mEdgeAccessToken = (String) intent.getSerializableExtra("edgeToken");
        }
        if (intent.hasExtra("userToken")) {
            mUserAccessToken = (String) intent.getSerializableExtra("userToken");

            mMsgProvider = MsgProvider.instance(WS_SERVER_URL, MainActivity.this);
            mMsgProvider.connect(MainActivity.this, null, mEdgeAccessToken);
            mMsgProvider.cleanUp();
        }
        if (hasToken()) {
            mScanButton.setEnabled(true);
                mProgressBar.setVisibility(View.VISIBLE);
                new RefreshDeviceListTask().execute();
        } else {
            toast("Missing login credentials. Go back and try again.");
        }
        multiPermissions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHATTING) {
            mLocalDevice = null;
            mRemoteDevice = null;
            mShareDevice = null;
            mMsgProvider.callStatus = MsgProvider.CallerStatus.UNKNOWN;
            mMsgProvider.cleanUp();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_CAMERA_AND_RECORD_AUDIO)
    private void multiPermissions() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            // ...
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.camera_and_audio_rationale),
                    RC_CAMERA_AND_RECORD_AUDIO, perms);
        }
    }

    private boolean hasToken() {
        return mEdgeAccessToken != null && !mEdgeAccessToken.isEmpty() && mUserAccessToken != null && !mUserAccessToken.isEmpty();
    }

    // Toast message display
    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void selectDevice(Device device) {
        Log.d(TAG, "selectDevice: " + device.name);

        mCallBar.setVisibility(View.VISIBLE);
        new PresenceCheckTask().execute(device);
    }

    @Override
    public void selectShareDevice(Device shareDevice) {
        Log.d(TAG, "selectShareDevice: " + shareDevice.name);
        for (Device device : OtherDeviceList) {
            if (device.id.equalsIgnoreCase(shareDevice.id)) {
                OtherDeviceList.set(OtherDeviceList.indexOf(device), shareDevice);
                mShareDevice = shareDevice;
            }
        }

        if (mAdapter instanceof  DeviceAdapter) {
            ((DeviceAdapter) mAdapter).swap(OtherDeviceList);
        }
    }

    @Override
    public void onIncomingCall(Device device, Device shareDevice) {
        Log.d(TAG, "onIncomingCall: " + device.name);
        mRemoteDevice = device;
        mShareDevice = shareDevice;
        mAnswer.setVisibility(View.VISIBLE);
        mIgnore.setVisibility(View.VISIBLE);
        long[] mVibratePattern = new long[]{0, 400, 200, 600, 200, 800, 200, 1000};
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(mVibratePattern, 0);
        }

        toast("Call from: " + mRemoteDevice.name);
    }

    // Perform work to get a device list from the example service
    private class RefreshDeviceListTask extends AsyncTask<SuperDriveProvider.DeviceFilter, Void, List<Device>> {
        @Override
        protected List<Device> doInBackground(final SuperDriveProvider.DeviceFilter... filterType) {
            List<Device> ret = null;
            try {
                if (filterType != null && filterType.length == 1) {
                    Response<DeviceList> response =
                            SuperDriveProvider.getDevices(filterType[0], mEdgeAccessToken).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        ret = response.body().data;
                    }
                } else {
                    Response<DeviceList> response =
                            SuperDriveProvider.getLocalDevices(mEdgeAccessToken).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        ret = response.body().data;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ret;
        }

        @Override
        protected void onPostExecute(final List<Device> deviceList) {
            mProgressBar.setVisibility(View.INVISIBLE);
            String localDeviceId = MdsProvider.instance().getDeviceId();
            Log.d(TAG, "Local device id: " + localDeviceId);
            if (deviceList != null) {
                OtherDeviceList.clear();
                for (Device device : deviceList) {
                    Log.d(TAG, "onPostExecute: " + device.name + " ~~ " + device.os + " ~~ " + device.id);
                    if (device.id.equalsIgnoreCase(localDeviceId)) {
                        mLocalDevice = device;
                    } else {
                        OtherDeviceList.add(device);
                    }
                }

                if (mAdapter instanceof  DeviceAdapter) {
                    ((DeviceAdapter) mAdapter).swap(OtherDeviceList);
                }
            }
        }
    }

    private class PresenceCheckTask extends AsyncTask<Device, Void, Device> {
        @Override
        protected Device doInBackground(Device... devices) {
            Device ret = null;
            try {
                Response<Device> response =
                        SuperDriveProvider.presenceCheck(devices[0], mEdgeAccessToken).execute();
                if (response.isSuccessful() && response.body() != null) {
                    ret = response.body();
                } else {
                    ret = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ret;
        }

        @Override
        protected void onPostExecute(final Device device) {
            mCallBar.setVisibility(View.INVISIBLE);
            if (device != null) {
                mMsgProvider.callStatus = MsgProvider.CallerStatus.INITIATOR;
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra("token", mEdgeAccessToken);
                intent.putExtra("localDevice", mLocalDevice);
                intent.putExtra("shareDevice", mShareDevice);
                intent.putExtra("remoteDevice", device);
                intent.putExtra("status", mMsgProvider.callStatus);
                startActivityForResult(intent, CHATTING);

                final Gson mGson = new GsonBuilder()
                        .create();

                SignalMsg msgBody = new SignalMsg();
                msgBody.type = "ring";
                msgBody.message = "{ caller:" + mGson.toJson(mLocalDevice) + ", share:" + mGson.toJson(mShareDevice) + "}" ;
                mMsgProvider.postMessages(device.url+"/"+BuildConfig.CLIENT_ID+"/msg/v1/", msgBody);
                toast("Ringing " + device.name);
            } else {
                toast("Device is online but not available.");
            }
        }
    }

}
