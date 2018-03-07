package com.quejue.mrvideo;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by chuan.shen on 2018/2/5.
 */

public class VidePlayer extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private class PreviewBufferInfo {
        public byte[] buffer;
        public int size;
        public long timestamp;
    }

    private Queue<PreviewBufferInfo> mDecodeBuffers_dirty;
    private Decoder mDecoder;
    private Thread thread;
    public VidePlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        mDecoder = new Decoder();
        try {
            mDecoder.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mDecodeBuffers_dirty = new LinkedList<>();
    }

    public void setPPs(byte[] pps, byte[] sps, int width, int height) {
        byte[] newPPs = new byte[pps.length + 4];
        newPPs[0] = 0;
        newPPs[1] = 0;
        newPPs[2] = 0;
        newPPs[3] = 1;
        System.arraycopy(pps, 0, newPPs, 4, pps.length);

        byte[] newSPs = new byte[sps.length + 4];
        newSPs[0] = 0;
        newSPs[1] = 0;
        newSPs[2] = 0;
        newSPs[3] = 1;
        System.arraycopy(sps, 0, newSPs, 4, sps.length);

        mDecoder.configure(newSPs, newPPs, width, height, getHolder().getSurface());
        mDecoder.start();
    }

    @Override
    public void run() {
        while (isActivated() || !Thread.currentThread().isInterrupted()) {
            int result = Decoder.BUFFER_OK;
            if (mDecodeBuffers_dirty.isEmpty()) {
                SystemClock.sleep(20);
                continue;
            }
            synchronized (mDecodeBuffers_dirty) {
                Iterator<PreviewBufferInfo> ite = mDecodeBuffers_dirty.iterator();
                while (ite.hasNext()) {
                    PreviewBufferInfo info = ite.next();
                    byte[] buff = new byte[info.size + 4];
                    buff[0] = 0;
                    buff[1] = 0;
                    buff[2] = 0;
                    buff[3] = 1;
                    System.arraycopy(info.buffer, 0, buff, 4, info.size);
                    result = mDecoder.input(buff, buff.length, info.timestamp);
                    if (result != Decoder.BUFFER_OK) {
                        break;        //the rest buffers shouldn't go into encoder, if the previous one get problem
                    } else {
                        ite.remove();
                    }
                }

                while (result == Decoder.BUFFER_OK) {
                    result = mDecoder.output(null);
                }
            }
        }
    }


    public void writeData(byte[] data, int datalen, int timestamp) {
        synchronized (mDecodeBuffers_dirty) {
            PreviewBufferInfo info = new PreviewBufferInfo();
            info.buffer = data;
            info.size = datalen;
            info.timestamp = timestamp;
            mDecodeBuffers_dirty.add(info);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mDecoder.release();
        thread.interrupt();
    }
}
