package com.zhketech.mstapp.client.port.project.utils;

import android.content.Context;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Root on 2018/7/23.
 */

public class TimeDo implements Runnable {
    public  String sipServerDataUrl = "http://" + "19.0.0.60" + ":8080/openapi/localuser/list?{\"syskey\":\"" + "123456" + "\"}";


    Callback callback;

    private volatile static TimeDo instance = null;
    private ScheduledExecutorService mScheduledExecutorService;
    private long time;

    private TimeDo() {
        mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }


    public void setListern( Callback callback){
        this.callback = callback;
    }

    @Override
    public void run() {

        SipHttpUtils sipHttpUtils = new SipHttpUtils(sipServerDataUrl, new SipHttpUtils.GetHttpData() {
            @Override
            public void httpData(String result) {
                callback.resultCallback(result);
            }
        });
        sipHttpUtils.start();
    }

    public static TimeDo getInstance() {
        if (instance == null) {
            synchronized (TimeDo.class) {
                if (instance == null) {
                    instance = new TimeDo();
                }
            }
        }
        return instance;
    }

    public void init(Context context, long time) {
        this.time = time;
    }

    public void start() {
        mScheduledExecutorService.scheduleWithFixedDelay(this, 0L, time, TimeUnit.MILLISECONDS);
    }


    public interface  Callback{
        public void resultCallback(String result);
    }
}