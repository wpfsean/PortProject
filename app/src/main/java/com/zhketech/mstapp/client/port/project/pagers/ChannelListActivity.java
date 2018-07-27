package com.zhketech.mstapp.client.port.project.pagers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.zhketech.mstapp.client.port.project.R;
import com.zhketech.mstapp.client.port.project.adpaters.ChannelListRecycleViewAdapter;
import com.zhketech.mstapp.client.port.project.base.App;
import com.zhketech.mstapp.client.port.project.base.BaseActivity;
import com.zhketech.mstapp.client.port.project.onvif.Device;
import com.zhketech.mstapp.client.port.project.utils.GsonUtils;
import com.zhketech.mstapp.client.port.project.utils.Logutils;
import com.zhketech.mstapp.client.port.project.utils.SharedPreferencesUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Root on 2018/7/18.
 */

public class ChannelListActivity extends BaseActivity {

    //recyclevire
    @BindView(R.id.channel_list_layout)
    RecyclerView recyclerView;
    //adapter
    ChannelListRecycleViewAdapter adapter;
    //盛放数据的集合
    List<Device> dataList = new ArrayList<>();

    @Override
    public int intiLayout() {
        return R.layout.activity_channel_list_pager;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);

    }

    @Override
    public void initData() {
        String dataSources = (String) SharedPreferencesUtils.getObject(ChannelListActivity.this, "result", "");
        if (TextUtils.isEmpty(dataSources)) {
            return;
        }
        List<Device> mlist = GsonUtils.getGsonInstace().str2List(dataSources);
        if (mlist != null && mlist.size() > 0) {
            dataList = mlist;
            adapter = new ChannelListRecycleViewAdapter(ChannelListActivity.this, dataList);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
            recyclerView.setAdapter(adapter);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ChannelListActivity.this, "No data !!!", Toast.LENGTH_SHORT).show();
                }
            });
            Logutils.i("NoData");
        }

    }


    /**
     * finish this pager
     */
    @OnClick(R.id.finish_back_layout)
    public void finishPager(View view) {
        ChannelListActivity.this.finish();
    }

    /**
     * refresh this pager data and show
     */
    @OnClick(R.id.channel_refresh)
    public void refresh(View view) {
        initData();
        Toast.makeText(ChannelListActivity.this, "已刷新！", Toast.LENGTH_SHORT).show();
    }

    /**
     * start preview video
     */
    @OnClick(R.id.start_play_video_layout)
    public void startPreview(View view) {

        int previewDataCount = adapter.previewData.size();
        Logutils.i("Count:" + previewDataCount);

        for (Device device : adapter.previewData) {
            Logutils.i(device.toString());
        }
        if (previewDataCount != 4) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ChannelListActivity.this, "请复选四个选项！！！", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        List<Device> mDeviceList = adapter.previewData;
        Logutils.i("mDeviceList:" + mDeviceList.size());
        Intent intent = new Intent();
        intent.setClass(ChannelListActivity.this, MutilScreenActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("previewdata", (Serializable) mDeviceList);
        intent.putExtras(bundle);
        ChannelListActivity.this.startActivity(intent);
        ChannelListActivity.this.finish();
    }
}
