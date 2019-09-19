package com.huivip.gpsspeedwidget.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.huivip.gpsspeedwidget.DeviceUuidFactory;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.beans.RegistEvent;
import com.huivip.gpsspeedwidget.listener.AutoLaunchSystemConfigReceiver;
import com.huivip.gpsspeedwidget.listener.GoToHomeReceiver;
import com.huivip.gpsspeedwidget.listener.NetWorkConnectChangedReceiver;
import com.huivip.gpsspeedwidget.listener.WeatherServiceReceiver;
import com.huivip.gpsspeedwidget.speech.AudioService;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.CrashHandler;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class BootStartService extends Service {
    public static String START_BOOT = "FromSTARTBOOT";
    boolean started = false;
    AlarmManager alarm;
    String deviceId;
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
        DeviceUuidFactory deviceUuidFactory=new DeviceUuidFactory(getApplicationContext());
        deviceId=deviceUuidFactory.getDeviceUuid().toString();
        boolean start = AppSettings.get().getAutoStart();
        Log.d("huivip","Auto Start: "+start);
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
                                alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 5000L, gotoHomeIntent);
                            }

                        }
                    }).start();
                } else if (AppSettings.get().isReturnHomeAfterLaunchOtherApp()) {
                    AlarmManager alarm = (AlarmManager) getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);
                    PendingIntent gotoHomeIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
                            new Intent(getApplicationContext(), GoToHomeReceiver.class), 0);
                    alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 5000L, gotoHomeIntent);
                }
            }
            PendingIntent autoLaunchIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
                    new Intent(getApplicationContext(), AutoLaunchSystemConfigReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
            alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000L, autoLaunchIntent);

            if (AppSettings.get().isPlayTime()|| AppSettings.get().isPlayWeather() || AppSettings.get().isPlayAddressOnStop()) {
                Intent weatherService = new Intent(getApplicationContext(), WeatherService.class);
                getApplicationContext().startService(weatherService);

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
        }
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean start = AppSettings.get().getAutoStart();
        if (intent != null) {
            if (start && !started) {
                started = true;
                PrefUtils.setEnableTempAudioService(getApplicationContext(), true);
                if (AppSettings.get().isEnableTimeWindow()) {
                    if (!Utils.isServiceRunning(getApplicationContext(), RealTimeFloatingService.class.getName())) {
                        Intent timeFloating = new Intent(getApplicationContext(), RealTimeFloatingService.class);
                        startService(timeFloating);
                    }
                }
                if(AppSettings.get().isEnableRoadLine()){
                    if(!Utils.isServiceRunning(getApplicationContext(),RoadLineService.class.getName())) {
                        Intent roadLineService = new Intent(getApplicationContext(), RoadLineService.class);
                        getApplicationContext().startService(roadLineService);
                    }
                    if(!Utils.isServiceRunning(getApplicationContext(),RoadLineFloatingService.class.getName())) {
                        Intent roadLineFloatingService = new Intent(getApplicationContext(), RoadLineFloatingService.class);
                        getApplicationContext().startService(roadLineFloatingService);
                    }
                }
                if(AppSettings.get().isEnableSpeed()) {
                    Utils.startFloatingWindows(getApplicationContext(), true);
                }
                if(AppSettings.get().isEnableAudio() &&  !Utils.isServiceRunning(getApplicationContext(), AudioService.class.getName())){
                    Intent audioService=new Intent(getApplicationContext(),AudioService.class);
                    startService(audioService);
                }
                if(Utils.isNetworkConnected(getApplicationContext())) {
                    if (AppSettings.get().isEnableXunHang() && !Utils.isServiceRunning(getApplicationContext(), AutoXunHangService.class.getName())) {
                        Intent xunHangService = new Intent(getApplicationContext(), AutoXunHangService.class);
                        startService(xunHangService);
                    }
                    if (AppSettings.get().isEnableTracker() && !Utils.isServiceRunning(getApplicationContext(),NaviTrackService.class.getName())) {
                        Intent trackService = new Intent(getApplicationContext(), NaviTrackService.class);
                        startService(trackService);
                    }
                } else {
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                    NetWorkConnectChangedReceiver broadcastReceiver=new NetWorkConnectChangedReceiver();
                    getApplicationContext().registerReceiver(broadcastReceiver, intentFilter);
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void registSelf(RegistEvent event){

    }
}
