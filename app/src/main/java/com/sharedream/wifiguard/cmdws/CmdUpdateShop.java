package com.sharedream.wifiguard.cmdws;

import org.json.JSONException;
import org.json.JSONObject;

public class CmdUpdateShop {
    public static final String PARAMS_ACCESS_TOKEN = "accessToken";
    public static final String PARAMS_SHOP_ID = "shopId";
    public static final String PARAMS_SHOP_NAME = "shopName";
    public static final String PARAMS_ADDRESS = "address";
    public static final String PARAMS_LNG = "lng";
    public static final String PARAMS_LAT = "lat";
    public static final String PARAMS_PHONE1 = "phone1";
    public static final String PARAMS_PHONE2 = "phone2";
    public static final String PARAMS_CATEGORY = "category";
    public static final String PARAMS_CITY_ID = "cityId";
    public static final String PARAMS_OWNER = "owner";
    public static final String PARAMS_LOGO_SRC = "logoSrc";
    public static final String PARAMS_COMPANY = "company";

    public static final String RESULTS_CODE = "code";
    public static final String RESULTS_MSG = "msg";

    public static String createRequestJson(String accessToken, int shopId, String shopName, String address, double lng, double lat, String phone1, String phone2, int category, int cityId, String owner, String groupId, String logoSrc) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PARAMS_ACCESS_TOKEN, accessToken);
            jsonObject.put(PARAMS_SHOP_ID, shopId);
            jsonObject.put(PARAMS_SHOP_NAME, shopName);
            jsonObject.put(PARAMS_ADDRESS, address);
            jsonObject.put(PARAMS_LNG, lng);
            jsonObject.put(PARAMS_LAT, lat);
            jsonObject.put(PARAMS_PHONE1, phone1);
            jsonObject.put(PARAMS_PHONE2, phone2);
            jsonObject.put(PARAMS_CATEGORY, category);
            jsonObject.put(PARAMS_CITY_ID, cityId);
            jsonObject.put(PARAMS_OWNER, owner);
            jsonObject.put(PARAMS_COMPANY, groupId);
            jsonObject.put(PARAMS_LOGO_SRC, logoSrc);
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
