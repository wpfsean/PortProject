package com.zhketech.mstapp.client.port.project.pagers;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zhketech.mstapp.client.port.project.R;
import com.zhketech.mstapp.client.port.project.adpaters.SettingParentAdapter;
import com.zhketech.mstapp.client.port.project.adpaters.SettingSubAdapter;
import com.zhketech.mstapp.client.port.project.base.App;
import com.zhketech.mstapp.client.port.project.base.BaseActivity;
import com.zhketech.mstapp.client.port.project.global.AppConfig;
import com.zhketech.mstapp.client.port.project.utils.Logutils;
import com.zhketech.mstapp.client.port.project.utils.SharedPreferencesUtils;
import com.zhketech.mstapp.client.port.project.view.CustomListView;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 設置中心頁面
 */
public class SettingActivity extends BaseActivity {
    @BindView(R.id.parent_listview_layout)
    CustomListView parentListView;
    @BindView(R.id.sub_listview_layout)
    CustomListView subListView;
    private SettingParentAdapter parentAdapter;
    private SettingSubAdapter subAdapter;

    @BindView(R.id.setting_time_layout)
    TextView timeTextView;

    String serverIp = (String) SharedPreferencesUtils.getObject(App.getInstance(), "serverip", "");


    String subTypes[][] = new String[][]{
            new String[]{"服务器Ip:" + serverIp, "心跳Port:" + AppConfig.heart_port + "", "", "", ""},
            new String[]{"报警服务Ip:" + AppConfig.alarm_server_ip, "报警服务port:" + AppConfig.alarm_server_port + "", "", "", ""},
            new String[]{"当前码流:" + AppConfig.isMainStream, "是否播放声音:" + AppConfig.isVideoSound, "", "", ""},
            new String[]{serverIp, AppConfig.server_port + "", AppConfig.current_user, AppConfig.current_pass, ""}
    };
    String type[] = new String[]{"心跳設置", "報警設置", "码流设置", "中心服務器設置"};
    int images[] = new int[]{R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher
    };


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
            }
        }
    };


    @Override
    public int intiLayout() {
        return R.layout.activity_setting;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
    }

    @Override
    public void initData() {
        parentAdapter = new SettingParentAdapter(getApplicationContext(), type, images);
        parentListView.setAdapter(parentAdapter);
        selectDefult();
        parentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
                final int location = position;
                parentAdapter.setSelectedPosition(position);
                parentAdapter.notifyDataSetInvalidated();
                subAdapter = new SettingSubAdapter(getApplicationContext(), subTypes, position);
                subListView.setAdapter(subAdapter);
                subListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1,
                                            int position, long arg3) {

                        updateInformation(location, position);
                        Toast.makeText(getApplicationContext(), subTypes[location][position], Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        TimeThread timeThread = new TimeThread();
        new Thread(timeThread).start();
    }

    private void updateInformation(final int location, final int position) {

        Logutils.i("AAAAAAAAA:" + location + "\t" + position);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                if (location == 0) {
                    builder.setTitle("心跳设置");
                } else if (location == 1) {
                    builder.setTitle("报警设置");
                }else if (location == 3){
                    builder.setTitle("中心服务器设置");
                }
                final EditText editText = new EditText(SettingActivity.this);
                builder.setView(editText).setPositiveButton("sure", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String editStr = editText.getText().toString().trim();
                        if (TextUtils.isEmpty(editStr)) {
                            return;
                        }
                        if (location == 0) {
                            if (position == 0) {
                                SharedPreferencesUtils.putObject(App.getInstance(), "serverip", editStr);
                            } else if (position == 1) {
                                AppConfig.heart_port = Integer.parseInt(editStr);
                            }
                        } else if (location == 1) {
                            if (position == 0) {
                                AppConfig.alarm_server_ip = editStr;
                            } else if (position == 1) {
                                AppConfig.alarm_server_port = Integer.parseInt(editStr);
                            }

                        }else if (location == 3) {

                            if (position == 0) {
                                SharedPreferencesUtils.putObject(App.getInstance(), "serverip", editStr);
                            } else if (position == 1) {
                                AppConfig.server_port = Integer.parseInt(editStr);
                            }

                        }
                        subTypes[location][position] = editText.getText().toString().trim();
                        subAdapter.notifyDataSetChanged();


                    }
                }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
            }
        });

    }

    private void selectDefult() {
        final int location = 0;
        parentAdapter.setSelectedPosition(0);
        parentAdapter.notifyDataSetInvalidated();
        subAdapter = new SettingSubAdapter(getApplicationContext(), subTypes, 0);
        subListView.setAdapter(subAdapter);
        subListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                updateInformation(location, position);
                Toast.makeText(getApplicationContext(), subTypes[location][position], Toast.LENGTH_SHORT).show();
            }
        });
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

    @OnClick(R.id.sip_group_finish_icon)
    public void finishPager(View view) {
        threadIsRun = false;
        SettingActivity.this.finish();
    }
}
