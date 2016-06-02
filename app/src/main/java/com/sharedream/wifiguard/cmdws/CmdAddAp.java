package com.sharedream.wifiguard.cmdws;

import org.json.JSONException;
import org.json.JSONObject;

public class CmdAddAp {
    public static final String PARAMS_ACCESS_TOKEN = "accessToken";
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

    public static String createRequestJson(String accessToken, int shopId, String bssid, String ssid, String pwd, int cityId, int level, int frequency, int security, String place, double lng, double lat) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PARAMS_ACCESS_TOKEN, accessToken);
            jsonObject.put(PARAMS_SHOP_ID, shopId);
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public static Results parseResponseJson(String response) {
        Results results = new Results();
        try {
            JSONObject jsonObject = new JSONObject(response);
            results.code = jsonObject.optInt(RESULTS_CODE);
            results.msg = jsonObject.optString(RESULTS_MSG);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return results;
    }

    public static class Results {
        public int code;
        public String msg;
    }
}
