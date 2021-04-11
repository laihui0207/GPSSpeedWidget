package com.huivip.gpsspeedwidget.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.activity.ConfigurationActivity;
import com.huivip.gpsspeedwidget.listener.SwitchReceiver;
import com.huivip.gpsspeedwidget.service.GpsSpeedService;
import com.huivip.gpsspeedwidget.utils.PrefUtils;


/**
 * @author sunlaihui
 */
public class GpsSpeedNumberWidget extends AppWidgetProvider {
    @Override
    public void onReceive(Context context, Intent paramIntent) {
        super.onReceive(context, paramIntent);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.speednumberwidget);
        Intent service = new Intent(context, GpsSpeedService.class);
        views.setOnClickPendingIntent(R.id.number_speed, PendingIntent.getService(context, 0,
                service, 0));
        Intent configActivity=new Intent(context, ConfigurationActivity.class);
        PendingIntent mainActivityPendingIntent=PendingIntent.getActivity(context,3,configActivity,0);
        views.setOnClickPendingIntent(R.id.image_config,mainActivityPendingIntent);
        PendingIntent goHomeIntent=sendAutoBroadCase(context,10040,0);
        PendingIntent goCompanyIntent=sendAutoBroadCase(context,10040,1);
        PendingIntent goAutoIntent = sendSwitchBroadCast(context,SwitchReceiver.SWITCH_TARGET_AUTOAMAP,10000);
        views.setOnClickPendingIntent(R.id.image_home,goHomeIntent);
        views.setOnClickPendingIntent(R.id.image_company,goCompanyIntent);
        views.setOnClickPendingIntent(R.id.image_auto,goAutoIntent);
        ComponentName localComponentName = new ComponentName(context, GpsSpeedNumberWidget.class);
        AppWidgetManager.getInstance(context).updateAppWidget(localComponentName, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
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
    private PendingIntent sendAutoBroadCase(Context context,int key,int type){
        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", key);
        if(key==10040) {
            intent.putExtra("DEST", type);
            intent.putExtra("IS_START_NAVI", 0);
        }
        intent.putExtra("SOURCE_APP","GPSWidget");
        return PendingIntent.getBroadcast(context,type+10,intent,0);
    }
    private PendingIntent sendSwitchBroadCast(Context context,String target,int type){
        Intent intent = new Intent();
        intent.setAction(SwitchReceiver.SWITCH_EVENT);
        intent.putExtra("TARGET", target);
        return PendingIntent.getBroadcast(context,10099+type,intent,PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
