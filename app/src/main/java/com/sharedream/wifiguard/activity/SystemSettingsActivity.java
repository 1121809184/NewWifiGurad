package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.content.Intent;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sharedream.wifiguard.MainActivity;
import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.cmdws.CmdGetMyShop;
import com.sharedream.wifiguard.cmdws.MyCmdHttpTask;
import com.sharedream.wifiguard.cmdws.MyCmdUtil;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.dialog.MessageDialog;
import com.sharedream.wifiguard.sqlite.DatabaseManager;
import com.sharedream.wifiguard.utils.GlobalField;
import com.sharedream.wifiguard.utils.LogUtils;
import com.sharedream.wifiguard.version.VersionManager;
import com.sharedream.wifiguard.vo.UserVo;

import java.util.ArrayList;

public class SystemSettingsActivity extends BaseActivity {

    private static Activity main;
    private RelativeLayout rlSettingsModifyPassword;
    private RelativeLayout rlSettingsQuit;
    private RelativeLayout rlSettingsVersionCheck;
    private ImageView ivSettingsVersionCheck;
    private TextView tvSettingsVersionSummary;
    private TextView tvSettingsUserName;
    private TextView tvSettingsUserShopDetail;
    private boolean login;
    private String accountShops;
    private RelativeLayout rlSysSetting;
    private TextView tvSettingsQuit;

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity, SystemSettingsActivity.class);
        activity.startActivity(intent);
        main = activity;
    }

    @Override
    protected void initAfterSetContentView() {
        super.enableMoreAction(false);
        initView();
        initData();
        setListener();
    }

    private void initView() {
        rlSysSetting = ((RelativeLayout) findViewById(R.id.rl_sys_setting));
        rlSettingsModifyPassword = ((RelativeLayout) findViewById(R.id.rl_settings_modify_passwd));
        rlSettingsQuit = ((RelativeLayout) findViewById(R.id.rl_settings_quit));
        rlSettingsVersionCheck = ((RelativeLayout) findViewById(R.id.rl_settings_version_check));
        ivSettingsVersionCheck = ((ImageView) findViewById(R.id.iv_settings_version_check));
        tvSettingsVersionSummary = ((TextView) findViewById(R.id.tv_settings_version_summary));
        tvSettingsUserName = ((TextView) findViewById(R.id.tv_settings_user_name));
        tvSettingsUserShopDetail = ((TextView) findViewById(R.id.tv_settings_user_shop_detail));
        tvSettingsQuit = ((TextView) findViewById(R.id.tv_settings_quit));
    }

    private void initData() {
        UserVo userVo = DatabaseManager.queryUser();
        if (userVo != null) {
            login = true;
            rlSysSetting.setVisibility(View.VISIBLE);
            rlSettingsModifyPassword.setVisibility(View.VISIBLE);
            tvSettingsQuit.setText("退出当前账号");
            String accountName = AppContext.getContext().getResources().getString(R.string.activity_system_settings_user);
            accountShops = AppContext.getContext().getResources().getString(R.string.activity_system_settings_user_shop);
            tvSettingsUserName.setText(String.format(accountName, userVo.userid));
            getMyShopFromServer();
        } else {
            login = false;
            rlSysSetting.setVisibility(View.GONE);
            rlSettingsModifyPassword.setVisibility(View.GONE);
            tvSettingsQuit.setText("登录");
        }
    }

    private void getMyShopFromServer() {
        String accessToken = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_TOKEN, null);
        String json = com.sharedream.wifiguard.cmdws.CmdGetMyShop.createRequestJson(accessToken);
        LogUtils.d("get my shops request >>> " + json);
        MyCmdUtil.sendRandomTagRequest(Constant.URL_WS_MY_SHOPS, json, new MyCmdHttpTask.CmdListener() {
            @Override
            public void onCmdExecuted(String responseResult) {
                if (!TextUtils.isEmpty(responseResult)) {
                    LogUtils.d("get my shops response >>> " + responseResult);
                    handleGetMyShopResults(responseResult);
                }
            }

            @Override
            public void onCmdException(Throwable exception) {
                LogUtils.d("get my shops exception >>> " + exception.getMessage());
            }
        });
    }

    private void handleGetMyShopResults(String response) {
        CmdGetMyShop.Results results = CmdGetMyShop.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            ArrayList<CmdGetMyShop.Shop> myShopList = results.data.list;
            int size = myShopList.size();
            int totalAp = results.data.totalAp;
            int shopLength = String.valueOf(size).trim().length();
            int apLength = String.valueOf(totalAp).trim().length();
            if (myShopList == null || myShopList.size() == 0) {
                tvSettingsUserShopDetail.setText("尚未添加商铺");
            } else {
                if (shopLength == 1 && apLength == 1) {
                    SpannableString styleText = new SpannableString(String.format(accountShops, size, totalAp));
                    styleText.setSpan(new ForegroundColorSpan(AppContext.getContext().getResources().getColor(R.color.theme_color)), 2, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    styleText.setSpan(new ForegroundColorSpan(AppContext.getContext().getResources().getColor(R.color.theme_color)), 7, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tvSettingsUserShopDetail.setText(styleText);

                } else if (shopLength == 1 && apLength == 2) {
                    SpannableString styleText = new SpannableString(String.format(accountShops, size, totalAp));
                    styleText.setSpan(new ForegroundColorSpan(AppContext.getContext().getResources().getColor(R.color.theme_color)), 2, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    styleText.setSpan(new ForegroundColorSpan(AppContext.getContext().getResources().getColor(R.color.theme_color)), 7, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tvSettingsUserShopDetail.setText(styleText);

                } else if (shopLength == 2 && apLength == 1) {
                    SpannableString styleText = new SpannableString(String.format(accountShops, size, totalAp));
                    styleText.setSpan(new ForegroundColorSpan(AppContext.getContext().getResources().getColor(R.color.theme_color)), 2, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    styleText.setSpan(new ForegroundColorSpan(AppContext.getContext().getResources().getColor(R.color.theme_color)), 8, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tvSettingsUserShopDetail.setText(styleText);

                } else if (shopLength == 2 && apLength == 2) {
                    SpannableString styleText = new SpannableString(String.format(accountShops, size, totalAp));
                    styleText.setSpan(new ForegroundColorSpan(AppContext.getContext().getResources().getColor(R.color.theme_color)), 2, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    styleText.setSpan(new ForegroundColorSpan(AppContext.getContext().getResources().getColor(R.color.theme_color)), 8, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tvSettingsUserShopDetail.setText(styleText);
                }
                //tvSettingsUserShopDetail.setText(String.format(accountShops, myShopList.size(), results.data.totalAp));
            }
        }
    }

    private void setListener() {
        rlSettingsModifyPassword.setOnClickListener(this);
        rlSettingsQuit.setOnClickListener(this);
        rlSettingsVersionCheck.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.rl_settings_modify_passwd:
                startUpdatePwdActivity();
                break;
            case R.id.rl_settings_quit:
                quitAndEnter();
                break;
            case R.id.rl_settings_version_check:
                checkVersion();
                break;
        }
    }

    private void showLogoutDialog() {
        final MessageDialog messageDialog = new MessageDialog(this, R.style.CustomDialogStyle);
        messageDialog.show();
        Window window = messageDialog.getWindow();
        window.setWindowAnimations(R.style.CustomDialogAnimationStyle);
        TextView tvDialogTitle = (TextView) messageDialog.findViewById(R.id.tv_dialog_title);
        TextView tvDialogNotice = (TextView) messageDialog.findViewById(R.id.tv_delete_notice);
        Button tvOk = (Button) messageDialog.findViewById(R.id.tv_ok);
        Button tvCancel = (Button) messageDialog.findViewById(R.id.tv_cancel);

        tvDialogTitle.setText(AppContext.getContext().getResources().getString(R.string.activity_sys_setting_logout_title));
        tvDialogNotice.setText(AppContext.getContext().getResources().getString(R.string.activity_sys_setting_logout_content));

        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quit();
                messageDialog.dismiss();
            }
        });

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageDialog.dismiss();
            }
        });
    }

    private void checkVersion() {
        new VersionManager(this, ivSettingsVersionCheck, tvSettingsVersionSummary, true).checkVersion();
    }

    private void quitAndEnter() {
        if (login) {
            showLogoutDialog();
        } else {
            LoginActivity.launch(this);
        }
    }

    private void quit() {
        main.finish();
        DatabaseManager.logout();
        GlobalField.saveField(AppContext.getContext(), Constant.INTENT_KEY_LOGIN, false);
        MainActivity.launch(this);
    }

    private void startUpdatePwdActivity() {
        UpdatePwdActivity.launch(this);
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_system_settings;
    }

    @Override
    public String getActivityTitle() {
        return "系统设置";
    }
}
