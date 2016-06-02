package com.sharedream.wifiguard.version;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;


public class FileDownloadService extends Service {

    private FileDownloadTask downloadTask;
    private List<String> listDownloadUrl;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        listDownloadUrl = new ArrayList<String>();
        LogUtils.d("Service被启动");
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle =  intent.getExtras();
        if (bundle != null) {
            String title = bundle.getString(Constant.BUNDLE_KEY_DOWNLOAD_TITLE);
            String folderPath = bundle.getString(Constant.BUNDLE_KEY_FOLDER_PATH);
            String filename = bundle.getString(Constant.BUNDLE_KEY_FILENAME);
            String url = bundle.getString(Constant.BUNDLE_KEY_DOWNLOAD_URL);
            LogUtils.d("Service url >>> " + url);
            if (!listDownloadUrl.contains(url)) {
                listDownloadUrl.add(url);
                downloadTask = new FileDownloadTask(title, folderPath, filename, this.getApplicationContext(), this);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    downloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
                } else {
                    downloadTask.execute(url);
                }
            }
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (downloadTask != null) {
            downloadTask.cancel(true);
        }
    }

    public void finishTask(String taskUrl) {
        listDownloadUrl.remove(taskUrl);
    }

    public boolean hasFinishAllTask() {
        return listDownloadUrl.size() == 0;
    }

}
