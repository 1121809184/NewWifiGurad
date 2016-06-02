package com.sharedream.wifiguard.utils;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader;

/**
 * Created by young on 2016/3/10.
 */
public class BitmapCache implements ImageLoader.ImageCache {
    private LruCache<String, Bitmap> mCache;

    public BitmapCache() {
        // 获取到可用内存的最大值，使用内存超出这个值会引起OutOfMemory异常
        // 使用最大可用内存值的1/8作为缓存的大小。
        int maxsize = (int) (Runtime.getRuntime().maxMemory() / 1024) / 8;
        mCache = new LruCache<String, Bitmap>(maxsize) {
            // 重写此方法来衡量每张图片的大小，默认返回图片数量
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return super.sizeOf(key, value);
            }
        };
    }

    /**
     * 获取缓存中图片
     */
    @Override
    public Bitmap getBitmap(String key) {
        return mCache.get(key);
    }

    /**
     * 存入缓存
     */
    @Override
    public void putBitmap(String key, Bitmap bitmap) {
        if (bitmap != null) mCache.put(key, bitmap);
    }
}
