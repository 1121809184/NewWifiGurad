package com.sharedream.wifiguard.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.listener.WifiCheckSubject;
import com.sharedream.wifiguard.listener.WifiSubject;

import java.util.Locale;

public class WiFiManager {
	private final static int WIFI_LIST_CHANGE_INTERVAL = 1000 * 2;
	private static WiFiManager instance = null;
	private WifiManager mWifiManager = null;

	private WiFiManager() {
		Context context = AppContext.getContext();
		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		registerReceiver();
	}

	public static WiFiManager getInstance() {
		if (instance == null) {
			synchronized (WiFiManager.class) {
				if (instance == null) {
					instance = new WiFiManager();
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

	public void destroy() {
		Context context = AppContext.getContext();
		if (context != null && receiver != null) {
			context.unregisterReceiver(receiver);
		}
	}

	public int connectAp(String ssid) {
		return connectAp(ssid, null, Constant.AP_SECURITY_OPEN);
	}

	public int connectAp(String ssid, String password, int passwordType) {
		return addNetwork(createWifiInfo(ssid, password, passwordType));
	}

	public void connectNetwork(int networkId) {
		if (mWifiManager == null) {
			mWifiManager = (WifiManager) AppContext.getContext().getSystemService(Context.WIFI_SERVICE);
		}

		mWifiManager.disconnect();
		mWifiManager.enableNetwork(networkId, true);
		mWifiManager.reconnect();
	}

	public void disconnect() {
		if (mWifiManager == null) {
			mWifiManager = (WifiManager) AppContext.getContext().getSystemService(Context.WIFI_SERVICE);
		}
		mWifiManager.disconnect();
	}

	public void enabledWifi() {
		if (mWifiManager == null) {
			mWifiManager = (WifiManager) AppContext.getContext().getSystemService(Context.WIFI_SERVICE);
		}
		mWifiManager.setWifiEnabled(true);
	}

	public void disableNetwork(int networkId) {
		if (mWifiManager == null) {
			mWifiManager = (WifiManager) AppContext.getContext().getSystemService(Context.WIFI_SERVICE);
		}
		mWifiManager.disableNetwork(networkId);
	}

	public int getCurrentNetworkId() {
		if (mWifiManager == null) {
			mWifiManager = (WifiManager) AppContext.getContext().getSystemService(Context.WIFI_SERVICE);
		}

		WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
		return (wifiInfo == null) ? 0 : wifiInfo.getNetworkId();
	}

	public String getCurrentSsid() {
		if (mWifiManager == null) {
			mWifiManager = (WifiManager) AppContext.getContext().getSystemService(Context.WIFI_SERVICE);
		}

		WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
		if (wifiInfo != null) {
			return trimQuotation(wifiInfo.getSSID());
		}
		return null;
	}

	public String getCurrentMacAddress() {
		if (mWifiManager == null) {
			mWifiManager = (WifiManager) AppContext.getContext().getSystemService(Context.WIFI_SERVICE);
		}

		WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
		return (wifiInfo == null) ? "" : wifiInfo.getMacAddress();
	}

	public void openWifi() {
		if (mWifiManager == null) {
			mWifiManager = (WifiManager) AppContext.getContext().getSystemService(Context.WIFI_SERVICE);
		}

		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
	}

	public void closeWifi() {
		if (mWifiManager == null) {
			mWifiManager = (WifiManager) AppContext.getContext().getSystemService(Context.WIFI_SERVICE);
		}

		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		}
	}

	public void disconnectWifi(int networkId) {
		if (mWifiManager == null) {
			mWifiManager = (WifiManager) AppContext.getContext().getSystemService(Context.WIFI_SERVICE);
		}

		mWifiManager.disableNetwork(networkId);
		mWifiManager.disconnect();
	}

	public WifiInfo getConnectionInfo() {
		if (mWifiManager == null) {
			mWifiManager = (WifiManager) AppContext.getContext().getSystemService(Context.WIFI_SERVICE);
		}
		return mWifiManager.getConnectionInfo();
	}

	private int addNetwork(WifiConfiguration config) {
		if (mWifiManager == null) {
			mWifiManager = (WifiManager) AppContext.getContext().getSystemService(Context.WIFI_SERVICE);
		}

		mWifiManager.disconnect();
		int networkId = mWifiManager.addNetwork(config);
		boolean enableNetwork = mWifiManager.enableNetwork(networkId, true);

		if (networkId == -1 && !enableNetwork) {
			WifiCheckSubject.getInstance().notifyWifiFailed();
		}

		if (enableNetwork) {
			mWifiManager.reconnect();
		}

		return networkId;
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

	public String formatIpAddress(int ip) {
		return String.format(Locale.CHINA, "%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		private long lastGetWifiScanResultTime;
		private DetailedState lastWifiDetailedState;
		private SupplicantState lastSupplicantState;

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)
						|| intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)
						|| intent.getAction().equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
					if (mWifiManager != null) {
						int wifiState = mWifiManager.getWifiState();
						if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
							WifiSubject.getInstance().notifyWifiOpen();
						}
						else if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
							WifiSubject.getInstance().notifyWifiClose();
						}

						/*WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
						if (wifiInfo != null) {
							SupplicantState supplicantState = wifiInfo.getSupplicantState();

							MyLog.debug(getClass(), "lastState / supplicantState = " + lastSupplicantState + " / " + supplicantState);

							if (lastSupplicantState != supplicantState) {
								lastSupplicantState = supplicantState;
								if (supplicantState == SupplicantState.FOUR_WAY_HANDSHAKE || supplicantState == SupplicantState.GROUP_HANDSHAKE) {
									WifiCheckSubject.getInstance().notifySupplicantVerifyingPassword();
								}
								else if (supplicantState == SupplicantState.DISCONNECTED) {
									WifiCheckSubject.getInstance().notifySupplicantDisconnected();
								}
								else if (supplicantState == SupplicantState.ASSOCIATING || supplicantState == SupplicantState.ASSOCIATED) {
									WifiCheckSubject.getInstance().notifySupplicantAssociated();
								}
								else if (supplicantState == SupplicantState.COMPLETED) {
									WifiCheckSubject.getInstance().notifySupplicantCompleted();
								}
								else if (supplicantState == SupplicantState.SCANNING) {
									WifiCheckSubject.getInstance().notifySupplicantScanning();
								}
							}
						}

						ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
						NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
						if (networkInfo != null) {
							DetailedState detailedState = networkInfo.getDetailedState();

							MyLog.warn(getClass(), "lastState / detailedState = " + lastWifiDetailedState + " / " + detailedState);

							if (lastWifiDetailedState != detailedState) {
								lastWifiDetailedState = detailedState;
								if (DetailedState.CONNECTING == detailedState) {
									WifiCheckSubject.getInstance().notifyWifiConnecting();
								}
								else if (DetailedState.OBTAINING_IPADDR == detailedState || DetailedState.AUTHENTICATING == detailedState) {
									WifiCheckSubject.getInstance().notifyWifiIpObtaining();
								} else if (DetailedState.CONNECTED == detailedState) {
									WifiSubject.getInstance().notifyWifiConnected();
									WifiCheckSubject.getInstance().notifyWifiConnected();
								} else if (DetailedState.SUSPENDED == detailedState) {
									WifiCheckSubject.getInstance().notifyWifiSuspended();
								} else if (DetailedState.FAILED == detailedState) {
									WifiCheckSubject.getInstance().notifyWifiFailed();
								} else if (DetailedState.DISCONNECTED == detailedState) {
									WifiSubject.getInstance().notifyWifiDisconnected();
									WifiCheckSubject.getInstance().notifyWifiDisconnected();
								} else {
									if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
										if (DetailedState.BLOCKED == detailedState) {
											WifiCheckSubject.getInstance().notifyWifiBlocked();
										}
									}
								}
							}
						}*/

						if (intent.getAction().equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
							SupplicantState supplicantState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
							Log.d(WifiManager.class.getSimpleName(), "new supplicantState = " + supplicantState);

							if (supplicantState == SupplicantState.FOUR_WAY_HANDSHAKE || supplicantState == SupplicantState.GROUP_HANDSHAKE) {
								WifiCheckSubject.getInstance().notifySupplicantVerifyingPassword();
							}
							else if (supplicantState == SupplicantState.DISCONNECTED) {
								WifiCheckSubject.getInstance().notifySupplicantDisconnected();
							}
							else if (supplicantState == SupplicantState.ASSOCIATING || supplicantState == SupplicantState.ASSOCIATED) {
								WifiCheckSubject.getInstance().notifySupplicantAssociated();
							}
							else if (supplicantState == SupplicantState.COMPLETED) {
								WifiCheckSubject.getInstance().notifySupplicantCompleted();
								WifiCheckSubject.getInstance().notifyWifiConnected();
							}
							else if (supplicantState == SupplicantState.SCANNING) {
								WifiCheckSubject.getInstance().notifySupplicantScanning();
							}

							int supplicantErrorCode = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
							if (supplicantErrorCode == WifiManager.ERROR_AUTHENTICATING) {
								WifiCheckSubject.getInstance().notifyWifiPasswordIncorrect();
							}
						}
					}
				} else if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
					long curTime = System.currentTimeMillis();
					if (curTime - lastGetWifiScanResultTime > WIFI_LIST_CHANGE_INTERVAL) {
						lastGetWifiScanResultTime = curTime;
						if (mWifiManager != null) {
							WifiSubject.getInstance().notifyWifiScanResultChanged(mWifiManager.getScanResults(), mWifiManager.getConfiguredNetworks());
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

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

	public void removeNetwork(int networkId) {
		if (mWifiManager == null) {
			mWifiManager = (WifiManager) AppContext.getContext().getSystemService(Context.WIFI_SERVICE);
		}
		mWifiManager.removeNetwork(networkId);
	}
}
