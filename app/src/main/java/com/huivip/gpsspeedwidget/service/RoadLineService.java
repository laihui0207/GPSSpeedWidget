package com.huivip.gpsspeedwidget.service;

import android.app.Service;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.GpsUtil;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;

public class RoadLineService extends Service {
    AppWidgetHost appWidgetHost;
    AppWidgetManager appWidgetManager;
    RoadLineBinder roadLineBinder;
    GpsUtil gpsUtil;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return roadLineBinder;
    }

    public class RoadLineBinder extends Binder {
        public View getRoadLineView(){
            return RoadLineService.this.getRoadLineView();
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        gpsUtil=GpsUtil.getInstance(getApplicationContext());
        roadLineBinder = new RoadLineBinder();
        appWidgetHost = new AppWidgetHost(getApplicationContext(), Constant.APP_WIDGET_HOST_ID);
        appWidgetManager = AppWidgetManager.getInstance(this);
    }

    private View getRoadLineView(){
        int id = PrefUtils.getSelectAMAPPLUGIN(getApplicationContext());
        if (id != -1) {
            AppWidgetProviderInfo popupWidgetInfo = appWidgetManager.getAppWidgetInfo(id);
            final View amapView = appWidgetHost.createView(this, id, popupWidgetInfo);
            View vv = null;
            if (gpsUtil.getAutoNaviStatus()==Constant.Navi_Status_Started) {
                vv = Utils.findlayoutViewById(amapView, "widget_daohang_road_line");
            } else {
                vv = Utils.findlayoutViewById(amapView, "road_line");
            }
            if(vv!=null && vv instanceof ImageView){
                return vv;
            } else {
                return null;
            }
        }
        return null;
    }
}
