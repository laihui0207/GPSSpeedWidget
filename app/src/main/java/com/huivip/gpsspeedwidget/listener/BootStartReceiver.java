package com.huivip.gpsspeedwidget.listener;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import com.huivip.gpsspeedwidget.GpsSpeedService;
import com.huivip.gpsspeedwidget.utils.PrefUtils;

public class BootStartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent!=null){
            if(intent.getAction().equalsIgnoreCase(Intent.ACTION_REBOOT)
                    || intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)
                    || intent.getAction().equalsIgnoreCase(Intent.ACTION_MEDIA_MOUNTED)){
                Log.d("huivip","get Action:"+intent.getAction());
                boolean start = PrefUtils.isEnableAutoStart(context);
                if(start) {
                    Intent service = new Intent(context, GpsSpeedService.class);
                    service.putExtra(GpsSpeedService.EXTRA_AUTOBOOT, true);
                    context.startService(service);

                    int delayTime=PrefUtils.getDelayStartOtherApp(context);
                    AlarmManager alarm=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                    PendingIntent thirdIntent = PendingIntent.getBroadcast(context, 0, new Intent(context,ThirdSoftLaunchReceiver.class), 0);
                    alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + (delayTime * 1000 + 300), thirdIntent);
                    PendingIntent autoLaunchIntent = PendingIntent.getBroadcast(context, 0, new Intent(context,AutoLaunchSystemConfigReceiver.class), 0);
                    alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,5000L,autoLaunchIntent);
                }
            }
        }
    }
}
