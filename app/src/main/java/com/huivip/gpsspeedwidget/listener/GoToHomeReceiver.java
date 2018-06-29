package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.huivip.gpsspeedwidget.utils.PrefUtils;

public class GoToHomeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent paramIntent) {
        if(PrefUtils.isGoToHomeAfterAutoLanuch(context)) {
            paramIntent = new Intent("android.intent.action.MAIN");
            paramIntent.addCategory("android.intent.category.HOME");
            paramIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(paramIntent);
        }

       /* int delayTime=PrefUtils.getDelayStartOtherApp(context);
        AlarmManager alarm=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent thirdIntent = PendingIntent.getBroadcast(context, 0, new Intent(context,ThirdSoftLaunchReceiver.class), 0);
        alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, (delayTime * 1000 + 300), thirdIntent);*/
    }
}
