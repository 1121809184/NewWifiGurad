package com.sharedream.wifiguard.cmd;

import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.utils.GlobalField;

import org.json.JSONException;
import org.json.JSONObject;

public class CmdAddShop {
    public static final String PARAMS_USER_ID = "userid";
    public static final String PARAMS_ACCESS_KEY = "accessKey";
    public static final String PARAMS_SHOP_NAME = "shopName";
    public static final String PARAMS_CITY_ID = "cityId";
    public static final String PARAMS_ADDRESS = "address";
    public static final String PARAMS_PHONE1 = "phone1";
    public static final String PARAMS_LONGITUDE = "lng";
    public static final String PARAMS_LATITUDE = "lat";
    public static final String PARAMS_CATEGORY = "category";

    public static final String RESULTS_CODE = "code";
    public static final String RESULTS_MSG = "msg";
    public static final String RESULTS_DATA = "data";
    public static final String RESULTS_DATA_SHOP_ID = "shopId";

    public static String createRequestJson(String shopName, int cityId, String address, String phone, double longitude, double latitude, int category) {
        String userid = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_USER_ID, null);
        String accessKey = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_KEY, null);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PARAMS_USER_ID, userid);
            jsonObject.put(PARAMS_ACCESS_KEY, accessKey);
            jsonObject.put(PARAMS_SHOP_NAME, shopName);
            jsonObject.put(PARAMS_CITY_ID, cityId);
            jsonObject.put(PARAMS_ADDRESS, address);
            jsonObject.put(PARAMS_PHONE1, phone);
            jsonObject.put(PARAMS_LONGITUDE, longitude);
            jsonObject.put(PARAMS_LATITUDE, latitude);
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
            JSONObject object = jsonObject.optJSONObject(RESULTS_DATA);
            if (object != null) {
                Data data = new Data();
                data.shopId = object.optInt(RESULTS_DATA_SHOP_ID);
                results.data = data;
            }
            return results;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class Results {
        public int code;
        public String msg;
        public Data data;
    }

    public static class Data {
        public int shopId;
    }
}
