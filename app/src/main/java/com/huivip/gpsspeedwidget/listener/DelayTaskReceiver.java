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
                if(TextFloatingService.TARGET.equalsIgnoreCase(intent.getStringExtra(TARGET))) {
                    EventBus.getDefault().post((new LaunchEvent(TextFloatingService.class)).setToClose(false));

                } else if(LyricFloatingService.TARGET.equalsIgnoreCase(intent.getStringExtra(TARGET))){
                    EventBus.getDefault().post((new LaunchEvent(LyricFloatingService.class)).setToClose(false));

                }

            }
            if ("START".equalsIgnoreCase(intent.getAction())) {
                if(DefaultFloatingService.TARGET.equalsIgnoreCase(intent.getStringExtra(TARGET))){
                    EventBus.getDefault().post(new FloatWindowsLaunchEvent(true));

                }
            }
        }
    }
}
