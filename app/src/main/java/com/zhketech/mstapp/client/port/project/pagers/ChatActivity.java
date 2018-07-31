package com.zhketech.mstapp.client.port.project.pagers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.zhketech.mstapp.client.port.project.R;
import com.zhketech.mstapp.client.port.project.adpaters.ChatMsgViewAdapter;
import com.zhketech.mstapp.client.port.project.base.App;
import com.zhketech.mstapp.client.port.project.base.BaseActivity;
import com.zhketech.mstapp.client.port.project.beans.ChatMsgEntity;
import com.zhketech.mstapp.client.port.project.beans.SipClient;
import com.zhketech.mstapp.client.port.project.db.DatabaseHelper;
import com.zhketech.mstapp.client.port.project.global.AppConfig;
import com.zhketech.mstapp.client.port.project.taking.Linphone;
import com.zhketech.mstapp.client.port.project.taking.MessageCallback;
import com.zhketech.mstapp.client.port.project.taking.SipService;
import com.zhketech.mstapp.client.port.project.utils.Logutils;
import com.zhketech.mstapp.client.port.project.utils.TimeUtils;

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatRoom;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Root on 2018/7/23.
 */

public class ChatActivity extends BaseActivity implements View.OnClickListener {

    //发送消息的按钮
    @BindView(R.id.send_message_btn_layout)
    TextView mBtnSend;
    //消息
    @BindView(R.id.sendmessage_layout)
    EditText mEditTextContent;
    //展示历史消息的ListView
    @BindView(R.id.message_listview_layout)
    ListView mListView;
    //当前的聊天室对象
    LinphoneChatRoom room = null;
    //数据库对象
    SQLiteDatabase db;
    //和谁正在聊天
    String who = "";
    //Linphone聊天对象的地址
    LinphoneAddress linphoneAddress;
    //历史消息适配器
    private ChatMsgViewAdapter mAdapter;
    //盛放消息的集合容器
    private List<ChatMsgEntity> mDataArrays = new ArrayList<ChatMsgEntity>();

    @Override
    public int intiLayout() {
        return R.layout.chat_activity;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        mBtnSend.setOnClickListener(this);

    }

    @Override
    public void initData() {
        //数据库对象
        DatabaseHelper databaseHelper = new DatabaseHelper(ChatActivity.this);
        db = databaseHelper.getWritableDatabase();
        mDataArrays.clear();
        //获取当前对话列表点击 的用户名
        SipClient sipClient = (SipClient) getIntent().getExtras().getSerializable("sipclient");
        String name = sipClient.getUsrname();
        if (!TextUtils.isEmpty(name)) {
            who = name;
            Logutils.i("who:" + who);
            try {
                linphoneAddress = LinphoneCoreFactory.instance().createLinphoneAddress("sip:" + who + "@" + AppConfig.native_sip_server_ip);
            } catch (LinphoneCoreException e) {
                e.printStackTrace();
            }
        } else {
            Logutils.e("No Get Chat Object!!!");
            return;
        }
        getAllHistory();
        //初始化适配器
        mAdapter = new ChatMsgViewAdapter(this, mDataArrays);
        mListView.setAdapter(mAdapter);
        mListView.setSelection(mListView.getCount());
    }

    /**
     * 取出所有的聊天记录
     *
     */
    private void getAllHistory() {
        //根据条件查询聊天记录
        Cursor cursor = db.query("chat", null, "fromuser =? or touser = ?", new String[]{who,who}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String time = cursor.getString(cursor.getColumnIndex("time"));
                String fromuser = cursor.getString(cursor.getColumnIndex("fromuser"));
                String message = cursor.getString(cursor.getColumnIndex("message"));
                String toUser = cursor.getString(cursor.getColumnIndex("touser"));
          //      Logutils.i(TimeUtils.longTime2Short(time) + "\t" + fromuser + "\t" + toUser + "\t" + message);
                if (fromuser.equals(AppConfig.native_sip_name)) {
                    ChatMsgEntity mEntity = new ChatMsgEntity();
                    mEntity.setDate(TimeUtils.longTime2Short(time));
                    mEntity.setName(fromuser);
                    mEntity.setMsgType(false);
                    mEntity.setText(message);
                    mDataArrays.add(mEntity);
                } else if (fromuser.equals(who)) {
                    ChatMsgEntity tEntity = new ChatMsgEntity();
                    tEntity.setDate(TimeUtils.longTime2Short(time));
                    tEntity.setName(fromuser);
                    tEntity.setMsgType(true);
                    tEntity.setText(message);
                    mDataArrays.add(tEntity);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        initMessReceiverCall();
    }


    //消息回调
    private void initMessReceiverCall() {
        SipService.addMessageCallback(new MessageCallback() {
            @Override
            public void receiverMessage(LinphoneChatMessage linphoneChatMessage) {
                ChatMsgEntity chatMsgEntity = new ChatMsgEntity();
                chatMsgEntity.setName(linphoneChatMessage.getFrom().getUserName());
                chatMsgEntity.setDate(TimeUtils.longTime2Short(new Date().toString()));
                chatMsgEntity.setMsgType(true);
                chatMsgEntity.setText(linphoneChatMessage.getText());
                mDataArrays.add(chatMsgEntity);
                mAdapter.notifyDataSetChanged();
                mEditTextContent.setText("");
                mListView.setSelection(mListView.getCount() - 1);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        initMessReceiverCall();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_message_btn_layout:
                sendMess();
                break;
        }
    }


    /**
     * 发消息
     */
    private void sendMess() {
        String chatMessage = mEditTextContent.getText().toString().trim();
        if (!TextUtils.isEmpty(chatMessage) && chatMessage.length() > 0) {
            //送消息的展示界面
            ChatMsgEntity entity = new ChatMsgEntity();
            entity.setText(chatMessage);
            entity.setMsgType(false);
            entity.setName(AppConfig.native_sip_name);
            entity.setDate(getDate());
            mDataArrays.add(entity);
            mAdapter.notifyDataSetChanged();
            mEditTextContent.setText("");
            mListView.setSelection(mListView.getCount() - 1);
            //（发送sip短消息到对方）
            if (SipService.isReady())
            Linphone.getLC().getChatRoom(linphoneAddress).sendMessage(chatMessage);

            //把发的消息插入到数据库
            ContentValues contentValues = new ContentValues();
            contentValues.put("time", new Date().toString());
            contentValues.put("fromuser", AppConfig.native_sip_name);
            contentValues.put("message", chatMessage);
            contentValues.put("touser", who);
            db.insert("chat", null, contentValues);
        }
    }

    //时间
    private String getDate() {
        String time = new Date().toString();
        return TimeUtils.longTime2Short(time);
    }

}
