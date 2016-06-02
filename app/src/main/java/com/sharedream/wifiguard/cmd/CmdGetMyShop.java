package com.sharedream.wifiguard.cmd;

import android.os.Parcel;
import android.os.Parcelable;

import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.utils.GlobalField;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CmdGetMyShop {
    public static final String PARAMS_USER_ID = "userid";
    public static final String PARAMS_ACCESS_KEY = "accessKey";

    public static final String RESULTS_CODE = "code";
    public static final String RESULTS_MSG = "msg";
    public static final String RESULTS_DATA = "data";
    public static final String RESULTS_DATA_TOTAL_AP = "totalAp";
    public static final String RESULTS_DATA_SHOP_LIST = "shopList";
    public static final String RESULTS_DATA_SHOP_ID = "shopId";
    public static final String RESULTS_DATA_SHOP_NAME = "shopName";
    public static final String RESULTS_DATA_SHOP_ADDRESS = "address";
    public static final String RESULTS_DATA_SHOP_CATEGORY = "category";
    public static final String RESULTS_DATA_SHOP_PHONE = "phone";
    public static final String RESULTS_DATA_CITY_ID = "cityId";
    public static final String RESULTS_DATA_AP_LIST = "apList";
    public static final String RESULTS_DATA_AP_ID = "apId";
    public static final String RESULTS_DATA_AP_SSID = "ssid";
    public static final String RESULTS_DATA_AP_BSSID = "bssid";
    public static final String RESULTS_DATA_AP_PLACE = "place";

    public static String createRequestJson() {
        String userid = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_USER_ID, null);
        String accessKey = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_KEY, null);
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
            JSONObject obj = jsonObject.optJSONObject(RESULTS_DATA);
            Data data = new Data();
            if (obj != null) {
                data.totalAp = obj.optInt(RESULTS_DATA_TOTAL_AP);
                JSONArray jsonArray = obj.optJSONArray(RESULTS_DATA_SHOP_LIST);
                List<MyShop> shopList = new ArrayList<MyShop>();
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        MyShop myShop = new MyShop();
                        JSONObject shopJsonObj = jsonArray.optJSONObject(i);
                        myShop.shopId = shopJsonObj.optInt(RESULTS_DATA_SHOP_ID);
                        myShop.shopName = shopJsonObj.optString(RESULTS_DATA_SHOP_NAME);
                        myShop.address = shopJsonObj.optString(RESULTS_DATA_SHOP_ADDRESS);
                        myShop.category = shopJsonObj.optInt(RESULTS_DATA_SHOP_CATEGORY);
                        myShop.phone = shopJsonObj.optString(RESULTS_DATA_SHOP_PHONE);
                        myShop.cityId = shopJsonObj.optInt(RESULTS_DATA_CITY_ID);
                        JSONArray array = shopJsonObj.optJSONArray(RESULTS_DATA_AP_LIST);
                        if (array != null) {
                            List<MyAp> apList = new ArrayList<MyAp>();
                            for (int j = 0; j < array.length(); j++) {
                                MyAp myAp = new MyAp();
                                JSONObject apJsonObj = array.optJSONObject(j);
                                myAp.apId = apJsonObj.optInt(RESULTS_DATA_AP_ID);
                                myAp.ssid = apJsonObj.optString(RESULTS_DATA_AP_SSID);
                                myAp.bssid = apJsonObj.optString(RESULTS_DATA_AP_BSSID);
                                myAp.place = apJsonObj.optString(RESULTS_DATA_AP_PLACE);
                                apList.add(myAp);
                            }
                            myShop.apList = apList;
                        }
                        shopList.add(myShop);
                    }
                    results.data = data;
                }
                data.shopList = shopList;
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
        public Data data;
    }

    public static class Data {
        public int totalAp;
        public List<MyShop> shopList;
    }

    public static class MyShop implements Parcelable{
        public int shopId;
        public String shopName;
        public String address;
        public int category;
        public String phone;
        public int cityId;
        public List<MyAp> apList;

        public MyShop(){}

        protected MyShop(Parcel in) {
            shopId = in.readInt();
            shopName = in.readString();
            address = in.readString();
            category = in.readInt();
            phone = in.readString();
            cityId = in.readInt();
        }

        public static final Creator<MyShop> CREATOR = new Creator<MyShop>() {
            @Override
            public MyShop createFromParcel(Parcel in) {
                return new MyShop(in);
            }

            @Override
            public MyShop[] newArray(int size) {
                return new MyShop[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(shopId);
            dest.writeString(shopName);
            dest.writeString(address);
            dest.writeInt(category);
            dest.writeString(phone);
            dest.writeInt(cityId);
        }
    }

    public static class MyAp implements Parcelable{
        public int apId;
        public String ssid;
        public String bssid;
        public String place;

        public MyAp(){}

        protected MyAp(Parcel in) {
            apId = in.readInt();
            ssid = in.readString();
            bssid = in.readString();
            place = in.readString();
        }

        public static final Creator<MyAp> CREATOR = new Creator<MyAp>() {
            @Override
            public MyAp createFromParcel(Parcel in) {
                return new MyAp(in);
            }

            @Override
            public MyAp[] newArray(int size) {
                return new MyAp[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(apId);
            dest.writeString(ssid);
            dest.writeString(bssid);
            dest.writeString(place);
        }
    }
}
