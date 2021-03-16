package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.huivip.gpsspeedwidget.beans.AutoCheckUpdateEvent;
import com.huivip.gpsspeedwidget.service.AutoXunHangService;
import com.huivip.gpsspeedwidget.service.BootStartService;
import com.huivip.gpsspeedwidget.service.NaviTrackService;
import com.huivip.gpsspeedwidget.service.WeatherService;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.xutils.x;

public class NetWorkConnectChangedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(Utils.isNetworkConnected(context)) {
            if(AppSettings.get().isEnableXunHang() && !Utils.isServiceRunning(context, AutoXunHangService.class.getName())) {
                Intent xunHangService=new Intent(context, AutoXunHangService.class);
                context.startService(xunHangService);
            }
            if(AppSettings.get().isEnableTracker() && !Utils.isServiceRunning(context,NaviTrackService.class.getName())) {
                Intent trackService=new Intent(context, NaviTrackService.class);
                context.startService(trackService);
            }
            new Thread(() -> Utils.registerSelf(context)).start();
            if(AppSettings.get().isAutoCheckUpdate()) {
                x.task().postDelayed(() -> {
                    EventBus.getDefault().post(new AutoCheckUpdateEvent().setAutoCheck(true));
                }, 1000 * 60);
            }

         }
        //Toast.makeText(context, "Network connect changed",Toast.LENGTH_SHORT).show();
        if(Utils.isNetworkConnected(context) && !Utils.isServiceRunning(context, WeatherService.class.getName())) {
            Intent bootStartService = new Intent(context, BootStartService.class);
            bootStartService.putExtra(BootStartService.START_RESUME, true);
            Utils.startService(context, bootStartService, true);
        }
    }
}
