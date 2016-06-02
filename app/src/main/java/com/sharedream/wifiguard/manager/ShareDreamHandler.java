package com.sharedream.wifiguard.manager;

import android.os.Handler;
import android.os.Message;

public class ShareDreamHandler extends Handler {

    public static final int MESSAGE_FLAG_AP_CONNECTION_NOTIFICATION = 0X0001;

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_FLAG_AP_CONNECTION_NOTIFICATION:
                break;
        }
    }

    public void sendMsgWithObjParams(int msgFlag, Object params) {
        sendMessage(Message.obtain(this, msgFlag, params));
    }

}
