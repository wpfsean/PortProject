package com.zhketech.mstapp.client.port.project.callbacks;

import com.zhketech.mstapp.client.port.project.beans.SipGroupBean;
import com.zhketech.mstapp.client.port.project.global.AppConfig;
import com.zhketech.mstapp.client.port.project.utils.ByteUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于请求sipgroup请求分组资源
 * <p>
 * Created by Root on 2018/5/25.
 */

public class SipGroupResourcesCallback implements Runnable {
    SipGroupDataCallback listern;
    List<SipGroupBean> mList = new ArrayList<>();

    public SipGroupResourcesCallback(SipGroupDataCallback listern) {
        this.listern = listern;
    }

    @Override
    public void run() {
        synchronized (this) {
            Socket socket = null;
            InputStream is = null;
            ByteArrayOutputStream bos = null;
            try {
                byte[] bys = new byte[140];
                byte[] zk = AppConfig.video_header_id.getBytes();
                for (int i = 0; i < zk.length; i++) {
                    bys[i] = zk[i];
                }
                // action 	 6 - 请求SIP组列表
                bys[4] = 6;
                bys[5] = 0;
                bys[6] = 0;
                bys[7] = 0;

                // 用户名密码
                String name = AppConfig.current_user+"/"+AppConfig.current_pass;
                byte[] na = name.getBytes(AppConfig.dataFormat);
                for (int i = 0; i < na.length; i++) {
                    bys[i + 8] = na[i];
                }
                socket = new Socket(AppConfig.server_ip, AppConfig.server_port);
                OutputStream os = socket.getOutputStream();
                os.write(bys);
                os.flush();
                is = socket.getInputStream();
                bos = new ByteArrayOutputStream();
                byte[] headers = new byte[56];
                int read = is.read(headers);

                byte[] flag = new byte[4];
                for (int i = 0; i < 4; i++) {
                    flag[i] = headers[i];
                }
                //数量
                byte[] count = new byte[4];
                for (int i = 0; i < 4; i++) {
                    count[i] = headers[i + 4];
                }
                int sipCounts = count[0];
                //SipGroupInfo   4+4+128+4 = 140
                int alldata = 140 * sipCounts;
                byte[] buffer = new byte[1024];
                int nIdx = 0;
                int nReadLen = 0;

                // 把数据写入bos
                while (nIdx < alldata) {
                    nReadLen = is.read(buffer);
                    bos.write(buffer, 0, nReadLen);
                    if (nReadLen > 0) {
                        nIdx += nReadLen;
                    } else {
                        break;
                    }
                }

                // 把总数据写入bos
                byte[] result = bos.toByteArray();
                int currentIndex = 0;
                List<byte[]> sipList = new ArrayList<>();
                for (int i = 0; i < sipCounts; i++) {
                    byte[] oneSip = new byte[140];
                    System.arraycopy(result, currentIndex, oneSip, 0, 140);
                    currentIndex += 140;
                    sipList.add(oneSip);
                }

                // 遍历数据
                for (byte[] vSip : sipList) {

                    // 标识头
                    byte[] sipFlageByte = new byte[4];
                    System.arraycopy(vSip, 0, sipFlageByte, 0, 4);
                    String sipFlageString = new String(sipFlageByte, AppConfig.dataFormat);

                    byte[] groupid = new byte[4];
                    System.arraycopy(vSip, 4, groupid, 0, 4);
                    int group_id = ByteUtils.bytesToInt(groupid, 0);


                    byte[] group_Name = new byte[128];
                    System.arraycopy(vSip, 8, group_Name, 0, 128);
                    int group_name_position = ByteUtils.getPosiotion(group_Name);
                    String sipgnameString = new String(group_Name,0,group_name_position, AppConfig.dataFormat);
//
                    byte[] sip_Count = new byte[4];
                    System.arraycopy(vSip, 136, sip_Count, 0, 4);
                    int sip_count = ByteUtils.bytesToInt(sip_Count, 0);

                    SipGroupBean sipGroupBean = new SipGroupBean(sipFlageString,group_id,sipgnameString,sip_count);
                    mList.add(sipGroupBean);
                }
                if (mList != null && mList.size() > 0){
                    if (listern != null){
                        listern.callbackSuccessData(mList);
                    }
                }
            } catch (Exception e) {
                if (listern != null) {
                    listern.callbackSuccessData(null);
                }
            }
        }
    }
    public void start(){
        new Thread(this).start();
    }

    public interface SipGroupDataCallback {
        void callbackSuccessData(List<SipGroupBean> mList);
    }
}
