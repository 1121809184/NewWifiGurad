package com.sharedream.wifiguard.cmd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CmdShopCategory {
    public static final String PARAMS_USER_ID = "userid";
    public static final String PARAMS_ACCESS_KEY = "accessKey";

    public static final String RESULTS_CODE = "code";
    public static final String RESULTS_MSG = "msg";
    public static final String RESULTS_DATA = "data";
    public static final String RESULTS_DATA_ID = "id";
    public static final String RESULTS_DATA_NAME = "name";
    public static final String RESULTS_DATA_CHILDREN = "children";
    public static final String RESULTS_DATA_CHILDREN_ID = "id";
    public static final String RESULTS_DATA_CHILDREN_NAME = "name";

    public static String createRequestJson(String userid, String accessKey) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PARAMS_USER_ID, userid);
            jsonObject.put(PARAMS_ACCESS_KEY, accessKey);
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
                List<BigCategory> bigCategoryList = new ArrayList<BigCategory>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    BigCategory bigCategory = new BigCategory();
                    JSONObject obj = jsonArray.optJSONObject(i);
                    bigCategory.id = obj.optInt(RESULTS_DATA_ID);
                    bigCategory.name = obj.optString(RESULTS_DATA_NAME);
                    JSONArray childrenJsonArray = obj.optJSONArray(RESULTS_DATA_CHILDREN);
                    if (childrenJsonArray != null) {
                        List<SmallCategory> smallCategoryList = new ArrayList<SmallCategory>();
                        for (int j = 0; j < childrenJsonArray.length(); j++) {
                            SmallCategory smallCategory = new SmallCategory();
                            JSONObject childrenObj = childrenJsonArray.optJSONObject(j);
                            smallCategory.id = childrenObj.optInt(RESULTS_DATA_CHILDREN_ID);
                            smallCategory.name = childrenObj.optString(RESULTS_DATA_CHILDREN_NAME);
                            smallCategoryList.add(smallCategory);
                        }
                        bigCategory.children = smallCategoryList;
                        bigCategoryList.add(bigCategory);
                    }
                }
                results.data = bigCategoryList;
                return results;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class Results {
        public int code;
        public String msg;
        public List<BigCategory> data;
    }

    public static class BigCategory {
        public int id;
        public String name;
        public List<SmallCategory> children;
    }

    public static class SmallCategory {
        public int id;
        public String name;
    }
}
