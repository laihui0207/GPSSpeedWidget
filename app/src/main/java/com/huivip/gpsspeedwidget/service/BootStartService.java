package com.huivip.gpsspeedwidget.service;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.huivip.gpsspeedwidget.AppObject;
import com.huivip.gpsspeedwidget.DeviceUuidFactory;
import com.huivip.gpsspeedwidget.GpsUtil;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.beans.AutoCheckUpdateEvent;
import com.huivip.gpsspeedwidget.beans.AutoMapStatusUpdateEvent;
import com.huivip.gpsspeedwidget.beans.BootEvent;
import com.huivip.gpsspeedwidget.beans.FloatWindowsLaunchEvent;
import com.huivip.gpsspeedwidget.beans.LaunchEvent;
import com.huivip.gpsspeedwidget.listener.AutoLaunchSystemConfigReceiver;
import com.huivip.gpsspeedwidget.listener.AutoMapBoardReceiver;
import com.huivip.gpsspeedwidget.listener.BootStartReceiver;
import com.huivip.gpsspeedwidget.listener.DateChangeReceiver;
import com.huivip.gpsspeedwidget.listener.GoToHomeReceiver;
import com.huivip.gpsspeedwidget.listener.MediaNotificationReceiver;
import com.huivip.gpsspeedwidget.listener.NetWorkConnectChangedReceiver;
import com.huivip.gpsspeedwidget.listener.ScreenOnReceiver;
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
    public static String START_RESUME = "FromHomeResume";
    boolean started = false;
    AlarmManager alarm;
    String deviceId;
    private static final String NOTIFICATION_CHANNEL_NAME = "BackgroundLocation";
    private NotificationManager notificationManager = null;
    boolean isCreateChannel = false;
    NetWorkConnectChangedReceiver netWorkConnectChangedReceiver;
    ScreenOnReceiver screenOnReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    MediaPlayer mPlayer;

    @Override
    public void onCreate() {
        CrashHandler.getInstance().init(getApplicationContext());
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int NOTIFICATION_ID = (int) (System.currentTimeMillis() % 10000);
            startForeground(NOTIFICATION_ID, Utils.buildNotification(getApplicationContext(),Integer.toString(NOTIFICATION_ID)));
        }
        DeviceUuidFactory deviceUuidFactory = new DeviceUuidFactory(getApplicationContext());
        deviceId = deviceUuidFactory.getDeviceUuid().toString();
        alarm = (AlarmManager) getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);
        boolean start = AppSettings.get().getAutoStart();
        if (start) {
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
           // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                registerBoardCast(getApplicationContext());
            //}
        }
        EventBus.getDefault().register(this);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        boolean start = AppSettings.get().getAutoStart();
        if (intent != null) {
            boolean boot_from_resume = intent.getBooleanExtra(START_RESUME, false);
            boolean boot_from_start = intent.getBooleanExtra(START_BOOT, false);
            if (start && (!started || boot_from_resume || boot_from_start)) {
                started = true;
                //Toast.makeText(getApplicationContext(), "Boot Start Service launched", Toast.LENGTH_SHORT).show();
                //buildNotification();
                if (netWorkConnectChangedReceiver == null) {
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                    netWorkConnectChangedReceiver = new NetWorkConnectChangedReceiver();
                    getApplicationContext().registerReceiver(netWorkConnectChangedReceiver, intentFilter);
                }
               /* if (screenOnReceiver == null) {
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(Intent.ACTION_SCREEN_ON);
                    screenOnReceiver = new ScreenOnReceiver();
                    getApplicationContext().registerReceiver(screenOnReceiver, intentFilter);
                }*/
                if(boot_from_start){
                    GpsUtil gpsUtil = GpsUtil.getInstance(getApplicationContext());
                    gpsUtil.resetData();

                }
                PrefUtils.setEnableTempAudioService(getApplicationContext(), true);
                if (AppSettings.get().isEnableTimeWindow()) {
                    Intent timeFloating = new Intent(getApplicationContext(), RealTimeFloatingService.class);
                    Utils.startService(getApplicationContext(), timeFloating);
                }
                if (AppSettings.get().isEnableRoadLine()) {
                    Intent roadLineService = new Intent(getApplicationContext(), RoadLineService.class);
                    Utils.startService(getApplicationContext(), roadLineService);
                    if (AppSettings.get().isEnableRoadLineFloatingWindow()) {
                        Intent roadLineFloatingService = new Intent(getApplicationContext(), RoadLineFloatingService.class);
                        Utils.startService(getApplicationContext(), roadLineFloatingService);
                    }
                }
                if (AppSettings.get().isEnableSpeed()) {
                    Utils.startFloatingWindows(getApplicationContext(), true);
                }
                if (AppSettings.get().isEnableAudio()) {
                    Intent audioService = new Intent(getApplicationContext(), AudioService.class);
                    Utils.startService(getApplicationContext(), audioService);
                }

                if (AppSettings.get().isEnableAltitudeWindow()) {
                    Intent altitudeWindowService = new Intent(getApplicationContext(), AltitudeFloatingService.class);
                    Utils.startService(getApplicationContext(), altitudeWindowService);
                }
                Intent weatherService = new Intent(getApplicationContext(), WeatherService.class);
                Utils.startService(getApplicationContext(), weatherService);

                if (AppSettings.get().isPlayTime() || AppSettings.get().isPlayWeather() || AppSettings.get().isPlayAddressOnStop()) {
                    PendingIntent weatherServiceIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
                            new Intent(getApplicationContext(), WeatherServiceReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
                    alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000L, weatherServiceIntent);
                }
                if (Utils.isNetworkConnected(getApplicationContext())) {
                    if (AppSettings.get().isEnableXunHang()){
                        Intent xunHangService = new Intent(getApplicationContext(), AutoXunHangService.class);
                        Utils.startService(getApplicationContext(), xunHangService);
                    }
                    if (AppSettings.get().isEnableTracker()){
                        Intent trackService = new Intent(getApplicationContext(), NaviTrackService.class);
                        Utils.startService(getApplicationContext(), trackService);
                    }
                    if (AppSettings.get().isEnableRecord()) {
                        Intent trackService = new Intent(getApplicationContext(), RecordGpsHistoryService.class);
                        Utils.startService(getApplicationContext(), trackService);
                    }
                    new Thread(() -> Utils.registerSelf(getApplicationContext())).start();
                    if (AppSettings.get().isAutoCheckUpdate()) {
                        x.task().postDelayed(() -> {
                            EventBus.getDefault().post(new AutoCheckUpdateEvent().setAutoCheck(true));
                        }, 1000 * 60);
                    }
                }
                if (PrefUtils.isSpeedNumberVWidgetEnable(getApplicationContext())) {
                    Intent widgetService = new Intent(getApplicationContext(), SpeedNumberVerticalService.class);
                    Utils.startService(getApplicationContext(), widgetService);
                }
                if (PrefUtils.isTimeHWidgetEnable(getApplicationContext())) {
                    Intent widgetService = new Intent(getApplicationContext(), TimeWidgetService.class);
                    Utils.startService(getApplicationContext(), widgetService);
                }
                if (PrefUtils.isTimeVWidgetEnable(getApplicationContext())){
                    Intent widgetService = new Intent(getApplicationContext(), TimeWidgetVerticalService.class);
                    Utils.startService(getApplicationContext(), widgetService);
                }
                if (PrefUtils.isSpeedMeterWidgetEnable(getApplicationContext())){
                    Intent bootService = new Intent(getApplicationContext(), GpsSpeedMeterService.class);
                    Utils.startService(getApplicationContext(), bootService);
                }
                if (PrefUtils.isMusicWidgetEnable(getApplicationContext())){
                    Intent widgetService = new Intent(getApplicationContext(), MusicControllerService.class);
                    Utils.startService(getApplicationContext(), widgetService);
                }
            }
            EventBus.getDefault().post(new BootEvent(true));
            x.task().postDelayed(this::getDistrictFromAuto, 10000);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (netWorkConnectChangedReceiver != null) {
            getApplicationContext().unregisterReceiver(netWorkConnectChangedReceiver);
        }
        if (screenOnReceiver != null) {
            getApplicationContext().unregisterReceiver(screenOnReceiver);
        }
        stopForeground(true);
    }
    @Subscribe
    public void launchFloatWindow(FloatWindowsLaunchEvent event){
        Utils.startFloatingWindows(getApplicationContext(),event.isEnable());
    }
    @Subscribe
    public void launchService(LaunchEvent event) {
        Intent mService = new Intent(AppObject.getContext(), event.getServiceClass());
        if(event.getExtentParameters()!=null && event.getExtentParameters().size()>0){
            for(String key:event.getExtentParameters().keySet()){
                mService.putExtra(key,event.getExtentParameters().get(key));
            }
        }
        if (event.getDelaySeconds() > 0) {
            new Handler().postDelayed(() -> {
                if (event.isToClose()) {
                    AppObject.getContext().stopService(mService);
                } else {
                    AppObject.getContext().startService(mService);
                }
            }, event.getDelaySeconds() * 1000);
        } else {
            if (event.isToClose()) {
                AppObject.getContext().stopService(mService);
            } else {
                AppObject.getContext().startService(mService);
            }
        }
    }

    @Subscribe
    public void launchAutoWidgetFloatingWindow(AutoMapStatusUpdateEvent event) {
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

    private void getDistrictFromAuto() {
        x.task().postDelayed(() -> {
            Intent intent = new Intent();
            intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
            intent.putExtra("KEY_TYPE", 10029);
            sendBroadcast(intent);
        }, 1000 * 10);
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
        IntentFilter bootstrapFiliter = new IntentFilter();
        bootstrapFiliter.addAction("android.intent.action.USER_PRESENT");
        bootstrapFiliter.addAction("android.intent.action.BOOT_COMPLETED");
        bootstrapFiliter.addAction("autochips.intent.action.QB_POWERON");
        bootstrapFiliter.addAction("xy.android.acc.on");
        bootstrapFiliter.addAction("com.nwd.action.ACTION_MCU_STATE_CHANGE");
        bootstrapFiliter.addAction("com.unisound.intent.action.DO_SLEEP");
        context.registerReceiver(new BootStartReceiver(), bootstrapFiliter);

        DateChangeReceiver receiver=new DateChangeReceiver();
        IntentFilter timeFilter=new IntentFilter(Intent.ACTION_TIME_TICK);
        timeFilter.addAction(Intent.ACTION_TIME_CHANGED);
        timeFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        context.getApplicationContext().registerReceiver(receiver,timeFilter);
    }
}
