package com.quejue.mrvideo;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.seu.magicfilter.utils.MagicFilterType;

import net.ossrs.yasea.SrsCameraView;
import net.ossrs.yasea.SrsEncodeHandler;
import net.ossrs.yasea.SrsEncoder;
import net.ossrs.yasea.SrsFlvMuxer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SrsEncodeHandler.SrsEncodeListener {
    private static VidePlayer mVidePlayer;
    private SrsCameraView mSrsCameraView;
    private SrsFlvMuxer mFlvMuxer;
    private SrsEncoder mEncoder;
    private Button mStart;
    private Button mStop;

    public static Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    mVidePlayer.setPPs(SrsFlvMuxer.PPS, SrsFlvMuxer.SPS, 640, 360);
                    break;
                case 1:
                    byte[] data = (byte[]) msg.obj;
                    int pts = msg.arg1;
                    mVidePlayer.writeData(data, data.length, pts);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVidePlayer = findViewById(R.id.videoPlayer);
        mSrsCameraView = findViewById(R.id.glsurfaceview_camera);
        mStart = findViewById(R.id.start);
        mStop = findViewById(R.id.stop);

        mStart.setOnClickListener(this);
        mStop.setOnClickListener(this);

        init();
    }

    private void init() {
        mFlvMuxer = new SrsFlvMuxer();
        mEncoder = new SrsEncoder(new SrsEncodeHandler(this));
        mEncoder.setFlvMuxer(mFlvMuxer);
        mEncoder.setVideoHDMode();
        mSrsCameraView.setPreviewCallback(new SrsCameraView.PreviewCallback() {
            @Override
            public void onGetRgbaFrame(byte[] data, int width, int height) {
                mEncoder.onGetRgbaFrame(data, width, height);
            }
        });
    }

    private void start() {
        mSrsCameraView.setPreviewResolution(640, 360);
        mSrsCameraView.setFilter(MagicFilterType.NONE);
        mEncoder.start();
        boolean result = mSrsCameraView.startCamera();
        mSrsCameraView.enableEncoding();

        Toast.makeText(this, "startCamera:" + result, Toast.LENGTH_SHORT).show();
    }

    private void stop() {
        mSrsCameraView.stopCamera();
        mEncoder.stop();
        Toast.makeText(this, "stopCamera", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start:
                start();
                break;
            case R.id.stop:
                stop();
                break;
        }
    }

    @Override
    public void onNetworkWeak() {

    }

    @Override
    public void onNetworkResume() {

    }

    @Override
    public void onEncodeIllegalArgumentException(IllegalArgumentException e) {

    }
}
