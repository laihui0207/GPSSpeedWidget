package com.huivip.gpsspeedwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.TTSUtil;


/**
 * @author sunlaihui
 */
public class GpsSpeedWidget extends AppWidgetProvider {
    @Override
    public void onReceive(Context context, Intent paramIntent) {
        super.onReceive(context, paramIntent);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.speedwidget);
        Intent service = new Intent(context, GpsSpeedService.class);
        views.setOnClickPendingIntent(R.id.ifreccia, PendingIntent.getService(context, 0,
                service, 0));
        if(paramIntent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)){
            boolean start = PrefUtils.isEnableAutoStart(context);
            if(start) {
                context.startService(service);
            }
        }

        ComponentName localComponentName = new ComponentName(context, GpsSpeedWidget.class);
        AppWidgetManager.getInstance(context).updateAppWidget(localComponentName, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
/*        super.onUpdate(context, appWidgetManager, appWidgetIds);*/
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        context.stopService(new Intent(context,GpsSpeedService.class));
    }
}
