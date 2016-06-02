package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.listener.LoadingDialogObserver;
import com.sharedream.wifiguard.listener.LoadingDialogSubject;


public class LoadingActivity extends Activity implements LoadingDialogObserver {

    protected TextView viewInfo;
    protected TextView viewDetail;
    protected LinearLayout layoutLoadingInfo;
    private int showTimeBeforeClose;

    public static void launch(Activity activity, Bundle bundle) {
        if (activity == null || bundle == null) {
            return;
        }
        Intent intent = new Intent(activity, LoadingActivity.class);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading);
        getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        viewInfo = (TextView) findViewById(R.id.tv_info);
        viewDetail = (TextView) findViewById(R.id.tv_detail);
        layoutLoadingInfo = (LinearLayout) findViewById(R.id.ll_loading_info);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            showTimeBeforeClose = bundle.getInt(Constant.BUNDLE_KEY_SHOW_TIME_BEFORE_CLOSE);
            String info = bundle.getString(Constant.BUNDLE_KEY_DIALOG_INFO);
            if (info != null) {
                viewInfo.setText(info);
            }
        }

        LoadingDialogSubject.getInstance().registObserver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LoadingDialogSubject.getInstance().unregistObserver(this);
    }

    @Override
    public void onRequestDialogDismiss() {
        if (showTimeBeforeClose > 0) {
            viewInfo.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, showTimeBeforeClose);
        } else {
            finish();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    public void onTitleChanged(String title) {
        viewInfo.setText(title);
    }
}
