package com.sharedream.wifiguard.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.SystemClock;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.activity.AddShopActivity;
import com.sharedream.wifiguard.activity.LoginActivity;
import com.sharedream.wifiguard.activity.RegisterActivity;
import com.sharedream.wifiguard.activity.ShopManagerActivity;
import com.sharedream.wifiguard.adapter.VerifyShopListAdapter;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.cmd.CmdDeleteShop;
import com.sharedream.wifiguard.cmdws.CmdGetMyShop;
import com.sharedream.wifiguard.cmdws.MyCmdHttpTask;
import com.sharedream.wifiguard.cmdws.MyCmdUtil;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.dialog.MessageDialog;
import com.sharedream.wifiguard.sqlite.DatabaseManager;
import com.sharedream.wifiguard.task.BaseCmdHttpTask;
import com.sharedream.wifiguard.utils.CmdUtil;
import com.sharedream.wifiguard.utils.GlobalField;
import com.sharedream.wifiguard.utils.LogUtils;
import com.sharedream.wifiguard.utils.MyUtils;
import com.sharedream.wifiguard.version.VersionManager;
import com.sharedream.wifiguard.widget.MyPopupWindow;
import com.sharedream.wifiguard.widget.MySwipeRefreshLauoyt;

import java.util.ArrayList;
import java.util.List;

public class VerifyCenterFragment extends BaseFragment implements VerifyShopListAdapter.OnEditClickListener, View.OnClickListener, AdapterView.OnItemClickListener {
    private ListView lvShopAndWifi;
    private TextView tvBindingMyShopNotice;
    private RelativeLayout rlShops;
    private ImageView ivBack;
    private ImageView ivMore;
    private TextView tvTitle;
    private RelativeLayout llNoShopsContainer;
    private Button btnAddSomeShops;
    private ProgressDialog progressDialog;
    private LinearLayout llFootView;

    private List<com.sharedream.wifiguard.cmdws.CmdGetMyShop.Shop> myShopList;
    private VerifyShopListAdapter verifyShopListAdapter;
    private String bindingFinishNotice;
    private String bindingNoShopsNotice;
    private boolean login;
    public static boolean hasCheckVersion;
    private TextView tvNologinNotice;
    private MySwipeRefreshLauoyt srlVerifyLayout;

    private void initTitleBar() {
        ivBack.setVisibility(View.INVISIBLE);
        rlShops.setVisibility(View.GONE);
        tvTitle.setVisibility(View.VISIBLE);
        ivMore.setVisibility(View.INVISIBLE);
        tvTitle.setText("管理中心");
    }

    @Override
    public View initView() {
        View view = View.inflate(AppContext.getContext(), R.layout.fagment_verify_center, null);
        srlVerifyLayout = ((MySwipeRefreshLauoyt) view.findViewById(R.id.srl_verify_center));

        srlVerifyLayout.setColorSchemeResources(R.color.theme_color);
        lvShopAndWifi = ((ListView) view.findViewById(R.id.lv_shop_and_wifi));
        srlVerifyLayout.setViewGroup(lvShopAndWifi);

        rlShops = ((RelativeLayout) view.findViewById(R.id.rl_shops));
        ivBack = ((ImageView) view.findViewById(R.id.iv_back));
        ivMore = ((ImageView) view.findViewById(R.id.iv_more));
        tvTitle = ((TextView) view.findViewById(R.id.tv_title));
        initTitleBar();

        tvBindingMyShopNotice = ((TextView) view.findViewById(R.id.tv_binding_my_shop_notice));
        llNoShopsContainer = ((RelativeLayout) view.findViewById(R.id.ll_no_shops_container));
        btnAddSomeShops = ((Button) view.findViewById(R.id.btn_add_some_shops));
        tvNologinNotice = ((TextView) view.findViewById(R.id.tv_no_login_notice));

        View footView = View.inflate(AppContext.getContext(), R.layout.foot_view_add_shop, null);
        llFootView = ((LinearLayout) footView.findViewById(R.id.ll_foot_view));
        llFootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddShopActivity.class);
                getActivity().startActivityForResult(intent,Constant.REQUEST_CODE_ADD_SHOP);
            }
        });

        ivMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View popView = View.inflate(AppContext.getContext(), R.layout.dialog_more, null);
                int statusHeight = MyUtils.getStatusHeight(getActivity());

                final MyPopupWindow popupWindow = new MyPopupWindow(getActivity(), popView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                popupWindow.setTouchable(true);
                popupWindow.setBackgroundDrawable(new BitmapDrawable());
                popupWindow.showAtLocation(ivMore, Gravity.NO_GRAVITY, 0, statusHeight);

                ImageView ivMoreClose = ((ImageView) popView.findViewById(R.id.iv_more_icon));
                TextView tvMoreAddShop = ((TextView) popView.findViewById(R.id.tv_more_add_shop));
                TextView tvMoreLogout = ((TextView) popView.findViewById(R.id.tv_more_log_out));

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
                        DatabaseManager.logout();
                        RegisterActivity.launch(getActivity());
                        popupWindow.dismiss();
                    }
                });
            }
        });

        return view;
    }

    public void initData() {
        myShopList = new ArrayList<com.sharedream.wifiguard.cmdws.CmdGetMyShop.Shop>();
        bindingFinishNotice = AppContext.getContext().getResources().getString(R.string.activity_verify_center_binding_already);
        bindingNoShopsNotice = AppContext.getContext().getResources().getString(R.string.activity_verify_center_binding_no_shops);

        login = GlobalField.restoreFieldBoolean(AppContext.getContext(), Constant.INTENT_KEY_LOGIN, false);
        refreshView();
        if (!hasCheckVersion) {
            new VersionManager(getActivity(), ivMore, tvNologinNotice, false).checkVersion();
        }
    }

    private void refreshView() {
        if (login) {
            tvBindingMyShopNotice.setText(bindingNoShopsNotice);
            llNoShopsContainer.setVisibility(View.INVISIBLE);
            lvShopAndWifi.setVisibility(View.INVISIBLE);
            tvNologinNotice.setVisibility(View.INVISIBLE);
            btnAddSomeShops.setText(AppContext.getContext().getResources().getString(R.string.item_add_new_shop));
            srlVerifyLayout.setEnabled(false);
        } else {
            tvBindingMyShopNotice.setText(bindingNoShopsNotice);
            llNoShopsContainer.setVisibility(View.VISIBLE);
            lvShopAndWifi.setVisibility(View.INVISIBLE);
            tvNologinNotice.setVisibility(View.VISIBLE);
            btnAddSomeShops.setText(AppContext.getContext().getResources().getString(R.string.item_add_new_shop_no_login));
            srlVerifyLayout.setEnabled(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        login = GlobalField.restoreFieldBoolean(AppContext.getContext(), Constant.INTENT_KEY_LOGIN, false);
        refreshView();
        if (login) {
            LogUtils.i("哈");
            getMyShopFromServer();
        }
    }

    private void getMyShopFromServer() {
        srlVerifyLayout.setEnabled(true);
        progressDialog = new ProgressDialog(getActivity(), ProgressDialog.THEME_HOLO_LIGHT);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("正在加载，请稍后.....");
        progressDialog.show();

        if (myShopList != null && verifyShopListAdapter != null) {
            llNoShopsContainer.setVisibility(View.INVISIBLE);
            lvShopAndWifi.setVisibility(View.VISIBLE);
            verifyShopListAdapter.setMyShopList(myShopList);
            verifyShopListAdapter.notifyDataSetChanged();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 5; i++) {
                    SystemClock.sleep(1000);
                }
                if (progressDialog.isShowing()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            MyUtils.showToast("当前网络不通，请尝试连接其他网络", getActivity());
                            srlVerifyLayout.setRefreshing(false);

                            if (myShopList != null && verifyShopListAdapter != null) {
                                llNoShopsContainer.setVisibility(View.INVISIBLE);
                                lvShopAndWifi.setVisibility(View.VISIBLE);
                                verifyShopListAdapter.setMyShopList(myShopList);
                                verifyShopListAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }

            }
        }).start();

        String accessToken = GlobalField.restoreFieldString(AppContext.getContext(), Constant.SP_KEY_ACCESS_TOKEN, null);
        String json = com.sharedream.wifiguard.cmdws.CmdGetMyShop.createRequestJson(accessToken);
        LogUtils.d("get my shops request >>> " + json);
        MyCmdUtil.sendRandomTagRequest(Constant.URL_WS_MY_SHOPS, json, new MyCmdHttpTask.CmdListener() {
            @Override
            public void onCmdExecuted(String responseResult) {
                srlVerifyLayout.setRefreshing(false);
                progressDialog.dismiss();

                if (!TextUtils.isEmpty(responseResult)) {
                    LogUtils.d("get my shops response >>> " + responseResult);
                    handleGetMyShopResults(responseResult);
                }
            }

            @Override
            public void onCmdException(Throwable exception) {
                srlVerifyLayout.setRefreshing(false);
                progressDialog.dismiss();

                MyUtils.showToast("网络异常，请检查网络", getActivity());
                srlVerifyLayout.setRefreshing(false);

                llNoShopsContainer.setVisibility(View.INVISIBLE);
                lvShopAndWifi.setVisibility(View.VISIBLE);
                verifyShopListAdapter.setMyShopList(myShopList);
                verifyShopListAdapter.notifyDataSetChanged();
                LogUtils.d("get my shops exception >>> " + exception.getMessage());
            }
        });
        //        String json = CmdGetMyShop.createRequestJson();
        //        LogUtils.d("我的商店request >>>>> " + json);
        //        CmdUtil.sendRandomTagRequest(Constant.URL_CMD_MY_SHOP, json, new BaseCmdHttpTask.CmdListener() {
        //            @Override
        //            public void onCmdExecuted(String responseResult) {
        //                progressDialog.dismiss();
        //                if (!TextUtils.isEmpty(responseResult)) {
        //                    LogUtils.d("我的商店response >>>>> " + responseResult);
        //                    handleGetMyShopResults(responseResult);
        //                }
        //            }
        //
        //            @Override
        //            public void onCmdException(Exception exception) {
        //                progressDialog.dismiss();
        //            }
        //        });
    }

    private void handleGetMyShopResults(String response) {
        com.sharedream.wifiguard.cmdws.CmdGetMyShop.Results results = com.sharedream.wifiguard.cmdws.CmdGetMyShop.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            myShopList = results.data.list;
            if (myShopList == null || myShopList.size() == 0) {
                llNoShopsContainer.setVisibility(View.VISIBLE);
                lvShopAndWifi.setVisibility(View.INVISIBLE);
                tvBindingMyShopNotice.setText(bindingNoShopsNotice);
                srlVerifyLayout.setEnabled(false);

            } else {
                llNoShopsContainer.setVisibility(View.INVISIBLE);
                lvShopAndWifi.setVisibility(View.VISIBLE);
                tvBindingMyShopNotice.setText(String.format(bindingFinishNotice, myShopList.size(), results.data.totalAp));
                if (verifyShopListAdapter == null) {
                    verifyShopListAdapter = new VerifyShopListAdapter(myShopList);
                    lvShopAndWifi.addFooterView(llFootView);
                    lvShopAndWifi.setAdapter(verifyShopListAdapter);
                } else {
                    verifyShopListAdapter.setMyShopList(myShopList);
                    verifyShopListAdapter.notifyDataSetChanged();
                }
                verifyShopListAdapter.setOnEditClickListener(this);
            }
        } else if (results.code == -2) {
            login = false;
            refreshView();
        }
        //        CmdGetMyShop.Results results = CmdGetMyShop.parseResponseJson(response);
        //        if (results == null) {
        //            return;
        //        }
        //        if (results.code == Constant.SERVER_SUCCESS_CODE) {
        //            myShopList = results.data.shopList;
        //            if (myShopList == null || myShopList.size() == 0) {//暂未添加
        //                llNoShopsContainer.setVisibility(View.VISIBLE);
        //                lvShopAndWifi.setVisibility(View.INVISIBLE);
        //                tvBindingMyShopNotice.setText(bindingNoShopsNotice);
        //            } else {
        //                llNoShopsContainer.setVisibility(View.INVISIBLE);
        //                lvShopAndWifi.setVisibility(View.VISIBLE);
        //                tvBindingMyShopNotice.setText(String.format(bindingFinishNotice, myShopList.size(), results.data.totalAp));
        //                if (verifyShopListAdapter == null) {
        //                    verifyShopListAdapter = new VerifyShopListAdapter(myShopList);
        //                    lvShopAndWifi.addFooterView(llFootView);
        //                    lvShopAndWifi.setAdapter(verifyShopListAdapter);
        //                } else {
        //                    verifyShopListAdapter.setMyShopList(myShopList);
        //                    verifyShopListAdapter.notifyDataSetChanged();
        //                }
        //                verifyShopListAdapter.setOnEditClickListener(this);
        //            }
        //        }
    }

    public void setListener() {
        lvShopAndWifi.setOnItemClickListener(this);
        btnAddSomeShops.setOnClickListener(this);
        srlVerifyLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LogUtils.i("哈哈");
                getMyShopFromServer();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_add_some_shops:
                if (login) {
                    AddShopActivity.launch(getActivity());
                } else {
                    //LoginActivity.launch(getActivity());
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    getActivity().startActivity(intent);
                }
                break;
        }
    }

    private void showDeleteDialog(final int shopId) {
        final MessageDialog messageDialog = new MessageDialog(getActivity(), R.style.CustomDialogStyle);
        messageDialog.show();
        Window window = messageDialog.getWindow();
        window.setWindowAnimations(R.style.CustomDialogAnimationStyle);
        Button tvOk = (Button) messageDialog.findViewById(R.id.tv_ok);
        Button tvCancel = (Button) messageDialog.findViewById(R.id.tv_cancel);

        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String json = CmdDeleteShop.createRequestJson(shopId);
                LogUtils.d("delete request json >>>>> " + json);
                CmdUtil.sendRandomTagRequest(Constant.URL_CMD_DELETE_SHOP, json, new BaseCmdHttpTask.CmdListener() {
                    @Override
                    public void onCmdExecuted(String responseResult) {
                        if (!TextUtils.isEmpty(responseResult)) {
                            LogUtils.d("delete response json >>>>> " + responseResult);
                            handleDeleteShopResults(responseResult);
                        }
                    }

                    @Override
                    public void onCmdException(Exception exception) {

                    }
                });
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
    public void onEditClick(int position) {
        editShop(position);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        editShop(position);
    }

    private void handleDeleteShopResults(String responseResult) {
        CmdDeleteShop.Results results = CmdDeleteShop.parseResponseJson(responseResult);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            getMyShopFromServer();
        }
    }

    private void editShop(int position){
        com.sharedream.wifiguard.cmdws.CmdGetMyShop.Shop myShop = myShopList.get(position);
        ArrayList<CmdGetMyShop.Ap> myApArrayList = myShop.apList;
        ShopManagerActivity.launch(getActivity(), myShop, myApArrayList);
    }
}
