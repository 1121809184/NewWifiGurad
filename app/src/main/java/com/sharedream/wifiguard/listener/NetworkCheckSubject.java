package com.sharedream.wifiguard.listener;

import java.util.ArrayList;
import java.util.List;

public class NetworkCheckSubject {
	private static volatile NetworkCheckSubject instance;
	private List<NetworkCheckObserver> observerList;

	private NetworkCheckSubject() {
		observerList = new ArrayList<NetworkCheckObserver>();
	}

	public static NetworkCheckSubject getInstance() {
		if (instance == null) {
			synchronized (NetworkCheckSubject.class) {
				if (instance == null) {
					instance = new NetworkCheckSubject();
				}
			}
		}
		return instance;
	}

	public void registObserver(NetworkCheckObserver observer) {
		unregistObserver(observer);
		observerList.add(observer);
	}
	
	public void unregistObserver(NetworkCheckObserver observer) {
		for (int k = 0; k < observerList.size(); k++) {
			NetworkCheckObserver item = observerList.get(k);
			if (item.getClass().getName().equals(observer.getClass().getName())) {
				observerList.remove(item);
				break;
			}
		}
	}
	
	public void notifyNetworkAvailable() {
		int size = observerList.size();
		for (int k = 0; k < size; k++) {
			NetworkCheckObserver observer = observerList.get(k);
			observer.onNetworkAvailable();
		}
	}
	
	public void notifyNetworkNotAvailable() {
		int size = observerList.size();
		for (int k = 0; k < size; k++) {
			NetworkCheckObserver observer = observerList.get(k);
			observer.onNetworkNotAvailable();
		}
	}
}
