package com.zhketech.mstapp.client.port.project.adpaters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhketech.mstapp.client.port.project.R;


/**
 * Created by Root on 2018/7/24.
 *
 * 設置中心左側Listview適配器
 *
 */

public class SettingParentAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;
    String [] mainType;
    int [] images;
    private int selectedPosition = -1;
    public SettingParentAdapter(Context context, String [] type, int[] images){
        this.context = context;
        this.mainType = type;
        this.images = images;
        inflater=LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mainType.length;
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
        ViewHolder  holder = null;
        if(convertView==null){
            convertView = inflater.inflate(R.layout.setting_parent_item_layout, null);
            holder = new ViewHolder();
            holder.textView =(TextView)convertView.findViewById(R.id.textview);
            holder.imageView =(ImageView)convertView.findViewById(R.id.imageview);
            holder.layout=(LinearLayout)convertView.findViewById(R.id.colorlayout);
            convertView.setTag(holder);
        }
        else{
            holder=(ViewHolder)convertView.getTag();
        }
        // 设置选中效果
        if(selectedPosition == position) {
            holder.textView.setTextColor(Color.BLUE);
            holder.layout.setBackgroundColor(Color.LTGRAY);
        } else {
            holder.textView.setTextColor(Color.BLACK);
            holder.layout.setBackgroundColor(Color.TRANSPARENT);
        }


        holder.textView.setText(mainType[position]);
      //  holder.textView.setTextColor(Color.BLACK);
        holder.imageView.setBackgroundResource(images[position]);

        return convertView;
    }

    public static class ViewHolder{
        public TextView textView;
        public ImageView imageView;
        public LinearLayout layout;
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
    }

}