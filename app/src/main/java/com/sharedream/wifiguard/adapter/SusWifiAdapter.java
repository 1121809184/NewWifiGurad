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

import java.util.List;

public class SusWifiAdapter extends BaseAdapter {
    private List<ScanResult> scanResultList;

    public SusWifiAdapter(List<ScanResult> scanResultList) {
        this.scanResultList = scanResultList;
    }

    @Override
    public int getCount() {
        return scanResultList.size();
    }

    @Override
    public Object getItem(int position) {
        return scanResultList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(AppContext.getContext(), R.layout.item_sus_wifi, null);
            holder = new ViewHolder();
            holder.ivPoliceWifiSignal = (ImageView) convertView.findViewById(R.id.iv_police_wifi_signal);
            holder.tvPoliceWifiSsid = (TextView) convertView.findViewById(R.id.tv_police_wifi_ssid);
            holder.tvPoliceWifiMark = (TextView) convertView.findViewById(R.id.tv_police_wifi_mark);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ScanResult scanResult = scanResultList.get(position);

        holder.ivPoliceWifiSignal.setImageResource(R.drawable.wifi_signal_normal_class);
        int rssi = MyUtils.convert2RssiLevel(scanResult.level);
        holder.ivPoliceWifiSignal.getDrawable().setLevel(rssi);

        String capabilities = scanResult.capabilities;
        int wifiSecurityType = MyUtils.getWifiSecurityType(capabilities);
        if (wifiSecurityType == 0) {
            holder.tvPoliceWifiMark.setVisibility(View.VISIBLE);
        } else {
            holder.tvPoliceWifiMark.setVisibility(View.INVISIBLE);
        }

        holder.tvPoliceWifiSsid.setText(scanResult.SSID);
        return convertView;
    }

    static class ViewHolder {
        public ImageView ivPoliceWifiSignal;
        public TextView tvPoliceWifiSsid;
        public TextView tvPoliceWifiMark;
    }
}
