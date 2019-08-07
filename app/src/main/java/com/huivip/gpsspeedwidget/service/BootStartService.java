package com.huivip.gpsspeedwidget.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.listener.AutoLaunchSystemConfigReceiver;
import com.huivip.gpsspeedwidget.listener.GoToHomeReceiver;
import com.huivip.gpsspeedwidget.listener.WeatherServiceReceiver;
import com.huivip.gpsspeedwidget.speech.AudioService;
import com.huivip.gpsspeedwidget.utils.CrashHandler;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.TimeThread;
import com.huivip.gpsspeedwidget.utils.Utils;


public class BootStartService extends Service {
    public static String START_BOOT = "FromSTARTBOOT";
    boolean started = false;
    AlarmManager alarm;

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
        boolean start = PrefUtils.isEnableAutoStart(getApplicationContext());
        if (start) {
                  /*  PendingIntent thirdIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), ThirdSoftLaunchReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
                    alarm.setExact(AlarmManager.RTC_WAKEUP, + 100L, thirdIntent);*/
            String apps = PrefUtils.getAutoLaunchApps(getApplicationContext());
            if (!TextUtils.isEmpty(apps)) {
                String[] autoApps = apps.split(",");
                if (autoApps != null || autoApps.length > 0) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int delayTime = PrefUtils.getDelayStartOtherApp(getApplicationContext());
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
                            if (PrefUtils.isGoToHomeAfterAutoLanuch(getApplicationContext())) {
                                AlarmManager alarm = (AlarmManager) getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);
                                PendingIntent gotoHomeIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
                                        new Intent(getApplicationContext(), GoToHomeReceiver.class), 0);
                                alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 5000L, gotoHomeIntent);
                            }

                        }
                    }).start();
                } else if (PrefUtils.isGoToHomeAfterAutoLanuch(getApplicationContext())) {
                    AlarmManager alarm = (AlarmManager) getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);
                    PendingIntent gotoHomeIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
                            new Intent(getApplicationContext(), GoToHomeReceiver.class), 0);
                    alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 5000L, gotoHomeIntent);
                }
            }

            TimeThread timeThread = new TimeThread(null);
            timeThread.setContext(getApplicationContext());
            timeThread.start();

            PendingIntent autoLaunchIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
                    new Intent(getApplicationContext(), AutoLaunchSystemConfigReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
            alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000L, autoLaunchIntent);

            if (PrefUtils.isPlayTime(getApplicationContext()) || PrefUtils.isPlayWeather(getApplicationContext()) || PrefUtils.isShowAddressWhenStop(getApplicationContext())) {
                Intent weatcherService = new Intent(getApplicationContext(), WeatherService.class);
                getApplicationContext().startService(weatcherService);

                PendingIntent weatherServiceIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
                        new Intent(getApplicationContext(), WeatherServiceReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
                alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000L, weatherServiceIntent);
            }
            if (PrefUtils.isPlayWarn(getApplicationContext())) {
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
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean start = PrefUtils.isEnableAutoStart(getApplicationContext());
        if (intent != null) {
            if (start) {
                started = true;
                if (PrefUtils.isWidgetActived(getApplicationContext())) {
                    if (!Utils.isServiceRunning(getApplicationContext(), GpsSpeedService.class.getName())) {
                        Intent service = new Intent(getApplicationContext(), GpsSpeedService.class);
                        service.putExtra(GpsSpeedService.EXTRA_AUTOBOOT, true);
                        startService(service);
                    }
                }

                PrefUtils.setEnableTempAudioService(getApplicationContext(), true);
                if (PrefUtils.isEnableTimeFloatingWidow(getApplicationContext())) {
                    if (!Utils.isServiceRunning(getApplicationContext(), RealTimeFloatingService.class.getName())) {
                        Intent timeFloating = new Intent(getApplicationContext(), RealTimeFloatingService.class);
                        startService(timeFloating);
                    }
                }
                //GpsUtil.getInstance(getApplicationContext()).startLocationService();

                /*if(!PrefUtils.isWidgetActived(getApplicationContext()) && !PrefUtils.isEnableFlatingWindow(getApplicationContext())){
                    GpsUtil.getInstance(getApplicationContext()).startLocationService();
                }*/
                if(PrefUtils.isEnableRoadLineFloating(getApplicationContext())){
                    Intent roadLineFloatingService=new Intent(getApplicationContext(),RoadLineFloatingService.class);
                    getApplicationContext().startService(roadLineFloatingService);
                }
                Utils.startFloatingWindows(getApplicationContext(), true);
                if(!Utils.isServiceRunning(getApplicationContext(), AudioService.class.getName())){
                    Intent audioService=new Intent(getApplicationContext(),AudioService.class);
                    startService(audioService);
                }
                if(!Utils.isServiceRunning(getApplicationContext(),AutoXunHangService.class.getName())) {
                    Intent xunHangService=new Intent(getApplicationContext(),AutoXunHangService.class);
                    startService(xunHangService);
                }
                if(PrefUtils.isEnableNAVIUploadGPSHistory(getApplicationContext())) {
                    Intent trackService=new Intent(getApplicationContext(), NaviTrackService.class);
                    startService(trackService);
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

}
