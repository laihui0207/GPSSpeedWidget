package com.huivip.gpsspeedwidget.lyric;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.huivip.gpsspeedwidget.beans.LrcBean;
import com.huivip.gpsspeedwidget.beans.LyricContentEvent;
import com.huivip.gpsspeedwidget.beans.MusicAlbumUpdateEvent;
import com.huivip.gpsspeedwidget.beans.MusicEvent;
import com.huivip.gpsspeedwidget.service.LyricFloatingService;
import com.huivip.gpsspeedwidget.service.LyricWidgetService;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.FileUtil;
import com.huivip.gpsspeedwidget.utils.LrcUtil;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

public class LyricService extends Service {
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
        EventBus.getDefault().register(this);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if(EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null && intent.getBooleanExtra(EXTRA_CLOSE,false)){
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public Long getDuration() {
        return duration;
    }

    @Subscribe
    public void updateSong(MusicEvent event){
        searchLyric(event.getSongName(),event.getArtistName());
        currentPosition=event.getCurrentPostion();
    }
    private void searchLyric(String inputSongName,String inputArtist){
        new Thread(new Runnable() {
            @Override
            public void run() {
                long startedTime=System.currentTimeMillis();
                lyricContent = FileUtil.loadLyric(inputSongName, inputArtist);
                String cover=FileUtil.loadAlbum(inputSongName,inputArtist);
                if(cover!=null){
                    MusicAlbumUpdateEvent event=new MusicAlbumUpdateEvent();
                    event.setPicUrl(cover);
                    EventBus.getDefault().post(event);
                }
                if (TextUtils.isEmpty(lyricContent) || cover==null) {
                    MusicEvent res= WangYiYunMusic.downloadLyric(inputSongName, inputArtist);
                    lyricContent=res.getLyricContent();
                    if(res.getMusicCover()!=null && cover==null){
                        MusicAlbumUpdateEvent event=new MusicAlbumUpdateEvent();
                        event.setPicUrl(res.getMusicCover());
                        EventBus.getDefault().post(event);
                        FileUtil.saveAlbum(getApplicationContext(),inputSongName,inputArtist,res.getMusicCover());
                   }
                }

                currentPosition+=System.currentTimeMillis()-startedTime;
                songName = inputSongName;
                artistName = inputArtist;
                Message msg = new Message();
                mHandler.sendMessage(msg);

            }
        }).start();
    }
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(!AppSettings.get().isLyricEnable()){
                return;
            }
            if(!TextUtils.isEmpty(lyricContent)) {
                List<LrcBean> list= LrcUtil.parseStr2List(lyricContent);
                if(list!=null && list.size()>0) {
                    FileUtil.saveLyric(getApplicationContext(), songName, artistName, lyricContent);
                    launchLrcFloatingWindows(true);
                } else {
                    launchLrcFloatingWindows(false);
                    FileUtil.deleteLyric(getApplicationContext(),songName,artistName);
                }
            } else {
                launchLrcFloatingWindows(false);
            }
            super.handleMessage(msg);
        }
    };
    private void launchLrcFloatingWindows(boolean start){
         if(AppSettings.get().isLyricFloattingWidownEnable()) {
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
         }
        if (PrefUtils.isLyricWidgetEnable(getApplicationContext())
                && !Utils.isServiceRunning(getApplicationContext(), LyricWidgetService.class.getName())) {
            Intent widgetService = new Intent(getApplicationContext(), LyricWidgetService.class);
            startService(widgetService);
            EventBus.getDefault().post(new LyricContentEvent(lyricContent,currentPosition));
        }
    }
}
