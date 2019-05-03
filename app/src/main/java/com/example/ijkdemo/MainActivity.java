package com.example.ijkdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;

public class MainActivity extends AppCompatActivity implements VideoListener{
    private VideoPlayer videoPlayer;
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

    }

    @Override
    public void onSeekComplete(IMediaPlayer iMediaPlayer) {

    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {

    }
}

