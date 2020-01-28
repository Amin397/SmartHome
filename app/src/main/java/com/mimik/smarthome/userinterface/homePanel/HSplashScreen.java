package com.mimik.smarthome.userinterface.homePanel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import com.mimik.smarthome.R;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import com.mimik.smarthome.BuildConfig;

public class HSplashScreen extends AppCompatActivity {
    private boolean _isPermissionRequested = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hsplash);
        Handler startHandler = new Handler();
        startHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                start();
            }
        }, 3000);
    }

    private void start() {
        Intent i = new Intent(this, HPIdle.class);
        startActivity(i);
        finish();

    }

    private boolean checkPermissions() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            if (_isPermissionRequested == false) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS, Manifest.permission.CALL_PHONE}, 0);
                _isPermissionRequested = true;
            }

            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        _isPermissionRequested = false;

        if (checkPermissions())
            start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
