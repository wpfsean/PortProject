package com.zhketech.mstapp.client.port.project.callbacks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;


import com.zhketech.mstapp.client.port.project.utils.ByteUtils;
import com.zhketech.mstapp.client.port.project.utils.PhoneUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 广播接收定时任务用于发送心跳
 * Created by Root on 2018/4/27.
 */

public class SendHeartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        List<String> points = new ArrayList<String>();
        String deviceId = PhoneUtils.getPhoneInfo(context, 1);
        if (!TextUtils.isEmpty(deviceId) && deviceId != null) {
            points.add(deviceId);
        } else {
            points.add(PhoneUtils.getString(24));
        }
        long stamp = PhoneUtils.dateStamp();
        byte[] timeByte = ByteUtils.longToBytes(stamp);
        SendHearToServerThread hearToServer = new SendHearToServerThread(points, timeByte);
        new Thread(hearToServer).start();
        Intent i = new Intent(context, SendheartService.class);
        context.startService(i);
    }
}
