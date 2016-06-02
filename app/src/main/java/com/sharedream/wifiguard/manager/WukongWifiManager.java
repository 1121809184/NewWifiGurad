package com.sharedream.wifiguard.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.listener.WifiSubject;
import com.sharedream.wifiguard.utils.LogUtils;

import java.lang.reflect.Method;
import java.util.List;

public class WukongWifiManager {
    private static WukongWifiManager instance = null;
    private WifiManager wifiManager = null;
    private Runnable scanWifiTask;
    private ShareDreamHandler handler;

    private WukongWifiManager() {
        Context context = AppContext.getContext();
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        handler = new ShareDreamHandler();
        registerReceiver();

        scanWifiTask = new Runnable() {
            @Override
            public void run() {
                LogUtils.d("start to scan wifi...");
                wifiManager.startScan();
            }
        };
    }

    public static WukongWifiManager getInstance() {
        if (instance == null) {
            synchronized (WukongWifiManager.class) {
                if (instance == null) {
                    instance = new WukongWifiManager();
                }
            }
        }
        return instance;
    }

    private void registerReceiver() {
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            AppContext.getContext().registerReceiver(receiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean disconnect() {
        if (wifiManager == null) {
            wifiManager = (WifiManager) AppContext.getContext().getSystemService(Context.WIFI_SERVICE);
        }
        return wifiManager.disconnect();
    }

    public void disableNetwork(int networkId) {
        if (wifiManager == null) {
            wifiManager = (WifiManager) AppContext.getContext().getSystemService(Context.WIFI_SERVICE);
        }
        wifiManager.disableNetwork(networkId);
    }

    public void removeNetwork(int networkId) {
        if (wifiManager == null) {
            wifiManager = (WifiManager) AppContext.getContext().getSystemService(Context.WIFI_SERVICE);
        }
        wifiManager.removeNetwork(networkId);
    }

    public void removeNetwork(String ssid, String bssid) {
        if (wifiManager == null) {
            wifiManager = (WifiManager) AppContext.getContext().getSystemService(Context.WIFI_SERVICE);
        }
        List<WifiConfiguration> listWifiConfiguration = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration wifiConfiguration : listWifiConfiguration) {
            if (wifiConfiguration.SSID.equals(ssid) && wifiConfiguration.BSSID.equals(bssid)) {
                wifiManager.removeNetwork(wifiConfiguration.networkId);
                break;
            }
        }
    }

    public void enableNetwork(int networkId) {
        if (wifiManager == null) {
            wifiManager = (WifiManager) AppContext.getContext().getSystemService(Context.WIFI_SERVICE);
        }
        wifiManager.enableNetwork(networkId, true);
        //        wifiManager.saveConfiguration();
        //        wifiManager.reconnect();
    }

    public int connectOpenSSID(String ssid) {
        return connectSSID(ssid, null, Constant.AP_SECURITY_OPEN);
    }

    public int connectSSID(String ssid, String password, int type) {
        LogUtils.d("connectSSID(" + ssid +", " + password + ", " + type + ")");
        if (wifiManager == null) {
            wifiManager = (WifiManager) AppContext.getContext().getSystemService(Context.WIFI_SERVICE);
        }

        WifiConfiguration config = createWifiInfo(ssid, password, type);
        if (wifiManager != null) {
            if (isUsingWifi()) {
                wifiManager.disconnect();
            }

            int formerNetworkId = wifiManager.updateNetwork(config);
            int networkId = formerNetworkId == -1 ? wifiManager.addNetwork(config) : formerNetworkId;
            LogUtils.d("Connecting SSID " + ssid + " with security " + type);

            Method connectMethod = connectWifiByReflectMethod(networkId);
            if (connectMethod == null) {
                LogUtils.d("connect wifi by system method: enableNetwork()");
                if (wifiManager.enableNetwork(networkId, true) && wifiManager.saveConfiguration() && wifiManager.reconnect()) {
                    return networkId;    // important, otherwise the network won't switch immediately, even it'll not switch
                }
            } else {
                return networkId;
            }
        }

        return -1;
    }

    public WifiConfiguration createWifiInfo(String ssid, String password, int type) {
        String finalSsid = trimQuotation(ssid);

        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"" + finalSsid + "\"";

        if (type == Constant.AP_SECURITY_OPEN) {        // WIFICIPHER_NOPASS
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (type == Constant.AP_SECURITY_WEP) {    // WIFICIPHER_WEP
            config.hiddenSSID = true;
            config.wepTxKeyIndex = 0;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == Constant.AP_SECURITY_WPA) { // WIFICIPHER_WPA
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.status = WifiConfiguration.Status.ENABLED;
        }

        return config;
    }

    private Method connectWifiByReflectMethod(int netId) {
        LogUtils.d("use reflex to connect wifi...");
        Method connectMethod = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // 反射方法： connect(int, listener) , 4.2 <= phone's android version
            for (Method methodSub : wifiManager.getClass().getDeclaredMethods()) {
                if ("connect".equalsIgnoreCase(methodSub.getName())) {
                    Class<?>[] types = methodSub.getParameterTypes();
                    if (types != null && types.length > 0) {
                        if ("int".equalsIgnoreCase(types[0].getName())) {
                            connectMethod = methodSub;
                        }
                    }
                }
            }
            if (connectMethod != null) {
                try {
                    connectMethod.invoke(wifiManager, netId, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
            // 反射方法: connect(Channel c, int networkId, ActionListener listener)
            // 暂时不处理4.1的情况 , 4.1 == phone's android version
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            // 反射方法：connectNetwork(int networkId) ,
            // 4.0 <= phone's android version < 4.1
            for (Method methodSub : wifiManager.getClass().getDeclaredMethods()) {
                if ("connectNetwork".equalsIgnoreCase(methodSub.getName())) {
                    Class<?>[] types = methodSub.getParameterTypes();
                    if (types != null && types.length > 0) {
                        if ("int".equalsIgnoreCase(types[0].getName())) {
                            connectMethod = methodSub;
                        }
                    }
                }
            }
            if (connectMethod != null) {
                try {
                    connectMethod.invoke(wifiManager, netId);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        } else {
            // < android 4.0
            return null;
        }

        return connectMethod;
    }

    public String formatIpAddress(int ip) {
        return String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
    }

    public boolean isWifiConnected() {
        if (wifiManager == null) {
            wifiManager = (WifiManager) AppContext.getContext().getSystemService(Context.WIFI_SERVICE);
        }
        return (wifiManager.isWifiEnabled() && isUsingWifi() && getConnectionInfo().getIpAddress() != 0) ? true : false;//mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
    }

    public WifiInfo getConnectionInfo() {
        if (wifiManager == null) {
            wifiManager = (WifiManager) AppContext.getContext().getSystemService(Context.WIFI_SERVICE);
        }
        return wifiManager.getConnectionInfo();
    }

    public int getCurrentNetworkId() {
        int ret = -1;
        WifiInfo wifiInfo = getConnectionInfo();
        if (wifiInfo != null) {
            ret = wifiInfo.getNetworkId();
        }
        return ret;
    }

    public void scanWifi() {
        if (wifiManager == null) {
            wifiManager = (WifiManager) AppContext.getContext().getSystemService(Context.WIFI_SERVICE);
        }

        handler.removeCallbacks(scanWifiTask);
        handler.postDelayed(scanWifiTask, 100);
    }

    public boolean isWifiEnabled() {
        if (wifiManager == null) {
            wifiManager = (WifiManager) AppContext.getContext().getSystemService(Context.WIFI_SERVICE);
        }
        return wifiManager.isWifiEnabled();
    }

    public boolean openWifi() {
        if (wifiManager == null) {
            wifiManager = (WifiManager) AppContext.getContext().getSystemService(Context.WIFI_SERVICE);
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                wifiManager.setWifiEnabled(true);
            }
        }, 100);

        return true;
    }

    public boolean closeWifi() {
        if (wifiManager == null) {
            wifiManager = (WifiManager) AppContext.getContext().getSystemService(Context.WIFI_SERVICE);
        }
        return wifiManager.setWifiEnabled(false);
    }

    public boolean isUsingWifi() {
        try {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) AppContext.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (mConnectivityManager != null) {
                NetworkInfo.State state = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
                if (state == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean isNetworkAvailable() {
        try {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) AppContext.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (mConnectivityManager != null) {
                NetworkInfo[] info = mConnectivityManager.getAllNetworkInfo();
                if (info != null) {
                    for (int i = 0; i < info.length; i++) {
                        if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        private SupplicantState lastSupplicantState;
        private int lastWifiState;
        private boolean isWifiConnected;

        public void onReceive(Context context, Intent intent) {
            LogUtils.d(WukongWifiManager.class.getSimpleName(), "Intent Action: " + intent.getAction());

            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                WifiSubject.getInstance().notifyWifiScanResultChanged(wifiManager.getScanResults(), wifiManager.getConfiguredNetworks());
                if (wifiManager != null) {
                    if (!isWifiConnected) {
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        if (wifiInfo != null) {
                            SupplicantState supplicantState = wifiInfo.getSupplicantState();
                            if (supplicantState == SupplicantState.COMPLETED && wifiInfo.getIpAddress() != 0) {
                                isWifiConnected = true;
                                WifiSubject.getInstance().notifyWifiConnected();
                            }
                        }
                    }
                }

                handleWifiScanReuslt();
            }

            if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)
                    || intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)
                    || intent.getAction().equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
                if (wifiManager != null) {
                    int wifiState = wifiManager.getWifiState();
                    if (lastWifiState != wifiState) {
                        lastWifiState = wifiState;
                        if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                            WifiSubject.getInstance().notifyWifiOpen();
                        } else if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
                            WifiSubject.getInstance().notifyWifiClose();
                        }
                    }

                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    if (wifiInfo != null) {
                        SupplicantState supplicantState = wifiInfo.getSupplicantState();
                        LogUtils.d(WukongWifiManager.class.getSimpleName(), "Supplicant cur state: " + supplicantState + " / last state: " + lastSupplicantState);

                        if (lastSupplicantState != supplicantState) {
                            if (supplicantState == SupplicantState.FOUR_WAY_HANDSHAKE || supplicantState == SupplicantState.GROUP_HANDSHAKE) {
                                isWifiConnected = false;
                                if (lastSupplicantState != SupplicantState.FOUR_WAY_HANDSHAKE && lastSupplicantState != SupplicantState.GROUP_HANDSHAKE) {
                                    WifiSubject.getInstance().notifyWifiVerifyingPassword();
                                }
                            } else if (supplicantState == SupplicantState.DISCONNECTED) {
                                isWifiConnected = false;
                                WifiSubject.getInstance().notifyWifiDisconnected();
                            } else if (supplicantState == SupplicantState.COMPLETED) {
                                WifiSubject.getInstance().notifyWifiVerificationCompleted();
                                WifiSubject.getInstance().notifyWifiIpObtaining();
                            } else if (supplicantState == SupplicantState.SCANNING) {
                                isWifiConnected = false;
                                WifiSubject.getInstance().notifyWifiConnecting();
                            } else if (supplicantState == SupplicantState.ASSOCIATING || supplicantState == SupplicantState.ASSOCIATED) {
                                isWifiConnected = false;
                            }
                            lastSupplicantState = supplicantState;
                        }

                        LogUtils.d(WukongWifiManager.class.getSimpleName(), "isWifiConnected = " + isWifiConnected + " and supplicantState = " + supplicantState + " and ip = " + wifiInfo.getIpAddress());

                        if (!isWifiConnected && supplicantState == SupplicantState.COMPLETED && wifiInfo.getIpAddress() != 0) {
                            isWifiConnected = true;
                            WifiSubject.getInstance().notifyWifiConnected();
                        }
                    }
                }
            }
        }
    };

    private void handleWifiScanReuslt() {
    }

    // remove " in ssid name for some android systems
    public String trimQuotation(String ssid) {
        if (ssid == null) {
            return null;
        }
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        return ssid;
    }
}
