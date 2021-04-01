package com.huivip.gpsspeedwidget.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationManagerCompat;
import android.view.KeyEvent;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.service.MusicControllerService;
import com.huivip.gpsspeedwidget.utils.PrefUtils;

import java.util.Set;


/**
 * @author sunlaihui
 */
public class MusicWidget extends AppWidgetProvider {
    @Override
    public void onReceive(Context context, Intent paramIntent) {
        super.onReceive(context, paramIntent);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.music_vertical_widget);
        ComponentName localComponentName = new ComponentName(context, MusicWidget.class);

        views.setOnClickPendingIntent(R.id.v_music_base_v,null);
        views.setOnClickPendingIntent(R.id.v_button_next,sendControllerBroadCast(context, KeyEvent.KEYCODE_MEDIA_NEXT,1));
        views.setOnClickPendingIntent(R.id.v_button_play,sendControllerBroadCast(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,2));
        views.setOnClickPendingIntent(R.id.v_button_prev,sendControllerBroadCast(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS,3));
        views.setOnClickPendingIntent(R.id.v_music_background,sendControllerBroadCast(context,0,4));
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
        if(!isNotificationListenerServiceEnabled(context)){
            if(Build.VERSION.SDK_INT>Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Intent settingIntent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                settingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(settingIntent);
                Toast.makeText(context, "请授予通知使用权限", Toast.LENGTH_SHORT).show();
            }
        }
        PrefUtils.setMusicWidgetEnable(context,true);
        super.onEnabled(context);
    }
    private boolean isNotificationListenerServiceEnabled(Context context) {
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(context);
        return packageNames.contains(context.getPackageName());
    }
    @Override
    public void onDisabled(Context context) {
        PrefUtils.setMusicWidgetEnable(context,false);
        super.onDisabled(context);
    }
    private PendingIntent sendControllerBroadCast(Context context, int key, int type){
        Intent intent = new Intent();
        intent.setAction(MusicControllerService.INTENT_ACTION);
        intent.putExtra("key", key);
        return PendingIntent.getBroadcast(context,1000+type,intent,PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
