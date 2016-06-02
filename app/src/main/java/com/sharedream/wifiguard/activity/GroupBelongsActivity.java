package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.cmd.CmdAddShop;
import com.sharedream.wifiguard.cmdws.CmdGroupBelongs;
import com.sharedream.wifiguard.cmdws.CmdUpdateShop;
import com.sharedream.wifiguard.cmdws.MyCmdHttpTask;
import com.sharedream.wifiguard.cmdws.MyCmdUtil;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.manager.WukongWifiManager;
import com.sharedream.wifiguard.utils.GlobalField;
import com.sharedream.wifiguard.utils.LogUtils;
import com.sharedream.wifiguard.utils.MyUtils;

import java.util.ArrayList;

public class GroupBelongsActivity extends BaseActivity {
    private LinearLayout llGroupBelongContainer;
    private TextView tvGroupName;
    private TextView tvGroupId;
    private Button btnAddShopSave;
    private Button btnAddShopSkip;
    private AutoCompleteTextView atvGroupInfo;

    private ArrayList<CmdGroupBelongs.GroupInfo> groupInfoList;
    private String shopName;
    private String address;
    private double lng;
    private double lat;
    private String phone;
    private int category;
    private String cityId;
    private String owner;
    private String logoSrc;
    private String accessToken;
    private int groupId;
    private int requestKey;
    private int shopId;

    public static void launch(Activity activity, String shopName, String address, double lng, double lat, String phone, int category, String cityId, String owner, String logoSrc, int requestKey, int shopId) {
        Intent intent = new Intent(activity, GroupBelongsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("shopName", shopName);
        bundle.putString("address", address);
        bundle.putDouble("lng", lng);
        bundle.putDouble("lat", lat);
        bundle.putString("phone", phone);
        bundle.putInt("category", category);
        bundle.putString("cityId", cityId);
        bundle.putString("owner", owner);
        bundle.putString("logoSrc", logoSrc);
        bundle.putInt("requestKey", requestKey);
        bundle.putInt("shopId", shopId);
        intent.putExtras(bundle);
        activity.startActivityForResult(intent, RESULT_FIRST_USER);
    }

    @Override
    protected void initAfterSetContentView() {
        super.enableMoreAction(false);
        initView();
        initData();
        setListener();
    }

    private void initView() {
        llGroupBelongContainer = ((LinearLayout) findViewById(R.id.ll_group_belong_container));
        tvGroupName = ((TextView) findViewById(R.id.tv_group_name));
        tvGroupId = ((TextView) findViewById(R.id.tv_group_id));
        btnAddShopSave = ((Button) findViewById(R.id.btn_add_shop_save));
        btnAddShopSkip = ((Button) findViewById(R.id.btn_add_shop_skip));
        atvGroupInfo = ((AutoCompleteTextView) findViewById(R.id.atv_group_info));
    }

    private void initData() {
        accessToken = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_TOKEN, null);
        Bundle bundle = getIntent().getExtras();
        shopName = bundle.getString("shopName");
        address = bundle.getString("address");
        lng = bundle.getDouble("lng");
        lat = bundle.getDouble("lat");
        phone = bundle.getString("phone");
        category = bundle.getInt("category");
        cityId = bundle.getString("cityId");
        owner = bundle.getString("owner");
        logoSrc = bundle.getString("logoSrc");
        requestKey = bundle.getInt("requestKey");
        shopId = bundle.getInt("shopId");
        getGroupBelongs();
    }

    private void getGroupBelongs() {
        String accessToken = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_TOKEN, null);
        String json = CmdGroupBelongs.createRequestJson(accessToken);
        LogUtils.d("group belong request >>> " + json);
        MyCmdUtil.sendRandomTagRequest(Constant.URL_WS_GROUP_BELONGS, json, new MyCmdHttpTask.CmdListener() {
            @Override
            public void onCmdExecuted(String responseResult) {
                LogUtils.d("group belong response >>> " + responseResult);
                handleGetGroupBelongsResults(responseResult);
            }

            @Override
            public void onCmdException(Throwable exception) {
                LogUtils.d("group belong exception >>> " + exception.getMessage());
            }
        });
    }

    private void handleGetGroupBelongsResults(String response) {
        CmdGroupBelongs.Results results = CmdGroupBelongs.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            groupInfoList = results.data;
            final int size = groupInfoList.size();
            String[] groupInfoArray = new String[size];
            for (int i = 0; i < size; i++) {
                groupInfoArray[i] = groupInfoList.get(i).name;
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, groupInfoArray);
            atvGroupInfo.setAdapter(adapter);

            atvGroupInfo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String obj = (String) parent.getItemAtPosition(position);
                    for (int i = 0; i < size; i++) {
                        CmdGroupBelongs.GroupInfo groupInfo = groupInfoList.get(i);
                        if (groupInfo.name.equals(obj)) {
                            tvGroupName.setText(obj);
                            tvGroupId.setText(groupInfo.id + "");
                            groupId = groupInfo.id;
                            break;
                        }
                    }
                }
            });
        } else {
            MyUtils.showToast(results.msg, Toast.LENGTH_SHORT, this);
        }
    }

    private void handleAddShopResulte(String response) {
        CmdAddShop.Results results = CmdAddShop.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            LogUtils.d("add shop return shopid >>>> " + results.data.shopId);
            boolean wifiEnabled = WukongWifiManager.getInstance().isWifiEnabled();
            MyUtils.showToast("新增商铺成功", Toast.LENGTH_SHORT, this);
            if (!wifiEnabled) {
                WukongWifiManager.getInstance().openWifi();
            }
            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_SHOP_ID, results.data.shopId);
            BindingWifiActivity.launchFromGroupBelongs(this, shopName, Constant.REQUEST_CODE_BINGDING_WIFI);
        } else {
            MyUtils.showToast(results.msg, Toast.LENGTH_SHORT, this);
        }
    }

    private void setListener() {
        btnAddShopSave.setOnClickListener(this);
        btnAddShopSkip.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.btn_add_shop_save:
                if (requestKey == Constant.BUNDLE_KEY_ADD_SHOP) {
                    save(String.valueOf(groupId));
                } else if (requestKey == Constant.BUNDLE_KEY_EDIT_SHOP) {
                    changeFinish(String.valueOf(groupId));
                }
                break;
            case R.id.btn_add_shop_skip:
                if (requestKey == Constant.BUNDLE_KEY_ADD_SHOP) {
                    save("");
                } else if (requestKey == Constant.BUNDLE_KEY_EDIT_SHOP) {
                    changeFinish("");
                }
                break;
        }
    }

    private void changeFinish(String groupId) {
        String json = com.sharedream.wifiguard.cmdws.CmdUpdateShop.createRequestJson(accessToken, shopId, shopName, address, lng, lat, phone, "", category, Integer.parseInt(cityId), owner, groupId, logoSrc);
        LogUtils.d("modify shop request >>>>> " + json);
        MyCmdUtil.sendRandomTagRequest(Constant.URL_WS_UPDATE_SHOP, json, new MyCmdHttpTask.CmdListener() {
            @Override
            public void onCmdExecuted(String responseResult) {
                if (!TextUtils.isEmpty(responseResult)) {
                    LogUtils.d("modify shop response >>>>> " + responseResult);
                    handleUpdateShopResults(responseResult);
                }
            }


            @Override
            public void onCmdException(Throwable exception) {
                LogUtils.d("modify shop exception >>>>> " + exception.getMessage());
            }
        });
    }

    private void handleUpdateShopResults(String responseResult) {
        //        CmdUpdateShop.Results results = CmdUpdateShop.parseResponseJson(responseResult);
        CmdUpdateShop.Results results = CmdUpdateShop.parseResponseJson(responseResult);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            MyUtils.showToast(results.msg, this);
            setResult(RESULT_OK);
            this.finish();
        } else if (results.code == -2) {
            MyUtils.showToast(results.msg, this);
        } else if (results.code == -1) {
            MyUtils.showToast(results.msg, this);
        }
    }

    private void skip() {

    }

    private void save(String groupId) {
        String json = com.sharedream.wifiguard.cmdws.CmdAddShop.createRequestJson(accessToken, shopName, address, lng, lat, phone, "", category, Integer.parseInt(cityId), owner, groupId, logoSrc);
        LogUtils.d("add shop request >>>>> " + json);
        MyCmdUtil.sendRandomTagRequest(Constant.URL_WS_ADD_SHOP, json, new MyCmdHttpTask.CmdListener() {
            @Override
            public void onCmdExecuted(String responseResult) {
                if (!TextUtils.isEmpty(responseResult)) {
                    LogUtils.d("add shop response >>>>> " + responseResult);
                    handleAddShopResulte(responseResult);
                }
            }

            @Override
            public void onCmdException(Throwable exception) {
                LogUtils.d("add shop exception >>>>> " + exception.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQUEST_CODE_BINGDING_WIFI && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_group_belongs;
    }

    @Override
    public String getActivityTitle() {
        return "归属信息";
    }
}
