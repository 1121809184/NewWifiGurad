package com.sharedream.wifiguard.listener;

import android.view.View;
import android.view.View.OnClickListener;

public abstract class SingleClickListener implements OnClickListener {
    public static final int MIN_CLICK_DELAY_TIME = 1000;
    private long lastClickTime = 0;

    @Override
    public void onClick(View v) {
        long time = System.currentTimeMillis();
        if(time - lastClickTime > MIN_CLICK_DELAY_TIME){
            onSingleClick(v);
        }
        lastClickTime  = time;
    }

    protected abstract void onSingleClick(View view);
}
