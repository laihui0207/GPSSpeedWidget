package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.huivip.gpsspeedwidget.beans.ACCOnEvent;
import com.huivip.gpsspeedwidget.beans.GetDistrictEvent;
import com.huivip.gpsspeedwidget.beans.PlayAudioEvent;
import com.huivip.gpsspeedwidget.beans.SearchWeatherEvent;
import com.huivip.gpsspeedwidget.util.AppSettings;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;

public class DateChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        if(min==0 || min==30){
            String text="";
            if(min==0){
                text="整点报时：当前时间:"+hour+"点整";
            }
            else {
                text="半点报时：当前时间:"+hour+"点"+min+"分";
            }
            if(AppSettings.get().isPlayTime()) {
                EventBus.getDefault().post(new PlayAudioEvent(text, true));
            }
            EventBus.getDefault().post(new SearchWeatherEvent(true));
        }
        if(min%5==0){
            EventBus.getDefault().post(new GetDistrictEvent());
        }
        EventBus.getDefault().post(new ACCOnEvent().setFrom("DateChange"));
    }
}
