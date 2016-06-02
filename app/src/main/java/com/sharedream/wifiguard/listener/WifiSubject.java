package com.sharedream.wifiguard.listener;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;

import java.util.ArrayList;
import java.util.List;

public class WifiSubject {
    private static volatile WifiSubject instance;
    private List<WifiObserver> observerList;

    private WifiSubject() {
        observerList = new ArrayList<WifiObserver>();
    }

    public static WifiSubject getInstance() {
        if (instance == null) {
            synchronized (WifiSubject.class) {
                if (instance == null) {
                    instance = new WifiSubject();
                }
            }
        }
        return instance;
    }

    public void registObserver(WifiObserver observer) {
        unregistObserver(observer);
        observerList.add(observer);
    }

    public void unregistObserver(WifiObserver observer) {
        for (int k = 0; k < observerList.size(); k++) {
            WifiObserver item = observerList.get(k);
            if (item.getClass().getName().equals(observer.getClass().getName())) {
                observerList.remove(item);
                break;
            }
        }
    }

    public void notifyWifiScanResultChanged(List<ScanResult> listResult, List<WifiConfiguration> listWifiConfiguration) {
        int size = observerList.size();
        for (int k = 0; k < size; k++) {
            WifiObserver observer = observerList.get(k);
            observer.onWifiScanResultChanged(listResult, listWifiConfiguration);
        }
    }

    public void notifyWifiClose() {
        int size = observerList.size();
        for (int k = 0; k < size; k++) {
            WifiObserver observer = observerList.get(k);
            observer.onWifiClose();
        }
    }

    public void notifyWifiOpen() {
        int size = observerList.size();
        for (int k = 0; k < size; k++) {
            WifiObserver observer = observerList.get(k);
            observer.onWifiOpen();
        }
    }

    public void notifyAvailableWifiFound(String ssid) {
        int size = observerList.size();
        for (int k = 0; k < size; k++) {
            WifiObserver observer = observerList.get(k);
            observer.onAvailableWifiFound(ssid);
        }
    }

    public void notifyAvailableWifiNotFound() {
        int size = observerList.size();
        for (int k = 0; k < size; k++) {
            WifiObserver observer = observerList.get(k);
            observer.onAvailableWifiNotFound();
        }
    }

    public void notifyWifiConnected() {
        int size = observerList.size();
        for (int k = 0; k < size; k++) {
            WifiObserver observer = observerList.get(k);
            observer.onWifiConnected();
        }
    }

    public void notifyWifiConnecting() {
        int size = observerList.size();
        for (int k = 0; k < size; k++) {
            WifiObserver observer = observerList.get(k);
            observer.onWifiConnecting();
        }
    }

    public void notifyWifiDisconnected() {
        int size = observerList.size();
        for (int k = 0; k < size; k++) {
            WifiObserver observer = observerList.get(k);
            observer.onWifiDisconnected();
        }
    }

    public void notifyWifiIpObtaining() {
        int size = observerList.size();
        for (int k = 0; k < size; k++) {
            WifiObserver observer = observerList.get(k);
            observer.onWifiIpObtaining();
        }
    }

    public void notifyWifiVerifyingPassword() {
        int size = observerList.size();
        for (int k = 0; k < size; k++) {
            WifiObserver observer = observerList.get(k);
            observer.onWifiPasswordVerifying();
        }
    }

    public void notifyWifiVerificationCompleted() {
        int size = observerList.size();
        for (int k = 0; k < size; k++) {
            WifiObserver observer = observerList.get(k);
            observer.onWifiCompleted();
        }
    }

    public void notifyWifiPasswordIncorrect() {
        int size = observerList.size();
        for (int k = 0; k < size; k++) {
            WifiObserver observer = observerList.get(k);
            observer.onWifiPasswordIncorrect();
        }
    }

    public void notifyWifiPasswordCorrect() {
        int size = observerList.size();
        for (int k = 0; k < size; k++) {
            WifiObserver observer = observerList.get(k);
            observer.onWifiPasswordCorrect();
        }
    }

    public void notifyWifiConnectTimeout() {
        int size = observerList.size();
        for (int k = 0; k < size; k++) {
            WifiObserver observer = observerList.get(k);
            observer.onWifiConnectTimeout();
        }
    }
}
