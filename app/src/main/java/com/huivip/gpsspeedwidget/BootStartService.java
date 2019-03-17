package com.huivip.gpsspeedwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import com.huivip.gpsspeedwidget.listener.*;
import com.huivip.gpsspeedwidget.utils.CrashHandler;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;


public class BootStartService extends Service {
    public static String START_BOOT="FromSTARTBOOT";
    boolean started=false;
    boolean autoStarted=false;
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
        super.onCreate();
        mPlayer = MediaPlayer.create(this, R.raw.warn);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean start = PrefUtils.isEnableAutoStart(getApplicationContext());
        if(intent!=null && !autoStarted){
            if(start) {
                if (intent.getBooleanExtra(START_BOOT, false)) {
                    Log.d(START_BOOT,"Auto Boot Start Service Launched");
                    autoStarted = true;
                    PendingIntent thirdIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), ThirdSoftLaunchReceiver.class), 0);
                    alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 100L, thirdIntent);

                    if(PrefUtils.isAutoLauchHotSpot(getApplicationContext())) {
                        PendingIntent autoLaunchIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), AutoLaunchSystemConfigReceiver.class), 0);
                        alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 5000L, autoLaunchIntent);
                    }
                    if (PrefUtils.isFTPAutoBackup(getApplicationContext())) {
                        PendingIntent autoFtpBackupIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), AutoFTPBackupReceiver.class), 0);
                        alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 120000L, autoFtpBackupIntent);
                    }
                    if(PrefUtils.isPlayTime(getApplicationContext()) || PrefUtils.isPlayWeather(getApplicationContext()) || PrefUtils.isShowAddressWhenStop(getApplicationContext())) {
                        PendingIntent weatherServiceIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), WeatherServiceReceiver.class), 0);
                        alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 60000L, weatherServiceIntent);
                    }
                }
                if(PrefUtils.isPlayWarn(getApplicationContext())){
                    mPlayer.start();
                }
            }
        }
        if(intent!=null && !started){
            Log.d(START_BOOT,"Boot Start Service Launched");
            if(start) {
                started=true;
                if(PrefUtils.isWidgetActived(getApplicationContext())) {
                    if(!Utils.isServiceRunning(getApplicationContext(),GpsSpeedService.class.getName())) {
                        Intent service = new Intent(getApplicationContext(), GpsSpeedService.class);
                        service.putExtra(GpsSpeedService.EXTRA_AUTOBOOT, true);
                        startService(service);
                    }
                }

                PrefUtils.setEnableTempAudioService(getApplicationContext(), true);
                if(!PrefUtils.isEnableAccessibilityService(getApplicationContext())){
                    PrefUtils.setShowFlattingOn(getApplicationContext(),PrefUtils.SHOW_ALL);
                }
                if(PrefUtils.isEnableTimeFloatingWidow(getApplicationContext())){
                    if(!Utils.isServiceRunning(getApplicationContext(),RealTimeFloatingService.class.getName())){
                        Intent timeFloating=new Intent(getApplicationContext(),RealTimeFloatingService.class);
                        startService(timeFloating);
                    }
                }
                if (PrefUtils.getShowFlatingOn(getApplicationContext()).equalsIgnoreCase(PrefUtils.SHOW_ALL)) {
                    Utils.startFloatingWindows(getApplicationContext(),true);
                }
                if(!PrefUtils.isWidgetActived(getApplicationContext()) && !PrefUtils.isEnableFlatingWindow(getApplicationContext())){
                    GpsUtil.getInstance(getApplicationContext()).startLocationService();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

}
