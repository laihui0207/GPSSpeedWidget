package com.huivip.gpsspeedwidget.lyric;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteController;
import android.os.*;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import com.huivip.gpsspeedwidget.LyricFloatingService;
import com.huivip.gpsspeedwidget.LyricWidgetService;
import com.huivip.gpsspeedwidget.utils.FileUtil;

import java.lang.ref.WeakReference;

public class LyricServiceLowVersion extends Service  {
    public static final String EXTRA_CLOSE = "com.huivip.gpsspeedwidget.EXTRA_CLOSE";
    public static String SONGNAME="lyric.songName";
    public static String ARTIST="lyric.artist";
    public static String DURATION="lyric.duration";
    public static String STATUS="lyric.status";
    public final String TAG="huivip";
    AudioManager am;
    String songName;
    String artistName;
    long currentPosition=0L;
    Long duration;
    String lyricContent;
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
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String inputSongName = intent.getStringExtra(SONGNAME);
            String inputArtist = intent.getStringExtra(ARTIST);
            boolean isPlaying = intent.getBooleanExtra(STATUS, false);
            if (!am.isMusicActive()) {
                stopSelf();
            }
            if (!TextUtils.isEmpty(inputSongName) && (songName == null || !inputSongName.equalsIgnoreCase(songName) || am.isMusicActive())) {
                lyricContent = null;
                searchLyric(inputSongName, inputArtist);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public String getSongName() {
        return songName;
    }

    public String getArtistName() {
        return artistName;
    }

    public long getCurrentPosition() {
        return currentPosition;
    }

    public Long getDuration() {
        return duration;
    }

    public String getLyricContent() {
        return lyricContent;
    }

    private void searchLyric(String inputSongName,String inputArtist){
        new Thread(new Runnable() {
            @Override
            public void run() {
                lyricContent = FileUtil.loadLric(getApplicationContext(), inputSongName, inputArtist);
                if (TextUtils.isEmpty(lyricContent)) {
                    lyricContent = WangYiYunMusic.downloadLyric(inputSongName, inputArtist);
                }
                songName = inputSongName;
                artistName = inputArtist;
                Message msg = new Message();
                mHandler.sendMessage(msg);

            }
        }).start();
    }
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(!TextUtils.isEmpty(lyricContent)) {
                FileUtil.saveLric(getApplicationContext(),songName,artistName,lyricContent);
                launchLrcFloationgWindows(true);
            } else {
                launchLrcFloationgWindows(false);
            }
            super.handleMessage(msg);
        }
    };
    private void launchLrcFloationgWindows(boolean start){
        Intent lycFloatingService = new Intent(getApplicationContext(), LyricFloatingService.class);
        lycFloatingService.putExtra(LyricFloatingService.SONGNAME, songName);
        lycFloatingService.putExtra(LyricFloatingService.ARTIST, artistName);
        if (currentPosition>0) {
            lycFloatingService.putExtra(LyricFloatingService.POSITION, currentPosition);
        }
        lycFloatingService.putExtra(LyricFloatingService.LYRIC_CONTENT,lyricContent);
        lycFloatingService.putExtra(LyricFloatingService.DURATION, duration);
         if(!start){
             lycFloatingService.putExtra(LyricFloatingService.EXTRA_CLOSE,true);
         }
         startService(lycFloatingService);

        Intent widgetService=new Intent(getApplicationContext(), LyricWidgetService.class);
        widgetService.putExtra(LyricWidgetService.DURATION,duration);
        widgetService.putExtra(LyricWidgetService.LYRIC_CONTENT,lyricContent);
        if(currentPosition>0){
            widgetService.putExtra(LyricWidgetService.POSITION,currentPosition);
        }
        if(!start){
            widgetService.putExtra(LyricWidgetService.EXTRA_CLOSE,true);
            stopService(widgetService);
        } else {
            startService(widgetService);
        }
    }
}