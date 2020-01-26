package com.mimik.smarthome;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.mimik.edgeappops.EdgeAppOps;
import com.mimik.edgeappops.edgeservice.EdgeConfig;
import com.mimik.smarthome.edgeSDK.Utils;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";

    EdgeAppOps mAppOps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Utils.isPackageInstalled("com.mimik.edgeservice", getPackageManager())) {
            Utils.showPackagePrompt(this, this.getClass());
        } else {
            EdgeConfig config = new EdgeConfig();
            mAppOps = new EdgeAppOps(this, config);
            if (mAppOps.startEdge()) {
                toast(getString(R.string.toast_start));
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(SplashActivity.this, StartupActivity.class);
                        startActivity(intent);

                    }
                }, 750);
            } else {
                toast(getString(R.string.toast_failed_start));
            }
        }
    }

    // Toast message display
    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
