package com.sharedream.wifiguard.cmd;

import android.text.TextUtils;

import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.utils.MyUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class CmdAccessKey {
    //请求json字段
    public static final String PARAMS_TOKEN = "token";
    public static final String PARAMS_UUID = "uuid";
    public static final String PARAMS_USERID = "userid";

    //响应json字段
    public static final String RESULTS_CODE = "code";
    public static final String RESULTS_MSG = "msg";
    public static final String RESULTS_DATA = "data";
    public static final String RESULTS_DATA_ACCESS_KEY = "accessKey";

    public static String createRequestJson(String userid) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PARAMS_TOKEN, Constant.TOKEN);
            jsonObject.put(PARAMS_UUID, MyUtils.getDeviceUUID(AppContext.getContext()));
            jsonObject.put(PARAMS_USERID, userid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public static Results parseResponseJson(String response) {
        if (TextUtils.isEmpty(response)) {
            return null;
        }
        Results results = new Results();
        try {
            JSONObject jsonObject = new JSONObject(response);
            results.code = jsonObject.optInt(RESULTS_CODE);
            results.msg = jsonObject.optString(RESULTS_MSG);
            JSONObject dataJsonObject = jsonObject.optJSONObject(RESULTS_DATA);
            if (dataJsonObject != null) {
                Data data = new Data();
                data.accessKey = dataJsonObject.optString(RESULTS_DATA_ACCESS_KEY);
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
        public String accessKey;
    }
}
