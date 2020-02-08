package com.mimik.smarthome.userinterface.visitorPanel;

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
import com.mimik.smarthome.fragments.visitor_panel.DateAndTime;
import com.mimik.smarthome.fragments.visitor_panel.ResetFactory;
import com.mimik.smarthome.fragments.visitor_panel.UnitConfigurations;
import com.mimik.smarthome.fragments.visitor_panel.UpdateFrimWare;

public class V_Setting extends AppCompatActivity {


    private BottomNavigationView bottom_nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN , WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_v_setting);

        initViews();

        bottom_nav.setOnNavigationItemSelectedListener(mBottomNavItemClickListener);
        bottom_nav.setSelectedItemId(R.id.nav_unit_config_id);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mBottomNavItemClickListener
             = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            Fragment fragment;
            switch (menuItem.getItemId()){
                case R.id.nav_date_time_id:{
                    fragment = new DateAndTime();
                    loadFragment(fragment);
                    return true;
                }
                case R.id.nav_reset_factory_id:{
                    fragment = new ResetFactory();
                    loadFragment(fragment);
                    return true;
                }
                case R.id.nav_unit_config_id:{
                    fragment = new UnitConfigurations();
                    loadFragment(fragment);
                    return true;
                }
                case R.id.nav_update_firmware_id:{
                    fragment = new UpdateFrimWare();
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

    private void initViews() {
        bottom_nav = (BottomNavigationView) findViewById(R.id.bottom_navigation_visitor_id);
    }
}
