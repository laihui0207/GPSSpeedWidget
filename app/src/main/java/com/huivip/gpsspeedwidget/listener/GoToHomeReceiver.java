package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.Utils;

public class GoToHomeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent paramIntent) {
        if(AppSettings.get().isReturnHomeAfterLaunchOtherApp()) {
            Utils.goHome(context);
        }
    }
}
