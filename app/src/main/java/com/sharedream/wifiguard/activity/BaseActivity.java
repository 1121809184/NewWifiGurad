package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.manager.SystemBarTintManager;

public abstract class BaseActivity extends Activity implements View.OnClickListener {
    private ImageView ivBack;
    private TextView tvTitle;
    private ImageView ivMore;
    private ImageView ivMoreClose;
    private TextView tvMoreAddShop;
    private TextView tvMoreLogout;
    public SystemBarTintManager tintManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.theme_color);
        }

        initBeforeSetContentView();
        setContentView(getContentViewId());
        initTitleBar();
        initAfterSetContentView();
    }

    private void initTitleBar() {
        ivBack = ((ImageView) findViewById(R.id.iv_back));
        tvTitle = ((TextView) findViewById(R.id.tv_title));
        ivMore = ((ImageView) findViewById(R.id.iv_more));

        tvTitle.setText(getActivityTitle());
        ivBack.setOnClickListener(this);
        ivMore.setOnClickListener(this);

        enableBackAction(true);
        enableMoreAction(true);
    }

    protected void enableBackAction(boolean enable) {
        if (enable) {
            ivBack.setVisibility(View.VISIBLE);
        } else {
            ivBack.setVisibility(View.GONE);
        }
    }

    protected void enableMoreAction(boolean enable) {
        if (enable) {
            ivMore.setVisibility(View.VISIBLE);
        } else {
            ivMore.setVisibility(View.GONE);
        }
    }

    protected void initBeforeSetContentView() {

    }

    protected void initAfterSetContentView() {

    }

    public abstract int getContentViewId();

    public abstract String getActivityTitle();


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finishActivity();
                break;
        }
    }

    protected void initMoreAction(View view) {
        View popView = View.inflate(AppContext.getContext(), R.layout.dialog_more, null);
        final PopupWindow popupWindow = new PopupWindow(popView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, 0, 0);

        ivMoreClose = ((ImageView) popView.findViewById(R.id.iv_more_icon));
        tvMoreAddShop = ((TextView) popView.findViewById(R.id.tv_more_add_shop));
        tvMoreLogout = ((TextView) popView.findViewById(R.id.tv_more_log_out));

        ivMoreClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        tvMoreAddShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        tvMoreLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    protected void finishActivity() {
        super.finish();
    }
}
