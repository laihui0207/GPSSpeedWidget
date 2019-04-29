package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.huivip.gpsspeedwidget.service.WeatherService;

public class WeatherServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        DateChangeReceiver receiver=new DateChangeReceiver();
        Log.d("huivip","Register Time tick!");
        context.getApplicationContext().registerReceiver(receiver,new IntentFilter(Intent.ACTION_TIME_TICK));

       WeatherService.getInstance(context).searchWeather();
    }
}
