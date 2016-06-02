package com.sharedream.wifiguard.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.sharedream.wifiguard.MainActivity;
import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.activity.BindingPhoneActivity;
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
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    private IWXAPI wxapi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wxapi = WXAPIFactory.createWXAPI(this, Constant.WX_APP_ID, false);
        wxapi.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        wxapi.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp baseResp) {
        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                String code = ((SendAuth.Resp) baseResp).code;
                LogUtils.d("微信返回code >>>>> " + code);
                getAccessTokenAndSave(code);
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:

                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:

                break;
            default:
                break;
        }
        finish();
    }

    private void getAccessTokenAndSave(final String code) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpClient client = new DefaultHttpClient();
                    String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + Constant.WX_APP_ID + "&secret=" + Constant.WX_APP_SECRET
                            + "&code=" + code + "&grant_type=authorization_code";
                    LogUtils.d("微信URL >>>> " + url);
                    HttpGet get = new HttpGet(url);
                    HttpResponse response = client.execute(get);
                    if (response.getStatusLine().getStatusCode() == Constant.HTTP_SUCCESS_CODE) {
                        InputStream is = response.getEntity().getContent();
                        BufferedReader br = new BufferedReader(new InputStreamReader(is));
                        StringBuffer buffer = new StringBuffer();
                        String line;
                        while ((line = br.readLine()) != null) {
                            buffer.append(line);
                        }
                        br.close();
                        is.close();
                        LogUtils.d("微信返回access token >>>> " + buffer.toString());
                        JSONObject jsonObject = new JSONObject(buffer.toString());
                        String uid = jsonObject.optString("openid");
                        LogUtils.d("微信id >>>> " + uid);
                        //GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_THIRD_LOGIN_UID, uid);//保存uid
                        //boolean noFirstLaunch = GlobalField.restoreFieldBoolean(AppContext.getContext(), Constant.SP_KEY_FIRST_LAUNCH, false);
                        DatabaseManager.insertUser(uid, "", Constant.LOGIN_MODE_WX);
                        //请求服务器微信登录
                        loginFromServer(uid);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void loginFromServer(String openid) {
        String json = CmdUserLogin.createRequestJson("", "", "", "", openid);
        LogUtils.d("微信登录 request >>> " + json);
        MyCmdUtil.sendRandomTagRequest(Constant.URL_USER_LOGIN, json, new MyCmdHttpTask.CmdListener() {
            @Override
            public void onCmdExecuted(String responseResult) {
                if (!TextUtils.isEmpty(responseResult)) {
                    LogUtils.d("微信登录 response >>> " + responseResult);
                    handleWxLoginResults(responseResult);
                }
            }

            @Override
            public void onCmdException(Throwable exception) {

            }
        });
    }

    private void handleWxLoginResults(String response) {
        CmdUserLogin.Results results = CmdUserLogin.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            //登录成功
            LogUtils.d("--------->>>>> " + results.data.accessToken);
            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_ACCESS_TOKEN, results.data.accessToken);
            LocalBroadcastManager.getInstance(AppContext.getContext()).sendBroadcast(new Intent("com.sharedream.wifiguard.weixin.login"));
            checkLoginStateByThirdLogin(results.data.accessToken, results.data.uid);
        } else if (results.code == Constant.SERVER_USER_SYS_USER_INEXISTENCE) {
            String str = AppContext.getContext().getResources().getString(R.string.activity_login_no_user);
            MyUtils.showToast(str, WXEntryActivity.this);
        } else if (results.code == Constant.SERVER_USER_SYS_WRONG_PASSWORD) {
            String str = AppContext.getContext().getResources().getString(R.string.activity_login_wrong_pwd);
            MyUtils.showToast(str, WXEntryActivity.this);
        } else if (results.code == Constant.SERVER_USER_SYS_ACCOUNT_FROZEN) {
            String str = AppContext.getContext().getResources().getString(R.string.activity_login_account_frozen);
            MyUtils.showToast(str, WXEntryActivity.this);
        } else if (results.code == Constant.SERVER_USER_SYS_BINDING_PHONE) {
            String str = AppContext.getContext().getResources().getString(R.string.activity_login_please_binding_phone);
            MyUtils.showToast(str, WXEntryActivity.this);

            LogUtils.d("--------->>>>> " + results.data.accessToken);
            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_ACCESS_TOKEN, results.data.accessToken);

            //去绑定
            Intent intent = new Intent(WXEntryActivity.this, BindingPhoneActivity.class);
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
}
