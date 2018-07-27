package com.zhketech.mstapp.client.port.project.rtsp.media;

import android.media.MediaCodec;


public abstract class MediaCodecBase {

    protected MediaCodec mEncoder;

    protected boolean isRun = false;

    public abstract void prepare();

    public abstract void release();


}
