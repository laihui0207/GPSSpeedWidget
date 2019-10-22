package com.huivip.gpsspeedwidget.service;

import android.app.Service;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.GpsUtil;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.beans.AimlessStatusUpdateEvent;
import com.huivip.gpsspeedwidget.beans.AudioTempMuteEvent;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.CrashHandler;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;
import com.huivip.gpsspeedwidget.widget.GpsSpeedNumberWidget;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.xutils.x;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author sunlaihui
 */
public class GpsSpeedNumberService extends Service {
    public static final String EXTRA_AUTOBOOT = "com.huivip.gpsspeedwidget.EXTRA_AUTOBOOT";
    public static final String EXTRA_CLOSE = "com.huivip.gpsspeedwidget.EXTRA_CLOSE";
    GpsUtil gpsUtil;
    AppWidgetManager manager;
    AppWidgetHost appWidgetHost;
    RemoteViews numberRemoteViews;
    TimerTask locationScanTask;
    boolean tempMute=false;
    boolean serviceStoped = true;
    boolean aimlessNaviStarted=false;
    Timer locationTimer = new Timer();
    ComponentName numberWidget;

    @Override
    public void onCreate() {
        gpsUtil = GpsUtil.getInstance(getApplicationContext());
        this.numberWidget = new ComponentName(this, GpsSpeedNumberWidget.class);
        this.manager = AppWidgetManager.getInstance(this);
        this.locationScanTask = new TimerTask() {
            @Override
            public void run() {
                x.task().autoPost(new Runnable() {
                    @Override
                    public void run() {
                        GpsSpeedNumberService.this.checkLocationData();
                    }
                });
            }
        };
        appWidgetHost = new AppWidgetHost(getApplicationContext(), Constant.APP_WIDGET_HOST_ID);
        CrashHandler.getInstance().init(getApplicationContext());
        this.locationTimer.schedule(this.locationScanTask, 0L, 100L);
        EventBus.getDefault().register(this);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.numberRemoteViews = new RemoteViews(getPackageName(), R.layout.speednumberwidget);
        if (intent != null) {
            if(intent.getBooleanExtra(EXTRA_CLOSE,false)){
                stopSelf();
                return super.onStartCommand(intent,flags,startId);
            }
            if (intent.getBooleanExtra(EXTRA_AUTOBOOT, false) || serviceStoped) {
                if (serviceStoped) {
                    serviceStoped = false;
                    this.numberRemoteViews.setTextViewText(R.id.number_speed, "...");
                    this.numberRemoteViews.setTextViewText(R.id.number_limit, "...");
                    this.numberRemoteViews.setProgressBar(R.id.progressBar, 125, 0, false);
                    this.manager.updateAppWidget(this.numberWidget, this.numberRemoteViews);
                    this.numberRemoteViews = null;
                    gpsUtil.startLocationService();
                    PrefUtils.setEnableTempAudioService(getApplicationContext(), true);
                    if(PrefUtils.isUserManualClosedService(getApplicationContext())) {
                        Utils.startFloatingWindows(getApplicationContext(),true);
                        PrefUtils.setUserManualClosedServer(getApplicationContext(), false);
                    }
                }
            } else {
                serviceStoped = true;
                this.numberRemoteViews.setTextViewText(R.id.number_speed, "关");
                this.numberRemoteViews.setProgressBar(R.id.progressBar, 125, 0, false);
                this.manager.updateAppWidget(this.numberWidget, this.numberRemoteViews);
                this.numberRemoteViews = null;

                gpsUtil.stopLocationService(true);
                if (this.locationTimer != null) {
                    this.locationTimer.cancel();
                    this.locationTimer.purge();
                    this.locationTimer = null;
                }
                Utils.startFloatingWindows(getApplicationContext(),false);
                PrefUtils.setUserManualClosedServer(getApplicationContext(), true);
                stopSelf();
            }


        }

        return Service.START_REDELIVER_INTENT; // super.onStartCommand(intent,Service.START_FLAG_REDELIVERY,startId);
    }

    @Override
    public void onDestroy() {
        if (this.locationTimer != null) {
            this.locationTimer.cancel();
            this.locationTimer.purge();
            this.locationTimer = null;
        }
        if(gpsUtil!=null){
            gpsUtil.destory();
        }
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    void checkLocationData() {
        if (gpsUtil.isGpsEnabled() && gpsUtil.isGpsLocationStarted()) {
            if (gpsUtil.isGpsLocationChanged()) {
                computeAndShowData();
            }
        }
        this.numberRemoteViews = new RemoteViews(getPackageName(), R.layout.speednumberwidget);
        if(aimlessNaviStarted){
            this.numberRemoteViews.setImageViewResource(R.id.image_xunhang_switch,R.drawable.ic_xunhang_enable);
            if(tempMute){
                this.numberRemoteViews.setImageViewResource(R.id.image_xunhang_switch,R.drawable.ic_xunhang_mute);

            }
        } else {
            this.numberRemoteViews.setImageViewResource(R.id.image_xunhang_switch,R.drawable.ic_xunhang_disable);
        }
        if(AppSettings.get().isLyricEnable()){
            this.numberRemoteViews.setImageViewResource(R.id.image_gas_station,R.drawable.lyric);
        } else {
            this.numberRemoteViews.setImageViewResource(R.id.image_gas_station,R.drawable.lyric_disabled);
        }
        this.manager.updateAppWidget(this.numberWidget, this.numberRemoteViews);
    }
    @Subscribe
    public void updateAimessStatus(AimlessStatusUpdateEvent event){
        aimlessNaviStarted=event.isStarted();
    }
    @Subscribe
    public void setTempMute(AudioTempMuteEvent event){
        this.tempMute = event.isMute();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    public void setSpeeding(boolean speeding) {
        int colorRes = speeding ? R.color.red500 : R.color.primary_text_default_material_dark;
        int color = ContextCompat.getColor(this, colorRes);
        this.numberRemoteViews.setTextColor(R.id.number_speed, color);
    }

    void computeAndShowData() {
        this.numberRemoteViews = new RemoteViews(getPackageName(), R.layout.speednumberwidget);
        setSpeeding(gpsUtil.isHasLimited());
        if (gpsUtil.getCameraType() > -1) {
            this.numberRemoteViews.setTextViewText(R.id.textView_limit_label, gpsUtil.getCameraTypeName());
        } else {
            this.numberRemoteViews.setTextViewText(R.id.textView_limit_label, "限速");
        }

        this.numberRemoteViews.setTextViewText(R.id.textView_direction, gpsUtil.getDirection() + "");
        if (gpsUtil.getLimitDistance() > 0) {
            this.numberRemoteViews.setTextViewText(R.id.textView_distance, gpsUtil.getLimitDistance() + "米");
        } else {
            this.numberRemoteViews.setTextViewText(R.id.textView_distance, gpsUtil.getDistance() + "");
        }

        if (TextUtils.isEmpty(gpsUtil.getCurrentRoadName())) {
            this.numberRemoteViews.setTextViewText(R.id.textView_unit, "km/h");
        } else {
            this.numberRemoteViews.setTextViewText(R.id.textView_unit, gpsUtil.getCurrentRoadName());
        }

        this.numberRemoteViews.setProgressBar(R.id.progressBarLimit, 100, gpsUtil.getLimitDistancePercentage(), false);
        this.numberRemoteViews.setProgressBar(R.id.progressBar, 125, gpsUtil.getSpeedometerPercentage(), false);
        this.numberRemoteViews.setTextViewText(R.id.number_speed, gpsUtil.getKmhSpeedStr() + "");
        this.numberRemoteViews.setTextViewText(R.id.number_limit, gpsUtil.getLimitSpeed() + "");
        this.manager.updateAppWidget(this.numberWidget, this.numberRemoteViews);
        this.numberRemoteViews = null;
    }
}
