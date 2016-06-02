package com.sharedream.wifiguard.cmdws;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CmdGetMyShop {
    public static final String PARAMS_ACCESS_TOKEN = "accessToken";
    public static final String RESULTS_CODE = "code";
    public static final String RESULTS_MSG = "msg";
    public static final String RESULTS_DATA = "data";
    public static final String RESULTS_DATA_TOTAL_AP = "totalAp";
    public static final String RESULTS_DATA_LIST = "list";
    public static final String RESULTS_DATA_SHOP_ADDRESS = "address";
    public static final String RESULTS_DATA_SHOP_CATEGORY = "category";
    public static final String RESULTS_DATA_SHOP_CITY_ID = "cityId";
    public static final String RESULTS_DATA_SHOP_DATE = "date";
    public static final String RESULTS_DATA_SHOP_LAT = "lat";
    public static final String RESULTS_DATA_SHOP_LNG = "lng";
    public static final String RESULTS_DATA_SHOP_NAME = "name";
    public static final String RESULTS_DATA_SHOP_OWNER = "owner";
    public static final String RESULTS_DATA_SHOP_PHONE1 = "phone1";
    public static final String RESULTS_DATA_SHOP_PHONE2 = "phone2";
    public static final String RESULTS_DATA_LOGO_SRC = "logoSrc";
    public static final String RESULTS_DATA_SHOP_ID = "shopId";
    public static final String RESULTS_DATA_AP_LIST = "apList";
    public static final String RESULTS_DATA_AP_SSID = "ssid";
    public static final String RESULTS_DATA_AP_BSSID = "bssid";
    public static final String RESULTS_DATA_AP_ID = "apId";
    public static final String RESULTS_DATA_AP_PLACE = "place";

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
            JSONObject object = jsonObject.optJSONObject(RESULTS_DATA);
            Data data = new Data();
            if (object != null) {
                data.totalAp = object.optInt(RESULTS_DATA_TOTAL_AP);
                JSONArray jsonArray = object.optJSONArray(RESULTS_DATA_LIST);
                ArrayList<Shop> list = new ArrayList<Shop>();
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        Shop shop = new Shop();
                        JSONObject shopJsonObj = jsonArray.optJSONObject(i);
                        shop.address = shopJsonObj.optString(RESULTS_DATA_SHOP_ADDRESS);
                        shop.category = shopJsonObj.optInt(RESULTS_DATA_SHOP_CATEGORY);
                        shop.cityId = shopJsonObj.optInt(RESULTS_DATA_SHOP_CITY_ID);
                        shop.date = shopJsonObj.optString(RESULTS_DATA_SHOP_DATE);
                        shop.lat = shopJsonObj.optDouble(RESULTS_DATA_SHOP_LAT);
                        shop.lng = shopJsonObj.optDouble(RESULTS_DATA_SHOP_LNG);
                        shop.name = shopJsonObj.optString(RESULTS_DATA_SHOP_NAME);
                        shop.owner = shopJsonObj.optString(RESULTS_DATA_SHOP_OWNER);
                        shop.phone1 = shopJsonObj.optString(RESULTS_DATA_SHOP_PHONE1);
                        shop.phone2 = shopJsonObj.optString(RESULTS_DATA_SHOP_PHONE2);
                        shop.logoSrc = shopJsonObj.optString(RESULTS_DATA_LOGO_SRC);
                        shop.shopId = shopJsonObj.optInt(RESULTS_DATA_SHOP_ID);
                        JSONArray array = shopJsonObj.optJSONArray(RESULTS_DATA_AP_LIST);
                        ArrayList<Ap> apList = new ArrayList<Ap>();
                        if (array != null) {
                            for (int k = 0; k < array.length(); k++) {
                                Ap ap = new Ap();
                                JSONObject apJsonObj = array.optJSONObject(k);
                                ap.apId = apJsonObj.optInt(RESULTS_DATA_AP_ID);
                                ap.ssid = apJsonObj.optString(RESULTS_DATA_AP_SSID);
                                ap.bssid = apJsonObj.optString(RESULTS_DATA_AP_BSSID);
                                ap.place = apJsonObj.optString(RESULTS_DATA_AP_PLACE);
                                apList.add(ap);
                            }
                            shop.apList = apList;
                        }
                        list.add(shop);
                    }
                    data.list = list;
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
        public Data data;
    }

    public static class Data {
        public int totalAp;
        public ArrayList<Shop> list;
    }

    public static class Shop implements Parcelable {
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
        public int shopId;
        public String phone1;
        public String phone2;
        public String logoSrc;
        public String name;
        public String owner;
        public double lng;
        public double lat;
        public String date;
        public int cityId;
        public int category;
        public String address;
        public ArrayList<Ap> apList;

        protected Shop(Parcel in) {
            shopId = in.readInt();
            phone1 = in.readString();
            phone2 = in.readString();
            logoSrc = in.readString();
            name = in.readString();
            owner = in.readString();
            lng = in.readDouble();
            lat = in.readDouble();
            date = in.readString();
            cityId = in.readInt();
            category = in.readInt();
            address = in.readString();
        }

        public Shop() {

        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(shopId);
            dest.writeString(phone1);
            dest.writeString(phone2);
            dest.writeString(logoSrc);
            dest.writeString(name);
            dest.writeString(owner);
            dest.writeDouble(lng);
            dest.writeDouble(lat);
            dest.writeString(date);
            dest.writeInt(cityId);
            dest.writeInt(category);
            dest.writeString(address);
        }
    }

    public static class Ap implements Parcelable {
        public static final Creator<Ap> CREATOR = new Creator<Ap>() {
            @Override
            public Ap createFromParcel(Parcel in) {
                return new Ap(in);
            }

            @Override
            public Ap[] newArray(int size) {
                return new Ap[size];
            }
        };
        public int apId;
        public String ssid;
        public String bssid;
        public String place;
        public int status;
        public int level;

        protected Ap(Parcel in) {
            apId = in.readInt();
            ssid = in.readString();
            bssid = in.readString();
            place = in.readString();
        }

        public Ap() {

        }

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
