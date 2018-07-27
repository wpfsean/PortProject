package com.zhketech.mstapp.client.port.project.rtsp.record;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.zhketech.mstapp.client.port.project.rtsp.media.VideoMediaCodec;


/**
 * Created by qingli on 2018/3/26
 * 摄像机数据编码线程
 */

public class CameraRecord extends Thread{
  private Context context;
  private VideoMediaCodec videoMediaCodec;

  public CameraRecord(Context context){
      this.context=context;
      videoMediaCodec = new VideoMediaCodec();
  }



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void run() {
      videoMediaCodec.prepare();
      videoMediaCodec.isRun(true);
      videoMediaCodec.getBuffers();
    }
}
