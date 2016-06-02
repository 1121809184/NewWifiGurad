package com.sharedream.wifiguard.http;

import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.utils.MyUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class CmdUserVerifyPasswd {
    public static final String PARAMS_ACCESS_TOKEN = "accessToken";
    public static final String PARAMS_OLD_PASSWD = "oldPasswd";
    public static final String PARAMS_NEW_PASSWD = "newPasswd";
    public static final String PARAMS_IMEI = "imei";
    public static final String PARAMS_MAC = "mac";

    public static final String RESULTS_CODE = "code";
    public static final String RESULTS_MSG = "msg";

    public static String createRequestJson(String accessToken, String oldPasswd, String newPasswd) {
        JSONObject jsonObject = new JSONObject();
        String imei = MyUtils.getImei(AppContext.getContext());
        String mac = MyUtils.getMacAddress(AppContext.getContext());
        try {
            jsonObject.put(PARAMS_ACCESS_TOKEN, accessToken);
            jsonObject.put(PARAMS_OLD_PASSWD, oldPasswd);
            jsonObject.put(PARAMS_NEW_PASSWD, newPasswd);
            jsonObject.put(PARAMS_IMEI, imei);
            jsonObject.put(PARAMS_MAC, mac);
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
