package com.sharedream.wifiguard.cmdws;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.utils.BitmapCache;

public class MyCmdUtil {

    private static RequestQueue requestQueue;
    private static ImageLoader mImageLoader;

    private static Gson gson = new Gson();

    public static String convertObject2Json(Object param) {
        String json = gson.toJson(param);
        return json;
    }

    public static <T> T convertJson2Object(String json, Class<T> clazz) {
        T result = null;
        try {
            result = gson.fromJson(json, clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static ImageLoader getImageLoader() {
        //创建RequestQueue，可发送异步请求
        requestQueue = getRequestQueue();
        //创建ImageLoader,用于将图片存入缓存和从缓存中取出图片
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(requestQueue, new BitmapCache());
        }
        return mImageLoader;
    }


    public static RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(AppContext.getContext());
        }
        return requestQueue;
    }

    public static void sendRandomTagRequest(String url, String json, MyCmdHttpTask.CmdListener listener) {
        new MyRandomTagCmdHttpTask().sendHttpRequest(url, json, listener);
    }
}
