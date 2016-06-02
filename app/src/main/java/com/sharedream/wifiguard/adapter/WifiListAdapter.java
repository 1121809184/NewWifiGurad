package com.sharedream.wifiguard.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.cmdws.CmdCheckWifiBind;
import com.sharedream.wifiguard.utils.MyUtils;

import java.util.List;

public class WifiListAdapter extends BaseAdapter {
    private List<CmdCheckWifiBind.Data> wifiList;

    public WifiListAdapter(List<CmdCheckWifiBind.Data> wifiList) {
        this.wifiList = wifiList;
    }

    public void setWifiList(List<CmdCheckWifiBind.Data> wifiList) {
        this.wifiList = wifiList;
    }

    @Override
    public int getCount() {
        return wifiList.size();
    }

    @Override
    public Object getItem(int position) {
        return wifiList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = View.inflate(AppContext.getContext(), R.layout.item_wifi_list, null);
            holder = new ViewHolder();
            holder.ivWifiSignal = (ImageView) convertView.findViewById(R.id.iv_wifi_signal);
            holder.tvWifiSsid = (TextView) convertView.findViewById(R.id.tv_wifi_ssid);
            holder.tvIsBinding = ((TextView) convertView.findViewById(R.id.tv_is_binding));
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        CmdCheckWifiBind.Data data = wifiList.get(position);
        String shopName = data.shopName;
        int rssi = MyUtils.convert2RssiLevel(data.level);

        if (data.status == -1) {
            holder.ivWifiSignal.setImageResource(R.drawable.wifi_signal_normal_class);
            holder.tvIsBinding.setTextColor(AppContext.getContext().getResources().getColor(R.color.theme_color));
            holder.tvWifiSsid.setTextColor(AppContext.getContext().getResources().getColor(R.color.binding_wifi_text_color));
            holder.tvIsBinding.setText(AppContext.getContext().getResources().getString(R.string.activity_binding_wifi_binding_no));
        } else {
            holder.ivWifiSignal.setImageResource(R.drawable.wifi_signal_binding_class);
            holder.tvIsBinding.setTextColor(AppContext.getContext().getResources().getColor(R.color.binding_wifi_text_bind_color));
            holder.tvWifiSsid.setTextColor(AppContext.getContext().getResources().getColor(R.color.binding_wifi_text_bind_color));
            //holder.tvIsBinding.setText(AppContext.getContext().getResources().getString(R.string.activity_binding_wifi_binding_yes));
            holder.tvIsBinding.setText(shopName);
        }
        holder.tvWifiSsid.setText(data.ssid);
        holder.ivWifiSignal.getDrawable().setLevel(rssi);
        return convertView;
    }

    static class ViewHolder {
        public ImageView ivWifiSignal;
        public TextView tvWifiSsid;
        public TextView tvIsBinding;
    }
}
