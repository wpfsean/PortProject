package com.zhketech.mstapp.client.port.project.callbacks;

/**
 * Created by Root on 2018/7/4.
 *
 * 电池电量和wifi信息的的回调
 *
 */

public abstract class BatteryAndWifiCallback {
    public void getBatteryData(int level) {
    }

    ;

    public void getWifiData(int rssi) {
    }

    ;

}
