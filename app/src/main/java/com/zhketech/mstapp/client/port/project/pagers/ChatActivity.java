package com.zhketech.mstapp.client.port.project.pagers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

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
import com.zhketech.mstapp.client.port.project.taking.SipManager;
import com.zhketech.mstapp.client.port.project.taking.SipService;
import com.zhketech.mstapp.client.port.project.taking.SipUtils;
import com.zhketech.mstapp.client.port.project.utils.Logutils;

import org.linphone.core.ErrorInfo;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatRoom;
import org.linphone.core.LinphoneContent;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneFriend;
import org.linphone.core.Reason;

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

    @BindView(R.id.send_message_btn_layout)
    Button mBtnSend;
    @BindView(R.id.sendmessage_layout)
    EditText mEditTextContent;
    @BindView(R.id.message_listview_layout)
    ListView mListView;

    LinphoneChatRoom room = null;
    SQLiteDatabase db;
    String who = "";

    private ChatMsgViewAdapter mAdapter;
    private List<ChatMsgEntity> mDataArrays = new ArrayList<ChatMsgEntity>();

    private List<ChatMsgEntity> mList = new ArrayList<ChatMsgEntity>();

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
        DatabaseHelper databaseHelper = new DatabaseHelper(ChatActivity.this);
        db = databaseHelper.getWritableDatabase();


        //获取当前对话列表点击 的用户名
        SipClient sipClient = (SipClient) getIntent().getExtras().getSerializable("sipclient");
        String name = sipClient.getUsrname();
        if (!TextUtils.isEmpty(name)) {
            who = name;
        } else {

        }
        if (SipService.isReady()) {
            //获取所有的聊天室
            LinphoneChatRoom[] rooms = SipManager.getLc().getChatRooms();
            //判断当前 的聊天室
            for (int i = 0; i < rooms.length; i++) {
                if (rooms[i].getPeerAddress().getUserName().equals(who)) {
                    room = rooms[i];
                    break;
                }
            }
            if (room != null) {
                LinphoneChatMessage[] ms = room.getHistory();
                for (int i = 0; i < ms.length; i++) {
                    ChatMsgEntity entity = new ChatMsgEntity();
                    entity.setDate(new Date().toString());
                    entity.setName(ms[i].getFrom().getUserName());
                    entity.setMsgType(true);
                    entity.setText(ms[i].getText());
                    mDataArrays.add(entity);
                }

            }
        }
        Cursor cursor = db.query("chat", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String time = cursor.getString(cursor.getColumnIndex("time"));
                String fromuser = cursor.getString(cursor.getColumnIndex("fromuser"));
                String message = cursor.getString(cursor.getColumnIndex("message"));
                if (fromuser.equals(who)) {
                    ChatMsgEntity entity = new ChatMsgEntity();
                    entity.setDate(time);
                    entity.setName(fromuser);
                    entity.setMsgType(false);
                    entity.setText(message);
                    mDataArrays.add(entity);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        mAdapter = new ChatMsgViewAdapter(this, mDataArrays);
        mListView.setAdapter(mAdapter);
        mListView.setSelection(mListView.getCount());
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
                chatMsgEntity.setDate(new Date().toString());
                chatMsgEntity.setMsgType(false);
                chatMsgEntity.setText(linphoneChatMessage.getText());
                mList.add(chatMsgEntity);
                if (chatMsgEntity != null) {
                    ChatMsgEntity entity = new ChatMsgEntity();
                    entity.setDate(chatMsgEntity.getDate());
                    entity.setName(chatMsgEntity.getName());
                    entity.setMsgType(true);
                    entity.setText(chatMsgEntity.getText());
                    mDataArrays.add(entity);
                    mAdapter.notifyDataSetChanged();
                    mEditTextContent.setText("");
                    mListView.setSelection(mListView.getCount() - 1);
                }
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
                send();
                break;
        }
    }


    /**
     * 发消息
     */
    private void send() {
        String contString = mEditTextContent.getText().toString().trim();
        if (contString.length() > 0) {
            ChatMsgEntity entity = new ChatMsgEntity();
            entity.setDate(getDate());
            entity.setName(AppConfig.native_sip_name);
            entity.setMsgType(false);
            entity.setText(contString);
            mDataArrays.add(entity);
            mAdapter.notifyDataSetChanged();
            mEditTextContent.setText("");
            mListView.setSelection(mListView.getCount() - 1);
            ContentValues contentValues = new ContentValues();
            contentValues.put("time", new Date().toString());
            contentValues.put("fromuser", who);
            contentValues.put("message", contString);
            contentValues.put("touser", AppConfig.native_sip_name);
            db.insert("chat", null, contentValues);
            Logutils.i("插入成功");
        } else {
            Logutils.i("no edit text!");
        }
    }

    //时间
    private String getDate() {
        Calendar c = Calendar.getInstance();
        String year = String.valueOf(c.get(Calendar.YEAR));
        String month = String.valueOf(c.get(Calendar.MONTH));
        String day = String.valueOf(c.get(Calendar.DAY_OF_MONTH) + 1);
        String hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
        String mins = String.valueOf(c.get(Calendar.MINUTE));
        StringBuffer sbBuffer = new StringBuffer();
        sbBuffer.append(year + "-" + month + "-" + day + " " + hour + ":" + mins);
        return sbBuffer.toString();
    }

}
