package com.huivip.gpsspeedwidget.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.service.GpsSpeedService;
import com.huivip.gpsspeedwidget.utils.PrefUtils;


/**
 * @author sunlaihui
 */
public class LyricWidgetProvider extends AppWidgetProvider {
    @Override
    public void onReceive(Context context, Intent paramIntent) {
        super.onReceive(context, paramIntent);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.lyric_widget);
       /* Intent service = new Intent(context, GpsSpeedService.class);
        views.setOnClickPendingIntent(R.id.number_widget, PendingIntent.getService(context, 0,
                service, 0));*/
        ComponentName localComponentName = new ComponentName(context, LyricWidgetProvider.class);
        AppWidgetManager.getInstance(context).updateAppWidget(localComponentName, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
      /*  for (int i = 0; i < appWidgetIds.length; i++) {
            int appWidgetId = appWidgetIds[i];

            Intent intent = new Intent(context, GpsSpeedService.class);
            intent.setAction(GpsSpeedService.EXTRA_ACION_SPEED_CLICK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.speednumberwidget);
            views.setOnClickPendingIntent(R.id.number_speed, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }*/
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        PrefUtils.setWidgetActived(context,false);
        PrefUtils.setEnabledNumberWidget(context,false);
        context.stopService(new Intent(context,GpsSpeedService.class));
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        PrefUtils.setUserManualClosedServer(context,false);
        PrefUtils.setEnabledNumberWidget(context,true);
        PrefUtils.setWidgetActived(context,true);
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        PrefUtils.setUserManualClosedServer(context,false);
        PrefUtils.setEnabledNumberWidget(context,false);
        PrefUtils.setWidgetActived(context,false);
        super.onDisabled(context);
    }

}
