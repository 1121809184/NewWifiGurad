package com.sharedream.wifiguard.listener;

import java.util.ArrayList;
import java.util.List;

public class DnsSubject {
    private static volatile DnsSubject instance;
    private List<DnsObserver> observerList;

    private DnsSubject() {
        observerList = new ArrayList<DnsObserver>();
    }

    public static DnsSubject getInstance() {
        if (instance == null) {
            synchronized (DnsSubject.class) {
                if (instance == null) {
                    instance = new DnsSubject();
                }
            }
        }
        return instance;
    }

    public void registObserver(DnsObserver observer) {
        unregistObserver(observer);
        observerList.add(observer);
    }

    public void unregistObserver(DnsObserver observer) {
        for (int k = 0; k < observerList.size(); k++) {
            DnsObserver item = observerList.get(k);
            if (item.getClass().getName().equals(observer.getClass().getName())) {
                observerList.remove(item);
                break;
            }
        }
    }

    public void notifyDns(boolean isSuccess1, boolean isSuccess2) {
        int size = observerList.size();
        for (int k = 0; k < size; k++) {
            DnsObserver observer = observerList.get(k);
            observer.onDnsRelatived(isSuccess1,isSuccess2);
        }
    }

}
