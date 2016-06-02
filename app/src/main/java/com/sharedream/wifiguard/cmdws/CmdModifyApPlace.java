package com.sharedream.wifiguard.cmdws;

import org.json.JSONException;
import org.json.JSONObject;

public class CmdModifyApPlace {
    public static final String PARAMS_ACCESS_TOKEN = "accessToken";
    public static final String PARAMS_AP_ID = "apId";
    public static final String PARAMS_PLACE = "place";

    public static final String RESULTS_CODE = "code";
    public static final String RESULTS_MSG = "msg";

    public static String createRequestJson(String accessToken, int apId, String place) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PARAMS_ACCESS_TOKEN, accessToken);
            jsonObject.put(PARAMS_AP_ID, apId);
            jsonObject.put(PARAMS_AP_ID, apId);
            jsonObject.put(PARAMS_PLACE, place);
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
