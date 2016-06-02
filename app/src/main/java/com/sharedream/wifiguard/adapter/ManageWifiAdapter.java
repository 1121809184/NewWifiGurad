package com.sharedream.wifiguard.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.cmdws.CmdGetMyShop;

import java.util.List;

public class ManageWifiAdapter extends BaseAdapter {

    private List<CmdGetMyShop.Ap> myApList;

    public ManageWifiAdapter(List<CmdGetMyShop.Ap> apList) {
        this.myApList = apList;
    }

    public void setMyApList(List<CmdGetMyShop.Ap> apList) {
        this.myApList = apList;
    }

    @Override
    public int getCount() {
        return myApList.size();
    }

    @Override
    public Object getItem(int position) {
        return myApList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(AppContext.getContext(), R.layout.item_manager_my_wifi_list, null);
            holder = new ViewHolder();
            holder.tvMyWifiName = (TextView) convertView.findViewById(R.id.tv_my_wifi_name);
            holder.tvMyWifiPlace = (TextView) convertView.findViewById(R.id.tv_my_wifi_place);
            holder.btnManageWifi = (TextView) convertView.findViewById(R.id.btn_manage_wifi);
            holder.tvLevel = (TextView) convertView.findViewById(R.id.tv_level);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        com.sharedream.wifiguard.cmdws.CmdGetMyShop.Ap myAp = myApList.get(position);
        int status = myAp.status;
        int level = myAp.level;
        holder.tvMyWifiName.setText(myAp.ssid);
        if ("".equals(myAp.place)) {
            holder.tvMyWifiPlace.setVisibility(View.GONE);
        } else {
            holder.tvMyWifiPlace.setVisibility(View.VISIBLE);
            holder.tvMyWifiPlace.setText(myAp.place);
        }
        if (status == 1) {
            if (Math.abs(level) >= 85) {
                holder.tvLevel.setText("信号弱");
            } else if (Math.abs(level) >= 70) {
                holder.tvLevel.setText("信号一般");
            } else {
                holder.tvLevel.setText("信号强");
            }
        } else if (status == 0) {
            holder.tvLevel.setText("不在附近");
        }

        return convertView;
    }

    static class ViewHolder {
        public TextView tvMyWifiName;//tv_my_wifi_place
        public TextView tvMyWifiPlace;
        public TextView btnManageWifi;
        public TextView tvLevel;
    }
}
