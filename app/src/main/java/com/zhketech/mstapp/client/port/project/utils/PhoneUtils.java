package com.zhketech.mstapp.client.port.project.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.apache.http.conn.util.InetAddressUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Random;

/**
 * Created by Root on 2018/4/20.
 *
 */

public class PhoneUtils {
    Context context;

    public PhoneUtils(Context context) {
        this.context = context;
        throw new UnsupportedOperationException("cannot be constructed");
    }

    /**
     * 获取秒的时间戳（用于向服务发送时间）
     *
     * @return
     */
    public static long dateStamp() {
        long time = System.currentTimeMillis() / 1000;
        return time;
    }


    /**
     * 获取设备信息
     *
     * @param context
     * @param type
     * @return
     */
    @SuppressLint("MissingPermission")
    public static String getPhoneInfo(Context context, int type) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            switch (type) {
                case 1:// 设备唯一标识
                    return telephonyManager.getDeviceId();
                case 2:// 系统版本号
                    return android.os.Build.VERSION.RELEASE;
                case 3:// 设备型号
                    return android.os.Build.MODEL;
                case 4:// 应用程序版本号
                    return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
                default:
                    return "";
            }
        } catch (Exception e) {
            return "error" + e.getMessage();
        }
    }

    public static String getTimeType() {
        long time = Calendar.getInstance().getTimeInMillis();
        String str = String.valueOf(time);
        return str;
    }


    public static String intToIp(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }

    public static String getLocalIpAddress() {
        try {
            String ipv4;
            ArrayList<NetworkInterface> nilist = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface ni : nilist) {
                ArrayList<InetAddress> ialist = Collections.list(ni.getInetAddresses());
                for (InetAddress address : ialist) {
                    if (!address.isLoopbackAddress() && InetAddressUtils.isIPv4Address(ipv4 = address.getHostAddress())) {
                        return ipv4;
                    }
                }

            }

        } catch (SocketException ex) {
            Log.e("localip", ex.toString());
        }
        return null;
    }


    /**
     * 跳转到本页面的权限设置页面
     *
     * @param context
     */
    public static void goToSetting(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", "com.zhketech.intercomclient", null);
        intent.setData(uri);
        context.startActivity(intent);
    }


    /**
     * 获取设备的ip
     *
     * @return ip
     */
    public static String displayIpAddress(Context context) {

        String ip = "";
        ConnectivityManager conMann = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobileNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mobileNetworkInfo.isConnected()) {
            ip = getLocalIpAddress();
            System.out.println("本地ip-----" + ip);

            return ip;
        } else if (wifiNetworkInfo.isConnected()) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            ip = intToIp(ipAddress);
            System.out.println("wifi_ip地址为------" + ip);
            return ip;
        }
        return  "";
    }

    //屏幕常亮或熄灭
    public static void keepScreenOn(Context context, boolean on) {
        PowerManager.WakeLock wakeLock = null;
        if (on) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE,"my tag");
            wakeLock.acquire();
        } else {
            if (wakeLock != null) {
                wakeLock.release();
                wakeLock = null;
            }
        }
    }

    public static String getString(int length) {
        char[] charArray = new char[length];
        for (int i = 0; i < length; i++) {
            Random r = new Random();
            int n = r.nextInt(123);
            while (n < 48 || (n > 57 && n < 65) || (n > 90 && n < 97)|| n > 122) {
                n = r.nextInt(123);
            }
            charArray[i] = (char) n;
        }
        return String.valueOf(charArray);
    }

}
