package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.utils.LogUtils;
import com.sharedream.wifiguard.utils.MyUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ScanWifiActivity extends BaseActivity {
    private ImageView ivShape1;
    private ImageView ivShape2;
    private ImageView ivShape3;
    private ImageView ivWifi1;
    private ImageView ivWifi2;
    private ImageView ivWifi3;
    private ImageView ivWifi4;
    private ImageView ivSmallShape1;
    private ImageView ivSmallShape2;
    private ImageView ivSmallShape3;
    private ImageView ivSmallShape4;
    private ImageView ivSmallShape5;
    private ImageView ivSmallShape6;
    private ImageView ivSmallShape7;
    private ImageView ivSmallShape8;
    private ImageView ivSmallShape9;
    private ImageView ivSmallShape10;
    private TextView tvPercent;
    private TextView tvQuery;
    private TextView tvFalseWifi;
    private SpannableString styleText;

    private WifiManager wifiManager;
    private MyHandler myHandler = new MyHandler(this);
    private static int count;
    private boolean isExit = false;

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, ScanWifiActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initAfterSetContentView() {
        enableMoreAction(false);
        initView();
        initData();
        setListener();
    }

    private void initView() {
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        ivShape1 = (ImageView) findViewById(R.id.iv_shape1);
        ivShape2 = (ImageView) findViewById(R.id.iv_shape2);
        ivShape3 = (ImageView) findViewById(R.id.iv_shape3);
        ivWifi1 = (ImageView) findViewById(R.id.iv_wifi1);
        ivWifi2 = (ImageView) findViewById(R.id.iv_wifi2);
        ivWifi3 = (ImageView) findViewById(R.id.iv_wifi3);
        ivWifi4 = (ImageView) findViewById(R.id.iv_wifi4);
        ivSmallShape1 = (ImageView) findViewById(R.id.iv_small_shape1);
        ivSmallShape2 = (ImageView) findViewById(R.id.iv_small_shape2);
        ivSmallShape3 = (ImageView) findViewById(R.id.iv_small_shape3);
        ivSmallShape4 = (ImageView) findViewById(R.id.iv_small_shape4);
        ivSmallShape5 = (ImageView) findViewById(R.id.iv_small_shape5);
        ivSmallShape6 = (ImageView) findViewById(R.id.iv_small_shape6);
        ivSmallShape7 = (ImageView) findViewById(R.id.iv_small_shape7);
        ivSmallShape8 = (ImageView) findViewById(R.id.iv_small_shape8);
        ivSmallShape9 = (ImageView) findViewById(R.id.iv_small_shape9);
        ivSmallShape10 = (ImageView) findViewById(R.id.iv_small_shape10);
        tvPercent = (TextView) findViewById(R.id.tv_percent);
        tvQuery = (TextView) findViewById(R.id.tv_check);
        tvFalseWifi = (TextView) findViewById(R.id.tv_false_wifi);
    }

    private void initData() {
        final ProgressDialog progressDialog = new ProgressDialog(this,ProgressDialog.THEME_HOLO_LIGHT);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("正在初始化，请稍后.....");
        progressDialog.show();
        new Thread(new Runnable() {
            int count = 0;
            @Override
            public void run() {
                while (true) {
                    SystemClock.sleep(1000);
                    count ++;
                    if(count == 5){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MyUtils.showToast("WiFi连接失败，请手动连接WiFi", ScanWifiActivity.this);
                                ScanWifiActivity.this.finish();
                                progressDialog.dismiss();
                            }
                        });
                        break;
                    }
                    boolean wifiConnected = MyUtils.isWifiConnected(AppContext.getContext());
                    if (wifiConnected) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                startScanWifiLegal();
                                startScanWifiAnimation();
                            }
                        });
                        break;
                    }
                }
            }
        }).start();

    }

    private void startScanWifiAnimation() {
        Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scan_wifi_shap_1_animation);
        ivShape1.startAnimation(animation1);
        ivShape2.postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scan_wifi_shap_1_animation);
                ivShape2.startAnimation(animation2);
            }
        }, 700);
        ivShape3.postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation animation3 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scan_wifi_shap_1_animation);
                ivShape3.startAnimation(animation3);
            }
        }, 1400);

        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(1000);
                final Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scan_wifi_1_animation);
                final Animation animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scan_wifi_2_animation);
                final Animation animation3 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scan_wifi_3_animation);
                final Animation animation4 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scan_wifi_4_animation);
                animation1.setFillAfter(true);
                animation2.setFillAfter(true);
                animation3.setFillAfter(true);
                animation4.setFillAfter(true);
                ivWifi1.post(new Runnable() {
                    @Override
                    public void run() {
                        ivWifi1.startAnimation(animation1);
                        ivWifi2.startAnimation(animation2);
                        ivWifi3.startAnimation(animation3);
                        ivWifi4.startAnimation(animation4);
                    }
                });
            }
        }).start();

        Animation smallAnimation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scan_small_wifi_animation);
        Animation smallAnimation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scan_small_wifi_animation);
        Animation smallAnimation3 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scan_small_wifi_animation);
        Animation smallAnimation4 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scan_small_wifi_animation);
        Animation smallAnimation5 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scan_small_wifi_animation);
        Animation smallAnimation6 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scan_small_wifi_animation);
        Animation smallAnimation7 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scan_small_wifi_animation);
        Animation smallAnimation8 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scan_small_wifi_animation);
        Animation smallAnimation9 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scan_small_wifi_animation);
        Animation smallAnimation10 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scan_small_wifi_animation);
        smallAnimation1.setStartOffset(1500);
        smallAnimation2.setStartOffset(500);
        smallAnimation3.setStartOffset(1000);
        smallAnimation4.setStartOffset(500);
        smallAnimation5.setStartOffset(1000);
        smallAnimation6.setStartOffset(1500);
        smallAnimation7.setStartOffset(1000);
        smallAnimation8.setStartOffset(1500);
        smallAnimation9.setStartOffset(500);
        smallAnimation10.setStartOffset(1000);
        ivSmallShape1.startAnimation(smallAnimation1);
        ivSmallShape2.startAnimation(smallAnimation2);
        ivSmallShape3.startAnimation(smallAnimation3);
        ivSmallShape4.startAnimation(smallAnimation4);
        ivSmallShape5.startAnimation(smallAnimation5);
        ivSmallShape6.startAnimation(smallAnimation6);
        ivSmallShape7.startAnimation(smallAnimation7);
        ivSmallShape8.startAnimation(smallAnimation8);
        ivSmallShape9.startAnimation(smallAnimation9);
        ivSmallShape10.startAnimation(smallAnimation10);
    }

    private ProgressRunnable pr;

    private void startScanWifiLegal() {
        pr = new ProgressRunnable();
        new Thread(pr).start();
    }

    private class ProgressRunnable implements Runnable{
        @Override
        public void run() {
            for (int i = 0; i <= 100; i++) {
                if(isExit){
                    break;
                }
                SystemClock.sleep(100);
                if (i == 0) {
                    styleText = new SpannableString(i + "%");
                    styleText.setSpan(new TextAppearanceSpan(getApplicationContext(), R.style.BigFontStyle), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    myHandler.sendEmptyMessage(0);
                } else if (i == 100) {
                    styleText = new SpannableString(i + "%");
                    styleText.setSpan(new TextAppearanceSpan(getApplicationContext(), R.style.BigFontStyle), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    myHandler.sendEmptyMessage(100);
                } else {
                    styleText = new SpannableString(i + "%");
                    styleText.setSpan(new TextAppearanceSpan(getApplicationContext(), R.style.BigFontStyle), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    myHandler.sendEmptyMessage(99);
                }
            }
        }
    }

    private void setListener() {

    }

    private static class MyHandler extends Handler {
        private final WeakReference<ScanWifiActivity> mActivity;

        public MyHandler(ScanWifiActivity scanWifiActivity) {
            this.mActivity = new WeakReference<ScanWifiActivity>(scanWifiActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            count = 0;
            ScanWifiActivity scanWifiActivity = mActivity.get();
            int value = msg.what;
            if (scanWifiActivity != null) {
                switch (value) {
                    case 0:
                        scanWifiActivity.tvPercent.setText(scanWifiActivity.styleText);
                        break;
                    case 99:
                        scanWifiActivity.tvPercent.setText(scanWifiActivity.styleText);
                        break;
                    case 100:
                        scanWifiActivity.tvPercent.setText(scanWifiActivity.styleText);
                        scanWifiActivity.tvQuery.setVisibility(View.INVISIBLE);
                        scanWifiActivity.wifiManager.startScan();
                        ArrayList<ScanResult> scanResultslist = (ArrayList<ScanResult>) scanWifiActivity.wifiManager.getScanResults();
                        int size = scanResultslist.size();
                        for (int i = 0; i < size; i++) {
                            ScanResult scanResult = scanResultslist.get(i);
                            LogUtils.d("扫描可疑Wifi:ssid >>> " + scanResult.SSID + ",bssid >>> " + scanResult.BSSID);
                            String capabilities = scanResult.capabilities;
                            int security = MyUtils.getWifiSecurityType(capabilities);
                            if (security == 0) {
                                count++;
                            }
                        }
                        if (count != 0) {
                            String wifiCount = AppContext.getContext().getString(R.string.activity_scan_wifi_false_wifi);
                            scanWifiActivity.tvFalseWifi.setVisibility(View.VISIBLE);
                            scanWifiActivity.tvFalseWifi.setText(String.format(wifiCount, count));
                            SuspiciousWifiActivity.launch(scanWifiActivity, count, scanResultslist);
                        } else {
                            String wifiCount = AppContext.getContext().getString(R.string.activity_scan_wifi_false_wifi_no);
                            scanWifiActivity.tvFalseWifi.setVisibility(View.VISIBLE);
                            scanWifiActivity.tvFalseWifi.setText(wifiCount);
                        }
                        break;
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean wifiConnected = MyUtils.isWifiConnected(AppContext.getContext());
        if(wifiConnected){
            startScanWifiAnimation();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        ivShape1.clearAnimation();
        ivShape2.clearAnimation();
        ivShape3.clearAnimation();
        ivWifi1.clearAnimation();
        ivWifi2.clearAnimation();
        ivWifi3.clearAnimation();
        ivWifi4.clearAnimation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isExit = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_FIRST_USER && resultCode == RESULT_OK) {
            startScanWifiLegal();
            tvFalseWifi.setVisibility(View.INVISIBLE);
            count = 0;
        }

    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_scan_wifi;
    }

    @Override
    public String getActivityTitle() {
        String title = AppContext.getContext().getResources().getString(R.string.title_activity_scan_wifi);
        return title;
    }
}
