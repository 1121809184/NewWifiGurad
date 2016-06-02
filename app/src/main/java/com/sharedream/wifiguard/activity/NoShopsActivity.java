package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.sharedream.wifiguard.MainActivity;
import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.utils.GlobalField;

public class NoShopsActivity extends BaseActivity {

    private Button btnNoShopsAddShop;

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, NoShopsActivity.class);
        activity.startActivity(intent);
        if(!(activity instanceof MainActivity)){
            activity.finish();
        }
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
        btnNoShopsAddShop = ((Button) findViewById(R.id.btn_no_shops_add_shop));
    }

    private void initData() {

    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.btn_no_shops_add_shop:
                GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_NO_SHOPS_ADD, true);
                AddShopActivity.launch(NoShopsActivity.this);
                break;
        }
    }

    private void setListener() {
        btnNoShopsAddShop.setOnClickListener(this);
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_no_shops;
    }

    @Override
    public String getActivityTitle() {
        String title = AppContext.getContext().getResources().getString(R.string.title_activity_verify_center);
        return title;
    }
}
