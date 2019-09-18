package com.huivip.gpsspeedwidget.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.service.GpsSpeedMeterService;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;


/**
 * @author sunlaihui
 */
public class GpsSpeedMeterWidget extends AppWidgetProvider {
    @Override
    public void onReceive(Context context, Intent paramIntent) {
        super.onReceive(context, paramIntent);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.speedmeterwidget);
        Intent service = new Intent(context, GpsSpeedMeterService.class);
        views.setOnClickPendingIntent(R.id.ifreccia_all, PendingIntent.getService(context, 0,
                service, 0));
        if(PrefUtils.isSpeedMeterWidgetEnable(context) && !Utils.isServiceRunning(context, GpsSpeedMeterService.class.getName())){
            Intent bootService=new Intent(context, GpsSpeedMeterService.class);
            context.startService(bootService);
        }
        ComponentName localComponentName = new ComponentName(context, GpsSpeedMeterWidget.class);
        AppWidgetManager.getInstance(context).updateAppWidget(localComponentName, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        PrefUtils.setWidgetActived(context,false);
        PrefUtils.setEnabledWatchWidget(context,false);
        context.stopService(new Intent(context, GpsSpeedMeterService.class));
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        PrefUtils.setSpeedMeterWidgetEnable(context,true);
        PrefUtils.setUserManualClosedServer(context,false);
        PrefUtils.setWidgetActived(context,true);
        PrefUtils.setEnabledWatchWidget(context,true);
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        PrefUtils.setSpeedMeterWidgetEnable(context,false);
        PrefUtils.setUserManualClosedServer(context,false);
        PrefUtils.setEnabledWatchWidget(context,false);
        PrefUtils.setWidgetActived(context,false);
        if(Utils.isServiceRunning(context, GpsSpeedMeterService.class.getName())){
            Intent bootService=new Intent(context, GpsSpeedMeterService.class);
            bootService.putExtra(GpsSpeedMeterService.EXTRA_CLOSE,true);
            context.startService(bootService);
        }
        super.onDisabled(context);
    }

}
