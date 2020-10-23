package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.huivip.gpsspeedwidget.service.BootStartService;
import com.huivip.gpsspeedwidget.utils.Utils;

public class BootStartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("huivip","Boot receiver");
        if(intent!=null){
            Log.d("huivip","get Action:"+intent.getAction());
            Intent bootService=new Intent(context, BootStartService.class);
            bootService.putExtra(BootStartService.START_BOOT,true);
            Utils.startService(context,bootService);
        }
    }
}
