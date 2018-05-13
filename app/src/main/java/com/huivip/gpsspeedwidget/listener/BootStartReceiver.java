package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.huivip.gpsspeedwidget.GpsSpeedService;
import com.huivip.gpsspeedwidget.utils.PrefUtils;

public class BootStartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent!=null){
            if(intent.getAction().equalsIgnoreCase(Intent.ACTION_REBOOT)
                    || intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)){
                boolean start = PrefUtils.isEnableAutoStart(context);
                if(start) {
                    Intent service = new Intent(context, GpsSpeedService.class);
                    service.putExtra(GpsSpeedService.EXTRA_AUTOBOOT, true);
                    context.startService(service);
                }
            }
        }
    }
}
