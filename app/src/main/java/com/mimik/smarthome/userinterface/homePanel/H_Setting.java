package com.mimik.smarthome.userinterface.homePanel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mimik.smarthome.R;
import com.mimik.smarthome.fragments.home_panel.Key_Generator;
import com.mimik.smarthome.fragments.home_panel.Logs;
import com.mimik.smarthome.fragments.home_panel.Mobile_App;
import com.mimik.smarthome.fragments.visitor_panel.DateAndTime;
import com.mimik.smarthome.fragments.visitor_panel.ResetFactory;
import com.mimik.smarthome.fragments.visitor_panel.UnitConfigurations;

public class H_Setting extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN , WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_h_setting);

        initView();

        bottomNavigationView.setOnNavigationItemSelectedListener(mNavHomeItemClickListener);

    }

    private BottomNavigationView.OnNavigationItemSelectedListener mNavHomeItemClickListener
             = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()){
                case R.id.nav_logs_id:{
                    fragment = new Logs();
                    loadFragment(fragment);
                    return true;
                }
                case R.id.nav_v_keys_generator_id:{
                    fragment = new Key_Generator();
                    loadFragment(fragment);
                    return true;
                }
                case R.id.nav_mobile_app_id:{
                    fragment = new Mobile_App();
                    loadFragment(fragment);
                    return true;
                }
            }
            return false;
        }
    };

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container_visitor_id, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void initView() {
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation_home_id);
    }
}
