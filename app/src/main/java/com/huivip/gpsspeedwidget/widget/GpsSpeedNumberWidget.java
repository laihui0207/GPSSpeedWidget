package com.huivip.gpsspeedwidget.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.activity.MainActivity;
import com.huivip.gpsspeedwidget.listener.SwitchReceiver;
import com.huivip.gpsspeedwidget.service.BootStartService;
import com.huivip.gpsspeedwidget.service.GpsSpeedService;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;


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
/*        Intent mapFloatingService=new Intent(context, MapFloatingService.class);*/
        PendingIntent launchMapFloatingService=sendSwitchBroadCast(context,SwitchReceiver.SWITCH_TARGET_MAPFLOATING,301);//PendingIntent.getService(context,1,mapFloatingService,PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.number_limit, launchMapFloatingService);
        if(!Utils.isServiceRunning(context, BootStartService.class.getName())){
            Intent bootService=new Intent(context,BootStartService.class);
            bootService.putExtra(BootStartService.START_BOOT,true);
            context.startService(bootService);
        }
        Intent mainActivity=new Intent(context, MainActivity.class);
        PendingIntent mainActivityPendingIntent=PendingIntent.getActivity(context,3,mainActivity,0);
        views.setOnClickPendingIntent(R.id.image_config,mainActivityPendingIntent);
        PendingIntent goHomeIntent= sendAutoBroadCast(context,10040,0);
        PendingIntent goCompanyIntent= sendAutoBroadCast(context,10040,1);
        PendingIntent goAutoIntent = sendSwitchBroadCast(context,SwitchReceiver.SWITCH_TARGET_AUTOAMAP,400);
        PendingIntent goGasStationIntent= sendSwitchBroadCast(context,SwitchReceiver.SWITCH_TARGET_LYRIC,500); //sendAutoBroadCast(context,10036,201);
        PendingIntent switchXunHang=sendSwitchBroadCast(context,SwitchReceiver.SWITCH_TARGET_XUNHANG,600);
        views.setOnClickPendingIntent(R.id.image_gas_station,goGasStationIntent);
        views.setOnClickPendingIntent(R.id.image_home,goHomeIntent);
        views.setOnClickPendingIntent(R.id.image_company,goCompanyIntent);
        views.setOnClickPendingIntent(R.id.image_auto,goAutoIntent);
        views.setOnClickPendingIntent(R.id.image_xunhang_switch,switchXunHang);

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
        return PendingIntent.getBroadcast(context,10099+type,intent,PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
