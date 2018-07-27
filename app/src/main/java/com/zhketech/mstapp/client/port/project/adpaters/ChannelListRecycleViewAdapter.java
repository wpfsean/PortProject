package com.zhketech.mstapp.client.port.project.adpaters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import com.zhketech.mstapp.client.port.project.R;
import com.zhketech.mstapp.client.port.project.onvif.Device;
import com.zhketech.mstapp.client.port.project.utils.Logutils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Root on 2018/7/5.
 * <p>
 * 视频资源的Adapter
 */

public class ChannelListRecycleViewAdapter extends RecyclerView.Adapter<ChannelListRecycleViewAdapter.MyViewHolder> {

    private List<Device> dataList;//数据集合
    private LayoutInflater layoutInflater;
    boolean isSelected = false;//当前的view是否选中
    public List<Device> previewData = new ArrayList(4);//存放已选中的数据

    //Construct
    public ChannelListRecycleViewAdapter(Context context, List<Device> dataList) {
        this.dataList = dataList;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.channel_list_item_layout, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Device itemDevice = dataList.get(position);
                if (previewData.contains(itemDevice)){
                    holder.video_checkbox.setChecked(false);
                    previewData.remove(itemDevice);
                }else {
                    previewData.add(itemDevice);
                    holder.video_checkbox.setChecked(true);
                }
            }
       });
        holder.video_title.setText(dataList.get(position).getVideoBen().getName());
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    //内部内ViewHolder
    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView video_title;
        private CheckBox video_checkbox;

        public MyViewHolder(View itemView) {
            super(itemView);
            video_title = (TextView) itemView.findViewById(R.id.video_name_layout);
            video_checkbox = (CheckBox) itemView.findViewById(R.id.video_checkbox_layout);
        }
    }


}
