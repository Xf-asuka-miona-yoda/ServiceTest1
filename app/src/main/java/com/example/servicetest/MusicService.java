package com.example.servicetest;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;

public class MusicService extends Service {



    private MusicBinder mbinder = new MusicBinder();
    class MusicBinder extends Binder{
        public MediaPlayer mediaPlayer = new MediaPlayer();
        public String musicurl = "";
        public void initmediaplayer(){
            try {
                File file = new File(Environment.getExternalStorageDirectory(),"起风了.mp3");
                musicurl = file.getPath();
                mediaPlayer.setDataSource(musicurl);
                mediaPlayer.prepare();
                mediaPlayer.setLooping(true);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        public void play(){
            if (!mediaPlayer.isPlaying()){
                mediaPlayer.start();
            }
        }

        public void pause(){
            if (mediaPlayer.isPlaying()){
                mediaPlayer.pause();
            }
        }

        public void stop(){
            if (mediaPlayer.isPlaying()){
                mediaPlayer.reset();
                initmediaplayer();
            }
        }
    }

    public MusicService() {
    }





    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mbinder;
    }
}
