package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;

import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.conf.Constant;

public class WifiDisableActivity extends BaseActivity {
    private Button btnOpenWifi;
    private WifiManager wifiManager;
    private static Activity sActivity;
    private String wantToOpen;

    public static void launch(Activity activity, String toOpen) {
        Intent intent = new Intent(activity, WifiDisableActivity.class);
        intent.putExtra(Constant.INTENT_KEY_WIFI_DISABLE_TO, toOpen);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initAfterSetContentView() {
        super.enableMoreAction(false);
        initView();
        initData();


    }

    private void initData() {
        wantToOpen = getIntent().getStringExtra(Constant.INTENT_KEY_WIFI_DISABLE_TO);
    }

    private void initView() {
        wifiManager = (WifiManager) AppContext.getContext().getSystemService(WIFI_SERVICE);
        btnOpenWifi = ((Button) findViewById(R.id.btn_open_wifi));

        final ProgressDialog progressDialog = new ProgressDialog(this,ProgressDialog.THEME_HOLO_LIGHT);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("正在打开WiFi，请稍后.....");

        btnOpenWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
                    wifiManager.setWifiEnabled(true);
                    progressDialog.show();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (true) {
                                SystemClock.sleep(1000);
                                if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {

                                    if (wantToOpen.equals(Constant.OPEN_POLICE_ACTIVITY)) {
                                        PoliceActivity.launch(WifiDisableActivity.this);
                                    } else if (wantToOpen.equals(Constant.OPEN_SCAN_WIFI_ACTIVITY)) {
                                        ScanWifiActivity.launch(WifiDisableActivity.this);
                                    } else if (wantToOpen.equals(Constant.OPEN_SAFE_CHECK_ACTIVITY)) {
                                        SafeCheckActivity.launch(WifiDisableActivity.this);
                                    } else if (wantToOpen.equals(Constant.OPEN_WIFI_SPEED_ACTIVITY)) {
                                        WifiSpeedActivity.launch(WifiDisableActivity.this);
                                    } else if (wantToOpen.equals(Constant.OPEN_OPTIMIZE_WIFI_ACTIVITY)) {
                                        OptimizeWifiActivity.launch(WifiDisableActivity.this);
                                    }

                                    WifiDisableActivity.this.finish();
                                    progressDialog.dismiss();
                                    break;
                                }
                            }
                        }
                    }).start();
                }
            }
        });
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_wifi_disable;
    }

    @Override
    public String getActivityTitle() {
        String title = AppContext.getContext().getResources().getString(R.string.title_activity_wifi_disable);
        return title;
    }

}
