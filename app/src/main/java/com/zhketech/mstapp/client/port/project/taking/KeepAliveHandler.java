package com.zhketech.mstapp.client.port.project.taking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class KeepAliveHandler extends BroadcastReceiver {
    private static final String TAG = "KeepAliveHandler";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (SipManager.getLcIfManagerNotDestroyOrNull() != null) {
            SipManager.getLc().refreshRegisters();
//            SPUtils.save(context, "keepAlive", true);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Log.e(TAG, "Cannot sleep for 2s");
            }
        }
    }
}
