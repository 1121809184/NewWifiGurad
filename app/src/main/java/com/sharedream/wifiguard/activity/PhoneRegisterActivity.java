package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sharedream.wifiguard.MainActivity;
import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.http.CmdUserLogin;
import com.sharedream.wifiguard.http.CmdUserRegister;
import com.sharedream.wifiguard.cmdws.CmdCheckLoginState;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.http.MyCmdHttpTask;
import com.sharedream.wifiguard.http.MyCmdUtil;
import com.sharedream.wifiguard.sqlite.DatabaseManager;
import com.sharedream.wifiguard.utils.GlobalField;
import com.sharedream.wifiguard.utils.LogUtils;
import com.sharedream.wifiguard.utils.MyUtils;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class PhoneRegisterActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {
    private EditText etPhoneNumber;
    private EditText etPassword;
    private ImageView ivClear;
    private ImageView ivEye;
    private EditText etVerifyCode;
    private Button btnSendSmsCode;
    private Button btnRegisterAndLogin;
    private TextView tvSendSmsTime;
    private LinearLayout llGoToLogin;
    private RadioGroup rgThirdLoginContainer;

    private boolean eyeOpen = false;
    private TimeCount timeCount;
    private AuthInfo authInfo;
    private SsoHandler weiboSsoHandler;
    private IWXAPI wxapi;
    private RadioButton rbLoginWeibo;
    private RadioButton rbLoginWeixin;
    private MyLoginReceiver myLoginReceiver;
    private MyHandler mHandler = new MyHandler(this);

    private static void handleUserRegisterResults(String response, Activity activity, String phone, String pwd) {
        CmdUserRegister.Results results = CmdUserRegister.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_ACCESS_TOKEN, results.data.accessToken);
            DatabaseManager.insertUser(phone, pwd, Constant.LOGIN_MODE_MB);

            checkLoginState(results.data.accessToken, results.data.uid, activity);
        } else if (results.code == -3) {
            String msg = AppContext.getContext().getResources().getString(R.string.activity_login_phone_exist);
            MyUtils.showToast(msg, activity);
        }
    }

    private static void checkLoginState(String accessToken, String uid, final Activity activity) {
        String json = CmdCheckLoginState.createRequestJson(accessToken, uid);
        LogUtils.d("check login state request >>>>> " + json);
        com.sharedream.wifiguard.cmdws.MyCmdUtil.sendRandomTagRequest(Constant.URL_WS_LOGIN_STATE, json, new com.sharedream.wifiguard.cmdws.MyCmdHttpTask.CmdListener() {
            @Override
            public void onCmdExecuted(String responseResult) {
                if (!TextUtils.isEmpty(responseResult)) {
                    LogUtils.d("check login state response >>>>> " + responseResult);
                    handleCheckLoginStateResults(responseResult, activity);
                }
            }

            @Override
            public void onCmdException(Throwable exception) {

            }
        });
    }

    private static void handleCheckLoginStateResults(String response, Activity activity) {
        CmdCheckLoginState.Results results = CmdCheckLoginState.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            GlobalField.saveField(AppContext.getContext(), Constant.INTENT_KEY_LOGIN, true);
            Intent intent = new Intent(activity, AddShopActivity.class);
            activity.startActivity(intent);
            activity.finish();
        } else {
            MyUtils.showToast(results.msg, activity);
        }
    }

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, PhoneRegisterActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterAllEventHandler();
        LocalBroadcastManager.getInstance(AppContext.getContext()).unregisterReceiver(myLoginReceiver);
    }

    @Override
    protected void initAfterSetContentView() {
        super.enableMoreAction(false);
        authInfo = new AuthInfo(this, Constant.WEIBO_APP_KEY, Constant.WEIBO_REDIRECT_URL, Constant.WEIBO_SCOPE);
        wxapi = WXAPIFactory.createWXAPI(this, Constant.WX_APP_ID);
        wxapi.registerApp(Constant.WX_APP_ID);

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
        btnSendSmsCode = ((Button) findViewById(R.id.btn_send_sms_code));
        btnRegisterAndLogin = ((Button) findViewById(R.id.btn_register_and_login));
        tvSendSmsTime = ((TextView) findViewById(R.id.tv_send_sms_time));
        llGoToLogin = ((LinearLayout) findViewById(R.id.ll_go_to_login));
        rgThirdLoginContainer = ((RadioGroup) findViewById(R.id.rg_third_login_container));
        rbLoginWeibo = ((RadioButton) findViewById(R.id.rb_login_weibo));
        rbLoginWeixin = ((RadioButton) findViewById(R.id.rb_login_weixin));

        ivClear.setVisibility(View.INVISIBLE);
        btnSendSmsCode.setVisibility(View.VISIBLE);
        tvSendSmsTime.setVisibility(View.INVISIBLE);
    }

    private void initData() {
        registerSmsSdk();
        timeCount = new TimeCount(60 * 1000, 1000);
        myLoginReceiver = new MyLoginReceiver();
        LocalBroadcastManager.getInstance(AppContext.getContext()).registerReceiver(myLoginReceiver, new IntentFilter("com.sharedream.wifiguard.weixin.login"));
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
        rgThirdLoginContainer.setOnCheckedChangeListener(this);
        btnRegisterAndLogin.setOnClickListener(this);
        llGoToLogin.setOnClickListener(this);
        btnSendSmsCode.setOnClickListener(this);
        ivClear.setOnClickListener(this);
        ivEye.setOnClickListener(this);
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ivClear.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_login_weixin:
                rbLoginWeibo.setChecked(false);
                wxLogin();
                break;
            case R.id.rb_login_weibo:
                rbLoginWeixin.setChecked(false);
                weiboLogin();
                break;
        }
    }

    private void wxLogin() {
        boolean wxAppInstalledAndSupported = MyUtils.isWXAppInstalledAndSupported();
        if (wxAppInstalledAndSupported) {
            SendAuth.Req req = new SendAuth.Req();
            req.scope = "snsapi_userinfo";
            req.state = "wechat_sdk_demo_test";
            wxapi.sendReq(req);
        } else {
            MyUtils.showToast("抱歉,您的手机还未安装微信客户端", PhoneRegisterActivity.this);
        }
    }

    private void weiboLogin() {
        weiboSsoHandler = new SsoHandler(this, authInfo);
        weiboSsoHandler.authorize(new AuthListener());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (weiboSsoHandler != null) {
            weiboSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    private void loginWeiboFromServer(String openid) {
        String json = CmdUserLogin.createRequestJson("", "", "", openid, "");
        LogUtils.d("weibo login request >>> " + json);
        MyCmdUtil.sendRandomTagRequest(Constant.URL_USER_LOGIN, json, new MyCmdHttpTask.CmdListener() {
            @Override
            public void onCmdExecuted(String responseResult) {
                if (!TextUtils.isEmpty(responseResult)) {
                    LogUtils.d("weibo login response >>> " + responseResult);
                    handleWeiboLoginResults(responseResult);
                }
            }

            @Override
            public void onCmdException(Throwable exception) {

            }
        });
    }

    private void handleWeiboLoginResults(String response) {
        CmdUserLogin.Results results = CmdUserLogin.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_ACCESS_TOKEN, results.data.accessToken);
            checkLoginStateByThirdLogin(results.data.accessToken, results.data.uid);

        } else if (results.code == Constant.SERVER_USER_SYS_USER_INEXISTENCE) {
            String str = AppContext.getContext().getResources().getString(R.string.activity_login_no_user);
            MyUtils.showToast(str, this);
        } else if (results.code == Constant.SERVER_USER_SYS_WRONG_PASSWORD) {
            String str = AppContext.getContext().getResources().getString(R.string.activity_login_wrong_pwd);
            MyUtils.showToast(str, this);
        } else if (results.code == Constant.SERVER_USER_SYS_ACCOUNT_FROZEN) {
            String str = AppContext.getContext().getResources().getString(R.string.activity_login_account_frozen);
            MyUtils.showToast(str, this);
        } else if (results.code == Constant.SERVER_USER_SYS_BINDING_PHONE) {
            String str = AppContext.getContext().getResources().getString(R.string.activity_login_please_binding_phone);
            MyUtils.showToast(str, this);
            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_ACCESS_TOKEN, results.data.accessToken);

            Intent intent = new Intent(this, BindingPhoneActivity.class);
            this.startActivity(intent);
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
            MainActivity.launch(this);
        } else {
            MyUtils.showToast(results.msg, this);
        }
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
            case R.id.btn_register_and_login:
                registerAndLogin();
                break;
            case R.id.ll_go_to_login:
                Intent intent = new Intent(this, LoginActivity.class);
                this.startActivity(intent);
                this.finish();
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

    private void getVerifyCode() {
        String phone = etPhoneNumber.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            String nullPhone = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_phone_null);
            MyUtils.showToast(nullPhone, PhoneRegisterActivity.this);
        } else {
            Pattern pattern = Pattern.compile("^[1]+[3,5,8]+\\d{9}");
            Matcher matcher = pattern.matcher(phone);
            if (matcher.matches()) {
                timeCount.start();
                SMSSDK.getVerificationCode(Constant.COUNTRY_CODE, phone);
            } else {
                String str = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_phone_err);
                MyUtils.showToast(str, PhoneRegisterActivity.this);
            }
        }
    }

    private boolean checkInputNotNull(String userid, String password, String code) {
        if (TextUtils.isEmpty(userid)) {
            String nullPhone = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_phone_null);
            MyUtils.showToast(nullPhone, Toast.LENGTH_SHORT, PhoneRegisterActivity.this);
            return false;
        } else {
            Pattern p = Pattern.compile("^[1]+[3,5,8,4]+\\d{9}");
            Matcher m = p.matcher(userid);
            if (!m.matches()) {
                String str = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_phone_err);
                MyUtils.showToast(str, Toast.LENGTH_SHORT, PhoneRegisterActivity.this);
                return false;
            }
        }

        if (TextUtils.isEmpty(password)) {
            String nullPhone = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_pwd_null);
            MyUtils.showToast(nullPhone, Toast.LENGTH_SHORT, PhoneRegisterActivity.this);
            return false;
        } else {
            if (password.length() < 6) {
                String str = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_pwd_less_six);
                MyUtils.showToast(str, Toast.LENGTH_SHORT, PhoneRegisterActivity.this);
                return false;
            } else {
                Pattern pattern = Pattern.compile("[a-zA-Z0-9]+");
                Matcher matcher = pattern.matcher(password);
                if (!matcher.matches()) {
                    String msg = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_pwd_ok);
                    MyUtils.showToast(msg, Toast.LENGTH_SHORT, PhoneRegisterActivity.this);
                    return false;
                }
            }
        }

        if (TextUtils.isEmpty(code)) {
            String nullPhone = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_code_null);
            MyUtils.showToast(nullPhone, Toast.LENGTH_SHORT, PhoneRegisterActivity.this);
            return false;
        }
        return true;
    }

    private void switchPwdState() {
        if (!eyeOpen) {
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            etPassword.setSelection(etPassword.getText().toString().trim().length());
            if (TextUtils.isEmpty(etPassword.getText().toString().trim())) {
                ivClear.setVisibility(View.INVISIBLE);
            }
            ivEye.setImageResource(R.drawable.eyeopen_icon_1080p);
            eyeOpen = true;
        } else {
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            etPassword.setSelection(etPassword.getText().toString().trim().length());
            if (TextUtils.isEmpty(etPassword.getText().toString().trim())) {
                ivClear.setVisibility(View.INVISIBLE);
            }
            ivEye.setImageResource(R.drawable.eyeclose_icon_1080p);
            eyeOpen = false;
        }
    }

    private void clearUserInput() {
        etPassword.setText("");
        ivClear.setVisibility(View.INVISIBLE);
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_phone_register;
    }

    @Override
    public String getActivityTitle() {
        String title = AppContext.getContext().getResources().getString(R.string.title_activity_phone_register);
        return title;
    }

    private static class MyHandler extends Handler {
        private final WeakReference<PhoneRegisterActivity> mActivity;

        public MyHandler(PhoneRegisterActivity activity) {
            this.mActivity = new WeakReference<PhoneRegisterActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final PhoneRegisterActivity activity = mActivity.get();
            if (activity != null) {
                int event = msg.arg1;
                int result = msg.arg2;
                Object data = msg.obj;
                if (result == SMSSDK.RESULT_COMPLETE) {
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        HashMap<String, Object> phoneMap = (HashMap<String, Object>) data;
                        final String phone = (String) phoneMap.get(Constant.SMS_SDK_PHONE);
                        final String pwd = activity.etPassword.getText().toString().trim();

                        String json = CmdUserRegister.createRequestJson(phone, pwd);
                        LogUtils.d("register request >>>>> " + json);
                        MyCmdUtil.sendRandomTagRequest(Constant.URL_USER_REGISTER, json, new MyCmdHttpTask.CmdListener() {
                            @Override
                            public void onCmdExecuted(String responseResult) {

                                if (!TextUtils.isEmpty(responseResult)) {
                                    LogUtils.d("register response >>>>> " + responseResult);

                                    handleUserRegisterResults(responseResult, activity, phone, pwd);
                                }
                            }

                            @Override
                            public void onCmdException(Throwable exception) {
                                LogUtils.d("register exception>>>> " + exception.getMessage());
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

    class MyLoginReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.sharedream.wifiguard.weixin.login")) {
                PhoneRegisterActivity.this.finish();
            }
        }
    }

    class AuthListener implements WeiboAuthListener {

        @Override
        public void onComplete(Bundle bundle) {
            Oauth2AccessToken oauth2AccessToken = Oauth2AccessToken.parseAccessToken(bundle);
            if (oauth2AccessToken.isSessionValid()) {
                String token = oauth2AccessToken.getToken();
                String uid = oauth2AccessToken.getUid();
                LogUtils.d("token >>>>> " + token);
                LogUtils.d("openid >>>>> " + uid);

                DatabaseManager.insertUser(uid, "", Constant.LOGIN_MODE_WB);
                loginWeiboFromServer(uid);
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {

        }

        @Override
        public void onCancel() {

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
}
