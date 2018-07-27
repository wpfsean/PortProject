package com.zhketech.mstapp.client.port.project.pagers;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhketech.mstapp.client.port.project.R;
import com.zhketech.mstapp.client.port.project.adpaters.ButtomSlidingAdapter;
import com.zhketech.mstapp.client.port.project.adpaters.SipGroupAdapter;
import com.zhketech.mstapp.client.port.project.base.BaseActivity;
import com.zhketech.mstapp.client.port.project.beans.ButtomSlidingBean;
import com.zhketech.mstapp.client.port.project.beans.SipGroupBean;
import com.zhketech.mstapp.client.port.project.callbacks.BatteryAndWifiCallback;
import com.zhketech.mstapp.client.port.project.callbacks.BatteryAndWifiService;
import com.zhketech.mstapp.client.port.project.callbacks.SipGroupResourcesCallback;
import com.zhketech.mstapp.client.port.project.global.AppConfig;
import com.zhketech.mstapp.client.port.project.onvif.Device;
import com.zhketech.mstapp.client.port.project.utils.GsonUtils;
import com.zhketech.mstapp.client.port.project.utils.Logutils;
import com.zhketech.mstapp.client.port.project.utils.SharedPreferencesUtils;
import com.zhketech.mstapp.client.port.project.utils.SipHttpUtils;
import com.zhketech.mstapp.client.port.project.utils.ToastUtils;
import com.zhketech.mstapp.client.port.project.utils.WriteLogToFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * sip分组页面
 */
public class SipGroupActivity extends BaseActivity {

    @BindView(R.id.sip_group_recyclearview)
    public RecyclerView recyclearview;

    @BindView(R.id.bottom_sliding_recyclerview)
    public RecyclerView bottomSlidingView;

    //存放SipGroup信息的集合
    List<SipGroupBean> mList = new ArrayList<>();
    String callNumber = "";


    @BindView(R.id.sipgroup_time_layout)
    public TextView timeTextView;

    boolean threadIsRun = true;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //主页面时间显示
            if (msg.what == 1) {
                long time = System.currentTimeMillis();
                Date date = new Date(time);
                SimpleDateFormat timeD = new SimpleDateFormat("HH:mm:ss");
                timeTextView.setText(timeD.format(date).toString());
//                SimpleDateFormat dateD = new SimpleDateFormat("MM月dd日 EEE");
//                dateTextView.setText(dateD.format(date).toString());
            }
        }
    };


    @Override
    public int intiLayout() {
        return R.layout.activity_sip_group;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
    }

    @Override
    public void initData() {
        SipHttpUtils sipHttpUtils = new SipHttpUtils(AppConfig.DUTY_ROOM_URL, new SipHttpUtils.GetHttpData() {
            @Override
            public void httpData(String result) {
                if (TextUtils.isEmpty(result)) {
                    return;
                }
                if (result.contains("Execption")) {
                    return;
                }
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    JSONObject data = jsonArray.getJSONObject(0);
                    String name = data.getString("name");
                    String number = data.getString("number");
                    String server = data.getString("server");
                    if (!TextUtils.isEmpty(number))
                        callNumber = number;
                } catch (JSONException e) {
                    callNumber = "";
                    e.printStackTrace();
                }
            }
        });
        sipHttpUtils.start();
        getSipGroupResources();
        initBottomData();
        TimeThread timeThread = new TimeThread();
        new Thread(timeThread).start();
    }


    private void initBottomData() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(SipGroupActivity.this, 1);
        gridLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        bottomSlidingView.setLayoutManager(gridLayoutManager);
        int images[] = new int[]{R.drawable.port_network_intercom_selected, R.drawable.port_instant_messaging_selected, R.drawable.port_video_surveillance_selected, R.drawable.port_alarm_btn_selected, R.drawable.port_bullet_btn_selected};
        ButtomSlidingAdapter ada = new ButtomSlidingAdapter(SipGroupActivity.this, images, 0);
        bottomSlidingView.setAdapter(ada);
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


    /**
     * CMS获取Sip分组信息
     */
    private void getSipGroupResources() {
        if (mList != null && mList.size() > 0) {
            mList.clear();
        }
        SipGroupResourcesCallback sipGroupResourcesCallback = new SipGroupResourcesCallback(new SipGroupResourcesCallback.SipGroupDataCallback() {
            @Override
            public void callbackSuccessData(final List<SipGroupBean> dataList) {
                if (dataList != null && dataList.size() > 0) {
                    mList = dataList;
                    int s = dataList.size();
                    Logutils.i("ss:" + s);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SipGroupAdapter adapter = new SipGroupAdapter(SipGroupActivity.this, mList);
                            recyclearview.setAdapter(adapter);
                            GridLayoutManager gridLayoutManager = new GridLayoutManager(SipGroupActivity.this, 3);
                            gridLayoutManager.setReverseLayout(false);
                            gridLayoutManager.setOrientation(GridLayout.VERTICAL);
                            recyclearview.setLayoutManager(gridLayoutManager);
                            adapter.setItemClickListener(new SipGroupAdapter.MyItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {
                                    int group_id = mList.get(position).getGroup_id();
                                    Logutils.i("aa:" + group_id);
                                    Intent intent = new Intent();
                                    intent.putExtra("group_id", group_id);
                                    intent.setClass(SipGroupActivity.this, SipInforActivity.class);
                                    startActivity(intent);
                                }
                            });
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.showShort("No data !!!");
                        }
                    });
                    WriteLogToFile.info("Sip group information is not obtained");
                }
            }
        });
        sipGroupResourcesCallback.start();
    }

    @OnClick({R.id.sip_group_lastpage_layout, R.id.sip_group_nextpage_layout, R.id.video_calls_duty_room_intercom_layout, R.id.voice_calls_duty_room_intercom_layout, R.id.sip_group_finish_icon, R.id.loading_more_videosources_layout})
    public void onclickEvent(View view) {
        switch (view.getId()) {
            case R.id.sip_group_lastpage_layout:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toastShort("无数据");
                    }
                });
                break;
            case R.id.sip_group_nextpage_layout:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toastShort("无数据");
                    }
                });
                break;
            case R.id.loading_more_videosources_layout:
                getSipGroupResources();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showShort("已刷新！");
                    }
                });
                break;
            case R.id.sip_group_finish_icon:
                SipGroupActivity.this.finish();
                break;

            case R.id.video_calls_duty_room_intercom_layout:
                call(1);
                break;
            case R.id.voice_calls_duty_room_intercom_layout:
                call(0);
                break;

        }
    }


    public void call(int type) {
        if (TextUtils.isEmpty(callNumber)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toastShort("未获取到值班室信息!!!");
                }
            });
            return;
        }
        Intent intent = new Intent();
        intent.setClass(SipGroupActivity.this, SingleCallActivity.class);
        intent.putExtra("userName", callNumber);
        intent.putExtra("isCall", true);
        if (type == 0) {
            intent.putExtra("isVideo", true);
        }
        startActivity(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();

//        BatteryAndWifiService.addBatterCallback(new BatteryAndWifiCallback() {
//            @Override
//            public void getBatteryData(final int level) {
//
//                if (level >= 75 && level <= 100) {
//                    updateUi(batteryIcon, R.mipmap.icon_electricity_a);
//                }
//                if (level >= 50 && level < 75) {
//                    updateUi(batteryIcon, R.mipmap.icon_electricity_b);
//                }
//                if (level >= 25 && level < 50) {
//                    updateUi(batteryIcon, R.mipmap.icon_electricity_c);
//                }
//                if (level >= 0 && level < 25) {
//                    updateUi(batteryIcon, R.mipmap.icon_electricity_disable);
//                }
//
//            }
//
//            @Override
//            public void getWifiData(int rssi) {
//                if (rssi > -50 && rssi < 0) {
//                    updateUi(networkIcon, R.mipmap.icon_network);
//                } else if (rssi > -70 && rssi <= -50) {
//                    updateUi(networkIcon, R.mipmap.icon_network_a);
//                } else if (rssi < -70) {
//                    updateUi(networkIcon, R.mipmap.icon_network_b);
//                } else if (rssi == -200) {
//                    updateUi(networkIcon, R.mipmap.icon_network_disable);
//                }
//            }
//        });
    }

    /**
     * 更新UI
     */
    public void updateUi(final ImageView imageView, final int n) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setBackgroundResource(n);
            }
        });
    }
}
