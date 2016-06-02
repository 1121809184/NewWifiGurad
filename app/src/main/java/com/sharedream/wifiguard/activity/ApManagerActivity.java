package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.cmdws.CmdGetMyShop;
import com.sharedream.wifiguard.cmdws.CmdModifyApPlace;
import com.sharedream.wifiguard.cmdws.MyCmdHttpTask;
import com.sharedream.wifiguard.cmdws.MyCmdUtil;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.listener.SingleClickListener;
import com.sharedream.wifiguard.utils.GlobalField;
import com.sharedream.wifiguard.utils.LogUtils;
import com.sharedream.wifiguard.utils.MyUtils;

public class ApManagerActivity extends BaseActivity {

    private TextView tvApName;
    private EditText etApDes;
    private Button btnApDelete;
    private Button btnApSave;
    private TextView tvLevel;

    private CmdGetMyShop.Ap myAp;
    private CmdGetMyShop.Shop myShop;
    private String levelTips;

    public static void launch(Activity activity, CmdGetMyShop.Ap myAp, CmdGetMyShop.Shop myShop, String levelTips) {
        Intent intent = new Intent(activity, ApManagerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constant.BUNDLE_KEY_MY_AP, myAp);
        bundle.putParcelable(Constant.BUNDLE_KEY_MY_SHOP, myShop);
        bundle.putString(Constant.BUNDLE_KEY_LEVEL_TIPS, levelTips);
        intent.putExtras(bundle);
        activity.startActivityForResult(intent, RESULT_FIRST_USER);
    }

    @Override
    protected void initAfterSetContentView() {
        enableMoreAction(false);
        initView();
        initData();
        setListener();
    }

    private void initView() {
        tvLevel = (TextView) findViewById(R.id.tv_level);
        tvApName = ((TextView) findViewById(R.id.tv_ap_name));
        etApDes = ((EditText) findViewById(R.id.et_ap_des));
        btnApDelete = ((Button) findViewById(R.id.btn_ap_delete));
        btnApSave = ((Button) findViewById(R.id.btn_ap_save));
    }

    private void initData() {
        myAp = getIntent().getExtras().getParcelable(Constant.BUNDLE_KEY_MY_AP);
        myShop = getIntent().getExtras().getParcelable(Constant.BUNDLE_KEY_MY_SHOP);
        levelTips = getIntent().getStringExtra(Constant.BUNDLE_KEY_LEVEL_TIPS);
        tvLevel.setText(levelTips);
        if (myAp != null) {
            tvApName.setText(myAp.ssid);
            etApDes.setText(myAp.place);
        }
        btnApSave.setEnabled(false);
    }

    private void setListener() {
        btnApDelete.setOnClickListener(new SingleClickListener() {
            @Override
            protected void onSingleClick(View view) {
                deleteAp();
            }
        });
        btnApSave.setOnClickListener(this);
        etApDes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ("".equals(s.toString().trim())) {
                    btnApSave.setEnabled(false);
                } else {
                    btnApSave.setEnabled(true);
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

            case R.id.btn_ap_save:
                saveApPlace();

                break;
        }
    }

    private void saveApPlace() {
        String accessToken = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_TOKEN, null);
        String place = etApDes.getText().toString().trim();
        String json = CmdModifyApPlace.createRequestJson(accessToken, myAp.apId, place);
        LogUtils.d("add ap place request >>>>> " + json);
        MyCmdUtil.sendRandomTagRequest(Constant.URL_WS_ADD_AP_PLACE, json, new MyCmdHttpTask.CmdListener() {
            @Override
            public void onCmdExecuted(String responseResult) {
                if (!TextUtils.isEmpty(responseResult)) {
                    LogUtils.d("add ap place response >>>>> " + responseResult);
                    handleModifyApPlaceResults(responseResult);
                }
            }

            @Override
            public void onCmdException(Throwable exception) {
                LogUtils.d("add ap place exception >>>>> " + exception.getMessage());
            }
        });
    }

    private void handleModifyApPlaceResults(String response) {
        CmdModifyApPlace.Results results = CmdModifyApPlace.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            MyUtils.showToast("热点修改成功", this);
            GlobalField.saveField(AppContext.getContext(), "from_wifi_detail", true);
            setResult(RESULT_OK);
            this.finish();
        } else {
            MyUtils.showToast(results.msg, this);
        }
    }

    private void deleteAp() {
        //        String json = CmdDeleteAp.createRequestJson(myShop.shopId, myAp.apId);
        //        LogUtils.d("删除ap request json >>>>> " + json);
        //        CmdUtil.sendRandomTagRequest(Constant.URL_CMD_DELETE_AP, json, new BaseCmdHttpTask.CmdListener() {
        //            @Override
        //            public void onCmdExecuted(String responseResult) {
        //                if (!TextUtils.isEmpty(responseResult)) {
        //                    LogUtils.d("删除ap response json >>>>> " + responseResult);
        //                    handleDeleteApResults(responseResult);
        //                }
        //            }
        //
        //            @Override
        //            public void onCmdException(Exception exception) {
        //                exception.printStackTrace();
        //            }
        //        });
        String accessToken = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_TOKEN, null);
        String json = com.sharedream.wifiguard.cmdws.CmdDeleteAp.createRequestJson(accessToken, myAp.apId);
        LogUtils.d("delete ap request >>>>> " + json);
        MyCmdUtil.sendRandomTagRequest(Constant.URL_WS_DELETE_AP, json, new MyCmdHttpTask.CmdListener() {
            @Override
            public void onCmdExecuted(String responseResult) {
                if (!TextUtils.isEmpty(responseResult)) {
                    LogUtils.d("delete ap response >>>>> " + responseResult);
                    handleDeleteApResults(responseResult);
                }
            }

            @Override
            public void onCmdException(Throwable exception) {
                LogUtils.d("delete ap exception >>>>> " + exception.getMessage());
            }
        });
    }

    private void handleDeleteApResults(String responseResult) {
        //        CmdDeleteAp.Results results = CmdDeleteAp.parseResponseJson(responseResult);
        com.sharedream.wifiguard.cmdws.CmdDeleteAp.Results results = com.sharedream.wifiguard.cmdws.CmdDeleteAp.parseResponseJson(responseResult);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            MyUtils.showToast(results.msg, this);
            GlobalField.saveField(AppContext.getContext(), "from_wifi_detail", true);
            setResult(RESULT_OK);
            this.finish();
        } else {
            MyUtils.showToast(results.msg, this);
        }
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_ap_manager;
    }

    @Override
    public String getActivityTitle() {
        return "热点管理";
    }
}
