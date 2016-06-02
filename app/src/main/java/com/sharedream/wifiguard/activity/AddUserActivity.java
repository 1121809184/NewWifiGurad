package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;

public class AddUserActivity extends BaseActivity {

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, AddUserActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initAfterSetContentView() {
        enableMoreAction(false);

    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_add_user;
    }

    @Override
    public String getActivityTitle() {
        String title = AppContext.getContext().getResources().getString(R.string.title_activity_add_user);
        return title;
    }
}
