package com.sharedream.wifiguard.utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings("deprecation")
public class HttpUtils {

	public static String sendHttpGetRequest(String url) {
		return sendHttpGetRequest(url, 15);
	}
	
	public static String sendHttpGetRequest(String url, int timeout) {
		HttpParams params = new BasicHttpParams();
		params.setParameter(ClientPNames.HANDLE_REDIRECTS, true);
		HttpConnectionParams.setConnectionTimeout(params, 1000 * timeout);
		HttpConnectionParams.setSoTimeout(params, 1000 * timeout);
		HttpClient httpClient = new DefaultHttpClient(params);
		
		HttpGet httpGet = new HttpGet(url);
		InputStream is = null;
		ByteArrayOutputStream baos = null;
		String result = null;
		try {
			HttpResponse response = httpClient.execute(httpGet);
			is = response.getEntity().getContent();
			baos = new ByteArrayOutputStream();
			int ch;
			byte[] buffer = new byte[1024];
			while ((ch = is.read(buffer)) != -1) {
				baos.write(buffer, 0, ch);
			}
			result = new String(baos.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (baos != null) {
				try {
					baos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
}
