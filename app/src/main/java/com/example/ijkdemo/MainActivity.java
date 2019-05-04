package com.example.ijkdemo;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.ijkdemo.util.CommonUtil;
import com.example.ijkdemo.util.ScreenUtils;
import com.example.ijkdemo.util.StatusBarUtil;

import java.io.IOException;
import java.util.Date;
import java.util.Random;

import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.ui.widget.DanmakuView;
import tv.danmaku.ijk.media.player.IMediaPlayer;

public class MainActivity extends AppCompatActivity
        implements CompoundButton.OnCheckedChangeListener , View.OnClickListener
{
    private boolean showDanmaku;//是否展示弹幕
    private VideoView videoView;
    private DanmakuView danmakuView;
    private DanmakuContext danmakuContext;
    long firstTime=0;//上一次发送弹幕的时间
    long secondTime;//这一次发送弹幕的时间
    boolean isSended=false;//判断是否发过弹幕  默认为没有发送过
    boolean isClosed=false;//判断弹幕框是否关闭,默认为开启状态
    private Button btnClose;//关闭弹幕的按钮

    //创建一个弹幕解析器
    private BaseDanmakuParser parser=new BaseDanmakuParser() {
        @Override
        protected IDanmakus parse() {
            return new Danmakus();
        }
    };
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
        initDan();
    }

    private void initDan() {
        //获取DanmakuView的实例
        danmakuView = (DanmakuView) findViewById(R.id.danmaku_view);
        //提升绘制效率
        danmakuView.enableDanmakuDrawingCache(true);
        //调用setCallback方法设置回调函数
        danmakuView.setCallback(new DrawHandler.Callback() {
            @Override
            public void prepared() {
                showDanmaku=true;
                //弹幕开始工作
                danmakuView.start();
                //当弹幕开始工作时,调用随机生成弹幕的方法,添加弹幕
                generateSomeDanmaku();

            }

            @Override
            public void updateTimer(DanmakuTimer timer) {

            }

            @Override
            public void drawingFinished() {

            }
        });

        //创建一个danmakuContext实例(用于对弹幕全局配置进行设定,包括字体,最大显示行数等)
        danmakuContext=DanmakuContext.create();
        //把解析器和danmakuContext传进去,使弹幕开始工作
        danmakuView.prepare(parser,danmakuContext);

        //发送弹幕的布局
        final LinearLayout operationLayout = (LinearLayout) findViewById(R.id.operation_layout);
        //发送按钮 点击发送弹幕
        Button send = (Button) findViewById(R.id.send);
        //输入框  输入弹幕内容
        final EditText editText = (EditText) findViewById(R.id.edit_text);

        //RelativeLayout的点击事件,点击显示发送弹幕的界面
        rlVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (operationLayout.getVisibility()==View.GONE){
                    operationLayout.setVisibility(View.VISIBLE);
                }else {
                    operationLayout.setVisibility(View.GONE);
                }
            }
        });

        //发送按钮的点击事件,点击可以发送弹幕
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content=editText.getText().toString();
                if (!TextUtils.isEmpty(content)) {
                    if (!isSended) {//还没有发送过弹幕
                        //调用添加弹幕的方法,添加一条弹幕(把第二个参数设置为true,用来和其他弹幕进行区分)
                        addDanmaku(content, true);
                        firstTime=(new Date()).getTime();
                        isSended = true;
                        editText.setText("");
                        Log.e("AAA",firstTime+"");

                    }else {//已经发送过弹幕了
                        secondTime=(new Date()).getTime();
                        Log.e("BBB", secondTime+"");
                        Log.e("CCC", secondTime-firstTime+"");
                        if (secondTime-firstTime<10000){//如果两次发送弹幕的间隔时间小于10s
                            Toast.makeText(MainActivity.this, "您说话太快了,请休息一会!", Toast.LENGTH_SHORT).show();
                        }else {
                            addDanmaku(content, true);
                            editText.setText("");
                            firstTime=secondTime;
                        }
                    }
                }
            }
        });

//        //由于系统输入法弹出的时候会导致焦点丢失，从而退出沉浸式模式，因此这里还对系统全局的UI变化进行了监听，保证程序一直可以处于沉浸式模式
//        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
//            @Override
//            public void onSystemUiVisibilityChange(int visibility) {
//                if (visibility==View.SYSTEM_UI_FLAG_VISIBLE){
//                    onWindowFocusChanged(true);
//                }
//            }
//        });

        //实现弹幕的开关功能
        btnClose = (Button) findViewById(R.id.btn_close);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isClosed){//如果弹幕是开启状态,点击一下关闭弹幕
                    isClosed=true;
                    btnClose.setText("开启弹幕");
                    danmakuView.setVisibility(View.GONE);
                }else {
                    isClosed=false;
                    btnClose.setText("关闭弹幕");
                    danmakuView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * 向弹幕View中 添加一条弹幕
     * 参数1:弹幕的内容
     * 参数2:弹幕是否有边框(如果是自己发送的弹幕,就为true,让弹幕加一个边框来和其他弹幕进行区分)
     */
    private void addDanmaku(String content,boolean withBorder){
        //创建一个BaseDanmaku实例,用于添加弹幕消息  TYPE_SCROLL_RL表示这是一条从右向左滚动的弹幕(LR,从左向右)
        BaseDanmaku danmaku=danmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        danmaku.text=content;//弹幕内容
        danmaku.padding=5;
        //这里要调用一个把sp转为px的方法,
        danmaku.textSize=sp2px(20);
        danmaku.textColor= Color.WHITE;
        danmaku.time=danmakuView.getCurrentTime();//显示时间
        danmaku.isLive=true;//是否是直播弹幕
        if (withBorder){//自己输入的弹幕带一个框,用来和别人的弹幕进行区分
            danmaku.borderColor=Color.GREEN;
            danmaku.textColor=Color.RED;
        }
        //把弹幕添加到danmakuView中去
        danmakuView.addDanmaku(danmaku);
    }

    private void initView() {
        StatusBarUtil.setStatusBar(this);
        viewStatus = findViewById(R.id.view_main_status);
        StatusBarUtil.setStatusBarHight(this, viewStatus);
        rlVideo = findViewById(R.id.rl_main_video);
        rlVideo.setOnClickListener(this);
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
                //若是滑动
                if (fromUser) {
                    videoPlayerVideo.seekTo(progress);
                }
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
        if (danmakuView!=null&&danmakuView.isPrepared()&&danmakuView.isPaused()){
            danmakuView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoPlayerVideo.pause();
        if (danmakuView!=null&&danmakuView.isPrepared()){
            danmakuView.pause();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        showDanmaku=false;
        if (danmakuView!=null){
            danmakuView.release();
            danmakuView=null;
        }
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
    //sp转px的方法。
    public int sp2px(float spValue) {
        final float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    //随机生成一些弹幕
    private void generateSomeDanmaku(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (showDanmaku){
                    int time=new Random().nextInt(300);
                    String content=""+time+time;
                    addDanmaku(content,false);
                    try {
                        Thread.sleep(time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    @Override
    public void onClick(View v) {

    }
}

