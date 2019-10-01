package com.huivip.gpsspeedwidget.music;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.media.RemoteController;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.KeyEvent;

import com.huivip.gpsspeedwidget.beans.KuWoStatusEvent;
import com.huivip.gpsspeedwidget.beans.MusicEvent;
import com.huivip.gpsspeedwidget.lyric.LyricService;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.FileUtil;
import com.huivip.gpsspeedwidget.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;

import cn.kuwo.autosdk.api.KWAPI;
import cn.kuwo.autosdk.api.OnExitListener;
import cn.kuwo.autosdk.api.OnGetLyricsListener;
import cn.kuwo.autosdk.api.OnGetSongImgListener;
import cn.kuwo.autosdk.api.OnPlayerStatusListener;
import cn.kuwo.autosdk.api.PlayState;
import cn.kuwo.autosdk.api.PlayerStatus;
import cn.kuwo.base.bean.Music;

@SuppressLint({"NewApi", "OverrideAbstract"})
public class MusicRemoteControllerService extends NotificationListenerService implements RemoteController.OnClientUpdateListener  {
    private String TAG="huivip";
    String songName;
    String artistName;
    long currentPosition=0L;
    Long duration;
    //Bitmap coverBitmap;
    AudioManager am;
    KWAPI mKwapi=null;
    boolean kuWoStarted=false;
    OnPlayerStatusListener kwPlayStatusListener=new OnPlayerStatusListener() {
        @Override
        public void onPlayerStatus(PlayerStatus playerStatus, Music music) {
           if(playerStatus.name().equalsIgnoreCase(PlayerStatus.PLAYING.name())){
               songName=music.name;
               artistName=music.artist;
               currentPosition = mKwapi.getCurrentPos();
               mKwapi.setMusicImg(music, new OnGetSongImgListener() {
                   @Override
                   public void sendSyncNotice_HeadPicStart(Music music) {
                   }
                   @Override
                   public void sendSyncNotice_HeadPicFinished(Music music, Bitmap bitmap) {
                      // Bitmap coverBitmap=bitmap;
                       currentPosition=mKwapi.getCurrentPos()+2000;
                       launchLyricService(RemoteControlClient.PLAYSTATE_PLAYING,currentPosition);
                       MusicEvent musicEvent=new MusicEvent(music.name,music.artist);
                       musicEvent.setDuration(music.duration);
                       musicEvent.setCurrentPostion(mKwapi.getCurrentPos());
                       if(AppSettings.get().isShowKuwoAlbum()) {
                           if(bitmap!=null) {
                               musicEvent.setCover(bitmap);
                               new Thread(() -> {
                                   FileUtil.saveAblumImage(bitmap, songName, artistName);
                               }).start();
                           }
                       }
                       EventBus.getDefault().post(musicEvent);
                       // for kuwo have album bug

                   }
                   @Override
                   public void sendSyncNotice_HeadPicFailed(Music music) {
                   }
                   @Override
                   public void sendSyncNotice_HeadPicNone(Music music) {
                   }
               });
               mKwapi.getLyrics(music, new OnGetLyricsListener() {
                   @Override
                   public void sendSyncNotice_LyricsStart(Music music) {

                   }

                   @Override
                   public void sendSyncNotice_LyricsFinished(Music music, String lyricContent) {
                       new Thread(()->{
                           FileUtil.saveLyric(music.name,music.artist,lyricContent);
                       }).start();
                       //LyricContentEvent event=new LyricContentEvent(music.name,lyricContent,mKwapi.getCurrentPos());
                       //EventBus.getDefault().post(event);
                   }

                   @Override
                   public void sendSyncNotice_LyricsFailed(Music music) {

                   }

                   @Override
                   public void sendSyncNotice_LyricsNone(Music music) {

                   }
               });

           }
        }
    };
    private RCBinder mBinder = new RCBinder();
    private static WeakReference<RemoteController> mRemoteControllerPreference = new WeakReference<>(null);
    @Override
    public void onCreate() {
        registerRemoteController();
        EventBus.getDefault().register(this);
        //kuwoStatusUpdate(new KuWoStatusEvent(true));
        super.onCreate();
    }
    public void registerRemoteController(){
        am= (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mRemoteControllerPreference = new WeakReference<>(new RemoteController(this, this));
        boolean registered;
        try {
            registered = am.registerRemoteController(mRemoteControllerPreference.get());
        } catch (NullPointerException | SecurityException e) {
            registered = false;
        }
        if (registered) {
            try {
                mRemoteControllerPreference.get().setArtworkConfiguration(800, 800);
                mRemoteControllerPreference.get().setSynchronizationMode(RemoteController.POSITION_SYNCHRONIZATION_CHECK);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d("huivip","Get Notification post");
        super.onNotificationPosted(sbn);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void kuwoStatusUpdate(KuWoStatusEvent event){
        if(event.isStarted() && mKwapi==null){
            kuWoStarted=event.isStarted();
            mKwapi = KWAPI.getKWAPI();
            mKwapi.bindAutoSdkService(this);
            mKwapi.registerPlayerStatusListener(kwPlayStatusListener);
            mKwapi.registerExitListener(new OnExitListener() {
                @Override
                public void onExit() {
                    releaseKuWo();
                }
            });
        }
    }

    private void releaseKuWo() {
        if(mKwapi!=null) {
            mKwapi.unbindAutoSdkService(this);
            mKwapi.unBindKuWoApp();
            //mKwapi.unRegisterPlayerStatusListener(this);
            //mKwapi.unRegisterExitListener(this);
            mKwapi = null;
            kuWoStarted = false;
        }
    }
    @Override
    public void onDestroy() {
        this.mBinder = null;
        if (mRemoteControllerPreference != null && mRemoteControllerPreference.get() != null)
            am.unregisterRemoteController(mRemoteControllerPreference.get());
        EventBus.getDefault().unregister(this);
        releaseKuWo();
        super.onDestroy();
    }
    public void sendMusicKeyEvent(int keyCode) {
        if(mKwapi!=null){
            kwController(keyCode);
        } else {
           defaultController(keyCode);
        }
    }
    private void defaultController(int keyCode){
        if (mRemoteControllerPreference != null && mRemoteControllerPreference.get() != null) {
            KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
            boolean down = mRemoteControllerPreference.get().sendMediaKeyEvent(keyEvent);
            keyEvent = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
            boolean up = mRemoteControllerPreference.get().sendMediaKeyEvent(keyEvent);
        } else {
            long eventTime = SystemClock.uptimeMillis();
            KeyEvent key = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0);
            dispatchMediaKeyToAudioService(key);
            dispatchMediaKeyToAudioService(KeyEvent.changeAction(key, KeyEvent.ACTION_UP));
        }
    }
    private void kwController(int keyCode){
        if(mKwapi==null) return;
        switch (keyCode){
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                mKwapi.setPlayState(PlayState.STATE_NEXT);
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                mKwapi.setPlayState(PlayState.STATE_PRE);
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                PlayerStatus status = mKwapi.getPlayerStatus();
                if(status.name().equalsIgnoreCase("playing")){
                    mKwapi.setPlayState(PlayState.STATE_PAUSE);
                } else {
                    mKwapi.setPlayState(PlayState.STATE_PLAY);
                }
        }
    }
    private void dispatchMediaKeyToAudioService(KeyEvent event) {
        if (am!= null) {
            try {
                am.dispatchMediaKeyEvent(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public boolean isPlaying(){
        return am.isMusicActive();
    }
    @Override
    public void onClientChange(boolean clearing) {

    }

    @Override
    public void onClientPlaybackStateUpdate(int state) {
    }
    @Override
    public void onClientPlaybackStateUpdate(int state, long stateChangeTimeMs, long currentPosMs, float speed) {
        Log.d("huivip","get update state:"+state+",postion:"+currentPosMs);
        launchLyricService(state,currentPosMs+1000);
        currentPosition=currentPosMs;
    }

    @Override
    public void onClientTransportControlUpdate(int transportControlFlags) {

    }

    @Override
    public void onClientMetadataUpdate(RemoteController.MetadataEditor metadataEditor) {
        artistName = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ARTIST, "null");
        String album = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ALBUM, "null");
        songName = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_TITLE, "null");
        duration = metadataEditor.getLong(MediaMetadataRetriever.METADATA_KEY_DURATION, -1);
        //Bitmap defaultCover = BitmapFactory.decodeResource(getResources(), R.drawable.fenmian);
       Bitmap coverBitmap = metadataEditor.getBitmap(RemoteController.MetadataEditor.BITMAP_KEY_ARTWORK, null);
        Log.d("huivip","get Song:"+songName+",artist:"+artistName);
        launchLyricService(RemoteControlClient.PLAYSTATE_PLAYING,1000);
        MusicEvent musicEvent=new MusicEvent(songName,artistName);
        musicEvent.setDuration(duration==null ? 0L:duration);
        musicEvent.setCover(coverBitmap);
        if(kuWoStarted && !AppSettings.get().isShowKuwoAlbum()) {
            musicEvent.setCover(null);
        }
        if (musicEvent.getCover() != null) {
            new Thread(() -> {
                FileUtil.saveAblumImage(musicEvent.getCover(), songName, artistName);
            }).start();
        }
        musicEvent.setCurrentPostion(1000);
        EventBus.getDefault().post(musicEvent);

    }
    private void launchLyricService(int state,long position){
        if (AppSettings.get().isLyricEnable()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (state== RemoteControlClient.PLAYSTATE_PLAYING) {
                        Intent lyricService = new Intent(getApplicationContext(), LyricService.class);
                        Utils.startService(getApplicationContext(), lyricService);
                        /*MusicEvent musicEvent=new MusicEvent(songName,artistName);
                        musicEvent.setCurrentPostion(position);
                        musicEvent.setDuration(duration==null ? 0: duration);
                        EventBus.getDefault().post(musicEvent);*/
                    } else if(state == RemoteControlClient.PLAYSTATE_PAUSED || state== RemoteControlClient.PLAYSTATE_STOPPED){
                        Intent lyricService = new Intent(getApplicationContext(), LyricService.class);
                        lyricService.putExtra(LyricService.EXTRA_CLOSE,true);
                        Utils.startService(getApplicationContext(), lyricService);
                    }
                }
            },1000);
        }
    }
    public class RCBinder extends Binder {
        public MusicRemoteControllerService getService() {
            return MusicRemoteControllerService.this;
        }
    }
}
