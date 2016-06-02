package com.sharedream.wifiguard.cmdws;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CmdShopCategory {
    public static final String PARAMS_ACCESS_TOKEN = "accessToken";

    public static final String RESULTS_CODE = "code";
    public static final String RESULTS_MSG = "msg";
    public static final String RESULTS_DATA = "data";
    public static final String RESULTS_DATA_ID = "id";
    public static final String RESULTS_DATA_NAME = "name";
    public static final String RESULTS_DATA_CHILDREN = "children";
    public static final String RESULTS_DATA_CHILDREN_ID = "id";
    public static final String RESULTS_DATA_CHILDREN_NAME = "name";
    public static final String RESULTS_DATA_CHILDREN_PID = "pid";

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
            ArrayList<BigCategory> data = new ArrayList<BigCategory>();
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    BigCategory bigCategory = new BigCategory();
                    JSONObject jsonObj = jsonArray.optJSONObject(i);
                    bigCategory.id = jsonObj.optInt(RESULTS_DATA_ID);
                    bigCategory.name = jsonObj.optString(RESULTS_DATA_NAME);
                    JSONArray array = jsonObj.optJSONArray(RESULTS_DATA_CHILDREN);
                    ArrayList<SmallCategory> children = new ArrayList<SmallCategory>();
                    if (array != null) {
                        for (int k = 0; k < array.length(); k++) {
                            SmallCategory smallCategory = new SmallCategory();
                            JSONObject obj = array.optJSONObject(k);
                            smallCategory.id = obj.optInt(RESULTS_DATA_CHILDREN_ID);
                            smallCategory.name = obj.optString(RESULTS_DATA_CHILDREN_NAME);
                            smallCategory.pid = obj.optInt(RESULTS_DATA_CHILDREN_PID);
                            children.add(smallCategory);
                        }
                    }
                    bigCategory.children = children;
                    data.add(bigCategory);
                }
            }
            results.data = data;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return results;
    }

    public static class Results {
        public int code;
        public String msg;
        public ArrayList<BigCategory> data;
    }

    public static class BigCategory {
        public int id;
        public String name;
        public ArrayList<SmallCategory> children;
    }

    public static class SmallCategory {
        public int id;
        public String name;
        public int pid;
    }
}
