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
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;

import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.beans.SearchWeatherEvent;
import com.huivip.gpsspeedwidget.beans.WeatherEvent;
import com.huivip.gpsspeedwidget.model.WeatherItem;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.ChinaDateUtil;
import com.huivip.gpsspeedwidget.utils.Utils;
import com.huivip.gpsspeedwidget.view.DigtalView;
import com.huivip.gpsspeedwidget.widget.TimeWidget_v;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeWidgetVerticalService extends Service {
    public static final String EXTRA_CLOSE="time.v.widget.close";
    DateFormat timeFormat=new SimpleDateFormat("HH:mm", Locale.CHINA);
    DateFormat weekFormat=new SimpleDateFormat("EEEE", Locale.CHINA);
    DateFormat dateFormat=new SimpleDateFormat("MM月dd日", Locale.CHINA);
    AppWidgetManager manager;
    ComponentName thisWidget;
    long updateTime;
    boolean weatherUpdated=false;
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
        this.thisWidget = new ComponentName(this, TimeWidget_v.class);
        EventBus.getDefault().register(this);
        if(!Utils.isServiceRunning(getApplicationContext(),WeatherService.class.getName())){
           Intent weatherService=new Intent(getApplicationContext(),WeatherService.class);
           startService(weatherService);
           Log.d("huivip","Widget Luanch weather service");
           EventBus.getDefault().post(new SearchWeatherEvent(false));
        }
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getBooleanExtra(EXTRA_CLOSE, false)) {
            onStop();
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
        EventBus.getDefault().post(new SearchWeatherEvent(false));
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
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void updateWeather(WeatherEvent event){
       RemoteViews weatherView = new RemoteViews(getPackageName(), R.layout.time_weather_v_widget);
       weatherView.setImageViewResource(R.id.image_weather_v, WeatherItem.getWeatherResId(event.getWeather()));
        int textSize=15+Integer.parseInt(AppSettings.get().getTimeWidgetOtherTextSize());
        if(!TextUtils.isEmpty(event.getCity())) {
            weatherView.setTextViewText(R.id.text_city_v, event.getCity());
            weatherView.setTextColor(R.id.text_city_v, AppSettings.get().getTimeWidgetOtherTextColor());
            weatherView.setTextViewTextSize(R.id.text_city_v, TypedValue.COMPLEX_UNIT_SP, textSize);
        }
        if(!TextUtils.isEmpty(event.getWeather())) {
            weatherView.setTextViewText(R.id.text_temperature_v, event.getWeather() + "/" + event.getTemperature() + "\u2103  ");
            weatherView.setTextColor(R.id.text_temperature_v, AppSettings.get().getTimeWidgetOtherTextColor());
            weatherView.setTextViewTextSize(R.id.text_temperature_v, TypedValue.COMPLEX_UNIT_SP, textSize);
        }

       weatherView.setTextViewText(R.id.text_altitude_v,"海拔:"+event.getAltitude()+"米");
        weatherView.setTextColor(R.id.text_altitude_v,AppSettings.get().getTimeWidgetOtherTextColor());
        weatherView.setTextViewTextSize(R.id.text_altitude_v, TypedValue.COMPLEX_UNIT_SP,textSize);

       manager.updateAppWidget(thisWidget,weatherView);
       updateTime=System.currentTimeMillis();
       weatherUpdated=true;
    }
    private void updateView(){
        Date date=new Date();
       RemoteViews timeView = new RemoteViews(getPackageName(), R.layout.time_weather_v_widget);
        timeView.setImageViewBitmap(R.id.image_time_v, getBitmap(timeFormat.format(date)));

        int textSize=15+Integer.parseInt(AppSettings.get().getTimeWidgetOtherTextSize());
        timeView.setTextViewText(R.id.text_day_v,dateFormat.format(date));
        timeView.setTextColor(R.id.text_day_v,AppSettings.get().getTimeWidgetOtherTextColor());
        timeView.setTextViewTextSize(R.id.text_day_v,TypedValue.COMPLEX_UNIT_SP,textSize);

        timeView.setTextViewText(R.id.text_week_v,weekFormat.format(date));
        timeView.setTextColor(R.id.text_week_v,AppSettings.get().getTimeWidgetOtherTextColor());
        timeView.setTextViewTextSize(R.id.text_week_v,TypedValue.COMPLEX_UNIT_SP,textSize);

        timeView.setTextViewText(R.id.text_chinaDate_v, new ChinaDateUtil(Calendar.getInstance()).toString());
        timeView.setTextColor(R.id.text_chinaDate_v,AppSettings.get().getTimeWidgetOtherTextColor());
        timeView.setTextViewTextSize(R.id.text_chinaDate_v,TypedValue.COMPLEX_UNIT_SP,textSize);

        timeView.setTextColor(R.id.text_altitude_v,AppSettings.get().getTimeWidgetOtherTextColor());
        timeView.setTextViewTextSize(R.id.text_altitude_v,TypedValue.COMPLEX_UNIT_SP,textSize);

        manager.updateAppWidget(thisWidget,timeView);
        if(!weatherUpdated || System.currentTimeMillis()-updateTime > 10*60*1000 ){
            EventBus.getDefault().post(new SearchWeatherEvent(false));
        }
    }
    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
           updateView();
        }

    };
    private Bitmap getBitmap(String text) {
        Bitmap bitmap = null;
        View view = View.inflate(getApplicationContext(),R.layout.view_widget_number, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        DigtalView timeView=view.findViewById(R.id.v_widget_number);
        timeView.setText(text);
        timeView.setTextColor(AppSettings.get().getTimeWidgetTimeTextColor());
        timeView.setTextSize(TypedValue.COMPLEX_UNIT_SP,50+Integer.parseInt(AppSettings.get().getTimeWidgetTimeTextSize()));
        view.measure(view.getMeasuredWidth(),view.getMeasuredHeight());
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());;
        view.buildDrawingCache();
        bitmap = view.getDrawingCache();
        return bitmap;
    }
}
