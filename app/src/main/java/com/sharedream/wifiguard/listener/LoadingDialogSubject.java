package com.sharedream.wifiguard.listener;

import java.util.ArrayList;
import java.util.List;

public class LoadingDialogSubject {
	private static volatile LoadingDialogSubject instance;
	private List<LoadingDialogObserver> observerList;

	private LoadingDialogSubject() {
		observerList = new ArrayList<LoadingDialogObserver>();
	}

	public static LoadingDialogSubject getInstance() {
		if (instance == null) {
			synchronized (LoadingDialogSubject.class) {
				if (instance == null) {
					instance = new LoadingDialogSubject();
				}
			}
		}
		return instance;
	}

	public void registObserver(LoadingDialogObserver observer) {
		unregistObserver(observer);
		observerList.add(observer);
	}
	
	public void unregistObserver(LoadingDialogObserver observer) {
		for (int k = 0; k < observerList.size(); k++) {
			LoadingDialogObserver item = observerList.get(k);
			if (item == observer) {
				observerList.remove(item);
				break;
			}
		}
	}
	
	public void notifyRequestDialogDismiss() {
		int size = observerList.size();
		for (int k = 0; k < size; k++) {
			LoadingDialogObserver observer = observerList.get(k);
			observer.onRequestDialogDismiss();
		}
	}
	
	public void notifyTitleChanged(String title) {
		int size = observerList.size();
		for (int k = 0; k < size; k++) {
			LoadingDialogObserver observer = observerList.get(k);
			observer.onTitleChanged(title);
		}
	}
}
