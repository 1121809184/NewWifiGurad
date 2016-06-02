package com.sharedream.wifiguard;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.sharedream.wifi.sdk.InitListener;
import com.sharedream.wifi.sdk.ShareDreamStyle;
import com.sharedream.wifi.sdk.ShareDreamWifiSdk;
import com.sharedream.wifi.sdk.activity.WifiManagerActivity;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.fragment.BaseFragment;
import com.sharedream.wifiguard.fragment.OptimizeFragment;
import com.sharedream.wifiguard.fragment.ToolFragment;
import com.sharedream.wifiguard.fragment.VerifyCenterFragment;
import com.sharedream.wifiguard.manager.SystemBarTintManager;
import com.sharedream.wifiguard.utils.GlobalField;
import com.sharedream.wifiguard.utils.MyUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements ViewPager.OnPageChangeListener, RadioGroup.OnCheckedChangeListener {
    private ViewPager vpHome;
    //private RadioButton rbData;
    private RadioGroup rgNavigation;
    private RadioButton rbSecurity;
    private RadioButton rbOptimize;
    private RadioButton rbTool;
    private long exitTime = 0;

    private int flag = 1;
    public List<BaseFragment> fragmentList = new ArrayList<BaseFragment>();

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
        activity.finish();
        //AppContext.getContext().finishAllActivity();
    }

    public static void launch(Activity activity,boolean login){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.theme_color);
        }
        setContentView(R.layout.activity_main);
        inidShareDreamSDK();
        initView();
        setListener();
        initFragment();
        initData();
    }

    private void inidShareDreamSDK() {
        //String userid = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_USER_ID, null);
        String uuid = MyUtils.getDeviceUUID(AppContext.getContext());
        ShareDreamWifiSdk.init(AppContext.getContext(), Constant.SHARE_DREAM_WIFI_SDK_TOKEN, uuid,"");
        ShareDreamWifiSdk.setTitle("WiFi卫士");
        ShareDreamWifiSdk.showAdBanner();

        ShareDreamStyle shareDreamStyle = new ShareDreamStyle();
        shareDreamStyle.colorMainStyle = Color.parseColor("#2f8bfb");
        shareDreamStyle.drawableSolidButtonBgSelector = R.drawable.button_phone_binding_bg;
        ShareDreamWifiSdk.setStyle(shareDreamStyle);

        ShareDreamWifiSdk.registerListener(new InitListener() {
            @Override
            public void onInitResponse(int i) {
                //MyUtils.showToast("Result:" + i, MainActivity.this);
            }
        });
    }

    private void initView() {
        vpHome = ((ViewPager) findViewById(R.id.vp_home));
        rgNavigation = ((RadioGroup) findViewById(R.id.rg_navigation));
        //rbData = ((RadioButton) findViewById(R.id.rb_data));
        rbSecurity = ((RadioButton) findViewById(R.id.rb_security));
        rbOptimize = ((RadioButton) findViewById(R.id.rb_optimize));
        rbTool = ((RadioButton) findViewById(R.id.rb_tool));

    }

    private void setListener() {
        vpHome.addOnPageChangeListener(this);
        rgNavigation.setOnCheckedChangeListener(this);
    }

    private void initFragment() {
        if (fragmentList.isEmpty()) {
            //DataFragment dataFragment = new DataFragment();
            VerifyCenterFragment securityFragment = new VerifyCenterFragment();
            OptimizeFragment optimizeFragment = new OptimizeFragment();
            ToolFragment toolFragment = new ToolFragment();
            //fragmentList.add(dataFragment);
            fragmentList.add(securityFragment);
            fragmentList.add(toolFragment);
            fragmentList.add(optimizeFragment);
        }
    }

    private void initData() {
        MainPagerAdapter mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        vpHome.setOffscreenPageLimit(2);
        vpHome.setAdapter(mainPagerAdapter);

        boolean moreAddShop = GlobalField.restoreFieldBoolean(AppContext.getContext(), Constant.SP_KEY_MORE_ADD_SHOP, false);
        if (moreAddShop) {
            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_MORE_ADD_SHOP, false);
            vpHome.setCurrentItem(0);
            rgNavigation.check(R.id.rb_security);
        } else {
            vpHome.setCurrentItem(0);
            rgNavigation.check(R.id.rb_security);
        }
        boolean login = GlobalField.restoreFieldBoolean(AppContext.getContext(), Constant.INTENT_KEY_LOGIN, false);
        if(!login){
            vpHome.setCurrentItem(0);
            rgNavigation.check(R.id.rb_security);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        GlobalField.saveField(AppContext.getContext(), "from_wifi_detail", false);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        switch (position) {
            case 0:
                rgNavigation.check(R.id.rb_security);
                break;
            case 1:
                rgNavigation.check(R.id.rb_optimize);
                break;
            case 2:
                rgNavigation.check(R.id.rb_tool);
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_security:
                vpHome.setCurrentItem(0, false);
                flag = 1;
                break;
            case R.id.rb_optimize:
                //vpHome.setCurrentItem(1, false);
                WifiManagerActivity.launch(MainActivity.this);
                if (flag == 1) {
                    rgNavigation.check(R.id.rb_security);
                } else {
                    rgNavigation.check(R.id.rb_tool);
                }
                break;
            case R.id.rb_tool:
                vpHome.setCurrentItem(2, false);
                flag = 2;
                break;
        }
    }

    class MainPagerAdapter extends FragmentStatePagerAdapter {

        public MainPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (System.currentTimeMillis() - exitTime > 2000) {
                MyUtils.showToast("再按一次退出程序", Toast.LENGTH_SHORT, this);
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
        }
        return true;
    }
}
