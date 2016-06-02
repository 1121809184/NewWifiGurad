package com.sharedream.wifiguard.listener;


public interface WifiCheckObserver {

	public void onWifiCheckBegin();
	public void onWifiCheckTimeout();
	public void onWifiInvisible();
	
	public void onWifiConnecting();
	public void onWifiIpObtaining();
	public void onWifiConnected();
	public void onWifiSuspended();
	public void onWifiBlocked();
	public void onWifiFailed();
	public void onWifiDisconnected();
	
	public void onSupplicantScanning();
	
	public void onSupplicantVerifyingPassword();
	public void onSupplicantDisconnected();
	public void onSupplicantAssociated();
	public void onSupplicantCompleted();
	public void onWifiNeedPassword();
	public void onWifiPasswordIncorrect();
	public void onWifiNetworkAvailable();
	public void onWifiNetworkNotAvailable();
	
	public void onWifiNotPortal();
	public void onWifiOpenPortalTimeout();
	public void onWifiPortalReceived(String portalWebPage);
	
}
