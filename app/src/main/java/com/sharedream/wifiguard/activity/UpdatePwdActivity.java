package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.http.CmdUserVerifyPasswd;
import com.sharedream.wifiguard.http.MyCmdHttpTask;
import com.sharedream.wifiguard.http.MyCmdUtil;
import com.sharedream.wifiguard.sqlite.DatabaseManager;
import com.sharedream.wifiguard.utils.GlobalField;
import com.sharedream.wifiguard.utils.LogUtils;
import com.sharedream.wifiguard.utils.MyUtils;
import com.sharedream.wifiguard.vo.UserVo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdatePwdActivity extends BaseActivity {

    private Button btnUpdateMyPwd;
    private EditText etOldPasswd;
    private EditText etNewPasswd;
    private EditText etNewPasswdAgain;
    private String newPwd;

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, UpdatePwdActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void initAfterSetContentView() {
        super.enableMoreAction(false);
        initView();
        initData();
        setListener();
    }

    private void initView() {
        etOldPasswd = ((EditText) findViewById(R.id.et_old_pwd));
        etNewPasswd = ((EditText) findViewById(R.id.et_new_pwd));
        etNewPasswdAgain = ((EditText) findViewById(R.id.et_new_pwd_again));
        btnUpdateMyPwd = ((Button) findViewById(R.id.btn_update_my_pwd));
    }

    private void initData() {

    }

    private void setListener() {
        btnUpdateMyPwd.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.btn_update_my_pwd:
                updatePasswd();
                break;
        }
    }

    private void updatePasswd() {
        String oldPwd = etOldPasswd.getText().toString().trim();
        newPwd = etNewPasswd.getText().toString().trim();
        String newPwdAgain = etNewPasswdAgain.getText().toString().trim();
        boolean isValidity = checkValidity(oldPwd, newPwd, newPwdAgain);
        if (!isValidity) {
            return;
        }
        UserVo userVo = DatabaseManager.queryUser();
        String passwd = userVo.passwd;
        if (!passwd.equals(oldPwd)) {
            MyUtils.showToast("旧密码输入错误,请输入正确的旧密码", Toast.LENGTH_SHORT, this);
            return;
        }
        String accessToken = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_TOKEN, null);
        String json = CmdUserVerifyPasswd.createRequestJson(accessToken, oldPwd, newPwd);
        LogUtils.d("update pwd request >>>>> " + json);
        MyCmdUtil.sendRandomTagRequest(Constant.URL_USER_VERIFY_PASSWORD, json, new MyCmdHttpTask.CmdListener() {
            @Override
            public void onCmdExecuted(String responseResult) {
                if (!TextUtils.isEmpty(responseResult)) {
                    LogUtils.d("update pwd response >>>>> " + responseResult);
                    handleUpdatePasswdResults(responseResult);
                }
            }

            @Override
            public void onCmdException(Throwable exception) {
                MyUtils.showToast("密码修改失败，请检查网络", Toast.LENGTH_SHORT, UpdatePwdActivity.this);
                LogUtils.d("update pwd exception >>>>> " + exception.getMessage());
            }
        });
    }

    private void handleUpdatePasswdResults(String response) {
        CmdUserVerifyPasswd.Results results = CmdUserVerifyPasswd.parseResponseJson(response);
        if (results == null) {
            MyUtils.showToast("密码修改失败，请检查网络", Toast.LENGTH_SHORT, this);
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            MyUtils.showToast("密码修改成功", Toast.LENGTH_SHORT, this);
            UserVo userVo = DatabaseManager.queryUser();
            if (userVo != null && userVo.mode == Constant.LOGIN_MODE_MB) {
                DatabaseManager.logout();
                DatabaseManager.insertUser(userVo.userid, newPwd, userVo.mode);
            }
            finish();
        }
    }

    /*private void relogin() {
        String json = CmdUserLogin.createRequestJson(newPhoneFromResetPwd, newPwd, "", "", "");
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
    }*/

    private boolean checkValidity(String oldPwd, String newPwd, String newPwdAgain) {
        if (TextUtils.isEmpty(oldPwd) || TextUtils.isEmpty(newPwd) || TextUtils.isEmpty(newPwdAgain)) {
            String nullPhone = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_pwd_null);
            MyUtils.showToast(nullPhone, Toast.LENGTH_SHORT, this);
            return false;
        } else {
            if (oldPwd.length() < 6 || newPwd.length() < 6 || newPwdAgain.length() < 6) {
                String str = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_pwd_less_six);
                MyUtils.showToast(str, Toast.LENGTH_SHORT, this);
                return false;
            } else {
                Pattern pattern = Pattern.compile("[a-zA-Z0-9]+");
                Matcher matcher = pattern.matcher(oldPwd);
                if (!matcher.matches()) {
                    String msg = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_pwd_ok);
                    MyUtils.showToast(msg, Toast.LENGTH_SHORT, this);
                    return false;
                } else {
                    matcher = pattern.matcher(newPwd);
                    if (!matcher.matches()) {
                        String msg = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_pwd_ok);
                        MyUtils.showToast(msg, Toast.LENGTH_SHORT, this);
                        return false;
                    } else {
                        matcher = pattern.matcher(newPwdAgain);
                        if (!matcher.matches()) {
                            String msg = AppContext.getContext().getResources().getString(R.string.activity_phone_button_register_notice_pwd_ok);
                            MyUtils.showToast(msg, Toast.LENGTH_SHORT, this);
                            return false;
                        }
                    }
                }
            }
            if (oldPwd.equals(newPwd)) {
                MyUtils.showToast("新密码不能和旧密码相同", Toast.LENGTH_SHORT, this);
                return false;
            }
            if (!newPwd.equals(newPwdAgain)) {
                MyUtils.showToast("新密码两次输入不一致", Toast.LENGTH_SHORT, this);
                return false;
            }
            return true;
        }
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_update_pwd;
    }

    @Override
    public String getActivityTitle() {
        String title = AppContext.getContext().getResources().getString(R.string.title_activity_update_pwd);
        return title;
    }
}
