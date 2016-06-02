package com.sharedream.wifiguard.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.cmdws.CmdShopCategory;

import java.util.List;

public class CategoryListAdapter extends BaseAdapter {
    private List<CmdShopCategory.BigCategory> bigCategoryList;
    private List<CmdShopCategory.SmallCategory> smallCategoryList;

    public CategoryListAdapter(List<CmdShopCategory.BigCategory> bigCategoryList, List<CmdShopCategory.SmallCategory> smallCategoryList) {
        this.bigCategoryList = bigCategoryList;
        this.smallCategoryList = smallCategoryList;
    }

    public void setBigCategoryList(List<CmdShopCategory.BigCategory> bigCategoryList) {
        this.bigCategoryList = bigCategoryList;
    }

    public void setSmallCategoryList(List<CmdShopCategory.SmallCategory> smallCategoryList) {
        this.smallCategoryList = smallCategoryList;
    }

    @Override
    public int getCount() {
        if (bigCategoryList != null) {
            return bigCategoryList.size();
        } else {
            return smallCategoryList.size();
        }
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
        if (convertView == null) {
            convertView = View.inflate(AppContext.getContext(), R.layout.item_category_list, null);
            holder = new ViewHolder();
            holder.tvCategoryName = (TextView) convertView.findViewById(R.id.tv_category_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (bigCategoryList != null) {
            holder.tvCategoryName.setText(bigCategoryList.get(position).name);
        } else {
            holder.tvCategoryName.setText(smallCategoryList.get(position).name);
        }

        return convertView;
    }

    static class ViewHolder {
        public TextView tvCategoryName;
    }
}
