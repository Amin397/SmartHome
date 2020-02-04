package com.mimik.smarthome;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.android.material.snackbar.Snackbar;

public class VideoCall extends AppCompatActivity {


    private ImageView call_reject , call_mute , call_mic_off , open_door;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN , WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_call);

        initView();

        call_mute.setOnClickListener(muteClickListener);
        call_reject.setOnClickListener(rejectClickListener);
        call_mic_off.setOnClickListener(micOffClickListener);
        open_door.setOnClickListener(openDoorClickListener);


    }

    private ImageView.OnClickListener openDoorClickListener
             = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Snackbar snackbar = Snackbar
                    .make(v, "Door Opened !", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    };

    private ImageView.OnClickListener micOffClickListener
             = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Snackbar snackbar = Snackbar
                    .make(v, "Microphone off !", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    };

    private ImageView.OnClickListener rejectClickListener
             = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Snackbar snackbar = Snackbar
                    .make(v, "Call Rejected !", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    };

    private ImageView.OnClickListener muteClickListener
             = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Snackbar snackbar = Snackbar
                    .make(v, "Muted !", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    };

    private void initView() {
        call_reject = (ImageView) findViewById(R.id.call_reject_id);
        call_mic_off = (ImageView) findViewById(R.id.call_mic_off_id);
        call_mute = (ImageView) findViewById(R.id.call_mute_id);
        open_door = (ImageView) findViewById(R.id.call_open_door_id);
    }
}
