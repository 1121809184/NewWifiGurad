package com.sharedream.wifiguard.fragment;

import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.activity.AddShopActivity;
import com.sharedream.wifiguard.activity.RegisterActivity;
import com.sharedream.wifiguard.adapter.MyShopsDataAdapter;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.cmd.CmdGetMyShop;
import com.sharedream.wifiguard.conf.Constant;
import com.sharedream.wifiguard.task.BaseCmdHttpTask;
import com.sharedream.wifiguard.utils.CmdUtil;
import com.sharedream.wifiguard.utils.DisplayUtils;
import com.sharedream.wifiguard.utils.MyUtils;
import com.sharedream.wifiguard.widget.MyPopupWindow;

import java.util.ArrayList;
import java.util.List;

public class DataFragment extends BaseFragment {
    private ImageView ivBack;
    private RelativeLayout rlShops;
    private TextView tvTitle;
    private ImageView ivMore;

    private List<CmdGetMyShop.MyShop> myShopList;
    private List<String> myShopNameList;
    private MyShopsDataAdapter myShopsDataAdapter;
    private TextView tvMyShop;
    private TextView tvNewAddGuest;

    @Override
    public View initView() {
        View view = View.inflate(AppContext.getContext(), R.layout.fragment_data, null);
        ivBack = ((ImageView) view.findViewById(R.id.iv_back));
        rlShops = ((RelativeLayout) view.findViewById(R.id.rl_shops));
        tvTitle = ((TextView) view.findViewById(R.id.tv_title));
        ivMore = ((ImageView) view.findViewById(R.id.iv_more));
        tvMyShop = ((TextView) view.findViewById(R.id.tv_my_shop));
        initTitleBar();

        tvNewAddGuest = ((TextView) view.findViewById(R.id.tv_new_add_guest));
        return view;
    }

    private void initTitleBar() {
        ivBack.setVisibility(View.INVISIBLE);
        tvTitle.setVisibility(View.INVISIBLE);
        rlShops.setVisibility(View.VISIBLE);
        ivMore.setVisibility(View.INVISIBLE);
    }

    @Override
    public void initData() {
        myShopNameList = new ArrayList<String>();
        getMyShopFromServer();

        String format = AppContext.getContext().getResources().getString(R.string.fragment_data_new_person);
        tvNewAddGuest.setText(String.format(format, String.valueOf(9)));
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
            if (myShopList == null || myShopList.size() == 0) {
                tvMyShop.setText("您还没有添加商铺");
                rlShops.setClickable(false);
            } else {
                rlShops.setClickable(true);
                tvMyShop.setText(myShopList.get(0).shopName);
                for (CmdGetMyShop.MyShop myShop : myShopList) {
                    myShopNameList.add(myShop.shopName);
                }
                myShopsDataAdapter = new MyShopsDataAdapter(myShopNameList);
            }
        }
    }

    @Override
    public void setListener() {
        ivMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View popView = View.inflate(AppContext.getContext(), R.layout.dialog_more, null);
                int statusHeight = MyUtils.getStatusHeight(getActivity());
                int sh = DisplayUtils.px2dip(AppContext.getContext(), statusHeight);
                popView.setPadding(0, sh, 0, 0);

                final PopupWindow popupWindow = new PopupWindow(popView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                popupWindow.setTouchable(true);
                popupWindow.setBackgroundDrawable(new BitmapDrawable());
                popupWindow.showAtLocation(ivMore, Gravity.NO_GRAVITY, 0, 0);

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
                        RegisterActivity.launch(getActivity());
                        getActivity().finish();
                        popupWindow.dismiss();
                    }
                });
            }
        });

        rlShops.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View popView = View.inflate(AppContext.getContext(), R.layout.dialog_my_shops, null);
                int statusHeight = MyUtils.getStatusHeight(getActivity());

                final MyPopupWindow popupWindow = new MyPopupWindow(getActivity(),popView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                popupWindow.setTouchable(true);
                popupWindow.setBackgroundDrawable(new BitmapDrawable());
                popupWindow.showAtLocation(rlShops, Gravity.NO_GRAVITY, 0, statusHeight);

                ImageView ivMoreClose = ((ImageView) popView.findViewById(R.id.iv_more_icon));
                ListView lvMyShops = (ListView) popView.findViewById(R.id.lv_my_shops);
                lvMyShops.setAdapter(myShopsDataAdapter);
                lvMyShops.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        CmdGetMyShop.MyShop shop = myShopList.get(position);
                        tvMyShop.setText(shop.shopName);
                        popupWindow.dismiss();
                    }
                });

                ivMoreClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });
            }
        });

        if (myShopList == null || myShopList.size() == 0) {
            rlShops.setClickable(false);
        } else {
            rlShops.setClickable(true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ivMore.setVisibility(View.INVISIBLE);
    }
}
