package com.quejue.mrvideo;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by chuibai on 2017/3/10.<br />
 */

public class Decoder {

    public static final int TRY_AGAIN_LATER = -1;
    public static final int BUFFER_OK = 0;
    public static final int BUFFER_TOO_SMALL = 1;
    public static final int OUTPUT_UPDATE = 2;

    private final String MIME_TYPE = "video/avc";
    private MediaCodec mMediaCodec = null;
    private MediaFormat mMediaFormat;
    private long BUFFER_TIMEOUT = 10000;
    private MediaCodec.BufferInfo mBI;

    /**
     * 初始化编码器
     * @throws IOException 创建编码器失败会抛出异常
     */
    public void init() throws IOException {
        mMediaCodec = MediaCodec.createDecoderByType(MIME_TYPE);
        mBI = new MediaCodec.BufferInfo();
    }

    /**
     * 配置解码器
     * @param sps 用于配置的sps参数
     * @param pps 用于配置的pps参数
     * @param surface 用于解码显示的Surface
     */
    public void configure(byte[] sps, byte[] pps, int width, int height, Surface surface){
        mMediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
        mMediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(sps));
        mMediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(pps));
        mMediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, width * height);
        mMediaCodec.configure(mMediaFormat, surface, null, 0);
    }

    /**
     * 开启解码器，获取输入输出缓冲区
     */
    public void start(){
        mMediaCodec.start();
    }

    /**
     * 输入数据
     * @param data 输入的数据
     * @param len 数据有效长度
     * @param timestamp 时间戳
     * @return 成功则返回{@link #BUFFER_OK} 否则返回{@link #TRY_AGAIN_LATER}
     */
    public int input(byte[] data,int len,long timestamp){
        int index = mMediaCodec.dequeueInputBuffer(BUFFER_TIMEOUT);
        Log.e("...","dequeueInputBuffer : " + index);
        if(index >= 0){
            ByteBuffer[] mInputBuffers = mMediaCodec.getInputBuffers();
            ByteBuffer inputBuffer = mInputBuffers[index];
            inputBuffer.clear();
            inputBuffer.put(data, 0, len);
            mMediaCodec.queueInputBuffer(index, 0, len, timestamp, 0);
            Log.e("...","queueInputBuffer timestamp = " + timestamp);
        }else {
            return TRY_AGAIN_LATER;
        }
        return BUFFER_OK;
    }

    public int output(byte[] data){
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int index = mMediaCodec.dequeueOutputBuffer(bufferInfo, BUFFER_TIMEOUT);
        Log.d("Decoder", "dequeueOutputBuffer == " + index);
        if(index >= 0){
            ByteBuffer[] mOutputBuffers = mMediaCodec.getOutputBuffers();
            if (mOutputBuffers[index] != null) {
                mOutputBuffers[index].position(mBI.offset);
                mOutputBuffers[index].limit(mBI.offset + mBI.size);

                if (data != null)
                    mOutputBuffers[index].get(data, 0, mBI.size);
            }
            mMediaCodec.releaseOutputBuffer(index, true);
        } else{
            return TRY_AGAIN_LATER;
        }
        return BUFFER_OK;
    }

    public void flish(){
        mMediaCodec.flush();
    }

    public void release() {
        mMediaCodec.stop();
        mMediaCodec.release();
        mMediaCodec = null;
    }
}
