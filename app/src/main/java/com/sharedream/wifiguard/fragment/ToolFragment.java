package com.sharedream.wifiguard.fragment;

import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sharedream.wifi.sdk.activity.WifiManagerActivity;
import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.activity.AddShopActivity;
import com.sharedream.wifiguard.activity.RegisterActivity;
import com.sharedream.wifiguard.activity.UserManagementActivity;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.sqlite.DatabaseManager;
import com.sharedream.wifiguard.utils.GlobalField;
import com.sharedream.wifiguard.utils.MyUtils;
import com.sharedream.wifiguard.widget.MyPopupWindow;

public class ToolFragment extends BaseFragment implements View.OnClickListener {
    private ImageView ivBack;
    private RelativeLayout rlShops;
    private ImageView ivMore;
    private TextView tvTitle;
    private RelativeLayout rlToolOneKeyWifi;
    private RelativeLayout rlToolUserManagement;

    @Override
    public View initView() {
        View view = View.inflate(AppContext.getContext(), R.layout.fragment_tool, null);
        ivBack = ((ImageView) view.findViewById(R.id.iv_back));
        ivMore = ((ImageView) view.findViewById(R.id.iv_more));
        tvTitle = ((TextView) view.findViewById(R.id.tv_title));
        rlShops = ((RelativeLayout) view.findViewById(R.id.rl_shops));
        initTitleBar();

        rlToolOneKeyWifi = ((RelativeLayout) view.findViewById(R.id.rl_tool_one_key_wifi));
        rlToolUserManagement = ((RelativeLayout) view.findViewById(R.id.rl_tool_setting));

        return view;
    }

    private void initTitleBar() {
        ivBack.setVisibility(View.INVISIBLE);
        rlShops.setVisibility(View.INVISIBLE);
        tvTitle.setVisibility(View.VISIBLE);
        ivMore.setVisibility(View.VISIBLE);
        tvTitle.setText(AppContext.getContext().getResources().getString(R.string.title_tool));
    }

    @Override
    public void initData() {

    }

    @Override
    public void setListener() {
        rlToolOneKeyWifi.setOnClickListener(this);
        rlToolUserManagement.setOnClickListener(this);

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
            case R.id.rl_tool_one_key_wifi:
                WifiManagerActivity.launch(getActivity());
                break;
            case R.id.rl_tool_setting:
                UserManagementActivity.launch(getActivity());
                break;
        }
    }
}
