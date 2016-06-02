package com.sharedream.wifiguard.adapter;


import android.net.wifi.ScanResult;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.utils.MyUtils;

import java.util.HashMap;
import java.util.List;

public class PoliceWifiAdapter extends BaseAdapter {
    private List<ScanResult> listWifi;
    private HashMap<Integer, Integer> selectState;

    public PoliceWifiAdapter(List<ScanResult> listWifi,HashMap<Integer, Integer> selectState) {
        this.listWifi = listWifi;
        this.selectState = selectState;
    }

    public void setListWifi(List<ScanResult> listWifi) {
        this.listWifi = listWifi;
    }

    public void setSelectState(HashMap<Integer, Integer> selectState) {
        this.selectState = selectState;
    }

    @Override
    public int getCount() {
        return listWifi.size();
    }

    @Override
    public Object getItem(int position) {
        return listWifi.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(AppContext.getContext(), R.layout.item_police_wifi_list, null);
            holder = new ViewHolder();
            holder.ivPoliceWifiSignal = (ImageView) convertView.findViewById(R.id.iv_police_wifi_signal);
            holder.tvPoliceWifiSsid = (TextView) convertView.findViewById(R.id.tv_police_wifi_ssid);
            holder.ivPoliceWifiSelect = (ImageView) convertView.findViewById(R.id.iv_police_wifi_select);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ScanResult scanResult = listWifi.get(position);

        holder.ivPoliceWifiSignal.setImageResource(R.drawable.wifi_signal_normal_class);
        int rssi = MyUtils.convert2RssiLevel(scanResult.level);
        holder.ivPoliceWifiSignal.getDrawable().setLevel(rssi);

        Integer state = selectState.get(position);
        if (state == 0) {
            holder.ivPoliceWifiSelect.setVisibility(View.INVISIBLE);
        } else if (state == 1) {
            holder.ivPoliceWifiSelect.setVisibility(View.VISIBLE);
        }
        holder.tvPoliceWifiSsid.setText(scanResult.SSID);
        return convertView;
    }

    static class ViewHolder {
        public ImageView ivPoliceWifiSignal;
        public TextView tvPoliceWifiSsid;
        public ImageView ivPoliceWifiSelect;
    }
}
