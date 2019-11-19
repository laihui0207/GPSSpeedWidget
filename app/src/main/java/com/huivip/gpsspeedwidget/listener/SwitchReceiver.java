package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.huivip.gpsspeedwidget.Constant;

public class SwitchReceiver extends BroadcastReceiver {
    private String TAG="huivip";
    public static String SWITCH_TARGET_AUTOAMAP="com.huivip.switch.AutoAmap";
    public static String SWITCH_EVENT="com.huivip.switch.event";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"Get Switch BoardCast");
        String target=intent.getStringExtra("TARGET");

        if(target!=null && SWITCH_TARGET_AUTOAMAP.equalsIgnoreCase(target)){
            Intent appIntent = context.getPackageManager().getLaunchIntentForPackage(Constant.AMAPAUTOPACKAGENAME);
            appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(appIntent);
        }
    }
}
