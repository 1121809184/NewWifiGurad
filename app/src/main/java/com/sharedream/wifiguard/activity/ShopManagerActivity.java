package com.sharedream.wifiguard.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.cmdws.MyCmdHttpTask;
import com.sharedream.wifiguard.cmdws.MyCmdUtil;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.dialog.MessageDialog;
import com.sharedream.wifiguard.utils.GlobalField;
import com.sharedream.wifiguard.utils.LogUtils;
import com.sharedream.wifiguard.utils.MyUtils;

import java.util.ArrayList;
import java.util.List;

public class ShopManagerActivity extends BaseActivity {

    private TextView tvManagerShopName;
    private TextView tvManagerShopAddress;
    private TextView tvManagerShopWifiNumber;
    private RelativeLayout rlManagerShopEdit;
    private RelativeLayout rlManagerWifiEdit;
    private Button btnManagerDeleteShop;
    private ImageView ivLogo;
    private ArrayList<com.sharedream.wifiguard.cmdws.CmdGetMyShop.Ap> apList;
    private com.sharedream.wifiguard.cmdws.CmdGetMyShop.Shop myShop;

    public static void launch(Activity activity, com.sharedream.wifiguard.cmdws.CmdGetMyShop.Shop myShop, ArrayList<com.sharedream.wifiguard.cmdws.CmdGetMyShop.Ap> myApArrayList) {
        Intent intent = new Intent(activity, ShopManagerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constant.BUNDLE_KEY_MY_SHOP, myShop);
        intent.putExtras(bundle);
        intent.putParcelableArrayListExtra(Constant.INTENT_KEY_AP_LIST, myApArrayList);
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
        tvManagerShopName = ((TextView) findViewById(R.id.tv_manager_shop_name));
        tvManagerShopAddress = ((TextView) findViewById(R.id.tv_manager_shop_address));
        tvManagerShopWifiNumber = ((TextView) findViewById(R.id.tv_manager_shop_wifi_number));
        rlManagerShopEdit = ((RelativeLayout) findViewById(R.id.rl_manager_shop_edit));
        rlManagerWifiEdit = ((RelativeLayout) findViewById(R.id.rl_manager_wifi_edit));
        btnManagerDeleteShop = ((Button) findViewById(R.id.btn_manager_delete_shop));
        ivLogo = ((ImageView) findViewById(R.id.iv_manager_shop_logo));
    }

    private void initData() {
        apList = getIntent().getParcelableArrayListExtra(Constant.INTENT_KEY_AP_LIST);
        Bundle bundle = getIntent().getExtras();
        myShop = bundle.getParcelable(Constant.BUNDLE_KEY_MY_SHOP);

        refreshCurrentShop();
    }

    private void refreshCurrentShop() {
        tvManagerShopName.setText(myShop.name);
        String addrFormat = AppContext.getContext().getResources().getString(R.string.activity_shop_manager_address);
        String wifiFormat = AppContext.getContext().getResources().getString(R.string.activity_verify_center_binding_number);
        String logoSrc = myShop.logoSrc;
        LogUtils.i("LOGO地址:" + logoSrc);
        if (logoSrc != null) {
            if (logoSrc.trim().length() > 0) {
//                ImageRequest imageRequest = new ImageRequest(logoSrc, new Response.Listener<Bitmap>() {
//                    @Override
//                    public void onResponse(Bitmap response) {
//                        //给imageView设置图片
//                        ivLogo.setImageBitmap(response);
//                    }
//                }, 128, 128, ImageView.ScaleType.FIT_XY, Bitmap.Config.RGB_565, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        //设置一张错误的图片，临时用ic_launcher代替
//                        ivLogo.setImageResource(R.drawable.shop_sys_logo);
//                    }
//                });
//                MyCmdUtil.getRequestQueue().add(imageRequest);
                MyCmdUtil.getImageLoader().get(logoSrc, ImageLoader.getImageListener(ivLogo,
                        R.drawable.shop_sys_logo, R.drawable.shop_sys_logo), 128, 128, ImageView.ScaleType.FIT_XY);
            }

        }

        tvManagerShopAddress.setText(String.format(addrFormat, myShop.address));
        if (apList == null || apList.size() == 0) {
            tvManagerShopWifiNumber.setText("未绑定");
        } else {
            tvManagerShopWifiNumber.setText(String.format(wifiFormat, apList.size()));
        }
    }

    private void setListener() {
        rlManagerShopEdit.setOnClickListener(this);
        rlManagerWifiEdit.setOnClickListener(this);
        btnManagerDeleteShop.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.rl_manager_shop_edit:
                startShopDetailActivity();
                break;
            case R.id.rl_manager_wifi_edit:
                startWifiDetailActivity();
                break;
            case R.id.btn_manager_delete_shop:
                showDeleteShopDialog();
                break;
        }
    }

    private void showDeleteShopDialog() {
        final MessageDialog messageDialog = new MessageDialog(this, R.style.CustomDialogStyle);
        messageDialog.show();
        Window window = messageDialog.getWindow();
        window.setWindowAnimations(R.style.CustomDialogAnimationStyle);
        TextView tvDialogTitle = (TextView) messageDialog.findViewById(R.id.tv_dialog_title);
        TextView tvDialogNotice = (TextView) messageDialog.findViewById(R.id.tv_delete_notice);
        Button tvOk = (Button) messageDialog.findViewById(R.id.tv_ok);
        Button tvCancel = (Button) messageDialog.findViewById(R.id.tv_cancel);

        tvDialogTitle.setText(AppContext.getContext().getResources().getString(R.string.activity_shop_manager_dialog_title));
        tvDialogNotice.setText(AppContext.getContext().getResources().getString(R.string.activity_shop_manager_dialog_message));

        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteShop();
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

    private void startWifiDetailActivity() {
        WifiDetailActivity.launch(ShopManagerActivity.this, myShop, apList);
    }

    private void startShopDetailActivity() {
        ShopDetailActivity.launch(ShopManagerActivity.this, myShop);
    }

    private void deleteShop() {
        String accessToken = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_TOKEN, null);
        String json = com.sharedream.wifiguard.cmdws.CmdDeleteShop.createRequestJson(accessToken, myShop.shopId);
        LogUtils.d("delete shop request >>>>> " + json);
        MyCmdUtil.sendRandomTagRequest(Constant.URL_WS_DELETE_SHOP, json, new MyCmdHttpTask.CmdListener() {
            @Override
            public void onCmdExecuted(String responseResult) {
                if (!TextUtils.isEmpty(responseResult)) {
                    LogUtils.d("delete shop response >>>>> " + responseResult);
                    handleDeleteShopResults(responseResult);
                }
            }

            @Override
            public void onCmdException(Throwable exception) {
                LogUtils.d("delete shop exception >>>>> " + exception.getMessage());
            }
        });
        //        String json = CmdDeleteShop.createRequestJson(myShop.shopId);
        //        LogUtils.d("删除商户request json >>>>> " + json);
        //        CmdUtil.sendRandomTagRequest(Constant.URL_CMD_DELETE_SHOP, json, new BaseCmdHttpTask.CmdListener() {
        //            @Override
        //            public void onCmdExecuted(String responseResult) {
        //                if (!TextUtils.isEmpty(responseResult)) {
        //                    LogUtils.d("删除商户response json >>>>> " + responseResult);
        //                    handleDeleteShopResults(responseResult);
        //                }
        //            }
        //
        //            @Override
        //            public void onCmdException(Exception exception) {
        //
        //            }
        //        });
    }

    private void handleDeleteShopResults(String response) {
        com.sharedream.wifiguard.cmdws.CmdDeleteShop.Results results = com.sharedream.wifiguard.cmdws.CmdDeleteShop.parseResponseJson(response);
        //CmdDeleteShop.Results results = CmdDeleteShop.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            MyUtils.showToast("删除成功", Toast.LENGTH_SHORT, this);
            this.finish();
        } else if (results.code == -1) {
            MyUtils.showToast(results.msg, this);
        } else if (results.code == -2) {
            MyUtils.showToast(results.msg, this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_FIRST_USER && resultCode == RESULT_OK) {
            finish();
        }
//        else if (requestCode == Constant.REQUEST_CODE_WIFI_DETAIL && resultCode == RESULT_OK) {
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
                    refreshCurrentShop();
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
        //                    apList = (ArrayList<CmdGetMyShop.MyAp>) shop.apList;
        //                    refreshCurrentShop();
        //                    break;
        //                }
        //            }
        //        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean from_wifi_detail = GlobalField.restoreFieldBoolean(AppContext.getContext(), "from_wifi_detail", false);
        if (from_wifi_detail) {
            getMyShopFromServer();
        }
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_shop_manager;
    }

    @Override
    public String getActivityTitle() {
        return "商铺管理";
    }
}
