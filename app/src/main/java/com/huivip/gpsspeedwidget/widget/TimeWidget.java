package com.huivip.gpsspeedwidget.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.beans.LaunchEvent;
import com.huivip.gpsspeedwidget.service.TimeWidgetService;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;

import org.greenrobot.eventbus.EventBus;


/**
 * @author sunlaihui
 */
public class TimeWidget extends AppWidgetProvider {
    @Override
    public void onReceive(Context context, Intent paramIntent) {
        super.onReceive(context, paramIntent);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.time_weather_widget);

        views.setOnClickPendingIntent(R.id.v_time_base,null);

        ComponentName localComponentName = new ComponentName(context, TimeWidget.class);
        AppWidgetManager.getInstance(context).updateAppWidget(localComponentName, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
       /* if(PrefUtils.isTimeHWidgetEnable(context) && !Utils.isServiceRunning(context, TimeWidgetService.class.getName())){
            Intent widgetService=new Intent(context,TimeWidgetService.class);
            Utils.startForegroundService(context,widgetService);
        }*/
        /*if(!Utils.isServiceRunning(context, WeatherService.class.getName())){
            Intent bootService=new Intent(context,BootStartService.class);
            bootService.putExtra(BootStartService.START_BOOT,true);
            context.startService(bootService);
        }*/
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        PrefUtils.setTimeHWidgetEnable(context,true);
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        PrefUtils.setTimeHWidgetEnable(context,false);
        if(Utils.isServiceRunning(context, TimeWidgetService.class.getName())){
            /*Intent widgetService=new Intent(context,TimeWidgetService.class);
            widgetService.putExtra(TimeWidgetService.EXTRA_CLOSE,true);
            Utils.startService(context,widgetService);*/
            LaunchEvent event=new LaunchEvent(TimeWidgetService.class);
            event.setToClose(true);
            event.setDelaySeconds(3);
            EventBus.getDefault().post(event);
        }
        super.onDisabled(context);
    }

}
