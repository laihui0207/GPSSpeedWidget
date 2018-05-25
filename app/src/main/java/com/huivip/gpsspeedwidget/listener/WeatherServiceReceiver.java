package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.huivip.gpsspeedwidget.WeatherService;

public class WeatherServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        DateChangeReceiver receiver=new DateChangeReceiver();
        context.registerReceiver(receiver,new IntentFilter(Intent.ACTION_TIME_TICK));
       WeatherService.getInstance(context).getLocationCityWeather(true);
    }
}
