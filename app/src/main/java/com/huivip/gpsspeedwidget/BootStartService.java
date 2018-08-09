package com.huivip.gpsspeedwidget;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import com.huivip.gpsspeedwidget.listener.*;
import com.huivip.gpsspeedwidget.lyric.MediaControllerCallback;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Set;

public class BootStartService extends Service {
    public static String START_BOOT="FromSTARTBOOT";
    boolean started=false;
    AlarmManager alarm;
    MediaSession session;
    MediaControllerCallback mediaControllerCallback=null;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        alarm = (AlarmManager) getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null && !started){
            boolean start = PrefUtils.isEnableAutoStart(getApplicationContext());
            if(start) {
                started=true;
                if(intent.getBooleanExtra(START_BOOT,false)) {
                    PendingIntent thirdIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), ThirdSoftLaunchReceiver.class), 0);
                    alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,  300L, thirdIntent);

                    PendingIntent autoLaunchIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), AutoLaunchSystemConfigReceiver.class), 0);
                    alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 5000L, autoLaunchIntent);

                    PendingIntent autoFtpBackupIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), AutoFTPBackupReceiver.class), 0);
                    alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 120000L, autoFtpBackupIntent);
                }

                PendingIntent weatcherServiceIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), WeatherServiceReceiver.class), 0);
                alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 60000L, weatcherServiceIntent);

                if(PrefUtils.isWidgetActived(getApplicationContext())) {
                    Intent service = new Intent(getApplicationContext(), GpsSpeedService.class);
                    service.putExtra(GpsSpeedService.EXTRA_AUTOBOOT, true);
                    startService(service);
                }

                PrefUtils.setEnableTempAudioService(getApplicationContext(), true);
                if(!PrefUtils.isEnableAccessibilityService(getApplicationContext())){
                    PrefUtils.setShowFlattingOn(getApplicationContext(),PrefUtils.SHOW_ALL);
                }
                if(PrefUtils.isEnableTimeFloationgWidow(getApplicationContext())){
                    if(!Utils.isServiceRunning(getApplicationContext(),RealTimeFloatingService.class.getName())){
                        Intent timeFloating=new Intent(getApplicationContext(),RealTimeFloatingService.class);
                        startService(timeFloating);
                    }
                }
                if (PrefUtils.getShowFlatingOn(getApplicationContext()).equalsIgnoreCase(PrefUtils.SHOW_ALL)) {
                    Utils.startFloationgWindows(getApplicationContext(),true);
                }
                if(!PrefUtils.isWidgetActived(getApplicationContext()) && !PrefUtils.isEnableFlatingWindow(getApplicationContext())){
                    GpsUtil.getInstance(getApplicationContext()).startLocationService();
                }
               /* if(mediaControllerCallback==null){
                    mediaControllerCallback=new MediaControllerCallback(null);
                }*/
               /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    MediaControllerCallback.registerFallbackControllerCallback(getApplicationContext(),mediaControllerCallback);
                }*//* else {*/
                   /* mediaControllerCallback = new MediaControllerCallback(this);
                    MediaSessionManager.OnActiveSessionsChangedListener sessionsChangedListener =
                            list -> mediaControllerCallback.registerActiveSessionCallback(ScrobblerService.this, list);
                    listener = new WeakReference<>(sessionsChangedListener);
                    ComponentName className = new ComponentName(getApplicationContext(), NotificationListenerService.class);
                    manager.addOnActiveSessionsChangedListener(sessionsChangedListener, className);
                    List<MediaController> controllers = manager.getActiveSessions(className);
                    mediaControllerCallback.registerActiveSessionCallback(ScrobblerService.this, controllers);*/
               /* }*/
              /* AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
               ComponentName mComponentName = new ComponentName(getPackageName(), MediaNotificationReceiver.class.getName());
                int result = audioManager
                        .requestAudioFocus(new MyOnAudioFocusChangeListener(),
                                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == result) {//到这一步，焦点已经请求成功了
                    if (android.os.Build.VERSION.SDK_INT >= 21) {
                        //注册媒体按键 API 21+（Android 5.0）
                        setMediaButtonEvent();
                    } else {
                        //注册媒体按键 API 21 以下， 通常的做法
                        audioManager.registerMediaButtonEventReceiver(mComponentName);
                    }
                }*/

                Set<String> desktopPackages=Utils.getDesktopPackageName(getApplicationContext());
                PrefUtils.setApps(getApplicationContext(),desktopPackages);
                PackageManager packageManager = getApplicationContext().getPackageManager();
                Intent intentLauncher = new Intent(Intent.ACTION_MAIN);
                intentLauncher.addCategory(Intent.CATEGORY_HOME);
                String selectDefaultLauncher=packageManager.resolveActivity(intentLauncher,PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
                String defaultLaunch = PrefUtils.getDefaultLanuchApp(getApplicationContext());
                Log.d("huivip","Default launch:"+defaultLaunch+",Select launcher:"+selectDefaultLauncher);
                if (!TextUtils.isEmpty(defaultLaunch) && "com.huivip.gpsspeedwidget".equalsIgnoreCase(selectDefaultLauncher)) {
                    if (!Utils.isServiceRunning(getApplicationContext(), defaultLaunch)) {
                        Log.d("huivip","Default launch No Running,then start default launch:"+defaultLaunch);
                        Intent launchIntent = getApplicationContext().getPackageManager().getLaunchIntentForPackage(defaultLaunch);
                        if (launchIntent != null) {
                            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getApplicationContext().startActivity(launchIntent);//null pointer check in case package name was not found
                        }
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)//注意申明 API 21
    private void setMediaButtonEvent() {
        session = new MediaSession(getApplicationContext(), "随便写一串 tag 就行");
        session.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                //这里处理播放器逻辑 播放
                updatePlaybackState(true);//播放暂停更新控制中心播放状态
            }

            @Override
            public void onPause() {
                super.onPause();
                //这里处理播放器逻辑 暂停
                updatePlaybackState(false);//播放暂停更新控制中心播放状态
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                //CMD NEXT 这里处理播放器逻辑 下一曲
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                //这里处理播放器逻辑 上一曲
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void updatePlaybackState(boolean isPlaying) {
        PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY
                        | PlaybackState.ACTION_PLAY_PAUSE
                        | PlaybackState.ACTION_PLAY_FROM_MEDIA_ID
                        | PlaybackState.ACTION_PAUSE
                        | PlaybackState.ACTION_SKIP_TO_NEXT
                        | PlaybackState.ACTION_SKIP_TO_PREVIOUS );
        if (isPlaying) {
            stateBuilder.setState(PlaybackState.STATE_PLAYING,
                    PlaybackState.PLAYBACK_POSITION_UNKNOWN,
                    SystemClock.elapsedRealtime());
            Log.d("huivip","updatePlayStates:Playing");
        } else {
            stateBuilder.setState(PlaybackState.STATE_PAUSED,
                    PlaybackState.PLAYBACK_POSITION_UNKNOWN,
                    SystemClock.elapsedRealtime());
            Log.d("huivip","updatePlayStates:Pause");
        }
        session.setPlaybackState(stateBuilder.build());
    }

    class MyOnAudioFocusChangeListener implements AudioManager.OnAudioFocusChangeListener {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch(focusChange) {

                case AudioManager.AUDIOFOCUS_GAIN:
                    // 重新获得焦点,  可做恢复播放，恢复后台音量的操作
                    Log.d("huivip","Get Audio Focus");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    // 永久丢失焦点除非重新主动获取，这种情况是被其他播放器抢去了焦点，  为避免与其他播放器混音，可将音乐暂停

                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    // 暂时丢失焦点，这种情况是被其他应用申请了短暂的焦点，可压低后台音量
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    // 短暂丢失焦点，这种情况是被其他应用申请了短暂的焦点希望其他声音能压低音量（或者关闭声音）凸显这个声音（比如短信提示音），
                    break;
            }
        }
    }

}
