package com.huivip.gpsspeedwidget.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.utils.PrefUtils;


/**
 * @author sunlaihui
 */
public class LyricWidget extends AppWidgetProvider {
    @Override
    public void onReceive(Context context, Intent paramIntent) {
        super.onReceive(context, paramIntent);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.lyric_widget);
        ComponentName localComponentName = new ComponentName(context, LyricWidget.class);
        views.setOnClickPendingIntent(R.id.lyric_widget,null);
        AppWidgetManager.getInstance(context).updateAppWidget(localComponentName, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        PrefUtils.setLyricWidgetEnable(context,true);
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        PrefUtils.setLyricWidgetEnable(context,false);
        super.onDisabled(context);
    }

}