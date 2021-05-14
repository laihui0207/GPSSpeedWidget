package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.huivip.gpsspeedwidget.beans.FloatWindowsLaunchEvent;
import com.huivip.gpsspeedwidget.beans.LaunchEvent;
import com.huivip.gpsspeedwidget.service.DefaultFloatingService;
import com.huivip.gpsspeedwidget.service.LyricFloatingService;
import com.huivip.gpsspeedwidget.service.TextFloatingService;

import org.greenrobot.eventbus.EventBus;

public class DelayTaskReceiver extends BroadcastReceiver {
    public static String TARGET="delayTask.Target";
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent!=null){
            if(TextFloatingService.EXTRA_CLOSE.equalsIgnoreCase(intent.getAction())){
                Intent textFloatingService=null;
                if(TextFloatingService.TARGET.equalsIgnoreCase(intent.getStringExtra(TARGET))) {
                   /* textFloatingService = new Intent(context, TextFloatingService.class);
                    textFloatingService.putExtra(TextFloatingService.EXTRA_CLOSE,true);
                    context.startService(textFloatingService);*/
                    EventBus.getDefault().post((new LaunchEvent(TextFloatingService.class)).setToClose(false));

                } else if(LyricFloatingService.TARGET.equalsIgnoreCase(intent.getStringExtra(TARGET))){
                   /* textFloatingService= new Intent(context,LyricFloatingService.class);
                    textFloatingService.putExtra(TextFloatingService.EXTRA_CLOSE,true);
                    context.startService(textFloatingService);*/
                    EventBus.getDefault().post((new LaunchEvent(LyricFloatingService.class)).setToClose(false));

                }

            }
            if ("START".equalsIgnoreCase(intent.getAction())) {
               /* if (AltitudeFloatingService.TARGET.equalsIgnoreCase(intent.getStringExtra(TARGET))) {
                    Intent altitudeFloatingService = new Intent(context, AltitudeFloatingService.class);
                    context.startService(altitudeFloatingService);
                }*/
                if(DefaultFloatingService.TARGET.equalsIgnoreCase(intent.getStringExtra(TARGET))){
                    //Utils.startFloatingWindows(context,true);
                    EventBus.getDefault().post(new FloatWindowsLaunchEvent(true));

                }
            }
        }
    }
}
