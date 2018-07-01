package com.huivip.gpsspeedwidget.listener;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import com.huivip.gpsspeedwidget.utils.PrefUtils;

import java.util.Set;

public class ThirdSoftLaunchReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Set<String> autoApps = PrefUtils.getAutoLaunchApps(context);
        if(autoApps==null || autoApps.size()==0){
            return;
        }
       /* int index=PrefUtils.getOtherAppIndex(context);
        if(index==autoApps.size()){
            PrefUtils.setOtherAppIndex(context,0);
            return;
        }
        List<String> apps=new ArrayList<>();
        apps.addAll(autoApps);*/
       int delayTime=PrefUtils.getDelayStartOtherApp(context);
       for(String packageName: autoApps) {
           //String packageName = apps.get(index);
           Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
           if (launchIntent != null) {
               launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
               context.startActivity(launchIntent);//null pointer check in case package name was not found
               SystemClock.sleep(SystemClock.elapsedRealtime()+ delayTime*1000+300L);
           }
       }
        //index++;
       // PrefUtils.setOtherAppIndex(context, index);
        AlarmManager alarm=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent gotoHomeInten = PendingIntent.getBroadcast(context, 0, new Intent(context,GoToHomeReceiver.class), 0);
        alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 300L, gotoHomeInten);
    }
}

