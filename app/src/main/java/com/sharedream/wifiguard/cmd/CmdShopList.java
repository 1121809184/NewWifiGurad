package com.sharedream.wifiguard.cmd;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CmdShopList {
    //请求json字段
    public static final String PARAMS_USER_ID = "userid";
    public static final String PARAMS_ACCESS_KEY = "accessKey";
    public static final String PARAMS_CITY_ID = "cityId";
    public static final String PARAMS_LNG = "lng";
    public static final String PARAMS_LAT = "lat";

    //响应json字段
    public static final String RESULTS_CODE = "code";
    public static final String RESULTS_MSG = "msg";
    public static final String RESULTS_DATA = "data";
    public static final String RESULTS_DATA_TOTAL = "total";
    public static final String RESULTS_DATA_SHOP_LIST = "shopList";
    public static final String RESULTS_DATA_SHOP_LIST_IS_COLLECTED = "isCollected";
    public static final String RESULTS_DATA_SHOP_LIST_SHOP_ID = "shopId";
    public static final String RESULTS_DATA_SHOP_LIST_SHOP_NAME = "shopName";
    public static final String RESULTS_DATA_SHOP_LIST_ADDRESS = "address";
    public static final String RESULTS_DATA_SHOP_LIST_CATEGORY = "category";
    public static final String RESULTS_DATA_SHOP_LIST_LOCATION = "location";
    public static final String RESULTS_DATA_SHOP_LIST_PHONE = "phone";

    public static String createRequestJson(String userId, String accessKey, String cityId, double longitude, double latitude) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PARAMS_USER_ID, userId);
            jsonObject.put(PARAMS_ACCESS_KEY, accessKey);
            jsonObject.put(PARAMS_CITY_ID, cityId);
            jsonObject.put(PARAMS_LNG, longitude);
            jsonObject.put(PARAMS_LAT, latitude);
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
            JSONObject dataJsonObject = jsonObject.optJSONObject(RESULTS_DATA);
            if (dataJsonObject != null) {
                Data data = new Data();
                data.total = dataJsonObject.optInt(RESULTS_DATA_TOTAL);
                JSONArray jsonArray = dataJsonObject.optJSONArray(RESULTS_DATA_SHOP_LIST);
                List<Shop> shopList = new ArrayList<Shop>();
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        Shop shop = new Shop();
                        JSONObject obj = jsonArray.optJSONObject(i);
                        shop.isCollected = obj.optInt(RESULTS_DATA_SHOP_LIST_IS_COLLECTED);
                        shop.shopId = obj.optInt(RESULTS_DATA_SHOP_LIST_SHOP_ID);
                        shop.shopName = obj.optString(RESULTS_DATA_SHOP_LIST_SHOP_NAME);
                        shop.address = obj.optString(RESULTS_DATA_SHOP_LIST_ADDRESS);
                        shop.category = obj.optInt(RESULTS_DATA_SHOP_LIST_CATEGORY);
                        shop.location = obj.optString(RESULTS_DATA_SHOP_LIST_LOCATION);
                        shop.phone = obj.optString(RESULTS_DATA_SHOP_LIST_PHONE);
                        shopList.add(shop);
                    }
                    data.shopList = shopList;
                    results.data = data;
                    return results;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class Results {
        public int code;
        public String msg;
        public Data data;
    }

    public static class Data {
        public int total;
        public List<Shop> shopList;
    }

    public static class Shop implements Parcelable{
        public int isCollected;
        public int shopId;
        public String shopName;
        public String address;
        public int category;
        public String location;
        public String phone;

        public Shop(){

        }

        protected Shop(Parcel in) {
            isCollected = in.readInt();
            shopId = in.readInt();
            shopName = in.readString();
            address = in.readString();
            category = in.readInt();
            location = in.readString();
            phone = in.readString();
        }

        public static final Creator<Shop> CREATOR = new Creator<Shop>() {
            @Override
            public Shop createFromParcel(Parcel in) {
                return new Shop(in);
            }

            @Override
            public Shop[] newArray(int size) {
                return new Shop[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(isCollected);
            dest.writeInt(shopId);
            dest.writeString(shopName);
            dest.writeString(address);
            dest.writeInt(category);
            dest.writeString(location);
            dest.writeString(phone);
        }
    }
}
