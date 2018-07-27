package com.zhketech.mstapp.client.port.project.pagers;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhketech.mstapp.client.port.project.R;
import com.zhketech.mstapp.client.port.project.adpaters.ButtomSlidingAdapter;
import com.zhketech.mstapp.client.port.project.adpaters.VideoResourcesListAda;
import com.zhketech.mstapp.client.port.project.base.App;
import com.zhketech.mstapp.client.port.project.base.BaseActivity;
import com.zhketech.mstapp.client.port.project.beans.ButtomSlidingBean;
import com.zhketech.mstapp.client.port.project.beans.VideoBen;
import com.zhketech.mstapp.client.port.project.callbacks.SendAlarmToServer;
import com.zhketech.mstapp.client.port.project.global.AppConfig;
import com.zhketech.mstapp.client.port.project.onvif.Device;
import com.zhketech.mstapp.client.port.project.utils.ControlPtz;
import com.zhketech.mstapp.client.port.project.utils.GsonUtils;
import com.zhketech.mstapp.client.port.project.utils.Logutils;
import com.zhketech.mstapp.client.port.project.utils.PageModel;
import com.zhketech.mstapp.client.port.project.utils.SharedPreferencesUtils;
import com.zhketech.mstapp.client.port.project.utils.ToastUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.nodemedia.NodePlayer;
import cn.nodemedia.NodePlayerDelegate;
import cn.nodemedia.NodePlayerView;

public class MutilScreenActivity extends BaseActivity implements NodePlayerDelegate, View.OnClickListener, View.OnTouchListener {
    PageModel pm;//分页加载器
    PageModel singlePm;//分页加载器;//分页加载器
    int videoCurrentPage = 1;//当前页码


    @BindView(R.id.four_screen_button_select)
    ImageButton four_screen_button_select;
    @BindView(R.id.single_screen_button_selecte)
    ImageButton single_screen_button_selecte;

    @BindView(R.id.paly_or_stop_button_select)
    ImageButton paly_or_stop_button_select;


    //数据集合
    List<Device> devicesList = new ArrayList<>();
    //当前四屏的数据集合
    List<Device> currentList = new ArrayList<>();
    //单屏时要播放的数据
    List<Device> currentSingleList = new ArrayList<>();
    //单屏的NodeMediaclient播放器
    NodePlayer singlePlayer;
    NodePlayer firstPalyer, secondPlayer, thirdPlayer, fourthPlayer;

    //单屏显示的player布局
    @BindView(R.id.single_player_layout)
    NodePlayerView single_player_layout;
    //第一个视频的view
    @BindView(R.id.first_player_layout)
    NodePlayerView firstPlayerView;
    //第一个视频 的Progressbar
    @BindView(R.id.first_pr_layout)
    ProgressBar first_pr_layout;
    //第一个视频 的loading
    @BindView(R.id.first_dispaly_loading_layout)
    TextView first_dispaly_loading_layout;
    //第一个视频所在的背景而
    @BindView(R.id.first_surfaceview_relativelayout)
    public RelativeLayout first_surfaceview_relativelayout;
    //第二个视频的view
    @BindView(R.id.second_player_layout)
    NodePlayerView secondPlayerView;
    //第二个视频所在的背景而
    @BindView(R.id.second_surfaceview_relativelayout)
    public RelativeLayout second_surfaceview_relativelayout;
    @BindView(R.id.second_pr_layout)
    ProgressBar second_pr_layout;
    //第一个视频 的loading
    @BindView(R.id.seond_dispaly_loading_layout)
    TextView second_dispaly_loading_layout;

    //第三个视频的view
    @BindView(R.id.third_player_layout)
    NodePlayerView thirdPlayerView;
    //第三个视频所在的背景而
    @BindView(R.id.third_surfaceview_relativelayout)
    public RelativeLayout third_surfaceview_relativelayout;

    //第三个视频 的progressbar
    @BindView(R.id.third_pr_layout)
    ProgressBar third_pr_layout;
    //第一个视频 的loading
    @BindView(R.id.third_dispaly_loading_layout)
    TextView third_dispaly_loading_layout;

    //第四个视频的view
    @BindView(R.id.fourth_player_layout)
    NodePlayerView fourthPlayerView;
    //第四个视频所在的背景而
    @BindView(R.id.fourth_surfaceview_relativelayout)
    public RelativeLayout fourth_surfaceview_relativelayout;

    //每四个progressbar
    @BindView(R.id.fourth_pr_layout)
    ProgressBar fourth_pr_layout;

    //返回按钮
    @BindView(R.id.fourth_dispaly_loading_layout)
    TextView fourth_dispaly_loading_layout;

    //单屏播放时的progressbar
    @BindView(R.id.single_player_progressbar_layout)
    ProgressBar single_player_progressbar_layout;
    //单屏时显示 的Loading
    @BindView(R.id.dispaly_video_loading_layout)
    TextView dispaly_video_loading_layout;
    //显示视频信息的Textview
    @BindView(R.id.display_video_information_text_layout)
    TextView display_video_information_text_layout;

    //上下左右四个键
    @BindView(R.id.video_ptz_up)
    ImageButton video_ptz_up;
    @BindView(R.id.video_ptz_down)
    ImageButton video_ptz_down;
    @BindView(R.id.video_ptz_left)
    ImageButton video_ptz_left;
    @BindView(R.id.video_ptz_right)
    ImageButton video_ptz_right;
    //放大缩小键盘
    @BindView(R.id.video_zoomout_button)
    ImageButton video_zoomout_button;
    @BindView(R.id.video_zoombig_button)
    ImageButton video_zoombig_button;

    //load more btn
    @BindView(R.id.loading_more_videosources_layout)
    ImageButton loadMoreData;

    //the parent layout of listview
    @BindView(R.id.relativelayout_listview)
    RelativeLayout relativelayout_listview;

    //control layout
    @BindView(R.id.show_relativelayout_all_button)
    RelativeLayout show_relativelayout_all_button;

    //direction layout
    @BindView(R.id.relativelayout_control)
    RelativeLayout relativelayout_control;


    //展示视频数据的listview
    @BindView(R.id.show_listresources)
    public ListView show_listresources;

    @BindView(R.id.bottom_sliding_recyclerview)
    public RecyclerView bottomSlidingView;

    //当前状态是四分屏
    boolean isCurrentFourScreen = true;
    //当前状态是单屏
    boolean isCurrentSingleScreen = false;
    //判断这四个视频 中否被选中
    boolean firstViewSelect = false;
    boolean secondViewSelect = false;
    boolean thirdViewSelect = false;
    boolean fourthViewSelect = false;


    //四屏所在 的父布局
    @BindView(R.id.four_surfaceview_parent_relativelayout)
    RelativeLayout four_surfaceview_parent_relativelayout;
    //单屏所在的父布局
    @BindView(R.id.single_surfaceview_parent_relativelayout)
    RelativeLayout single_surfaceview_parent_relativelayout;

    @BindView(R.id.mutilscreen_time_layout)
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
        return R.layout.activity_mutil_screen;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        video_ptz_up.setOnTouchListener(this);
        video_ptz_down.setOnTouchListener(this);
        video_ptz_left.setOnTouchListener(this);
        video_ptz_right.setOnTouchListener(this);
        video_zoomout_button.setOnTouchListener(this);
        video_zoombig_button.setOnTouchListener(this);
        loadMoreData.setOnClickListener(this);

        firstPalyer = new NodePlayer(this);
        firstPalyer.setPlayerView(firstPlayerView);
        firstPlayerView.setOnClickListener(this);

        secondPlayer = new NodePlayer(this);
        secondPlayer.setPlayerView(secondPlayerView);
        secondPlayerView.setOnClickListener(this);


        thirdPlayer = new NodePlayer(this);
        thirdPlayer.setPlayerView(thirdPlayerView);
        thirdPlayerView.setOnClickListener(this);

        fourthPlayer = new NodePlayer(this);
        fourthPlayer.setPlayerView(fourthPlayerView);
        fourthPlayerView.setOnClickListener(this);

    }

    @Override
    public void initData() {
        //取出事先解析好的数据
        String dataSources = (String) SharedPreferencesUtils.getObject(MutilScreenActivity.this, "result", "");
        if (TextUtils.isEmpty(dataSources)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showShort("No data!!!");
                }
            });
            return;
        }
        List<Device> mlist = GsonUtils.getGsonInstace().str2List(dataSources);
        if (mlist != null && mlist.size() > 0) {
            devicesList = mlist;
        }
        pm = new PageModel(devicesList, 4);
        singlePm = new PageModel(devicesList, 1);
        Intent intent = this.getIntent();
        List<Device> passingDataList = (List<Device>) intent.getSerializableExtra("previewdata");//获取list方式

        if (passingDataList != null) {
            Logutils.i("passingDataList:" + passingDataList.size());
            currentList = passingDataList;
        } else {
            Logutils.i("NoData");
            //初始页面显示的四屏数据
            currentList = pm.getObjects(videoCurrentPage);
            //初始页面单屏的数据
        }
        currentSingleList = singlePm.getObjects(videoCurrentPage);
        initPlayer();
        //加载底部数据
        initBottomData();
        //当前播放的视频中是否包含云台控制
        getPtz();
        //时间显示
        TimeThread timeThread = new TimeThread();
        new Thread(timeThread).start();


    }


    String mRtsp = "";
    String mToken = "";

    //是否有ptz功能的云台
    public void getPtz() {
        if (currentList != null && currentList.size() > 0) {
            for (Device d : currentList) {
                String rtsp = d.getPtzUrl();
                if (!TextUtils.isEmpty(rtsp)) {
                    mRtsp = rtsp;
                    String token = d.getProfiles().get(0).getToken();
                    if (!TextUtils.isEmpty(token)) {
                        mToken = token;
                    }
                }
            }
        }
    }

    /**
     * 底部的滑动
     */
    private void initBottomData() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(MutilScreenActivity.this, 1);
        gridLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        bottomSlidingView.setLayoutManager(gridLayoutManager);
        int images[] = new int[]{R.drawable.port_network_intercom_selected, R.drawable.port_instant_messaging_selected, R.drawable.port_video_surveillance_selected, R.drawable.port_alarm_btn_selected, R.drawable.port_bullet_btn_selected};
        ButtomSlidingAdapter ada = new ButtomSlidingAdapter(MutilScreenActivity.this, images, 2);
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
     * 初始化四个播放器
     */
    private void initPlayer() {
        single_screen_button_selecte.setBackgroundResource(R.mipmap.port_btn_single_normal);
        four_screen_button_select.setBackgroundResource(R.mipmap.port_monitoring_btn_4splitscreen_selected);
        if (currentList.size() == 4) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    first_pr_layout.setVisibility(View.VISIBLE);
                    first_dispaly_loading_layout.setVisibility(View.VISIBLE);
                    first_dispaly_loading_layout.setText("Loading...");

                    second_pr_layout.setVisibility(View.VISIBLE);
                    second_dispaly_loading_layout.setVisibility(View.VISIBLE);
                    second_dispaly_loading_layout.setText("Loading...");

                    third_pr_layout.setVisibility(View.VISIBLE);
                    third_dispaly_loading_layout.setVisibility(View.VISIBLE);
                    third_dispaly_loading_layout.setText("Loading...");

                    fourth_pr_layout.setVisibility(View.VISIBLE);
                    fourth_dispaly_loading_layout.setVisibility(View.VISIBLE);
                    fourth_dispaly_loading_layout.setText("Loading...");
                }
            });


            String rtsp1 = "";
            if (!TextUtils.isEmpty(currentList.get(0).getRtspUrl())) {
                rtsp1 = currentList.get(0).getRtspUrl();
            } else {
                rtsp1 = "";
            }
            String rtsp2 = "";
            if (!TextUtils.isEmpty(currentList.get(1).getRtspUrl())) {
                rtsp2 = currentList.get(1).getRtspUrl();
            } else {
                rtsp2 = "";
            }
            String rtsp3 = "";
            if (!TextUtils.isEmpty(currentList.get(2).getRtspUrl())) {
                rtsp3 = currentList.get(2).getRtspUrl();
            } else {
                rtsp3 = "";
            }
            String rtsp4 = "";
            if (!TextUtils.isEmpty(currentList.get(3).getRtspUrl())) {
                rtsp4 = currentList.get(3).getRtspUrl();
            } else {
                rtsp4 = "";
            }

            Logutils.i("rtsp1:" + rtsp1 + "\n" + "rtsp2:" + rtsp2 + "\n" + "rtsp3:" + rtsp3 + "\n" + "rtsp4:" + rtsp4);

            if (firstPalyer != null && firstPalyer.isPlaying()) {
                firstPalyer.stop();
            }
            if (secondPlayer != null && secondPlayer.isPlaying()) {
                secondPlayer.stop();
            }
            if (thirdPlayer != null && thirdPlayer.isPlaying()) {
                thirdPlayer.stop();
            }
            if (fourthPlayer != null && fourthPlayer.isPlaying()) {
                fourthPlayer.stop();
            }
            firstPalyer.setInputUrl(rtsp1);
            firstPalyer.setNodePlayerDelegate(this);
            firstPalyer.setAudioEnable(AppConfig.isVideoSound);
            firstPalyer.setVideoEnable(true);
            firstPalyer.start();
            secondPlayer.setInputUrl(rtsp2);
            secondPlayer.setNodePlayerDelegate(this);
            secondPlayer.setAudioEnable(AppConfig.isVideoSound);
            secondPlayer.setVideoEnable(true);
            secondPlayer.start();
            thirdPlayer.setInputUrl(rtsp3);
            thirdPlayer.setNodePlayerDelegate(this);
            thirdPlayer.setAudioEnable(AppConfig.isVideoSound);
            thirdPlayer.setVideoEnable(true);
            thirdPlayer.start();
            fourthPlayer.setInputUrl(rtsp4);
            fourthPlayer.setNodePlayerDelegate(this);
            fourthPlayer.setAudioEnable(AppConfig.isVideoSound);
            fourthPlayer.setVideoEnable(true);
            fourthPlayer.start();
        }
    }

    /**
     * 按钮的单击事件
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //加载更多数据
            case R.id.loading_more_videosources_layout:
                initListViewData();
                break;
            case R.id.first_player_layout:
                first_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_select_shape);
                second_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                third_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                fourth_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                firstViewSelect = true;
                secondViewSelect = false;
                thirdViewSelect = false;
                fourthViewSelect = false;
                break;
            case R.id.second_player_layout:
                first_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                second_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_select_shape);
                third_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                fourth_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                firstViewSelect = false;
                secondViewSelect = true;
                thirdViewSelect = false;
                fourthViewSelect = false;
                break;
            case R.id.third_player_layout:
                first_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                second_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                third_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_select_shape);
                fourth_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                firstViewSelect = false;
                secondViewSelect = false;
                thirdViewSelect = true;
                fourthViewSelect = false;
                break;
            case R.id.fourth_player_layout:
                first_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                second_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                third_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                fourth_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_select_shape);
                firstViewSelect = false;
                secondViewSelect = false;
                thirdViewSelect = false;
                fourthViewSelect = true;
                break;
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {

            case R.id.video_ptz_up:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!TextUtils.isEmpty(mRtsp) && !TextUtils.isEmpty(mToken)) {
                        ControlPtz controlPtz = new ControlPtz(mRtsp, mToken, "top", 0.00, 0.03);
                        controlPtz.start();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ControlPtz controlPtz = new ControlPtz(mRtsp, mToken, "stop", 0.00, 0.00);
                    controlPtz.start();
                }

                break;

            case R.id.video_ptz_down:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!TextUtils.isEmpty(mRtsp) && !TextUtils.isEmpty(mToken)) {
                        ControlPtz controlPtz = new ControlPtz(mRtsp, mToken, "below", 0.00, -0.03);
                        controlPtz.start();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ControlPtz controlPtz = new ControlPtz(mRtsp, mToken, "stop", 0.00, 0.00);
                    controlPtz.start();
                }
                break;

            case R.id.video_ptz_left:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!TextUtils.isEmpty(mRtsp) && !TextUtils.isEmpty(mToken)) {
                        ControlPtz controlPtz = new ControlPtz(mRtsp, mToken, "left", 0.03, 0.00);
                        controlPtz.start();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ControlPtz controlPtz = new ControlPtz(mRtsp, mToken, "stop", 0.00, 0.00);
                    controlPtz.start();
                }
                break;

            case R.id.video_ptz_right:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!TextUtils.isEmpty(mRtsp) && !TextUtils.isEmpty(mToken)) {
                        ControlPtz controlPtz = new ControlPtz(mRtsp, mToken, "right", -0.03, 0.00);
                        controlPtz.start();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ControlPtz controlPtz = new ControlPtz(mRtsp, mToken, "stop", 0.00, 0.00);
                    controlPtz.start();
                }
                break;

            case R.id.video_zoomout_button:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!TextUtils.isEmpty(mRtsp) && !TextUtils.isEmpty(mToken)) {
                        ControlPtz controlPtz = new ControlPtz(mRtsp, mToken, "zoom_s", 0.3, 0.03);
                        controlPtz.start();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ControlPtz controlPtz = new ControlPtz(mRtsp, mToken, "stop", 0.00, 0.00);
                    controlPtz.start();
                }
                break;

            case R.id.video_zoombig_button:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!TextUtils.isEmpty(mRtsp) && !TextUtils.isEmpty(mToken)) {
                        ControlPtz controlPtz = new ControlPtz(mRtsp, mToken, "zoom_b", -0.3, -0.03);
                        controlPtz.start();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ControlPtz controlPtz = new ControlPtz(mRtsp, mToken, "stop", 0.00, 0.00);
                    controlPtz.start();
                }
                break;
        }
        return false;
    }

    /**
     * 播放器回调监听
     *
     * @param player
     * @param event
     * @param msg
     */
    @Override
    public void onEventCallback(NodePlayer player, int event, final String msg) {
        if (firstPalyer == player) {
            if (event == 1001) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        first_pr_layout.setVisibility(View.GONE);
                        first_dispaly_loading_layout.setVisibility(View.GONE);
                    }
                });
            } else if (event == 1003) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        first_dispaly_loading_layout.setVisibility(View.VISIBLE);
                        first_pr_layout.setVisibility(View.GONE);
                        first_dispaly_loading_layout.setText(msg);
                    }
                });
            }
        }
        if (secondPlayer == player) {
            if (event == 1001) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        second_pr_layout.setVisibility(View.GONE);
                        second_dispaly_loading_layout.setVisibility(View.GONE);
                    }
                });
            } else if (event == 1003) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        second_dispaly_loading_layout.setVisibility(View.VISIBLE);
                        second_pr_layout.setVisibility(View.GONE);
                        second_dispaly_loading_layout.setText(msg);
                    }
                });
            }
        }


        if (thirdPlayer == player) {
            if (event == 1001) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        third_pr_layout.setVisibility(View.GONE);
                        third_dispaly_loading_layout.setVisibility(View.GONE);
                    }
                });
            } else if (event == 1003) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        third_dispaly_loading_layout.setVisibility(View.VISIBLE);
                        third_pr_layout.setVisibility(View.GONE);
                        third_dispaly_loading_layout.setText(msg);
                    }
                });
            }
        }
        if (fourthPlayer == player) {
            if (event == 1001) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fourth_pr_layout.setVisibility(View.GONE);
                        fourth_dispaly_loading_layout.setVisibility(View.GONE);
                    }
                });
            } else if (event == 1003) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fourth_dispaly_loading_layout.setVisibility(View.VISIBLE);
                        fourth_pr_layout.setVisibility(View.GONE);
                        fourth_dispaly_loading_layout.setText(msg);
                    }
                });
            }
        }
        if (singlePlayer == player) {
            if (event == 1001) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        single_player_progressbar_layout.setVisibility(View.GONE);
                        dispaly_video_loading_layout.setVisibility(View.GONE);
                    }
                });
            } else if (event == 1003) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dispaly_video_loading_layout.setVisibility(View.VISIBLE);
                        single_player_progressbar_layout.setVisibility(View.GONE);
                        dispaly_video_loading_layout.setText(msg);
                    }
                });
            }

        }

    }


    /**
     * finish本页面
     */
    @OnClick(R.id.finish_back_layout)
    public void finishThisActivity(View view) {
        if (firstPalyer != null) firstPalyer.release();
        firstPalyer = null;
        if (secondPlayer != null) secondPlayer.release();
        secondPlayer = null;
        if (thirdPlayer != null) thirdPlayer.release();
        thirdPlayer = null;
        if (fourthPlayer != null) fourthPlayer.release();
        fourthPlayer = null;
        if (singlePlayer != null) singlePlayer.release();
        singlePlayer = null;
        MutilScreenActivity.this.finish();
    }


    /**
     * 向服务器发送报警
     */
    @OnClick(R.id.send_alarmtoServer_button)
    public void sendAlarmToServer(View view) {

        if (isCurrentSingleScreen) {
            if (currentSingleList != null && currentSingleList.size() > 0) {
                VideoBen v = currentSingleList.get(0).getVideoBen();
                SendAlarmToServer sendAlarmToServer = new SendAlarmToServer(v, new SendAlarmToServer.Callback() {
                    @Override
                    public void getCallbackData(final String result) {
                        if (!TextUtils.isEmpty(result)){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MutilScreenActivity.this);
                                    builder.setTitle("报警信息:").setMessage(result).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).create().show();
                                }
                            });
                        }
                    }
                });
                sendAlarmToServer.start();
            }
        }
        if (isCurrentFourScreen) {

            if (firstViewSelect) {
                sendToAlarm(1);
            } else if (secondViewSelect) {
                sendToAlarm(2);
            } else if (thirdViewSelect) {
                sendToAlarm(3);
            } else if (fourthViewSelect) {
                sendToAlarm(4);
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toastShort("please select one window");
                    }
                });
            }
        }
    }

    private void sendToAlarm(int tag) {

        VideoBen v = null;
        if (currentList != null && currentList.size() > 0) {
            if (tag == 1) {
                v = currentList.get(0).getVideoBen();
            } else if (tag == 2) {
                v = currentList.get(1).getVideoBen();
            } else if (tag == 3) {
                v = currentList.get(2).getVideoBen();
            } else if (tag == 4) {
                v = currentList.get(3).getVideoBen();
            }
            if (v == null) {
                return;
            }
            SendAlarmToServer sendAlarmToServer = new SendAlarmToServer(v, new SendAlarmToServer.Callback() {
                @Override
                public void getCallbackData(final String result) {
                    if (!TextUtils.isEmpty(result)){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MutilScreenActivity.this);
                                builder.setTitle("报警信息:").setMessage(result).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).create().show();
                            }
                        });
                    }
                }
            });
            sendAlarmToServer.start();
        }
    }


    /**
     * ListView列表展示数据
     */
    private void initListViewData() {
        openActivity(ChannelListActivity.class);
    }

    /**
     * 单屏播放rtsp
     */
    private void initSinglePlayer(String rtsp) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dispaly_video_loading_layout.setVisibility(View.VISIBLE);
                single_player_progressbar_layout.setVisibility(View.VISIBLE);
                dispaly_video_loading_layout.setText("Loading");
            }
        });

        if (singlePlayer != null && singlePlayer.isPlaying()) {
            singlePlayer.pause();
            singlePlayer.stop();
        }

        if (firstPalyer != null && firstPalyer.isPlaying()) {
            firstPalyer.pause();
            firstPalyer.stop();
        }
        if (secondPlayer != null && secondPlayer.isPlaying()) {
            secondPlayer.pause();
            secondPlayer.stop();
        }

        if (thirdPlayer != null && thirdPlayer.isPlaying()) {
            thirdPlayer.pause();
            thirdPlayer.stop();
        }

        if (fourthPlayer != null && fourthPlayer.isPlaying()) {
            fourthPlayer.pause();
            fourthPlayer.stop();
        }

        firstPlayerView.setVisibility(View.GONE);
        secondPlayerView.setVisibility(View.GONE);
        thirdPlayerView.setVisibility(View.GONE);
        fourthPlayerView.setVisibility(View.GONE);
        four_surfaceview_parent_relativelayout.setVisibility(View.GONE);
        single_surfaceview_parent_relativelayout.setVisibility(View.VISIBLE);

        single_player_layout.setVisibility(View.VISIBLE);
        singlePlayer = new NodePlayer(this);
        singlePlayer.setPlayerView(single_player_layout);
        singlePlayer.setInputUrl(rtsp);
        singlePlayer.setNodePlayerDelegate(this);
        singlePlayer.setAudioEnable(AppConfig.isVideoSound);
        singlePlayer.setVideoEnable(true);
        singlePlayer.start();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                display_video_information_text_layout.setText(currentSingleList.get(0).getVideoBen().getName());
            }
        });
        single_player_layout.setOnClickListener(this);
    }


    /**
     * 下一页
     */
    @OnClick(R.id.video_nextpage_button)
    public void videoNextPage(View view) {
        videoCurrentPage++;
        if (isCurrentFourScreen) {
            if (isCurrentFourScreen) {
                if (pm != null && pm.isHasNextPage()) {
                    currentList = pm.getObjects(videoCurrentPage);
                    initPlayer();
                }
            }
        }
        if (isCurrentSingleScreen) {
            if (singlePm != null && singlePm.isHasNextPage()) {
                currentSingleList = singlePm.getObjects(videoCurrentPage);
                String rtsp = "";
                if (!TextUtils.isEmpty(currentSingleList.get(0).getRtspUrl())) {
                    rtsp = currentSingleList.get(0).getRtspUrl();
                }
                initSinglePlayer(rtsp);
            }
        }

        getPtz();
    }

    /**
     * 上一页
     */
    @OnClick(R.id.video_previous_button)
    public void videoPreviousPage(View view) {
        videoCurrentPage--;
        if (isCurrentFourScreen) {
            if (pm != null && pm.isHasPreviousPage()) {
                currentList = pm.getObjects(videoCurrentPage);
                initPlayer();
            } else {
                videoCurrentPage = 1;
            }
        }

        if (isCurrentSingleScreen) {
            if (singlePm != null && singlePm.isHasPreviousPage()) {
                currentSingleList = singlePm.getObjects(videoCurrentPage);
                String rtsp = "";
                if (!TextUtils.isEmpty(currentSingleList.get(0).getRtspUrl())) {
                    rtsp = currentSingleList.get(0).getRtspUrl();
                }
                initSinglePlayer(rtsp);
            } else {
                videoCurrentPage = 1;
            }
        }
        getPtz();
    }


    /**
     * 单屏播放rtsp
     */
    @OnClick(R.id.single_screen_button_selecte)
    public void singleScreenVideo(View view) {
        single_screen_button_selecte.setBackgroundResource(R.mipmap.port_btn_single_selected);
        four_screen_button_select.setBackgroundResource(R.mipmap.port_monitoring_btn_4splitscreen_normal);
        isCurrentFourScreen = false;
        isCurrentSingleScreen = true;


        if (firstViewSelect) {
            String rtsp = "";
            if (!TextUtils.isEmpty(currentList.get(0).getRtspUrl())) {
                rtsp = currentList.get(0).getRtspUrl();
            }
            initSinglePlayer(rtsp);
        }
        if (secondViewSelect) {
            String rtsp = "";
            if (!TextUtils.isEmpty(currentList.get(1).getRtspUrl())) {
                rtsp = currentList.get(1).getRtspUrl();
            }
            initSinglePlayer(rtsp);
        }

        if (thirdViewSelect) {
            String rtsp = "";
            if (!TextUtils.isEmpty(currentList.get(2).getRtspUrl())) {
                rtsp = currentList.get(2).getRtspUrl();
            }
            initSinglePlayer(rtsp);
        }

        if (fourthViewSelect) {
            String rtsp = "";
            if (!TextUtils.isEmpty(currentList.get(3).getRtspUrl())) {
                rtsp = currentList.get(3).getRtspUrl();
            }
            initSinglePlayer(rtsp);
        }
    }


    /**
     * 四屏播放rtsp
     */
    @OnClick(R.id.four_screen_button_select)
    public void fourScreenVideo(View view) {
        single_screen_button_selecte.setBackgroundResource(R.mipmap.port_btn_single_normal);
        four_screen_button_select.setBackgroundResource(R.mipmap.port_monitoring_btn_4splitscreen_selected);
        isCurrentSingleScreen = false;
        isCurrentFourScreen = true;

        if (singlePlayer != null && singlePlayer.isPlaying()) {
            singlePlayer.pause();
            singlePlayer.stop();
        }
        four_surfaceview_parent_relativelayout.setVisibility(View.VISIBLE);
        single_surfaceview_parent_relativelayout.setVisibility(View.GONE);
        single_player_layout.setVisibility(View.GONE);

        firstPlayerView.setVisibility(View.VISIBLE);
        secondPlayerView.setVisibility(View.VISIBLE);
        thirdPlayerView.setVisibility(View.VISIBLE);
        fourthPlayerView.setVisibility(View.VISIBLE);

        firstPalyer.start();
        secondPlayer.start();
        thirdPlayer.start();
        fourthPlayer.start();
    }

    boolean firstPlayerIsStop = false;

    /**
     * 播放器停止
     */
    @OnClick(R.id.paly_or_stop_button_select)
    public void stopOrRestartVideo(View view) {
        if (isCurrentFourScreen) {
            if (firstViewSelect) {
                if (firstPalyer != null) {
                    if (!firstPlayerIsStop) {
                        firstPalyer.stop();
                        firstPlayerIsStop = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                paly_or_stop_button_select.setBackgroundResource(R.mipmap.port_monitoring_icon_player_selected);
                            }
                        });
                    } else {
                        firstPlayerIsStop = false;
                        firstPalyer.start();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                paly_or_stop_button_select.setBackgroundResource(R.mipmap.port_monitoring_icon_stopplay_normal);
                            }
                        });
                    }

                }
            } else if (secondViewSelect) {
                if (secondPlayer != null) {
                    if (!firstPlayerIsStop) {
                        secondPlayer.stop();
                        firstPlayerIsStop = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                paly_or_stop_button_select.setBackgroundResource(R.mipmap.port_monitoring_icon_player_selected);
                            }
                        });
                    } else {
                        firstPlayerIsStop = false;
                        secondPlayer.start();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                paly_or_stop_button_select.setBackgroundResource(R.mipmap.port_monitoring_icon_stopplay_normal);
                            }
                        });
                    }

                }
            } else if (thirdViewSelect) {
                if (thirdPlayer != null) {
                    if (!firstPlayerIsStop) {
                        thirdPlayer.stop();
                        firstPlayerIsStop = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                paly_or_stop_button_select.setBackgroundResource(R.mipmap.port_monitoring_icon_player_selected);
                            }
                        });
                    } else {
                        firstPlayerIsStop = false;
                        thirdPlayer.start();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                paly_or_stop_button_select.setBackgroundResource(R.mipmap.port_monitoring_icon_stopplay_normal);
                            }
                        });
                    }

                }
            } else if (fourthViewSelect) {
                if (fourthPlayer != null) {
                    if (!firstPlayerIsStop) {
                        fourthPlayer.stop();
                        firstPlayerIsStop = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                paly_or_stop_button_select.setBackgroundResource(R.mipmap.port_monitoring_icon_player_selected);
                            }
                        });
                    } else {
                        firstPlayerIsStop = false;
                        fourthPlayer.start();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                paly_or_stop_button_select.setBackgroundResource(R.mipmap.port_monitoring_icon_stopplay_normal);
                            }
                        });
                    }

                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showShort("未选中窗口！！！");
                    }
                });
            }
        } else if (isCurrentSingleScreen) {
            if (singlePlayer != null) {
                singlePlayer.stop();
            }
        }
    }
}
