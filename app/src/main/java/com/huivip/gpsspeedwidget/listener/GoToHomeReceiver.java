package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;

public class GoToHomeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent paramIntent) {
        if(PrefUtils.isGoToHomeAfterAutoLanuch(context)) {
            Utils.goHome(context);
        }
    }
}
