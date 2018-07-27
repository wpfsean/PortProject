package com.zhketech.mstapp.client.port.project.callbacks;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;

import com.zhketech.mstapp.client.port.project.utils.SharedPreferencesUtils;

/**
 * 获取电量信息和wifi信息
 */
public class BatteryAndWifiService extends Service {

    public static BatteryAndWifiCallback mBatteryCallback;

    private static final String TAG = "TAG";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter batteryfilter = new IntentFilter();
        batteryfilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, batteryfilter);

        IntentFilter wififilter = new IntentFilter();
        wififilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        registerReceiver(wifiReceiver, wififilter);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY; //
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(batteryReceiver);
        this.unregisterReceiver(wifiReceiver);

    }

    // 接收电池信息更新的广播
    private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int level = intent.getIntExtra("level", 0);
            SharedPreferencesUtils.putObject(context,"battery",level);
           // Logutils.i("电量："+level);

            if (mBatteryCallback != null) {
                mBatteryCallback.getBatteryData(level);
            }
        }
    };

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifi.getConnectionInfo();
            if (wifiInfo.getSSID() != null) {
                int num = wifiInfo.getRssi();
               // Logutils.i("w信号强度："+num);
                SharedPreferencesUtils.putObject(context,"wifi",num);
                if (mBatteryCallback != null) {
                    mBatteryCallback.getWifiData(num);
                }
            }
        }
    };


    public static void addBatterCallback(BatteryAndWifiCallback batteryCallback) {
        mBatteryCallback = batteryCallback;
    }


}