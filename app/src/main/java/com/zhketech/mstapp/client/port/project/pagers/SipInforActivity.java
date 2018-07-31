package com.zhketech.mstapp.client.port.project.pagers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhketech.mstapp.client.port.project.R;
import com.zhketech.mstapp.client.port.project.adpaters.ButtomSlidingAdapter;
import com.zhketech.mstapp.client.port.project.base.BaseActivity;
import com.zhketech.mstapp.client.port.project.beans.ButtomSlidingBean;
import com.zhketech.mstapp.client.port.project.beans.SipBean;
import com.zhketech.mstapp.client.port.project.beans.SipClient;
import com.zhketech.mstapp.client.port.project.callbacks.BatteryAndWifiCallback;
import com.zhketech.mstapp.client.port.project.callbacks.BatteryAndWifiService;
import com.zhketech.mstapp.client.port.project.callbacks.RequestSipSourcesThread;
import com.zhketech.mstapp.client.port.project.global.AppConfig;
import com.zhketech.mstapp.client.port.project.taking.SipService;
import com.zhketech.mstapp.client.port.project.utils.Logutils;
import com.zhketech.mstapp.client.port.project.utils.SharedPreferencesUtils;
import com.zhketech.mstapp.client.port.project.utils.SipHttpUtils;
import com.zhketech.mstapp.client.port.project.utils.TimeDo;
import com.zhketech.mstapp.client.port.project.utils.ToastUtils;
import com.zhketech.mstapp.client.port.project.utils.WriteLogToFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SipInforActivity extends BaseActivity {

    //显示当前时间
    @BindView(R.id.sipinfor_time_layout)
    public TextView timeTextView;
    //显示正在加载数据 的布局
    @BindView(R.id.loading_data_show_layout)
    RelativeLayout loading_data_show_layout;
    //底部button布局
    @BindView(R.id.bottom_sliding_recyclerview)
    public RecyclerView bottomSlidingView;
    //展示数据的gridview
    @BindView(R.id.gridview)
    public GridView gridview;

    Context mContext;
    //当前的选 项
    int selected = -1;
    //适配器
    SipInforAdapter ada = null;
    //cms获取 的sip数据
    List<SipBean> sipListResources = new ArrayList<>();
    //miniSipServer获取到的数据
    List<SipClient> mList = new ArrayList<>();
    //两集合的交集数据
    List<SipClient> adapterList = new ArrayList<>();
    //时间线程是否正在运行
    boolean threadIsRun = true;
    //handler刷新主Ui显示时间
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
    public int intiLayout() {
        return R.layout.activity_sip_infor;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        mContext = this;
    }

    @Override
    public void initData() {
        //显示正在加载数据的布局
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                loading_data_show_layout.setVisibility(View.VISIBLE);
            }
        });

        //时间显示
        TimeThread timeThread = new TimeThread();
        new Thread(timeThread).start();
        //传递过来 的groupid信息
        final int groupID = getIntent().getIntExtra("group_id", 0);
        if (groupID != 0) {
            RequestSipSourcesThread requestSipSourcesThread = new RequestSipSourcesThread(mContext, groupID + "", new RequestSipSourcesThread.SipListern() {
                @Override
                public void getDataListern(List<SipBean> sipList) {
                    if (sipList != null && sipList.size() > 0) {
                        sipListResources = sipList;
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showShort("No get SipGroup infor !!!");
                            }
                        });
                        WriteLogToFile.info("No get SipGroup infor !!!");
                    }
                }
            });
            requestSipSourcesThread.start();
        } else {
            Logutils.i("未获取到正常的groupID");
            return;
        }
        //定时获取数据
        TimeDo.getInstance().init(mContext, 3 * 1000);
        TimeDo.getInstance().start();
        TimeDo.getInstance().setListern(new TimeDo.Callback() {
            @Override
            public void resultCallback(final String result) {
                if (mList.size() > 0) {
                    mList.clear();
                }
                if (!TextUtils.isEmpty(result)) {
                    if (!result.contains("Execption") && !result.contains("code != 200")) {
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
                        } catch (Exception e) {
                            Logutils.e("sip数据json解析error");
                        }
                    }
                }

                if (adapterList != null && adapterList.size() > 0) {
                    adapterList.clear();
                }
                for (int i = 0; i < mList.size(); i++) {
                    for (int j = 0; j < sipListResources.size(); j++) {
                        if (mList.get(i).getUsrname().equals(sipListResources.get(j).getNumber())) {
                            SipClient sipClient = new SipClient();
                            sipClient.setState(mList.get(i).getState());
                            sipClient.setUsrname(mList.get(i).getUsrname());
                            adapterList.add(sipClient);
                        }
                    }
                }
                List<SipClient> dd = adapterList;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (adapterList != null && adapterList.size() > 0) {
                            if (ada != null) {
                                ada = null;
                            }
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    loading_data_show_layout.setVisibility(View.GONE);
                                }
                            });
                            ada = new SipInforAdapter(mContext);
                            gridview.setAdapter(ada);
                            gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    ada.setSeclection(position);
                                    ada.notifyDataSetChanged();
                                    Logutils.i("Position:" + position);
                                    selected = position;
                                }
                            });
                        }
                    }
                });
            }
        });
        initBottomData();
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

    class SipInforAdapter extends BaseAdapter {
        private int clickTemp = -1;
        private LayoutInflater layoutInflater;

        public SipInforAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return adapterList.size();
        }

        @Override
        public Object getItem(int position) {
            return adapterList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void setSeclection(int position) {
            clickTemp = position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.sipstatus_item, null);
                viewHolder.item_name = (TextView) convertView.findViewById(R.id.item_name);
                viewHolder.linearLayout = (LinearLayout) convertView.findViewById(R.id.item_layout);
                viewHolder.main_layout = convertView.findViewById(R.id.sipstatus_main_layout);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            String native_name = (String) SharedPreferencesUtils.getObject(SipInforActivity.this, "sipNum", "");

            if (!TextUtils.isEmpty(native_name)) {
                if (adapterList.get(position).getUsrname().equals(native_name)) {
                    viewHolder.item_name.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG); //中间横线
                    viewHolder.item_name.setTextColor(0xffDC143C);
                }
            }
            if (adapterList.get(position).getState().equals("0")) {
                viewHolder.item_name.setText("哨位名:" + adapterList.get(position).getUsrname());
                viewHolder.linearLayout.setBackgroundResource(R.mipmap.btn_lixian);
            } else if (adapterList.get(position).getState().equals("1")) {
                viewHolder.item_name.setText("哨位名:" + adapterList.get(position).getUsrname());
                viewHolder.linearLayout.setBackgroundResource(R.drawable.sip_call_select_bg);
            } else if (adapterList.get(position).getState().equals("2")) {
                viewHolder.item_name.setText("哨位名:" + adapterList.get(position).getUsrname());
                viewHolder.linearLayout.setBackgroundResource(R.mipmap.btn_zhenling);
            } else if (adapterList.get(position).getState().equals("3")) {
                viewHolder.item_name.setText("哨位名:" + adapterList.get(position).getUsrname());
                viewHolder.linearLayout.setBackgroundResource(R.mipmap.btn_tonghua);
            }

            if (clickTemp == position) {
                if (adapterList.get(position).getState().equals("1") && !adapterList.get(position).getUsrname().equals(native_name))
                    viewHolder.main_layout.setBackgroundResource(R.drawable.sip_selected_bg);
            } else {
                viewHolder.main_layout.setBackgroundColor(Color.TRANSPARENT);
            }
            return convertView;
        }

        class ViewHolder {
            TextView item_name;
            LinearLayout main_layout;
            LinearLayout linearLayout;
        }
    }

    /**
     * 初始化底部数据
     */
    private void initBottomData() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(SipInforActivity.this, 1);
        gridLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        bottomSlidingView.setLayoutManager(gridLayoutManager);
        int images[] = new int[]{R.drawable.port_network_intercom_selected, R.drawable.port_instant_messaging_selected, R.drawable.port_video_surveillance_selected, R.drawable.port_alarm_btn_selected, R.drawable.port_bullet_btn_selected};
        ButtomSlidingAdapter ada = new ButtomSlidingAdapter(SipInforActivity.this, images, 0);
        bottomSlidingView.setAdapter(ada);

    }

    @OnClick({R.id.voice_intercom_icon_layout, R.id.video_intercom_layout, R.id.sip_group_lastpage_layout, R.id.sip_group_nextpage_layout, R.id.sip_group_refresh_layout, R.id.sip_group_back_layout})
    public void onclickEvent(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.voice_intercom_icon_layout:
                if (!SipService.isReady()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toastShort("sip未注册");
                        }
                    });
                    return;
                }
                if (adapterList != null && adapterList.size() > 0) {
                    intent.putExtra("isCall", true);
                    if (selected != -1) {
                        if (adapterList.get(selected) != null) {
                            intent.setClass(SipInforActivity.this, SingleCallActivity.class);
                            intent.putExtra("userName", adapterList.get(selected).getUsrname());
                            startActivity(intent);
                        }
                    }
                }

                break;
            case R.id.video_intercom_layout:

                if (!SipService.isReady()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toastShort("sip未注册");
                        }
                    });
                    return;
                }

                if (adapterList != null && adapterList.size() > 0) {
                    intent.putExtra("isCall", true);
                    if (selected != -1) {
                        if (adapterList.get(selected) != null) {
                            intent.setClass(SipInforActivity.this, SingleCallActivity.class);
                            intent.putExtra("userName", adapterList.get(selected).getUsrname());
                            intent.putExtra("isVideo", true);
                            startActivity(intent);
                        }
                    }
                }
                break;
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
            case R.id.sip_group_refresh_layout:
                initData();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toastShort("已更新");
                    }
                });
                break;
            case R.id.sip_group_back_layout:
                SipInforActivity.this.finish();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

}
