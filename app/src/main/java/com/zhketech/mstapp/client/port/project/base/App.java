package com.zhketech.mstapp.client.port.project.base;

import android.app.Application;
import android.content.Context;

import com.zhketech.mstapp.client.port.project.onvif.Device;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Root on 2018/7/11.
 */

public class App extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }


    public static Context getInstance() {
        return mContext;
    }


}
