package com.zhketech.mstapp.client.port.project.rtsp.media;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Surface;

import com.zhketech.mstapp.client.port.project.pagers.SingleCallActivity;
import com.zhketech.mstapp.client.port.project.rtsp.record.Constant;
import com.zhketech.mstapp.client.port.project.utils.Logutils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;


public class VideoMediaCodec extends MediaCodecBase {

    private final static String TAG = "VideoMediaCodec";
    //视频录制的硬编码参数
    private static int queuesize = 10;
    public static ArrayBlockingQueue<h264data> h264Queue = new ArrayBlockingQueue<>(queuesize);
    public static ArrayBlockingQueue<byte[]> YUVQueue = new ArrayBlockingQueue<>(queuesize);

    private Surface mSurface;
    private long startTime = 0;
    private int TIMEOUT_USEC = 12000;
    public byte[] configbyte;

    private static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/zkth.h263";
    private BufferedOutputStream outputStream;
    FileOutputStream outStream;

    private void createfile() {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * **/
    public VideoMediaCodec() {
        createfile();
        Log.d("views", "path: " + path);
//        prepare();
    }

    public Surface getSurface() {
        return mSurface;
    }

    public void isRun(boolean isR) {
        this.isRun = isR;
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void prepare() {
        try {
            MediaFormat format = MediaFormat.createVideoFormat(Constant.MIME_TYPE, Constant.VIDEO_HEIGHT, Constant.VIDEO_WIDTH);

            //    format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            format.setInteger(MediaFormat.KEY_BIT_RATE, Constant.VIDEO_BITRATE);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, Constant.VIDEO_FRAMERATE);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, Constant.VIDEO_IFRAME_INTER);
//            format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);
//            format.setInteger(MediaFormat.KEY_COMPLEXITY, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
            mEncoder = MediaCodec.createEncoderByType(Constant.MIME_TYPE);
            try {
                mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                mEncoder.start();
            } catch (Exception e) {
                Logutils.e("error:" + e.getMessage());
            }
        } catch (IOException e) {

        }
    }

    @Override
    public void release() {
        this.isRun = false;

    }


    /**
     * 根据手机摄像头的数据获取h264编码的数据
     *
     * @param
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void getBuffers() {
        long pts = 0;
        long generateIndex = 0;
        byte[] input = new byte[0];
        try {
            while (isRun) {
                if (mEncoder == null)
                    break;
                if (YUVQueue.size() > 0) {
                    input = YUVQueue.poll();
                    byte[] yuv420sp = new byte[Constant.VIDEO_WIDTH * Constant.VIDEO_HEIGHT * 3 / 2];
                    NV21ToNV12(input, yuv420sp, Constant.VIDEO_WIDTH, Constant.VIDEO_HEIGHT);
                    input = yuv420sp;
                }

                ByteBuffer[] inputBuffers = mEncoder.getInputBuffers();
                ByteBuffer[] outputBuffers = mEncoder.getOutputBuffers();
                int inputBufferIndex = mEncoder.dequeueInputBuffer(-1);
                if (inputBufferIndex >= 0) {
                    pts = computePresentationTime(generateIndex);
//                    Log.d("views", "inputBufferIndex>000>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                    ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                    inputBuffer.clear();
                    inputBuffer.put(input);
                    mEncoder.queueInputBuffer(inputBufferIndex, 0, input.length, pts, 0);
                    generateIndex += 1;
                }
                MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
                int outputBufferIndex = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
                while (outputBufferIndex >= 0) {
//                    Log.d("views", "h264编码数据>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//                    ByteBuffer outputBuffer = mEncoder.getOutputBuffers()[outputBufferIndex];
                    ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                    byte[] outData = new byte[mBufferInfo.size];
                    outputBuffer.get(outData);
//                    Log.d("views", "mBufferInfo.flag :" + mBufferInfo.flags);
                    if (mBufferInfo.flags == 2) {
                        configbyte = new byte[mBufferInfo.size];
                        configbyte = outData;
                    } else if (mBufferInfo.flags == 1) {
                        byte[] keyframe = new byte[mBufferInfo.size + configbyte.length];
                        System.arraycopy(configbyte, 0, keyframe, 0, configbyte.length);
                        System.arraycopy(outData, 0, keyframe, configbyte.length, outData.length);

                        putData(keyframe, 1, mBufferInfo.presentationTimeUs * 1000L);

                    } else {
                        putData(outData, 2, mBufferInfo.presentationTimeUs * 1000L);
                    }
                    mEncoder.releaseOutputBuffer(outputBufferIndex, false);
                    outputBufferIndex = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
//                    Log.d("views","outputBufferIndex: "+outputBufferIndex);
                }
            }
        } catch (Exception e) {

        }
        try {
            mEncoder.stop();
            mEncoder.release();
            mEncoder = null;
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000 / 30;
    }


    /**
     * 对视频流数据YUV进行90度旋转
     *
     * @param data
     * @param imageWidth
     * @param imageHeight
     * @return
     */
    public static byte[] rotateYUVDegree90(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        //旋转Y
        int i = 0;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = imageHeight - 1; y >= 0; y--) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        // 旋转UV
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i--;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x - 1)];
                i--;
            }
        }
        return yuv;
    }

    /**
     * 对视频流YUV进行270度旋转
     *
     * @param data
     * @param imageWidth
     * @param imageHeight
     * @return
     */
    public static byte[] rotateYUV420Degree270(byte[] data, int imageWidth,
                                               int imageHeight) {

        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        int nWidth = 0, nHeight = 0;
        int wh = 0;
        int uvHeight = 0;
        if (imageWidth != nWidth || imageHeight != nHeight) {
            nWidth = imageWidth;
            nHeight = imageHeight;
            wh = imageWidth * imageHeight;
            uvHeight = imageHeight >> 1;
        }
        int k = 0;
        for (int i = 0; i < imageWidth; i++) {
            int nPos = 0;
            for (int j = 0; j < imageHeight; j++) {
                yuv[k] = data[nPos + i];
                k++;
                nPos += imageWidth;
            }
        }
        for (int i = 0; i < imageWidth; i += 2) {
            int nPos = wh;
            for (int j = 0; j < uvHeight; j++) {
                yuv[k] = data[nPos + i];
                yuv[k + 1] = data[nPos + i + 1];
                k += 2;
                nPos += imageWidth;
            }
        }
        return rotateYUV420Degree180(yuv, imageWidth, imageHeight);
    }

    /**
     * 对视频流YUV进行180度旋转
     *
     * @param data
     * @param imageWidth
     * @param imageHeight
     * @return
     */
    private static byte[] rotateYUV420Degree180(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        int i = 0;
        int count = 0;
        for (i = imageWidth * imageHeight - 1; i >= 0; i--) {
            yuv[count] = data[i];
            count++;
        }
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (i = imageWidth * imageHeight * 3 / 2 - 1; i >= imageWidth
                * imageHeight; i -= 2) {
            yuv[count++] = data[i - 1];
            yuv[count++] = data[i];
        }
        return yuv;
    }

    /**
     * 视频流数据默认为NV21类型,大部分支持,转为NV12 也就是通常的YUV420数据,然后进行h264的编码
     *
     * @param nv21
     * @param nv12
     * @param width
     * @param height
     */
    private void NV21ToNV12(byte[] nv21, byte[] nv12, int width, int height) {
        if (nv21 == null || nv12 == null) return;
        int framesize = width * height;
        int i = 0, j = 0;
        System.arraycopy(nv21, 0, nv12, 0, framesize);
        for (i = 0; i < framesize; i++) {
            nv12[i] = nv21[i];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j - 1] = nv21[j + framesize];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j] = nv21[j + framesize - 1];
        }
    }


    /**
     * put数据
     *
     * @param buffer
     * @param type
     * @param ts
     */
    public static void putData(byte[] buffer, int type, long ts) {
        if (h264Queue.size() >= queuesize) {
            h264Queue.poll();
        }
        h264data data = new h264data();
        data.data = buffer;
        data.type = type;
        data.ts = ts;
        h264Queue.add(data);
    }


    public static void putYUVData(byte[] buffer, int length) {
        if (YUVQueue.size() >= 10) {
            YUVQueue.poll();
        }
        YUVQueue.add(buffer);
    }
}
