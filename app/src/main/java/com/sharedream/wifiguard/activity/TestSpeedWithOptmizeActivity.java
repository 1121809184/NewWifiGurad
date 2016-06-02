package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.task.HttpGetTask;
import com.sharedream.wifiguard.utils.LogUtils;
import com.sharedream.wifiguard.utils.MyUtils;

import org.apache.http.client.HttpClient;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class TestSpeedWithOptmizeActivity extends BaseActivity {
    private ImageView ivBigBg;
    private ImageView ivSmallCircle;
    private ImageView ivMiddleCircle;
    private ImageView ivBigCircle;
    private ImageView ivRocket;
    private ImageView ivBlue1;
    private ImageView ivBlue2;
    private Button btnTest;
    private Button btnOptimize;
    private Timer timer;
    private long lastTotalRxBytes = 0;
    private long lastTimeStamp = 0;
    private long speed;
    private String url = "http://dldir1.qq.com/qqfile/qq/QQ8.1/17283/QQ8.1.exe";
    private ImageView ivPointer;
    private TimerTask task;
    private TextView tvAdd;
    private TextView tvOptimizeScore1;
    private TextView tvOptimizeSameScore1;
    private TextView tvOptimizeScore2;
    private TextView tvOptimizeSameScore2;
    private TextView tvPercentSign;
    private TextView tvSpeed;
    private TextView tvResult;
    private FrameLayout flTestSpeed;
    private FrameLayout flOptmize;
    private LinearLayout llOptmizeResult;
    private int end;
    private boolean isFirst = true;
    private int begin = -120;
    private List<ImageView> ivDownloadList = new ArrayList<ImageView>();
    private List<ImageView> ivUploadList = new ArrayList<ImageView>();
    private long dowanloadSpeed;
    private long uploadSpeed;
    private int blueEnd;
    private int blueFirst;
    private MyHandler handler = new MyHandler(this);

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, TestSpeedWithOptmizeActivity.class);
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
        ivPointer = (ImageView) findViewById(R.id.iv_pointer);
        ivBigBg = (ImageView) findViewById(R.id.iv_big_bg);
        ivSmallCircle = (ImageView) findViewById(R.id.iv_small_circle);
        ivMiddleCircle = (ImageView) findViewById(R.id.iv_middle_circle);
        ivBigCircle = (ImageView) findViewById(R.id.iv_big_circle);
        ivRocket = (ImageView) findViewById(R.id.iv_rocket);
        ivBlue1 = (ImageView) findViewById(R.id.iv_blue1);
        ivBlue2 = (ImageView) findViewById(R.id.iv_blue2);
        btnTest = (Button) findViewById(R.id.btn_test);
        btnOptimize = (Button) findViewById(R.id.btn_optimize);
        tvSpeed = (TextView) findViewById(R.id.tv_speed);
        tvAdd = (TextView) findViewById(R.id.tv_add);
        tvOptimizeScore1 = (TextView) findViewById(R.id.tv_optimize_score1);
        tvOptimizeSameScore1 = (TextView) findViewById(R.id.tv_optimize_same_score1);
        tvOptimizeScore2 = (TextView) findViewById(R.id.tv_optimize_score2);
        tvOptimizeSameScore2 = (TextView) findViewById(R.id.tv_optimize_same_score2);
        tvPercentSign = (TextView) findViewById(R.id.tv_percent_sign);
        llOptmizeResult = (LinearLayout) findViewById(R.id.ll_optmize_result);
        tvResult = (TextView) findViewById(R.id.tv_result);
        flTestSpeed = (FrameLayout) findViewById(R.id.fl_test_speed);
        flOptmize = (FrameLayout) findViewById(R.id.fl_optmize);
    }

    private void initData() {
        RotateAnimation tateAnimation = new RotateAnimation(0, -120, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        tateAnimation.setDuration(500);
        tateAnimation.setFillAfter(true);
        ivPointer.startAnimation(tateAnimation);
    }

    private void setListener() {
        btnTest.setOnClickListener(this);
        btnOptimize.setOnClickListener(this);
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_test_speed_with_optmize;
    }

    @Override
    public String getActivityTitle() {
        return "测速优化";
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        switch (id) {
            case R.id.btn_test:
                btnTest.setEnabled(false);
                flOptmize.setVisibility(View.INVISIBLE);
                flTestSpeed.setVisibility(View.VISIBLE);
                boolean wifiConnected = MyUtils.isWifiConnected(AppContext.getContext());
                if (wifiConnected) {
                    start();
                } else {
                    MyUtils.showToast("WiFi连接失败，请尝试手动连接WiFi", TestSpeedWithOptmizeActivity.this);
                }
                break;
            case R.id.btn_optimize:
                btnTest.setEnabled(false);
                btnOptimize.setEnabled(false);
                flTestSpeed.setVisibility(View.INVISIBLE);
                flOptmize.setVisibility(View.VISIBLE);
                startOptimize();
        }
    }

    private void startOptimize() {
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.optimeze_wifi_animation);
        animation.setFillAfter(true);
        ivSmallCircle.startAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Animation narrowAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.optimeze_narrow_animation);
                narrowAnimation.setFillAfter(true);
                ivSmallCircle.startAnimation(narrowAnimation);
                narrowAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        startRotateWithCircle();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void startRotateWithCircle() {
        Animation middleAnimation = AnimationUtils.loadAnimation(AppContext.getContext(), R.anim.optimize_middle_circle_anim);
        middleAnimation.setInterpolator(new LinearInterpolator());
        ivMiddleCircle.startAnimation(middleAnimation);

        Animation bigAnimation = AnimationUtils.loadAnimation(AppContext.getContext(), R.anim.optimize_big_circle_anim);
        bigAnimation.setInterpolator(new LinearInterpolator());
        ivBigCircle.startAnimation(bigAnimation);

        bigAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startRecovery();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void startRecovery() {
        Animation bigRecoverAnimation = AnimationUtils.loadAnimation(AppContext.getContext(), R.anim.optimize_big_circle_recover_anim);
        bigRecoverAnimation.setInterpolator(new LinearInterpolator());
        ivBigCircle.startAnimation(bigRecoverAnimation);

        bigRecoverAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Animation bigAnimation = AnimationUtils.loadAnimation(AppContext.getContext(), R.anim.optimize_big_circle_scale_anim);
                //bigAnimation.setInterpolator(new LinearInterpolator());
                bigAnimation.setInterpolator(new DecelerateInterpolator());
                bigAnimation.setFillAfter(true);
                ivBigCircle.startAnimation(bigAnimation);

                Animation middleAnimation = AnimationUtils.loadAnimation(AppContext.getContext(), R.anim.optimize_middle_circle_scale_anim);
                //middleAnimation.setInterpolator(new LinearInterpolator());
                middleAnimation.setInterpolator(new DecelerateInterpolator());
                middleAnimation.setFillAfter(true);
                ivMiddleCircle.startAnimation(middleAnimation);

                bigAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        ivBigCircle.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                optimizeFinish();
                            }
                        }, 4000);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void optimizeFinish() {
        Animation smallAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.optimeze_small_circle_animation);
        smallAnimation.setFillAfter(true);
        ivSmallCircle.startAnimation(smallAnimation);

        ivSmallCircle.postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation bigBlueBgAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.optimeze_big_blue_bg_animation);
                bigBlueBgAnimation.setFillAfter(true);
                ivBigBg.startAnimation(bigBlueBgAnimation);

                bigBlueBgAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        Animation narrowAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.optimeze_narrow_animation);
                        narrowAnimation.setFillAfter(true);
                        ivBigBg.startAnimation(narrowAnimation);

                        narrowAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                startRocket();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        }, 700);
    }

    private void startRocket() {
        Animation rocketAnimation = AnimationUtils.loadAnimation(AppContext.getContext(), R.anim.optimeze_rocket_fly_animation);
        rocketAnimation.setInterpolator(new LinearInterpolator());
        rocketAnimation.setFillAfter(true);
        ivRocket.startAnimation(rocketAnimation);

        rocketAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ivRocket.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Animation upAnimation = AnimationUtils.loadAnimation(AppContext.getContext(), R.anim.optimeze_rocket_up_animation);
                        upAnimation.setInterpolator(new LinearInterpolator());
                        upAnimation.setFillAfter(true);
                        ivRocket.startAnimation(upAnimation);

                        startShowOptmizeRelust();
                    }
                }, 500);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void startShowOptmizeRelust() {
        Animation addUpAnimation = AnimationUtils.loadAnimation(AppContext.getContext(), R.anim.optimeze_add_up_animation);
        addUpAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        addUpAnimation.setFillAfter(true);
        tvAdd.startAnimation(addUpAnimation);

        Animation percentUpAnimation = AnimationUtils.loadAnimation(AppContext.getContext(), R.anim.optimeze_percent_up_animation);
        percentUpAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        percentUpAnimation.setFillAfter(true);
        tvPercentSign.startAnimation(percentUpAnimation);

        Animation scoreUpAnimation1 = AnimationUtils.loadAnimation(AppContext.getContext(), R.anim.optimeze_score_up_animation);
        scoreUpAnimation1.setInterpolator(new AccelerateDecelerateInterpolator());
        scoreUpAnimation1.setFillAfter(true);
        tvOptimizeScore1.startAnimation(scoreUpAnimation1);

        tvOptimizeScore2.postDelayed(new Runnable() {
            @Override
            public void run() {
                int number = new Random().nextInt(9) + 1;
                tvOptimizeScore2.setText(number + "");
                Animation scoreUpAnimation2 = AnimationUtils.loadAnimation(AppContext.getContext(), R.anim.optimeze_score_up_animation);
                scoreUpAnimation2.setInterpolator(new AccelerateDecelerateInterpolator());
                scoreUpAnimation2.setFillAfter(true);
                tvOptimizeScore2.startAnimation(scoreUpAnimation2);

                scoreUpAnimation2.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        startScoreup();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        }, 200);

        addUpAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Animation addDownAnimation = AnimationUtils.loadAnimation(AppContext.getContext(), R.anim.optimeze_add_down_animation);
                addDownAnimation.setInterpolator(new LinearInterpolator());
                addDownAnimation.setFillAfter(true);
                tvAdd.startAnimation(addDownAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void startScoreup() {
        tvOptimizeScore2.postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation scoreUpAnimation2 = AnimationUtils.loadAnimation(AppContext.getContext(), R.anim.optimeze_score_up_again_animation);
                scoreUpAnimation2.setInterpolator(new LinearInterpolator());
                scoreUpAnimation2.setFillAfter(true);
                tvOptimizeScore2.startAnimation(scoreUpAnimation2);
            }
        }, 200);

        tvOptimizeScore1.postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation scoreUpAnimation1 = AnimationUtils.loadAnimation(AppContext.getContext(), R.anim.optimeze_score_up_again_animation);
                scoreUpAnimation1.setInterpolator(new LinearInterpolator());
                scoreUpAnimation1.setFillAfter(true);
                tvOptimizeScore1.startAnimation(scoreUpAnimation1);
            }
        }, 500);

        tvOptimizeSameScore2.postDelayed(new Runnable() {
            @Override
            public void run() {
                int number = new Random().nextInt(9) + 1;
                tvOptimizeSameScore2.setText(number + "");
                Animation sameScoreUpAnimation2 = AnimationUtils.loadAnimation(AppContext.getContext(), R.anim.optimeze_score_up_animation);
                sameScoreUpAnimation2.setInterpolator(new LinearInterpolator());
                sameScoreUpAnimation2.setFillAfter(true);
                tvOptimizeSameScore2.startAnimation(sameScoreUpAnimation2);
            }
        }, 700);

        tvOptimizeSameScore1.postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation sameScoreUpAnimation1 = AnimationUtils.loadAnimation(AppContext.getContext(), R.anim.optimeze_score_up_animation);
                sameScoreUpAnimation1.setInterpolator(new LinearInterpolator());
                sameScoreUpAnimation1.setFillAfter(true);
                tvOptimizeSameScore1.startAnimation(sameScoreUpAnimation1);

                btnOptimize.setEnabled(true);
                btnOptimize.setText("重新优化");
            }
        }, 1000);

        tvOptimizeSameScore1.postDelayed(new Runnable() {
            @Override
            public void run() {
                String result = tvOptimizeSameScore2.getText().toString().trim();
                String optmizeResult = getString(R.string.activity_optmize_result, "2" + result);
                tvResult.setText(optmizeResult);
                Animation llResultAnimation = AnimationUtils.loadAnimation(AppContext.getContext(), R.anim.optimeze_linearlayout_up_animation);
                llResultAnimation.setInterpolator(new LinearInterpolator());
                llResultAnimation.setFillAfter(true);
                llOptmizeResult.startAnimation(llResultAnimation);
            }
        }, 1100);
    }

    private void start() {
        begin = -120;
        dowanloadSpeed = 0;
        uploadSpeed = 0;
        btnTest.setEnabled(false);
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
                                    RotateAnimation rotateAnimation = new RotateAnimation(begin, -120, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                    rotateAnimation.setDuration(1000);
                                    rotateAnimation.setFillAfter(true);
                                    ivPointer.startAnimation(rotateAnimation);
                                    startBlue();
                                    task.cancel();

                                    btnTest.setEnabled(true);
                                    btnTest.setText("重新测速");
                                    btnOptimize.setEnabled(true);
                                }
                            }, 8000
        );
    }

    private void startBlue() {
        if (blueFirst > 0) {
            RotateAnimation blue2Animation = new RotateAnimation(blueFirst, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            blue2Animation.setDuration(500);
            blue2Animation.setFillAfter(true);
            ivBlue2.startAnimation(blue2Animation);

            blue2Animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    RotateAnimation blue1Animation = new RotateAnimation(120, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    blue1Animation.setDuration(500);
                    blue1Animation.setFillAfter(true);
                    ivBlue1.startAnimation(blue1Animation);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        } else {
            RotateAnimation blue1Animation = new RotateAnimation(blueFirst, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            blue1Animation.setDuration(1000);
            blue1Animation.setFillAfter(true);
            ivBlue1.startAnimation(blue1Animation);
        }

    }

    private void showDownloadSpeed() {
        long nowTotalRxBytes = getTotalRxBytes();
        long nowTimeStamp = System.currentTimeMillis();
        speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));//毫秒转换

        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;
        handler.sendEmptyMessage(2);//更新界面
    }

    private void startAnimation(double d) {
        /**
         * 前两个参数定义旋转的起始和结束的度数，后两个参数定义圆心的位置
         */
        // Random random = new Random();
        end = getDuShu(d) - 120;
        Log.i("", "********************begin:" + begin + "***负数end:" + end);

        blueEnd = end + 120;
        blueFirst = begin + 120;

        if (isFirst) {
            RotateAnimation rotateAnimation = new RotateAnimation(begin, end, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(1000);
            rotateAnimation.setFillAfter(true);
            ivPointer.startAnimation(rotateAnimation);

            if (blueEnd > 0) {
                RotateAnimation blue2Animation = new RotateAnimation(blueFirst, blueEnd, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                blue2Animation.setDuration(1000);
                blue2Animation.setFillAfter(true);
                ivBlue2.startAnimation(blue2Animation);
                LogUtils.i("TestSpeedWithOptmizeActivity.startAnimation()  #  blueEnd > 0 first");
            } else {
                RotateAnimation blue1Animation = new RotateAnimation(blueFirst, blueEnd, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                blue1Animation.setDuration(1000);
                blue1Animation.setFillAfter(true);
                ivBlue1.startAnimation(blue1Animation);
                LogUtils.i("TestSpeedWithOptmizeActivity.startAnimation()  #  blueEnd <= 0 first");
            }

            begin = end;
            isFirst = false;
        } else {
            RotateAnimation rotateAnimation = new RotateAnimation(begin, end, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(500);
            rotateAnimation.setFillAfter(true);
            ivPointer.startAnimation(rotateAnimation);

            if (blueEnd > 0) {
                if (blueFirst > 0) {
                    RotateAnimation blue2Animation = new RotateAnimation(blueFirst, blueEnd, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    blue2Animation.setDuration(500);
                    blue2Animation.setFillAfter(true);
                    ivBlue2.startAnimation(blue2Animation);
                    LogUtils.i("TestSpeedWithOptmizeActivity.startAnimation()  #  blueEnd > 0");
                } else {
                    RotateAnimation blue1Animation = new RotateAnimation(blueFirst, 120, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    blue1Animation.setDuration(250);
                    blue1Animation.setFillAfter(true);
                    ivBlue1.startAnimation(blue1Animation);

                    blue1Animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            RotateAnimation blue2Animation = new RotateAnimation(120, blueEnd, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                            blue2Animation.setDuration(250);
                            blue2Animation.setFillAfter(true);
                            ivBlue2.startAnimation(blue2Animation);
                            LogUtils.i("TestSpeedWithOptmizeActivity.startAnimation()  #  blueEnd > 0");
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
            } else {
                if (blueFirst > 0) {
                    RotateAnimation blue2Animation = new RotateAnimation(blueFirst, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    blue2Animation.setDuration(250);
                    blue2Animation.setFillAfter(true);
                    ivBlue2.startAnimation(blue2Animation);

                    blue2Animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            RotateAnimation blue1Animation = new RotateAnimation(120, blueEnd, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                            blue1Animation.setDuration(250);
                            blue1Animation.setFillAfter(true);
                            ivBlue1.startAnimation(blue1Animation);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                } else {
                    RotateAnimation blue1Animation = new RotateAnimation(blueFirst, blueEnd, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    blue1Animation.setDuration(500);
                    blue1Animation.setFillAfter(true);
                    ivBlue1.startAnimation(blue1Animation);
                    LogUtils.i("TestSpeedWithOptmizeActivity.startAnimation()  #  blueEnd <= 0");
                }
            }
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
        }
        return (int) a;
    }

    private long getTotalRxBytes() {
        return TrafficStats.getUidRxBytes(getApplicationInfo().uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);//转为KB
    }

    private static class MyHandler extends Handler {
        private final WeakReference<TestSpeedWithOptmizeActivity> mActivity;

        public MyHandler(TestSpeedWithOptmizeActivity testSpeedWithOptmizeActivity) {
            mActivity = new WeakReference<TestSpeedWithOptmizeActivity>(testSpeedWithOptmizeActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            TestSpeedWithOptmizeActivity activity = mActivity.get();
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
}
