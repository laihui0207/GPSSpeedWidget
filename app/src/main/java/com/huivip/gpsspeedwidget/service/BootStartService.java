package com.huivip.gpsspeedwidget.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.huivip.gpsspeedwidget.GpsUtil;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.listener.AutoLaunchSystemConfigReceiver;
import com.huivip.gpsspeedwidget.listener.GoToHomeReceiver;
import com.huivip.gpsspeedwidget.listener.WeatherServiceReceiver;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;

public class BootStartService extends Service {
    public static String START_BOOT="FromSTARTBOOT";
    boolean started=false;
    boolean autoStarted=false;
    AlarmManager alarm;
    private NotificationManager notificationManager = null;
    private static final String NOTIFICATION_CHANNEL_NAME = "BackgroundLocation";

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
        super.onCreate();
        if (PrefUtils.isPlayWaring(getApplicationContext())) {
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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean start = PrefUtils.isEnableAutoStart(getApplicationContext());
        if(intent!=null && !autoStarted){
            if(start) {
                if (intent.getBooleanExtra(START_BOOT, false)) {
                    Log.d(START_BOOT,"Auto Boot Start Service Launched");
                    autoStarted = true;
                    startForeground(1,buildNotification());
                    String apps = PrefUtils.getAutoLaunchApps(getApplicationContext());
                    if(!TextUtils.isEmpty(apps)) {
                        String[] autoApps = apps.split(",");
                        if (autoApps != null || autoApps.length > 0) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    int delayTime = PrefUtils.getDelayStartOtherApp(getApplicationContext());
                                    for (String packageName : autoApps) {
                                        Intent launchIntent = getApplicationContext().getPackageManager().getLaunchIntentForPackage(packageName);
                                        if (launchIntent != null && !Utils.isServiceRunning(getApplicationContext(), packageName)) {
                                            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            getApplicationContext().startActivity(launchIntent);//null pointer check in case package name was not found
                                        }
                                        try {
                                            Thread.sleep(delayTime * 1000 + 300L);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    AlarmManager alarm = (AlarmManager) getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);
                                    PendingIntent gotoHomeIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), GoToHomeReceiver.class), 0);
                                    alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 5000L, gotoHomeIntent);
                                }
                            }).start();
                        }
                    }
                    AlarmManager alarm = (AlarmManager) getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);
                    PendingIntent gotoHomeIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), GoToHomeReceiver.class), 0);
                    alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 5000L, gotoHomeIntent);
                   /* PendingIntent thirdIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), ThirdSoftLaunchReceiver.class), 0);
                    alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 100L, thirdIntent);*/

                    PendingIntent autoLaunchIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), AutoLaunchSystemConfigReceiver.class), 0);
                    alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 5000L, autoLaunchIntent);

                   /* PendingIntent autoFtpBackupIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), AutoFTPBackupReceiver.class), 0);
                    alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 120000L, autoFtpBackupIntent);*/
                    Intent searchWeatherService=new Intent(getApplicationContext(),WeatherService.class);
                    getApplicationContext().startService(searchWeatherService);
                    if(PrefUtils.isEnableNAVIUploadGPSHistory(getApplicationContext())){
                        Intent naviTrackService=new Intent(getApplicationContext(), NaviTrackService.class);
                        getApplicationContext().startService(naviTrackService);
                    }
                    PendingIntent weatherServiceIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), WeatherServiceReceiver.class), 0);
                    alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 60000L, weatherServiceIntent);
                }
            }
        }
        if(intent!=null && !started){
            Log.d(START_BOOT,"Boot Start Service Launched");
            if(start) {
                started=true;
                    if(!Utils.isServiceRunning(getApplicationContext(), GpsSpeedService.class.getName())) {
                        Intent service = new Intent(getApplicationContext(), GpsSpeedService.class);
                        service.putExtra(GpsSpeedService.EXTRA_AUTOBOOT, true);
                        startService(service);
                    }

                PrefUtils.setEnableTempAudioService(getApplicationContext(), true);
                if(!PrefUtils.isEnableAccessibilityService(getApplicationContext())){
                    PrefUtils.setShowFlattingOn(getApplicationContext(),PrefUtils.SHOW_ALL);
                }
               // if (PrefUtils.getShowFlatingOn(getApplicationContext()).equalsIgnoreCase(PrefUtils.SHOW_ALL)) {
                    Utils.startFloatingWindows(getApplicationContext(),true);
                //}
                if(!PrefUtils.isWidgetActived(getApplicationContext()) && !PrefUtils.isEnableFlatingWindow(getApplicationContext())){
                    GpsUtil.getInstance(getApplicationContext()).startLocationService();
                }
                if(PrefUtils.getEnableAltitudeWindow(getApplicationContext())){
                    Intent altitudeFloatingService=new Intent(getApplicationContext(),AltitudeFloatingService.class);
                    getApplicationContext().startService(altitudeFloatingService);
                }
                if(PrefUtils.isEnableRoadLineFloating(getApplicationContext())){
                    Intent roadLineFloatingService=new Intent(getApplicationContext(),RoadLineFloatingService.class);
                    getApplicationContext().startService(roadLineFloatingService);
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
    @SuppressLint("NewApi")
    private Notification buildNotification() {

        Notification.Builder builder = null;
        Notification notification = null;
        if(android.os.Build.VERSION.SDK_INT >= 26) {
            //Android O上对Notification进行了修改，如果设置的targetSDKVersion>=26建议使用此种方式创建通知栏
            if (null == notificationManager) {
                notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            }
            String channelId = getApplicationContext().getPackageName();
            if(!isCreateChannel) {
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
                // .setSmallIcon(R.drawable.ic_speedometer_notif)
                .setContentText("正在后台运行")
                .setWhen(System.currentTimeMillis());

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            notification = builder.build();
        } else {
            return builder.getNotification();
        }
        return notification;
    }
}
