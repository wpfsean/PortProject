package com.zhketech.mstapp.client.port.project.adpaters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhketech.mstapp.client.port.project.R;
import com.zhketech.mstapp.client.port.project.beans.SipGroupBean;

import java.util.List;

/**
 * SipGroup分组适配器
 *
 * Created by Root on 2018/5/21.
 */

public class SipGroupAdapter extends RecyclerView.Adapter<SipGroupAdapter.GridViewHolder> {
    private Context mContext;
    private List<SipGroupBean> mDateBeen;
    private MyItemClickListener mItemClickListener;

    public SipGroupAdapter(Context context, List<SipGroupBean> dateBeen) {
        mContext = context;
        mDateBeen = dateBeen;
    }

    @Override
    public GridViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View    itemView = View.inflate(mContext, R.layout.sip_group_recyclearview_item, null);
        GridViewHolder gridViewHolder = new GridViewHolder(itemView,mItemClickListener);
        return gridViewHolder;
    }

    @Override
    public void onBindViewHolder(GridViewHolder holder, int position) {
        SipGroupBean dateBean = mDateBeen.get(position);
        holder.setData(dateBean);
    }

    //决定RecyclerView有多少条item
    @Override
    public int getItemCount() {
//数据不为null，有几条数据就显示几条数据
        if (mDateBeen != null && mDateBeen.size() > 0) {
            return mDateBeen.size();
        }
        return 0;
    }

    //自动帮我们写的ViewHolder，参数：View布局对象
    public static class GridViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private MyItemClickListener mListener;
        private final TextView item_title;

        public GridViewHolder(View itemView, MyItemClickListener myItemClickListener) {
            super(itemView);
            item_title = (TextView) itemView.findViewById(R.id.show_sip_group_name);
            this.mListener = myItemClickListener;
            itemView.setOnClickListener(this);

        }

        public void setData(SipGroupBean data) {
            item_title.setText(data.getGroup_name());
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(v, getPosition());
            }

        }
    }
    public interface MyItemClickListener {
        void onItemClick(View view, int position);
    }

    /**
     * 在activity里面adapter就是调用的这个方法,将点击事件监听传递过来,并赋值给全局的监听
     *
     * @param myItemClickListener
     */
    public void setItemClickListener(MyItemClickListener myItemClickListener) {
        this.mItemClickListener = myItemClickListener;
    }
}
