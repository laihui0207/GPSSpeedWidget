package com.huivip.gpsspeedwidget.lyric;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
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
import com.huivip.gpsspeedwidget.beans.LrcBean;
import com.huivip.gpsspeedwidget.utils.FileUtil;
import com.huivip.gpsspeedwidget.utils.LrcUtil;
import com.huivip.gpsspeedwidget.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class LyricService extends NotificationListenerService implements RemoteController.OnClientUpdateListener  {
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
    private RCBinder mBinder = new RCBinder();
    private static WeakReference<RemoteController> mRemoteController = new WeakReference<>(null);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        am= (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mRemoteController = new WeakReference<>(new RemoteController(this, this));
        boolean registered;
        try {
            registered = am.registerRemoteController(mRemoteController.get());
        } catch (NullPointerException | SecurityException e) {
            registered = false;
        }
        if (registered) {
            try {
                mRemoteController.get().setArtworkConfiguration(100, 100);
                mRemoteController.get().setSynchronizationMode(RemoteController.POSITION_SYNCHRONIZATION_CHECK);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        this.mBinder = null;
        if (mRemoteController != null && mRemoteController.get() != null)
            am.unregisterRemoteController(mRemoteController.get());
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null){
            String inputSongName=intent.getStringExtra(SONGNAME);
            String inputArtist=intent.getStringExtra(ARTIST);
            boolean isPlaying=intent.getBooleanExtra(STATUS,false);
            if(!am.isMusicActive()){
                stopSelf();
            }
            currentPosition=0;
            if(!TextUtils.isEmpty(inputSongName) && (songName==null || !inputSongName.equalsIgnoreCase(songName) || am.isMusicActive())){
                lyricContent=null;
               searchLyric(inputSongName,inputArtist);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public RemoteController getmRemoteController(){
        return mRemoteController.get();
    }
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.e(TAG, "onNotificationPosted...");
    }
    public boolean sendMusicKeyEvent(int keyCode) {
        Log.d("huvip","lyric service Send key:"+keyCode);
        if (mRemoteController != null) {
            KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
            boolean down = mRemoteController.get().sendMediaKeyEvent(keyEvent);
            keyEvent = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
            boolean up = mRemoteController.get().sendMediaKeyEvent(keyEvent);
            return down && up;
        }
        return false;
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

    @Override

    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.e(TAG, "onNotificationRemoved...");
    }
    @Override
    public void onClientChange(boolean clearing) {

    }

    @Override
    public void onClientPlaybackStateUpdate(int state) {
        Log.d("huivip","Status:"+state);
    }

    @Override
    public void onClientPlaybackStateUpdate(int state, long stateChangeTimeMs, long currentPosMs, float speed) {
        /*                onPlaybackStateUpdate(state);*/
        Log.d("huivip","Status:"+state);
        Log.d("huivip","stateChangeTimes:"+stateChangeTimeMs);
        Log.d("huivip","CurrentPost:"+currentPosMs);
        Log.d("huivip","Speed:"+speed);
        if(state==RemoteControlClient.PLAYSTATE_PAUSED){
            launchLrcFloationgWindows(false);
        } else if(state== RemoteControlClient.PLAYSTATE_PLAYING){
            currentPosition=currentPosMs;
            searchLyric(songName,artistName);
        }
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
        Bitmap defaultCover = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_compass);
        Bitmap bitmap = metadataEditor.getBitmap(RemoteController.MetadataEditor.BITMAP_KEY_ARTWORK, defaultCover);
        searchLyric(songName,artistName);
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
                currentPosition=System.currentTimeMillis()-startedTime;
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
    public class RCBinder extends Binder {
        public LyricService getService() {
            return LyricService.this;
        }
    }
}
