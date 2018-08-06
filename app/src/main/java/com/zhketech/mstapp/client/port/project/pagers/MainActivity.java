package com.zhketech.mstapp.client.port.project.pagers;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.zhketech.mstapp.client.port.project.R;
import com.zhketech.mstapp.client.port.project.base.App;
import com.zhketech.mstapp.client.port.project.base.BaseActivity;
import com.zhketech.mstapp.client.port.project.beans.AlarmBen;
import com.zhketech.mstapp.client.port.project.beans.SipBean;
import com.zhketech.mstapp.client.port.project.beans.VideoBen;
import com.zhketech.mstapp.client.port.project.callbacks.AmmoRequestCallBack;
import com.zhketech.mstapp.client.port.project.callbacks.ReceiveEmergencyAlarm;
import com.zhketech.mstapp.client.port.project.callbacks.ReceiveServerMess;
import com.zhketech.mstapp.client.port.project.callbacks.ReceiverServerAlarm;
import com.zhketech.mstapp.client.port.project.callbacks.RequestSipSourcesThread;
import com.zhketech.mstapp.client.port.project.callbacks.RequestVideoSourcesThread;
import com.zhketech.mstapp.client.port.project.callbacks.SendEmergencyAlarmToServer;
import com.zhketech.mstapp.client.port.project.callbacks.SendheartService;
import com.zhketech.mstapp.client.port.project.db.DatabaseHelper;
import com.zhketech.mstapp.client.port.project.global.AppConfig;
import com.zhketech.mstapp.client.port.project.onvif.Device;
import com.zhketech.mstapp.client.port.project.onvif.Onvif;
import com.zhketech.mstapp.client.port.project.taking.Linphone;
import com.zhketech.mstapp.client.port.project.taking.PhoneCallback;
import com.zhketech.mstapp.client.port.project.taking.RegistrationCallback;
import com.zhketech.mstapp.client.port.project.taking.SipService;
import com.zhketech.mstapp.client.port.project.utils.GsonUtils;
import com.zhketech.mstapp.client.port.project.utils.Logutils;
import com.zhketech.mstapp.client.port.project.utils.SharedPreferencesUtils;
import com.zhketech.mstapp.client.port.project.utils.WriteLogToFile;

import org.linphone.core.LinphoneCall;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author wpf
 *         主页面
 */

public class MainActivity extends BaseActivity {
    //handler时间标识
    public static int timeFlage = 10001;
    //线程池
    ExecutorService fixedThreadPool = null;
    //数据标识
    public static final int FLAGE = 1000;
    //时间线程是否正在运行
    boolean threadIsRun = true;
    //存放设备信息的集合
    List<Device> dataSources = new ArrayList<>();

    List<SipVideo> sipData = new ArrayList<>();
    int videoResourcesNum = -1;
    int sipResourcesNum = 0;
    //时间
    @BindView(R.id.main_incon_time)
    TextView timeTextView;
    //日期
    @BindView(R.id.main_icon_date)
    TextView dateTextView;

    @BindView(R.id.main_loading_layout)
    ImageView main_loading_layout;

    @BindView(R.id.main_loading_textview_layout)
    TextView main_loading_textview_layout;

    Animation mLoadingAnim;

    boolean isSendAlaem;


    //handler處理ui
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //主页面时间显示
            if (msg.what == timeFlage) {
                long time = System.currentTimeMillis();
                Date date = new Date(time);
                SimpleDateFormat timeD = new SimpleDateFormat("HH:mm:ss");
                timeTextView.setText(timeD.format(date).toString());
                SimpleDateFormat dateD = new SimpleDateFormat("MM月dd日 EEE");
                dateTextView.setText(dateD.format(date).toString());
            } else if (msg.what == FLAGE) {
                //onvif数据处理
                Bundle bundle = msg.getData();
                Device device = (Device) bundle.getSerializable("device");
                dataSources.add(device);
                if (dataSources.size() == videoResourcesNum) {
                    String json = GsonUtils.getGsonInstace().list2String(dataSources);
                    if (TextUtils.isEmpty(json)) {
                        return;
                    }
                    SharedPreferencesUtils.putObject(MainActivity.this, "result", json);
                }
            } else if (msg.what == 111) {
                Bundle bundle = msg.getData();
                SipVideo mSipVideo = (SipVideo) bundle.getSerializable("sipVideo");
                sipData.add(mSipVideo);
                if (sipData.size() == sipResourcesNum) {
                    Gson gson = new Gson();
                    String str = gson.toJson(sipData);
                    if (!TextUtils.isEmpty(str)) {
                        SharedPreferencesUtils.putObject(MainActivity.this, "sipresult", str);
                        Logutils.i("success sip ");
                    }
                }
            }
        }
    };

    @Override
    public int intiLayout() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
    }
    String nativeIp ;
    @Override
    public void initData() {
         nativeIp = (String) SharedPreferencesUtils.getObject(App.getInstance(), "nativeIp", "");

        //啟動心中服務
        startService(new Intent(this, SendheartService.class));
        mLoadingAnim = AnimationUtils.loadAnimation(this, R.anim.loading);
        fixedThreadPool = Executors.newFixedThreadPool(5);

        //接收短消息
        ReceiveServerMess receiveServerMess = new ReceiveServerMess(new ReceiveServerMess.GetSmsListern() {
            @Override
            public void getSmsContent(final String ms) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                        alert.setTitle("短信息").setMessage(ms).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                    }
                });

                DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this);
                databaseHelper.insertMessData(new Date().toString(), "true", ms, "receivermess");
                databaseHelper.close();

            }
        });
        new Thread(receiveServerMess).start();
        //接收報警報文
        ReceiverServerAlarm receiverServerAlarm = new ReceiverServerAlarm(new ReceiverServerAlarm.GetAlarmFromServerListern() {
            @Override
            public void getListern(final AlarmBen alarmBen, final String flage) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("公告信息").setMessage(alarmBen.getAlertType()).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                    }
                });
                DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this);
                databaseHelper.insertMessData(new Date().toString(), flage + "", alarmBen.toString(), "receiveralarm");
                databaseHelper.close();
            }
        });
        new Thread(receiverServerAlarm).start();

        //顯示當前頁面的時間
        TimeThread timeThread = new TimeThread();
        new Thread(timeThread).start();
        //獲取sip信息
        getNativeSipInformation();
        //獲取所願的videoResources資源
        getAllVideoResoucesInformation();
    }

    /**
     * 获取所有的video资源并解析rtsp
     */
    private void getAllVideoResoucesInformation() {
        RequestVideoSourcesThread requestVideoSourcesThread = new RequestVideoSourcesThread(MainActivity.this, new RequestVideoSourcesThread.GetDataListener() {
            @Override
            public void getResult(final List<VideoBen> mList) {

                if (mList != null && mList.size() > 0) {
                    Logutils.i("AAAAAAAAA:" + mList.size());
                    //总数据量
                    videoResourcesNum = mList.size();
                    for (int i = 0; i < mList.size(); i++) {
                        String deviceType = mList.get(i).getDevicetype();
                        if (deviceType.equals("ONVIF")) {
                            String ip = mList.get(i).getIp();
                            final Device device = new Device();
                            device.setVideoBen(mList.get(i));
                            device.setServiceUrl("http://" + ip + "/onvif/device_service");
                            Onvif onvif = new Onvif(device, new Onvif.GetRtspCallback() {
                                @Override
                                public void getDeviceInfoResult(String rtsp, boolean isSuccess, Device mDevice) {
                                    Message message = new Message();
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("device", mDevice);
                                    message.setData(bundle);
                                    message.what = FLAGE;
                                    handler.sendMessage(message);
                                }
                            });
                            fixedThreadPool.execute(onvif);
                        } else if (deviceType.equals("RTSP")) {
                            String mRtsp = "rtsp://" + mList.get(i).getUsername() + ":" + mList.get(i).getPassword() + "@" + mList.get(i).getIp() + "/" + mList.get(i).getChannel();
                            VideoBen v = mList.get(i);
                            v.setRtsp(mRtsp);
                            Device device = new Device();
                            device.setRtspUrl(mRtsp);
                            device.setVideoBen(v);
                            Message message = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("device", device);
                            message.setData(bundle);
                            message.what = FLAGE;
                            handler.sendMessage(message);
                        }
                    }
                } else {
                    Logutils.i("AAAAAAAA:<0");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("No Video Infor").setMessage("未获取到VideoResources，请检查本机网络是否连接正常~~").create().show();
                        }
                    });
                    WriteLogToFile.info("No get Video Resources Data !!!");
                }
            }
        });
        requestVideoSourcesThread.start();
    }

    /**
     * 获取本机的sip信息
     */
    private void getNativeSipInformation() {

        RequestSipSourcesThread sipThread = new RequestSipSourcesThread(MainActivity.this, "0", new RequestSipSourcesThread.SipListern() {
            @Override
            public void getDataListern(List<SipBean> mList) {
                Logutils.i("mlist:" + mList.size());
                if (mList != null && mList.size() > 0) {
                    String nativeIp = (String) SharedPreferencesUtils.getObject(MainActivity.this, "nativeIp", "");
                    if (!TextUtils.isEmpty(nativeIp)) {
                        for (SipBean s : mList) {
                            if (s.getIp().equals(nativeIp)) {
                                String sipName = s.getName();
                                String sipNum = s.getNumber();
                                String sipPwd = s.getSippass();
                                String sipServer = s.getSipserver();
                                if (!TextUtils.isEmpty(sipNum) && !TextUtils.isEmpty(sipPwd) && !TextUtils.isEmpty(sipServer)) {
                                    SharedPreferencesUtils.putObject(MainActivity.this, "sipName", sipName);
                                    SharedPreferencesUtils.putObject(MainActivity.this, "sipNum", sipNum);
                                    SharedPreferencesUtils.putObject(MainActivity.this, "sipPwd", sipPwd);
                                    SharedPreferencesUtils.putObject(MainActivity.this, "sipServer", sipServer);
                                    registerSipIntoServer(sipNum, sipPwd, sipServer);
                                }
                                break;
                            }
                        }
                    }
                    for (int i = 0; i < mList.size(); i++) {
                        final SipVideo sipVideo = new SipVideo();
                        sipVideo.setNum(mList.get(i).getNumber());
                        String deviceType = mList.get(i).getVideoBen().getDevicetype();
                        if (!TextUtils.isEmpty(deviceType)) {
                            sipResourcesNum += 1;
                            if (deviceType.equals("ONVIF")) {
                                String ip = mList.get(i).getVideoBen().getIp();
                                final Device device = new Device();
                                device.setVideoBen(mList.get(i).getVideoBen());
                                device.setServiceUrl("http://" + ip + "/onvif/device_service");
                                Onvif onvif = new Onvif(device, new Onvif.GetRtspCallback() {
                                    @Override
                                    public void getDeviceInfoResult(String rtsp, boolean isSuccess, Device mDevice) {
                                        Message message = new Message();
                                        Bundle bundle = new Bundle();
                                        sipVideo.setRtsp(rtsp);
                                        bundle.putSerializable("sipVideo", sipVideo);
                                        message.setData(bundle);
                                        message.what = 111;
                                        handler.sendMessage(message);
                                    }
                                });
                                fixedThreadPool.execute(onvif);
                            } else if (deviceType.equals("RTSP")) {
                                String mRtsp = "rtsp://" + mList.get(i).getVideoBen().getUsername() + ":" + mList.get(i).getVideoBen().getPassword() + "@" + mList.get(i).getIp() + "/" + mList.get(i).getVideoBen().getChannel();
                                Message message = new Message();
                                Bundle bundle = new Bundle();
                                sipVideo.setRtsp(mRtsp);
                                bundle.putSerializable("sipVideo", sipVideo);
                                message.setData(bundle);
                                message.what = 111;
                                handler.sendMessage(message);
                            }
                        }
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("No Sip Infor").setMessage("未获取到sip信息，请检查本机ip是否配置正确~~").create().show();
                        }
                    });
                }
            }
        });
        fixedThreadPool.execute(sipThread);


        /**
         * 监听应急报警
         */
        ReceiveEmergencyAlarm ReceiveEmergencyAlarm = new ReceiveEmergencyAlarm(new ReceiveEmergencyAlarm.EmergencyAlarmText() {
            @Override
            public void getListern(final AlarmBen alarmBen) {

                if (!isSendAlaem){
                    return;
                }
                final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (alarmBen != null) {
                            builder.setTitle("友邻哨报警信息").setMessage(alarmBen.getVideoBen().getName()+"\n"+alarmBen.getAlertType()).create().show();
                        } else {
                            toastShort("infor is null ");
                        }
                    }
                });
            }
        });
        new Thread(ReceiveEmergencyAlarm).start();
    }

    /**
     * 解析sip视频信息中的rtsp
     *
     * @param mList
     */
    private void resolveSipResourcesRtsp(List<SipBean> mList) {

        for (int i = 0; i < mList.size(); i++) {
            String deviceType = mList.get(i).getVideoBen().getDevicetype();
            if (!TextUtils.isEmpty(deviceType)) {
                if (deviceType.equals("ONVIF")) {
                    String ip = mList.get(i).getVideoBen().getIp();
                    final Device device = new Device();
                    device.setVideoBen(mList.get(i).getVideoBen());
                    device.setServiceUrl("http://" + ip + "/onvif/device_service");
                    Onvif onvif = new Onvif(device, new Onvif.GetRtspCallback() {
                        @Override
                        public void getDeviceInfoResult(String rtsp, boolean isSuccess, Device mDevice) {
                            Message message = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("sipdevice", mDevice);
                            message.setData(bundle);
                            message.what = 111;
                            handler.sendMessage(message);
                        }
                    });
                    fixedThreadPool.execute(onvif);
                } else if (deviceType.equals("RTSP")) {
                    String mRtsp = "rtsp://" + mList.get(i).getVideoBen().getUsername() + ":" + mList.get(i).getVideoBen().getPassword() + "@" + mList.get(i).getIp() + "/" + mList.get(i).getVideoBen().getChannel();
                    VideoBen v = mList.get(i).getVideoBen();
                    v.setRtsp(mRtsp);
                    Device device = new Device();
                    device.setRtspUrl(mRtsp);
                    device.setVideoBen(v);
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("sipdevice", device);
                    message.setData(bundle);
                    message.what = 111;
                    handler.sendMessage(message);
                }
            }
        }


    }

    @OnClick({R.id.button_alarm, R.id.button_intercom, R.id.button_video, R.id.button_applyforplay, R.id.button_chat, R.id.button_setup})
    public void onclickEvent(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.button_intercom:
                intent.setClass(MainActivity.this, SipGroupActivity.class);
                startActivity(intent);
                break;
            case R.id.button_video:
                intent.setClass(MainActivity.this, MutilScreenActivity.class);
                startActivity(intent);
                break;
            case R.id.button_applyforplay:
                applyForUnpacking();
                break;
            case R.id.button_chat:
                intent.setClass(MainActivity.this, ChatListActivity.class);
                startActivity(intent);
                break;
            case R.id.button_setup:
                intent.setClass(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.button_alarm:
                emergencyAlarm();
                break;
        }
    }

    /**
     * 开箱申请
     */
    private void applyForUnpacking() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                main_loading_layout.setVisibility(View.VISIBLE);
                main_loading_layout.startAnimation(mLoadingAnim);
                main_loading_textview_layout.setVisibility(View.VISIBLE);
                main_loading_textview_layout.setText("正在计算开锁码");
            }
        });
        AmmoRequestCallBack ammoRequestCallBack = new AmmoRequestCallBack(new AmmoRequestCallBack.GetDataListern() {
            @Override
            public void getDataInformation(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        main_loading_layout.setVisibility(View.GONE);
                        main_loading_layout.clearAnimation();
                        main_loading_textview_layout.setVisibility(View.GONE);
                        main_loading_textview_layout.setText("");
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("申请开箱状态:").setMessage(result).create().show();
                    }
                });
            }
        });
        ammoRequestCallBack.start();
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
                    msg.what = timeFlage;
                    handler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (threadIsRun);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        threadIsRun = false;
    }

    /**
     * 註冊sip話機到sip服務器(MiniSipServer)
     *
     * @param sipNum
     * @param sipPwd
     * @param sipServer
     */
    private void registerSipIntoServer(String sipNum, String sipPwd, String sipServer) {

        if (!SipService.isReady()) {
            Linphone.startService(this);
        }
        Linphone.setAccount(sipNum, sipPwd, sipServer);
        Linphone.login();
    }


    @Override
    protected void onResume() {
        super.onResume();

        //sip監聽囘調
        Linphone.addCallback(new RegistrationCallback() {
            @Override
            public void registrationProgress() {
                Logutils.i("registering");
            }

            @Override
            public void registrationOk() {
                Logutils.i("register ok");
                SharedPreferencesUtils.putObject(MainActivity.this, "registerStatus", true);
            }

            @Override
            public void registrationFailed() {
                Logutils.i("register fail");
                SharedPreferencesUtils.putObject(MainActivity.this, "registerStatus", false);
            }
        }, new PhoneCallback() {
            @Override
            public void incomingCall(LinphoneCall linphoneCall) {
                Logutils.i("来电");
            }

            @Override
            public void outgoingInit() {
                Logutils.i("拨打");
            }

            @Override
            public void callConnected() {
                Logutils.i("接通");
            }

            @Override
            public void callEnd() {
                Logutils.i("结束");
            }

            @Override
            public void callReleased() {
                Logutils.i("释放");
            }

            @Override
            public void error() {
                Logutils.i("出错");
            }
        });
    }


    class SipVideo implements Serializable {
        private String rtsp;
        private String num;

        public String getRtsp() {
            return rtsp;
        }

        public void setRtsp(String rtsp) {
            this.rtsp = rtsp;
        }

        public String getNum() {
            return num;
        }

        public void setNum(String num) {
            this.num = num;
        }

        public SipVideo() {
        }

        @Override
        public String toString() {
            return "SipVideo{" +
                    "rtsp='" + rtsp + '\'' +
                    ", num='" + num + '\'' +
                    '}';
        }
    }


    //应急报警
    public void emergencyAlarm() {
        final String[] alermType = {"脱逃", "暴狱", "袭击", "灾害", "挟持", "突发"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请选择报警类型:");
        builder.setItems(alermType, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String type = alermType[which];
                VideoBen videoBen = new VideoBen("flage", "flage", "flage", "flage", nativeIp, "flage", "flage", "flage", "flage", "flage", false, "flage", "flage");
                SendEmergencyAlarmToServer  sendEmergencyAlarmToServer = new SendEmergencyAlarmToServer(videoBen, type, new SendEmergencyAlarmToServer.Callback() {
                    @Override
                    public void getCallbackData(String result) {
                        Logutils.i("callback result:"+result);
                        isSendAlaem = true;
                    }
                });
                new Thread(sendEmergencyAlarmToServer).start();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
