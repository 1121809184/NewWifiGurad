package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sharedream.wifiguard.MainActivity;
import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.adapter.WifiListAdapter;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.cmdws.CmdAddAp;
import com.sharedream.wifiguard.cmdws.CmdCheckWifiBind;
import com.sharedream.wifiguard.cmdws.CmdModifyAp;
import com.sharedream.wifiguard.cmdws.MyCmdHttpTask;
import com.sharedream.wifiguard.cmdws.MyCmdUtil;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.dialog.MessageDialog;
import com.sharedream.wifiguard.listener.WifiObserver;
import com.sharedream.wifiguard.listener.WifiSubject;
import com.sharedream.wifiguard.manager.WiFiManager;
import com.sharedream.wifiguard.manager.WukongWifiManager;
import com.sharedream.wifiguard.utils.GlobalField;
import com.sharedream.wifiguard.utils.LogUtils;
import com.sharedream.wifiguard.utils.MyUtils;
import com.sharedream.wifiguard.vo.WifiConnectVo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BindingWifiActivity extends BaseActivity implements WifiObserver, AdapterView.OnItemClickListener {
    private ListView lvWifi;
    private TextView tvShopName;
    private TextView tvShopWifi;
    private LinearLayout llCurrentWifi;

    private List<CmdCheckWifiBind.Data> wifiCheckedList;
    private List<ScanResult> listWifi;
    private WifiListAdapter wifiListAdapter;
    private TextView tvBindingMyWifi;
    private ProgressDialog progressDialog;
    private ImageView ivListNoData;

    private int okNetworkId = 0;
    private int apId;
    private String ssid;
    private String bssid;
    private int level;
    private int frequency;
    private int security;
    private List<WifiConfiguration> listWifiConfiguration;

    public static void launch(Activity activity, String shopName) {
        Intent intent = new Intent(activity, BindingWifiActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(Constant.BUNDLE_KEY_SHOP_NAME, shopName);
        intent.putExtras(bundle);
        activity.startActivity(intent);
        AppContext.getContext().addActivity(activity);
    }

    public static void launchFromGroupBelongs(Activity activity, String shopName, int requestCode) {
        Intent intent = new Intent(activity, BindingWifiActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(Constant.BUNDLE_KEY_SHOP_NAME, shopName);
        intent.putExtras(bundle);
        activity.startActivityForResult(intent,requestCode);
        AppContext.getContext().addActivity(activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WifiSubject.getInstance().unregistObserver(this);
    }

    @Override
    protected void initAfterSetContentView() {
        super.enableMoreAction(false);
        initView();
        initData();
        setListener();
    }

    private void initView() {
        tvShopName = ((TextView) findViewById(R.id.tv_shop_name));
        tvShopWifi = ((TextView) findViewById(R.id.tv_shop_wifi));
        tvBindingMyWifi = ((TextView) findViewById(R.id.tv_binding_my_wifi));
        lvWifi = ((ListView) findViewById(R.id.lv_wifi));
        ivListNoData = ((ImageView) findViewById(R.id.iv_list_no_data));
        llCurrentWifi = (LinearLayout) findViewById(R.id.ll_current_wifi);

        lvWifi.setEmptyView(ivListNoData);
        //lvWifi.setEmptyView(tvTips);
        tvBindingMyWifi.setEnabled(false);
        //progressDialog = new ProgressDialog(BindingWifiActivity.this);
    }

    private void initData() {
        boolean wifiConnected = WukongWifiManager.getInstance().isWifiConnected();
        if (!wifiConnected) {
            llCurrentWifi.setVisibility(View.GONE);
        }

        WifiSubject.getInstance().registObserver(this);
        WukongWifiManager.getInstance().scanWifi();
        Bundle bundle = getIntent().getExtras();
        String shopName = bundle.getString(Constant.BUNDLE_KEY_SHOP_NAME);
        tvShopName.setText(shopName);
        tvShopWifi.setText("");

        progressDialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_LIGHT);
        progressDialog.setMessage("正在扫描WiFi，请稍后......");
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        WifiInfo wifiInfo = WukongWifiManager.getInstance().getConnectionInfo();
        if (wifiInfo.getSSID() != null && wifiInfo.getBSSID() != null) {
            LogUtils.d("wifiinfo >>>>>> " + wifiInfo);
            if (wifiInfo.getSSID().startsWith("\"")) {
                tvShopWifi.setText(getRealString(wifiInfo.getSSID()));
            } else {
                tvShopWifi.setText(wifiInfo.getSSID());
            }
        }
    }

    public String getRealString(String str) {
        if (!TextUtils.isEmpty(str)) {
            int stratIndex = str.indexOf("\"");
            int lastIndex = str.lastIndexOf("\"");
            return str.substring(stratIndex + 1, lastIndex);
        }
        return null;
    }

    private void setListener() {
        lvWifi.setOnItemClickListener(this);
        tvBindingMyWifi.setOnClickListener(this);
        ivListNoData.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.tv_binding_my_wifi:
                bindingCurrentLinkedWifi();
                break;
            case R.id.iv_list_no_data:
                WukongWifiManager.getInstance().openWifi();
                break;
        }
    }

    private void bindingCurrentLinkedWifi() {
        String currentWifiSsid = tvShopWifi.getText().toString().trim();
        CmdCheckWifiBind.Data currentWifi = null;
        for (int i = 0; i < wifiCheckedList.size(); i++) {
            CmdCheckWifiBind.Data data = wifiCheckedList.get(i);
            if (currentWifiSsid.equals(data.ssid)) {
                currentWifi = data;
                break;
            }
        }
        if (currentWifi != null) {
            if (currentWifi.status != -1) {
                showBindingWifiDialog(currentWifi);
            } else {
                if (currentWifi.security == 0) {
                    startConnectWifi();
                } else {
                    InputPwdActivity.launch(BindingWifiActivity.this, currentWifi.apId, currentWifi.ssid, currentWifi.bssid, currentWifi.level, currentWifi.frequency, currentWifi.security, searchNetworkIdFromWifiConfiguration(currentWifi.ssid));
                }
            }
        }
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_binding_wifi;
    }

    @Override
    public String getActivityTitle() {
        String title = AppContext.getContext().getResources().getString(R.string.title_activity_binding_wifi);
        return title;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            boolean wifiEnabled = WukongWifiManager.getInstance().isWifiEnabled();
            if (!wifiEnabled) {
                WukongWifiManager.getInstance().openWifi();
            }
        }
        return super.onTouchEvent(event);
    }

    private void getWifiData(List<ScanResult> listTempResult) {
        //tvBindingMyWifi.setEnabled(true);
        if (listWifi != null) {
            listWifi.clear();
        }
        listWifi = new ArrayList<ScanResult>();

        for (ScanResult scanResult : listTempResult) {
            if (scanResult.SSID == null || scanResult.SSID.trim().length() == 0) {
                continue;
            }
            listWifi.add(scanResult);
        }

        Comparator comp = new Comparator() {
            public int compare(Object o1, Object o2) {
                ScanResult sr1 = (ScanResult) o1;
                ScanResult sr2 = (ScanResult) o2;
                if (sr1.level > sr2.level)
                    return -1;
                else if (sr1.level == sr2.level)
                    return 0;
                else if (sr1.level < sr2.level)
                    return 1;
                return 0;
            }
        };
        Collections.sort(listWifi, comp);

        checkWifiIsBinding(listWifi);
    }

    private void checkWifiIsBinding(List<ScanResult> wifiList) {
        String cityId = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_LOC_CITY_ID, null);
        float lng = GlobalField.restoreFieldFloat(AppContext.getContext(), Constant.SP_KEY_LOC_LNG, 0);
        float lat = GlobalField.restoreFieldFloat(AppContext.getContext(), Constant.SP_KEY_LOC_LAT, 0);
        //        List<CmdCheckWifiBinding.WifiDetail> wifiDetailList = new ArrayList<CmdCheckWifiBinding.WifiDetail>();
        //        for (int i = 0; i < wifiList.size(); i++) {
        //            ScanResult scanResult = wifiList.get(i);
        //            CmdCheckWifiBinding.WifiDetail wifiDetail = new CmdCheckWifiBinding.WifiDetail();
        //            wifiDetail.bssid = scanResult.BSSID;
        //            wifiDetail.ssid = scanResult.SSID;
        //            //wifiDetail.passwd = "";
        //            wifiDetail.level = scanResult.level;
        //            wifiDetail.frequency = scanResult.frequency;
        //            wifiDetail.security = MyUtils.getWifiSecurityType(scanResult.capabilities);
        //            wifiDetail.lng = lng;
        //            wifiDetail.lat = lat;
        //            wifiDetailList.add(wifiDetail);
        //            LogUtils.d("level >>>>> " + scanResult.level + " ,ssid >>>> " + scanResult.SSID + " ,security >>>> " + wifiDetail.security);
        //        }
        //        String json = CmdCheckWifiBinding.createRequestJson(Integer.parseInt(cityId), wifiDetailList);
        //        LogUtils.d("检测周边ap请求json >>>>> " + json);
        //        CmdUtil.sendRandomTagRequest(Constant.URL_CMD_BUSINESS_CHECK_AP, json, new BaseCmdHttpTask.CmdListener() {
        //            @Override
        //            public void onCmdExecuted(String responseResult) {
        //                if (!TextUtils.isEmpty(responseResult)) {
        //                    LogUtils.d("检测周边ap返回json >>>>> " + responseResult);
        //                    handleCheckWifiBindingResults(responseResult);
        //                }
        //            }
        //
        //            @Override
        //            public void onCmdException(Exception exception) {
        //
        //            }
        //        });

        ArrayList<CmdCheckWifiBind.WifiDetail> myWifiDetailList = new ArrayList<CmdCheckWifiBind.WifiDetail>();
        for (int i = 0; i < wifiList.size(); i++) {
            ScanResult scanResult = wifiList.get(i);
            CmdCheckWifiBind.WifiDetail wifiDetail = new CmdCheckWifiBind.WifiDetail();
            wifiDetail.bssid = scanResult.BSSID;
            wifiDetail.ssid = scanResult.SSID;
            wifiDetail.passwd = "";
            wifiDetail.level = scanResult.level;
            wifiDetail.frequency = scanResult.frequency;
            wifiDetail.security = MyUtils.getWifiSecurityType(scanResult.capabilities);
            wifiDetail.lng = lng;
            wifiDetail.lat = lat;
            myWifiDetailList.add(wifiDetail);
        }
        String accessToken = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_TOKEN, null);
        String json = CmdCheckWifiBind.createRequestJson(accessToken, Integer.parseInt(cityId), myWifiDetailList);
        LogUtils.d("check ap bind request >>>>> " + json);
        MyCmdUtil.sendRandomTagRequest(Constant.URL_WS_CHECK_AP, json, new MyCmdHttpTask.CmdListener() {
            @Override
            public void onCmdExecuted(String responseResult) {
                progressDialog.dismiss();
                if (!TextUtils.isEmpty(responseResult)) {
                    LogUtils.d("check ap bind response >>>>> " + responseResult);
                    handleCheckWifiBindingResults(responseResult);
                }
            }

            @Override
            public void onCmdException(Throwable exception) {
                progressDialog.dismiss();
                MyUtils.showToast("WiFi网络异常", Toast.LENGTH_SHORT, BindingWifiActivity.this);
                LogUtils.d("check ap bind exception >>>>> " + exception.getMessage());
            }
        });
    }

    private void handleCheckWifiBindingResults(String response) {
        //        CmdCheckWifiBinding.Results results = CmdCheckWifiBinding.parseResponseJson(response);
        CmdCheckWifiBind.Results results = CmdCheckWifiBind.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            wifiCheckedList = results.data;
            if (wifiListAdapter == null) {
                wifiListAdapter = new WifiListAdapter(wifiCheckedList);
                lvWifi.setAdapter(wifiListAdapter);
            } else {
                wifiListAdapter.setWifiList(results.data);
                wifiListAdapter.notifyDataSetChanged();
            }
            tvBindingMyWifi.setEnabled(true);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CmdCheckWifiBind.Data data = wifiCheckedList.get(position);
        apId = data.apId;
        ssid = data.ssid;
        bssid = data.bssid;
        level = data.level;
        frequency = data.frequency;
        security = data.security;
        if (data.status != -1) {
            showBindingWifiDialog(data);
        } else {
            if (security == 0) {
                startConnectWifi();
            } else {
                InputPwdActivity.launch(BindingWifiActivity.this, data.apId, data.ssid, data.bssid, data.level, data.frequency, data.security, searchNetworkIdFromWifiConfiguration(data.ssid));
            }
        }
    }

    private void startConnectWifi() {
        WifiConnectVo wifiConnectVo = new WifiConnectVo();
        wifiConnectVo.setSsid(ssid);
        wifiConnectVo.setMac(bssid);
        wifiConnectVo.setPasswordType(security);
        wifiConnectVo.setFrequency(frequency);
        wifiConnectVo.setNetworkId(searchNetworkIdFromWifiConfiguration(ssid));
        //WukongWifiManager.getInstance().connectSSID(ssid, pwd, security);
        ArrayList<WifiConnectVo> listWifiConnectVo = new ArrayList<WifiConnectVo>();
        wifiConnectVo.setStatus(Constant.WIFI_STATUS_UNKNOWN); // 必须添加，否则不再进行检测
        //wifiConnectVo.setPassword(pwd); // 设置用户输入的密码
        listWifiConnectVo.add(wifiConnectVo);
        MyUtils.showWifiPasswordCheckDialog(getString(R.string.sharedream_sdk_wifi_checking), listWifiConnectVo, 2, this, Constant.REQUEST_CODE_CHECK_WIFI, 0);
    }

    private int searchNetworkIdFromWifiConfiguration(String ssid) {
        if (ssid == null) {
            return 0;
        }

        int networkId = 0;
        if (listWifiConfiguration != null) {
            for (WifiConfiguration wifiConfiguration : listWifiConfiguration) {
                if (ssid.equalsIgnoreCase(WukongWifiManager.getInstance().trimQuotation(wifiConfiguration.SSID))) {
                    networkId = wifiConfiguration.networkId;
                    break;
                }
            }
        }
        return networkId;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.REQUEST_CODE_CHECK_WIFI) {
            if (data != null) {
                ArrayList<WifiConnectVo> wifiConnectVoList = data.getParcelableArrayListExtra(Constant.BUNDLE_KEY_WIFI_LIST);
                if (wifiConnectVoList != null && wifiConnectVoList.size() > 0) {
                    WifiConnectVo wifiVo = wifiConnectVoList.get(0);
                    int status = wifiVo.getStatus();
                    if (status == Constant.WIFI_STATUS_OPEN) {
                        if (apId == 0) {
                            bindingWifi();
                        } else {
                            unbindAp();
                        }
                    } else if (status == Constant.WIFI_STATUS_MAINTAIN) {
                        MyUtils.showToast(getString(R.string.sharedream_sdk_wifi_check_detail_password_incorrect), this);
                        WiFiManager.getInstance().removeNetwork(wifiVo.getNetworkId());
                        if (okNetworkId > 0) {
                            WiFiManager.getInstance().connectNetwork(okNetworkId);
                        }
                    } else if (status == Constant.WIFI_STATUS_UNKNOWN) {
                        WiFiManager.getInstance().removeNetwork(wifiVo.getNetworkId());
                        if (okNetworkId > 0) {
                            WiFiManager.getInstance().connectNetwork(okNetworkId);
                        }
                    }
                }
            }
        } else if (requestCode == Constant.REQUEST_CODE_INPUTPWD && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }

    private void showBindingWifiDialog(final CmdCheckWifiBind.Data data) {
        final MessageDialog messageDialog = new MessageDialog(this, R.style.CustomDialogStyle);
        messageDialog.show();
        Window window = messageDialog.getWindow();
        window.setWindowAnimations(R.style.CustomDialogAnimationStyle);
        TextView tvDialogTitle = (TextView) messageDialog.findViewById(R.id.tv_dialog_title);
        TextView tvDialogNotice = (TextView) messageDialog.findViewById(R.id.tv_delete_notice);
        Button tvOk = (Button) messageDialog.findViewById(R.id.tv_ok);
        Button tvCancel = (Button) messageDialog.findViewById(R.id.tv_cancel);

        tvDialogTitle.setText(AppContext.getContext().getResources().getString(R.string.activity_bind_wifi_dialog_title));
        tvDialogNotice.setText(AppContext.getContext().getResources().getString(R.string.activity_bind_wifi_dialog_message));

        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.security == 0) {
                    startConnectWifi();
                } else {
                    InputPwdActivity.launch(BindingWifiActivity.this, data.apId, data.ssid, data.bssid, data.level, data.frequency, data.security, searchNetworkIdFromWifiConfiguration(data.ssid));
                }
                messageDialog.dismiss();
            }
        });

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageDialog.dismiss();
            }
        });
    }

    @Override
    public void onWifiOpen() {

    }

    @Override
    public void onWifiClose() {
        llCurrentWifi.setVisibility(View.GONE);
        wifiCheckedList.clear();
        wifiListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onWifiDisconnected() {

    }

    @Override
    public void onWifiConnecting() {

    }

    @Override
    public void onWifiPasswordVerifying() {

    }

    @Override
    public void onWifiCompleted() {

    }

    @Override
    public void onWifiPasswordIncorrect() {

    }

    @Override
    public void onWifiPasswordCorrect() {

    }

    @Override
    public void onWifiIpObtaining() {

    }

    @Override
    public void onWifiConnected() {
        llCurrentWifi.setVisibility(View.VISIBLE);
        WifiInfo wifiInfo = WukongWifiManager.getInstance().getConnectionInfo();
        if (wifiInfo.getSSID().startsWith("\"")) {
            tvShopWifi.setText(getRealString(wifiInfo.getSSID()));
        } else {
            tvShopWifi.setText(wifiInfo.getSSID());
        }
    }

    @Override
    public void onWifiConnectTimeout() {

    }

    @Override
    public void onWifiScanResultChanged(List<ScanResult> listResult, List<WifiConfiguration> listWifiConfiguration) {
        this.listWifiConfiguration = listWifiConfiguration;
        getWifiData(listResult);
    }

    @Override
    public void onAvailableWifiFound(String ssid) {

    }

    @Override
    public void onAvailableWifiNotFound() {

    }

    private void bindingWifi() {
        int shopId = GlobalField.restoreFieldInt(AppContext.getContext(), Constant.SP_KEY_SHOP_ID, 0);
        String cityId = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_LOC_CITY_ID, null);
        String place = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_SHOP_PLACE, null);
        float lng = GlobalField.restoreFieldFloat(AppContext.getContext(), Constant.SP_KEY_LOC_LNG, 0);
        float lat = GlobalField.restoreFieldFloat(AppContext.getContext(), Constant.SP_KEY_LOC_LAT, 0);
        String accessToken = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_TOKEN, null);
        String json = CmdAddAp.createRequestJson(accessToken, shopId, bssid, ssid, "", Integer.parseInt(cityId), level, frequency, security, "", lng, lat);
        LogUtils.d("bind ap request >>>> " + json);
        SystemClock.sleep(200);
        MyCmdUtil.sendRandomTagRequest(Constant.URL_WS_ADD_AP, json, new MyCmdHttpTask.CmdListener() {
            @Override
            public void onCmdExecuted(String responseResult) {
                if (!TextUtils.isEmpty(responseResult)) {
                    LogUtils.d("bind ap response >>>> " + responseResult);
                    handleBindingWifiResults(responseResult);
                }
            }

            @Override
            public void onCmdException(Throwable exception) {
                LogUtils.d("bind ap exception >>>> " + exception.getMessage());
            }
        });
    }

    private void handleBindingWifiResults(String response) {
        boolean from_wifi_detail = GlobalField.restoreFieldBoolean(AppContext.getContext(), "from_wifi_detail", false);
        //        CmdBindingWifi.Results results = CmdBindingWifi.parseResponseJson(response);
        CmdAddAp.Results results = CmdAddAp.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            //            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_AP_ID, results.data.apId);//保存绑定后的apid
//            if (from_wifi_detail) {
//                AppContext.getContext().finishAllActivity();
//                finish();
//            } else {
//                MainActivity.launch(this);
//            }
//
//        } else {
//            MyUtils.showToast(results.msg, this);
//            if (from_wifi_detail) {
//                AppContext.getContext().finishAllActivity();
//                finish();
//            } else {
//                MainActivity.launch(this);
//            }
            setResult(RESULT_OK);
            finish();
        }
    }

    private void unbindAp() {
        String accessToken = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_TOKEN, null);
        String json = CmdModifyAp.createRequestJson(accessToken, apId);
        LogUtils.d("unbind ap request >>>>> " + json);
        MyCmdUtil.sendRandomTagRequest(Constant.URL_WS_MODIFY_AP, json, new MyCmdHttpTask.CmdListener() {
            @Override
            public void onCmdExecuted(String responseResult) {
                if (!TextUtils.isEmpty(responseResult)) {
                    LogUtils.d("unbind ap response >>>>> " + responseResult);
                    handleUnbindAp(responseResult);
                }
            }

            @Override
            public void onCmdException(Throwable exception) {
                LogUtils.d("unbind ap exception >>>>> " + exception.getMessage());
            }
        });
    }

    private void handleUnbindAp(String response) {
        //        CmdUnbindAp.Results results = CmdUnbindAp.parseResponseJson(response);
        CmdModifyAp.Results results = CmdModifyAp.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            bindingWifi();
        } else if (results.code == -1) {
            MyUtils.showToast(results.msg, this);
        } else if (results.code == -2) {
            MyUtils.showToast(results.msg, this);
        }
    }
}
