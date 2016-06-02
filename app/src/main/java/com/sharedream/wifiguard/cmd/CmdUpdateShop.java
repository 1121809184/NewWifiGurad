package com.sharedream.wifiguard.cmd;

import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.utils.GlobalField;

import org.json.JSONException;
import org.json.JSONObject;

public class CmdUpdateShop {
    public static final String PARAMS_USER_ID = "userid";
    public static final String PARAMS_ACCESS_KEY = "accessKey";
    public static final String PARAMS_SHOP_ID = "shopId";
    public static final String PARAMS_SHOP_NAME = "shopName";
    public static final String PARAMS_CITY_ID = "cityId";
    public static final String PARAMS_ADDRESS = "address";
    public static final String PARAMS_PHONE = "phone";
    public static final String PARAMS_LNG = "lng";
    public static final String PARAMS_LAT = "lat";
    public static final String PARAMS_CATEGORY = "category";

    public static final String RESULTS_CODE = "code";
    public static final String RESULTS_MSG = "msg";

    public static String createRequestJson(int shopId, String shopName, int cityId, String address, String phone, double lng, double lat, int category) {
        String userid = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_USER_ID, null);
        String accessKey = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_KEY, null);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PARAMS_USER_ID, userid);
            jsonObject.put(PARAMS_ACCESS_KEY, accessKey);
            jsonObject.put(PARAMS_SHOP_ID, shopId);
            jsonObject.put(PARAMS_SHOP_NAME, shopName);
            jsonObject.put(PARAMS_CITY_ID, cityId);
            jsonObject.put(PARAMS_ADDRESS, address);
            jsonObject.put(PARAMS_PHONE, phone);
            jsonObject.put(PARAMS_LNG, lng);
            jsonObject.put(PARAMS_LAT, lat);
            jsonObject.put(PARAMS_CATEGORY, category);
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
