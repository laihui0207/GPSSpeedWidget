package com.huivip.gpsspeedwidget.service;

import android.app.Service;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;

import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.GpsUtil;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.utils.CrashHandler;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;
import com.huivip.gpsspeedwidget.view.MeterWheel;
import com.huivip.gpsspeedwidget.widget.GpsSpeedMeterWidget;

import org.xutils.x;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author sunlaihui
 */
public class GpsSpeedMeterService extends Service {
    static final int MAX_VELOCITA_NUMBER = 140;
    public static final String EXTRA_AUTOBOOT = "com.huivip.gpsspeedwidget.EXTRA_AUTOBOOT";
    public static final String EXTRA_CLOSE= "com.huivip.gpsspeedwidget.EXTRA_CLOSE";
    GpsUtil gpsUtil;
    AppWidgetManager manager;
    AppWidgetHost appWidgetHost;
    RemoteViews remoteViews;
    TimerTask locationScanTask;
    boolean serviceStoped = true;
    Timer locationTimer = new Timer();
    ComponentName thisWidget;

    @Override
    public void onCreate() {
        gpsUtil = GpsUtil.getInstance(getApplicationContext());
        this.thisWidget = new ComponentName(this, GpsSpeedMeterWidget.class);
        this.manager = AppWidgetManager.getInstance(this);
        this.locationScanTask = new TimerTask() {
            @Override
            public void run() {
                x.task().autoPost(new Runnable() {
                    @Override
                    public void run() {
                        GpsSpeedMeterService.this.checkLocationData();
                    }
                });
            }
        };
        appWidgetHost = new AppWidgetHost(getApplicationContext(), Constant.APP_WIDGET_HOST_ID);
        CrashHandler.getInstance().init(getApplicationContext());
        this.locationTimer.schedule(this.locationScanTask, 0L, 100L);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.remoteViews = new RemoteViews(getPackageName(), R.layout.speedmeterwidget);
        if (intent != null) {
            if(intent.getBooleanExtra(EXTRA_CLOSE,false)){
                stopSelf();
                return super.onStartCommand(intent,Service.START_FLAG_REDELIVERY,startId);
            }
            if (intent.getBooleanExtra(EXTRA_AUTOBOOT, false) || serviceStoped) {
                if (serviceStoped) {
                    serviceStoped = false;
                    this.remoteViews.setTextViewText(R.id.textView1_watch_speed, "...");
                    this.manager.updateAppWidget(this.thisWidget, this.remoteViews);
                    this.remoteViews = null;
                    gpsUtil.startLocationService();
                    PrefUtils.setEnableTempAudioService(getApplicationContext(), true);
                    if(PrefUtils.isUserManualClosedService(getApplicationContext())) {
                        Utils.startFloatingWindows(getApplicationContext(),true);
                        PrefUtils.setUserManualClosedServer(getApplicationContext(), false);
                    }
                }
            } else {
                serviceStoped = true;
                this.remoteViews.setTextViewText(R.id.textView1_watch_speed, "关");
                this.remoteViews.setImageViewResource(R.id.ialtimetro, R.drawable.base);
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_0);
                this.manager.updateAppWidget(this.thisWidget, this.remoteViews);
                this.remoteViews = null;

                gpsUtil.stopLocationService(true);
                if (this.locationTimer != null) {
                    this.locationTimer.cancel();
                    this.locationTimer.purge();
                    this.locationTimer = null;
                }
                Utils.startFloatingWindows(getApplicationContext(),false);
                PrefUtils.setUserManualClosedServer(getApplicationContext(), true);
                //Toast.makeText(getApplicationContext(), "GPS服务关闭", Toast.LENGTH_SHORT).show();
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
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    void checkLocationData() {
      /*  if (gpsUtil.isGpsEnabled() && gpsUtil.isGpsLocationStarted()) {
            if (gpsUtil.isGpsLocationChanged()){ */
                computeAndShowData();
      /*      }
        }*/
    }

    public void setSpeeding(boolean speeding) {
        int colorRes = speeding ? R.color.red500 : R.color.primary_text_default_material_dark;
        int color = ContextCompat.getColor(this, colorRes);
        this.remoteViews.setTextColor(R.id.textView1_watch_speed, color);
    }
    private Bitmap getSpeedBitmap() {
        Bitmap bitmap = null;
        View view = View.inflate(getApplicationContext(), R.layout.view_speed, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        MeterWheel meterView = view.findViewById(R.id.v_meter_View);
        meterView.setRotation((float)(gpsUtil.getSpeedometerPercentage()/100d*252f));
        //meterView.setRotation(50f);
        view.measure(view.getMeasuredWidth(), view.getMeasuredHeight());
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        bitmap = view.getDrawingCache();
        return bitmap;
    }
    void computeAndShowData() {
        this.remoteViews = new RemoteViews(getPackageName(), R.layout.speedmeterwidget);
        int mphNumber = gpsUtil.getMphSpeed().intValue();
        setSpeeding(gpsUtil.isHasLimited());
        this.remoteViews.setTextViewText(R.id.textView1_watch_speed, gpsUtil.getKmhSpeedStr() + "");
        this.remoteViews.setTextViewText(R.id.textView_watch_limit, gpsUtil.getLimitSpeed() + "");
        this.remoteViews.setTextViewText(R.id.textView_watch_direction, gpsUtil.getDirection() + "");
        this.remoteViews.setImageViewBitmap(R.id.ifreccia_all,getSpeedBitmap());
       /* switch (mphNumber) {
            default:
                if (mphNumber > MAX_VELOCITA_NUMBER) {
                    this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_150);
                }
                break;
            case 0:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_0);
                break;
            case 1:
            case 2:
            case 3:
            case 4:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_4);
                break;
            case 5:
            case 6:
            case 7:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_7);
                break;
            case 8:
            case 9:
            case 10:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_10);
                break;
            case 11:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_11);
                break;
            case 12:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_12);
                break;
            case 13:
            case 14:
            case 15:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_15);
                break;
            case 16:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_16);
                break;
            case 17:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_17);
                break;
            case 18:
            case 19:
            case 20:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_20);
                break;
            case 21:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_21);
                break;
            case 22:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_22);
                break;
            case 23:
            case 24:
            case 25:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_25);
                break;
            case 26:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_26);
                break;
            case 27:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_27);
                break;
            case 28:
            case 29:
            case 30:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_30);
                break;
            case 31:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_31);
                break;
            case 32:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_32);
                break;
            case 33:
            case 34:
            case 35:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_35);
                break;
            case 36:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_36);
                break;
            case 37:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_37);
                break;
            case 38:
            case 39:
            case 40:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_40);
                break;
            case 41:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_41);
                break;
            case 42:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_42);
                break;
            case 43:
            case 44:
            case 45:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_45);
                break;
            case 46:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_46);
                break;
            case 47:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_47);
                break;
            case 48:
            case 49:
            case 50:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_50);
                break;
            case 51:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_51);
                break;
            case 52:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_52);
                break;
            case 53:
            case 54:
            case 55:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_55);
                break;
            case 56:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_56);
                break;
            case 57:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_57);
                break;
            case 58:
            case 59:
            case 60:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_60);
                break;
            case 61:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_61);
                break;
            case 62:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_62);
                break;
            case 63:
            case 64:
            case 65:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_65);
                break;
            case 66:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_66);
                break;
            case 67:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_67);
                break;
            case 68:
            case 69:
            case 70:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_70);
                break;
            case 71:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_71);
                break;
            case 72:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_72);
                break;
            case 73:
            case 74:
            case 75:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_75);
                break;
            case 76:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_76);
                break;
            case 77:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_77);
                break;
            case 78:
            case 79:
            case 80:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_80);
                break;
            case 81:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_81);
                break;
            case 82:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_82);
                break;
            case 83:
            case 84:
            case 85:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_85);
                break;
            case 86:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_86);
                break;
            case 87:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_87);
                break;
            case 88:
            case 89:
            case 90:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_90);
                break;
            case 91:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_91);
                break;
            case 92:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_92);
                break;
            case 93:
            case 94:
            case 95:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_95);
                break;
            case 96:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_96);
                break;
            case 97:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_97);
                break;
            case 98:
            case 99:
            case 100:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_100);
                break;
            case 101:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_100);
                break;
            case 102:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_102);
                break;
            case 103:
            case 104:
            case 105:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_105);
                break;
            case 106:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_100);
                break;
            case 107:
            case 108:
            case 109:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_107);
                break;
            case 110:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_110);
                break;
            case 111:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_111);
                break;
            case 112:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_112);
                break;
            case 113:
            case 114:
            case 115:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_115);
                break;
            case 116:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_116);
                break;
            case 117:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_117);
                break;
            case 118:
            case 119:
            case 120:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_120);
                break;
            case 121:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_121);
                break;
            case 122:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_122);
                break;
            case 123:
            case 124:
            case 125:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_125);
                break;
            case 126:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_126);
                break;
            case 127:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_127);
                break;
            case 128:
            case 129:
            case 130:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_130);
                break;
            case 131:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_131);
                break;
            case 132:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_132);
                break;
            case 133:
            case 134:
            case 135:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_135);
                break;
            case 136:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_136);
                break;
            case 137:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_137);
                break;
            case 138:
            case 139:
            case 140:
                this.remoteViews.setImageViewResource(R.id.ifreccia_all, R.drawable.alt_140);
                break;
        }*/
        if (gpsUtil.getCameraType() > -1) {
            this.remoteViews.setTextViewText(R.id.textView_watch_limit_label, gpsUtil.getCameraTypeName());
        } else {
            this.remoteViews.setTextViewText(R.id.textView_watch_limit_label, "限速");
        }

        if (gpsUtil.getLimitSpeed() > 0 || gpsUtil.getCameraDistance()>0) {
            this.remoteViews.setTextViewText(R.id.watch_number_limit, gpsUtil.getLimitSpeed() + "");
            this.remoteViews.setProgressBar(R.id.watch_progressBarLimit, 100, gpsUtil.getLimitDistancePercentage(), false);
            this.remoteViews.setTextViewText(R.id.textView_watch_distance, gpsUtil.getLimitDistance() + "米");
            this.remoteViews.setViewVisibility(R.id.watch_limitLayout, View.VISIBLE);
        } else {
            this.remoteViews.setViewVisibility(R.id.watch_limitLayout, View.GONE);
        }
        this.manager.updateAppWidget(this.thisWidget, this.remoteViews);
        this.remoteViews = null;
    }
}
