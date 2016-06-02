package com.sharedream.wifiguard.cmd;

import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.utils.GlobalField;

import org.json.JSONException;
import org.json.JSONObject;

public class CmdBindingWifi {
    public static final String PARAMS_USER_ID = "userid";
    public static final String PARAMS_ACCESS_KEY = "accessKey";
    public static final String PARAMS_SHOP_ID = "shopId";
    public static final String PARAMS_BSSID = "bssid";
    public static final String PARAMS_SSID = "ssid";
    public static final String PARAMS_PASSWD = "passwd";
    public static final String PARAMS_CITY_ID = "cityId";
    public static final String PARAMS_LEVEL = "level";
    public static final String PARAMS_FREQUENCY = "frequency";
    public static final String PARAMS_SECURITY = "security";
    public static final String PARAMS_PLACE = "place";
    public static final String PARAMS_LNG = "lng";
    public static final String PARAMS_LAT = "lat";

    public static final String RESULTS_CODE = "code";
    public static final String RESULTS_MSG = "msg";
    public static final String RESULTS_DATA = "data";
    public static final String RESULTS_DATA_AP_ID = "apId";

    public static String createRequestJson(int ShopId, String bssid, String ssid, String pwd, int cityId, int level, int frequency, int security, String place, double lng, double lat) {
        String userid = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_USER_ID, null);
        String accessKey = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_KEY, null);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PARAMS_USER_ID, userid);
            jsonObject.put(PARAMS_ACCESS_KEY, accessKey);
            jsonObject.put(PARAMS_SHOP_ID, ShopId);
            jsonObject.put(PARAMS_BSSID, bssid);
            jsonObject.put(PARAMS_SSID, ssid);
            jsonObject.put(PARAMS_PASSWD, pwd);
            jsonObject.put(PARAMS_CITY_ID, cityId);
            jsonObject.put(PARAMS_LEVEL, level);
            jsonObject.put(PARAMS_FREQUENCY, frequency);
            jsonObject.put(PARAMS_SECURITY, security);
            jsonObject.put(PARAMS_PLACE, place);
            jsonObject.put(PARAMS_LNG, lng);
            jsonObject.put(PARAMS_LAT, lat);
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
            JSONObject obj = jsonObject.optJSONObject(RESULTS_DATA);
            Data data = new Data();
            if (obj != null) {
                data.apId = obj.optInt(RESULTS_DATA_AP_ID);
                results.data = data;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return results;
    }

    public static class Results {
        public int code;
        public String msg;
        public Data data;
    }

    public static class Data {
        public int apId;
    }

}
