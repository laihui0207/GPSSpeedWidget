package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.huivip.gpsspeedwidget.service.BootStartService;

public class BootStartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent!=null){
            //Toast.makeText(context, "Boot Start get Action:"+intent.getAction(), Toast.LENGTH_SHORT).show();
            Intent bootService=new Intent(context,BootStartService.class);
            bootService.putExtra(BootStartService.START_BOOT,true);
            context.startService(bootService);
        }
    }
}
