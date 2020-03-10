package com.huivip.gpsspeedwidget.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.huivip.gpsspeedwidget.AppObject;
import com.huivip.gpsspeedwidget.DeviceUuidFactory;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.beans.AutoCheckUpdateEvent;
import com.huivip.gpsspeedwidget.beans.AutoMapStatusUpdateEvent;
import com.huivip.gpsspeedwidget.beans.LaunchEvent;
import com.huivip.gpsspeedwidget.listener.AutoLaunchSystemConfigReceiver;
import com.huivip.gpsspeedwidget.listener.AutoMapBoardReceiver;
import com.huivip.gpsspeedwidget.listener.GoToHomeReceiver;
import com.huivip.gpsspeedwidget.listener.MediaNotificationReceiver;
import com.huivip.gpsspeedwidget.listener.NetWorkConnectChangedReceiver;
import com.huivip.gpsspeedwidget.listener.SwitchReceiver;
import com.huivip.gpsspeedwidget.listener.WeatherServiceReceiver;
import com.huivip.gpsspeedwidget.speech.AudioService;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.CrashHandler;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.xutils.x;


public class BootStartService extends Service {
    public static String START_BOOT = "FromSTARTBOOT";
    boolean started = false;
    AlarmManager alarm;
    String deviceId;
    private static final String NOTIFICATION_CHANNEL_NAME = "BackgroundLocation";
    private NotificationManager notificationManager = null;
    boolean isCreateChannel = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    MediaPlayer mPlayer;

    @Override
    public void onCreate() {
        alarm = (AlarmManager) getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);
        CrashHandler.getInstance().init(getApplicationContext());
        DeviceUuidFactory deviceUuidFactory = new DeviceUuidFactory(getApplicationContext());
        deviceId = deviceUuidFactory.getDeviceUuid().toString();
        boolean start = AppSettings.get().getAutoStart();
        Log.d("huivip", "Auto Start: " + start);
        if (start) {
            startForeground(1, buildNotification());
            String apps = PrefUtils.getAutoLaunchApps(getApplicationContext());
            if (AppSettings.get().isEnableLaunchOtherApp() && !TextUtils.isEmpty(apps)) {
                String[] autoApps = apps.split(",");
                if (autoApps.length > 0) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int delayTime = AppSettings.get().getDelayTimeBetweenLaunchOtherApp();
                            for (String packageName : autoApps) {
                                try {
                                    Thread.sleep(delayTime * 1000 + 500L);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                Intent launchIntent = getApplicationContext().getPackageManager().getLaunchIntentForPackage(packageName);
                                if (launchIntent != null && !Utils.isServiceRunning(getApplicationContext(), packageName)) {
                                    launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    getApplicationContext().startActivity(launchIntent);//null pointer check in case package name was not found

                                }
                            }
                            if (AppSettings.get().isReturnHomeAfterLaunchOtherApp()) {
                                AlarmManager alarm = (AlarmManager) getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);
                                PendingIntent gotoHomeIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
                                        new Intent(getApplicationContext(), GoToHomeReceiver.class), 0);
                                alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000L, gotoHomeIntent);
                            }

                        }
                    }).start();
                } else if (AppSettings.get().isReturnHomeAfterLaunchOtherApp()) {
                    AlarmManager alarm = (AlarmManager) getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);
                    PendingIntent gotoHomeIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
                            new Intent(getApplicationContext(), GoToHomeReceiver.class), 0);
                    alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000L, gotoHomeIntent);
                }
            }
            PendingIntent autoLaunchIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
                    new Intent(getApplicationContext(), AutoLaunchSystemConfigReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
            alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000L, autoLaunchIntent);

            if (AppSettings.get().isPlayTime() || AppSettings.get().isPlayWeather() || AppSettings.get().isPlayAddressOnStop()) {
                Intent weatherService = new Intent(getApplicationContext(), WeatherService.class);
                Utils.startService(getApplicationContext(), weatherService);

                PendingIntent weatherServiceIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
                        new Intent(getApplicationContext(), WeatherServiceReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
                alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000L, weatherServiceIntent);
            }
            if (AppSettings.get().isEnablePlayWarnAudio()) {
                mPlayer = MediaPlayer.create(this, R.raw.warn);
                if (mPlayer != null) {
                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            if (mPlayer != null) {
                                mPlayer.reset();
                                mPlayer.release();
                            }
                            mPlayer = null;
                        }
                    });
                    mPlayer.start();
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                registerBoardCast(getApplicationContext());
            }
        }
        EventBus.getDefault().register(this);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean start = AppSettings.get().getAutoStart();
        if (intent != null) {
            if (start && !started) {
                started = true;
                //buildNotification();
                PrefUtils.setEnableTempAudioService(getApplicationContext(), true);
                if (AppSettings.get().isEnableTimeWindow()) {
                    if (!Utils.isServiceRunning(getApplicationContext(), RealTimeFloatingService.class.getName())) {
                        Intent timeFloating = new Intent(getApplicationContext(), RealTimeFloatingService.class);
                        Utils.startService(getApplicationContext(), timeFloating);
                    }
                }
                if (AppSettings.get().isEnableRoadLine()) {
                    if (!Utils.isServiceRunning(getApplicationContext(), RoadLineService.class.getName())) {
                        Intent roadLineService = new Intent(getApplicationContext(), RoadLineService.class);
                        Utils.startService(getApplicationContext(), roadLineService);
                    }
                    if (AppSettings.get().isEnableRoadLineFloatingWindow() &&
                            !Utils.isServiceRunning(getApplicationContext(), RoadLineFloatingService.class.getName())) {
                        Intent roadLineFloatingService = new Intent(getApplicationContext(), RoadLineFloatingService.class);
                        Utils.startService(getApplicationContext(), roadLineFloatingService);
                    }
                }
                if (AppSettings.get().isEnableSpeed()) {
                    Utils.startFloatingWindows(getApplicationContext(), true);
                }
                if (AppSettings.get().isEnableAudio() && !Utils.isServiceRunning(getApplicationContext(), AudioService.class.getName())) {
                    Intent audioService = new Intent(getApplicationContext(), AudioService.class);
                    Utils.startService(getApplicationContext(), audioService);
                }
                if (AppSettings.get().isEnableXunHang() && !Utils.isServiceRunning(getApplicationContext(), AutoXunHangService.class.getName())) {
                    Intent xunHangService = new Intent(getApplicationContext(), AutoXunHangService.class);
                    Utils.startService(getApplicationContext(), xunHangService);
                }
                if (Utils.isNetworkConnected(getApplicationContext())) {
                    if (AppSettings.get().isEnableTracker() && !Utils.isServiceRunning(getApplicationContext(), NaviTrackService.class.getName())) {
                        Intent trackService = new Intent(getApplicationContext(), NaviTrackService.class);
                        Utils.startService(getApplicationContext(), trackService);
                    }
                    if(AppSettings.get().isEnableRecord()){
                        Intent trackService = new Intent(getApplicationContext(), RecordGpsHistoryService.class);
                        Utils.startService(getApplicationContext(), trackService);
                    }
                    new Thread(() -> Utils.registerSelf(getApplicationContext())).start();
                    if(AppSettings.get().isAutoCheckUpdate()) {
                        x.task().postDelayed(() -> {
                            EventBus.getDefault().post(new AutoCheckUpdateEvent().setAutoCheck(true));
                        }, 1000 * 60);
                    }
                } else {
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                    NetWorkConnectChangedReceiver broadcastReceiver = new NetWorkConnectChangedReceiver();
                    getApplicationContext().registerReceiver(broadcastReceiver, intentFilter);
                }
            }
        }
       /* new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                stopSelf();
            }
        }, 1000 * 60 * 5);*/
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
    }

    private Notification buildNotification() {

        Notification.Builder builder = null;
        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            //Android O上对Notification进行了修改，如果设置的targetSDKVersion>=26建议使用此种方式创建通知栏
            if (null == notificationManager) {
                notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            }
            String channelId = getApplicationContext().getPackageName();
            if (!isCreateChannel) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId,
                        NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.enableLights(true);//是否在桌面icon右上角展示小圆点
                notificationChannel.setLightColor(Color.BLUE); //小圆点颜色
                notificationChannel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
                notificationManager.createNotificationChannel(notificationChannel);
                isCreateChannel = true;
            }
            builder = new Notification.Builder(getApplicationContext(), channelId);
        } else {
            builder = new Notification.Builder(getApplicationContext());
        }
        builder.setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("GPS速度插件")
                .setSmallIcon(R.drawable.ic_speedometer_notif)
                .setContentText("正在后台运行")
                .setWhen(System.currentTimeMillis());

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            notification = builder.build();
        } else {
            return builder.getNotification();
        }
        return notification;
    }
    @Subscribe
    public void launchService(LaunchEvent event){
        Intent mService=new Intent(AppObject.getContext(),event.getServiceClass());
        if(event.getDelaySeconds()>0){
            new Handler().postDelayed(()->{
                if(event.isToClose()){
                    AppObject.getContext().stopService(mService);
                } else {
                    AppObject.getContext().startService(mService);
                }
            },event.getDelaySeconds()*1000);
        } else {
            if(event.isToClose()){
                AppObject.getContext().stopService(mService);
            } else {
                AppObject.getContext().startService(mService);
            }
        }
    }
    @Subscribe
    public void launchAutoWidgetFloattingWindow(AutoMapStatusUpdateEvent event) {
        if (AppSettings.get().isShowAmapWidgetContent()) {
            if (event.isXunHangStarted()) {
                Intent autoWidgetFloatingService = new Intent(AppObject.getContext(), AutoWidgetFloatingService.class);
                AppObject.getContext().startService(autoWidgetFloatingService);
            }
        } else {
            Intent autoWidgetFloatingService = new Intent(AppObject.getContext(), AutoWidgetFloatingService.class);
            autoWidgetFloatingService.putExtra(AutoWidgetFloatingService.EXTRA_CLOSE, true);
            AppObject.getContext().startService(autoWidgetFloatingService);
        }
    }
    private void registerBoardCast(Context context) {
        String[] actions = new String[]{"com.android.music.metachanged",
                "com.android.music.statuschanged",
                "com.android.music.musicservicecommand",
                "com.android.music.updateprogress",
                "com.kugou.android.music.metachanged",
                "com.tencent.qqmusic.widgetupdate",
                "cn.flyaudio.media.action.TRACK_DETAILS",
                "cn.kuwo.kwmusicauto.action.PLAYER_STATUS",
                "cn.kuwo.kwmusicauto.action.OPEN_DESKLYRIC",
                "com.miui.player.metachanged",
                "com.android.music.playstatechanged",
                "com.android.music.playbackcomplete",
                "com.android.music.queuechanged",
                "fm.last.android.metachanged",
                "com.nullsoft.winamp.playstatechanged",
                "update.widget.update_proBar",
                "update.widget.playbtnstate",
                "com.ijidou.card.music",
                "com.ijidou.action.UPDATE_PROGRESS",
                "com.tencent.qqmusiccar.action.PLAY_COMMAND_SEND_FOR_THIRD"
        };
        IntentFilter intentFilter = new IntentFilter();
        for (String action : actions) {
            intentFilter.addAction(action);
        }
        context.registerReceiver(new MediaNotificationReceiver(), intentFilter);

        IntentFilter switchFiliter = new IntentFilter();
        switchFiliter.addAction(SwitchReceiver.SWITCH_EVENT);
        context.registerReceiver(new SwitchReceiver(), switchFiliter);

        IntentFilter autoMapFiliter = new IntentFilter();
        autoMapFiliter.addAction("AUTONAVI_STANDARD_BROADCAST_SEND");
        context.registerReceiver(new AutoMapBoardReceiver(), autoMapFiliter);
    }
}
