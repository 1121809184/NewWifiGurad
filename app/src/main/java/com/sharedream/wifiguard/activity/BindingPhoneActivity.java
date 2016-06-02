package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.sharedream.wifiguard.MainActivity;
import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.http.CmdUserBind;
import com.sharedream.wifiguard.cmdws.CmdCheckLoginState;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.http.MyCmdHttpTask;
import com.sharedream.wifiguard.http.MyCmdUtil;
import com.sharedream.wifiguard.sqlite.DatabaseManager;
import com.sharedream.wifiguard.utils.GlobalField;
import com.sharedream.wifiguard.utils.LogUtils;
import com.sharedream.wifiguard.utils.MyUtils;
import com.sharedream.wifiguard.vo.UserVo;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class BindingPhoneActivity extends BaseActivity {

    private EditText etPhoneNumber;
    private EditText etPassword;
    private ImageView ivClear;
    private ImageView ivEye;
    private EditText etVerifyCode;
    private TextView tvSendSmsTime;
    private Button btnSendSmsCode;
    private Button btnBindingPhone;

    private boolean eyeOpen = false;
    private TimeCount timeCount;

    private MyHandler mHandler = new MyHandler(this);
    private static String openid;
    private static int mode;

    private static class MyHandler extends Handler {
        private final WeakReference<BindingPhoneActivity> mActivity;

        public MyHandler(BindingPhoneActivity activity) {
            this.mActivity = new WeakReference<BindingPhoneActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final BindingPhoneActivity activity = mActivity.get();
            if (activity != null) {
                int event = msg.arg1;
                int result = msg.arg2;
                Object data = msg.obj;
                if (result == SMSSDK.RESULT_COMPLETE) {
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        HashMap<String, Object> phoneMap = (HashMap<String, Object>) data;
                        String phone = (String) phoneMap.get(Constant.SMS_SDK_PHONE);
                        LogUtils.d("phone >>>>> " + phone);
                        String pwd = activity.etPassword.getText().toString().trim();

                        String token = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_TOKEN, null);
                        //UserVo userVo = DatabaseManager.queryUser();
                        if (TextUtils.isEmpty(token)) {
                            return;
                        }

                        String json = "";
                        if (mode == Constant.LOGIN_MODE_WB) {
                            json = CmdUserBind.createRequestJson(token, phone, pwd, "", openid, "");
                            LogUtils.d("weibo request >>>> " + json);

                        } else if (mode == Constant.LOGIN_MODE_WX) {
                            json = CmdUserBind.createRequestJson(token, phone, pwd, "", "", openid);
                            LogUtils.d("weixin request >>>> " + json);
                        }

                        MyCmdUtil.sendRandomTagRequest(Constant.URL_USER_BINDING, json, new MyCmdHttpTask.CmdListener() {
                            @Override
                            public void onCmdExecuted(String responseResult) {
                                if (!TextUtils.isEmpty(responseResult)) {
                                    LogUtils.d("weibo response >>>> " + responseResult);
                                    handleBindPhoneResults(responseResult, activity);
                                }
                            }

                            @Override
                            public void onCmdException(Throwable exception) {

                            }
                        });

                    } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {

                    }
                } else {
                    MyUtils.showToast("验证码错误", activity);
                }
            }
        }
    }

    private static void handleBindPhoneResults(String response, Activity activity) {
        CmdUserBind.Results results = CmdUserBind.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            DatabaseManager.insertUser(openid, "", mode);

            checkLoginStateByThirdLogin(results.data.accessToken, results.data.uid,activity);
            MyUtils.showToast("绑定成功", activity);
        } else if (results.code == -2) {
            DatabaseManager.logout();
            MyUtils.showToast("未登录或登录状态已失效", activity);
        } else if (results.code == -3) {
            DatabaseManager.logout();
            MyUtils.showToast("要绑定的手机号已经存在", activity);
        } else if (results.code == -4) {
            DatabaseManager.logout();
            MyUtils.showToast("手机号已被绑定", activity);
        }
    }

    private static void checkLoginStateByThirdLogin(String accessToken, String uid,final Activity activity) {
        String json = CmdCheckLoginState.createRequestJson(accessToken, uid);
        LogUtils.d("check login state request >>>>> " + json);
        com.sharedream.wifiguard.cmdws.MyCmdUtil.sendRandomTagRequest(Constant.URL_WS_LOGIN_STATE, json, new com.sharedream.wifiguard.cmdws.MyCmdHttpTask.CmdListener() {
            @Override
            public void onCmdExecuted(String responseResult) {
                if (!TextUtils.isEmpty(responseResult)) {
                    LogUtils.d("check login state response >>>>> " + responseResult);
                    handleCheckLoginStateByThirdLoginResults(responseResult,activity);
                }
            }

            @Override
            public void onCmdException(Throwable exception) {
                LogUtils.d("check login state exception >>>>> " + exception.getMessage());
            }
        });
    }

    private static void handleCheckLoginStateByThirdLoginResults(String response,Activity activity) {
        CmdCheckLoginState.Results results = CmdCheckLoginState.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            GlobalField.saveField(AppContext.getContext(), Constant.INTENT_KEY_LOGIN, true);
            MainActivity.launch(activity);
        } else {
            MyUtils.showToast(results.msg, activity);
        }
    }

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, BindingPhoneActivity.class);
        activity.startActivity(intent);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterAllEventHandler();
    }

    @Override
    protected void initAfterSetContentView() {
        enableMoreAction(false);

        initView();
        initData();
        setListener();
    }

    private void initView() {
        etPhoneNumber = ((EditText) findViewById(R.id.et_phone_number));
        etPassword = ((EditText) findViewById(R.id.et_password));
        ivClear = ((ImageView) findViewById(R.id.iv_clear));
        ivEye = ((ImageView) findViewById(R.id.iv_eye));
        etVerifyCode = ((EditText) findViewById(R.id.et_verify_code));
        tvSendSmsTime = ((TextView) findViewById(R.id.tv_send_sms_time));
        btnSendSmsCode = ((Button) findViewById(R.id.btn_send_sms_code));
        btnBindingPhone = ((Button) findViewById(R.id.btn_binding_phone));

        ivClear.setVisibility(View.INVISIBLE);
        ivEye.setImageResource(R.drawable.user_eye_close_con_1080p);

        tvSendSmsTime.setVisibility(View.INVISIBLE);
        btnSendSmsCode.setVisibility(View.VISIBLE);
    }

    private void initData() {
        registerSmsSdk();
        timeCount = new TimeCount(60 * 1000, 1000);

        UserVo userVo = DatabaseManager.queryUser();
        if (userVo != null) {
            openid = userVo.userid;
            mode = userVo.mode;
        }
        DatabaseManager.logout();

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
        ivClear.setOnClickListener(this);
        ivEye.setOnClickListener(this);
        btnSendSmsCode.setOnClickListener(this);
        btnBindingPhone.setOnClickListener(this);

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    ivClear.setVisibility(View.INVISIBLE);
                } else {
                    ivClear.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.iv_clear:
                clearUserInput();
                break;
            case R.id.iv_eye:
                switchPwdState();
                break;
            case R.id.btn_send_sms_code:
                getVerifyCode();
                break;
            case R.id.btn_binding_phone:
                registerAndLogin();
                break;
        }
    }

    private void registerAndLogin() {
        String phone = etPhoneNumber.getText().toString().trim();
        String verifyCode = etVerifyCode.getText().toString().trim();
        String pwd = etPassword.getText().toString().trim();
        boolean isInputOk = checkInputNotNull(phone, pwd, verifyCode);
        if (isInputOk) {
            SMSSDK.submitVerificationCode(Constant.COUNTRY_CODE, phone, verifyCode);
        }
    }

    private boolean checkInputNotNull(String userid, String password, String code) {
        if (TextUtils.isEmpty(userid)) {
            String nullPhone = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_phone_null);
            MyUtils.showToast(nullPhone, BindingPhoneActivity.this);
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            String nullPhone = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_pwd_null);
            MyUtils.showToast(nullPhone, BindingPhoneActivity.this);
            return false;
        } else {
            if (password.length() < 6) {
                MyUtils.showToast("密码不能小于6位", BindingPhoneActivity.this);
                return false;
            }
        }
        if (TextUtils.isEmpty(code)) {
            String nullPhone = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_code_null);
            MyUtils.showToast(nullPhone, BindingPhoneActivity.this);
            return false;
        }
        return true;
    }

    private void getVerifyCode() {
        String phone = etPhoneNumber.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            String nullPhone = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_phone_null);
            MyUtils.showToast(nullPhone, BindingPhoneActivity.this);
        } else {
            Pattern pattern = Pattern.compile("^[1]+[3,5,8]+\\d{9}");
            Matcher matcher = pattern.matcher(phone);
            if (matcher.matches()) {
                timeCount.start();
                SMSSDK.getVerificationCode(Constant.COUNTRY_CODE, phone);
            } else {
                String str = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_phone_err);
                MyUtils.showToast(str, BindingPhoneActivity.this);
            }
        }
    }

    private void clearUserInput() {
        etPassword.setText("");
        ivClear.setVisibility(View.INVISIBLE);
    }

    private void switchPwdState() {
        if (!eyeOpen) {
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            etPassword.setSelection(etPassword.getText().toString().trim().length());
            if (TextUtils.isEmpty(etPassword.getText().toString().trim())) {
                ivClear.setVisibility(View.INVISIBLE);
            }
            ivEye.setImageResource(R.drawable.user_eye_open_icon_1080p);
            eyeOpen = true;
        } else {
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            etPassword.setSelection(etPassword.getText().toString().trim().length());
            if (TextUtils.isEmpty(etPassword.getText().toString().trim())) {
                ivClear.setVisibility(View.INVISIBLE);
            }
            ivEye.setImageResource(R.drawable.user_eye_close_con_1080p);
            eyeOpen = false;
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
        return R.layout.activity_binding_phone;
    }

    @Override
    public String getActivityTitle() {
        String title = AppContext.getContext().getResources().getString(R.string.title_activity_binding_phone);
        return title;
    }
}
