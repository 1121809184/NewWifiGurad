package com.sharedream.wifiguard.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;

import java.util.List;

public class MyShopsDataAdapter extends BaseAdapter{
    public List<String> shopNameList;

    public MyShopsDataAdapter(List<String> shopNameList){
        this.shopNameList = shopNameList;
    }

    @Override
    public int getCount() {
        return shopNameList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            convertView = View.inflate(AppContext.getContext(), R.layout.item_dialog_my_shops,null);
            holder = new ViewHolder();
            holder.tvMyShopName = (TextView) convertView.findViewById(R.id.tv_my_shop_name);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvMyShopName.setText(shopNameList.get(position));
        return convertView;
    }

    static class ViewHolder{
        public TextView tvMyShopName;
    }
}
