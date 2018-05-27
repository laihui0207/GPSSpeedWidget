package com.huivip.gpsspeedwidget.listener;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import com.huivip.gpsspeedwidget.*;
import com.huivip.gpsspeedwidget.utils.PrefUtils;

public class BootStartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("huivip","Boot receiver");
        if(intent!=null){
            Log.d("huivip","get Action:"+intent.getAction());
            if(intent.getAction().equalsIgnoreCase(Intent.ACTION_REBOOT)
                    || intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)
                    || intent.getAction().equalsIgnoreCase(Intent.ACTION_MEDIA_MOUNTED)
                    || intent.getAction().equalsIgnoreCase("android.intent.action.QUICKBOOT_POWERON")
                    || intent.getAction().equalsIgnoreCase("android.intent.action.QUICKBOOT_POWEROFF")
                    || intent.getAction().equalsIgnoreCase("android.net.conn.CONNECTIVITY_CHANGE")
                    /*|| intent.getAction().equalsIgnoreCase("android.net.wifi.WIFI_STATE_CHANGED")
                    || intent.getAction().equalsIgnoreCase("android.net.wifi.STATE_CHANGE")
                    || intent.getAction().equalsIgnoreCase("android.net.nsd.STATE_CHANGED")*/){
                boolean start = PrefUtils.isEnableAutoStart(context);
                if(start) {
                    GpsUtil.getInstance(context).startLocationService();
                    if(PrefUtils.isWidgetActived(context)) {
                        Intent service = new Intent(context, GpsSpeedService.class);
                        service.putExtra(GpsSpeedService.EXTRA_AUTOBOOT, true);
                        context.startService(service);
                    }

                    PrefUtils.setEnableTempAudioService(context, true);
                    if (PrefUtils.getShowFlatingOn(context).equalsIgnoreCase(PrefUtils.SHOW_ALL)) {
                        Intent floatService = new Intent(context, FloatingService.class);
                        String floatingStyle = PrefUtils.getFloatingStyle(context);
                        if (floatingStyle.equalsIgnoreCase(PrefUtils.FLOATING_AUTONAVI)) {
                            floatService = new Intent(context, AutoNaviFloatingService.class);
                        } else if (floatingStyle.equals(PrefUtils.FLOATING_METER)) {
                            floatService = new Intent(context, MeterFloatingService.class);
                        }
                        context.startService(floatService);
                    }

                    int delayTime=PrefUtils.getDelayStartOtherApp(context);
                    AlarmManager alarm=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                    PendingIntent thirdIntent = PendingIntent.getBroadcast(context, 0, new Intent(context,ThirdSoftLaunchReceiver.class), 0);
                    alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + (delayTime * 1000 + 300), thirdIntent);
                    PendingIntent autoLaunchIntent = PendingIntent.getBroadcast(context, 0, new Intent(context,AutoLaunchSystemConfigReceiver.class), 0);
                    alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,5000L,autoLaunchIntent);
                    if (intent.getAction().equalsIgnoreCase("android.net.conn.CONNECTIVITY_CHANGE")) {
                        PendingIntent weatcherServiceIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, WeatherServiceReceiver.class), 0);
                        alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 60000L, weatcherServiceIntent);
                        PendingIntent autoFtpBackupIntent = PendingIntent.getBroadcast(context, 0, new Intent(context,AutoFTPBackupReceiver.class), 0);
                        alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,120000L,autoFtpBackupIntent);
                    }



                }
            }
        }
    }
}
