package com.zhketech.mstapp.client.port.project.callbacks;

import com.zhketech.mstapp.client.port.project.beans.AlarmBen;
import com.zhketech.mstapp.client.port.project.beans.VideoBen;
import com.zhketech.mstapp.client.port.project.utils.ByteUtils;
import com.zhketech.mstapp.client.port.project.utils.Logutils;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 回调接收服务器的报警报文(须去掉空的byte)
 *
 * Created by Root on 2018/4/20.
 */

public class ReceiverServerAlarm extends Thread {

    GetAlarmFromServerListern listern;

    public ReceiverServerAlarm(GetAlarmFromServerListern listern) {
        this.listern = listern;
    }

    @Override
    public void run() {
        super.run();


        try {
            ServerSocket serverSocket = new ServerSocket(2000, 3);
            InputStream in = null;
            while (true) {
                Socket socket = null;
                try {
                    socket = serverSocket.accept();
                    in = socket.getInputStream();
                    //falge 4
                    byte[] header = new byte[524];
                    int read = in.read(header);
                    byte[] flage = new byte[4];
                    for (int i = 0; i < 4; i++) {
                        flage[i] = header[i];
                    }
                    String falge1 = new String(flage, "gb2312");
                    //sender 32
                    byte[] sender = new byte[32];
                    for (int i = 0; i < 32; i++) {
                        sender[i] = header[i + 4];
                    }
                    int sender_position = ByteUtils.getPosiotion(sender);
                    String sender1 = new String(sender,0,sender_position, "gb2312");

                    //videoource

                    //videoFlage 4
                    byte[] videoFlage = new byte[4];
                    for (int i = 0; i < 4; i++) {
                        videoFlage[i] = header[i + 36];
                    }
                    String videoFlage1 = new String(videoFlage, "gb2312");

                    //videoId 48
                    byte[] videoId = new byte[48];
                    for (int i = 0; i < 48; i++) {
                        videoId[i] = header[i + 40];
                    }
                    int videoId_position = ByteUtils.getPosiotion(videoId);
                    String videoId1 = new String(videoId,0,videoId_position, "gb2312");

                    //name 128
                    byte[] videoName = new byte[128];
                    for (int i = 0; i < 128; i++) {
                        videoName[i] = header[i + 88];
                    }
                    int videoName_position = ByteUtils.getPosiotion(videoName);
                    String videoName1 = new String(videoName,0,videoName_position, "gb2312");

                    //DeviceType  16
                    byte[] videoDeviceType = new byte[16];
                    for (int i = 0; i < 16; i++) {
                        videoDeviceType[i] = header[i + 216];
                    }
                    int videoDeviceType_position = ByteUtils.getPosiotion(videoDeviceType);
                    String deviceType1 = new String(videoDeviceType,0,videoDeviceType_position, "gb2312");

                    //videoIPAddress  32
                    byte[] videoIPAddress = new byte[32];
                    for (int i = 0; i < 16; i++) {
                        videoIPAddress[i] = header[i + 232];
                    }
                    int videoIPAddress_position = ByteUtils.getPosiotion(videoIPAddress);
                    String videoIp1 = new String(videoIPAddress,0,videoIPAddress_position, "gb2312");

                    //port_icon 4
                    int sentryId = ByteUtils.bytesToInt(header, 264);
                    System.out.println(sentryId);

                    //Channel  128
                    byte[] videoChannel = new byte[128];
                    for (int i = 0; i < 16; i++) {
                        videoChannel[i] = header[i + 268];
                    }
                    int videoChannel_position = ByteUtils.getPosiotion(videoChannel);
                    String channel1 = new String(videoChannel,0,videoChannel_position, "gb2312");

                    //Username  32
                    byte[] videoUsername = new byte[32];
                    for (int i = 0; i < 32; i++) {
                        videoUsername[i] = header[i + 396];
                    }
                    int videoUsername_position = ByteUtils.getPosiotion(videoUsername);
                    String username1 = new String(videoUsername,0,videoUsername_position, "gb2312");

                    //Password  32
                    byte[] videoPassword = new byte[32];
                    for (int i = 0; i < 32; i++) {
                        videoPassword[i] = header[i + 428];
                    }
                    int videoPassword_position = ByteUtils.getPosiotion(videoPassword);
                    String videoPass1 = new String(videoPassword,0,videoPassword_position, "gb2312");

                    //alarmType 32
                    byte[] videoAlarmType = new byte[32];
                    for (int i = 0; i < 32; i++) {
                        videoAlarmType[i] = header[i + 460];
                    }
                    int videoAlarmType_position = ByteUtils.getPosiotion(videoAlarmType);
                    String alarmType1 = new String(videoAlarmType,0,videoAlarmType_position, "gb2312");

                    VideoBen videoBen = new VideoBen(videoFlage1,videoId1,videoName1,deviceType1,videoIp1,sentryId+"",channel1,username1,videoPass1,"",false,"","");
                    AlarmBen alarmBen = new AlarmBen(sender1,videoBen,alarmType1,"");

                    if(listern != null){
                        listern.getListern(alarmBen,"success");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    listern.getListern(null,"fail");
                } finally {
                    in.close();
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logutils.e("接收报警socket异常:"+e.getMessage());
        }
    }


    public interface GetAlarmFromServerListern {
        public void getListern(AlarmBen alarmBen, String flage);
    }
}
