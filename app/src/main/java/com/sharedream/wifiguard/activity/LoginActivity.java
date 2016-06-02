package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
import android.widget.Toast;

import com.sharedream.wifiguard.MainActivity;
import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.http.CmdUserLogin;
import com.sharedream.wifiguard.cmdws.CmdCheckLoginState;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.http.MyCmdHttpTask;
import com.sharedream.wifiguard.http.MyCmdUtil;
import com.sharedream.wifiguard.sqlite.DatabaseManager;
import com.sharedream.wifiguard.utils.GlobalField;
import com.sharedream.wifiguard.utils.LogUtils;
import com.sharedream.wifiguard.utils.MyUtils;
import com.sharedream.wifiguard.vo.UserVo;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {

    private EditText etPassword;
    private EditText etPhoneNumber;
    private ImageView ivEye;
    private LinearLayout llGoToRegister;
    private RadioGroup rgThirdLoginContainer;
    private Button btnLogin;

    private boolean eyeOpen = false;
    private boolean noFirstLaunch;
    private UserVo userVo;
    private AuthInfo authInfo;
    private SsoHandler weiboSsoHandler;
    private IWXAPI wxapi;
    private RadioButton rbLoginWeixin;
    private RadioButton rbLoginWeibo;
    private MyLoginReceiver myLoginReceiver;
    private LinearLayout llResetPasswd;

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);
        activity.startActivity(intent);
        AppContext.getContext().addActivity(activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        ivEye = ((ImageView) findViewById(R.id.iv_eye));
        btnLogin = ((Button) findViewById(R.id.btn_login));
        llGoToRegister = ((LinearLayout) findViewById(R.id.ll_go_to_register));
        rgThirdLoginContainer = ((RadioGroup) findViewById(R.id.rg_third_login_container));
        rbLoginWeixin = ((RadioButton) findViewById(R.id.rb_login_weixin));
        rbLoginWeibo = ((RadioButton) findViewById(R.id.rb_login_weibo));
        llResetPasswd = ((LinearLayout) findViewById(R.id.ll_reset_password));

        etPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ((s.toString().trim()).length() > 11) {

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initData() {
        noFirstLaunch = GlobalField.restoreFieldBoolean(AppContext.getContext(), Constant.SP_KEY_FIRST_LAUNCH, false);
        myLoginReceiver = new MyLoginReceiver();
        LocalBroadcastManager.getInstance(AppContext.getContext()).registerReceiver(myLoginReceiver, new IntentFilter("com.sharedream.wifiguard.weixin.login"));
    }

    private void setListener() {
        ivEye.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        llGoToRegister.setOnClickListener(this);
        llResetPasswd.setOnClickListener(this);
        rgThirdLoginContainer.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.iv_eye:
                switchPwdState();
                break;
            case R.id.btn_login:
                login();
                break;
            case R.id.ll_go_to_register:
                startPhoneRegisterActivity();
                break;
            case R.id.ll_reset_password:
                startResetPasswdActivity();
                break;
        }
    }

    private void startResetPasswdActivity() {
        //ResetPasswdActivity.launch(this);
        VerifyMobileActivity.launch(this);
    }

    private void startPhoneRegisterActivity() {
        PhoneRegisterActivity.launch(this);
    }

    private void checkUserFromServer(final String phone, final String pwd) {
        String json = CmdUserLogin.createRequestJson(phone, pwd, "", "", "");
        LogUtils.d("phone login request >>>>> " + json);
        MyCmdUtil.sendRandomTagRequest(Constant.URL_USER_LOGIN, json, new MyCmdHttpTask.CmdListener() {
            @Override
            public void onCmdExecuted(String responseResult) {

                if (!TextUtils.isEmpty(responseResult)) {
                    LogUtils.d("phone login response >>>>> " + responseResult);
                    handleUserLoginResults(responseResult, phone, pwd);
                }
            }

            @Override
            public void onCmdException(Throwable exception) {
                LogUtils.d("phone login Exception >>>>> " + exception.getMessage());
            }
        });
    }

    private void handleUserLoginResults(String response, String phone, String pwd) {
        CmdUserLogin.Results results = CmdUserLogin.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            LogUtils.d("phone login accessToken >>> " + results.data.accessToken);
            LogUtils.d("phone login uid >>> " + results.data.uid);
            DatabaseManager.insertUser(phone, pwd, Constant.LOGIN_MODE_MB);
            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_ACCESS_TOKEN, results.data.accessToken);

            //验证登陆状态
            checkLoginState(results.data.accessToken, results.data.uid);
        } else if (results.code == Constant.SERVER_USER_SYS_USER_INEXISTENCE) {
            String str = AppContext.getContext().getResources().getString(R.string.activity_login_no_user);
            MyUtils.showToast(str, LoginActivity.this);
        } else if (results.code == Constant.SERVER_USER_SYS_WRONG_PASSWORD) {
            String str = AppContext.getContext().getResources().getString(R.string.activity_login_wrong_pwd);
            MyUtils.showToast(str, LoginActivity.this);
        } else if (results.code == Constant.SERVER_USER_SYS_ACCOUNT_FROZEN) {
            String str = AppContext.getContext().getResources().getString(R.string.activity_login_account_frozen);
            MyUtils.showToast(str, LoginActivity.this);
        }
    }

    private void checkLoginState(String accessToken, String uid) {
        String json = CmdCheckLoginState.createRequestJson(accessToken, uid);
        LogUtils.d("check login state request >>>>> " + json);
        com.sharedream.wifiguard.cmdws.MyCmdUtil.sendRandomTagRequest(Constant.URL_WS_LOGIN_STATE, json, new com.sharedream.wifiguard.cmdws.MyCmdHttpTask.CmdListener() {
            @Override
            public void onCmdExecuted(String responseResult) {
                if (!TextUtils.isEmpty(responseResult)) {
                    LogUtils.d("check login state response >>>>> " + responseResult);
                    handleCheckLoginStateResults(responseResult);
                }
            }

            @Override
            public void onCmdException(Throwable exception) {

            }
        });
    }

    private void handleCheckLoginStateResults(String response) {
        CmdCheckLoginState.Results results = CmdCheckLoginState.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            MainActivity.launch(LoginActivity.this);
            GlobalField.saveField(AppContext.getContext(), Constant.INTENT_KEY_LOGIN, true);
        }
    }

    private void login() {
        String userName = etPhoneNumber.getText().toString().trim();
        String pwd = etPassword.getText().toString().trim();
        boolean isLoginSuccess = verifyUser(userName, pwd);
        if (isLoginSuccess) {
            checkUserFromServer(userName, pwd);
        }
    }

    private boolean verifyUser(String userName, String password) {
        if (TextUtils.isEmpty(userName)) {
            String string = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_phone_null);
            MyUtils.showToast(string, Toast.LENGTH_SHORT, LoginActivity.this);
            return false;
        } else {
            Pattern p = Pattern.compile("^[1]+[3,5,8,4]+\\d{9}");
            Matcher m = p.matcher(userName);
            if (!m.matches()) {
                String str = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_phone_err);
                MyUtils.showToast(str, Toast.LENGTH_SHORT, LoginActivity.this);
                return false;
            }
        }
        if (TextUtils.isEmpty(password)) {
            String string = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_pwd_null);
            MyUtils.showToast(string, Toast.LENGTH_SHORT, LoginActivity.this);
            return false;
        } else {
            Pattern p1 = Pattern.compile("^[a-zA-Z0-9]+$");
            Matcher m1 = p1.matcher(password);
            if (!m1.matches()) {
                String msg = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_pwd_fail);
                MyUtils.showToast(msg, Toast.LENGTH_SHORT, LoginActivity.this);
                return false;
            }
        }
        return true;
    }

    private void switchPwdState() {
        if (!eyeOpen) {
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            etPassword.setSelection(etPassword.getText().toString().trim().length());
            ivEye.setImageResource(R.drawable.eyeopen_icon_1080p);
            eyeOpen = true;
        } else {
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            etPassword.setSelection(etPassword.getText().toString().trim().length());
            ivEye.setImageResource(R.drawable.eyeclose_icon_1080p);
            eyeOpen = false;
        }
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_login;
    }

    @Override
    public String getActivityTitle() {
        String title = AppContext.getContext().getResources().getString(R.string.title_activity_login);
        return title;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_login_weibo:
                rbLoginWeibo.setChecked(false);
                weiboLogin();
                break;
            case R.id.rb_login_weixin:
                rbLoginWeixin.setChecked(false);
                wxLogin();
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
            MyUtils.showToast("抱歉,您的手机还未安装微信客户端", LoginActivity.this);
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
        LogUtils.d("weibi login request >>> " + json);
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
            MyUtils.showToast(str, LoginActivity.this);
        } else if (results.code == Constant.SERVER_USER_SYS_WRONG_PASSWORD) {
            String str = AppContext.getContext().getResources().getString(R.string.activity_login_wrong_pwd);
            MyUtils.showToast(str, LoginActivity.this);
        } else if (results.code == Constant.SERVER_USER_SYS_ACCOUNT_FROZEN) {
            String str = AppContext.getContext().getResources().getString(R.string.activity_login_account_frozen);
            MyUtils.showToast(str, LoginActivity.this);
        } else if (results.code == Constant.SERVER_USER_SYS_BINDING_PHONE) {
            String str = AppContext.getContext().getResources().getString(R.string.activity_login_please_binding_phone);
            MyUtils.showToast(str, LoginActivity.this);
            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_ACCESS_TOKEN, results.data.accessToken);

            Intent intent = new Intent(LoginActivity.this, BindingPhoneActivity.class);
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
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(AppContext.getContext()).unregisterReceiver(myLoginReceiver);
    }

    class MyLoginReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.sharedream.wifiguard.weixin.login")) {
                LoginActivity.this.finish();
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
}
