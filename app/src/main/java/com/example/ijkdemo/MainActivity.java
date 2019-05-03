package com.example.ijkdemo;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ijkdemo.util.CommonUtil;
import com.example.ijkdemo.util.ScreenUtils;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;

public class MainActivity extends AppCompatActivity implements VideoListener, CompoundButton.OnCheckedChangeListener{
    private VideoPlayer videoPlayer;
    private CheckBox checkPlay, checkZoom;
    private TextView txtStartTime, txtStopTime;
    private SeekBar seekBar;
    //标记判断视频是否准备完成
    private boolean flag = false;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==1){
                txtStartTime.setText(CommonUtil.formatTime((int) videoPlayer.getCurrentPosition()));
                seekBar.setProgress((int) videoPlayer.getCurrentPosition());
                handler.sendEmptyMessageDelayed(1,1000);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoPlayer = findViewById(R.id.video);
        videoPlayer.setVideoListener(this);
        videoPlayer.setPath("http://vf2.mtime.cn/Video/2016/05/12/mp4/160512105140329960.mp4");
        try {
            videoPlayer.load();
        } catch (IOException e) {
            Toast.makeText(this,"播放失败",Toast.LENGTH_SHORT);
            e.printStackTrace();
        }
        initView();
        setListener();
    }

    private void initView() {
        checkPlay =  findViewById(R.id.checkbox_play);
        checkZoom =  findViewById(R.id.checkbox_zoom);
        txtStartTime =  findViewById(R.id.txt_starttime);
        txtStopTime = findViewById(R.id.txt_totletile);
        seekBar = findViewById(R.id.seekbar_progress);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
               // videoPlayer.seekTo((int) videoPlayer.getCurrentPosition());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 开始拖拽
                videoPlayer.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 停止拖拽
                videoPlayer.pause();
            }
        });

    }

    private void setListener() {
        checkPlay.setOnCheckedChangeListener(this);
        checkZoom.setOnCheckedChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoPlayer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoPlayer.pause();
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {

    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {

    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        videoPlayer.start();
        //mp.start();
        flag = true;
        //设置时间
        txtStopTime.setText(CommonUtil.formatTime((int) iMediaPlayer.getDuration()));
        //设置总进度
        seekBar.setMax((int) iMediaPlayer.getDuration());

    }

    @Override
    public void onSeekComplete(IMediaPlayer iMediaPlayer) {

    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {

    }
//============================
@Override
public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    switch (buttonView.getId()) {
        case R.id.checkbox_play:
            if (isChecked && flag) {
                videoPlayer.start();
                handler.sendEmptyMessageDelayed(1,1000);
            } else {
                videoPlayer.pause();
            }
            break;
        case R.id.checkbox_zoom:
            if (isChecked) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            }
            break;
    }
}

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                    , WindowManager.LayoutParams.FLAG_FULLSCREEN);
            Log.i(">>>width", ">>>" + ScreenUtils.getScreenWidth(this));
            Log.i(">>>height", ">>>" + ScreenUtils.getScreenHeight(this));
            videoPlayer.getLayoutParams().height = ScreenUtils.getScreenHeight(this);
            videoPlayer.getLayoutParams().width = ScreenUtils.getScreenWidth(this);


        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            WindowManager.LayoutParams attrs = getWindow().getAttributes();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attrs);

            videoPlayer.getLayoutParams().height = ScreenUtils.getScreenHeight(this) / 3;
            videoPlayer.getLayoutParams().width = ScreenUtils.getScreenWidth(this);
        }
    }
}

