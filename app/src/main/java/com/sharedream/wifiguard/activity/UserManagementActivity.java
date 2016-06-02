package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;

public class UserManagementActivity extends BaseActivity {

    private RelativeLayout rlBindingPhone;
    private RelativeLayout rlUpdateSecret;
    private RelativeLayout rlAddUser;

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, UserManagementActivity.class);
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
        rlBindingPhone = ((RelativeLayout) findViewById(R.id.rl_binding_phone));
        rlUpdateSecret = ((RelativeLayout) findViewById(R.id.rl_update_secret));
        rlAddUser = ((RelativeLayout) findViewById(R.id.rl_add_user));
    }

    private void initData() {

    }

    private void setListener() {
        rlBindingPhone.setOnClickListener(this);
        rlUpdateSecret.setOnClickListener(this);
        rlAddUser.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.rl_binding_phone:
                BindingPhoneActivity.launch(UserManagementActivity.this);
                break;
            case R.id.rl_update_secret:
                UpdatePwdActivity.launch(UserManagementActivity.this);
                break;
            case R.id.rl_add_user:
                AddUserActivity.launch(UserManagementActivity.this);
                break;
        }
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_user_management;
    }

    @Override
    public String getActivityTitle() {
        String title = AppContext.getContext().getResources().getString(R.string.title_activity_user_management);
        return title;
    }
}
