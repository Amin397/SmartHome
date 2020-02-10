package com.mimik.smarthome.userinterface.homePanel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

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

public class H_Setting extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN , WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_h_setting);

        initView();

        setUpViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        setTabIcons();
    }

    private void setTabIcons() {
        int[] ids = {R.drawable.ic_list_black_24dp , R.drawable.qr_code
                , R.drawable.application};
        int selectedColor = Color.parseColor("#00574B");
        int unSelectedColor = Color.parseColor("#000000");
        Util.setupTabIcons(getApplicationContext() , tabLayout , ids , 1 , selectedColor , unSelectedColor);
    }

    private void setUpViewPager(ViewPager viewPager) {
        Util.ViewPagerAdapter adapter = new Util.ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new Logs(), "Log");
        adapter.addFragment(new Key_Generator(), "Virtual Key Generator");
        adapter.addFragment(new Mobile_App(), "Mobile Application Settings");
        viewPager.setAdapter(adapter);
    }

    private void initView() {
        tabLayout = (TabLayout) findViewById(R.id.tab_layout_home_id);
        viewPager = (ViewPager) findViewById(R.id.view_pager_home_id);
    }
}
