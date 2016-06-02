package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.listener.NetworkCheckObserver;
import com.sharedream.wifiguard.listener.NetworkCheckSubject;
import com.sharedream.wifiguard.listener.WifiCheckObserver;
import com.sharedream.wifiguard.listener.WifiCheckSubject;
import com.sharedream.wifiguard.manager.WiFiManager;
import com.sharedream.wifiguard.task.NetworkCheckTask;
import com.sharedream.wifiguard.utils.HttpUtils;
import com.sharedream.wifiguard.utils.LogUtils;
import com.sharedream.wifiguard.utils.MyUtils;
import com.sharedream.wifiguard.vo.WifiConnectVo;

import java.util.ArrayList;
import java.util.List;

public class WifiCheckActivity extends LoadingActivity implements WifiCheckObserver, NetworkCheckObserver {

    private ScrollView layoutScroll;
    private RelativeLayout layout;

    private int curCheckingWifiIndex;
    private int size;
    private int showDurationTime;
    private boolean isWifiPasswordVerifying;
    private boolean isChecking;
    private WifiConnectVo wifiVo;
    private ArrayList<WifiConnectVo> listWifiConnectVo;
    private List<String> listApPortalJson;
    private int okNetworkId;
    private int countScanTimes;
    private boolean checkApPasswordOnly;
    private boolean isNetworkAvailable;
    private int countDisconnectTimesWhileWifiPasswordVerifying;
    private int index;

    public static void launch(Activity activity, Fragment fragment, Bundle bundle, int requestCode) {
        Intent intent = new Intent(activity, WifiCheckActivity.class);
        if (bundle != null)
            intent.putExtras(bundle);
        fragment.startActivityForResult(intent, requestCode);
    }

    public static void launch(Activity activity, Bundle bundle, int requestCode) {
        Intent intent = new Intent(activity, WifiCheckActivity.class);
        if (bundle != null)
            intent.putExtras(bundle);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WifiCheckSubject.getInstance().registObserver(this);
        NetworkCheckSubject.getInstance().registObserver(this);

        layoutScroll = (ScrollView) findViewById(R.id.layout_scroll);
        layout = (RelativeLayout) findViewById(R.id.layout);

        checkApPasswordOnly = getIntent().getBooleanExtra(Constant.BUNDLE_KEY_CHECK_PASSWORD_ONLY, false);
        showDurationTime = getIntent().getIntExtra(Constant.BUNDLE_KEY_SHOW_DURATION_TIME_ATFER_FINISH_ALL, 0);
        listWifiConnectVo = getIntent().getParcelableArrayListExtra(Constant.BUNDLE_KEY_WIFI_LIST);
        index = getIntent().getIntExtra(Constant.BUNDLE_KEY_INDEX, 0);
        size = listWifiConnectVo.size();
        isChecking = true;

        startCheckWifiList();
    }

    private void startCheckWifiList() {
        curCheckingWifiIndex = -1;
        checkNextWifiIfNecessary();
    }

    private void checkNextWifiIfNecessary() {
        curCheckingWifiIndex++;

        if (curCheckingWifiIndex < size) {
            wifiVo = listWifiConnectVo.get(curCheckingWifiIndex);
            if (wifiVo == null) {
                checkNextWifiIfNecessary();
                return;
            }

            int status = wifiVo.getStatus();
            if (status != Constant.WIFI_STATUS_UNKNOWN) {
                if (okNetworkId <= 0) {
                    if (status == Constant.WIFI_STATUS_VIEW) {
                        okNetworkId = wifiVo.getNetworkId();
                    }
                }
                checkNextWifiIfNecessary();
                return;
            }

            String ssid = wifiVo.getSsid();
            int passwordType = wifiVo.getPasswordType();
            WifiCheckSubject.getInstance().notifyWifiCheckBegin();

            if (passwordType == Constant.WIFI_PASSWORD_TYPE_OPEN) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 && wifiVo.getNetworkId() > 0) { // 6.0系统里直接连接曾连接过的WiFi
                    WiFiManager.getInstance().connectNetwork(wifiVo.getNetworkId());
                } else {
                    int newNetworkId = WiFiManager.getInstance().connectAp(ssid);
                    if (wifiVo != null) {
                        wifiVo.setNetworkId(newNetworkId);
                    }
                }
            } else {
                String password = wifiVo.getPassword();
                if (password != null) {
                    countDisconnectTimesWhileWifiPasswordVerifying = 0;
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 && wifiVo.getNetworkId() > 0) { // 6.0系统里直接连接曾连接过的WiFi
                        WiFiManager.getInstance().connectNetwork(wifiVo.getNetworkId());
                        MyUtils.showToast("enable wifi networkId: " + wifiVo.getNetworkId(), getApplicationContext());
                    } else {
                        int newNetworkId = WiFiManager.getInstance().connectAp(ssid, password, wifiVo.getPasswordType());
                        if (wifiVo != null) {
                            wifiVo.setNetworkId(newNetworkId);
                        }
                    }
                } else {
                    WifiCheckSubject.getInstance().notifyWifiNeedPassword();
                }
            }
        } else {
            appendDetail(getString(R.string.sharedream_sdk_wifi_check_detail_finish_all));
            MyUtils.hideLoadingDialog(viewInfo);
            finishAllCheck();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WifiCheckSubject.getInstance().unregistObserver(this);
        NetworkCheckSubject.getInstance().unregistObserver(this);

        curCheckingWifiIndex = 999;
        isChecking = false;
        isWifiPasswordVerifying = false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN
                && event.getRepeatCount() == 0) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onRequestDialogDismiss() {
        LogUtils.d(getClass().getSimpleName(), "************** onRequestDialogDismiss()");
    }

    private void finishAllCheck() {
        LogUtils.d(getClass().getSimpleName(), "************** finishAllCheck()");

        viewInfo.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!checkApPasswordOnly) {
                    startThread2UploadPortals();
                }

                Intent intent = new Intent();
                intent.putParcelableArrayListExtra(Constant.BUNDLE_KEY_WIFI_LIST, listWifiConnectVo);
                intent.putExtra(Constant.BUNDLE_KEY_OK_NETWORK_ID, okNetworkId);
                intent.putExtra(Constant.BUNDLE_KEY_INDEX, index);
                setResult(RESULT_OK, intent);
                finish();
            }
        }, 1000 * showDurationTime);

        layoutLoadingInfo.setVisibility(View.GONE);
        String info = String.format(getString(R.string.sharedream_sdk_wifi_check_detail_close_after_second), showDurationTime);
        appendDetail(info);

        isChecking = false;
        if (!checkApPasswordOnly && okNetworkId > 0) {
            WiFiManager.getInstance().connectNetwork(okNetworkId);
        }
    }

    private void finishOnException() {
        LogUtils.d(getClass().getSimpleName(), "************** finishOnException()");

        viewInfo.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                String info = String.format(getString(R.string.sharedream_sdk_wifi_check_detail_connected_wifi_wrong), wifiVo.getSsid());
                intent.putExtra(Constant.BUNDLE_KEY_WIFI_EXCEPTION_RESULT, info);
                intent.putExtra(Constant.BUNDLE_KEY_INDEX, index);
                setResult(RESULT_OK, intent);
                finish();
            }
        }, 1000 * showDurationTime);

        layoutLoadingInfo.setVisibility(View.GONE);
        String info = String.format(getString(R.string.sharedream_sdk_wifi_check_detail_close_after_second), showDurationTime + 3);
        appendDetail(info);
        isChecking = false;
    }

    private void startThread2UploadPortals() {
        /*if (listApPortalJson != null) {
            new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(1000 * 2); // 延长2秒时间，尽可能确保WiFi通畅
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					for (String json : listApPortalJson) {
						CmdUtil.sendRequest(json, new CmdListener() {
							@Override
							public void onCmdExecuted(String responseResult) {
								CmdUtil.convertJson2Object(responseResult, CmdApPortalAdd.Results.class);
							}

							@Override
							public void onCmdException(Throwable exception) {
								MyLog.error(getClass(), exception);
							}
						});
					}
				}
			}).start();
		}*/
    }

	/*private void startThread2UploadApStatusList() {
        if (listApCheckStatusJson != null) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(1000 * 2); // 延长2秒时间，尽可能确保WiFi通畅
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					for (String json : listApCheckStatusJson) {
						CmdUtil.sendRequest(json, new CmdListener() {
							@Override
							public void onCmdExecuted(String responseResult) {
								CmdUtil.convertJson2Object(responseResult, CmdApCheck.Results.class);
							}

							@Override
							public void onCmdException(Throwable exception) {
								MyLog.error(getClass(), exception);
							}
						});
					}
				}
			}).start();
		}
	}*/

    public void appendDetail(String detailItem) {
        if (!isChecking)
            return;

        final String FLAG = " ";
        String detail = viewDetail.getText().toString();
        if (detail.trim().length() != 0) {
            detail += "\n";
        }

        String finalDetail = detail + FLAG + detailItem;
        viewDetail.setText(finalDetail);

        if (layoutScroll.getVisibility() != View.VISIBLE) {
            layoutScroll.setVisibility(View.VISIBLE);
        }

        layoutScroll.post(new Runnable() {
            @Override
            public void run() {
                layoutScroll.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    @Override
    public void onWifiNetworkAvailable() {
        appendDetail(getString(R.string.sharedream_sdk_wifi_check_detail_network_available));
        okNetworkId = WiFiManager.getInstance().getCurrentNetworkId();
        wifiVo.setNetworkId(okNetworkId);
        wifiVo.setStatus(Constant.WIFI_STATUS_VIEW);

        //        String realMac = getRealMac();
        //        if (realMac != null) {
        //            wifiVo.setRealMac(realMac.toUpperCase(Locale.CHINA));
        //        }
        //
        //        saveApCheckResult2Sqlite(Constant.AP_STATUS_NORMAL, wifiVo.getRealMac());
        //        saveApInfo2Sqlite();
        checkNextWifiIfNecessary();
    }

    @Override
    public void onWifiNetworkNotAvailable() {
        appendDetail(getString(R.string.sharedream_sdk_wifi_check_detail_network_not_available));
        // showWifiNotAvailableDialog();
    }

    //    private void showWifiNotAvailableDialog() {
    //        if (isFinishing())
    //            return;
    //
    //        final Dialog dialog = new WifiNotAvailableDialog(this, R.style.MyDialogStyleBottom);
    //        dialog.show();
    //        dialog.setCancelable(true);
    //
    //        Window window = dialog.getWindow();
    //        window.setGravity(Gravity.CENTER_VERTICAL);
    //
    //        Button buttonConfirm = (Button) dialog.findViewById(R.id.btn_confirm);
    //        TextView viewContent = (TextView) dialog.findViewById(R.id.tv_content);
    //
    //        viewContent.setText(getString(R.string.sharedream_sdk_dialog_info_wifi_not_available));
    //        buttonConfirm.setOnClickListener(new View.OnClickListener() {
    //            @Override
    //            public void onClick(View v) {
    //                checkNextWifiIfNecessary();
    //                dialog.dismiss();
    //            }
    //        });
    //    }

    @Override
    public void onWifiConnected() {

        if (!isChecking) {
            return;
        }

        appendDetail(getString(R.string.sharedream_sdk_wifi_check_detail_connected));
        if (wifiVo != null) {
            if (wifiVo.getPasswordType() == Constant.WIFI_PASSWORD_TYPE_OPEN) {
                wifiVo.setStatus(Constant.WIFI_STATUS_OPEN);
                okNetworkId = WiFiManager.getInstance().getCurrentNetworkId();
                wifiVo.setNetworkId(okNetworkId);
//                wifiVo.setStatus(Constant.WIFI_STATUS_VIEW);
                checkNextWifiIfNecessary();
                //                checkPortal();
            } else {
                if (checkApPasswordOnly) {
                    //					if (isWifiPasswordVerifying) {
                    //						appendDetail(getString(R.string.wifi_check_detail_verifying_password_again));
                    //						int newNetworkId = WiFiManager.getInstance().connectAp(wifiVo.getSsid(), wifiVo.getPassword(), wifiVo.getPasswordType());
                    //						wifiVo.setNetworkId(newNetworkId);
                    //
                    //					}
                    //					else {
//                    appendDetail(getString(R.string.sharedream_sdk_wifi_check_detail_checking_network_status));
//                    checkIfNetworkAvailable();

                    okNetworkId = WiFiManager.getInstance().getCurrentNetworkId();
                    wifiVo.setNetworkId(okNetworkId);
                    wifiVo.setStatus(Constant.WIFI_STATUS_VIEW);
                    checkNextWifiIfNecessary();
                    //					}
                } else {
                    if (okNetworkId < 1) {
                        okNetworkId = WiFiManager.getInstance().getCurrentNetworkId();
                        wifiVo.setNetworkId(okNetworkId);
                    } else {
                        wifiVo.setNetworkId(WiFiManager.getInstance().getCurrentNetworkId());
                    }
                    wifiVo.setStatus(Constant.WIFI_STATUS_VIEW);

                    //                    String realMac = getRealMac();
                    //                    if (realMac != null) {
                    //                        wifiVo.setRealMac(realMac.toUpperCase(Locale.CHINA));
                    //                    }

                    //                    saveApCheckResult2Sqlite(Constant.AP_STATUS_NORMAL, wifiVo.getRealMac());
                    //                    saveApInfo2Sqlite();
                    //                    checkNextWifiIfNecessary();
                }
            }
        }
    }

    private void checkIfNetworkAvailable() {
        isNetworkAvailable = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                int count = 5;
                final int timeout = 2;

                while (!isNetworkAvailable && count > 0) {
                    new NetworkCheckTask().execute();

                    try {
                        Thread.sleep(1000 * timeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    count--;

                    LogUtils.d(getClass().getSimpleName(), "isNetworkAvailable = " + isNetworkAvailable);
                }

                runOnUiThread(new Runnable() {
                    public void run() {
                        if (isNetworkAvailable) {
                            WifiCheckSubject.getInstance().notifyWifiNetworkAvailable();
                        } else {
                            WifiCheckSubject.getInstance().notifyWifiNetworkNotAvailable();
                        }
                    }
                });
            }
        }).start();
    }

    //    private String getRealMac() {
    //        final int TOTAL_TIMES = 10;
    //        int count = TOTAL_TIMES;
    //        String realMac = null;
    //        while ((realMac == null || realMac.equals("00:00:00:00:00:00")) && count > 0) {
    //            try {
    //                Thread.sleep(500);
    //            } catch (InterruptedException e) {
    //                e.printStackTrace();
    //            }
    //
    //            count--;
    //            realMac = MyUtils.getRealMac(getApplicationContext());
    //            LogUtils.d(getClass().getSimpleName(), (TOTAL_TIMES - count) + " --> real mac from system file: " + realMac);
    //        }
    //        return realMac;
    //    }
    //
    //    private void saveApCheckResult2Sqlite(int status, String realMac) {
    //        int line = DatabaseManager.updateApCheckStatus(wifiVo.getSsid(), wifiVo.getMac(), wifiVo.getRealMac(), status);
    //        if (line < 1) {
    //            ApCheckResultVo vo = new ApCheckResultVo();
    //            vo.setSsid(wifiVo.getSsid());
    //            vo.setMac(wifiVo.getMac());
    //            vo.setRealMac(realMac);
    //            vo.setStatus(status);
    //            vo.setTime(MyUtils.getCurrentDateTime());
    //            DatabaseManager.insertApCheckResult(vo);
    //        }
    //    }
    //
    //    private void saveApInfo2Sqlite() {
    //        if (!checkApPasswordOnly && wifiVo != null) {
    //            int count = DatabaseManager.updateApInfo(wifiVo);
    //            if (count < 1) {
    //                DatabaseManager.insertApInfo(wifiVo);
    //            }
    //        }
    //    }

    private void checkPortal() {
        final String URL = "http://www.baidu.com";
        final String[] WEB_PAGE_KEY = {"t10.baidu.com", "news.baidu.com"};

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                final String response = HttpUtils.sendHttpGetRequest(URL);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response == null) {
                            WifiCheckSubject.getInstance().notifyWifiOpenPortalTimeout();
                            return;
                        }

                        boolean existPortal = true;
                        for (String key : WEB_PAGE_KEY) {
                            if (response.contains(key)) {
                                existPortal = false;
                                break;
                            }
                        }

                        if (existPortal) {
                            WifiCheckSubject.getInstance().notifyWifiPortalReceived(response);
                        } else {
                            WifiCheckSubject.getInstance().notifyWifiNotPortal();
                        }
                    }
                });
            }
        }).start();
    }

    @Override
    public void onWifiConnecting() {
        appendDetail(getString(R.string.sharedream_sdk_wifi_check_detail_connecting));
    }

    @Override
    public void onWifiIpObtaining() {
        appendDetail(getString(R.string.sharedream_sdk_wifi_check_detail_ip_obtaining));
    }

    @Override
    public void onWifiSuspended() {
        appendDetail(getString(R.string.sharedream_sdk_wifi_check_detail_suspended));
        if (wifiVo != null) {
            wifiVo.setStatus(Constant.WIFI_STATUS_UNKNOWN);
        }
        checkNextWifiIfNecessary();
    }

    @Override
    public void onWifiBlocked() {
        appendDetail(getString(R.string.sharedream_sdk_wifi_check_detail_blocked));
        if (wifiVo != null) {
            wifiVo.setStatus(Constant.WIFI_STATUS_UNKNOWN);
        }
        checkNextWifiIfNecessary();
    }

    @Override
    public void onWifiFailed() {
        appendDetail(getString(R.string.sharedream_sdk_wifi_check_detail_failed));
        if (wifiVo != null) {
            wifiVo.setStatus(Constant.WIFI_STATUS_UNKNOWN);
        }
        checkNextWifiIfNecessary();
    }

    @Override
    public void onWifiDisconnected() {
        appendDetail(getString(R.string.sharedream_sdk_wifi_check_detail_disconnected));
    }

    @Override
    public void onWifiNotPortal() {
        appendDetail(getString(R.string.sharedream_sdk_wifi_check_detail_not_portal));
        //saveApInfo2Sqlite();

        if (wifiVo != null) {
            int networkId = wifiVo.getNetworkId();
            WiFiManager.getInstance().disableNetwork(networkId);
        }
        checkNextWifiIfNecessary();
    }

    @Override
    public void onWifiPortalReceived(String portalWebPage) {
        if (listApPortalJson == null) {
            listApPortalJson = new ArrayList<String>();
        }

        //        String jsonApPortal = createApPortalJson(portalWebPage);
        //        listApPortalJson.add(jsonApPortal);

        //saveApInfo2Sqlite();
        if (wifiVo != null) {
            int networkId = wifiVo.getNetworkId();
            WiFiManager.getInstance().disableNetwork(networkId);
        }

        viewInfo.postDelayed(new Runnable() {
            @Override
            public void run() {
                appendDetail(getString(R.string.sharedream_sdk_wifi_check_detail_portal_received));
                checkNextWifiIfNecessary();
            }
        }, 1000);
    }

    //    private String createApPortalJson(String content) {
    //        CmdApPortalAdd.Input apPortalAddInputData = getApInfo(content);
    //        CmdApPortalAdd.Params params = CmdApPortalAdd.createParams(apPortalAddInputData);
    //        String json = CmdUtil.convertObject2Json(params);
    //        return json;
    //    }

    //    private CmdApPortalAdd.Input getApInfo(String portal) {
    //        CmdApPortalAdd.Input input = new CmdApPortalAdd.Input();
    //        if (wifiVo != null) {
    //            input.setSsid(wifiVo.getSsid());
    //            input.setMac(wifiVo.getMac());
    //            input.setRealmac(getRealMac());
    //            input.setFrequency(wifiVo.getFrequency());
    //            input.setPortal(Base64.encodeToString(portal.getBytes(), Base64.DEFAULT));
    //        }
    //        return input;
    //    }

    @Override
    public void onWifiCheckBegin() {
        if (wifiVo != null) {
            countScanTimes = 0;
            String name = wifiVo.getSsid();
            int networkId = wifiVo.getNetworkId();
            int passwordType = wifiVo.getPasswordType();

            LogUtils.d(getClass().getSimpleName(), "begin to check wifi: " + name + " - " + networkId + " / " + passwordType);

            String info = String.format(getString(R.string.sharedream_sdk_wifi_check_detail_begin), name);
            appendDetail(info);
        }
    }

    @Override
    public void onWifiCheckTimeout() {
        if (!isChecking) {
            return;
        }

        appendDetail(getString(R.string.sharedream_sdk_wifi_check_detail_timeout));
        if (wifiVo != null) {
            int networkId = wifiVo.getNetworkId();
            WiFiManager.getInstance().removeNetwork(networkId);
            wifiVo.setStatus(Constant.WIFI_STATUS_UNKNOWN);
        }
        checkNextWifiIfNecessary();
    }

    @Override
    public void onSupplicantAssociated() {
        //		if (curWifi.getPasswordType() != Constant.WIFI_PASSWORD_TYPE_OPEN) {
        //			isWifiPasswordVerifying = true;
        //			wifiPasswordCheckStartTime = System.currentTimeMillis();
        //			new Thread(threadWifiPasswordCheckTimeout).start();
        //		}
    }

    @Override
    public void onSupplicantCompleted() {
        if (wifiVo != null) {
            if (wifiVo.getPasswordType() != Constant.WIFI_PASSWORD_TYPE_OPEN) {
                if (checkApPasswordOnly) {
                    LogUtils.d(getClass().getSimpleName(), wifiVo.getSsid() + " / " + WiFiManager.getInstance().getCurrentSsid());

                    if (wifiVo.getSsid().equals(WiFiManager.getInstance().getCurrentSsid())) {
                        isWifiPasswordVerifying = false;
                        appendDetail(getString(R.string.sharedream_sdk_wifi_check_detail_password_correct));
                    } else {
                        String info = String.format(getString(R.string.sharedream_sdk_wifi_check_detail_connected_wifi_wrong), wifiVo.getSsid());
                        appendDetail(info);
                        finishOnException();
                    }
                } else {
                    isWifiPasswordVerifying = false;
                    appendDetail(getString(R.string.sharedream_sdk_wifi_check_detail_password_correct));
                }
            }
        }
    }

    @Override
    public void onWifiPasswordIncorrect() {
        appendDetail(getString(R.string.sharedream_sdk_wifi_check_detail_password_incorrect));
        if (wifiVo != null) {
            wifiVo.setStatus(Constant.WIFI_STATUS_MAINTAIN);
            WiFiManager.getInstance().removeNetwork(wifiVo.getNetworkId());
        }
        //        saveApInfo2Sqlite();
        //        saveApCheckResult2Sqlite(Constant.AP_STATUS_MAINTAIN, null);
        checkNextWifiIfNecessary();
    }

    @Override
    public void onWifiNeedPassword() {
        appendDetail(getString(R.string.sharedream_sdk_wifi_check_detail_need_password));
        if (wifiVo != null) {
            wifiVo.setStatus(Constant.WIFI_STATUS_EXPLOITABLE);
        }
        //		addApStatus2JsonList(Constant.AP_STATUS_EXPLOITABLE);
        checkNextWifiIfNecessary();
    }

    @Override
    public void onWifiInvisible() {
        appendDetail(getString(R.string.sharedream_sdk_wifi_check_detail_invisible));
        if (wifiVo != null) {
            wifiVo.setStatus(Constant.WIFI_STATUS_UNKNOWN);
        }
        checkNextWifiIfNecessary();
    }

    @Override
    public void onSupplicantVerifyingPassword() {
        isWifiPasswordVerifying = true;
        String info = String.format(getString(R.string.sharedream_sdk_wifi_check_detail_verifying_password), (countDisconnectTimesWhileWifiPasswordVerifying + 1));
        appendDetail(info);
    }

    @Override
    public void onSupplicantDisconnected() {
        if (isWifiPasswordVerifying) {
            countDisconnectTimesWhileWifiPasswordVerifying++;
            if (countDisconnectTimesWhileWifiPasswordVerifying >= 2) {
                isWifiPasswordVerifying = false;
                WifiCheckSubject.getInstance().notifyWifiPasswordIncorrect();
            }
        }
    }

    @Override
    public void onWifiOpenPortalTimeout() {
        if (wifiVo != null) {
            int networkId = wifiVo.getNetworkId();
            WiFiManager.getInstance().disableNetwork(networkId);
        }

        appendDetail(getString(R.string.sharedream_sdk_wifi_check_detail_open_portal_timeout));
        checkNextWifiIfNecessary();
    }

    @Override
    public void onSupplicantScanning() {
        countScanTimes++;

        String info = String.format(getString(R.string.sharedream_sdk_wifi_check_detail_the_scan_time), countScanTimes);
        appendDetail(info);

        if (countScanTimes >= 3) {
            WifiCheckSubject.getInstance().notifyWifiCheckTimeout();
        }
    }

    @Override
    public void onNetworkAvailable() {
        this.isNetworkAvailable = true;
    }

    @Override
    public void onNetworkNotAvailable() {
    }


}
