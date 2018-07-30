package com.huivip.gpsspeedwidget.lyric;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.huivip.gpsspeedwidget.view.LrcView;

public class LyricService extends Service {
    public static String SONGNAME="lyric.songName";
    public static String ARTIST="lyric.artist";
    public static String POSITION="lyfic.position";
    public static String STATUS="lyric.status";
    LrcView lrcView;
    MockTimeThread mock;
    AudioManager am;
    String songName;
    String artistName;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        am= (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null){
            String inputSongName=intent.getStringExtra(SONGNAME);
            String intutArtist=intent.getStringExtra(ARTIST);
            if(inputSongName!=null && songName!=null && !inputSongName.equalsIgnoreCase(songName)){
                String lyrcContent=GecimeKu.downloadLyric(inputSongName,intutArtist);
                if(TextUtils.isEmpty(lyrcContent)) {
                    stopSelf();
                    return  super.onStartCommand(intent, flags, startId);
                }
                else {

                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
