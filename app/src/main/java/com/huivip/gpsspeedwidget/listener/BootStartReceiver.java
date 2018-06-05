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
            Intent bootService=new Intent(context,BootStartService.class);
            bootService.putExtra(BootStartService.START_BOOT,true);
            context.startService(bootService);
        }
    }
}
