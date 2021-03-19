package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.huivip.gpsspeedwidget.service.BootStartService;
import com.huivip.gpsspeedwidget.utils.Utils;

public class ScreenOnReceiver extends BroadcastReceiver {
    String SCREEN_ON = "android.intent.action.SCREEN_ON";
    String SCREEN_OFF = "android.intent.action.SCREEN_OFF";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (SCREEN_ON.equals(intent.getAction())) {
            Intent bootStartService = new Intent(context, BootStartService.class);
            bootStartService.putExtra(BootStartService.START_BOOT, true);
            Utils.startService(context, bootStartService, true);
        }

    }
}
