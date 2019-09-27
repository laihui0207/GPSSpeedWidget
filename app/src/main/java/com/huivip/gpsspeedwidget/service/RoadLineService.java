package com.huivip.gpsspeedwidget.service;

import android.app.Service;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.GpsUtil;
import com.huivip.gpsspeedwidget.beans.RoadLineEvent;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.Utils;

import org.greenrobot.eventbus.EventBus;

import java.util.Timer;
import java.util.TimerTask;

public class RoadLineService extends Service {
    AppWidgetHost appWidgetHost;
    AppWidgetManager appWidgetManager;
    RoadLineBinder roadLineBinder;
    Timer scanTimer=new Timer();
    TimerTask scanTask;
    Handler scanHandler=new Handler();
    GpsUtil gpsUtil;
    View roadLineView = null;
    View preRoadLineView=null;
    View widgetView=null;
    View amapView=null;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return roadLineBinder;
    }

    public class RoadLineBinder extends Binder {
        public View getRoadLineView(){
            return roadLineView;
        }
        public View getWidgetView(){
            return widgetView;
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!AppSettings.get().isEnableRoadLine()){
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
        int id =AppSettings.get().getAmapPluginId();
        if (id != -1) {
            AppWidgetProviderInfo appWidgetInfo = appWidgetManager.getAppWidgetInfo(id);
            amapView = appWidgetHost.createView(this, id, appWidgetInfo);
            widgetView=amapView;
        } else {
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
        return Service.START_REDELIVER_INTENT;//super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        gpsUtil=GpsUtil.getInstance(getApplicationContext());
        roadLineBinder = new RoadLineBinder();
        appWidgetHost = new AppWidgetHost(getApplicationContext(), Constant.APP_WIDGET_HOST_ID);
        appWidgetManager = AppWidgetManager.getInstance(this);
        scanTask = new TimerTask() {
            @Override
            public void run() {
                scanHandler.post(new Runnable() {
                    @Override
                    public void run() {
                       roadLineView = getRoadLineView();
                       if(preRoadLineView!=roadLineView){
                           if(roadLineView!=null) {
                               EventBus.getDefault().post(new RoadLineEvent(true,roadLineView));
                           } else {
                               EventBus.getDefault().post(new RoadLineEvent(false));
                           }
                           preRoadLineView = roadLineView;
                       }
                    }
                });
            }
        };
        scanTimer.schedule(scanTask,0,1000L);
    }

    @Override
    public void onDestroy() {
        scanTimer.cancel();
        scanTimer.purge();
        super.onDestroy();
    }

    private View getRoadLineView() {
        View roladLineImage = null;
        if (gpsUtil.getAutoNaviStatus() == Constant.Navi_Status_Started) {
            roladLineImage = Utils.findlayoutViewById(amapView, "widget_daohang_road_line");
        } else {
            roladLineImage = Utils.findlayoutViewById(amapView, "road_line");
        }
        if (roladLineImage != null && roladLineImage instanceof ImageView) {
            return roladLineImage;
        } else {
            return null;
        }
    }
}
