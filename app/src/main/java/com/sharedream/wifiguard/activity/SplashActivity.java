package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.Toast;

import com.sharedream.wifiguard.MainActivity;
import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.http.CmdUserLogin;
import com.sharedream.wifiguard.cmdws.CmdCheckLoginState;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.http.MyCmdHttpTask;
import com.sharedream.wifiguard.http.MyCmdUtil;
import com.sharedream.wifiguard.manager.SystemBarTintManager;
import com.sharedream.wifiguard.sqlite.DatabaseManager;
import com.sharedream.wifiguard.utils.GlobalField;
import com.sharedream.wifiguard.utils.LogUtils;
import com.sharedream.wifiguard.utils.MyUtils;
import com.sharedream.wifiguard.vo.UserVo;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends Activity {
    private boolean keep = true;
    private Thread mThread;
    private LoginReceiver loginReceiver;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(AppContext.getContext()).unregisterReceiver(loginReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.theme_color);
        }

        setContentView(R.layout.activity_splash);
        loginReceiver = new LoginReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.sd.wifiguard.login");
        filter.addAction("com.sd.wifiguard.login.msg");
        LocalBroadcastManager.getInstance(AppContext.getContext()).registerReceiver(loginReceiver, filter);
        GlobalField.saveField(AppContext.getContext(), "loginReturn", false);
        boolean loginReturn = GlobalField.restoreFieldBoolean(AppContext.getContext(), "loginReturn", false);
        LogUtils.d("oncreate loginReturn >>> " + loginReturn);

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(2000);
                UserVo userVo = DatabaseManager.queryUser();
                if (userVo == null) {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    SplashActivity.this.startActivity(intent);
                    SplashActivity.this.finish();
                    GlobalField.saveField(AppContext.getContext(), Constant.INTENT_KEY_LOGIN, false);
                } else {
                    if (userVo.mode == Constant.LOGIN_MODE_WB) {
                        //weibo login
                        LocalBroadcastManager.getInstance(AppContext.getContext()).sendBroadcast(new Intent("com.sd.wifiguard.login"));
                        String json = CmdUserLogin.createRequestJson("", "", "", userVo.userid, "");
                        LogUtils.d("weibo login request >>>>> " + json);
                        MyCmdUtil.sendRandomTagRequest(Constant.URL_USER_LOGIN, json, new MyCmdHttpTask.CmdListener() {
                            @Override
                            public void onCmdExecuted(String responseResult) {
                                if (!TextUtils.isEmpty(responseResult)) {
                                    GlobalField.saveField(AppContext.getContext(), "loginReturn", true);
                                    LogUtils.d("weibo login response >>>>> " + responseResult);
                                    handleUserLoginResults(responseResult);
                                }
                            }

                            @Override
                            public void onCmdException(Throwable exception) {

                                LogUtils.d("weibo login onCmdException >>>>> " + exception.getMessage());
                            }
                        });

                    } else if (userVo.mode == Constant.LOGIN_MODE_WX) {
                        //weixin login
                        LocalBroadcastManager.getInstance(AppContext.getContext()).sendBroadcast(new Intent("com.sd.wifiguard.login"));
                        String json = CmdUserLogin.createRequestJson("", "", "", "", userVo.userid);
                        LogUtils.d("weixin login request >>>>> " + json);
                        MyCmdUtil.sendRandomTagRequest(Constant.URL_USER_LOGIN, json, new MyCmdHttpTask.CmdListener() {
                            @Override
                            public void onCmdExecuted(String responseResult) {
                                if (!TextUtils.isEmpty(responseResult)) {
                                    GlobalField.saveField(AppContext.getContext(), "loginReturn", true);
                                    LogUtils.d("weixin login response >>>>> " + responseResult);
                                    handleUserLoginResults(responseResult);
                                }
                            }

                            @Override
                            public void onCmdException(Throwable exception) {

                                LogUtils.d("weixin login onCmdException >>>>> " + exception.getMessage());
                            }
                        });

                    } else if (userVo.mode == Constant.LOGIN_MODE_MB) {
                        //mobile login
                        LocalBroadcastManager.getInstance(AppContext.getContext()).sendBroadcast(new Intent("com.sd.wifiguard.login"));
                        String json = CmdUserLogin.createRequestJson(userVo.userid, userVo.passwd, "", "", "");
                        LogUtils.d("mobile login request >>>>> " + json);
                        MyCmdUtil.sendRandomTagRequest(Constant.URL_USER_LOGIN, json, new MyCmdHttpTask.CmdListener() {
                            @Override
                            public void onCmdExecuted(String responseResult) {
                                if (!TextUtils.isEmpty(responseResult)) {
                                    GlobalField.saveField(AppContext.getContext(), "loginReturn", true);
                                    LogUtils.d("mobile login response >>>>> " + responseResult);
                                    handleUserLoginResults(responseResult);
                                }
                            }

                            @Override
                            public void onCmdException(Throwable exception) {
                                LogUtils.d("mobile login onCmdException >>>>> " + exception.getMessage());
                            }
                        });
                    }
                }
            }
        });

        boolean networkAvailable = MyUtils.isNetworkAvailable(AppContext.getContext());
        if (!networkAvailable) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SystemClock.sleep(3000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MyUtils.showToast("无网络，请打开WiFi或3G/4G网络", AppContext.getContext());
                        }
                    });
                    SplashActivity.this.finish();
                }
            }).start();

        } else {
            mThread.start();
        }
    }

    private void handleUserLoginResults(String response) {
        CmdUserLogin.Results results = CmdUserLogin.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            LogUtils.i("uid:"+results.data.uid);
            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_ACCESS_TOKEN, results.data.accessToken);
            checkLoginStateByThirdLogin(results.data.accessToken, results.data.uid);
        }
    }

    private void checkLoginStateByThirdLogin(String accessToken, String uid) {
        String json = CmdCheckLoginState.createRequestJson(accessToken, uid);
        LogUtils.d("check login state request >>>>> " + json);
        com.sharedream.wifiguard.cmdws.MyCmdUtil.sendRandomTagRequest(Constant.URL_WS_LOGIN_STATE, json, new com.sharedream.wifiguard.cmdws.MyCmdHttpTask.CmdListener() {
            @Override
            public void onCmdExecuted(String responseResult) {
                if (!TextUtils.isEmpty(responseResult)) {
                    LogUtils.d("check login state response >>>>> " + responseResult);
                    handleCheckLoginStateByThirdLoginResults(responseResult);
                }
            }

            @Override
            public void onCmdException(Throwable exception) {
                LogUtils.d("check login state exception >>>>> " + exception.getMessage());
            }
        });
    }

    private void handleCheckLoginStateByThirdLoginResults(String response) {
        CmdCheckLoginState.Results results = CmdCheckLoginState.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            GlobalField.saveField(AppContext.getContext(), Constant.INTENT_KEY_LOGIN, true);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            MyUtils.showToast(results.msg, this);
        }
    }

    private static class LoginReceiver extends BroadcastReceiver {
        private Timer timer = new Timer();
        private int time = 5;
        private Activity mActivity;

        public LoginReceiver(Activity activity) {
            mActivity = activity;
        }

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                time--;
                LogUtils.d("time >>> " + time);
                boolean loginReturn = GlobalField.restoreFieldBoolean(AppContext.getContext(), "loginReturn", false);
                LogUtils.d("loginReturn >>> " + loginReturn);
                if (loginReturn) {
                    timer.cancel();
                } else {
                    if (time == 0) {
                        timer.cancel();
                        LocalBroadcastManager.getInstance(AppContext.getContext()).sendBroadcast(new Intent("com.sd.wifiguard.login.msg"));
                    }
                }
            }
        };

        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.sd.wifiguard.login".equals(intent.getAction())) {
                timer.schedule(task, 1000, 1000);
            } else if ("com.sd.wifiguard.login.msg".equals(intent.getAction())) {
                MyUtils.showToast("登录失败，请检查网络再手动登录", Toast.LENGTH_SHORT, context);
                GlobalField.saveField(AppContext.getContext(), Constant.INTENT_KEY_LOGIN, false);
                Intent mainIntent = new Intent(mActivity, MainActivity.class);
                mActivity.startActivity(mainIntent);
                mActivity.finish();
            }
        }
    }
}
