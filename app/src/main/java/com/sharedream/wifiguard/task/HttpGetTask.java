package com.sharedream.wifiguard.task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by young on 2016/3/15.
 */
public class HttpGetTask extends AsyncTask<String, Integer, String> {
    private Context context;
    private String url;
    private InputStream stream;
    private int currentByte = 0;
    private int fileLength = 0;
    private byte[] b = null;
    private long startTime = 0;
    private long intervalTime = 0;
    private int bytecount = 0;
    public static DefaultHttpClient httpClient;
    private FileOutputStream fos;


    public HttpGetTask(Context context, String url) {
        this.context = context;
        this.url = url;
    }

    @Override
    protected String doInBackground(String... params) {
        httpClient = new DefaultHttpClient();
        HttpParams httpParams = httpClient.getParams();
        httpParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 60000);
        httpParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, 60000);
        httpParams.setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
        httpParams.setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET, "UTF-8");
        HttpGet httpGet = new HttpGet(url);
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            stream = entity.getContent();
            startTime = System.currentTimeMillis();
            File file = new File(Environment.getExternalStorageDirectory().toString(), "qq");
            fos = new FileOutputStream(file);
            byte[] buff = new byte[1024 * 4];
            while(stream.read(buff) != -1){
                fos.write(buff,0,buff.length);
            }
        } catch (Exception e) {
            Log.e("exception : ", e.getMessage() + "");
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                Log.e("exception : ", e.getMessage());
            }


//     //   return b;
        }

        return null;
    }

    public static HttpClient getHttpClient() {
        return httpClient;
    }
}
