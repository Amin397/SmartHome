package com.mimik.smarthome.userinterface.visitorPanel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.mimik.smarthome.R;
import com.mimik.smarthome.Util;
import com.mimik.smarthome.fragments.home_panel.Key_Generator;
import com.mimik.smarthome.fragments.home_panel.Logs;
import com.mimik.smarthome.fragments.home_panel.Mobile_App;
import com.mimik.smarthome.fragments.visitor_panel.DateAndTime;
import com.mimik.smarthome.fragments.visitor_panel.ResetFactory;
import com.mimik.smarthome.fragments.visitor_panel.UnitConfigurations;
import com.mimik.smarthome.fragments.visitor_panel.UpdateFrimWare;
import com.mimik.smarthome.userinterface.homePanel.MyAdapter;

import java.util.ArrayList;
import java.util.List;

public class V_Setting extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_v_setting);

        initViews();

        setUpViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        setTabIcons();
    }

    private void setTabIcons() {
        int[] ids = {R.drawable.unit_configuration_24dp , R.drawable.date_time_24dp
         , R.drawable.ic_settings_black_24dp , R.drawable.ic_reset_factory_24dp};
        int selectedColor = Color.parseColor("#00574B");
        int unSelectedColor = Color.parseColor("#000000");
        Util.setupTabIcons(getApplicationContext() , tabLayout , ids , 0 , selectedColor , unSelectedColor);
    }

    private void setUpViewPager(ViewPager viewPager) {
        Util.ViewPagerAdapter adapter = new Util.ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new UnitConfigurations(), "Unit Configurations");
        adapter.addFragment(new DateAndTime(), "Date & Time");
        adapter.addFragment(new UpdateFrimWare(), "Firmware");
        adapter.addFragment(new ResetFactory(), "Reset Factory");
        viewPager.setAdapter(adapter);
    }

    private void initViews() {
        tabLayout = (TabLayout) findViewById(R.id.tab_layout_visitor_id);
        viewPager = (ViewPager) findViewById(R.id.view_pager_visitor_id);
    }
}
