package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.WifiUtils;

public class AutoLaunchSystemConfigReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(PrefUtils.isAutoLauchHotSpot(context)) {
            boolean enabled = WifiUtils.switchWifiHotspot(context, Constant.WIFI_USERNAME,Constant.WIFI_PASSWORD, true);
            if (enabled) {
                Toast.makeText(context, "移动热点已启动:"+Constant.WIFI_USERNAME+",密码:"+Constant.WIFI_PASSWORD, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "移动热点启动失败！", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
