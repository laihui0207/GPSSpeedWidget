package com.huivip.gpsspeedwidget.listener;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.text.TextUtils;

import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;

public class ThirdSoftLaunchReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String apps = PrefUtils.getAutoLaunchApps(context);
        if(TextUtils.isEmpty(apps)) return;
        String[] autoApps=apps.split(",");
        if(autoApps!=null || autoApps.length>0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int delayTime = PrefUtils.getDelayStartOtherApp(context);
                    for (String packageName : autoApps) {
                        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                        if (launchIntent != null && !Utils.isServiceRunning(context,packageName)) {
                            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(launchIntent);//null pointer check in case package name was not found
                        }
                        SystemClock.sleep(delayTime * 1000 + 300L);
                    }
                    if(PrefUtils.isGoToHomeAfterAutoLanuch(context)) {
                        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        PendingIntent gotoHomeIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, GoToHomeReceiver.class), 0);
                        alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 5000L, gotoHomeIntent);
                    }
                }
            }).start();
        }
    }
}

