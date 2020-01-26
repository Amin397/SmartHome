package com.mimik.smarthome.userinterface.homePanel;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.mimik.edgeappops.EdgeAppOps;
import com.mimik.edgeappops.edgeservice.EdgeConfig;
import com.mimik.smarthome.BuildConfig;
import com.mimik.smarthome.MainActivity;
import com.mimik.smarthome.Model.CAdmin;
import com.mimik.smarthome.R;
import com.mimik.smarthome.dataAccess.AdminRepository;
import com.mimik.smarthome.dataAccess.SQLiteHelper;
import com.mimik.smarthome.edgeSDK.Device;
import com.mimik.smarthome.edgeSDK.DeviceAdapter;
import com.mimik.smarthome.edgeSDK.DeviceList;
import com.mimik.smarthome.edgeSDK.MdsProvider;
import com.mimik.smarthome.edgeSDK.MsgProvider;
import com.mimik.smarthome.edgeSDK.SignalMsg;
import com.mimik.smarthome.edgeSDK.SuperDriveProvider;
import com.mimik.smarthome.hwinterface.deviceUSB;
import com.mimik.smarthome.infrastructure.StaticData;
import com.mimik.smarthome.userinterface.common.ChatActivity;
import com.mimik.smarthome.userinterface.visitorPanel.VPIdle;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Response;

import static com.mimik.smarthome.infrastructure.StaticData.getContext;

public class HPIdle extends AppCompatActivity implements DeviceAdapter.IDeviceSelectCallback, MsgProvider.MsgListener {

    //-----
    private static final String TAG = "HPanelActivity";
    private static final int CHATTING = 167;
    private static final int RC_CAMERA_AND_RECORD_AUDIO = 153;

    private final String WS_SERVER_URL = "ws://127.0.0.1:8083/ws/"+ BuildConfig.CLIENT_ID+"/msg/v1";
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
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    ImageButton mAnswer;
    ImageButton mIgnore;
    Button mScanButton;

    //-----------Admin form -------------

      LinearLayout FAdmin;
      Button mSIGNIN;
      EditText mTAdminUser;
      EditText mTadminPassword;
      AdminRepository admindb;
    //-----------------------------------



    //-----------------------------------
    ImageButton mSetting;
    TabLayout   mTabSetting;
    ViewPager   mViewpageSetting;





    private ChipGroup MessageGroup;

    SQLiteHelper DatabaseHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hpidle);

        StaticData.setContext(this);





        //-------------------------------

        //---------Check Configuration-------------


        call_init();
        AdminPanelInit();
    }
    //----------------------------------------
    private void AdminPanelInit(){


        //-------------Setting TAB------------------
        mTabSetting=findViewById(R.id.SettingTab);
        mViewpageSetting=findViewById(R.id.ViewpageSetting);

        mTabSetting.addTab(mTabSetting.newTab().setText("VirtualKeys"));
        mTabSetting.addTab(mTabSetting.newTab().setText("FoB"));
        mTabSetting.addTab(mTabSetting.newTab().setText("Users"));
        mTabSetting.setTabGravity(TabLayout.GRAVITY_FILL);

        final MyAdapter adapter = new MyAdapter(this,getSupportFragmentManager(), mTabSetting.getTabCount());
        mViewpageSetting.setAdapter(adapter);

        mViewpageSetting.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabSetting));

        mTabSetting.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewpageSetting.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        //------------------------------------------------------
        mSIGNIN=findViewById(R.id.bAdminSignin);
        mTadminPassword=findViewById(R.id.TAdminPassword);
        mTAdminUser=findViewById(R.id.TAdminUser);

        MessageGroup =findViewById(R.id.messageGroup);

        Chip maleChip = getChip(MessageGroup, "Message 1");
        Chip femaleChip = getChip(MessageGroup, "Message 2");
        MessageGroup.addView(maleChip);
        MessageGroup.addView(femaleChip);



        mSIGNIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTAdminUser.getText()!=null && !mTAdminUser.getText().toString().isEmpty() &&
                        mTadminPassword.getText()!=null && !mTadminPassword.getText().toString().isEmpty()
                ) {

                    CAdmin admin= admindb.GetAdminByNamePass(mTAdminUser.getText().toString(),mTadminPassword.getText().toString());
                    if (admin.getName() != "Admin" && admin.getPass()=="Mimik" )
                    {
                        //User OK.
                        showDialogMessage("Hi, Admin is ok");
                        //Go to Setting Page
                        FAdmin.setVisibility(View.INVISIBLE);

                        //---------------------------------------------------


                        //----------------------------------------------------
                    }
                    else{
                        showDialogMessage(getResources().getString(R.string.invalid_username_password));
                        FAdmin.setVisibility(View.INVISIBLE);
                        // sendNotification("Hi New Message ","Tittle","",this);
                    }
                }
                else {
                    showDialogMessage(getResources().getString(R.string.invalid_username_password));
                }
            }
        });


    }
    //_____________________________________________________
    private void call_init()
    {

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
            new HPIdle.RefreshDeviceListTask().execute();
        });
        List<Device> mDeviceList = new ArrayList<>();

        mAdapter = new DeviceAdapter(mDeviceList);

        if (mAdapter instanceof DeviceAdapter) {
            ((DeviceAdapter) mAdapter).registerCallback(HPIdle.this);
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

            Intent intent = new Intent(HPIdle.this, ChatActivity.class);
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


    private Chip getChip(final ChipGroup chipGroup, String text){
        final Chip chip = new Chip(this);
        chip.setChipDrawable(ChipDrawable.createFromResource(this, R.xml.bgbutton));
        int paddingDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        chip.setPadding(paddingDp, paddingDp, paddingDp, paddingDp);
        chip.setText(text);
        chip.setOnCloseIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chipGroup.removeView(chip);
            }
        });
        return chip;
    }

    private void showDialogMessage(String Message){

        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(Message);
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });



        AlertDialog alert11 = builder1.create();
        alert11.show();

    }


    private class generateQrcode extends AsyncTask<String, Void, Bitmap> {
        public final static int WIDTH = 400;
        ImageView bmImage;

        public generateQrcode(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String Value = urls[0];
            QRCodeWriter writer = new QRCodeWriter();
            Bitmap bitmap = null;
            BitMatrix bitMatrix = null;

            try {
                bitMatrix = writer.encode(Value, com.google.zxing.BarcodeFormat.QR_CODE, WIDTH, WIDTH);
                bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.RGB_565);
                for (int i = 0; i < 400; i++) {
                    for (int j = 0; j < 400; j++) {
                        bitmap.setPixel(i, j, bitMatrix.get(i, j) ? Color.BLACK
                                : Color.WHITE);
                    }
                }
            } catch (WriterException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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


    @Override
    public void selectDevice(Device device) {
        Log.d(TAG, "selectDevice: " + device.name);

        mCallBar.setVisibility(View.VISIBLE);
        new HPIdle.PresenceCheckTask().execute(device);
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
                Intent intent = new Intent(HPIdle.this, ChatActivity.class);
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

            mMsgProvider = MsgProvider.instance(WS_SERVER_URL, HPIdle.this);
            mMsgProvider.connect(HPIdle.this, null, mEdgeAccessToken);
            mMsgProvider.cleanUp();
        }
        if (hasToken()) {
            mScanButton.setEnabled(true);
            mProgressBar.setVisibility(View.VISIBLE);
            new HPIdle.RefreshDeviceListTask().execute();
        } else {
            toast("Missing login credentials. Go back and try again.");
        }
        multiPermissions();
    }

}
