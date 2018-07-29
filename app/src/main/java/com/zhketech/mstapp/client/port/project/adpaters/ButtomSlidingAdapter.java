package com.zhketech.mstapp.client.port.project.adpaters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zhketech.mstapp.client.port.project.R;
import com.zhketech.mstapp.client.port.project.beans.ButtomSlidingBean;
import com.zhketech.mstapp.client.port.project.beans.SipClient;

import java.util.List;

/**
 * Created by Root on 2018/7/9.
 * <p>
 * y底部的sliding滑动适配器
 *
 * 根據type判定
 *
 * type:
 * 0:網絡對講
 * 1:視頻監控
 * 2:即時通信
 * 3:應急報警
 * 4:申請供彈
 */

public class ButtomSlidingAdapter extends RecyclerView.Adapter<ButtomSlidingAdapter.ViewHolder> {

    Context context;
    int[] images;
    int type;
    private OnItemClickListener onItemClickListener;

    public ButtomSlidingAdapter(Context context, int[] images, int type) {
        this.context = context;
        this.images = images;
        this.type = type;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.function3_button_activity, null);
        //实例化ViewHolder
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if (type ==0){
            if (position == 0){
                holder.imageButton.setBackgroundResource(R.mipmap.port_network_intercom_btn_selected);
            }else {
                holder.imageButton.setBackgroundResource(images[position]);
            }
        }else if (type ==2){
            if (position == 2){
                holder.imageButton.setBackgroundResource(R.mipmap.port_video_surveillance_btn_selected);
            }else {
                holder.imageButton.setBackgroundResource(images[position]);
            }
        }else if (type ==1){
            if (position == 1){
                holder.imageButton.setBackgroundResource(R.mipmap.port_instant_messaging_btn_selected);
            }else {
                holder.imageButton.setBackgroundResource(images[position]);
            }
        }

        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onClick(position);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return images.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageButton imageButton;
        public ViewHolder(View itemView) {
            super(itemView);
            imageButton = itemView.findViewById(R.id.network_intercom_button_layout);
        }
    }


    public interface OnItemClickListener {
        void onClick(int position );
    }
}
