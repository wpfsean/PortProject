package com.zhketech.mstapp.client.port.project.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhketech.mstapp.client.port.project.onvif.Device;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Root on 2018/7/2.
 */

public class GsonUtils {

    public static GsonUtils getInstance = null;
    public static Gson gson = null;

    private GsonUtils() {
    }

    public static GsonUtils getGsonInstace() {
        if (getInstance == null) {
            synchronized (GsonUtils.class) {
                getInstance = new GsonUtils();
                gson = new Gson();
            }
        }
        return getInstance;
    }

    //把存放Device的集合转成string字符串
    public static String list2String(List<Device> mlist) {
        String str = gson.toJson(mlist);
        if (TextUtils.isEmpty(str)) return "";
        return str;
    }

    //把字符串转成存放Device的集合
    public static List<Device> str2List(String str) {
        Type type = new TypeToken<List<Device>>() {
        }.getType();
        List<Device> alterSamples = new ArrayList<>();
        alterSamples = gson.fromJson(str, type);
        if (alterSamples != null) return alterSamples;
        return null;
    }


}
