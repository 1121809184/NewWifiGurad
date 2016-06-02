package com.sharedream.wifiguard.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.cmd.CmdShopList;

import java.util.List;

public class ShopListAdapter extends BaseAdapter {
    private List<CmdShopList.Shop> shopList;

    public ShopListAdapter(List<CmdShopList.Shop> shopList) {
        this.shopList = shopList;
    }

    public void setShopList(List<CmdShopList.Shop> shopList) {
        this.shopList = shopList;
    }

    @Override
    public int getCount() {
        return shopList.size();
    }

    @Override
    public Object getItem(int position) {
        return shopList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        ViewHolder holder = null;
        if (convertView == null) {
            view = View.inflate(AppContext.getContext(), R.layout.item_shop_list, null);
            holder = new ViewHolder();
            holder.tvShopName = (TextView) view.findViewById(R.id.tv_shop_name);
            holder.tvShopAddress = (TextView) view.findViewById(R.id.tv_shop_address);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }
        CmdShopList.Shop shop = shopList.get(position);
        holder.tvShopName.setText(shop.shopName);
        holder.tvShopAddress.setText(shop.address);
        return view;
    }

    static class ViewHolder {
        public TextView tvShopName;
        public TextView tvShopAddress;
    }
}
