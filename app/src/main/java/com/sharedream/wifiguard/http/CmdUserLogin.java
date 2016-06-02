package com.sharedream.wifiguard.http;


import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.utils.MyUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class CmdUserLogin {
    public static final String PARAMS_PHONE = "phone";
    public static final String PARAMS_PASSWD = "passwd";
    public static final String PARAMS_QQ_ID = "qqId";
    public static final String PARAMS_WEIBO_ID = "wbId";
    public static final String PARAMS_WECHAT_ID = "wxId";
    public static final String PARAMS_IP = "ip";
    public static final String PARAMS_IMEI = "imei";
    public static final String PARAMS_MAC = "mac";

    public static final String RESULTS_CODE = "code";
    public static final String RESULTS_MSG = "msg";
    public static final String RESULTS_DATA = "data";
    public static final String RESULTS_DATA_UID = "uid";
    public static final String RESULTS_DATA_PHONE = "phone";
    public static final String RESULTS_DATA_EMAIL = "email";
    public static final String RESULTS_DATA_ACCESS_TOKEN = "accessToken";

    public static String createRequestJson(String phone, String passwd, String qqId, String weiboId, String wechatId) {
        String imei = MyUtils.getImei(AppContext.getContext());
        String mac = MyUtils.getMacAddress(AppContext.getContext());
        String ip = MyUtils.getIpAddress(AppContext.getContext());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PARAMS_PHONE, phone);
            jsonObject.put(PARAMS_PASSWD, passwd);
            jsonObject.put(PARAMS_QQ_ID, qqId);
            jsonObject.put(PARAMS_WEIBO_ID, weiboId);
            jsonObject.put(PARAMS_WECHAT_ID, wechatId);
            jsonObject.put(PARAMS_IP, ip);
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
                data.uid = jsonObj.optString(RESULTS_DATA_UID);
                data.phone = jsonObj.optString(RESULTS_DATA_PHONE);
                data.email = jsonObj.optString(RESULTS_DATA_EMAIL);
                data.accessToken = jsonObj.optString(RESULTS_DATA_ACCESS_TOKEN);
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
        public String uid;
        public String phone;
        public String email;
        public String accessToken;
    }
}
