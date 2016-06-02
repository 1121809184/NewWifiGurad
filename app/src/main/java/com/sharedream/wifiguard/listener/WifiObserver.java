package com.sharedream.wifiguard.listener;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;

import java.util.List;

public interface WifiObserver {

	void onWifiOpen();
	void onWifiClose();

	void onWifiDisconnected();
	void onWifiConnecting();
	void onWifiPasswordVerifying();
	void onWifiCompleted();
	void onWifiPasswordIncorrect();
	void onWifiPasswordCorrect();
	void onWifiIpObtaining();
	void onWifiConnected();
	void onWifiConnectTimeout();

	void onWifiScanResultChanged(List<ScanResult> listResult, List<WifiConfiguration> listWifiConfiguration);

	void onAvailableWifiFound(String ssid);
	void onAvailableWifiNotFound();

}
