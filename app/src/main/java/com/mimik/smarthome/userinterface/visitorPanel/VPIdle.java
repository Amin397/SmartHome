package com.mimik.smarthome.userinterface.visitorPanel;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.animation.Animator;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mimik.edgeappops.EdgeAppOps;
import com.mimik.edgeappops.edgeservice.EdgeConfig;
import com.mimik.smarthome.BuildConfig;
import com.mimik.smarthome.R;
import com.mimik.smarthome.userinterface.common.ChatActivity;
import com.mimik.smarthome.edgeSDK.Device;
import com.mimik.smarthome.edgeSDK.DeviceAdapter;
import com.mimik.smarthome.edgeSDK.DeviceList;
import com.mimik.smarthome.edgeSDK.MdsProvider;
import com.mimik.smarthome.edgeSDK.MsgProvider;
import com.mimik.smarthome.edgeSDK.SignalMsg;
import com.mimik.smarthome.edgeSDK.SuperDriveProvider;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Response;

public class VPIdle extends AppCompatActivity implements DeviceAdapter.IDeviceSelectCallback, MsgProvider.MsgListener {
    //-----
    private static final String TAG = "VPanelActivity";
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

    //___________

    private EditText mBuzzText;
    private EditText mMainPass;

    ProgressBar mProgressBar;
    ProgressBar mCallBar;
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    ImageButton mAnswer;
    ImageButton mIgnore;
    Button mScanButton;

    ImageButton mCall;
    ImageButton mUnlock;

    private Button mbtnGo;
    private TextView mnfcText;
    private int flag_in_QRCODE_Read=0;

    protected NfcAdapter nfcAdapter;
    PendingIntent mPendingIntent;
    IntentFilter[] mIntentFilters;
    String[][] mNFCTechLists;
    String _inputQrcode="";
    int count=0;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    public static final String ERROR_DETECTED = "No NFC tag detected!";
    public static final String WRITE_SUCCESS = "Text written to the NFC tag successfully!";
    public static final String WRITE_ERROR = "Error during writing, is the NFC tag close enough to your device?";

    boolean writeMode;
    Tag myTag;
    Context context;
    private TextToSpeech mTTS;







    private ImageView logo_click;
    private View include_V;
    private LottieAnimationView anim_setting;
    private LottieAnimationView anim_focuse;

    private TextInputLayout txt_buzz_num , txt_password;
    private Button btn_buzz_click , btn_password_click;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN , WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_vpidle);
        initializeComponents();
        //init_Text_to_Speech();

        anim_focuse.playAnimation();
        logo_click.setOnClickListener(logoAnimationVisitor);

        btn_buzz_click.setOnClickListener(buzzClickListener);
        btn_password_click.setOnClickListener(passwordClickListener);

    }

    private Button.OnClickListener passwordClickListener
             = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            animationPasswordButtonClick();
        }
    };

    private void animationPasswordButtonClick() {
        ViewPropertyAnimator viewPropertyAnimator = txt_password.animate();
        viewPropertyAnimator.x(300f);
        viewPropertyAnimator.setDuration(900);
        viewPropertyAnimator.setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                txt_password.setEnabled(true);
                txt_password.setSelected(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private Button.OnClickListener buzzClickListener
             = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            animationBuzzButtonClick();
        }
    };

    private void animationBuzzButtonClick() {
        ViewPropertyAnimator viewPropertyAnimator = txt_buzz_num.animate();
        viewPropertyAnimator.x(300f);
        viewPropertyAnimator.setDuration(900);
        viewPropertyAnimator.setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                txt_buzz_num.setEnabled(true);
                txt_buzz_num.setSelected(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private ImageView.OnClickListener logoAnimationVisitor
             = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            animationLogo();
        }
    };

    private void animationLogo() {
        ViewPropertyAnimator viewPropertyAnimator = logo_click.animate();
        viewPropertyAnimator.x(50f);
        viewPropertyAnimator.y(80f);
        viewPropertyAnimator.setDuration(1000);
        viewPropertyAnimator.setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                anim_focuse.setVisibility(View.GONE);
                logo_click.setImageResource(R.drawable.mimik_logo_small2);

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                include_V.setVisibility(View.VISIBLE);
                anim_setting.playAnimation();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }


    public void init_Text_to_Speech(){

        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.CANADA);

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    } else {
                        Speak("Welcome to Mimik Technology");
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });
    }

    private void Speak(String text) {
        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void initializeComponents() {

        txt_password = (TextInputLayout) findViewById(R.id.password_layout_id);
        btn_password_click = (Button) findViewById(R.id.btn_show_password_id);
        anim_setting = (LottieAnimationView) findViewById(R.id.anim_setting_id);
        anim_focuse = (LottieAnimationView) findViewById(R.id.anim_focuse_id);
        mBuzzText = (EditText) findViewById(R.id.TBNum);
        logo_click = (ImageView) findViewById(R.id.image_logo_visitor_id);
        mMainPass = (EditText) findViewById(R.id.TPass);
        btn_buzz_click = (Button) findViewById(R.id.btn_show_username_id);
        txt_buzz_num = (TextInputLayout) findViewById(R.id.buzz_layout_id);

        mCall =(ImageButton)findViewById(R.id.bcall);
        mUnlock=(ImageButton)findViewById(R.id.bunlock);

        mnfcText = (TextView) findViewById(R.id.nfcText);
        include_V = findViewById(R.id.include_visitor_id);

        mCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCall_clicked();
            }
        });


        //nfc_process();
        //call_init();


    }
    //_____________________________________________________


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
            new RefreshDeviceListTask().execute();
        });
        List<Device> mDeviceList = new ArrayList<>();

        mAdapter = new DeviceAdapter(mDeviceList);

        if (mAdapter instanceof DeviceAdapter) {
            ((DeviceAdapter) mAdapter).registerCallback(VPIdle.this);
        }

        mLayoutManager = new LinearLayoutManager(this);

        /*mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);*/

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

            Intent intent = new Intent(VPIdle.this, ChatActivity.class);
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


    // Toast message display
    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void nfc_process()
    {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
        }
        readFromIntent(getIntent());

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };

    }

    private void QRCode_process()
    {
        //Toast.makeText(getApplicationContext(), _inputQrcode, Toast.LENGTH_LONG).show();
        Speak("Welcome");
    }

    private void mCall_clicked() {


        if (!mBuzzText.getText().toString().isEmpty() && !mMainPass.getText().toString().isEmpty()) {


            checkBuzz(mBuzzText.getText().toString().trim());

            checkPass(mMainPass.getText().toString().trim());


           // selectDevice();


        } else {
            ShowMessage("Message", "Please select Unit number or if you are Owner/Admin Please enter password.");

        }
    }

    private boolean checkBuzz(String buzzNum) {

        //Check Vaildation unit number format
        Toast.makeText(this,
                "buzz number is " + buzzNum,
                Toast.LENGTH_SHORT).show();
        return true;
    }

    private boolean checkPass(String Pass) {

        //Check Vaildation unit number format
        return true;
    }

    private void ShowMessage(String Tittle, String Message) {

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(Tittle);
        alertDialog.setMessage(Message);
        //alertDialog.setIcon(R.drawable.firstpage);

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog.show();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getAction() == 1 && event.getKeyCode() != 56 && flag_in_QRCODE_Read == 1) {


            _inputQrcode += (char) event.getUnicodeChar();
            count++;
            return true;
        } else if (event.getAction() == 1 && event.getKeyCode() == 56 && flag_in_QRCODE_Read == 0) {
            flag_in_QRCODE_Read = 1;
            count = 0;
            _inputQrcode = "";
            return true;

        } else if (event.getAction() == 1 && event.getKeyCode() == 56 && flag_in_QRCODE_Read == 1) {
            //System.out.println(_inputQrcode);
            //call QRCode function....
            QRCode_process();

            flag_in_QRCODE_Read = 0;
            _inputQrcode = "";
            return true;
        }      // if(event.getAction()==1 && event.getUnicodeChar()!='^')
        else if(flag_in_QRCODE_Read==1 ||  event.getKeyCode() == 56 )
            return true;
        //if(event.getAction()==1 )
        // System.out.println(event.getAction() + " --" +"Flag"+ String.valueOf(flag)+"  "+ event.getKeyCode() + " - " + (char) event.getUnicodeChar());

        return super.dispatchKeyEvent(event);

    }


    /******************************************************************************
     **********************************Read From NFC Tag***************************
     ******************************************************************************/
    private void readFromIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_TAG);
            byte[] tagId = getIntent().getByteArrayExtra(NfcAdapter.EXTRA_ID);

            String hexdump = new String();
            for (int i = 0; i < tagId.length; i++) {
                String x = Integer.toHexString(((int) tagId[i] & 0xff));
                if (x.length() == 1) {
                    x = '0' + x;
                }
                hexdump += x + ' ';
            }

            Toast.makeText(getApplicationContext(), hexdump, Toast.LENGTH_SHORT).show();

            NdefMessage[] msgs = null;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            buildTagViews(msgs);
        }
    }
    private void buildTagViews(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) return;

        String text = "";
//        String tagId = new String(msgs[0].getRecords()[0].getType());
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; // Get the Text Encoding
        int languageCodeLength = payload[0] & 0063; // Get the Language Code, e.g. "en"
        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");

        try {
            // Get the Text
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e("UnsupportedEncoding", e.toString());
        }

        // tvNFCContent.setText("NFC Content: " + text);
    }


    /******************************************************************************
     **********************************Write to NFC Tag****************************
     ******************************************************************************/

    private void write(String text, Tag tag) throws IOException, FormatException {
        NdefRecord[] records = { createRecord(text) };
        NdefMessage message = new NdefMessage(records);
        // Get an instance of Ndef for the tag.
        Ndef ndef = Ndef.get(tag);
        // Enable I/O
        ndef.connect();
        // Write the message
        ndef.writeNdefMessage(message);
        // Close the connection
        ndef.close();
    }
    private NdefRecord createRecord(String text) throws UnsupportedEncodingException {
        String lang       = "en";
        byte[] textBytes  = text.getBytes();
        byte[] langBytes  = lang.getBytes("US-ASCII");
        int    langLength = langBytes.length;
        int    textLength = textBytes.length;
        byte[] payload    = new byte[1 + langLength + textLength];

        // set status byte (see NDEF spec for actual bits)
        payload[0] = (byte) langLength;

        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1,              langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_TEXT,  new byte[0], payload);

        return recordNFC;
    }



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        readFromIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }
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
                Intent intent = new Intent(VPIdle.this, ChatActivity.class);
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
        //mProgressBar.setVisibility(View.INVISIBLE);
        Intent intent = getIntent();
        if (intent.hasExtra("edgeToken")) {
            mEdgeAccessToken = (String) intent.getSerializableExtra("edgeToken");
        }
        if (intent.hasExtra("userToken")) {
            mUserAccessToken = (String) intent.getSerializableExtra("userToken");

            mMsgProvider = MsgProvider.instance(WS_SERVER_URL, VPIdle.this);
            mMsgProvider.connect(VPIdle.this, null, mEdgeAccessToken);
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
}