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
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.support.annotation.RequiresApi;
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
                mRemoteControllerPreference.get().setArtworkConfiguration(600, 600);
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

    }
    @Override
    public void onClientPlaybackStateUpdate(int state, long stateChangeTimeMs, long currentPosMs, float speed) {
        if (AppSettings.get().isLyricEnable()) {
            if (state== RemoteControlClient.PLAYSTATE_PLAYING &&  !Utils.isServiceRunning(getApplicationContext(), LyricService.class.getName())) {
                Intent lyricService = new Intent(getApplicationContext(), LyricService.class);
                Utils.startService(getApplicationContext(), lyricService);
            } else if(state == RemoteControlClient.PLAYSTATE_PAUSED || state== RemoteControlClient.PLAYSTATE_STOPPED){
                Intent lyricService = new Intent(getApplicationContext(), LyricService.class);
                lyricService.putExtra(LyricService.EXTRA_CLOSE,true);
                Utils.startService(getApplicationContext(), lyricService);
            }
        }
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
        Bitmap defaultCover = BitmapFactory.decodeResource(getResources(), R.drawable.fenmian);
        Bitmap bitmap = metadataEditor.getBitmap(RemoteController.MetadataEditor.BITMAP_KEY_ARTWORK, defaultCover);
        MusicEvent musicEvent=new MusicEvent(songName,artistName);
        musicEvent.setDuration(duration);
        musicEvent.setCover(bitmap);
        EventBus.getDefault().post(musicEvent);
    }
    public class RCBinder extends Binder {
        public MusicRemoteControllerService getService() {
            return MusicRemoteControllerService.this;
        }
    }
}
