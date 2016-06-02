package com.sharedream.wifiguard.cmdws;

import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.utils.MyUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class CmdAppVersion {
    public static final String PARAMS_VERSION = "version";
    public static final String PARAMS_OS = "os";

    public static final String RESULTS_CODE = "code";
    public static final String RESULTS_MSG = "msg";
    public static final String RESULTS_DATA = "data";
    public static final String RESULTS_DATA_VERSION = "version";
    public static final String RESULTS_DATA_LOGS = "logs";
    public static final String RESULTS_DATA_URL = "url";
    public static final String RESULTS_DATA_FORCE = "force";
    public static final String RESULTS_DATA_DATE = "date";

    public static String createRequestJson() {
        JSONObject jsonObject = new JSONObject();
        String version = MyUtils.getSysVersionName(AppContext.getContext());
        try {
            jsonObject.put(PARAMS_VERSION, version);
            jsonObject.put(PARAMS_OS, 1);
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
                data.version = jsonObj.optString(RESULTS_DATA_VERSION);
                data.logs = jsonObj.optString(RESULTS_DATA_LOGS);
                data.url = jsonObj.optString(RESULTS_DATA_URL);
                data.force = jsonObj.optInt(RESULTS_DATA_FORCE);
                data.date = jsonObj.optString(RESULTS_DATA_DATE);
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
        public String version;
        public String logs;
        public String url;
        public int force;
        public String date;
    }
}
