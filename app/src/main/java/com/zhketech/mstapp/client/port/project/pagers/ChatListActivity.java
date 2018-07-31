package com.zhketech.mstapp.client.port.project.pagers;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zhketech.mstapp.client.port.project.R;
import com.zhketech.mstapp.client.port.project.adpaters.ButtomSlidingAdapter;
import com.zhketech.mstapp.client.port.project.adpaters.ChatListAdapter;
import com.zhketech.mstapp.client.port.project.base.BaseActivity;
import com.zhketech.mstapp.client.port.project.beans.ChatMsgEntity;
import com.zhketech.mstapp.client.port.project.beans.SipClient;
import com.zhketech.mstapp.client.port.project.global.AppConfig;
import com.zhketech.mstapp.client.port.project.status.views.StateLayout;
import com.zhketech.mstapp.client.port.project.taking.MessageCallback;
import com.zhketech.mstapp.client.port.project.taking.SipService;
import com.zhketech.mstapp.client.port.project.utils.Logutils;
import com.zhketech.mstapp.client.port.project.utils.SipHttpUtils;
import com.zhketech.mstapp.client.port.project.utils.SpaceItemDecoration;
import com.zhketech.mstapp.client.port.project.utils.TimeUtils;
import com.zhketech.mstapp.client.port.project.view.WrapContentLinearLayoutManager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.linphone.core.LinphoneChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatListActivity extends BaseActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    //显示联系人列表的recyclearview
    @BindView(R.id.chat_contact_list_layout)
    RecyclerView chatList;
    //显示下拉刷新的SwipeRefreshLayout
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout sw;
    //数据适配器
    ChatListAdapter ada = null;
    //list展示数据
    List<SipClient> mList = new ArrayList<>();
    //list展示数据
    List<SipClient> newList = new ArrayList<>();

    //底部横向滑动的recyclerview
    @BindView(R.id.bottom_sliding_recyclerview)
    public RecyclerView bottomSlidingView;
    //显示当前的时间
    @BindView(R.id.chat_list_time_layout)
    public TextView timeTextView;
    //是否正在运行
    boolean threadIsRun = true;

    StateLayout stateLayout;

    //hander修改主线程ui
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //主页面时间显示
            if (msg.what == 1) {
                long time = System.currentTimeMillis();
                Date date = new Date(time);
                SimpleDateFormat timeD = new SimpleDateFormat("HH:mm:ss");
                timeTextView.setText(timeD.format(date).toString());
            }
        }
    };

    @Override
    public void onClick(View v) {

    }

    @Override
    public int intiLayout() {
        return R.layout.activity_chat_list;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);

        stateLayout = (StateLayout) findViewById(R.id.chatlist_statelayout);

        //设置下拉刷新的圈颜色并添加监听事件
        sw.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        sw.setOnRefreshListener(this);
        //设置recyclerview的布局及item间隔
        chatList.setLayoutManager(new WrapContentLinearLayoutManager(ChatListActivity.this, WrapContentLinearLayoutManager.VERTICAL, false));
        chatList.addItemDecoration(new SpaceItemDecoration(0, 30));
        chatList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    @Override
    public void initData() {

        stateLayout.showProgressView();
        stateLayout.showProgressView("正在加载数据...");
        //开始显示时间
        TimeThread timeThread = new TimeThread();
        new Thread(timeThread).start();

        getData();

        //初始化底部recyclerview横向滑动的数据
        initBottomData();
    }

    private void getData() {

        //获取sip数据并展示
        SipHttpUtils sipHttpUtils = new SipHttpUtils(AppConfig.sipServerDataUrl, new SipHttpUtils.GetHttpData() {
            @Override
            public void httpData(final String result) {

                Logutils.i("result:" + result);
                //判断是否正常的获取到数据
                if (!TextUtils.isEmpty(result) && !result.contains("Execption")) {
                    //清除定时循环前的数据集合
                    if (mList != null || mList.size() > 0) {
                        mList.clear();
                    }
                    //解析json转成数据对象并添加到数据集合中
                    try {
                        JSONArray jsonArray = new JSONArray(result);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String username = jsonObject.getString("usrname");
                            String description = jsonObject.getString("description");
                            String dispname = jsonObject.getString("dispname");
                            String addr = jsonObject.getString("addr");
                            String state = jsonObject.getString("state");
                            String userAgent = jsonObject.getString("userAgent");
                            SipClient sipClient = new SipClient(username, description, dispname, addr, state, userAgent);
                            mList.add(sipClient);

                        }
                        //子线程无法更改主Ui
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                stateLayout.showContentView();
                                ada = new ChatListAdapter(ChatListActivity.this, mList);
                                chatList.setAdapter(ada);
                                ada.setOnItemClickListener(new ChatListAdapter.OnItemClickListener() {
                                    @Override
                                    public void onClick(SipClient sipClient) {
                                        Logutils.i("sss:" + sipClient.toString());
                                        if (sipClient != null) {
                                            Intent intent = new Intent();
                                            intent.setClass(ChatListActivity.this, ChatActivity.class);
                                            Bundle bundle = new Bundle();
                                            bundle.putSerializable("sipclient", sipClient);
                                            intent.putExtras(bundle);
                                            ChatListActivity.this.startActivity(intent);
                                        } else {
                                            Logutils.i("Null");
                                        }
                                    }
                                });
                            }
                        });

                    } catch (Exception e) {
                    }
                } else {
                   runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           stateLayout.showErrorView();
                           stateLayout.showErrorView(result);
                           stateLayout.setErrorAction(new View.OnClickListener() {
                               @Override
                               public void onClick(View v) {
                                   getData();
                               }
                           });
                       }
                   });
                }
            }
        });
        sipHttpUtils.start();
    }


    /**
     * 底部横向滑动的recycler
     */
    private void initBottomData() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(ChatListActivity.this, 1);
        gridLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        bottomSlidingView.setLayoutManager(gridLayoutManager);
        int images[] = new int[]{R.drawable.port_network_intercom_selected, R.drawable.port_instant_messaging_selected, R.drawable.port_video_surveillance_selected, R.drawable.port_alarm_btn_selected, R.drawable.port_bullet_btn_selected};
        ButtomSlidingAdapter ada = new ButtomSlidingAdapter(ChatListActivity.this, images, 1);
        bottomSlidingView.setAdapter(ada);


        ada.setOnItemClickListener(new ButtomSlidingAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {

                switch (position) {
                    case 0:
                        openActivityAndCloseThis(SipInforActivity.class);
                        break;
                    case 1:

                        break;
                    case 2:
                        openActivityAndCloseThis(MutilScreenActivity.class);
                        break;
                    case 3:
                        break;
                }
            }
        });
    }

    /**
     * 下拉刷新
     */
    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getData();
                sw.setRefreshing(false);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ChatListActivity.this, "No data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, 2 * 1000);
    }


    //显示时间的线程
    class TimeThread extends Thread {
        @Override
        public void run() {
            super.run();
            do {
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (threadIsRun);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        SipService.addMessageCallback(new MessageCallback() {
            @Override
            public void receiverMessage(LinphoneChatMessage linphoneChatMessage) {

                String from = linphoneChatMessage.getFrom().getUserName();
                int p = -1;
                for (int i = 0; i < mList.size(); i++) {
                    if (mList.get(i).getUsrname().equals(from)) {
                        p = i;
                        break;
                    }
                }
                ada.notifyItemChanged(p);

            }
        });
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        getData();
    }
}
