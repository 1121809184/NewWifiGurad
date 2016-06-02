package com.sharedream.wifiguard.fragment;

import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.activity.AddShopActivity;
import com.sharedream.wifiguard.activity.NoShopsActivity;
import com.sharedream.wifiguard.activity.RegisterActivity;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.cmd.CmdGetMyShop;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.sqlite.DatabaseManager;
import com.sharedream.wifiguard.task.BaseCmdHttpTask;
import com.sharedream.wifiguard.utils.CmdUtil;
import com.sharedream.wifiguard.utils.GlobalField;
import com.sharedream.wifiguard.utils.MyUtils;
import com.sharedream.wifiguard.widget.MyPopupWindow;

import java.util.List;

public class SecurityFragment extends BaseFragment implements View.OnClickListener {
    private ImageView ivBack;
    private RelativeLayout rlShops;
    private ImageView ivMore;
    private TextView tvTitle;

    private List<CmdGetMyShop.MyShop> myShopList;
    private RelativeLayout rlWifiVerify;


    @Override
    public View initView() {
        View view = View.inflate(AppContext.getContext(), R.layout.fragment_security, null);
        ivBack = ((ImageView) view.findViewById(R.id.iv_back));
        ivMore = ((ImageView) view.findViewById(R.id.iv_more));
        tvTitle = ((TextView) view.findViewById(R.id.tv_title));
        rlShops = ((RelativeLayout) view.findViewById(R.id.rl_shops));
        initTitleBar();

        rlWifiVerify = ((RelativeLayout) view.findViewById(R.id.rl_wifi_verify));

        return view;
    }

    private void initTitleBar() {
        ivBack.setVisibility(View.INVISIBLE);
        rlShops.setVisibility(View.INVISIBLE);
        tvTitle.setVisibility(View.VISIBLE);
        ivMore.setVisibility(View.VISIBLE);
        tvTitle.setText(AppContext.getContext().getResources().getString(R.string.title_security));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ivMore.setVisibility(View.INVISIBLE);
    }

    @Override
    public void initData() {
        getMyShopFromServer();
    }

    private void getMyShopFromServer() {
        String json = CmdGetMyShop.createRequestJson();
        CmdUtil.sendRandomTagRequest(Constant.URL_CMD_MY_SHOP, json, new BaseCmdHttpTask.CmdListener() {
            @Override
            public void onCmdExecuted(String responseResult) {
                if (!TextUtils.isEmpty(responseResult)) {
                    handleGetMyShopResults(responseResult);
                }
            }

            @Override
            public void onCmdException(Exception exception) {

            }
        });
    }

    private void handleGetMyShopResults(String response) {
        CmdGetMyShop.Results results = CmdGetMyShop.parseResponseJson(response);
        if (results == null) {
            return;
        }
        if (results.code == Constant.SERVER_SUCCESS_CODE) {
            myShopList = results.data.shopList;
        }
    }

    @Override
    public void setListener() {
        rlWifiVerify.setOnClickListener(this);

        ivMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalField.saveField(AppContext.getContext(), Constant.SP_KEY_MORE_ADD_SHOP, true);
                View popView = View.inflate(AppContext.getContext(), R.layout.dialog_more, null);
                int statusHeight = MyUtils.getStatusHeight(getActivity());

                final MyPopupWindow popupWindow = new MyPopupWindow(getActivity(),popView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
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
                        getActivity().finish();
                        popupWindow.dismiss();
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_wifi_verify:
                startMerchantActivity();
                break;
        }
    }


    private void startMerchantActivity() {
        if (myShopList == null || myShopList.size() == 0) {
            NoShopsActivity.launch(getActivity());
        } else {

        }
    }

}
