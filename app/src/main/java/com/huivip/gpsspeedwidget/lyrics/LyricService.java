package com.huivip.gpsspeedwidget.lyrics;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
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
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class LyricService extends Service {
    public static final String EXTRA_CLOSE = "com.huivip.gpsspeedwidget.EXTRA_CLOSE";
    public static String SONGNAME="lyric.songName";
    public static String ARTIST="lyric.artist";
    public static String DURATION="lyric.duration";
    public static String STATUS="lyric.status";
    public final String TAG="huivip";
    AudioManager am;
    Long duration;

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

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void searchLyric(MusicEvent musicEvent) {
        long startedTime = System.currentTimeMillis();
        String inputSongName=musicEvent.getSongName();
        String inputArtist=musicEvent.getArtistName();
        long currentPosition=musicEvent.getCurrentPostion();
        Bitmap musicCover=musicEvent.getCover();
        String cover = null;
        if (musicCover == null) {
            cover = FileUtil.loadAlbum(inputSongName, inputArtist);
            if (cover != null) {
                MusicAlbumUpdateEvent event = new MusicAlbumUpdateEvent();
                event.setSongName(inputSongName);
                event.setPicUrl(cover);
                EventBus.getDefault().post(event);
            }
        }
        String lyricContent = FileUtil.loadLyric(inputSongName, inputArtist);
        if (TextUtils.isEmpty(lyricContent) || (cover==null && musicCover==null)) {
            MusicEvent res = WangYiYunMusic.downloadLyric(inputSongName, inputArtist);
            lyricContent = res.getLyricContent();
            if (res.getMusicCover() != null && cover == null && musicCover == null) {
                new Thread(()->{
                    FileUtil.saveAlbum(inputSongName, inputArtist, res.getMusicCover());
                }).start();
            }
        }

       currentPosition += System.currentTimeMillis() - startedTime;
        Message msg = new Message();
        Bundle bundle=new Bundle();
        bundle.putString("songName",inputSongName);
        bundle.putString("artistName",inputArtist);
        bundle.putString("lyricContent",lyricContent);
        bundle.putLong("currentPosition",currentPosition);
        msg.setData(bundle);
        mHandler.sendMessage(msg);

    }
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(!AppSettings.get().isLyricEnable()){
                return;
            }
            Bundle data=msg.getData();
            String lyricContent=data.getString("lyricContent");
            String songName=data.getString("songName");
            String artistName=data.getString("artistName");
            Long currentPosition=data.getLong("currentPosition");
            if(!TextUtils.isEmpty(lyricContent)) {
                List<LrcBean> list= LrcUtil.parseStr2List(lyricContent);
                if(list!=null && list.size()>0) {
                    new Thread(()->{
                        FileUtil.saveLyric(songName, artistName, lyricContent,false);
                        EventBus.getDefault().post(new LyricContentEvent(songName, artistName, lyricContent, currentPosition));
                    }).start();
                    launchLrcFloatingWindows(songName,artistName,lyricContent,currentPosition);
                } else {
                    stopLyric(songName);
                    FileUtil.deleteLyric(getApplicationContext(),songName,artistName);
                }
            } else {
                stopLyric(songName);
            }
            super.handleMessage(msg);
        }
    };
    private void stopLyric(String songName) {
        if (AppSettings.get().isLyricFloattingWidownEnable()) {
            Intent lycFloatingService = new Intent(getApplicationContext(), LyricFloatingService.class);
            lycFloatingService.putExtra(LyricFloatingService.EXTRA_CLOSE,true);
            Utils.startService(getApplicationContext(),lycFloatingService, false);
        }
        EventBus.getDefault().post(new LyricContentEvent(songName));
    }

    private void launchLrcFloatingWindows(String songName, String artistName, String lyricContent, long currentPosition) {
        if (AppSettings.get().isLyricFloattingWidownEnable()) {
            Intent lycFloatingService = new Intent(getApplicationContext(), LyricFloatingService.class);
            lycFloatingService.putExtra(LyricFloatingService.SONGNAME, songName);
            lycFloatingService.putExtra(LyricFloatingService.ARTIST, artistName);
            if (currentPosition > 0) {
                lycFloatingService.putExtra(LyricFloatingService.POSITION, currentPosition);
            }
            lycFloatingService.putExtra(LyricFloatingService.LYRIC_CONTENT, lyricContent);
            lycFloatingService.putExtra(LyricFloatingService.DURATION, duration);
            Utils.startService(getApplicationContext(),lycFloatingService, false);
        }
        if (PrefUtils.isLyricWidgetEnable(getApplicationContext())) {
            if (!Utils.isServiceRunning(getApplicationContext(), LyricWidgetService.class.getName()))
            {
                Intent widgetService = new Intent(getApplicationContext(), LyricWidgetService.class);
                Utils.startService(getApplicationContext(),widgetService, false);
            }
            EventBus.getDefault().post(new LyricContentEvent(songName, artistName,lyricContent, currentPosition));
        }
    }
}
