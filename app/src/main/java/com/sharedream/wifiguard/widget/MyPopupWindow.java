package com.sharedream.wifiguard.widget;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.manager.SystemBarTintManager;

/**
 * Created by gdp on 2016/3/28.
 */
public class MyPopupWindow extends PopupWindow {
    private SystemBarTintManager tintManager;

    public MyPopupWindow(Activity activity, View view, int width, int height, boolean focusable) {
        super(view, width, height, focusable);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            tintManager = new SystemBarTintManager(activity);
            tintManager.setStatusBarTintEnabled(true);
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            tintManager.setStatusBarTintResource(R.color.theme_color);
        }
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            tintManager.setStatusBarTintResource(R.color.more_dialog_bg);
        }
    }
}
