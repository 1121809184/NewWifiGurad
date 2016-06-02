package com.sharedream.wifiguard.app;

import android.app.Activity;
import android.app.Application;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.utils.GlobalField;

import java.util.ArrayList;
import java.util.List;

import cn.smssdk.SMSSDK;

public class AppContext extends Application {
    private static AppContext instance;
    private List<Activity> activityList;

    public static AppContext getContext() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        activityList = new ArrayList<Activity>();
        SMSSDK.initSDK(this, Constant.SMS_SDK_APP_KEY, Constant.SMS_SDK_APP_SECRET);
        SDKInitializer.initialize(this);
        startLocation();
    }

    private void startLocation() {
        LocationClient locationClient = new LocationClient(this);
        MyLocationListener myLocationListener = new MyLocationListener();
        locationClient.registerLocationListener(myLocationListener);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
        option.setCoorType("bd09ll");
        option.setOpenGps(false);
        option.setScanSpan(1000 * 60);
        option.setIsNeedAddress(true);
        locationClient.setLocOption(option);
        locationClient.start();
        locationClient.requestLocation();
    }

    class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null) {
                return;
            }
            float lng = (float) location.getLongitude();
            float lat = (float) location.getLatitude();
            float radius = location.getRadius();
            String cityId = location.getCityCode();
            String addrStr = location.getAddrStr();
            String province = location.getProvince();
            String city = location.getCity();
            String district = location.getDistrict();
            String street = location.getStreet();
            String streetNumber = location.getStreetNumber();
            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_LOC_LNG, lng);
            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_LOC_LAT, lat);
            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_LOC_RADIUS, radius);
            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_LOC_CITY_ID, cityId);
            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_LOC_ADDRESS, addrStr);
            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_LOC_PROVINCE, province);
            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_LOC_CITY, city);
            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_LOC_DISTRICT, district);
            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_LOC_STREET, street);
            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_LOC_STREET_NUMBER, streetNumber);
        }
    }

    public void addActivity(Activity activity) {
        if (activity != null) {
            activityList.add(activity);
        }
    }

    public void finishActivity(Activity activity) {
        if (activity != null && activityList.contains(activity)) {
            activityList.remove(activity);
            activity.finish();
        }
    }

    public void finishAllActivity() {
        if (activityList != null) {
            for (int i = 0; i < activityList.size(); i++) {
                Activity activity = activityList.get(i);
                activity.finish();
            }
        }
    }
}
