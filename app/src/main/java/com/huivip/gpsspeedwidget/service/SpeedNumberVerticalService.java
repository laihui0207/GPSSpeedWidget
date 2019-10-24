package com.huivip.gpsspeedwidget.service;

import android.app.Service;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.GpsUtil;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.beans.AimlessStatusUpdateEvent;
import com.huivip.gpsspeedwidget.beans.AudioTempMuteEvent;
import com.huivip.gpsspeedwidget.beans.AutoMapStatusUpdateEvent;
import com.huivip.gpsspeedwidget.beans.NaviInfoUpdateEvent;
import com.huivip.gpsspeedwidget.beans.RoadLineEvent;
import com.huivip.gpsspeedwidget.beans.TMCSegmentEvent;
import com.huivip.gpsspeedwidget.model.SegmentModel;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.util.Tool;
import com.huivip.gpsspeedwidget.utils.CrashHandler;
import com.huivip.gpsspeedwidget.view.DigtalView;
import com.huivip.gpsspeedwidget.view.TmcSegmentView;
import com.huivip.gpsspeedwidget.widget.SpeedNumberVerticalWidget;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.x;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author sunlaihui
 */
public class SpeedNumberVerticalService extends Service {
    public static final String EXTRA_AUTOBOOT = "com.huivip.gpsspeedwidget.EXTRA_AUTOBOOT";
    public static final String EXTRA_CLOSE= "com.huivip.gpsspeedwidget.EXTRA_CLOSE";
    GpsUtil gpsUtil;
    AppWidgetManager manager;
    AppWidgetHost appWidgetHost;
    RemoteViews numberRemoteViews;
    TimerTask locationScanTask;
    boolean tempMute = false;
    boolean aimlessNaviStarted = false;
    boolean naviStarted=false;
    Timer locationTimer = new Timer();
    ComponentName numberWidget;
    DecimalFormat decimalFormat=new DecimalFormat("0.0");

    @Override
    public void onCreate() {
        gpsUtil = GpsUtil.getInstance(getApplicationContext());
        if(!gpsUtil.isGpsLocationStarted()){
            gpsUtil.startLocationService();
        }
        this.numberWidget = new ComponentName(this, SpeedNumberVerticalWidget.class);
        this.manager = AppWidgetManager.getInstance(this);
        this.locationScanTask = new TimerTask() {
            @Override
            public void run() {
                x.task().autoPost(new Runnable() {
                    @Override
                    public void run() {
                        SpeedNumberVerticalService.this.computeAndShowData();
                    }
                });
            }
        };
        appWidgetHost = new AppWidgetHost(getApplicationContext(), Constant.APP_WIDGET_HOST_ID);
        CrashHandler.getInstance().init(getApplicationContext());
        this.locationTimer.schedule(this.locationScanTask, 0L, 500L);
        EventBus.getDefault().register(this);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //gpsUtil.startLocationService();
        if(intent!=null && intent.getBooleanExtra(EXTRA_CLOSE,false)){
            stopSelf();
            return super.onStartCommand(intent,flags,startId);
        }
        this.numberRemoteViews = new RemoteViews(getPackageName(), R.layout.speed_number_vertical_widget);
        this.numberRemoteViews.setImageViewBitmap(R.id.image_speed_v, getSpeedBitmap("0",AppSettings.get().getSpeedVerticalWidgetSpeedTextColor()));
        this.manager.updateAppWidget(this.numberWidget, this.numberRemoteViews);
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        if (this.locationTimer != null) {
            this.locationTimer.cancel();
            this.locationTimer.purge();
            this.locationTimer = null;
        }
        if (gpsUtil != null) {
            gpsUtil.destory();
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    void checkLocationData() {
      /*  if (gpsUtil.isGpsEnabled() && gpsUtil.isGpsLocationStarted()) {
         if (gpsUtil.isGpsLocationChanged()) {*/
                computeAndShowData();
       /*    }
        }*/
    }
    @Subscribe
    public void updateNaviStatus(AutoMapStatusUpdateEvent event){
        this.naviStarted=event.isDaoHangStarted();
        this.numberRemoteViews = new RemoteViews(getPackageName(), R.layout.speed_number_vertical_widget);
        if(naviStarted){
            numberRemoteViews.setViewVisibility(R.id.v_navi_layout,View.VISIBLE);
        } else {
            numberRemoteViews.setViewVisibility(R.id.v_navi_layout,View.GONE);
        }
        this.manager.updateAppWidget(this.numberWidget, this.numberRemoteViews);
    }
    @Subscribe
    public void updateAimessStatus(AimlessStatusUpdateEvent event) {
        aimlessNaviStarted = event.isStarted();
    }

    @Subscribe
    public void setTempMute(AudioTempMuteEvent event) {
        this.tempMute = event.isMute();
    }
    @Subscribe
    public void updateNaviInfo(NaviInfoUpdateEvent event){
        if(gpsUtil.getAutoNaviStatus()!=Constant.Navi_Status_Started) return;
        this.numberRemoteViews = new RemoteViews(getPackageName(), R.layout.speed_number_vertical_widget);
        if (event.getLimitDistance()> 0) {
            this.numberRemoteViews.setTextViewText(R.id.textView_distance_v,event.getLimitDistance()+"米" );
            int limitDistancePercentage=0;
            if (event.getLimitDistance()<300 && event.getLimitDistance()>0) {
                limitDistancePercentage = Math.round((300F -event.getLimitDistance() ) / 300 * 100);
            }
            this.numberRemoteViews.setViewVisibility(R.id.v_edog_camera,View.VISIBLE);
            this.numberRemoteViews.setProgressBar(R.id.progressBarLimit_v, 100, limitDistancePercentage, false);
        } else {
            this.numberRemoteViews.setViewVisibility(R.id.v_edog_camera,View.GONE);
            this.numberRemoteViews.setTextViewText(R.id.textView_distance_v, gpsUtil.getDistance()+"/"+gpsUtil.getDriveTimeString());
            this.numberRemoteViews.setProgressBar(R.id.progressBarLimit_v, 100, 0, false);
        }

        if (TextUtils.isEmpty(event.getCurRoadName())) {
            this.numberRemoteViews.setTextViewText(R.id.v_current_road_name_v, "");
        } else {
            this.numberRemoteViews.setTextViewText(R.id.v_current_road_name_v, event.getCurRoadName());
        }
        if(event.getCameraSpeed()>0) {
            this.numberRemoteViews.setTextViewText(R.id.number_limit_v, event.getCameraSpeed() + "");
        } else {
            this.numberRemoteViews.setTextViewText(R.id.number_limit_v, "0");
        }
        if(tempMute){
            this.numberRemoteViews.setViewVisibility(R.id.v_edog_mute,View.VISIBLE);
        } else {
            this.numberRemoteViews.setViewVisibility(R.id.v_edog_mute,View.GONE);
        }
        if(gpsUtil.getAutoNaviStatus()==Constant.Navi_Status_Started){
            numberRemoteViews.setViewVisibility(R.id.v_navi_layout,View.VISIBLE);
            this.numberRemoteViews.setTextViewText(R.id.textView_nextRoadName_v,event.getNextRoadName());
            String distance=event.getSegRemainDis()+"米后";
            if(event.getSegRemainDis()>1000){
                if(event.getSegRemainDis()>1000){
                    distance= decimalFormat.format((float)event.getSegRemainDis()/1000)+ "公里后";
                }
            }
            this.numberRemoteViews.setTextViewText(R.id.textView_nextdistance_v,distance);
            this.numberRemoteViews.setImageViewResource(R.id.imageView_turnicon_v,getTurnIcon(event.getIcon()));
        } else {
            numberRemoteViews.setViewVisibility(R.id.v_navi_layout,View.GONE);
        }
        this.manager.updateAppWidget(this.numberWidget, this.numberRemoteViews);
    }

    public void setSpeeding(boolean speeding) {
        int colorRes = speeding ? ContextCompat.getColor(this, R.color.red500): AppSettings.get().getSpeedVerticalWidgetSpeedTextColor();
       // int color = ContextCompat.getColor(this, colorRes);
        this.numberRemoteViews.setImageViewBitmap(R.id.image_speed_v, getSpeedBitmap(gpsUtil.getKmhSpeedStr()+"",colorRes));
    }

    void computeAndShowData() {
        this.numberRemoteViews = new RemoteViews(getPackageName(), R.layout.speed_number_vertical_widget);
        setSpeeding(gpsUtil.isHasLimited());
        this.numberRemoteViews.setTextViewText(R.id.v_current_road_name_v,gpsUtil.getCurrentRoadName());
        this.numberRemoteViews.setTextViewText(R.id.v_direction,"\u2191 "+gpsUtil.getDirection());
        if(gpsUtil.getAutoNaviStatus()==Constant.Navi_Status_Started){
            numberRemoteViews.setViewVisibility(R.id.v_navi_layout,View.VISIBLE);
        } else {
            numberRemoteViews.setViewVisibility(R.id.v_navi_layout,View.GONE);
            if(gpsUtil.getLimitDistance()>0){
                this.numberRemoteViews.setTextViewText(R.id.textView_distance_v, gpsUtil.getLimitDistance() + "米");
                this.numberRemoteViews.setProgressBar(R.id.progressBarLimit_v, 100, gpsUtil.getLimitDistancePercentage(), false);
                this.numberRemoteViews.setViewVisibility(R.id.v_edog_camera,View.VISIBLE);
            } else {
                this.numberRemoteViews.setViewVisibility(R.id.v_edog_camera,View.GONE);
                this.numberRemoteViews.setTextViewText(R.id.textView_distance_v, gpsUtil.getDistance() + "/"+gpsUtil.getDriveTimeString());
                this.numberRemoteViews.setProgressBar(R.id.progressBarLimit_v, 100, 0, false);
            }
            this.numberRemoteViews.setTextViewText(R.id.number_limit_v, gpsUtil.getLimitSpeed()+ "");
        }
        this.manager.updateAppWidget(this.numberWidget, this.numberRemoteViews);
    }
    @Subscribe
    public void showRoadLine(RoadLineEvent event) {
        this.numberRemoteViews = new RemoteViews(getPackageName(), R.layout.speed_number_vertical_widget);
        if (event.isShowed()) {
            ImageView vv = (ImageView) event.getRoadLineView();
            this.numberRemoteViews.setImageViewBitmap(R.id.image_roadLine_v, Tool.drawableToBitmap(vv.getDrawable()));
        } else {
            this.numberRemoteViews.setImageViewBitmap(R.id.image_roadLine_v, null);
        }
        this.manager.updateAppWidget(this.numberWidget, this.numberRemoteViews);
    }
    private Bitmap getSpeedBitmap(String text, int color) {
        Bitmap bitmap = null;
        View view = View.inflate(getApplicationContext(), R.layout.view_widget_number, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        DigtalView timeView = view.findViewById(R.id.v_widget_number);
        timeView.setText(text);
        timeView.setTextColor(color);
        timeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 60 + Integer.parseInt(AppSettings.get().getSpeedVerticalWidgetSpeedTextSize()));
        view.measure(view.getMeasuredWidth(), view.getMeasuredHeight());
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        bitmap = view.getDrawingCache();
        return bitmap;
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onTmcSegmentUpdateEvent(final TMCSegmentEvent event) {
        String info = event.getInfo();
        if (TextUtils.isEmpty(info)) {
            return;
        }
        List<SegmentModel> models = new ArrayList<>();
        try {
            JSONObject tmc = new JSONObject(info);
            if (tmc != null) {
                JSONArray segmentArray = tmc.getJSONArray("tmc_info");
                if (segmentArray != null && segmentArray.length() > 0) {
                    for (int i = 0; i < segmentArray.length(); i++) {
                        JSONObject obj = segmentArray.getJSONObject(i);
                        if (obj != null) {
                            models.add(new SegmentModel()
                                    .setDistance(obj.getInt("tmc_segment_distance"))
                                    .setStatus(obj.getInt("tmc_status"))
                                    .setNumber(obj.getInt("tmc_segment_number"))
                                    .setPercent(obj.getInt("tmc_segment_percent")));
                        }
                    }
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.numberRemoteViews = new RemoteViews(getPackageName(), R.layout.speed_number_vertical_widget);
        if (models.size() > 0) {
            Bitmap bitmap = getTmcBitmap(models);
           this.numberRemoteViews.setViewVisibility(R.id.v_tmc_view, View.VISIBLE);
            this.numberRemoteViews.setImageViewBitmap(R.id.v_tmc_view,bitmap);
        } else {
            this.numberRemoteViews.setViewVisibility(R.id.v_tmc_view, View.GONE);
        }
        this.manager.updateAppWidget(this.numberWidget, this.numberRemoteViews);
    }
    private Bitmap getTmcBitmap(List<SegmentModel> models) {
        View view = View.inflate(getApplicationContext(), R.layout.view_tmc, null);
        view.setDrawingCacheEnabled(true);
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        //view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TmcSegmentView tmcSegmentView = view.findViewById(R.id.v_segmentView);
        tmcSegmentView.setSegments(models);
      /*  Bitmap bitmap = Bitmap.createBitmap(300,100,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bitmap);
        view.draw(canvas);*/
        view.buildDrawingCache(true);
        Bitmap bitmap=view.getDrawingCache();
        if(bitmap==null){
            Log.d("huivip","getTmc view cache is null");
        }
        return bitmap;
    }
    private int getTurnIcon(int iconValue) {
        int returnValue = -1;
        switch (iconValue) {
            case 1:
                returnValue = R.drawable.sou0_night;
                break;
            case 2:
                returnValue = R.drawable.sou2_night;
                break;
            case 3:
                returnValue = R.drawable.sou3_night;
                break;
            case 4:
                returnValue = R.drawable.sou4_night;
                break;
            case 5:
                returnValue = R.drawable.sou5_night;
                break;
            case 6:
                returnValue = R.drawable.sou6_night;
                break;
            case 7:
                returnValue = R.drawable.sou7_night;
                break;
            case 8:
                returnValue = R.drawable.sou8_night;
                break;
            case 9:
                returnValue = R.drawable.sou9_night;
                break;
            case 10:
                returnValue = R.drawable.sou10_night;
                break;
            case 11:
                returnValue = R.drawable.sou11_night;
                break;
            case 12:
                returnValue = R.drawable.sou12_night;
                break;
            case 13:
                returnValue = R.drawable.sou13_night;
                break;
            case 14:
                returnValue = R.drawable.sou14_night;
                break;
            case 15:
                returnValue = R.drawable.sou15_night;
                break;
            case 16:
                returnValue = R.drawable.sou16_night;
                break;
            case 17:
                returnValue = R.drawable.sou17_night;
                break;
            case 18:
                returnValue = R.drawable.sou18_night;
                break;
            case 19:
                returnValue = R.drawable.sou19_night;
                break;
            case 20:
                returnValue = R.drawable.sou20_night;
                break;
        }
        return returnValue;
    }

}
