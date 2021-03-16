package com.huivip.gpsspeedwidget.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.beans.LaunchEvent;
import com.huivip.gpsspeedwidget.service.TimeWidgetVerticalService;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;

import org.greenrobot.eventbus.EventBus;


/**
 * @author sunlaihui
 */
public class TimeWidget_v extends AppWidgetProvider {
    @Override
    public void onReceive(Context context, Intent paramIntent) {
        super.onReceive(context, paramIntent);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.time_weather_v_widget);

      //  views.setOnClickPendingIntent(R.id.v_time_base_v,null);

        ComponentName localComponentName = new ComponentName(context, TimeWidget_v.class);
        AppWidgetManager.getInstance(context).updateAppWidget(localComponentName, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
       /* if(PrefUtils.isTimeVWidgetEnable(context) && !Utils.isServiceRunning(context, TimeWidgetVerticalService.class.getName())){
            Intent widgetService=new Intent(context, TimeWidgetVerticalService.class);
            Utils.startForegroundService(context,widgetService);
        }*/
       /* if(!Utils.isServiceRunning(context, WeatherService.class.getName())){
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
        PrefUtils.setTimeVWidgetEnable(context,true);
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        if(Utils.isServiceRunning(context, TimeWidgetVerticalService.class.getName())){
           /* Intent widgetService=new Intent(context, TimeWidgetVerticalService.class);
            widgetService.putExtra(TimeWidgetVerticalService.EXTRA_CLOSE,true);
            Utils.startService(context,widgetService);*/
           LaunchEvent event=new LaunchEvent(TimeWidgetVerticalService.class);
           event.setDelaySeconds(3);
           event.setToClose(true);
           EventBus.getDefault().post(event);
        }
        PrefUtils.setTimeVWidgetEnable(context,false);
        super.onDisabled(context);
    }

}
