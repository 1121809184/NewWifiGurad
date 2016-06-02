package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResetPasswdActivity extends BaseActivity {

    private EditText etPasswd;
    private ImageView ivEye;
    private EditText etPasswdAgain;
    private ImageView ivEyeAgain;
    private Button btnResetPasswdNext;

    private boolean eyeOpen = false;
    private String phone;
    private String passwd1;

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, ResetPasswdActivity.class);
        activity.startActivity(intent);
    }

    public static void launch(Activity activity, String phone) {
        Intent intent = new Intent(activity, ResetPasswdActivity.class);
        intent.putExtra(Constant.INTENT_KEY_NEW_PHONE, phone);
        activity.startActivity(intent);
    }

    @Override
    protected void initAfterSetContentView() {
        super.enableMoreAction(false);
        initView();
        initData();
        setListener();
    }

    private void initView() {
        etPasswd = ((EditText) findViewById(R.id.et_password));
        ivEye = ((ImageView) findViewById(R.id.iv_eye));
        etPasswdAgain = ((EditText) findViewById(R.id.et_password_again));
        ivEyeAgain = ((ImageView) findViewById(R.id.iv_eye_again));
        btnResetPasswdNext = ((Button) findViewById(R.id.btn_reset_next));
    }

    private void initData() {
        phone = getIntent().getStringExtra(Constant.INTENT_KEY_NEW_PHONE);
    }

    private void setListener() {
        ivEye.setOnClickListener(this);
        ivEyeAgain.setOnClickListener(this);
        btnResetPasswdNext.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.iv_eye:
                switchPwdState(etPasswd, ivEye);
                break;
            case R.id.iv_eye_again:
                switchPwdState(etPasswdAgain, ivEyeAgain);
                break;
            case R.id.btn_reset_next:
                startVerifyMobileActivity();
                break;
        }
    }

    private void startVerifyMobileActivity() {
        passwd1 = etPasswd.getText().toString().trim();
        String passwd2 = etPasswdAgain.getText().toString().trim();
        boolean valid = checkValid(passwd1, passwd2);
        if (valid) {
            //VerifyMobileActivity.launch(this, passwd1);
            getPasswdFromServer(phone, passwd1);
        }
    }

    private void getPasswdFromServer(String phone, String passwd) {
        String json = CmdUserResetPasswd.createRequestJson(phone, passwd);
        LogUtils.d("reset passwd request >>>>> " + phone);
        MyCmdUtil.sendRandomTagRequest(Constant.URL_USER_RESET_PASSWORD, json, new MyCmdHttpTask.CmdListener() {
            @Override
            public void onCmdExecuted(String responseResult) {
                LogUtils.d("reset passwd response >>>>> " + responseResult);
                handleGetPasswdFromServerResults(responseResult);
            }

            @Override
            public void onCmdException(Throwable exception) {
                LogUtils.d("reset passwd exception >>>>> " + exception.getMessage());
            }
        });
    }

    private void handleGetPasswdFromServerResults(String response) {
        CmdUserResetPasswd.Results results = CmdUserResetPasswd.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            DatabaseManager.logout();
            DatabaseManager.insertUser(phone, passwd1, Constant.LOGIN_MODE_MB);//*************************************
            relogin();
        }
    }

    //**********************************************************************************************
    private void relogin() {
        String json = CmdUserLogin.createRequestJson(phone, passwd1, "", "", "");
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

    private boolean checkValid(String arg0, String arg1) {
        if (TextUtils.isEmpty(arg0)) {
            String nullPhone = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_pwd_null);
            MyUtils.showToast(nullPhone, Toast.LENGTH_SHORT, this);
            return false;
        } else {
            if (arg0.length() < 6) {
                String str = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_pwd_less_six);
                MyUtils.showToast(str, Toast.LENGTH_SHORT, this);
                return false;
            } else {
                Pattern pattern = Pattern.compile("[a-zA-Z0-9]+");
                Matcher matcher = pattern.matcher(arg0);
                if (!matcher.matches()) {
                    String msg = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_pwd_ok);
                    MyUtils.showToast(msg, Toast.LENGTH_SHORT, this);
                    return false;
                }
            }
        }
        if (TextUtils.isEmpty(arg1)) {
            String nullPhone = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_pwd_null);
            MyUtils.showToast(nullPhone, Toast.LENGTH_SHORT, this);
            return false;
        } else {
            if (arg1.length() < 6) {
                String str = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_pwd_less_six);
                MyUtils.showToast(str, Toast.LENGTH_SHORT, this);
                return false;
            } else {
                Pattern pattern = Pattern.compile("[a-zA-Z0-9]+");
                Matcher matcher = pattern.matcher(arg1);
                if (!matcher.matches()) {
                    String msg = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_pwd_ok);
                    MyUtils.showToast(msg, Toast.LENGTH_SHORT, this);
                    return false;
                }
            }
        }
        if (!arg0.equals(arg1)) {
            MyUtils.showToast("两次输入密码不一致", Toast.LENGTH_SHORT, this);
            return false;
        }
        return true;
    }

    private void switchPwdState(EditText passwd, ImageView eye) {
        if (!eyeOpen) {
            passwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            passwd.setSelection(passwd.getText().toString().trim().length());
            eye.setImageResource(R.drawable.user_eye_open_icon_1080p);
            eyeOpen = true;
        } else {
            passwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
            passwd.setSelection(passwd.getText().toString().trim().length());
            eye.setImageResource(R.drawable.user_eye_close_con_1080p);
            eyeOpen = false;
        }
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_reset_passwd;
    }

    @Override
    public String getActivityTitle() {
        return "密码重置";
    }
}
