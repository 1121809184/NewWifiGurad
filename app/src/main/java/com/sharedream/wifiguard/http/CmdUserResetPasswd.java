package com.sharedream.wifiguard.http;


import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.utils.MyUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class CmdUserResetPasswd {
    public static final String PARAMS_PHONE = "phone";
    public static final String PARAMS_PASSWD = "passwd";
    public static final String PARAMS_IMEI = "imei";
    public static final String PARAMS_MAC = "mac";

    public static final String RESULTS_CODE = "code";
    public static final String RESULTS_MSG = "msg";
    public static final String RESULTS_DATA = "data";
    public static final String RESULTS_DATA_NEW_PASSWD = "newPasswd";

    public static String createRequestJson(String phone,String passwd) {
        String imei = MyUtils.getImei(AppContext.getContext());
        String mac = MyUtils.getMacAddress(AppContext.getContext());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PARAMS_PHONE, phone);
            jsonObject.put(PARAMS_PASSWD, passwd);
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
            JSONObject jsonObj = jsonObject.optJSONObject(RESULTS_DATA);
            if (jsonObj != null) {
                Data data = new Data();
                data.newPasswd = jsonObj.optString(RESULTS_DATA_NEW_PASSWD);
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
        public String newPasswd;
    }
}
