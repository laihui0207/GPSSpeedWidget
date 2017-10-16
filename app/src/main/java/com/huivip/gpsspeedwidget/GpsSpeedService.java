package com.huivip.gpsspeedwidget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author sunlaihui
 */
public class GpsSpeedService extends Service {
    String latitudine;
    String longitudine;
    String velocita;
    Integer velocita2;
    AppWidgetManager manager;
    LocationListener myLocationListener;
    String providerId = "gps";
    RemoteViews remoteViews;
    TimerTask scanTask;
    Double speed2;
    boolean stato = false;
    Timer timer = new Timer();
    ComponentName thisWidget;
    final Handler handler = new Handler();
    Integer velocita2_prec = Integer.valueOf(62536);
    Integer ris_prec = Integer.valueOf(62536);
    String velocita_prec = "ciao";
    Integer c = Integer.valueOf(0);
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("GPS widget","Service Create .......................");
        this.stato = false;
        this.remoteViews = new RemoteViews(getPackageName(), R.layout.speedwidget);
        this.thisWidget = new ComponentName(this, GpsSpeedWidget.class);
        this.manager = AppWidgetManager.getInstance(this);
        /*this.scanTask = new TimerTask()
        {
            @Override
            public void run()
            {
                GpsSpeedService.this.handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        GpsSpeedService.this.aggiornamento_tot();
                    }
                });
            }
        };
        this.timer.schedule(this.scanTask, 0L, 100L);*/
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("GpsWidget","Service Start--------------------");
        /*boolean bool;
        if (!this.stato)
        {
            bool = true;
            this.stato = bool;
            this.myLocationListener = new LocationListener()
            {
                @Override
                public void onLocationChanged(Location paramAnonymousLocation)
                {
                    GpsSpeedService.this.updateLocationData(paramAnonymousLocation);
                    if (GpsSpeedService.this.stato) {}
                }

                @Override
                public void onProviderDisabled(String paramAnonymousString) {}

                @Override
                public void onProviderEnabled(String paramAnonymousString) {}

                @Override
                public void onStatusChanged(String paramAnonymousString, int paramAnonymousInt, Bundle paramAnonymousBundle)
                {
                    if (paramAnonymousInt == 2) {}
                }
            };
            if (!this.stato) {
                spento();
                return START_REDELIVER_INTENT;
               // break label43;
            }
            aggiornamento_tot();
        }*/
        /*for (;;)
        {
            return 0;
            bool = false;
            break;
            *//*label43:
            spento();*//*
        }*/
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private void updateLocationData(Location paramLocation)
    {

        this.latitudine = Double.toString((int)paramLocation.getLatitude());
        this.velocita = null;
        Log.d("GPSWidget","Get GPS info......");
        if (paramLocation.hasSpeed())
        {
            Log.d("GPSWidget","GPS Info: have speed");
            this.velocita2 = Integer.valueOf((int)paramLocation.getSpeed());
            NumberFormat localNumberFormat = NumberFormat.getNumberInstance();
            localNumberFormat.setMaximumFractionDigits(1);
            this.speed2 = Double.valueOf(paramLocation.getSpeed());
            this.velocita = localNumberFormat.format(this.speed2);
        }
    }
    void aggiornamento_tot()
    {
        LocationManager locationManager = (LocationManager)getSystemService("location");
        if (locationManager.getProvider(this.providerId) == null) {
            Log.d("GPS","No location provider");
            return;
        }
        boolean bool = locationManager.isProviderEnabled(this.providerId);
        for (;;)
        {
            Location localLocation = locationManager.getLastKnownLocation("gps");
            if (localLocation != null) {
                Log.d("GPS widget","Location have done!");
                updateLocationData(localLocation);
            }
            locationManager.requestLocationUpdates(this.providerId, 1L, 1.0F, this.myLocationListener);
            if ((this.velocita != null) && (bool))
            {
                if (!this.velocita.equals(this.velocita_prec)) {
                    aggiornamento();
                }
                this.velocita_prec = this.velocita;
            }
            if ((this.latitudine != null) || (!bool)) {
                break;
            }
            int localInt = this.c;
            this.c = Integer.valueOf(this.c.intValue() + 1);
            localInt = Integer.valueOf(this.c.intValue() / 50);
            if (!((Integer)localInt).equals(this.ris_prec)) {
                Toast.makeText(getApplicationContext(), "...waiting GPS signal...", 0).show();
            }
            this.ris_prec = ((Integer)localInt);
            this.remoteViews.setTextViewText(R.id.textView1, "  WAIT");
            this.manager.updateAppWidget(this.thisWidget, this.remoteViews);
            break;
/*            spento();*/
        }
    }
    void aggiornamento(){
        Log.d("GPS","Compute GPS speed");
        NumberFormat localNumberFormat = NumberFormat.getNumberInstance();
        localNumberFormat.setMaximumFractionDigits(1);
        int mphNumber = (int)(this.velocita2.intValue() * 3.6D / 1.609344D);
        int kmhNumber = (int)(this.velocita2.intValue() * 3.6D);
        this.remoteViews.setTextViewText(R.id.textView1, localNumberFormat.format(mphNumber));
        this.remoteViews.setTextViewText(R.id.textView1Mph, "Mph");
        this.remoteViews.setTextViewText(R.id.textView1Kmh, "Km/h");
        this.remoteViews.setTextViewText(R.id.textView1_1, localNumberFormat.format(kmhNumber));
        Log.d("GPS","Compute GPS speed:"+kmhNumber);
        /*switch (mphNumber)
        {
            default:
                if (mphNumber > 140) {
                    this.remoteViews.setImageViewResource(R.id.ifreccia,R.drawable.alt_150);
                }
                switch (mphNumber)
                {
                }
                break;
        }*/
    }
    void spento()
    {
        this.timer.cancel();
        stopSelf();
        this.remoteViews.setTextViewText(R.id.textView1, "   OFF");
        this.remoteViews.setTextViewText(R.id.textView1Mph, "");
        this.remoteViews.setTextViewText(R.id.textView1_1, "");
        this.remoteViews.setTextViewText(R.id.textView1Kmh, "");
        this.remoteViews.setImageViewResource(R.id.ialtimetro,R.drawable.base);
        this.remoteViews.setImageViewResource(R.id.ifreccia,R.drawable.alt_0);
        this.manager.updateAppWidget(this.thisWidget, this.remoteViews);
    }

}
