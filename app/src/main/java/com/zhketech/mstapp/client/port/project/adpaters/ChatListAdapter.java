package com.zhketech.mstapp.client.port.project.adpaters;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhketech.mstapp.client.port.project.R;
import com.zhketech.mstapp.client.port.project.beans.ChatMsgEntity;
import com.zhketech.mstapp.client.port.project.beans.SipClient;
import com.zhketech.mstapp.client.port.project.db.DatabaseHelper;
import com.zhketech.mstapp.client.port.project.global.AppConfig;
import com.zhketech.mstapp.client.port.project.pagers.ChatActivity;
import com.zhketech.mstapp.client.port.project.taking.SipManager;
import com.zhketech.mstapp.client.port.project.taking.SipService;
import com.zhketech.mstapp.client.port.project.utils.Logutils;
import com.zhketech.mstapp.client.port.project.utils.TimeUtils;

import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatRoom;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Root on 2018/7/23.
 * <p>
 * 显示当前sip列表的适配器
 */

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.MyViewHolder> {

    Context context;
    List<SipClient> mList;
    LinphoneChatRoom[] rooms;
    Cursor cursor;

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public ChatListAdapter(Context context, List<SipClient> mList) {
        this.context = context;
        this.mList = mList;

        if (SipService.isReady()) {
            rooms = SipManager.getLc().getChatRooms();
        }


        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        cursor = db.query("chat", null, null, null, null, null, null);

    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.item_layout, null);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.name.setText(mList.get(position).getUsrname());

        //判断当前 的sip是在线状态or 离线状态
        if (mList != null && mList.size() > 0) {
            String status = mList.get(position).getState();
            if (status.equals("0")) {
                holder.status.setBackgroundResource(R.mipmap.led_error);
            } else if (status.equals("1")) {
                holder.status.setBackgroundResource(R.mipmap.led_connected);
            }
        }


        try {
            Logutils.i("rooms:"+rooms.length);
            for (int i = 0; i < rooms.length; i++) {
                String name = rooms[i].getPeerAddress().getUserName();
                if (!mList.get(position).getUsrname().equals(AppConfig.native_sip_name)) {
                    if (name.equals(mList.get(position).getUsrname())) {
                        LinphoneChatMessage[] his = rooms[i].getHistory();
                        for (int j = 0; j < his.length; j++) {
                            String mess = his[j].getText();
                            long time = his[j].getTime();
                            Logutils.i(mess+"\n"+time);
                            holder.mess.setText(mess);
                            holder.time.setText(TimeUtils.timeStamp2Date(time/1000+""));
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logutils.e("sql error:" + e.getMessage());
        }

//        if (cursor != null) {
//            String currentUsrer = mList.get(position).getUsrname();
//            cursor.moveToLast();
//            try {
//                String time = cursor.getString(cursor.getColumnIndex("time"));
//                String fromuser = cursor.getString(cursor.getColumnIndex("fromuser"));
//                String message = cursor.getString(cursor.getColumnIndex("message"));
//                Logutils.i("time:" + time);
//                if (currentUsrer.equals(fromuser)) {
//                    holder.time.setText(TimeUtils.longTime2Short(time));
//                    holder.mess.setText(fromuser + ":" + message);
//                }
//            } catch (Exception e) {
//            }
//        }
//

        //item点击事件
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
        private TextView name;//sip名称
        private TextView mess;//最后 的消息
        private TextView time;//最后消息的时间
        private ImageView status;//是否在线

        public MyViewHolder(View itemView) {
            super(itemView);
            //findViewbyId
            name = itemView.findViewById(R.id.item_sip_uesername_layout);
            mess = itemView.findViewById(R.id.last_mess_layout);
            time = itemView.findViewById(R.id.last_message_time_layout);
            status = itemView.findViewById(R.id.sip_status_layout);
        }
    }

    public interface OnItemClickListener {
        void onClick(SipClient sipClient);
    }
}
