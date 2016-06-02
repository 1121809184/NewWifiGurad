package com.sharedream.wifiguard.fragment;

import android.graphics.drawable.BitmapDrawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sharedream.wifiguard.MainActivity;
import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.activity.AddShopActivity;
import com.sharedream.wifiguard.activity.LoginActivity;
import com.sharedream.wifiguard.activity.OptimizeWifiActivity;
import com.sharedream.wifiguard.activity.PoliceActivity;
import com.sharedream.wifiguard.activity.SafeCheckActivity;
import com.sharedream.wifiguard.activity.ScanWifiActivity;
import com.sharedream.wifiguard.activity.SystemSettingsActivity;
import com.sharedream.wifiguard.activity.TestSpeedWithOptmizeActivity;
import com.sharedream.wifiguard.activity.WifiDisableActivity;
import com.sharedream.wifiguard.activity.WifiSpeedActivity;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.cmdws.CmdGetMyShop;
import com.sharedream.wifiguard.cmdws.MyCmdHttpTask;
import com.sharedream.wifiguard.cmdws.MyCmdUtil;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.dialog.MessageDialog;
import com.sharedream.wifiguard.listener.WifiObserver;
import com.sharedream.wifiguard.listener.WifiSubject;
import com.sharedream.wifiguard.manager.WukongWifiManager;
import com.sharedream.wifiguard.sqlite.DatabaseManager;
import com.sharedream.wifiguard.utils.GlobalField;
import com.sharedream.wifiguard.utils.LogUtils;
import com.sharedream.wifiguard.utils.MyUtils;
import com.sharedream.wifiguard.widget.MyPopupWindow;

import java.util.ArrayList;
import java.util.List;

public class OptimizeFragment extends BaseFragment implements View.OnClickListener, WifiObserver {
    private RelativeLayout rlShops;
    private ImageView ivBack;
    private ImageView ivMore;
    private TextView tvTitle;
    private RelativeLayout rlWifiSafeCheck;
    private RelativeLayout rlWifiSpeedCheck;
    private RelativeLayout rlWifiPolice;
    private RelativeLayout rlWifiProblem;
    private RelativeLayout rlCurrentWifiContainer;
    private LinearLayout llCurrentWifiDetail;
    private TextView tvCurrentWifiName;
    private LinearLayout llCurrentWifiDetailNo;
    private TextView tvCurrentWifiSsid;
    private boolean login;
    private RelativeLayout rlSysSetting;
    private String realSSID;
    private ArrayList<CmdGetMyShop.Shop> myShopList;
    private int totalAp;
    private int moreFlag = 0;

    @Override
    public View initView() {
        View view = View.inflate(AppContext.getContext(), R.layout.fragment_optimize, null);
        rlShops = ((RelativeLayout) view.findViewById(R.id.rl_shops));
        ivBack = ((ImageView) view.findViewById(R.id.iv_back));
        ivMore = ((ImageView) view.findViewById(R.id.iv_more));
        tvTitle = ((TextView) view.findViewById(R.id.tv_title));
        initTitleBar();

        rlWifiSafeCheck = ((RelativeLayout) view.findViewById(R.id.rl_wifi_safe_check));
        rlWifiSpeedCheck = ((RelativeLayout) view.findViewById(R.id.rl_wifi_speed_check));
        rlWifiPolice = ((RelativeLayout) view.findViewById(R.id.rl_wifi_police));
        rlWifiProblem = ((RelativeLayout) view.findViewById(R.id.rl_wifi_problem));
        rlSysSetting = ((RelativeLayout) view.findViewById(R.id.rl_sys_setting));
        rlCurrentWifiContainer = ((RelativeLayout) view.findViewById(R.id.rl_current_wifi_container));
        llCurrentWifiDetail = ((LinearLayout) view.findViewById(R.id.ll_current_wifi_detail));
        tvCurrentWifiName = ((TextView) view.findViewById(R.id.tv_current_wifi_name));
        llCurrentWifiDetailNo = ((LinearLayout) view.findViewById(R.id.ll_current_wifi_detail_no));
        tvCurrentWifiSsid = ((TextView) view.findViewById(R.id.tv_current_wifi_ssid));

        return view;
    }

    private void initTitleBar() {
        ivBack.setVisibility(View.INVISIBLE);
        rlShops.setVisibility(View.INVISIBLE);
        tvTitle.setVisibility(View.VISIBLE);
        ivMore.setVisibility(View.VISIBLE);
        tvTitle.setText(AppContext.getContext().getResources().getString(R.string.title_optimize));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        WifiSubject.getInstance().unregistObserver(this);
    }

    @Override
    public void initData() {
        WifiSubject.getInstance().registObserver(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        boolean isWifiConnected = MyUtils.isWifiConnected(AppContext.getContext());
        login = GlobalField.restoreFieldBoolean(AppContext.getContext(), Constant.INTENT_KEY_LOGIN, false);
        if (isWifiConnected) {
            refreshWifiConnectedLayout();
        } else {
            rlCurrentWifiContainer.setVisibility(View.GONE);
        }
        if (login) {
            getMyShopFromServer();
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
            totalAp = results.data.totalAp;
            myShopList = results.data.list;
            if (myShopList == null || myShopList.size() == 0) {

            } else {
                for (int i = 0; i < myShopList.size(); i++) {
                    CmdGetMyShop.Shop shop = myShopList.get(i);
                    ArrayList<CmdGetMyShop.Ap> apList = shop.apList;
                    if (apList != null) {
                        for (int k = 0; k < apList.size(); k++) {
                            CmdGetMyShop.Ap ap = apList.get(k);
                            if (realSSID.equals(ap.ssid)) {
                                rlCurrentWifiContainer.setVisibility(View.VISIBLE);
                                llCurrentWifiDetail.setVisibility(View.VISIBLE);
                                llCurrentWifiDetailNo.setVisibility(View.GONE);
                                tvCurrentWifiName.setText(realSSID);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private void refreshWifiConnectedLayout() {
        rlCurrentWifiContainer.setVisibility(View.VISIBLE);
        llCurrentWifiDetail.setVisibility(View.GONE);
        llCurrentWifiDetailNo.setVisibility(View.VISIBLE);
        WifiInfo wifiInfo = WukongWifiManager.getInstance().getConnectionInfo();
        realSSID = getRealSSID(wifiInfo.getSSID());
        tvCurrentWifiSsid.setText(realSSID);
    }

    @Override
    public void setListener() {
        rlWifiSafeCheck.setOnClickListener(this);
        rlWifiSpeedCheck.setOnClickListener(this);
        rlWifiPolice.setOnClickListener(this);
        rlWifiProblem.setOnClickListener(this);
        rlSysSetting.setOnClickListener(this);

        ivMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_MORE_ADD_SHOP, true);
                View popView = View.inflate(AppContext.getContext(), R.layout.dialog_more, null);
                int statusHeight = MyUtils.getStatusHeight(getActivity());

                final MyPopupWindow popupWindow = new MyPopupWindow(getActivity(), popView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                popupWindow.setTouchable(true);
                popupWindow.setBackgroundDrawable(new BitmapDrawable());
                popupWindow.showAtLocation(ivMore, Gravity.NO_GRAVITY, 0, statusHeight);

                ImageView ivMoreClose = ((ImageView) popView.findViewById(R.id.iv_more_icon));
                TextView tvMoreAddShop = ((TextView) popView.findViewById(R.id.tv_more_add_shop));
                TextView tvMoreLogout = ((TextView) popView.findViewById(R.id.tv_more_log_out));
                tvMoreAddShop.setVisibility(View.GONE);

                if (login) {
                    tvMoreLogout.setText("退出登录");
                    moreFlag = 1;
                } else {
                    tvMoreLogout.setText("登录");
                    moreFlag = 0;
                }

                ivMoreClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });

                tvMoreAddShop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AddShopActivity.launch(getActivity());
                        popupWindow.dismiss();
                    }
                });

                tvMoreLogout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (moreFlag == 0) {
                            LoginActivity.launch(getActivity());
                        } else if (moreFlag == 1) {
                            //                            DatabaseManager.logout();
                            //                            LoginActivity.launch(getActivity());
                            //                            getActivity().finish();
                            showLogoutDialog();
                        }
                        popupWindow.dismiss();
                    }
                });
            }
        });
    }

    private void quit() {
        getActivity().finish();
        DatabaseManager.logout();
        GlobalField.saveField(AppContext.getContext(), Constant.INTENT_KEY_LOGIN, false);
        MainActivity.launch(getActivity());
    }

    private void showLogoutDialog() {
        final MessageDialog messageDialog = new MessageDialog(getActivity(), R.style.CustomDialogStyle);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_wifi_safe_check:
                startSafeCheckActivity();
                break;
            case R.id.rl_wifi_speed_check:
                startWifiSpeedActivity();
                break;
            case R.id.rl_wifi_police:
                startPoliceActivity();
                break;
            case R.id.rl_wifi_problem:
                startScanWifiActivity();
                break;
            case R.id.rl_sys_setting:
                stratSystemSettingsActivity();
                break;
        }
    }

    private String getRealSSID(String ssid) {
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            return ssid.substring(1, ssid.length() - 1);
        } else {
            return ssid;
        }
    }

    private void stratSystemSettingsActivity() {
        SystemSettingsActivity.launch(getActivity());
    }

    private void startScanWifiActivity() {
        boolean enabled = WukongWifiManager.getInstance().isWifiEnabled();
        if (enabled) {
            ScanWifiActivity.launch(getActivity());
        } else {
            WifiDisableActivity.launch(getActivity(), "ScanWifiActivity");
        }
    }

    private void startPoliceActivity() {
        boolean enabled = WukongWifiManager.getInstance().isWifiEnabled();
        if (enabled) {
            PoliceActivity.launch(getActivity());
        } else {
            WifiDisableActivity.launch(getActivity(), "PoliceActivity");
        }
    }

    private void startSafeCheckActivity() {
        boolean enabled = WukongWifiManager.getInstance().isWifiEnabled();
        if (enabled) {
            SafeCheckActivity.launch(getActivity());
        } else {
            WifiDisableActivity.launch(getActivity(), "SafeCheckActivity");
        }
    }

    private void startWifiSpeedActivity() {
        boolean enabled = WukongWifiManager.getInstance().isWifiEnabled();
        if (enabled) {
            WifiSpeedActivity.launch(getActivity());
        } else {
            WifiDisableActivity.launch(getActivity(), "WifiSpeedActivity");
        }
    }

    private void startOptimizeWifiActivity() {
        boolean enabled = WukongWifiManager.getInstance().isWifiEnabled();
        if (enabled) {
            OptimizeWifiActivity.launch(getActivity());
        } else {
            WifiDisableActivity.launch(getActivity(), "OptimizeWifiActivity");
        }
    }

    @Override
    public void onWifiOpen() {

    }

    @Override
    public void onWifiClose() {
        rlCurrentWifiContainer.setVisibility(View.GONE);
    }

    @Override
    public void onWifiDisconnected() {
        rlCurrentWifiContainer.setVisibility(View.GONE);
    }

    @Override
    public void onWifiConnecting() {

    }

    @Override
    public void onWifiPasswordVerifying() {

    }

    @Override
    public void onWifiCompleted() {

    }

    @Override
    public void onWifiPasswordIncorrect() {

    }

    @Override
    public void onWifiPasswordCorrect() {

    }

    @Override
    public void onWifiIpObtaining() {

    }

    @Override
    public void onWifiConnected() {
        refreshWifiConnectedLayout();
    }

    @Override
    public void onWifiConnectTimeout() {

    }

    @Override
    public void onWifiScanResultChanged(List<ScanResult> listResult, List<WifiConfiguration> listWifiConfiguration) {

    }

    @Override
    public void onAvailableWifiFound(String ssid) {

    }

    @Override
    public void onAvailableWifiNotFound() {

    }
}
