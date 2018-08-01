package com.zhketech.mstapp.client.port.project.adpaters;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhketech.mstapp.client.port.project.R;
import com.zhketech.mstapp.client.port.project.utils.Logutils;

/**
 * Created by Root on 2018/7/24.
 * <p>
 * 設置中心右側子listview適配器
 */
public class SettingSubAdapter extends BaseAdapter {

    Context context;
    LayoutInflater layoutInflater;
    String[][] subType;
    public int subPosition;

    public SettingSubAdapter(Context context, String[][] cities, int position) {
        this.context = context;
        this.subType = cities;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.subPosition = position;
    }

    @Override
    public int getCount() {
        return subType.length;
    }

    @Override
    public Object getItem(int position) {
        return getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        final int location = position;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.setting_sub_item_layout, null);
            viewHolder = new ViewHolder();
            viewHolder.textView = (TextView) convertView
                    .findViewById(R.id.sub_item_textview_layout);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Logutils.i("dd:"+subPosition+"\t"+position);
        String infor = subType[subPosition][position];
        if (!TextUtils.isEmpty(infor)) {
            viewHolder.textView.setText(infor);
            viewHolder.textView.setTextColor(Color.BLACK);
        }else {
            viewHolder.textView.setText("");
        }
        return convertView;
    }

    public static class ViewHolder {
        public TextView textView;
    }
}