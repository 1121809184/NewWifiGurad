package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.task.HttpGetTask;
import com.sharedream.wifiguard.task.HttpPostTask;
import com.sharedream.wifiguard.utils.MyUtils;

import org.apache.http.client.HttpClient;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WifiSpeedActivity extends BaseActivity {
    private Button btnTest;
    private Timer timer;
    private long lastTotalRxBytes = 0;
    private long lastTotalTxBytes = 0;
    private long lastTimeStamp = 0;
    private long lastTimeStam = 0;
    private long speed;
    private String url = "http://dldir1.qq.com/qqfile/qq/QQ8.1/17283/QQ8.1.exe";
    private ImageView ivPointer;
    private TimerTask task;
    private TextView tvSpeed;
    private int end;
    private boolean isFirst = true;
    private long begin = -120;
    private List<ImageView> ivDownloadList = new ArrayList<ImageView>();
    private List<ImageView> ivUploadList = new ArrayList<ImageView>();
    private int currentDownload = 0;
    private int currentUpload = 0;
    private boolean isDownloadChecked;
    private boolean isUploadChecked;
    private TextView tvDownload;
    private TextView tvUpload;
    private TextView tvDonloadSpeed;
    private TextView tvUploadSpeed;
    private TextView tvDonloadKb;
    private TextView tvUploadKb;
    private LinearLayout llDownloadSpeed;
    private LinearLayout llUploadSpeed;
    private long dowanloadSpeed;
    private long uploadSpeed;
    private ImageView ivBg;
    private ImageView iv;
    private ImageView ivDarkBg;
    private ImageView ivGreen;

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, WifiSpeedActivity.class);
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
        setListener();
    }

    private void initView() {
        btnTest = (Button) findViewById(R.id.btn_test);
        ivPointer = (ImageView) findViewById(R.id.iv_pointer);
        tvSpeed = (TextView) findViewById(R.id.tv_speed);
        tvDownload = (TextView) findViewById(R.id.tv_download);
        tvUpload = (TextView) findViewById(R.id.tv_upload);
        ivBg = (ImageView) findViewById(R.id.iv_bg);
        ivDarkBg = (ImageView) findViewById(R.id.iv_dark_green);
        ivGreen = (ImageView) findViewById(R.id.iv_green);
        llDownloadSpeed = (LinearLayout) findViewById(R.id.ll_download_tabs);
        llUploadSpeed = (LinearLayout) findViewById(R.id.ll_upload_tabs);
        tvDonloadSpeed = (TextView) findViewById(R.id.tv_download_speed);
        tvUploadSpeed = (TextView) findViewById(R.id.tv_upload_speed);
        tvDonloadKb = (TextView) findViewById(R.id.tv_download_kb);
        tvUploadKb = (TextView) findViewById(R.id.tv_upload_kb);


    }

    private void initData() {
        lastTotalRxBytes = getTotalRxBytes();
        lastTotalTxBytes = getTotalTxBytes();
        lastTimeStamp = System.currentTimeMillis();
        lastTimeStam = System.currentTimeMillis();

        RotateAnimation tateAnimation = new RotateAnimation(0, -120, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        tateAnimation.setDuration(500);
        tateAnimation.setFillAfter(true);
        ivPointer.startAnimation(tateAnimation);

        ivDownloadList.add((ImageView) findViewById(R.id.iv_download_tab1));
        ivDownloadList.add((ImageView) findViewById(R.id.iv_download_tab2));
        ivDownloadList.add((ImageView) findViewById(R.id.iv_download_tab3));

        ivUploadList.add((ImageView) findViewById(R.id.iv_upload_tab1));
        ivUploadList.add((ImageView) findViewById(R.id.iv_upload_tab2));
        ivUploadList.add((ImageView) findViewById(R.id.iv_upload_tab3));

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
                    count++;
                    if (count == 5) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MyUtils.showToast("WiFi连接失败，请手动连接WiFi", WifiSpeedActivity.this);
                                WifiSpeedActivity.this.finish();
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
                            }
                        });
                        break;
                    }
                }
            }
        }).start();
    }

    private void setListener() {
        btnTest.setOnClickListener(this);
    }

    private long getTotalRxBytes() {
        return TrafficStats.getUidRxBytes(getApplicationInfo().uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);//转为KB
    }

    private long getTotalTxBytes() {
        return TrafficStats.getUidTxBytes(getApplicationInfo().uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalTxBytes() / 1024);//转为KB
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.btn_test:
                boolean wifiConnected = MyUtils.isWifiConnected(AppContext.getContext());
                if(wifiConnected){
                    start();
                }else{
                    MyUtils.showToast("WiFi连接失败，请尝试手动连接WiFi",WifiSpeedActivity.this);
                }
                break;
        }
    }

    private void start() {
        begin = -120;
        dowanloadSpeed = 0;
        uploadSpeed = 0;
        llDownloadSpeed.setVisibility(View.VISIBLE);
        tvDonloadSpeed.setVisibility(View.INVISIBLE);
        tvDonloadKb.setVisibility(View.INVISIBLE);
        tvDownload.setSelected(true);
        btnTest.setEnabled(false);
        isDownloadChecked = false;
        isFirst = true;
        HttpGetTask httpGetTask = new HttpGetTask(getApplicationContext(), url);
        httpGetTask.execute();

        timer = new Timer();
        if (task != null) {
            task.cancel();
        }

        task = new TimerTask() {
            @Override
            public void run() {
                showDownloadSpeed();
            }
        };
        timer.schedule(task, 500, 1000);

        //初始化
        Animation alphaAnimation = new AlphaAnimation(0.1f, 1.0f);
        alphaAnimation.setDuration(1500);
        alphaAnimation.setFillAfter(true);
        ivDarkBg.startAnimation(alphaAnimation);
        ivGreen.startAnimation(alphaAnimation);

        btnTest.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                HttpClient httpClient = HttpGetTask.getHttpClient();
                                                httpClient.getConnectionManager().shutdown();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }).start();
                                    end = -120;
                                    RotateAnimation rotateAnimation = new RotateAnimation(begin, end, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                    rotateAnimation.setDuration(1000);
                                    rotateAnimation.setFillAfter(true);
                                    //animationSet.addAnimation(rotateAnimation);
                                    ivPointer.startAnimation(rotateAnimation);
                                    task.cancel();
                                    isDownloadChecked = true;
                                    tvDownload.setSelected(false);
                                    llDownloadSpeed.setVisibility(View.INVISIBLE);
                                    tvDonloadSpeed.setVisibility(View.VISIBLE);
                                    tvDonloadKb.setVisibility(View.VISIBLE);
                                    tvDonloadSpeed.setText(dowanloadSpeed + "");
                                    tvSpeed.setText(0 + "");
                                    ivGreen.setVisibility(View.INVISIBLE);


                                    begin = -120;
                                    tvUpload.setSelected(true);
                                    tvUploadKb.setVisibility(View.INVISIBLE);
                                    tvUploadSpeed.setVisibility(View.INVISIBLE);
                                    llUploadSpeed.setVisibility(View.VISIBLE);
                                    isUploadChecked = false;
                                    startTestUploadSpeed();
                                    HttpPostTask httpGetTask = new HttpPostTask(getApplicationContext(), "http://upload.qiniu.com/");
                                    httpGetTask.execute();
                                    timer = new Timer();
                                    if (task != null) {
                                        task.cancel();
                                    }
                                    task = new TimerTask() {
                                        @Override
                                        public void run() {
                                            showUploadSpeed();
                                        }
                                    };
                                    timer.schedule(task, 1500, 1000);
                                    btnTest.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            task.cancel();
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        HttpURLConnection conn = HttpPostTask.getConn();
                                                        conn.disconnect();
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }).start();

                                        }
                                    }, 4500);

                                    btnTest.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            end = -120;
                                            RotateAnimation rotateAnimation = new RotateAnimation(begin, end, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                            rotateAnimation.setDuration(1000);
                                            rotateAnimation.setFillAfter(true);
                                            //animationSet.addAnimation(rotateAnimation);
                                            ivPointer.startAnimation(rotateAnimation);
                                            isUploadChecked = true;
                                            tvUpload.setSelected(false);
                                            llUploadSpeed.setVisibility(View.INVISIBLE);
                                            tvUploadSpeed.setVisibility(View.VISIBLE);
                                            tvUploadKb.setVisibility(View.VISIBLE);
                                            tvUploadSpeed.setText(uploadSpeed + "");
                                            btnTest.setEnabled(true);
                                            tvSpeed.setText(0 + "");
                                            btnTest.setText("重新测速");

                                            //初始化
                                            Animation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
                                            alphaAnimation.setDuration(1000);
                                            alphaAnimation.setFillAfter(true);
                                            ivDarkBg.startAnimation(alphaAnimation);
                                            ivGreen.setVisibility(View.INVISIBLE);

                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        HttpClient httpClient = HttpGetTask.getHttpClient();
                                                        httpClient.getConnectionManager().shutdown();
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }).start();

                                        }
                                    }, 6000);
                                }
                            }, 8000
        );
        startTestDownloadSpeed();
    }


    private void startTestDownloadSpeed() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 1; i < Integer.MAX_VALUE; i++) {
                        if (!isDownloadChecked) {
                            Thread.sleep(200);
                            final int finalI = i;
                            final int b = finalI % 3;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ImageView ivNewTab = ivDownloadList.get(b);
                                    ImageView ivOldTab = ivDownloadList.get(currentDownload);
                                    ivNewTab.setSelected(true);
                                    ivOldTab.setSelected(false);
                                    currentDownload = b;
                                }
                            });
                        } else {
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startTestUploadSpeed() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 1; i < Integer.MAX_VALUE; i++) {
                        if (!isUploadChecked) {
                            Thread.sleep(200);
                            final int finalI = i;
                            final int b = finalI % 3;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ImageView ivNewTab = ivUploadList.get(b);
                                    ImageView ivOldTab = ivUploadList.get(currentUpload);
                                    ivNewTab.setSelected(true);
                                    ivOldTab.setSelected(false);
                                    currentUpload = b;
                                }
                            });
                        } else {
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startAnimation(double d) {
        /**
         * 前两个参数定义旋转的起始和结束的度数，后两个参数定义圆心的位置
         */
        // Random random = new Random();
        end = getDuShu(d) - 120;
        Log.i("", "********************begin:" + begin + "***负数end:" + end);

        if (isFirst) {
            RotateAnimation rotateAnimation = new RotateAnimation(begin, end, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(1000);
            rotateAnimation.setFillAfter(true);
            //animationSet.addAnimation(rotateAnimation);
            ivPointer.startAnimation(rotateAnimation);
            begin = end;
            isFirst = false;
        } else {
            RotateAnimation rotateAnimation = new RotateAnimation(begin, end, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(500);
            rotateAnimation.setFillAfter(true);
            //animationSet.addAnimation(rotateAnimation);
            ivPointer.startAnimation(rotateAnimation);
            begin = end;
        }


    }

    private int getDuShu(double number) {
        double a = 0;
        if (number >= 0 && number <= 200) {
            a = number / 200 * 120;
            Log.i("进入了哪个阶段", "200****" + a + "," + number);
        } else if (number > 200 && number <= 1024) {
            a = (number - 200) / 824 * 60 + 120;
            Log.i("进入了哪个阶段", "1024****" + a + "," + number);
        } else if (number > 1024 && number <= 5120) {
            a = (number - 1024) / 4096 * 60 + 180;
            Log.i("进入了哪个阶段", "5120****" + a + "," + number);
            ivGreen.setVisibility(View.VISIBLE);
        }
        return (int) a;
    }

    private void showUploadSpeed() {
        long nowTotalTxBytes = getTotalTxBytes();
        long nowTimeStamp = System.currentTimeMillis();
        speed = ((nowTotalTxBytes - lastTotalTxBytes) * 1000 / (nowTimeStamp - lastTimeStam));//毫秒转换

        lastTimeStam = nowTimeStamp;
        lastTotalTxBytes = nowTotalTxBytes;
        handler.sendEmptyMessage(3);//更新界面
    }

    private void showDownloadSpeed() {
        long nowTotalRxBytes = getTotalRxBytes();
        long nowTimeStamp = System.currentTimeMillis();
        speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));//毫秒转换

        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;
        handler.sendEmptyMessage(2);//更新界面
    }

    private MyHandler handler = new MyHandler(this);

    private static class MyHandler extends Handler {
        private final WeakReference<WifiSpeedActivity> mActivity;

        public MyHandler(WifiSpeedActivity wifiSpeedActivity) {
            mActivity = new WeakReference<WifiSpeedActivity>(wifiSpeedActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            WifiSpeedActivity activity = mActivity.get();
            int value = msg.what;
            if (activity != null) {
                switch (value) {
                    case 2:
                        activity.startAnimation(Double.parseDouble(activity.speed + ""));
                        activity.tvSpeed.setText(activity.speed + "");
                        if (activity.dowanloadSpeed < activity.speed) {
                            activity.dowanloadSpeed = activity.speed;
                        }
                        break;
                    case 3:
                        activity.startAnimation(Double.parseDouble(activity.speed + ""));
                        activity.tvSpeed.setText(activity.speed + "");
                        if (activity.uploadSpeed < activity.speed) {
                            activity.uploadSpeed = activity.speed;
                        }
                        break;
                }
            }
        }
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_wifi_speed;
    }

    @Override
    public String getActivityTitle() {
        String title = AppContext.getContext().getResources().getString(R.string.title_activity_wifi_speed);
        return title;
    }
}
