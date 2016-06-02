package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sharedream.wifiguard.MainActivity;
import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.cmdws.CmdCheckLoginState;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.http.CmdUserLogin;
import com.sharedream.wifiguard.http.CmdUserResetPasswd;
import com.sharedream.wifiguard.http.MyCmdHttpTask;
import com.sharedream.wifiguard.http.MyCmdUtil;
import com.sharedream.wifiguard.sqlite.DatabaseManager;
import com.sharedream.wifiguard.utils.GlobalField;
import com.sharedream.wifiguard.utils.LogUtils;
import com.sharedream.wifiguard.utils.MyUtils;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class VerifyMobileActivity extends BaseActivity {
    private EditText etPhoneNumber;
    private EditText etVerifyCode;
    private TextView tvSendSmsTime;
    private Button btnSendSmsCode;
    private Button btnResetPasswd;

    private TimeCount timeCount;
    private MyHandler mHandler = new MyHandler(this);
    private String phone;
    private String verifyCode;
    private String passwd;

    private static class MyHandler extends Handler {
        private final WeakReference<VerifyMobileActivity> mActivity;

        public MyHandler(VerifyMobileActivity activity) {
            this.mActivity = new WeakReference<VerifyMobileActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final VerifyMobileActivity activity = mActivity.get();
            if (activity != null) {
                int event = msg.arg1;
                int result = msg.arg2;
                Object data = msg.obj;
                if (result == SMSSDK.RESULT_COMPLETE) {
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        HashMap<String, Object> phoneMap = (HashMap<String, Object>) data;
                        String phone = (String) phoneMap.get(Constant.SMS_SDK_PHONE);
                        LogUtils.d("verified mobile phone success");
                        //activity.getPasswdFromServer(phone, activity.passwd, activity);
                        ResetPasswdActivity.launch(activity,phone);
                    } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {

                    }
                } else {
                    MyUtils.showToast("erify code error", activity);
                }
            }
        }
    }

    private void getPasswdFromServer(String phone, String passwd, final Activity activity) {
        String json = CmdUserResetPasswd.createRequestJson(phone, passwd);
        LogUtils.d("reset passwd request >>>>> " + phone);
        MyCmdUtil.sendRandomTagRequest(Constant.URL_USER_RESET_PASSWORD, json, new MyCmdHttpTask.CmdListener() {
            @Override
            public void onCmdExecuted(String responseResult) {
                LogUtils.d("reset passwd response >>>>> " + responseResult);
                handleGetPasswdFromServerResults(responseResult, activity);
            }

            @Override
            public void onCmdException(Throwable exception) {
                LogUtils.d("reset passwd exception >>>>> " + exception.getMessage());
            }
        });
    }

    private void handleGetPasswdFromServerResults(String response, Activity activity) {
        CmdUserResetPasswd.Results results = CmdUserResetPasswd.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            DatabaseManager.logout();
            DatabaseManager.insertUser(phone, passwd, Constant.LOGIN_MODE_MB);
            relogin();
        }
    }

    private void relogin() {
        String json = CmdUserLogin.createRequestJson(phone, passwd, "", "", "");
        LogUtils.d("mobile login request >>>>> " + json);
        MyCmdUtil.sendRandomTagRequest(Constant.URL_USER_LOGIN, json, new MyCmdHttpTask.CmdListener() {
            @Override
            public void onCmdExecuted(String responseResult) {
                if (!TextUtils.isEmpty(responseResult)) {
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

    private void handleUserLoginResults(String response) {
        CmdUserLogin.Results results = CmdUserLogin.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
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

    public static void launch(Activity activity, String passwd) {
        Intent intent = new Intent(activity, VerifyMobileActivity.class);
        intent.putExtra(Constant.INTENT_KEY_NEW_PASSWORD, passwd);
        activity.startActivity(intent);
    }

    public static void launch(Activity activity){
        Intent intent = new Intent(activity, VerifyMobileActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterAllEventHandler();
    }

    @Override
    protected void initAfterSetContentView() {
        super.enableMoreAction(false);
        initView();
        initData();
        setListener();
    }

    private void initView() {
        etPhoneNumber = ((EditText) findViewById(R.id.et_phone_number));
        etVerifyCode = ((EditText) findViewById(R.id.et_verify_code));
        tvSendSmsTime = ((TextView) findViewById(R.id.tv_send_sms_time));
        btnSendSmsCode = ((Button) findViewById(R.id.btn_send_sms_code));
        btnResetPasswd = ((Button) findViewById(R.id.btn_reset_passwd));

        registerSmsSdk();
        timeCount = new TimeCount(60 * 1000, 1000);
    }

    private void initData() {
        passwd = getIntent().getStringExtra(Constant.INTENT_KEY_NEW_PASSWORD);
    }

    private void registerSmsSdk() {
        EventHandler eh = new EventHandler() {
            @Override
            public void afterEvent(int event, int result, Object data) {
                Message msg = Message.obtain();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                mHandler.sendMessage(msg);
            }
        };
        SMSSDK.registerEventHandler(eh);
    }

    private void setListener() {
        btnSendSmsCode.setOnClickListener(this);
        btnResetPasswd.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.btn_send_sms_code:
                getVerifyCode();
                break;
            case R.id.btn_reset_passwd:
                resetPasswd();
                break;
        }
    }

    private void resetPasswd() {
        phone = etPhoneNumber.getText().toString().trim();
        verifyCode = etVerifyCode.getText().toString().trim();
        boolean valid = checkValid();
        if (valid) {
            SMSSDK.submitVerificationCode(Constant.COUNTRY_CODE, phone, verifyCode);
        }
    }

    private boolean checkValid() {
        if (TextUtils.isEmpty(phone)) {
            String nullPhone = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_phone_null);
            MyUtils.showToast(nullPhone, Toast.LENGTH_SHORT, this);
            return false;
        } else {
            Pattern p = Pattern.compile("^[1]+[3,5,8,4]+\\d{9}");
            Matcher m = p.matcher(phone);
            if (!m.matches()) {
                String str = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_phone_err);
                MyUtils.showToast(str, Toast.LENGTH_SHORT, this);
                return false;
            }
        }
        if (TextUtils.isEmpty(verifyCode)) {
            String nullCode = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_code_null);
            MyUtils.showToast(nullCode, Toast.LENGTH_SHORT, this);
            return false;
        }
        return true;
    }

    private void getVerifyCode() {
        phone = etPhoneNumber.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            String nullPhone = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_phone_null);
            MyUtils.showToast(nullPhone, this);
        } else {
            Pattern pattern = Pattern.compile("^[1]+[3,5,8]+\\d{9}");
            Matcher matcher = pattern.matcher(phone);
            if (matcher.matches()) {
                timeCount.start();
                SMSSDK.getVerificationCode(Constant.COUNTRY_CODE, phone);
            } else {
                String str = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_phone_err);
                MyUtils.showToast(str, this);
            }
        }
    }

    class TimeCount extends CountDownTimer {

        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            tvSendSmsTime.setVisibility(View.VISIBLE);
            btnSendSmsCode.setVisibility(View.INVISIBLE);
            tvSendSmsTime.setText(millisUntilFinished / 1000 + "s");
        }

        @Override
        public void onFinish() {
            btnSendSmsCode.setVisibility(View.VISIBLE);
            tvSendSmsTime.setVisibility(View.INVISIBLE);
            String state = AppContext.getContext().getResources().getString(R.string.activity_phone_register_time_count);
            btnSendSmsCode.setText(state);
        }
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_verify_mobile;
    }

    @Override
    public String getActivityTitle() {
        return "手机验证";
    }
}
