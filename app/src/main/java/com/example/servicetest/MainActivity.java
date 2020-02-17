package com.example.servicetest;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView musicLength,musicCur;
    private SeekBar seekBar;
    private Timer timer;
    private boolean isSeekBarChanging;//互斥变量，防止进度条与定时器冲突。
    private int currentPosition;//当前音乐播放的进度
    SimpleDateFormat format;

    private MusicService.MusicBinder musicBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicBinder = (MusicService.MusicBinder) service;
            musicBinder.initmediaplayer();
            seekBar.setMax(musicBinder.mediaPlayer.getDuration());
            musicLength.setText(format.format(musicBinder.mediaPlayer.getDuration())+"");
            musicCur.setText("00:00");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        format = new SimpleDateFormat("mm:ss");

        Button play = (Button) findViewById(R.id.play);
        Button pause = (Button) findViewById(R.id.pause);
        Button stop = (Button) findViewById(R.id.stop);

        musicLength = (TextView) findViewById(R.id.music_length);
        musicCur = (TextView) findViewById(R.id.music_cur);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new MySeekBar());

        play.setOnClickListener(this);
        pause.setOnClickListener(this);
        stop.setOnClickListener(this);

        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
        bindService(intent, connection, BIND_AUTO_CREATE);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    musicBinder.initmediaplayer();
                } else {
                    Toast.makeText(this, "拒绝将导致程序无法使用", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    @Override
    public void onClick(View v) {
        if (musicBinder.musicurl == ""){ //如果当前播放器为空或者与要播放的音乐不一直，初始化播放器
            musicBinder.initmediaplayer();
            seekBar.setMax(musicBinder.mediaPlayer.getDuration());
            musicLength.setText(format.format(musicBinder.mediaPlayer.getDuration())+"");
            musicCur.setText("00:00");
        }
        switch (v.getId()){
            case R.id.play:
                musicBinder.play();
                //监听播放时回调函数
                timer = new Timer();
                timer.schedule(new TimerTask() {

                    Runnable updateUI = new Runnable() {
                        @Override
                        public void run() {
                            musicCur.setText(format.format(musicBinder.mediaPlayer.getCurrentPosition())+"");
                        }
                    };
                    @Override
                    public void run() {
                        if(!isSeekBarChanging){
                            seekBar.setProgress(musicBinder.mediaPlayer.getCurrentPosition());
                            runOnUiThread(updateUI);
                        }
                    }
                },0,50);
                break;
            case R.id.pause:
                musicBinder.pause();
                break;
            case R.id.stop:
                musicBinder.stop();
                break;
            default:
                break;
        }
    }
    //进度条事件处理
    public class MySeekBar implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        /*滚动时,应当暂停后台定时器*/
        public void onStartTrackingTouch(SeekBar seekBar) {
            isSeekBarChanging = true;
        }
        /*滑动结束后，重新设置值*/
        public void onStopTrackingTouch(SeekBar seekBar) {
            isSeekBarChanging = false;
            musicBinder.mediaPlayer.seekTo(seekBar.getProgress());
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}
