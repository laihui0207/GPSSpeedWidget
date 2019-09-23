package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.huivip.gpsspeedwidget.service.BootStartService;
import com.huivip.gpsspeedwidget.utils.Utils;

public class BootStartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent!=null){
            //Toast.makeText(context, "Boot Start get Action:"+intent.getAction(), Toast.LENGTH_SHORT).show();
            Utils.getDesktopPackageName(context);
            Intent bootService=new Intent(context,BootStartService.class);
            bootService.putExtra(BootStartService.START_BOOT,true);
            Utils.startService(context,bootService);
           /* if(Build.VERSION.SDK_INT >= 26){
                context.startForegroundService(bootService);
            } else {
                context.startService(bootService);
            }*/
        }
    }
}
