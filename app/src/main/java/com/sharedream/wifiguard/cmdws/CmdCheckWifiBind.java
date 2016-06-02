package com.sharedream.wifiguard.cmdws;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CmdCheckWifiBind {
    public static final String PARAMS_ACCESS_TOKEN = "accessToken";
    public static final String PARAMS_CITY_ID = "cityId";
    public static final String PARAMS_AP_LIST = "apList";
    public static final String PARAMS_AP_BSSID = "bssid";
    public static final String PARAMS_AP_SSID = "ssid";
    public static final String PARAMS_AP_PASSWD = "passwd";
    public static final String PARAMS_AP_LEVEL = "level";
    public static final String PARAMS_AP_FREQUENCY = "frequency";
    public static final String PARAMS_AP_SECURITY = "security";
    public static final String PARAMS_AP_LNG = "lng";
    public static final String PARAMS_AP_LAT = "lat";

    public static final String RESULTS_CODE = "code";
    public static final String RESULTS_MSG = "msg";
    public static final String RESULTS_DATA = "data";
    public static final String RESULTS_AP_ID = "apId";
    public static final String RESULTS_SSID = "ssid";
    public static final String RESULTS_BSSID = "bssid";
    public static final String RESULTS_SECURITY = "security";
    public static final String RESULTS_LEVEL = "level";
    public static final String RESULTS_FREQUENCY = "frequency";
    public static final String RESULTS_STATUS = "status";
    public static final String RESULTS_LNG = "lng";
    public static final String RESULTS_LAT = "lat";
    public static final String RESULTS_SHOP_NAME = "shopName";

    public static String createRequestJson(String accessToken, int cityId, ArrayList<WifiDetail> wifiDetailList) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PARAMS_ACCESS_TOKEN, accessToken);
            jsonObject.put(PARAMS_CITY_ID, cityId);
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < wifiDetailList.size(); i++) {
                WifiDetail wifiDetail = wifiDetailList.get(i);
                JSONObject object = new JSONObject();
                object.put(PARAMS_AP_BSSID, wifiDetail.bssid);
                object.put(PARAMS_AP_SSID, wifiDetail.ssid);
                object.put(PARAMS_AP_SSID, wifiDetail.ssid);
                object.put(PARAMS_AP_PASSWD, wifiDetail.passwd);
                object.put(PARAMS_AP_LEVEL, wifiDetail.level);
                object.put(PARAMS_AP_FREQUENCY, wifiDetail.frequency);
                object.put(PARAMS_AP_SECURITY, wifiDetail.security);
                object.put(PARAMS_AP_LAT, wifiDetail.lat);
                object.put(PARAMS_AP_LNG, wifiDetail.lng);
                jsonArray.put(object);
            }
            jsonObject.put(PARAMS_AP_LIST, jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
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

    public static Results parseResponseJson(String response) {
        Results results = new Results();
        try {
            JSONObject jsonObject = new JSONObject(response);
            results.code = jsonObject.optInt(RESULTS_CODE);
            results.msg = jsonObject.optString(RESULTS_MSG);
            JSONArray jsonArray = jsonObject.optJSONArray(RESULTS_DATA);
            ArrayList<Data> dataList = new ArrayList<Data>();
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    Data data = new Data();
                    JSONObject jsonObj = jsonArray.optJSONObject(i);
                    data.ssid = jsonObj.optString(RESULTS_SSID);
                    data.bssid = jsonObj.optString(RESULTS_BSSID);
                    data.apId = jsonObj.optInt(RESULTS_AP_ID);
                    data.security = jsonObj.optInt(RESULTS_SECURITY);
                    data.level = jsonObj.optInt(RESULTS_LEVEL);
                    data.frequency = jsonObj.optInt(RESULTS_FREQUENCY);
                    data.status = jsonObj.optInt(RESULTS_STATUS);
                    data.lng = jsonObj.optDouble(RESULTS_LNG);
                    data.lat = jsonObj.optDouble(RESULTS_LAT);
                    data.shopName = jsonObj.optString(RESULTS_SHOP_NAME);
                    dataList.add(data);
                }
                results.data = dataList;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return results;
    }

    public static class Results {
        public int code;
        public String msg;
        public ArrayList<Data> data;
    }

    public static class Data {
        public String ssid;
        public String bssid;
        public int security;
        public int apId;
        public int level;
        public int frequency;
        public int status;
        public double lng;
        public double lat;
        public String shopName;
    }
}
