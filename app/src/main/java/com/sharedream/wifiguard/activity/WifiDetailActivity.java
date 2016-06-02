package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.adapter.ManageWifiAdapter;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.cmdws.CmdGetMyShop;
import com.sharedream.wifiguard.cmdws.MyCmdHttpTask;
import com.sharedream.wifiguard.cmdws.MyCmdUtil;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.listener.WifiObserver;
import com.sharedream.wifiguard.listener.WifiSubject;
import com.sharedream.wifiguard.manager.WiFiManager;
import com.sharedream.wifiguard.manager.WukongWifiManager;
import com.sharedream.wifiguard.utils.GlobalField;
import com.sharedream.wifiguard.utils.LogUtils;
import com.sharedream.wifiguard.utils.MyUtils;

import java.util.ArrayList;
import java.util.List;

public class WifiDetailActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private TextView tvManagerWifiShopName;
    private TextView tvManagerWifiCount;
    private ImageView ivLogo;
    private ListView lvMyWifi;
    private LinearLayout llAddWifi;

    private List<CmdGetMyShop.Ap> apList;
    private List<ScanResult> resultList;
    private CmdGetMyShop.Shop myShop;
    private ManageWifiAdapter manageWifiAdapter;
    private ProgressDialog progressDialog;
    private WifiManager wifiManager;

    public static void launch(Activity activity, com.sharedream.wifiguard.cmdws.CmdGetMyShop.Shop myShop, ArrayList<com.sharedream.wifiguard.cmdws.CmdGetMyShop.Ap> myApArrayList) {
        Intent intent = new Intent(activity, WifiDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constant.BUNDLE_KEY_MY_SHOP, myShop);
        intent.putExtras(bundle);
        intent.putParcelableArrayListExtra(Constant.INTENT_KEY_AP_LIST, myApArrayList);
        activity.startActivity(intent);
    }

    @Override
    protected void initAfterSetContentView() {
        enableMoreAction(false);
        initView();
        initData();
        setListener();
    }

    private void initView() {
        ivLogo = (ImageView) findViewById(R.id.iv_manager_shop_logo);
        tvManagerWifiShopName = ((TextView) findViewById(R.id.tv_manager_wifi_shop_name));
        tvManagerWifiCount = ((TextView) findViewById(R.id.tv_manager_wifi_count));
        lvMyWifi = ((ListView) findViewById(R.id.lv_my_wifi));
        llAddWifi = ((LinearLayout) findViewById(R.id.ll_add_wifi));
        resultList = new ArrayList<ScanResult>();
    }

    private void initData() {
        WukongWifiManager.getInstance().scanWifi();
        apList = getIntent().getParcelableArrayListExtra(Constant.INTENT_KEY_AP_LIST);
        Bundle bundle = getIntent().getExtras();
        myShop = bundle.getParcelable(Constant.BUNDLE_KEY_MY_SHOP);
        String logoSrc = myShop.logoSrc;
//        ImageRequest imageRequest = new ImageRequest(logoSrc, new Response.Listener<Bitmap>() {
//            @Override
//            public void onResponse(Bitmap response) {
//                //给imageView设置图片
//                ivLogo.setImageBitmap(response);
//            }
//        }, 100, 100, ImageView.ScaleType.FIT_XY, Bitmap.Config.RGB_565, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                //设置一张错误的图片，临时用ic_launcher代替
//                ivLogo.setImageResource(R.drawable.shop_sys_logo);
//            }
//        });
//        MyCmdUtil.getRequestQueue().add(imageRequest);
        MyCmdUtil.getImageLoader().get(logoSrc, ImageLoader.getImageListener(ivLogo,
                R.drawable.shop_sys_logo, R.drawable.shop_sys_logo), 100, 100, ImageView.ScaleType.FIT_XY);

        refreshCurrentWifi();
    }

    private void refreshCurrentWifi() {
        tvManagerWifiShopName.setText(myShop.name);
        String wifiFormat = AppContext.getContext().getResources().getString(R.string.activity_update_shop_ap_total);
        if (apList == null || apList.size() == 0) {
            tvManagerWifiCount.setText("未绑定");
            llAddWifi.setVisibility(View.VISIBLE);
            lvMyWifi.setVisibility(View.GONE);
        } else {
            tvManagerWifiCount.setText(String.format(wifiFormat, apList.size()));
            llAddWifi.setVisibility(View.GONE);
            lvMyWifi.setVisibility(View.VISIBLE);

            //FootView
            View footView = View.inflate(AppContext.getContext(), R.layout.foot_view_add_shop, null);
            LinearLayout llFootView = ((LinearLayout) footView.findViewById(R.id.ll_foot_view));
            TextView tvFootContent = (TextView) footView.findViewById(R.id.tv_foot_content);
            tvFootContent.setText("添加新热点");
            llFootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addAp();
                }
            });

            wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            wifiManager.startScan();
            if (resultList.size() != 0) {
                resultList.clear();
            }
            resultList = wifiManager.getScanResults();


            if (resultList.size() != 0) {
                checkNearWifi(apList, resultList);
            }

//            for (int i = 0; i < apList.size(); i++) {
//                CmdGetMyShop.Ap ap = apList.get(i);
//                LogUtils.i("status:" + ap.status + ";" + "level:" + ap.level
//                );
//            }

            if (manageWifiAdapter == null) {
                lvMyWifi.addFooterView(footView);
                manageWifiAdapter = new ManageWifiAdapter(apList);
                lvMyWifi.setAdapter(manageWifiAdapter);
            } else {
                manageWifiAdapter.setMyApList(apList);
                manageWifiAdapter.notifyDataSetChanged();
            }

            if (progressDialog != null) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        }
    }

    private void checkNearWifi(List<CmdGetMyShop.Ap> apList, List<ScanResult> resultList) {
        for (int i = 0; i < apList.size(); i++) {
            CmdGetMyShop.Ap ap = apList.get(i);
            String bssid = ap.bssid;
            String ssid = ap.ssid;
            for (int k = 0; k < resultList.size(); k++) {
                ScanResult scanResult = resultList.get(k);
                int level = scanResult.level;
                String ssidFromList = scanResult.SSID;
                String bssidFromList = scanResult.BSSID;
                if (bssidFromList.equals(bssid)) {
                    ap.status = 1;
                    ap.level = level;
                    continue;
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean from_wifi_detail = GlobalField.restoreFieldBoolean(AppContext.getContext(), "from_wifi_detail", false);
        if (from_wifi_detail) {
            getMyShopFromServer();
        }
    }

    private void addAp() {
        boolean wifiEnabled = WukongWifiManager.getInstance().isWifiEnabled();
        if (wifiEnabled) {
            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_ADD_AP, true);
            GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_SHOP_ID, myShop.shopId);
            GlobalField.saveField(AppContext.getContext(), "from_wifi_detail", true);
            Intent intent = new Intent(this, BindingWifiActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(Constant.BUNDLE_KEY_SHOP_NAME, myShop.name);
            intent.putExtras(bundle);
            startActivityForResult(intent, Constant.REQUEST_CODE_BINGDING_WIFI);
        } else {
            MyUtils.showToast("请先打开手机WIFI,再尝试添加新热点", AppContext.getContext());
        }
    }

    private void setListener() {
        llAddWifi.setOnClickListener(this);
        lvMyWifi.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.ll_add_wifi:
                addAp();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TextView tvLevel = (TextView) view.findViewById(R.id.tv_level);
        String levelTips = tvLevel.getText().toString().trim();
        CmdGetMyShop.Ap myAp = apList.get(position);
        ApManagerActivity.launch(this, myAp, myShop,levelTips);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_FIRST_USER && resultCode == RESULT_OK) {
            getMyShopFromServer();
        }
//        else if (requestCode == Constant.REQUEST_CODE_BINGDING_WIFI && resultCode == RESULT_OK) {
//            setResult(RESULT_OK);
//            finish();
//        }
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
        //        String json = CmdGetMyShop.createRequestJson();
        //        LogUtils.d("我的商店request >>>>> " + json);
        //        CmdUtil.sendRandomTagRequest(Constant.URL_CMD_MY_SHOP, json, new BaseCmdHttpTask.CmdListener() {
        //            @Override
        //            public void onCmdExecuted(String responseResult) {
        //                if (!TextUtils.isEmpty(responseResult)) {
        //                    LogUtils.d("我的商店response >>>>> " + responseResult);
        //                    handleGetMyShopResults(responseResult);
        //                }
        //            }
        //
        //            @Override
        //            public void onCmdException(Exception exception) {
        //
        //            }
        //        });
    }

    private void handleGetMyShopResults(String response) {
        com.sharedream.wifiguard.cmdws.CmdGetMyShop.Results results = com.sharedream.wifiguard.cmdws.CmdGetMyShop.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            List<com.sharedream.wifiguard.cmdws.CmdGetMyShop.Shop> myShopList = results.data.list;
            for (int i = 0; i < myShopList.size(); i++) {
                com.sharedream.wifiguard.cmdws.CmdGetMyShop.Shop shop = myShopList.get(i);
                if (shop.shopId == myShop.shopId) {
                    myShop = shop;
                    apList = shop.apList;
                    refreshCurrentWifi();
                    break;
                }
            }
        }
        //        CmdGetMyShop.Results results = CmdGetMyShop.parseResponseJson(response);
        //        if (results == null) {
        //            return;
        //        }
        //        if (results.code == Constant.SERVER_SUCCESS_CODE) {
        //            List<CmdGetMyShop.MyShop> myShopList = results.data.shopList;
        //            for (int i = 0; i < myShopList.size(); i++) {
        //                CmdGetMyShop.MyShop shop = myShopList.get(i);
        //                if (shop.shopId == myShop.shopId) {
        //                    myShop = shop;
        //                    apList = shop.apList;
        //                    refreshCurrentWifi();
        //                    break;
        //                }
        //            }
        //        }
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_wifi_detail;
    }

    @Override
    public String getActivityTitle() {
        return "热点信息";
    }

}
