package com.zhketech.mstapp.client.port.project.pagers;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.zhketech.mstapp.client.port.project.R;
import com.zhketech.mstapp.client.port.project.adpaters.ChatListAdapter;
import com.zhketech.mstapp.client.port.project.base.BaseActivity;
import com.zhketech.mstapp.client.port.project.beans.SipClient;
import com.zhketech.mstapp.client.port.project.utils.Logutils;
import com.zhketech.mstapp.client.port.project.utils.SpaceItemDecoration;
import com.zhketech.mstapp.client.port.project.utils.TimeDo;
import com.zhketech.mstapp.client.port.project.view.WrapContentLinearLayoutManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatListActivity extends BaseActivity implements View.OnClickListener ,SwipeRefreshLayout.OnRefreshListener{

    //显示联系人列表的recyclearview
    @BindView(R.id.chat_friends_list_layout)
    RecyclerView rw;
    //显示下拉刷新的SwipeRefreshLayout
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout sw;
    //数据适配器
    ChatListAdapter ada = null;
    //list展示数据
    List<SipClient> mList = new ArrayList<>();

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
        //设置下拉刷新的圈颜色并添加监听事件
        sw.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        sw.setOnRefreshListener(this);
        //设置recyclerview的布局及item间隔
        rw.setLayoutManager(new WrapContentLinearLayoutManager(ChatListActivity.this,WrapContentLinearLayoutManager.VERTICAL,false));
        rw.addItemDecoration(new SpaceItemDecoration(0,30));
    }

    @Override
    public void initData() {
        //定时器每三秒刷新数据
        TimeDo.getInstance().init(this, 10 * 1000);
        TimeDo.getInstance().start();
        TimeDo.getInstance().setListern(new TimeDo.Callback() {
            @Override
            public void resultCallback(String result) {
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
                                //设置适配器并添加点击事件
                                if (ada != null) {
                                    ada = null;
                                }
                                ada = new ChatListAdapter(ChatListActivity.this, mList);
                                rw.setAdapter(ada);
                                ada.setOnItemClickListener(new ChatListAdapter.OnItemClickListener() {
                                    @Override
                                    public void onClick(SipClient sipClient) {
                                        Logutils.i("sss:"+sipClient.toString());
                                        if (sipClient != null){
                                            Intent intent = new Intent();
                                            intent.setClass(ChatListActivity.this,ChatActivity.class);
                                            Bundle bundle = new Bundle();
                                            bundle.putSerializable("sipclient",sipClient);
                                            intent.putExtras(bundle);
                                            ChatListActivity.this.startActivity(intent);
                                        }else {
                                            Logutils.i("Null");
                                        }
                                    }
                                });
                            }
                        });

                    } catch (Exception e) {
                    }
                } else {
                    Log.i("TAG", "Error Data");
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ada.notifyDataSetChanged();
                sw.setRefreshing(false);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ChatListActivity.this,"No data",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        },2*1000);
    }
}
