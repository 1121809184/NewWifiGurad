package com.sharedream.wifiguard.task;

import android.os.AsyncTask;

import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.listener.NetworkCheckSubject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;


@SuppressWarnings("deprecation")
public class NetworkCheckTask extends AsyncTask<String, Throwable, String> {

	private final static int TIMEOUT = 5;
	private final static String NETWORK_NOT_AVAILABLE = "NETWORK_NOT_AVAILABLE";
	private final static String NETWORK_KEY = "x8b3AcG";

	protected String doInBackground(String... params) {
		InputStream is = null;
		ByteArrayOutputStream baos = null;
		String result = null;

		try {
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT * 1000);
			HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT * 1000);
			HttpClient httpClient = new DefaultHttpClient(httpParams);
			HttpGet httpGet = new HttpGet(Constant.URL_NETWORK_TEST_SERVER);
			HttpResponse response = httpClient.execute(httpGet);
			
			HttpEntity responseEntity = response.getEntity();
			is = responseEntity.getContent();
			if (is != null) {
				baos = new ByteArrayOutputStream();
				byte[] buff = new byte[512];
				int ch = -1;
				while ((ch = is.read(buff)) != -1) {
					baos.write(buff, 0, ch); 
				}
			}
			responseEntity.consumeContent();
			result = new String(baos.toByteArray(), Constant.SYS_ENCODING);
		} catch (Exception e) {
			if (e instanceof UnknownHostException || e instanceof SocketException || e instanceof SocketTimeoutException) {
				result = NETWORK_NOT_AVAILABLE;
			}
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

	@Override
	protected void onPostExecute(String result) {
		if (NETWORK_NOT_AVAILABLE.equals(result)) {
			NetworkCheckSubject.getInstance().notifyNetworkNotAvailable();
			return;
		}
		
		if (result != null && result.contains(NETWORK_KEY)) {
			NetworkCheckSubject.getInstance().notifyNetworkAvailable();
		}
		else {	
			NetworkCheckSubject.getInstance().notifyNetworkNotAvailable();
		}
	} 

	@Override
	protected void onPreExecute() {} 

	@Override
	protected void onProgressUpdate(Throwable... values) {}
	
} 
