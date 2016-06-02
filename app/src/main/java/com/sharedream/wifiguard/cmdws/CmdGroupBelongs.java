package com.sharedream.wifiguard.cmdws;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CmdGroupBelongs {
    public static final String PARAMS_ACCESS_TOKEN = "accessToken";

    public static final String RESULTS_CODE = "code";
    public static final String RESULTS_MSG = "msg";
    public static final String RESULTS_DATA = "data";
    public static final String RESULTS_DATA_ID = "id";
    public static final String RESULTS_DATA_NAME = "name";

    public static String createRequestJson(String accessToken) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PARAMS_ACCESS_TOKEN, accessToken);
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
            JSONArray jsonArray = jsonObject.optJSONArray(RESULTS_DATA);
            if (jsonArray != null) {
                ArrayList<GroupInfo> data = new ArrayList<GroupInfo>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObj = jsonArray.optJSONObject(i);
                    GroupInfo groupInfo = new GroupInfo();
                    groupInfo.id = jsonObj.optInt(RESULTS_DATA_ID);
                    groupInfo.name = jsonObj.optString(RESULTS_DATA_NAME);
                    data.add(groupInfo);
                }
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
        public ArrayList<GroupInfo> data;
    }

    public static class GroupInfo {
        public int id;
        public String name;
    }
}
