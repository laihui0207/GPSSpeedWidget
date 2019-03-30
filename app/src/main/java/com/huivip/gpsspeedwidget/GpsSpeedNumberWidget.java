package com.huivip.gpsspeedwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.*;
import android.widget.RemoteViews;
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
        Intent mapFloatingService=new Intent(context,MapFloatingService.class);
        PendingIntent launchMapFloatingService=PendingIntent.getService(context,1,mapFloatingService,0);
        views.setOnClickPendingIntent(R.id.number_limit,launchMapFloatingService);
        if(!Utils.isServiceRunning(context,BootStartService.class.getName())){
            Intent bootService=new Intent(context,BootStartService.class);
            bootService.putExtra(BootStartService.START_BOOT,true);
            context.startService(bootService);
        }
        PendingIntent goHomeIntent=sendAutoBroadCase(context,1);
        PendingIntent goCompanyIntent=sendAutoBroadCase(context,2);
        views.setOnClickPendingIntent(R.id.image_home,goHomeIntent);
        views.setOnClickPendingIntent(R.id.image_company,goCompanyIntent);
        ComponentName localComponentName = new ComponentName(context, GpsSpeedNumberWidget.class);
        AppWidgetManager.getInstance(context).updateAppWidget(localComponentName, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
       /* for (int i = 0; i < appWidgetIds.length; i++) {
            int appWidgetId = appWidgetIds[i];

            Intent intent = new Intent(context, GpsSpeedService.class);
            intent.setAction(GpsSpeedService.EXTRA_ACION_SPEED_CLICK);
            intent.putExtra("Key",appWidgetId);
            Log.d("huivip","Widget Id:"+appWidgetId);
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
    private PendingIntent sendAutoBroadCase(Context context,int type){
        Intent intent = new Intent();
       /* intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", 10045);
        intent.putExtra("EXTRA_TYPE",type);*/

        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", 10040);
        intent.putExtra("DEST", type);
        intent.putExtra("IS_START_NAVI", 0);
        intent.putExtra("SOURCE_APP","GPSWidget");
        return PendingIntent.getBroadcast(context,type+10,intent,0);
    }
}
