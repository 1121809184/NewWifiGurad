package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.sharedream.wifiguard.MainActivity;
import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.adapter.ShopListAdapter;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.cmd.CmdShopList;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.task.BaseCmdHttpTask;
import com.sharedream.wifiguard.utils.CmdUtil;
import com.sharedream.wifiguard.utils.GlobalField;

import java.util.ArrayList;
import java.util.List;

public class MerchantActivity extends BaseActivity {
    private TextView tvSearch;
    private ListView lvMerchant;

    private ShopListAdapter shopListAdapter;
    private List<CmdShopList.Shop> shopList;
    private static boolean firstLaunch;
    private TextView tvAddShop;

    public static void launch(Activity activity) {
        firstLaunch = GlobalField.restoreFieldBoolean(AppContext.getContext(), Constant.SP_KEY_FIRST_LAUNCH, false);
        Intent intent = new Intent(activity, MerchantActivity.class);
        activity.startActivity(intent);
        if (firstLaunch) {
            activity.finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initAfterSetContentView() {
        super.enableMoreAction(false);
        initView();
        initData();
        setListener();
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_merchant;
    }

    @Override
    public String getActivityTitle() {
        String title = AppContext.getContext().getResources().getString(R.string.title_activity_merchant);
        return title;
    }

    private void initView() {
        tvAddShop = ((TextView) findViewById(R.id.tv_add));
        lvMerchant = ((ListView) findViewById(R.id.lv_merchant));
        tvSearch = ((TextView) findViewById(R.id.tv_search));
    }

    private void initData() {
        shopList = new ArrayList<CmdShopList.Shop>();
        getShopInfoFromServer();
    }

    private void getShopInfoFromServer() {
        String accessKey = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_KEY, null);
        float lng = GlobalField.restoreFieldFloat(AppContext.getContext(), Constant.SP_KEY_LOC_LNG, 0);
        float lat = GlobalField.restoreFieldFloat(AppContext.getContext(), Constant.SP_KEY_LOC_LAT, 0);
        String cityId = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_LOC_CITY_ID, null);
        String json = CmdShopList.createRequestJson("13888888888", accessKey, cityId, lng, lat);
        CmdUtil.sendRandomTagRequest(Constant.URL_CMD_MERCHANT_LIST, json, new BaseCmdHttpTask.CmdListener() {
            @Override
            public void onCmdExecuted(String responseResult) {
                if (!TextUtils.isEmpty(responseResult)) {
                    handleShopInfoResults(responseResult);
                }
            }

            @Override
            public void onCmdException(Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    private void handleShopInfoResults(String response) {
        CmdShopList.Results results = CmdShopList.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            CmdShopList.Data data = results.data;
            if (data != null) {
                shopList = data.shopList;
                shopListAdapter = new ShopListAdapter(shopList);
                lvMerchant.setAdapter(shopListAdapter);
            }
        }
    }

    private void setListener() {
        tvSearch.setOnClickListener(this);
        tvAddShop.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.tv_search:

                break;
            case R.id.tv_add:
                AddShopActivity.launch(MerchantActivity.this);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (firstLaunch) {
            MainActivity.launch(MerchantActivity.this);
            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_FIRST_LAUNCH, false);
            finish();
        }
    }

    @Override
    protected void finishActivity() {
        if (firstLaunch) {
            MainActivity.launch(MerchantActivity.this);
            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_FIRST_LAUNCH, false);
            finish();
        } else {
            finish();
        }
    }
}
