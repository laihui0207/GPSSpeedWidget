package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.huivip.gpsspeedwidget.beans.SearchWeatherEvent;

import org.greenrobot.eventbus.EventBus;

public class WeatherServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        EventBus.getDefault().post(new SearchWeatherEvent());
    }
}
