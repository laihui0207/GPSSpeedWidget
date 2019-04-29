package com.huivip.gpsspeedwidget.lyric;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.huivip.gpsspeedwidget.service.LyricFloatingService;
import com.huivip.gpsspeedwidget.widget.LyricWidgetService;
import com.huivip.gpsspeedwidget.beans.LrcBean;
import com.huivip.gpsspeedwidget.utils.FileUtil;
import com.huivip.gpsspeedwidget.utils.LrcUtil;
import com.huivip.gpsspeedwidget.utils.Utils;

import java.util.List;

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
                long startedTime=System.currentTimeMillis();
                lyricContent = FileUtil.loadLric(getApplicationContext(), inputSongName, inputArtist);
                if (TextUtils.isEmpty(lyricContent)) {
                    lyricContent = WangYiYunMusic.downloadLyric(inputSongName, inputArtist);
                }
                songName = inputSongName;
                artistName = inputArtist;
                currentPosition=System.currentTimeMillis()-startedTime;
                Message msg = new Message();
                mHandler.sendMessage(msg);

            }
        }).start();
    }
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(!TextUtils.isEmpty(lyricContent)) {
                List<LrcBean> list= LrcUtil.parseStr2List(lyricContent);
                if(list!=null && list.size()>0) {
                    FileUtil.saveLric(getApplicationContext(), songName, artistName, lyricContent);
                    launchLrcFloationgWindows(true);
                } else {
                    launchLrcFloationgWindows(false);
                    FileUtil.deleteLric(getApplicationContext(),songName,artistName);
                }
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
        if (!Utils.isServiceRunning(getApplicationContext(), LyricWidgetService.class.getName())) {
            Intent widgetService = new Intent(getApplicationContext(), LyricWidgetService.class);
           /* widgetService.putExtra(LyricWidgetService.DURATION, duration);
            widgetService.putExtra(LyricWidgetService.LYRIC_CONTENT, lyricContent);
            if (currentPosition > 0) {
                widgetService.putExtra(LyricWidgetService.POSITION, currentPosition);
            }*/
            startService(widgetService);
        }
        Intent intent2 = new Intent();
        intent2.setAction("com.huivip.widget.lyric.changed");
        intent2.putExtra(LyricWidgetService.LYRIC_CONTENT, lyricContent);
        intent2.putExtra(LyricWidgetService.POSITION,currentPosition);
        sendBroadcast(intent2);
    }
}
