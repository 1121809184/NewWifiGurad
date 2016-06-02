package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.sharedream.wifiguard.MainActivity;
import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.cmdws.CmdAddAp;
import com.sharedream.wifiguard.cmdws.CmdModifyAp;
import com.sharedream.wifiguard.cmdws.MyCmdHttpTask;
import com.sharedream.wifiguard.cmdws.MyCmdUtil;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.listener.WifiObserver;
import com.sharedream.wifiguard.listener.WifiSubject;
import com.sharedream.wifiguard.manager.WiFiManager;
import com.sharedream.wifiguard.utils.GlobalField;
import com.sharedream.wifiguard.utils.LogUtils;
import com.sharedream.wifiguard.utils.MyUtils;
import com.sharedream.wifiguard.vo.WifiConnectVo;

import java.util.ArrayList;
import java.util.List;

public class InputPwdActivity extends BaseActivity {
    private Button btnBindingComplete;
    private EditText etWifiPasswd;
    private String ssid;
    private String bssid;
    private int level;
    private int frequency;
    private int security;
    private int networkId;
    private String pwd;
    private int okNetworkId = 0;
    private int apId;
    private ImageView ivEye;

    private boolean eyeOpen = false;

    public static void launch(Activity activity, int apId, String ssid, String bssid, int level, int frequency, int security, int networkId) {
        Intent intent = new Intent(activity, InputPwdActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(Constant.BUNDLE_KEY_AP_ID, apId);
        bundle.putString(Constant.BUNDLE_KEY_SSID, ssid);
        bundle.putString(Constant.BUNDLE_KEY_BSSID, bssid);
        bundle.putInt(Constant.BUNDLE_KEY_LEVEL, level);
        bundle.putInt(Constant.BUNDLE_KEY_FREQUENCY, frequency);
        bundle.putInt(Constant.BUNDLE_KEY_SECURITY, security);
        bundle.putInt(Constant.BUNDLE_KEY_NETWORK_ID, networkId);
        intent.putExtras(bundle);
        activity.startActivityForResult(intent, Constant.REQUEST_CODE_INPUTPWD);
        AppContext.getContext().addActivity(activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void initAfterSetContentView() {
        super.enableMoreAction(false);
        initView();
        initData();
        setListener();
    }

    private void initView() {
        btnBindingComplete = ((Button) findViewById(R.id.btn_binding_complete));
        etWifiPasswd = ((EditText) findViewById(R.id.et_wifi_passwd));
        ivEye = ((ImageView) findViewById(R.id.iv_eye));
    }

    private void initData() {
        Bundle bundle = getIntent().getExtras();
        apId = bundle.getInt(Constant.BUNDLE_KEY_AP_ID);
        ssid = bundle.getString(Constant.BUNDLE_KEY_SSID);
        bssid = bundle.getString(Constant.BUNDLE_KEY_BSSID);
        level = bundle.getInt(Constant.BUNDLE_KEY_LEVEL);
        frequency = bundle.getInt(Constant.BUNDLE_KEY_FREQUENCY);
        security = bundle.getInt(Constant.BUNDLE_KEY_SECURITY);
        networkId = bundle.getInt(Constant.BUNDLE_KEY_NETWORK_ID);
        LogUtils.d("要关联的wifi >>>>> ssid:" + ssid + ",security:" + security + ",apid:" + apId);
        String pwdFromSpf = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_PASSWORD, null);
        etWifiPasswd.setText(pwdFromSpf);
    }

    private void setListener() {
        btnBindingComplete.setOnClickListener(this);
        ivEye.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.btn_binding_complete:
                startConnectWifi();
                break;
            case R.id.iv_eye:
                switchPwdState();
                break;
        }
    }

    private void switchPwdState() {
        if (!eyeOpen) {
            etWifiPasswd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            etWifiPasswd.setSelection(etWifiPasswd.getText().toString().trim().length());
            ivEye.setImageResource(R.drawable.user_eye_open_icon_1080p);
            eyeOpen = true;
        } else {
            etWifiPasswd.setTransformationMethod(PasswordTransformationMethod.getInstance());
            etWifiPasswd.setSelection(etWifiPasswd.getText().toString().trim().length());
            ivEye.setImageResource(R.drawable.user_eye_close_con_1080p);
            eyeOpen = false;
        }
    }

    private void startConnectWifi() {
        WifiConnectVo wifiConnectVo = new WifiConnectVo();
        pwd = etWifiPasswd.getText().toString().trim();
        if (pwd.length() < 6) {
            MyUtils.showToast(getString(R.string.sharedream_sdk_toast_input_password), AppContext.getContext());
            return;
        } else {
            wifiConnectVo.setSsid(ssid);
            wifiConnectVo.setMac(bssid);
            wifiConnectVo.setPasswordType(security);
            wifiConnectVo.setFrequency(frequency);
            wifiConnectVo.setNetworkId(networkId);
        }
        //WukongWifiManager.getInstance().connectSSID(ssid, pwd, security);
        ArrayList<WifiConnectVo> listWifiConnectVo = new ArrayList<WifiConnectVo>();
        wifiConnectVo.setStatus(Constant.WIFI_STATUS_UNKNOWN); // 必须添加，否则不再进行检测
        wifiConnectVo.setPassword(pwd); // 设置用户输入的密码
        listWifiConnectVo.add(wifiConnectVo);
        MyUtils.showWifiPasswordCheckDialog(getString(R.string.sharedream_sdk_wifi_checking), listWifiConnectVo, 2, this, Constant.REQUEST_CODE_CHECK_WIFI, 0);
    }

    private void bindingWifi() {
        int shopId = GlobalField.restoreFieldInt(AppContext.getContext(), Constant.SP_KEY_SHOP_ID, 0);
        String cityId = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_LOC_CITY_ID, null);
        String place = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_SHOP_PLACE, null);
        float lng = GlobalField.restoreFieldFloat(AppContext.getContext(), Constant.SP_KEY_LOC_LNG, 0);
        float lat = GlobalField.restoreFieldFloat(AppContext.getContext(), Constant.SP_KEY_LOC_LAT, 0);
        //        String json = CmdBindingWifi.createRequestJson(shopId, bssid, ssid, pwd, Integer.parseInt(cityId), level, frequency, security, place, lng, lat);
        //        LogUtils.d("绑定wifi请求json >>>> " + json);
        //        SystemClock.sleep(200);
        //        CmdUtil.sendRandomTagRequest(Constant.URL_CMD_RELEVANCE_MERCHANT_AP, json, new BaseCmdHttpTask.CmdListener() {
        //            @Override
        //            public void onCmdExecuted(String responseResult) {
        //                if (!TextUtils.isEmpty(responseResult)) {
        //                    LogUtils.d("绑定wifi返回json >>>> " + responseResult);
        //                    handleBindingWifiResults(responseResult);
        //                }
        //            }
        //
        //            @Override
        //            public void onCmdException(Exception exception) {
        //                exception.printStackTrace();
        //            }
        //        });
        String accessToken = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_TOKEN, null);
        String json = CmdAddAp.createRequestJson(accessToken, shopId, bssid, ssid, pwd, Integer.parseInt(cityId), level, frequency, security, "", lng, lat);
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
//            //            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_AP_ID, results.data.apId);//保存绑定后的apid
//            if (from_wifi_detail) {
//                AppContext.getContext().finishAllActivity();
//                finish();
//            } else {
//                MainActivity.launch(InputPwdActivity.this);
//            }
//
//        } else {
//            MyUtils.showToast(results.msg, InputPwdActivity.this);
//            if (from_wifi_detail) {
//                AppContext.getContext().finishAllActivity();
//                finish();
//            } else {
//                MainActivity.launch(InputPwdActivity.this);
//            }

            MyUtils.showToast("录入成功", InputPwdActivity.this);
            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_ACCESS_PASSWORD, pwd);
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_input_pwd;
    }

    @Override
    public String getActivityTitle() {
        String title = AppContext.getContext().getResources().getString(R.string.title_activity_input_pwd);
        return title;
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
            LogUtils.i("返回的信息:" + results.msg);
            MyUtils.showToast(results.msg, this);
        } else if (results.code == -2) {
            LogUtils.i("返回的信息1:" + results.msg);
            MyUtils.showToast(results.msg, this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.REQUEST_CODE_CHECK_WIFI) {
            if (data != null) {
                ArrayList<WifiConnectVo> wifiConnectVoList = data.getParcelableArrayListExtra(Constant.BUNDLE_KEY_WIFI_LIST);
                if (wifiConnectVoList != null && wifiConnectVoList.size() > 0) {
                    WifiConnectVo wifiVo = wifiConnectVoList.get(0);
                    int status = wifiVo.getStatus();
                    if (status == Constant.WIFI_STATUS_VIEW) {//连接成功
                        if (apId == 0) {
                            bindingWifi();
                        } else {
                            unbindAp();
                        }
                    } else if (status == Constant.WIFI_STATUS_MAINTAIN) {//连接失败
                        MyUtils.showToast(getString(R.string.sharedream_sdk_wifi_check_detail_password_incorrect), InputPwdActivity.this);
                        WiFiManager.getInstance().removeNetwork(wifiVo.getNetworkId());
                        if (okNetworkId > 0) {
                            WiFiManager.getInstance().connectNetwork(okNetworkId);
                        }
                    } else if (status == Constant.WIFI_STATUS_UNKNOWN) {//连接超时
                        WiFiManager.getInstance().removeNetwork(wifiVo.getNetworkId());
                        if (okNetworkId > 0) {
                            WiFiManager.getInstance().connectNetwork(okNetworkId);
                        }
                    }
                }
            }
        }
    }
}
