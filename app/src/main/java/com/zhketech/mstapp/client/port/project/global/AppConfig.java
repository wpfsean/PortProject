package com.zhketech.mstapp.client.port.project.global;

import com.zhketech.mstapp.client.port.project.base.App;
import com.zhketech.mstapp.client.port.project.pagers.MainActivity;
import com.zhketech.mstapp.client.port.project.utils.SharedPreferencesUtils;

/**
 * Created by Root on 2018/6/28.
 */

public class AppConfig {

    public AppConfig()

    {
        throw new UnsupportedOperationException("can not construct");
    }

    /**
     * 标识:
     * nativeIp  本机ip
     * sipName  sip名称
     * sipNum   sip号码
     * sipPwd   sip密码
     * sipServer sip服务器地址
     * name 当前设备的哨位信息
     */

    //true为主码流，false为子码流
    public static boolean isMainStream = false;


    public static int direction = 2;//(1竖屏，2横屏)

    //播放视频是否有声音
    public static boolean isVideoSound = false;

    //数据编码格式
    public static String dataFormat = "GB2312";
    //数据头
    public static String video_header_id = "ZKTH";

    //登录端口
    public static int server_port = 2010;
    //发送心跳的端口
    public static int heart_port = 2020;
    //服务器ip
    public static String server_ip = "19.0.0.28";
    //本机Ip
    public static String current_ip = (String) SharedPreferencesUtils.getObject(App.getInstance(), "nativeIp", "");
    public static String current_user = "admin";
    public static String current_pass = "pass";
    //sip服务器ip
    public static String native_sip_server_ip = (String) SharedPreferencesUtils.getObject(App.getInstance(), "sipServer", "");
    public static String native_sip_name = (String) SharedPreferencesUtils.getObject(App.getInstance(), "sipNum", "");
    //sip服务器管理员密码
    public static String sipServerPass = (String) SharedPreferencesUtils.getObject(App.getInstance(), "sipPwd", "");
    //sip服务器获取所有的sip用户信息
    public static String sipServerDataUrl = "http://" + "19.0.0.60" + ":8080/openapi/localuser/list?{\"syskey\":\"" + "123456" + "\"}";


    public static String data = "";

    //云台水平方向移动速率
    public static String x = "0.2";
    //云台垂直方向移动速率
    public static String y = "0.2";
    //云台的缩放速率
    public static String s = "0.2";

    //发送报文地ip和port
    public static String alarm_server_ip = "19.0.0.27";
    public static int alarm_server_port = 2000;


    //值班室信息
    public static final String DUTY_ROOM_URL = "http://wesk.top/zhketech/dutyRoomData.php";

    // public static final String DUTY_ROOM_URL = "http://19.0.0.28/zkth/dutyRoomData.php";

    /**
     * 蓝牙信息
     */
    public static String blueToothMac = "D0:33:8B:F6:1A:84";
    public static String serviceUuid = "0000FFF0-0000-1000-8000-00805F9B34FB";
    public static String charateristicUuid = "0000FFF6-0000-1000-8000-00805F9B34FB";

    //ftp://wang1210@wang1210.ftp-gz01.bcehost.com:8010/webroot/zhketech/dutyRoomData.php


    public static String sb = (String) SharedPreferencesUtils.getObject(App.getInstance(),"sb","");

}
