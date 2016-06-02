package com.sharedream.wifiguard.cmd;

import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.utils.GlobalField;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CmdCheckWifiBinding {
    public static final String PARAMS_USER_ID = "userid" ;
    public static final String PARAMS_ACCESS_KEY = "accessKey" ;
    public static final String PARAMS_CITY_ID = "cityId" ;
    public static final String PARAMS_AP_LIST = "apList" ;
    public static final String PARAMS_BSSID = "bssid" ;
    public static final String PARAMS_SSID = "ssid" ;
    public static final String PARAMS_PASSWD = "passwd" ;
    public static final String PARAMS_LEVEL = "level" ;
    public static final String PARAMS_FREQUENCY = "frequency" ;
    public static final String PARAMS_SECURITY = "security" ;
    public static final String PARAMS_LONGITUDE = "lng" ;
    public static final String PARAMS_LATITUDE = "lat" ;

    public static final String RESULTS_CODE = "code" ;
    public static final String RESULTS_MSG = "msg" ;
    public static final String RESULTS_DATA = "data" ;
    public static final String RESULTS_AP_ID = "apId" ;
    public static final String RESULTS_SSID = "ssid" ;
    public static final String RESULTS_BSSID = "bssid" ;
    public static final String RESULTS_SECURITY = "security" ;
    public static final String RESULTS_LEVEL = "level" ;
    public static final String RESULTS_FREQUENCY = "frequency" ;
    public static final String RESULTS_PLACE = "place" ;
    public static final String RESULTS_SHOP_ID = "shopId" ;
    public static final String RESULTS_SHOP_NAME = "shopName" ;
    public static final String RESULTS_STATUS = "status" ;

    public static String createRequestJson(int cityId, List<WifiDetail> wifiDetailList) {
        String userid = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_USER_ID, null);
        String accessKey = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_KEY, null);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PARAMS_USER_ID, userid);
            jsonObject.put(PARAMS_ACCESS_KEY, accessKey);
            jsonObject.put(PARAMS_CITY_ID, cityId);
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < wifiDetailList.size(); i++) {
                WifiDetail wifiDetail = wifiDetailList.get(i);
                JSONObject object = new JSONObject();
                object.put(PARAMS_BSSID, wifiDetail.bssid);
                object.put(PARAMS_SSID, wifiDetail.ssid);
                object.put(PARAMS_PASSWD, wifiDetail.passwd);
                object.put(PARAMS_LEVEL, wifiDetail.level);
                object.put(PARAMS_FREQUENCY, wifiDetail.frequency);
                object.put(PARAMS_SECURITY, wifiDetail.security);
                object.put(PARAMS_LONGITUDE, wifiDetail.lng);
                object.put(PARAMS_LATITUDE, wifiDetail.lat);
                jsonArray.put(object);
            }
            jsonObject.put(PARAMS_AP_LIST, jsonArray);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Results parseResponseJson(String response) {
        Results results = new Results();
        try {
            JSONObject jsonObject = new JSONObject(response);
            results.code = jsonObject.optInt(RESULTS_CODE);
            results.msg = jsonObject.optString(RESULTS_MSG);
            JSONArray jsonArray = jsonObject.optJSONArray(RESULTS_DATA);
            List<Data> dataList = new ArrayList<Data>();
            for (int i = 0; i < jsonArray.length(); i++) {
                Data data = new Data();
                JSONObject object = jsonArray.optJSONObject(i);
                data.apId = object.optInt(RESULTS_AP_ID);
                data.ssid = object.optString(RESULTS_SSID);
                data.bssid = object.optString(RESULTS_BSSID);
                data.security = object.optInt(RESULTS_SECURITY);
                data.level = object.optInt(RESULTS_LEVEL);
                data.frequency = object.optInt(RESULTS_FREQUENCY);
                data.place = object.optString(RESULTS_PLACE);
                data.shopId = object.optInt(RESULTS_SHOP_ID);
                data.shopName = object.optString(RESULTS_SHOP_NAME);
                data.status = object.optInt(RESULTS_STATUS);
                dataList.add(data);
            }
            results.data = dataList;
            return results;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class WifiDetail {
        public String ssid;
        public String bssid;
        public String passwd;
        public int level;
        public int frequency;
        public int security;
        public double lng;
        public double lat;
    }

    public static class Results {
        public int code;
        public String msg;
        public List<Data> data;
    }

    public static class Data {
        public int apId;
        public String ssid;
        public String bssid;
        public int security;
        public int level;
        public int frequency;
        public String place;
        public int shopId;
        public String shopName;
        public int status;
    }
}
