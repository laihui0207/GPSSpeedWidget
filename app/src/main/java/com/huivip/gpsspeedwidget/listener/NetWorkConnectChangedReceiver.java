package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.huivip.gpsspeedwidget.service.AutoXunHangService;
import com.huivip.gpsspeedwidget.service.NaviTrackService;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;

public class NetWorkConnectChangedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(Utils.isNetworkConnected(context)) {
            if(!Utils.isServiceRunning(context, AutoXunHangService.class.getName())) {
                Intent xunHangService=new Intent(context,AutoXunHangService.class);
                context.startService(xunHangService);
            }
            if(PrefUtils.isEnableNAVIUploadGPSHistory(context)) {
                Intent trackService=new Intent(context, NaviTrackService.class);
                context.startService(trackService);
            }
            context.getApplicationContext().unregisterReceiver(this);
        }
    }
}
