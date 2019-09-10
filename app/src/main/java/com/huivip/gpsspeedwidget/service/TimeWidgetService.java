package com.huivip.gpsspeedwidget.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;

import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.beans.WeatherEvent;
import com.huivip.gpsspeedwidget.utils.ChinaDateUtil;
import com.huivip.gpsspeedwidget.view.DigtalView;
import com.huivip.gpsspeedwidget.widget.TimeWidget;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeWidgetService extends Service {
    public static final String EXTRA_CLOSE="lyric.widget.close";
    DateFormat timeFormat=new SimpleDateFormat("HH:mm", Locale.CHINA);
    DateFormat weekFormat=new SimpleDateFormat("E", Locale.CHINA);
    DateFormat dateFormat=new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
    AppWidgetManager manager;
    ComponentName thisWidget;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onCreate() {
        this.manager = AppWidgetManager.getInstance(this);
        getApplicationContext().registerReceiver(myBroadcastReceiver,new IntentFilter(Intent.ACTION_TIME_TICK));
        this.thisWidget = new ComponentName(this, TimeWidget.class);
        EventBus.getDefault().register(this);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getBooleanExtra(EXTRA_CLOSE, false)) {
            onStop();
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
        updateView();
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
        getApplicationContext().unregisterReceiver(myBroadcastReceiver);
    }
    public void onStop(){

    }
    @Subscribe
    public void updateWeather(WeatherEvent event){
       RemoteViews weatherView = new RemoteViews(getPackageName(), R.layout.time_weather_widget);
       weatherView.setTextViewText(R.id.text_city,event.getCity()+"   "+event.getWeather());
       weatherView.setTextViewText(R.id.text_temperature,"温度:"+event.getTemperature());
       weatherView.setTextViewText(R.id.text_altitude,"海拔:"+event.getAltitude());
       manager.updateAppWidget(thisWidget,weatherView);
    }
    private void updateView(){
        Date date=new Date();
       RemoteViews timeView = new RemoteViews(getPackageName(), R.layout.time_weather_widget);
        timeView.setTextViewText(R.id.text_day,dateFormat.format(date));
        timeView.setImageViewBitmap(R.id.image_time, getBitmap(timeFormat.format(date)));
        timeView.setTextViewText(R.id.text_week,weekFormat.format(date));
        timeView.setTextViewText(R.id.text_chinaDate, new ChinaDateUtil(Calendar.getInstance()).toString());
        manager.updateAppWidget(thisWidget,timeView);
    }
    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
           updateView();
            Log.d("huivip","update Time View");
        }

    };
    private Bitmap getBitmap(String text) {
        Bitmap bitmap = null;
        View view = View.inflate(getApplicationContext(),R.layout.view_widget_time, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        DigtalView directionView=view.findViewById(R.id.v_widget_time);
        directionView.setText(text);
        view.measure(view.getMeasuredWidth(),view.getMeasuredHeight());
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());;
        view.buildDrawingCache();
        bitmap = view.getDrawingCache();
        return bitmap;
    }
}
