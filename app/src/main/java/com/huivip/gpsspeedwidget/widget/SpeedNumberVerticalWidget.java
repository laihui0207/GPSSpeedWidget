package com.huivip.gpsspeedwidget.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.listener.SwitchReceiver;
import com.huivip.gpsspeedwidget.utils.PrefUtils;


/**
 * @author sunlaihui
 */
public class SpeedNumberVerticalWidget extends AppWidgetProvider {
    @Override
    public void onReceive(Context context, Intent paramIntent) {
        super.onReceive(context, paramIntent);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.speed_number_vertical_widget);
        //Intent service = new Intent(context, SpeedNumberVerticalService.class);
        views.setOnClickPendingIntent(R.id.v_speed_base_v, null);
        PendingIntent launchMapFloatingService=sendSwitchBroadCast(context,SwitchReceiver.SWITCH_TARGET_MAPFLOATING,302);//PendingIntent.getService(context,1,mapFloatingService,PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.v_speed_edog, launchMapFloatingService);

        /*Intent configureActivity=new Intent(context, MainActivity.class);
        PendingIntent mainActivityPendingIntent=PendingIntent.getActivity(context,3,configureActivity,0);
        views.setOnClickPendingIntent(R.id.image_config,mainActivityPendingIntent);*/
        PendingIntent goHomeIntent= sendAutoBroadCast(context,10040,0);
        PendingIntent goCompanyIntent= sendAutoBroadCast(context,10040,1);
        PendingIntent goAutoIntent = sendSwitchBroadCast(context,SwitchReceiver.SWITCH_TARGET_AUTOAMAP,400);
        PendingIntent switchGPSService= sendSwitchBroadCast(context,SwitchReceiver.SWITCH_TARGET_GPS,500);
        // PendingIntent goGasStationIntent= sendSwitchBroadCast(context,SwitchReceiver.SWITCH_TARGET_LYRIC,500); //sendAutoBroadCast(context,10036,201);
        PendingIntent switchXunHang=sendSwitchBroadCast(context,SwitchReceiver.SWITCH_TARGET_XUNHANG,600);
        //views.setOnClickPendingIntent(R.id.image_gas_station,goGasStationIntent);
        views.setOnClickPendingIntent(R.id.v_gohome,goHomeIntent);
        views.setOnClickPendingIntent(R.id.v_gocompany,goCompanyIntent);
        views.setOnClickPendingIntent(R.id.v_gomap,goAutoIntent);
        views.setOnClickPendingIntent(R.id.v_navi_layout,goAutoIntent);
        views.setOnClickPendingIntent(R.id.v_speed_layout,switchGPSService);
        views.setOnClickPendingIntent(R.id.v_edog,switchXunHang);

        ComponentName localComponentName = new ComponentName(context, SpeedNumberVerticalWidget.class);
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
        PrefUtils.setSpeedNumberVWidgetEnable(context,true);
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        PrefUtils.setSpeedNumberVWidgetEnable(context,false);
        super.onDisabled(context);
    }
    private PendingIntent sendAutoBroadCast(Context context, int key, int type){
        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", key);
        if(key==10040) {
            intent.putExtra("DEST", type);
            intent.putExtra("IS_START_NAVI", 0);
        } else if(key==10036){
            intent.putExtra("KEYWORDS","加油站");
        }
        intent.putExtra("SOURCE_APP","GPSWidget");
        return PendingIntent.getBroadcast(context,type+10,intent,0);
    }
    private PendingIntent sendSwitchBroadCast(Context context,String target,int type){
        Intent intent = new Intent();
        intent.setAction(SwitchReceiver.SWITCH_EVENT);
        intent.putExtra("TARGET", target);
        return PendingIntent.getBroadcast(context,1099+type,intent,PendingIntent.FLAG_UPDATE_CURRENT);
    }
}