package com.zhketech.mstapp.client.port.project.callbacks;

import android.content.Context;

import com.zhketech.mstapp.client.port.project.base.App;
import com.zhketech.mstapp.client.port.project.beans.VideoBen;
import com.zhketech.mstapp.client.port.project.global.AppConfig;
import com.zhketech.mstapp.client.port.project.utils.ByteUtils;
import com.zhketech.mstapp.client.port.project.utils.Logutils;
import com.zhketech.mstapp.client.port.project.utils.SharedPreferencesUtils;
import com.zhketech.mstapp.client.port.project.utils.WriteLogToFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * 登录后使用回调的方法获取 视频资源的list列表
 * Created by Root on 2018/4/5.
 */

public class RequestVideoSourcesThread extends Thread {
    List<VideoBen> videoSourceInfoList = new ArrayList<>();//所有返回视频源列表集合
    Socket socket = null;
    private InputStream is = null;//读取输入流
    private ByteArrayOutputStream bos = null;
    Context mContext;
    GetDataListener listener;


    public RequestVideoSourcesThread(Context mContext, GetDataListener listener) {
        this.mContext = mContext;
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            //数据头
            byte[] bys = new byte[140];
            byte[] zk = AppConfig.video_header_id.getBytes();
            for (int i = 0; i < zk.length; i++) {
                bys[i] = zk[i];
            }
            //action 1（获取资源列表）
            bys[4] = 1;
            bys[5] = 0;
            bys[6] = 0;
            bys[7] = 0;
            //用户名列表
            String name = AppConfig.current_user + "/" + AppConfig.current_pass + "/" + SharedPreferencesUtils.getObject(App.getInstance(),"nativeIp","");
            byte[] na = name.getBytes(AppConfig.dataFormat);
            for (int i = 0; i < na.length; i++) {
                bys[i + 8] = na[i];
            }
            //socket请求
            socket = new Socket(AppConfig.server_ip, AppConfig.server_port);
            OutputStream os = socket.getOutputStream();
            os.write(bys);
            os.flush();
            is = socket.getInputStream();
            bos = new ByteArrayOutputStream();
            //获取前8个byte
            byte[] headers = new byte[188];
            int read = is.read(headers);
            //获取前4个，获取头文件
            byte[] flag = new byte[4];
            for (int i = 0; i < 4; i++) {
                flag[i] = headers[i];
            }
            //获取后4个字节
            byte[] videos = new byte[4];
            for (int i = 0; i < 4; i++) {
                videos[i] = headers[i + 4];
            }
            int videoCount = videos[0];
            byte[] guid = new byte[48];
            for (int i = 0; i < 48; i++) {
                guid[i] = headers[i + 8];
            }
            int guidPosition = ByteUtils.getPosiotion(guid);
            String str_guid = new String(guid, 0, guidPosition, AppConfig.dataFormat);
//            if (!TextUtils.isEmpty(str_guid)) {
//                SharedPreferencesUtils.putObject(mContext, "device_guid", str_guid);
//            }

            byte[] deviceName = new byte[128];
            for (int i = 0; i < 128; i++) {
                deviceName[i] = headers[i + 8 + 48];
            }
            int deviceName_position = ByteUtils.getPosiotion(deviceName);
            String str_deviceName = new String(deviceName, 0, deviceName_position, AppConfig.dataFormat);
//            if (!TextUtils.isEmpty(str_deviceName)) {
//                SharedPreferencesUtils.putObject(mContext, "device_name", str_deviceName);
//            }
            // 总的数据长度（查文档，查看每个视频源的长度）
            int alldata = 424 * videoCount;
            byte[] buffer = new byte[1024];
            int nIdx = 0;
            int nReadLen = 0;
            while (nIdx < alldata) {
                nReadLen = is.read(buffer);
                bos.write(buffer, 0, nReadLen);
                if (nReadLen > 0) {
                    nIdx += nReadLen;
                } else {
                    break;
                }
            }
            // 总数据写入bos
            byte[] result = bos.toByteArray();
            //System.out.println(new String(result, 0, result.length));
            int currentIndex = 0;
            List<byte[]> videoList = new ArrayList<>();
            for (int i = 0; i < videoCount; i++) {
                byte[] oneVideo = new byte[424];
                System.arraycopy(result, currentIndex, oneVideo, 0, 424);
                currentIndex += 424;
                videoList.add(oneVideo);
            }

            // 解析每个视频数据
            for (byte[] vInfo : videoList) {
                byte[] videoFlag = new byte[4];
                System.arraycopy(vInfo, 0, videoFlag, 0, 4);
                String videoFlag1 = new String(videoFlag, AppConfig.dataFormat);
                byte[] id = new byte[48];
                System.arraycopy(vInfo, 4, id, 0, 48);
                String id1 = new String(id, AppConfig.dataFormat).trim();
                byte[] videoName = new byte[128];
                System.arraycopy(vInfo, 52, videoName, 0, 128);
                String videoName1 = new String(videoName, AppConfig.dataFormat).trim();

                byte[] deviceType = new byte[16];
                System.arraycopy(vInfo, 180, deviceType, 0, 16);
                String deviceType1 = new String(deviceType, AppConfig.dataFormat).trim();
                byte[] ipAddress = new byte[32];
                System.arraycopy(vInfo, 196, ipAddress, 0, 32);
                String ipAddress1 = new String(ipAddress, AppConfig.dataFormat).trim();
                byte[] port = new byte[4];
                System.arraycopy(vInfo, 228, port, 0, 4);
                String port1 = port[0] + "";
                byte[] channel = new byte[128];
                System.arraycopy(vInfo, 232, channel, 0, 128);
                String channel1 = new String(channel, AppConfig.dataFormat).trim();
                byte[] userName = new byte[32];
                System.arraycopy(vInfo, 360, userName, 0, 32);
                String userName1 = new String(userName, AppConfig.dataFormat).trim();
                byte[] passWord = new byte[32];
                System.arraycopy(vInfo, 392, passWord, 0, 32);
                String passWord1 = new String(passWord, AppConfig.dataFormat).trim();

                VideoBen videoBen = new VideoBen();
                videoBen.setFlage(videoFlag1);
                videoBen.setId(id1);
                videoBen.setName(videoName1);
                videoBen.setDevicetype(deviceType1);
                videoBen.setIp(ipAddress1);
                videoBen.setPort(port1);
                videoBen.setChannel(channel1);
                videoBen.setUsername(userName1);
                videoBen.setPassword(passWord1);
                videoBen.setRtsp("");
                videoBen.setPtz_url("");
                videoBen.setToken("");
                videoSourceInfoList.add(videoBen);
            }
            if (listener != null) {
                listener.getResult(videoSourceInfoList);
            }
        } catch (Exception e) {
            if (listener != null) {
                listener.getResult(null);
            }
            Logutils.e("Execption:" + "get videoSources execption");
            WriteLogToFile.info("getVideoResources error :" + e.getMessage());
        }
    }

    public interface GetDataListener {
        void getResult(List<VideoBen> devices);
    }

}
