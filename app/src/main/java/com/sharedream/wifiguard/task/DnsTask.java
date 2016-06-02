package com.sharedream.wifiguard.task;

import android.content.Context;
import android.os.AsyncTask;

import com.sharedream.wifiguard.listener.DnsSubject;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

/**
 * Created by young on 2016/3/15.
 */
public class DnsTask extends AsyncTask<String, Integer, String> {
    private Context context;
    public static boolean isSuccess1;
    public static boolean isSuccess2;

    public DnsTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... params) {
        DefaultHttpClient httpClient1 = new DefaultHttpClient();
        HttpParams httpParams1 = httpClient1.getParams();
        httpParams1.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 3000);
        httpParams1.setParameter(CoreConnectionPNames.SO_TIMEOUT, 3000);
        httpParams1.setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
        httpParams1.setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET, "UTF-8");
        HttpGet httpGet1 = new HttpGet("http://www.baidu.com");
        try {
            //第三步：执行请求，获取服务器发还的相应对象
            HttpResponse httpResponse1 = httpClient1.execute(httpGet1);
            //第四步：检查相应的状态是否正常：检查状态码的值是200表示正常
            if (httpResponse1.getStatusLine().getStatusCode() == 200) {
                isSuccess1 = true;
                DefaultHttpClient httpClient2 = new DefaultHttpClient();
                HttpParams httpParams2 = httpClient2.getParams();
                httpParams2.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 3000);
                httpParams2.setParameter(CoreConnectionPNames.SO_TIMEOUT, 3000);
                httpParams2.setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
                httpParams2.setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET, "UTF-8");
                HttpGet httpGet2 = new HttpGet("http://14.215.177.37");
                HttpResponse httpResponse2 = httpClient2.execute(httpGet2);
                if (httpResponse2.getStatusLine().getStatusCode() == 200) {
                    isSuccess2 = true;
                } else {
                    isSuccess2 = false;
                }
            } else {
                isSuccess1 = false;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        DnsSubject.getInstance().notifyDns(isSuccess1,isSuccess2);
    }
}
