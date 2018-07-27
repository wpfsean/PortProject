package com.zhketech.mstapp.client.port.project.rtsp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 报警监听系统,一直监听端口2000
 */
public class RtspReceiveService extends Service {
    ServerSocket serverSocket;

    public RtspReceiveService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            serverSocket = new ServerSocket(2000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        initThread();

        return START_STICKY;
    }

    private void initThread() {
        new WorkThread().start();
    }

    class WorkThread extends Thread {

        @Override
        public void run() {
            try {
                Log.d("views", "开始监听");

                Socket socket = serverSocket.accept();
                InputStream is = socket.getInputStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int len = 0;
                byte[] bytes = new byte[1024];
                while ((len = is.read(bytes)) > 0) {
                    bos.write(bytes, 0, len);
                    if (len < bytes.length) {
                        break;
                    }

                }
                Log.d("views", "读取导数据: " + new String(bos.toByteArray()));
                toParseResult(bos.toByteArray());
                bos.close();
                is.close();
                socket.close();

                initThread();


            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }
    //解析推送的数据
    private void toParseResult(byte[] bytes) {



    }


}
