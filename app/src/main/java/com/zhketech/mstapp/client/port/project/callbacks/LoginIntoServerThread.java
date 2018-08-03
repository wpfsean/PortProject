package com.zhketech.mstapp.client.port.project.callbacks;

import android.content.Context;

import com.zhketech.mstapp.client.port.project.beans.LoginParameters;
import com.zhketech.mstapp.client.port.project.global.AppConfig;
import com.zhketech.mstapp.client.port.project.utils.Logutils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 *  简易的方法实现纯种登录的方法
 *  注释：
 *  通过判断获取到的VideoResources的Count长度来断定是否登录成功
 *  Count > 0 成功
 *  Connt < 0 失败
 */
public class LoginIntoServerThread implements Runnable {
    IsLoginListern listern;
    Context mContext;
    LoginParameters loginBean;
    public LoginIntoServerThread(Context mContext, LoginParameters loginBean, IsLoginListern listern) {
        this.mContext = mContext;
        this.loginBean = loginBean;
        this.listern = listern;
    }
    @Override
    public void run() {
        Socket socket = null;
        InputStream is = null;//读取输入流
        try {
            byte[] bys = new byte[140];
            String fl = AppConfig.video_header_id;
            byte[] zk = fl.getBytes();
            for (int i = 0; i < zk.length; i++) {
                bys[i] = zk[i];
            }
            //action 1（获取资源列表------需查看文档，根据实际的要求写入Action----------）
            bys[4] = 1;
            bys[5] = 0;
            bys[6] = 0;
            bys[7] = 0;
            //用户名列表
            String name = loginBean.getUsername() + "/" + loginBean.getPass() + "/" + loginBean.getNative_ip();
            byte[] na = name.getBytes(AppConfig.dataFormat);
            for (int i = 0; i < na.length; i++) {
                bys[i + 8] = na[i];
            }
            //socket请求
            socket = new Socket(loginBean.getServer_ip(), AppConfig.server_port);
            OutputStream os = socket.getOutputStream();
            os.write(bys);
            os.flush();
            is = socket.getInputStream();
            //获取前8个byte
            byte[] headers = new byte[188];
            int read = is.read(headers);

            byte[] videos = new byte[4];
            for (int i = 0; i < 4; i++) {
                videos[i] = headers[i + 4];
            }
            int videoCount = videos[0];
            if (videoCount > 0) {
                if (listern != null) {
                    listern.loginStatus("success");
                }
            } else if (videoCount < 0) {
                if (listern != null) {
                    listern.loginStatus("fail");
                }
            }
        } catch (Exception e) {
            if (listern != null) {
                listern.loginStatus("fail" + e.getMessage());
            }
        }
    }
    //Run
    public void start() {
        new Thread(this).start();
    }

    //回调接口
    public interface IsLoginListern {
        void loginStatus(String status);
    }
}
