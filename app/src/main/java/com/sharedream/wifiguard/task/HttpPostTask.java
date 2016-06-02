package com.sharedream.wifiguard.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.sharedream.wifiguard.utils.MyUtils;

import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;


/**
 * Created by young on 2016/3/16.
 */
public class HttpPostTask extends AsyncTask<String, Integer, String> {
    public static DefaultHttpClient httpClient;
    private String url;
    private Context context;
    private String AccessKey = "VBvN33BnrqcaAqrbOHLD4EbTuIwxwjA_VSdUo6B5";
    private String SecretKey = "fdSgoq2c71UX8rJ8luX5zf3BsC03TmDmbOdKnr7e";
    public static HttpURLConnection conn;

    public HttpPostTask(Context context, String url) {
        this.context = context;
        this.url = url;
    }


    @Override
    protected String doInBackground(String... params) {
//        httpClient = new DefaultHttpClient();
//        HttpParams httpParams = httpClient.getParams();
//        httpParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 60000);
//        httpParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, 60000);
//        httpParams.setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
//        httpParams.setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
//        httpParams.setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET, "UTF-8");
//        HttpPost httpPost = new HttpPost(url);
//
//        httpPost.setHeader("Connection", "Keep-Alive");
//        httpPost.setHeader("Cache-Control", "no-cache");
//        httpPost.setHeader("Content-Type", "multipart/form-data" + ";boundary=" + UUID.randomUUID().toString());
//
//        File file = new File("file:///android_asset/wifi1.png");
//
//        FileEntity reqEntity = new FileEntity(file, "binary/octet-stream");
//
//        httpPost.setEntity(reqEntity);
//        reqEntity.setContentType("binary/octet-stream");


//        JSONObject jsonObject = new JSONObject();
//
//        try {
//            jsonObject.put("content", Contant.content);
//        } catch (JSONException e1) {
//            e1.printStackTrace();
//        }
//        String transJson = jsonObject.toString();
//        httpClient = new DefaultHttpClient();
//        HttpPost httpPost = new HttpPost(url);
//        StringEntity se = null;
//        try {
//            se = new StringEntity(transJson, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
//        httpPost.setEntity(se);
//
//
//        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
//        parameters.add(new BasicNameValuePair("file", "123321"));
//
//        HttpEntity entity = null;
//        try {
//            entity = new UrlEncodedFormEntity(parameters);
//            httpPost.setEntity(entity);
//            // httpClient执行httpPost表单提交
//            httpClient.execute(httpPost);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        try {
//            httpClient.execute(httpPost);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        /*httpClient = new DefaultHttpClient();
        HttpParams httpParams = httpClient.getParams();
        httpParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 60000);
        httpParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, 60000);
        httpParams.setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
        httpParams.setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
        httpParams.setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET, "UTF-8");
        HttpPost httpPost = new HttpPost(url);




        // 1 构造上传策略
        JSONObject policyJson = new JSONObject();
        long _dataline = System.currentTimeMillis() / 1000 + 3600;
        try {
            policyJson.put("deadline", _dataline);// 有效时间为一个小时
            policyJson.put("scope", "mappushstore:wifi1.png");
            String policy = policyJson.toString();

            String encodedPutPolicy = new String(Base64.encode(policy.getBytes(), Base64.URL_SAFE));
            byte[] _sign = MyUtils.hmacSha1(encodedPutPolicy, SecretKey).getBytes();
            String key = "wifi1.png";
            String _encodedSign = new String(Base64.encode(_sign, Base64.URL_SAFE));
            Log.i("_encodedSign", "_encodedSign****" + _encodedSign);
            String token = AccessKey + ':' + _encodedSign + ':'
                    + encodedPutPolicy;
            File file = new File("file:///android_asset/wifi1.png");

            List<NameValuePair> parameters = new ArrayList<NameValuePair>();
            parameters.add(new BasicNameValuePair("key", key));
            parameters.add(new BasicNameValuePair("token", token));

            UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(parameters);
            urlEncodedFormEntity.setContentType("multipart/form-data");
            httpPost.setEntity(urlEncodedFormEntity);

            // httpClient执行httpPost表单提交
            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();

            Log.i("返回码", "10000000****" + statusCode);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            InputStream is = response.getEntity().getContent();
            int ch;
            byte[] bytes = new byte[1024];
            while ((ch = is.read(bytes)) != -1) {
                byteArrayOutputStream.write(bytes, 0, ch);
            }

            String result = new String(bytes);
            Log.i("QQ", "result === " + result);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();

        }*/
        ByteArrayOutputStream byteArrayOutputStream = null;
        InputStream inputStream = null;
        String BOUNDARY = UUID.randomUUID().toString(); //边界标识 随机生成
        String PREFIX = "--", LINE_END = "\r\n";


        try {
            URL url = new URL(this.url);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(20000);
            conn.setConnectTimeout(20000);
            conn.setDoInput(true); // 允许输入流
            conn.setDoOutput(true); // 允许输出流
            conn.setUseCaches(false); // 不允许使用缓存
            conn.setRequestMethod("POST"); // 请求方式
//            conn.setRequestProperty("Charset", CHARSET); // 设置编码
            conn.setRequestProperty("connection", "keep-alive");
//            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);

            JSONObject policyJson = new JSONObject();
            long _dataline = System.currentTimeMillis() / 1000 + 3600;
            policyJson.put("deadline", _dataline);// 有效时间为一个小时
            policyJson.put("scope", "mappushstore:wifi1.png");
            String policy = policyJson.toString();

            String encodedPutPolicy = new String(Base64.encode(policy.getBytes(), Base64.URL_SAFE));
            byte[] _sign = MyUtils.hmacSha1(encodedPutPolicy, SecretKey).getBytes();
            String key = "wifi1.png";
            String _encodedSign = new String(Base64.encode(_sign, Base64.URL_SAFE));
            Log.i("_encodedSign", "_encodedSign****" + _encodedSign);
            String token = AccessKey + ':' + _encodedSign + ':'
                    + encodedPutPolicy;
            File file = new File("file:///android_asset/wifi1.png");

            conn.setRequestProperty("key", key);
            conn.setRequestProperty("token", token);
            conn.setRequestProperty("file","wifi1.png");

//            OutputStream outputStream = conn.getOutputStream();
//            outputStream.write("Test ... ABCDEFG .............................. qq".getBytes());


//            byteArrayOutputStream = new ByteArrayOutputStream();
//            inputStream = conn.getInputStream();
//            int ch;
//            byte[] bytes = new byte[1024];
//            while ((ch = inputStream.read(bytes)) != -1) {
//                byteArrayOutputStream.write(bytes, 0, ch);
//            }
//            String result = new String(bytes);
//            Log.i("result", "result === " + result);


            if (file != null) {
                /** * 当文件不为空，把文件包装并且上传 */
                OutputStream outputSteam = conn.getOutputStream();
                DataOutputStream dos = new DataOutputStream(outputSteam);
                StringBuffer sb = new StringBuffer();
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINE_END);
                /**
                 * 这里重点注意：
                 * name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件
                 * filename是文件的名字，包含后缀名的 比如:abc.png
                 */
                sb.append("Content-Disposition: form-data; name=\"img\"; filename=\"" + file.getName() + "\"" + LINE_END);
                sb.append("Content-Type: application/octet-stream; charset=" + "UTF_8" + LINE_END);
                sb.append(LINE_END);
                dos.write(sb.toString().getBytes());
                //InputStream is = new FileInputStream(file);
                InputStream is = context.getAssets().open("wifi1.png");
                byte[] bytes = new byte[1024];
                int len = 0;
                while ((len = is.read(bytes)) != -1) {
                    dos.write(bytes, 0, len);
                }
                is.close();
                dos.write(LINE_END.getBytes());
                byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                dos.write(end_data);
                dos.flush();
                /**
                 * 获取响应码 200=成功
                 * 当响应成功，获取响应的流
                 */
                int res = conn.getResponseCode();
                Log.i("响应码", "code" + res);
                if (res == 200) {

                }
            }


//            InputStream inputStream = url.openStream();
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            int ch;
//            byte[] bytes = new byte[1024];
//            while ((ch = inputStream.read(bytes)) != -1) {
//                byteArrayOutputStream.write(bytes, 0, ch);
//            }
//
//            String result = new String(bytes);
//            Log.i("_encodedSign", "result === " + result);
//
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


//        response = httpclient.execute(httpost);

        return null;
    }

    public static HttpURLConnection getConn() {
        return conn;
    }
}
