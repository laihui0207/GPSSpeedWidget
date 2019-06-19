package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.huivip.gpsspeedwidget.beans.SearchWeatherEvent;

import org.greenrobot.eventbus.EventBus;

public class WeatherServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        DateChangeReceiver receiver=new DateChangeReceiver();
        context.getApplicationContext().registerReceiver(receiver,new IntentFilter(Intent.ACTION_TIME_TICK));
        EventBus.getDefault().post(new SearchWeatherEvent());
    }
}
