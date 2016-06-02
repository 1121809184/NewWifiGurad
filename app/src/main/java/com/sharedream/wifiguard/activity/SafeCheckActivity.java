package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
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
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.listener.DnsObserver;
import com.sharedream.wifiguard.listener.DnsSubject;
import com.sharedream.wifiguard.listener.NetworkCheckObserver;
import com.sharedream.wifiguard.listener.NetworkCheckSubject;
import com.sharedream.wifiguard.listener.WifiCheckSubject;
import com.sharedream.wifiguard.task.DnsTask;
import com.sharedream.wifiguard.task.NetworkCheckTask;
import com.sharedream.wifiguard.utils.Arp;
import com.sharedream.wifiguard.utils.LogUtils;
import com.sharedream.wifiguard.utils.MyUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class SafeCheckActivity extends BaseActivity implements DnsObserver, NetworkCheckObserver {
    private ImageView ivCheckShape;
    private ImageView ivCheckFinsh;
    private ImageView ivLoding1;
    private ImageView ivLoding2;
    private ImageView ivLoding3;
    private ImageView ivLoding4;
    private ImageView ivLoding5;
    private TextView tvCheck;
    private TextView tvPercent;
    private SpannableString styleText;
    private RelativeLayout rlBg;
    private Animation animation1;
    private Animation animation2;
    private Animation animation3;
    private Animation animation4;
    private Animation animation5;

    private MyHandler handler = new MyHandler(this);
    private boolean detect;
    private boolean isNetworkAvailable = false;
    private boolean isFinishIvLoading2;
    private boolean isFinishIvLoading3;
    private List<ImageView> ivList;

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, SafeCheckActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DnsSubject.getInstance().unregistObserver(this);
        NetworkCheckSubject.getInstance().unregistObserver(this);
    }

    @Override
    protected void initAfterSetContentView() {
        enableMoreAction(false);
        initView();
        initData();
        setListener();
    }

    private void initView() {
        ivCheckShape = (ImageView) findViewById(R.id.iv_shape);
        ivCheckFinsh = (ImageView) findViewById(R.id.iv_finish);
        ivLoding1 = (ImageView) findViewById(R.id.iv_loding1);
        ivLoding2 = (ImageView) findViewById(R.id.iv_loding2);
        ivLoding3 = (ImageView) findViewById(R.id.iv_loding3);
        ivLoding4 = (ImageView) findViewById(R.id.iv_loding4);
        ivLoding5 = (ImageView) findViewById(R.id.iv_loding5);
        tvCheck = (TextView) findViewById(R.id.tv_check);
        tvPercent = (TextView) findViewById(R.id.tv_percent);
        rlBg = (RelativeLayout) findViewById(R.id.rl_bg);
    }

    private void initData() {
        ivList = new ArrayList<ImageView>();
        DnsSubject.getInstance().registObserver(this);
        NetworkCheckSubject.getInstance().registObserver(this);

        //判断wifi是否连接
        final ProgressDialog progressDialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_LIGHT);
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
                    count++;
                    if (count == 5) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MyUtils.showToast("WiFi连接失败，请手动连接WiFi", SafeCheckActivity.this);
                                SafeCheckActivity.this.finish();
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
                                startSafeCheck();
                            }
                        });
                        break;
                    }
                }
            }
        }).start();
    }

    private void startSafeCheck() {
        Animation animation = AnimationUtils.loadAnimation(AppContext.getContext(), R.anim.safe_check_loading_anim);
        animation.setInterpolator(new LinearInterpolator());
        ivCheckShape.startAnimation(animation);
        ivCheckShape.postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i <= 100; i++) {
                            SystemClock.sleep(100);
                            if (i == 0) {
                                styleText = new SpannableString(i + "%");
                                styleText.setSpan(new TextAppearanceSpan(getApplicationContext(), R.style.BigFontStyle), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                handler.sendEmptyMessage(0);
                            } else if (i == 100) {
                                finishAllAnimation();
                                styleText = new SpannableString(i + "%");
                                styleText.setSpan(new TextAppearanceSpan(getApplicationContext(), R.style.BigFontStyle), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                handler.sendEmptyMessage(100);
                            } else {
                                styleText = new SpannableString(i + "%");
                                styleText.setSpan(new TextAppearanceSpan(getApplicationContext(), R.style.BigFontStyle), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                handler.sendEmptyMessage(99);
                            }
                        }
                    }
                }).start();
            }
        }, 1000);

        startCheckFiveItem();

        ivLoding1.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkPasswordSafe();
            }
        }, 2000);

        ivLoding2.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkWifiSurfNet();
            }
        }, 4000);

        ivLoding3.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkDns();
            }
        }, 6500);

        ivLoding4.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkArp();
            }
        }, 6000);

        ivLoding5.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkFalseAp();
            }
        }, 10000);
    }

    private void finishAllAnimation() {
        animation2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isFinishIvLoading2 = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animation3.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isFinishIvLoading3 = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        if (!isFinishIvLoading3) {
            DnsSubject.getInstance().unregistObserver(this);
        }
        ivLoding1.post(new Runnable() {
            @Override
            public void run() {
                if (!isFinishIvLoading2) {
                    ivLoding2.clearAnimation();
                    ivLoding2.setImageResource(R.drawable.wrong);
                }
                if (!isFinishIvLoading3) {
                    ivLoding3.clearAnimation();
                    ivLoding3.setImageResource(R.drawable.wrong);
                }
            }
        });
    }

    private void checkDns() {
        final DnsTask dnsTask = new DnsTask(getApplicationContext());
        dnsTask.execute();
    }

    private void checkFalseAp() {
        ivLoding5.clearAnimation();
        if (!isNetworkAvailable) {
            ivLoding5.setImageResource(R.drawable.wrong);
        } else {
            ivLoding5.setImageResource(R.drawable.right);
            ivList.add(ivLoding5);
        }
    }

    private void checkArp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
                Arp arp = new Arp(dhcpInfo);
                detect = arp.detect();
                handler.sendEmptyMessage(1);
            }
        }).start();
    }

    private void checkWifiSurfNet() {
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

    private void checkPasswordSafe() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        wifiManager.startScan();
        List<ScanResult> scanResults = wifiManager.getScanResults();
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        String currentBssid = connectionInfo.getBSSID();
        String capabilities = null;
        for (int i = 0; i < scanResults.size(); i++) {
            ScanResult scanResult = scanResults.get(i);
            String bssid = scanResult.BSSID;
            if (currentBssid.equals(bssid)) {
                capabilities = scanResult.capabilities;
                break;
            }
        }
        int security = MyUtils.getWifiSecurityType(capabilities);
        ivLoding1.clearAnimation();
        if (security == 0) {
            ivLoding1.setImageResource(R.drawable.wrong);
        } else {
            ivLoding1.setImageResource(R.drawable.right);
            ivList.add(ivLoding1);
        }
    }

    private void startCheckFiveItem() {
        animation1 = new AnimationUtils().loadAnimation(getApplicationContext(), R.anim.safe_check_item_loading_animation);
        LinearInterpolator lir1 = new LinearInterpolator();
        animation1.setInterpolator(lir1);
        ivLoding1.startAnimation(animation1);

        animation2 = new AnimationUtils().loadAnimation(getApplicationContext(), R.anim.safe_check_item_loading_animation);
        LinearInterpolator lir2 = new LinearInterpolator();
        animation2.setInterpolator(lir2);
        ivLoding2.startAnimation(animation2);

        animation3 = new AnimationUtils().loadAnimation(getApplicationContext(), R.anim.safe_check_item_loading_animation);
        LinearInterpolator lir3 = new LinearInterpolator();
        animation3.setInterpolator(lir3);
        ivLoding3.startAnimation(animation3);

        animation4 = new AnimationUtils().loadAnimation(getApplicationContext(), R.anim.safe_check_item_loading_animation);
        LinearInterpolator lir4 = new LinearInterpolator();
        animation4.setInterpolator(lir4);
        ivLoding4.startAnimation(animation4);

        animation5 = new AnimationUtils().loadAnimation(getApplicationContext(), R.anim.safe_check_item_loading_animation);
        LinearInterpolator lir5 = new LinearInterpolator();
        animation5.setInterpolator(lir5);
        ivLoding5.startAnimation(animation5);
    }

    private void setListener() {

    }

    @Override
    public void onDnsRelatived(boolean isSuccess1, boolean isSuccess2) {
        ivLoding3.clearAnimation();
        isFinishIvLoading3 = true;
        if (isSuccess1 && isSuccess2) {
            ivLoding3.setImageResource(R.drawable.right);
            ivList.add(ivLoding3);
        } else {
            ivLoding3.setImageResource(R.drawable.wrong);
        }
    }

    @Override
    public void onNetworkAvailable() {
        isFinishIvLoading2 = true;
        ivLoding2.clearAnimation();
        this.isNetworkAvailable = true;
        ivLoding2.setImageResource(R.drawable.right);
        ivList.add(ivLoding2);
    }

    @Override
    public void onNetworkNotAvailable() {
        isFinishIvLoading2 = true;
        ivLoding2.clearAnimation();
        ivLoding2.setImageResource(R.drawable.wrong);
    }

    private void checkResult() {
        String state = "";
        int size = ivList.size();
        ivCheckFinsh.setVisibility(View.VISIBLE);
        if (size == 0) {
            state = AppContext.getContext().getResources().getString(R.string.activity_safe_check_err);
            ivCheckFinsh.setImageResource(R.drawable.safe_red);
            rlBg.setBackgroundColor(getResources().getColor(R.color.safe_check_red));
            SpannableString spannable = new SpannableString("0分");
            spannable.setSpan(new TextAppearanceSpan(getApplicationContext(), R.style.BigFontStyle), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvPercent.setText(spannable);
            tvCheck.setText(state);
        } else if (size == 1) {
            state = AppContext.getContext().getResources().getString(R.string.activity_safe_check_warn_4);
            SpannableString spannable = new SpannableString("20分");
            spannable.setSpan(new TextAppearanceSpan(getApplicationContext(), R.style.BigFontStyle), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvPercent.setText(spannable);
            tvCheck.setText(state);
        } else if (size == 2) {
            state = AppContext.getContext().getResources().getString(R.string.activity_safe_check_warn_3);
            SpannableString spannable = new SpannableString("40分");
            spannable.setSpan(new TextAppearanceSpan(getApplicationContext(), R.style.BigFontStyle), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvPercent.setText(spannable);
            tvCheck.setText(state);
        } else if (size == 3) {
            state = AppContext.getContext().getResources().getString(R.string.activity_safe_check_warn_2);
            SpannableString spannable = new SpannableString("60分");
            spannable.setSpan(new TextAppearanceSpan(getApplicationContext(), R.style.BigFontStyle), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvPercent.setText(spannable);
            tvCheck.setText(state);
        } else if (size == 4) {
            state = AppContext.getContext().getResources().getString(R.string.activity_safe_check_warn_1);
            SpannableString spannable = new SpannableString("80分");
            spannable.setSpan(new TextAppearanceSpan(getApplicationContext(), R.style.BigFontStyle), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvPercent.setText(spannable);
            tvCheck.setText(state);
        } else if (size == 5) {
            state = AppContext.getContext().getResources().getString(R.string.activity_safe_check_ok);
            SpannableString spannable = new SpannableString("100分");
            spannable.setSpan(new TextAppearanceSpan(getApplicationContext(), R.style.BigFontStyle), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvPercent.setText(spannable);
            tvCheck.setText(state);
        }
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_safe_check;
    }

    @Override
    public String getActivityTitle() {
        String title = AppContext.getContext().getResources().getString(R.string.title_activity_safe_check);
        return title;
    }

    private static class MyHandler extends Handler {
        private final WeakReference<SafeCheckActivity> mActivity;

        public MyHandler(SafeCheckActivity safeCheckActivity) {
            this.mActivity = new WeakReference<SafeCheckActivity>(safeCheckActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            SafeCheckActivity activity = mActivity.get();
            int value = msg.what;
            switch (value) {
                case 0:
                    activity.tvPercent.setText(activity.styleText);
                    break;
                case 99:
                    activity.tvPercent.setText(activity.styleText);
                    break;
                case 100:
                    activity.tvPercent.setText(activity.styleText);
                    activity.ivCheckShape.setVisibility(View.INVISIBLE);
                    activity.ivCheckShape.clearAnimation();
                    activity.checkResult();
                    break;
                case 1:
                    activity.ivLoding4.clearAnimation();
                    if (activity.detect) {
                        activity.ivLoding4.setImageResource(R.drawable.right);
                        activity.ivList.add(activity.ivLoding4);
                    } else {
                        activity.ivLoding4.setImageResource(R.drawable.wrong);
                    }
                    break;
            }
        }
    }
}
