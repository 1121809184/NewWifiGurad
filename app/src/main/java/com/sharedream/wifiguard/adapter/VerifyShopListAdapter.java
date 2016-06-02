package com.sharedream.wifiguard.adapter;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.sharedream.wifiguard.R;
import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.cmdws.MyCmdUtil;
import com.sharedream.wifiguard.widget.CropSquareTransformation;
import com.squareup.picasso.Picasso;

import java.util.List;

public class VerifyShopListAdapter extends BaseAdapter {
    private List<com.sharedream.wifiguard.cmdws.CmdGetMyShop.Shop> myShopList;
    private OnEditClickListener onEditClickListener;
    private ImageLoader imageLoader;
    private RequestQueue requestQueue;

    public VerifyShopListAdapter(List<com.sharedream.wifiguard.cmdws.CmdGetMyShop.Shop> myShopList) {
        this.myShopList = myShopList;
        this.imageLoader = MyCmdUtil.getImageLoader();
        this.requestQueue = MyCmdUtil.getRequestQueue();
    }

    public void setMyShopList(List<com.sharedream.wifiguard.cmdws.CmdGetMyShop.Shop> myShopList) {
        this.myShopList = myShopList;
    }

    public void setOnEditClickListener(OnEditClickListener onEditClickListener) {
        this.onEditClickListener = onEditClickListener;
    }

    @Override
    public int getCount() {
        return myShopList.size();
    }

    @Override
    public Object getItem(int position) {
        return myShopList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(AppContext.getContext(), R.layout.item_binding_success_shop_list, null);
            holder = new ViewHolder();
            holder.tvVerifyShopName = (TextView) convertView.findViewById(R.id.tv_verify_shop_name);
            holder.tvVerifyShopWifi = (TextView) convertView.findViewById(R.id.tv_verify_shop_wifi);
            holder.ivShopLogo = (ImageView) convertView.findViewById(R.id.iv_shop_logo);
            holder.btnManageShop = (TextView) convertView.findViewById(R.id.btn_manage_shop);
            holder.tvShopUpdateTime = (TextView) convertView.findViewById(R.id.tv_shop_update_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        com.sharedream.wifiguard.cmdws.CmdGetMyShop.Shop myShop = myShopList.get(position);
        // Picasso.with(AppContext.getContext()).load(R.drawable.shop_sys_logo).transform(new CropSquareTransformation()).into(holder.ivShopLogo);
        holder.tvVerifyShopName.setText(myShop.name);
        String logoSrc = myShop.logoSrc;
//        int length = logoSrc.trim().length();
//        if (length > 0) {
//            imageLoader.get(logoSrc, ImageLoader.getImageListener(holder.ivShopLogo,
//                    R.drawable.shop_sys_logo, R.drawable.shop_sys_logo));
//        } else {
//            holder.ivShopLogo.setImageResource(R.drawable.shop_sys_logo);
//        }

//        ImageRequest imageRequest = new ImageRequest(logoSrc, new Response.Listener<Bitmap>() {
//            @Override
//            public void onResponse(Bitmap response) {
//                //给imageView设置图片
//                holder.ivShopLogo.setImageBitmap(response);
//            }
//        }, 128, 128, ImageView.ScaleType.FIT_XY, Bitmap.Config.RGB_565, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                //设置一张错误的图片，临时用ic_launcher代替
//                holder.ivShopLogo.setImageResource(R.drawable.shop_sys_logo);
//            }
//        });
//        requestQueue.add(imageRequest);

//        imageLoader.get(logoSrc, ImageLoader.getImageListener(holder.ivShopLogo,
//                R.drawable.shop_sys_logo, R.drawable.shop_sys_logo));

        imageLoader.get(logoSrc,ImageLoader.getImageListener(holder.ivShopLogo,
                R.drawable.shop_sys_logo, R.drawable.shop_sys_logo),128,128,ImageView.ScaleType.FIT_XY);

        if (myShop.apList.size() > 0) {
            String format = AppContext.getContext().getResources().getString(R.string.activity_verify_center_binding_number);
            holder.tvVerifyShopWifi.setTextColor(AppContext.getContext().getResources().getColor(R.color.verify_center_text_shop_wifi));
            holder.tvVerifyShopWifi.setText(String.format(format, myShop.apList.size()));
        } else {
            String bindingNo = AppContext.getContext().getResources().getString(R.string.activity_binding_wifi_binding_no);
            holder.tvVerifyShopWifi.setTextColor(AppContext.getContext().getResources().getColor(R.color.theme_color));
            holder.tvVerifyShopWifi.setText(bindingNo);
        }
        holder.tvShopUpdateTime.setText(getRealDate(myShop.date));
        holder.btnManageShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEditClickListener.onEditClick(position);
            }
        });
        return convertView;
    }

    private String getRealDate(String date) {
        String substring = date.substring(0, 16);
        String realDate = substring+" 更新";
        return realDate;
    }

//    private String getRealDate(String date) {
//        String[] split = date.split("T");
//        int index = split[1].lastIndexOf(":");
//        String endStr = split[1].substring(0, index);
//        return split[0] + " " + endStr + "更新";
//    }

    public interface OnEditClickListener {
        void onEditClick(int position);
    }

    static class ViewHolder {
        public TextView tvVerifyShopName;
        public TextView tvVerifyShopWifi;
        public ImageView ivShopLogo;
        public TextView btnManageShop;
        public TextView tvShopUpdateTime;
    }
}
