package com.sharedream.wifiguard.activity;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
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
import com.sharedream.wifiguard.utils.MyUtils;

public class OptimizeWifiActivity extends BaseActivity {
    private ImageView ivOptimizeBg;
    private ImageView ivOptimizeButton;

    private boolean isOptimizing = false;
    private TextView tvOptimizeOption;
    private RelativeLayout rlOptimizeBefore;
    private RelativeLayout rlOptimizeAfter;
    private ImageView ivOptimizeBgReal;
    private TextView tvOptimizeScore;

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, OptimizeWifiActivity.class);
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
        ivOptimizeBg = ((ImageView) findViewById(R.id.iv_optimize_bg));
        ivOptimizeButton = ((ImageView) findViewById(R.id.iv_optimize_btn));
        tvOptimizeOption = ((TextView) findViewById(R.id.tv_optimize_option));
        rlOptimizeBefore = ((RelativeLayout) findViewById(R.id.rl_optimize_before));
        rlOptimizeAfter = ((RelativeLayout) findViewById(R.id.rl_optimize_after));
        ivOptimizeBgReal = ((ImageView) findViewById(R.id.iv_optimize_bg_real));
        tvOptimizeScore = ((TextView) findViewById(R.id.tv_optimize_score));

        ivOptimizeBg.setImageResource(R.drawable.optimization_start_bg_1080p);
        ivOptimizeButton.setImageResource(R.drawable.optimization_start_btn_1080p);
        rlOptimizeBefore.setVisibility(View.VISIBLE);
        rlOptimizeAfter.setVisibility(View.INVISIBLE);
        ivOptimizeBgReal.setVisibility(View.INVISIBLE);
        tvOptimizeOption.setText("点击优化");
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
                    count++;
                    if (count == 5) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MyUtils.showToast("WiFi连接失败，请手动连接WiFi", OptimizeWifiActivity.this);
                                OptimizeWifiActivity.this.finish();
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
        ivOptimizeButton.setOnClickListener(this);
        rlOptimizeBefore.setOnClickListener(this);
        rlOptimizeAfter.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.rl_optimize_before:
                startOptimize();
                break;
            case R.id.rl_optimize_after:
                startOptimize();
                break;
            case R.id.iv_optimize_btn:
                startOptimize();
                break;
        }
    }

    private void startOptimize() {
        boolean wifiConnected = MyUtils.isWifiConnected(AppContext.getContext());
        if(!wifiConnected){
            MyUtils.showToast("WiFi连接失败，请尝试手动连接WiFi",OptimizeWifiActivity.this);
            return;
        }

        if (!isOptimizing) {
            Animation animation = AnimationUtils.loadAnimation(AppContext.getContext(), R.anim.optimize_loading_anim);
            animation.setInterpolator(new LinearInterpolator());
            ivOptimizeBg.startAnimation(animation);

            ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 100);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int percent = (Integer) animation.getAnimatedValue();
                    if (percent < 20) {
                        ivOptimizeButton.setClickable(false);
                        rlOptimizeBefore.setVisibility(View.VISIBLE);
                        rlOptimizeAfter.setVisibility(View.INVISIBLE);
                        ivOptimizeBg.setImageResource(R.drawable.optimization_loading_1080p);
                        ivOptimizeButton.setImageResource(R.drawable.optimization_loading_btn_1080p);
                        tvOptimizeOption.setText("优化WiFi信号");
                    } else if (percent < 50) {
                        ivOptimizeButton.setClickable(false);
                        rlOptimizeBefore.setVisibility(View.VISIBLE);
                        rlOptimizeAfter.setVisibility(View.INVISIBLE);
                        ivOptimizeBg.setImageResource(R.drawable.optimization_loading_1080p);
                        ivOptimizeButton.setImageResource(R.drawable.optimization_loading_btn_1080p);
                        tvOptimizeOption.setText("优化WiFi下载速度");
                    } else if (percent < 70) {
                        ivOptimizeButton.setClickable(false);
                        rlOptimizeBefore.setVisibility(View.VISIBLE);
                        rlOptimizeAfter.setVisibility(View.INVISIBLE);
                        ivOptimizeBg.setImageResource(R.drawable.optimization_loading_1080p);
                        ivOptimizeButton.setImageResource(R.drawable.optimization_loading_btn_1080p);
                        tvOptimizeOption.setText("优化WiFi上传速度");
                    } else if (percent < 100) {
                        ivOptimizeButton.setClickable(false);
                        rlOptimizeBefore.setVisibility(View.VISIBLE);
                        rlOptimizeAfter.setVisibility(View.INVISIBLE);
                        ivOptimizeBg.setImageResource(R.drawable.optimization_loading_1080p);
                        ivOptimizeButton.setImageResource(R.drawable.optimization_loading_btn_1080p);
                        tvOptimizeOption.setText("优化WiFi传输状态");
                    } else {
                        ivOptimizeButton.setClickable(true);
                        rlOptimizeBefore.setVisibility(View.INVISIBLE);
                        rlOptimizeAfter.setVisibility(View.VISIBLE);

                        SpannableString spannable = new SpannableString("100分");
                        spannable.setSpan(new TextAppearanceSpan(getApplicationContext(), R.style.BigFontStyle), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        tvOptimizeScore.setText(spannable);

                        ivOptimizeBg.setImageResource(R.drawable.optimization_end_bg_1080p);
                        ivOptimizeButton.setImageResource(R.drawable.optimization_end_btn_1080p);
                        ivOptimizeBg.clearAnimation();
                    }
                }
            });
            valueAnimator.setDuration(8000);
            valueAnimator.start();

            isOptimizing = true;
        } else {
            ivOptimizeBg.clearAnimation();
            isOptimizing = false;
        }
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_optimize_wifi;
    }

    @Override
    public String getActivityTitle() {
        String title = AppContext.getContext().getResources().getString(R.string.title_activity_wifi_optimize);
        return title;
    }
}
