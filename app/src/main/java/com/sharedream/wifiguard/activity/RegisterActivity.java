package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sharedream.wifiguard.MainActivity;
import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.cmd.CmdAccessKey;
import com.sharedream.wifiguard.http.CmdUserLogin;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.http.MyCmdHttpTask;
import com.sharedream.wifiguard.http.MyCmdUtil;
import com.sharedream.wifiguard.manager.SystemBarTintManager;
import com.sharedream.wifiguard.sqlite.DatabaseManager;
import com.sharedream.wifiguard.task.BaseCmdHttpTask;
import com.sharedream.wifiguard.utils.CmdUtil;
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
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

public class RegisterActivity extends Activity implements View.OnClickListener {

    private TextView tvLogin;
    private Button btnPhoneRegister;
    private Button btnWeiboLogin;
    private Button btnWXLogin;
    private Tencent tencent;
    private AuthInfo authInfo;
    private SsoHandler weiboSsoHandler;
    private IWXAPI wxapi;

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, RegisterActivity.class);
        activity.startActivity(intent);
        activity.finish();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.theme_color);//通知栏所需颜色
        }
        setContentView(R.layout.activity_register);
        tencent = Tencent.createInstance(Constant.QQ_SDK_APP_ID, AppContext.getContext());
        authInfo = new AuthInfo(this, Constant.WEIBO_APP_KEY, Constant.WEIBO_REDIRECT_URL, Constant.WEIBO_SCOPE);
        wxapi = WXAPIFactory.createWXAPI(this, Constant.WX_APP_ID);
        wxapi.registerApp(Constant.WX_APP_ID);

        AppContext.getContext().addActivity(this);
        initView();
        initData();
        setListener();
    }

    private void initView() {
        tvLogin = ((TextView) findViewById(R.id.tv_login));
        btnPhoneRegister = ((Button) findViewById(R.id.btn_phone_register));
        btnWeiboLogin = ((Button) findViewById(R.id.btn_weibo_login));
        btnWXLogin = ((Button) findViewById(R.id.btn_wx_login));
    }

    private void initData() {
        getAccessKeyFromServer();
    }

    private void getAccessKeyFromServer() {
        String uid = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_USER_ID, null);
        String json = CmdAccessKey.createRequestJson(uid);
        LogUtils.d("accesskey request>>>>> " + json);
        CmdUtil.sendRandomTagRequest(Constant.URL_CMD_SERVER, json, new BaseCmdHttpTask.CmdListener() {
            @Override
            public void onCmdExecuted(String responseResult) {
                if (!TextUtils.isEmpty(responseResult)) {
                    LogUtils.d("accesskey response>>>>> " + responseResult);
                    handleAccessKeyResults(responseResult);
                }
            }

            @Override
            public void onCmdException(Exception exception) {
                LogUtils.d("accesskey exception>>>>> " + exception.getMessage());
            }
        });

    }

    private void handleAccessKeyResults(String response) {
        CmdAccessKey.Results results = CmdAccessKey.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            CmdAccessKey.Data data = results.data;
            if (data != null) {
                GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_ACCESS_KEY, data.accessKey);
            }
        }
    }

    private void setListener() {
        tvLogin.setOnClickListener(this);
        btnPhoneRegister.setOnClickListener(this);
        btnWeiboLogin.setOnClickListener(this);
        btnWXLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_login:
                startLaunchRegisterMerchantActivity();
                break;
            case R.id.btn_phone_register:
                startPhoneRegisterActivity();
                break;
            case R.id.btn_weibo_login:
                weiboLogin();
                break;
            case R.id.btn_wx_login:
                wxLogin();
                break;
        }
    }

    private void wxLogin() {
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "wechat_sdk_demo_test";
        wxapi.sendReq(req);
    }

    private void weiboLogin() {
        weiboSsoHandler = new SsoHandler(this, authInfo);
        weiboSsoHandler.authorize(new AuthListener());
    }

    private void qqLogin() {
        tencent.login(this, "all", new QQLoginListener());
    }

    private void startPhoneRegisterActivity() {
        PhoneRegisterActivity.launch(RegisterActivity.this);
    }

    private void startLaunchRegisterMerchantActivity() {
        LoginActivity.launch(RegisterActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (weiboSsoHandler != null) {
            weiboSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
        tencent.onActivityResult(requestCode, resultCode, data);
    }

    class QQLoginListener implements IUiListener {

        @Override
        public void onComplete(Object o) {
            Toast.makeText(RegisterActivity.this, o.toString(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onError(UiError uiError) {

        }

        @Override
        public void onCancel() {

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

            //            Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
            //            this.startActivity(intent);
            //            this.finish();
            boolean noFirstLaunch = GlobalField.restoreFieldBoolean(AppContext.getContext(), Constant.SP_KEY_FIRST_LAUNCH, false);
            if (noFirstLaunch) {
                MainActivity.launch(RegisterActivity.this);
            } else {
                LaunchRegisterMerchantActivity.launch(RegisterActivity.this);
            }
        } else if (results.code == Constant.SERVER_USER_SYS_USER_INEXISTENCE) {
            String str = AppContext.getContext().getResources().getString(R.string.activity_login_no_user);
            MyUtils.showToast(str, RegisterActivity.this);
        } else if (results.code == Constant.SERVER_USER_SYS_WRONG_PASSWORD) {
            String str = AppContext.getContext().getResources().getString(R.string.activity_login_wrong_pwd);
            MyUtils.showToast(str, RegisterActivity.this);
        } else if (results.code == Constant.SERVER_USER_SYS_ACCOUNT_FROZEN) {
            String str = AppContext.getContext().getResources().getString(R.string.activity_login_account_frozen);
            MyUtils.showToast(str, RegisterActivity.this);
        } else if (results.code == Constant.SERVER_USER_SYS_BINDING_PHONE) {
            String str = AppContext.getContext().getResources().getString(R.string.activity_login_please_binding_phone);
            MyUtils.showToast(str, RegisterActivity.this);
            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_ACCESS_TOKEN, results.data.accessToken);
            Intent intent = new Intent(RegisterActivity.this, BindingPhoneActivity.class);
            this.startActivity(intent);
            this.finish();
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
                LogUtils.d("uid >>>>> " + uid);

                //GlobalField.saveField(AppContext.getContext(),Constant.SP_KEY_THIRD_LOGIN_UID,uid);//保存uid
                DatabaseManager.insertUser(uid, "", Constant.LOGIN_MODE_WB);
                loginWeiboFromServer(uid);

                //boolean noFirstLaunch = GlobalField.restoreFieldBoolean(AppContext.getContext(), Constant.SP_KEY_FIRST_LAUNCH, false);
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
