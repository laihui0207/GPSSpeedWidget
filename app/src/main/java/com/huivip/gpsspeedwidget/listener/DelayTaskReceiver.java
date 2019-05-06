package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.huivip.gpsspeedwidget.service.LyricFloatingService;
import com.huivip.gpsspeedwidget.service.TextFloatingService;

public class DelayTaskReceiver extends BroadcastReceiver {
    public static String TARGET="delayTask.Target";
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent!=null){
            if(intent.getAction().equalsIgnoreCase(TextFloatingService.EXTRA_CLOSE)){
                Intent textFloatingService=null;
                if(TextFloatingService.TARGET.equalsIgnoreCase(intent.getStringExtra(TARGET))) {
                    textFloatingService = new Intent(context, TextFloatingService.class);
                    textFloatingService.putExtra(TextFloatingService.EXTRA_CLOSE,true);
                    context.startService(textFloatingService);
                } else if(LyricFloatingService.TARGET.equalsIgnoreCase(intent.getStringExtra(TARGET))){
                    textFloatingService= new Intent(context,LyricFloatingService.class);
                    textFloatingService.putExtra(TextFloatingService.EXTRA_CLOSE,true);
                    context.startService(textFloatingService);
                }

            }
        }
    }
}
