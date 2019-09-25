package com.huivip.gpsspeedwidget.music;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.media.RemoteController;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.KeyEvent;

import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.beans.MusicEvent;
import com.huivip.gpsspeedwidget.lyric.LyricService;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.Utils;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;

@SuppressLint({"OverrideAbstract", "NewApi"})
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MusicRemoteControllerService extends NotificationListenerService implements RemoteController.OnClientUpdateListener  {
    String songName;
    String artistName;
    long currentPosition=0L;
    Long duration;
    Bitmap coverBitmap;
    AudioManager am;
    private RCBinder mBinder = new RCBinder();
    private static WeakReference<RemoteController> mRemoteControllerPreference = new WeakReference<>(null);

    @Override
    public void onCreate() {
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
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        this.mBinder = null;
        if (mRemoteControllerPreference != null && mRemoteControllerPreference.get() != null)
            am.unregisterRemoteController(mRemoteControllerPreference.get());
        super.onDestroy();
    }
    public boolean sendMusicKeyEvent(int keyCode) {
        if (mRemoteControllerPreference != null) {
            KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
            boolean down = mRemoteControllerPreference.get().sendMediaKeyEvent(keyEvent);
            keyEvent = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
            boolean up = mRemoteControllerPreference.get().sendMediaKeyEvent(keyEvent);
            return down && up;
        }
        return false;
    }
    public boolean isPlaying(){
        return am.isMusicActive();
    }
    @Override
    public void onClientChange(boolean clearing) {

    }

    @Override
    public void onClientPlaybackStateUpdate(int state) {
        launchLyricService(state,0L);
    }
    @Override
    public void onClientPlaybackStateUpdate(int state, long stateChangeTimeMs, long currentPosMs, float speed) {
        Log.d("huivip","get update state:"+state+",postion:"+currentPosMs);
        launchLyricService(state,currentPosMs);
        currentPosition=currentPosMs;
    }

    @Override
    public void onClientTransportControlUpdate(int transportControlFlags) {

    }
    private void launchLyricService(int state,long position){
        if (AppSettings.get().isLyricEnable()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (state== RemoteControlClient.PLAYSTATE_PLAYING) {
                        Intent lyricService = new Intent(getApplicationContext(), LyricService.class);
                        Utils.startService(getApplicationContext(), lyricService);
                        MusicEvent musicEvent=new MusicEvent(songName,artistName);
                        Log.d("huivip","Launch lyric:"+songName+","+artistName);
                        musicEvent.setCurrentPostion(position);
                        musicEvent.setDuration(duration);
                        musicEvent.setCover(coverBitmap);
                        EventBus.getDefault().post(musicEvent);
                    } else if(state == RemoteControlClient.PLAYSTATE_PAUSED || state== RemoteControlClient.PLAYSTATE_STOPPED){
                        Intent lyricService = new Intent(getApplicationContext(), LyricService.class);
                        lyricService.putExtra(LyricService.EXTRA_CLOSE,true);
                        Utils.startService(getApplicationContext(), lyricService);
                    }
                }
            },1000);
        }
    }
    @Override
    public void onClientMetadataUpdate(RemoteController.MetadataEditor metadataEditor) {
        artistName = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ARTIST, "null");
        String album = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ALBUM, "null");
        songName = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_TITLE, "null");
        duration = metadataEditor.getLong(MediaMetadataRetriever.METADATA_KEY_DURATION, -1);
        Bitmap defaultCover = BitmapFactory.decodeResource(getResources(), R.drawable.fenmian);
        coverBitmap = metadataEditor.getBitmap(RemoteController.MetadataEditor.BITMAP_KEY_ARTWORK, defaultCover);
        launchLyricService(RemoteControlClient.PLAYSTATE_PLAYING,currentPosition);
        MusicEvent musicEvent=new MusicEvent(songName,artistName);
        musicEvent.setDuration(duration);
        musicEvent.setCover(coverBitmap);
        EventBus.getDefault().post(musicEvent);
    }
    public class RCBinder extends Binder {
        public MusicRemoteControllerService getService() {
            return MusicRemoteControllerService.this;
        }
    }
}
