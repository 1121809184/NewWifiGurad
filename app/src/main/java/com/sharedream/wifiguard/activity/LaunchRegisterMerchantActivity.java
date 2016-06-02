package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.sharedream.wifiguard.MainActivity;
import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.utils.GlobalField;

public class LaunchRegisterMerchantActivity extends BaseActivity {

    private Button btnRegisterShop;

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, LaunchRegisterMerchantActivity.class);
        activity.startActivity(intent);
        activity.finish();
        AppContext.getContext().finishAllActivity();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initAfterSetContentView() {
        super.enableBackAction(false);
        super.enableMoreAction(false);
        initView();
        initData();
        setListener();
    }

    private void initView() {
        btnRegisterShop = ((Button) findViewById(R.id.btn_register_shop));
    }

    private void initData() {

    }

    private void setListener() {
        btnRegisterShop.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.btn_register_shop:
                AddShopActivity.launch(LaunchRegisterMerchantActivity.this);
                break;

        }
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_launch_register_merchant;
    }

    @Override
    public String getActivityTitle() {
        String title = AppContext.getContext().getResources().getString(R.string.title_activity_security);
        return title;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN
                && event.getRepeatCount() == 0) {
            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_FIRST_LAUNCH, true);
            MainActivity.launch(this);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
