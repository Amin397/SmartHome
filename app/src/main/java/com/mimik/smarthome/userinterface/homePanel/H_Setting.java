package com.mimik.smarthome.userinterface.homePanel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.material.tabs.TabLayout;
import com.mimik.smarthome.R;
import com.mimik.smarthome.fragments.home_panel.Key_Generator;
import com.mimik.smarthome.fragments.home_panel.Logs;
import com.mimik.smarthome.fragments.home_panel.Mobile_App;

public class H_Setting extends AppCompatActivity {

    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN , WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_h_setting);

        initView();

        tabDetails();

        tabLayout.addOnTabSelectedListener(tabClickListener);

    }

    private TabLayout.OnTabSelectedListener tabClickListener
             = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            Fragment fragment;
            switch (tab.getPosition()){
                case 0:{
                    fragment = new Logs();
                    loadFragment(fragment);
                    break;
                }
                case 1:{
                    fragment = new Key_Generator();
                    loadFragment(fragment);
                    break;
                }
                case 2:{
                    fragment = new Mobile_App();
                    loadFragment(fragment);
                    break;
                }
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    };

    private void tabDetails() {
        tabLayout.addTab(tabLayout.newTab().setText(R.string.logs).setIcon(R.drawable.ic_list_black_24dp));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.key_generator).setIcon(R.drawable.qr_code));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.mobile_app).setIcon(R.drawable.application));
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container_home_id, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void initView() {
        tabLayout = (TabLayout) findViewById(R.id.tab_layout_home_id);
    }
}
