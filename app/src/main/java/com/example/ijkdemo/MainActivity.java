package com.example.ijkdemo;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ijkdemo.util.CommonUtil;
import com.example.ijkdemo.util.ScreenUtils;
import com.example.ijkdemo.util.StatusBarUtil;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    private View viewStatus;
    private RelativeLayout rlVideo;
    private VideoPlayer videoPlayerVideo;
    private CheckBox cbPlay, cbZoom;
    private TextView tvRunTime, tvTotalTime;
    private SeekBar seekBar;
    private boolean isPlayer = false; //标记判断视频是否准备完成
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                tvRunTime.setText(CommonUtil.formatTime((int) videoPlayerVideo.getCurrentPosition()));
                seekBar.setProgress((int) videoPlayerVideo.getCurrentPosition());
                handler.sendEmptyMessageDelayed(1, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setListener();
    }

    private void initView() {
        StatusBarUtil.setStatusBar(this);
        viewStatus = findViewById(R.id.view_main_status);
        StatusBarUtil.setStatusBarHight(this, viewStatus);
        rlVideo = findViewById(R.id.rl_main_video);
        rlVideo.getLayoutParams().height = ScreenUtils.getScreenHeight(this) / 3;
        rlVideo.getLayoutParams().width = ScreenUtils.getScreenWidth(this);
        cbPlay = findViewById(R.id.cb_main_play);
        cbZoom = findViewById(R.id.cb_main_zoom);
        tvRunTime = findViewById(R.id.tv_main_run_time);
        tvTotalTime = findViewById(R.id.tv_main_total_time);
        seekBar = findViewById(R.id.seekbar_progress);
        videoPlayerVideo = findViewById(R.id.video_player_main_video);
    }

    private void setListener() {
        cbPlay.setOnCheckedChangeListener(this);
        cbZoom.setOnCheckedChangeListener(this);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //videoPlayerVideo.seekTo((int) videoPlayerVideo.getCurrentPosition());

                if (fromUser) {
                    videoPlayerVideo.seekTo(progress);
                }
                Log.e("1233===","====progress==="+progress);
                Log.e("1233===","===getCurrentPosition===="+(int) videoPlayerVideo.getCurrentPosition());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 开始拖拽
                videoPlayerVideo.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 停止拖拽
                videoPlayerVideo.start();
            }
        });
        videoPlayerVideo.setVideoListener(new VideoListener() {
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
                videoPlayerVideo.start();
                //mp.start();
                isPlayer = true;
                handler.sendEmptyMessageDelayed(1, 1000);
                //设置时间
                tvTotalTime.setText(CommonUtil.formatTime((int) iMediaPlayer.getDuration()));
                //设置总进度
                seekBar.setMax((int) iMediaPlayer.getDuration());
            }

            @Override
            public void onSeekComplete(IMediaPlayer iMediaPlayer) {

            }

            @Override
            public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {

            }
        });
        videoPlayerVideo.setPath("http://vf2.mtime.cn/Video/2016/05/12/mp4/160512105140329960.mp4");
        try {
            videoPlayerVideo.load();
        } catch (IOException e) {
            Toast.makeText(this, "播放失败", Toast.LENGTH_SHORT);
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoPlayerVideo.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoPlayerVideo.pause();
    }


    private void showStatusBar(boolean enable) {
        if (enable) { //显示状态栏
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else { //隐藏状态栏
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(lp);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_main_play:
                if (isChecked && isPlayer) {
                    videoPlayerVideo.pause();
                } else {
                    videoPlayerVideo.start();
                    handler.sendEmptyMessageDelayed(1, 1000);
                }
                break;
            case R.id.cb_main_zoom:
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
            showStatusBar(false);
            viewStatus.setVisibility(View.GONE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                    , WindowManager.LayoutParams.FLAG_FULLSCREEN);
            rlVideo.getLayoutParams().height = ScreenUtils.getScreenHeight(this);
            rlVideo.getLayoutParams().width = ScreenUtils.getScreenWidth(this);
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            showStatusBar(true);
            viewStatus.setVisibility(View.VISIBLE);
            WindowManager.LayoutParams attrs = getWindow().getAttributes();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attrs);
            rlVideo.getLayoutParams().height = ScreenUtils.getScreenHeight(this) / 3;
            rlVideo.getLayoutParams().width = ScreenUtils.getScreenWidth(this);
        }
    }
}

