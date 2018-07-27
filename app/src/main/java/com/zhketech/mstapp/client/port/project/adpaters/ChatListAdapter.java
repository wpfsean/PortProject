package com.zhketech.mstapp.client.port.project.adpaters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhketech.mstapp.client.port.project.R;
import com.zhketech.mstapp.client.port.project.beans.SipClient;

import java.util.List;

/**
 * Created by Root on 2018/7/23.
 */

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.MyViewHolder> {

    Context context;
    List<SipClient> mList;
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public ChatListAdapter(Context context, List<SipClient> mList) {
        this.context = context;
        this.mList = mList;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.item_layout, null);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.itemView.setBackgroundResource(R.drawable.ripple_bg);
        holder.name.setText(mList.get(position).getUsrname());
        if (mList!= null && mList.size() >0){
            String status = mList.get(position).getState();
            if (status.equals("0")) {
                holder.status.setText("状态:OffLine");
            } else if (status.equals("1")) {
                holder.status.setText("状态:OnLine");
            } else {
                holder.status.setText("UnKnow");
            }
            holder.itemView.setBackgroundResource(R.drawable.ripple_bg);
        }

        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onClick(mList.get(position));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mList.size() > 0 ? mList.size() : 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView mess;
        private TextView time;
        private TextView status;

        public MyViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name_layout);
            mess = itemView.findViewById(R.id.last_mess_layout);
            time = itemView.findViewById(R.id.time_layout);
            status = itemView.findViewById(R.id.sip_status_layout);
        }
    }

    public interface OnItemClickListener {
        void onClick(SipClient sipClient);
    }
}
