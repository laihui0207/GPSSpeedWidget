package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.WifiUtils;

public class AutoLaunchSystemConfigReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(!PrefUtils.isAutoLauchHotSpot(context)) return;
        boolean enabled=WifiUtils.switchWifiHotspot(context,"GPSWidget_wifi","1234567890",true);
        if(enabled){
            Toast.makeText(context,"移动热点已启动:GPSWidget_wifi,密码: 1234567890",Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(context,"移动热点启动失败！",Toast.LENGTH_SHORT).show();
        }
    }
}
