package com.sharedream.wifiguard.listener;

import java.util.ArrayList;
import java.util.List;

public class WifiCheckSubject {
	private static volatile WifiCheckSubject instance;
	private List<WifiCheckObserver> observerList;

	private WifiCheckSubject() {
		observerList = new ArrayList<WifiCheckObserver>();
	}

	public static WifiCheckSubject getInstance() {
		if (instance == null) {
			synchronized (WifiCheckSubject.class) {
				if (instance == null) {
					instance = new WifiCheckSubject();
				}
			}
		}
		return instance;
	}

	public void registObserver(WifiCheckObserver observer) {
		unregistObserver(observer);
		observerList.add(observer);
	}

	public void unregistObserver(WifiCheckObserver observer) {
		for (int k = 0; k < observerList.size(); k++) {
			WifiCheckObserver item = observerList.get(k);
			if (item.getClass().getName().equals(observer.getClass().getName())) {
				observerList.remove(item);
				break;
			}
		}
	}

	public void notifyWifiCheckBegin() {
		int size = observerList.size();
		for (int k = 0; k < size; k++) {
			WifiCheckObserver observer = observerList.get(k);
			observer.onWifiCheckBegin();
		}
	}

	public void notifyWifiCheckTimeout() {
		int size = observerList.size();
		for (int k = 0; k < size; k++) {
			WifiCheckObserver observer = observerList.get(k);
			observer.onWifiCheckTimeout();
		}
	}

	public void notifyWifiInvisible() {
		int size = observerList.size();
		for (int k = 0; k < size; k++) {
			WifiCheckObserver observer = observerList.get(k);
			observer.onWifiInvisible();
		}
	}

	public void notifyWifiConnected() {
		int size = observerList.size();
		for (int k = 0; k < size; k++) {
			WifiCheckObserver observer = observerList.get(k);
			observer.onWifiConnected();
		}
	}

	public void notifyWifiDisconnected() {
		int size = observerList.size();
		for (int k = 0; k < size; k++) {
			WifiCheckObserver observer = observerList.get(k);
			observer.onWifiDisconnected();
		}
	}

	public void notifySupplicantAssociated() {
		int size = observerList.size();
		for (int k = 0; k < size; k++) {
			WifiCheckObserver observer = observerList.get(k);
			observer.onSupplicantAssociated();
		}
	}

	public void notifySupplicantCompleted() {
		int size = observerList.size();
		for (int k = 0; k < size; k++) {
			WifiCheckObserver observer = observerList.get(k);
			observer.onSupplicantCompleted();
		}
	}

	public void notifyWifiPasswordIncorrect() {
		int size = observerList.size();
		for (int k = 0; k < size; k++) {
			WifiCheckObserver observer = observerList.get(k);
			observer.onWifiPasswordIncorrect();
		}
	}

	public void notifySupplicantVerifyingPassword() {
		int size = observerList.size();
		for (int k = 0; k < size; k++) {
			WifiCheckObserver observer = observerList.get(k);
			observer.onSupplicantVerifyingPassword();
		}
	}

	public void notifySupplicantDisconnected() {
		int size = observerList.size();
		for (int k = 0; k < size; k++) {
			WifiCheckObserver observer = observerList.get(k);
			observer.onSupplicantDisconnected();
		}
	}

	public void notifySupplicantScanning() {
		int size = observerList.size();
		for (int k = 0; k < size; k++) {
			WifiCheckObserver observer = observerList.get(k);
			observer.onSupplicantScanning();
		}
	}

	public void notifyWifiNeedPassword() {
		int size = observerList.size();
		for (int k = 0; k < size; k++) {
			WifiCheckObserver observer = observerList.get(k);
			observer.onWifiNeedPassword();
		}
	}

	public void notifyWifiBlocked() {
		int size = observerList.size();
		for (int k = 0; k < size; k++) {
			WifiCheckObserver observer = observerList.get(k);
			observer.onWifiBlocked();
		}
	}

	public void notifyWifiConnecting() {
		int size = observerList.size();
		for (int k = 0; k < size; k++) {
			WifiCheckObserver observer = observerList.get(k);
			observer.onWifiConnecting();
		}
	}

	public void notifyWifiFailed() {
		int size = observerList.size();
		for (int k = 0; k < size; k++) {
			WifiCheckObserver observer = observerList.get(k);
			observer.onWifiFailed();
		}
	}

	public void notifyWifiIpObtaining() {
		int size = observerList.size();
		for (int k = 0; k < size; k++) {
			WifiCheckObserver observer = observerList.get(k);
			observer.onWifiIpObtaining();
		}
	}

	public void notifyWifiNetworkAvailable() {
		int size = observerList.size();
		for (int k = 0; k < size; k++) {
			WifiCheckObserver observer = observerList.get(k);
			observer.onWifiNetworkAvailable();
		}
	}

	public void notifyWifiNetworkNotAvailable() {
		int size = observerList.size();
		for (int k = 0; k < size; k++) {
			WifiCheckObserver observer = observerList.get(k);
			observer.onWifiNetworkNotAvailable();
		}
	}

	public void notifyWifiPortalReceived(String portalWebPage) {
		int size = observerList.size();
		for (int k = 0; k < size; k++) {
			WifiCheckObserver observer = observerList.get(k);
			observer.onWifiPortalReceived(portalWebPage);
		}
	}

	public void notifyWifiNotPortal() {
		int size = observerList.size();
		for (int k = 0; k < size; k++) {
			WifiCheckObserver observer = observerList.get(k);
			observer.onWifiNotPortal();
		}
	}

	public void notifyWifiOpenPortalTimeout() {
		int size = observerList.size();
		for (int k = 0; k < size; k++) {
			WifiCheckObserver observer = observerList.get(k);
			observer.onWifiOpenPortalTimeout();
		}
	}

	public void notifyWifiSuspended() {
		int size = observerList.size();
		for (int k = 0; k < size; k++) {
			WifiCheckObserver observer = observerList.get(k);
			observer.onWifiSuspended();
		}
	}
}
