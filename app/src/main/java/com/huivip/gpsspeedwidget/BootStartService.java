package com.huivip.gpsspeedwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import com.huivip.gpsspeedwidget.listener.AutoFTPBackupReceiver;
import com.huivip.gpsspeedwidget.listener.AutoLaunchSystemConfigReceiver;
import com.huivip.gpsspeedwidget.listener.ThirdSoftLaunchReceiver;
import com.huivip.gpsspeedwidget.listener.WeatherServiceReceiver;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;

public class BootStartService extends Service {
    public static String START_BOOT="FromSTARTBOOT";
    boolean started=false;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null && !started){
            boolean start = PrefUtils.isEnableAutoStart(getApplicationContext());
            if(start) {
                if(PrefUtils.isWidgetActived(getApplicationContext())) {
                    Intent service = new Intent(getApplicationContext(), GpsSpeedService.class);
                    service.putExtra(GpsSpeedService.EXTRA_AUTOBOOT, true);
                    startService(service);
                }

                PrefUtils.setEnableTempAudioService(getApplicationContext(), true);
                if(!PrefUtils.isEnableAccessibilityService(getApplicationContext())){
                    PrefUtils.setShowFlattingOn(getApplicationContext(),PrefUtils.SHOW_ALL);
                }
                boolean floatingServiceIsRunning=false;
                if (PrefUtils.getShowFlatingOn(getApplicationContext()).equalsIgnoreCase(PrefUtils.SHOW_ALL)) {
                    /*Intent floatService = new Intent(getApplicationContext(), FloatingService.class);
                    String floatingStyle = PrefUtils.getFloatingStyle(getApplicationContext());
                    floatingServiceIsRunning=Utils.isServiceRunning(getApplicationContext(),FloatingService.class.getName());
                    if (floatingStyle.equalsIgnoreCase(PrefUtils.FLOATING_AUTONAVI)) {
                        floatingServiceIsRunning=Utils.isServiceRunning(getApplicationContext(),AutoNaviFloatingService.class.getName());
                        floatService = new Intent(getApplicationContext(), AutoNaviFloatingService.class);
                    } else if (floatingStyle.equals(PrefUtils.FLOATING_METER)) {
                        floatService = new Intent(getApplicationContext(), MeterFloatingService.class);
                        floatingServiceIsRunning=Utils.isServiceRunning(getApplicationContext(),MeterFloatingService.class.getName());
                    }
                    if(!floatingServiceIsRunning){
                        startService(floatService);
                    }*/
                    Utils.startFloationgWindows(getApplicationContext(),true);
                }
                if(!PrefUtils.isWidgetActived(getApplicationContext()) && !PrefUtils.isEnableFlatingWindow(getApplicationContext())){
                    GpsUtil.getInstance(getApplicationContext()).startLocationService();
                }
                started=true;
                AlarmManager alarm = (AlarmManager) getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);
                if(intent.getBooleanExtra(START_BOOT,false)) {
                    int delayTime = PrefUtils.getDelayStartOtherApp(getApplicationContext());
                    PendingIntent thirdIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), ThirdSoftLaunchReceiver.class), 0);
                    alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + (delayTime * 1000 + 300), thirdIntent);

                    PendingIntent autoLaunchIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), AutoLaunchSystemConfigReceiver.class), 0);
                    alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 5000L, autoLaunchIntent);

                    PendingIntent autoFtpBackupIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), AutoFTPBackupReceiver.class), 0);
                    alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 120000L, autoFtpBackupIntent);
                }

                PendingIntent weatcherServiceIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), WeatherServiceReceiver.class), 0);
                alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 60000L, weatcherServiceIntent);

            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
