package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.GpsUtil;
import com.huivip.gpsspeedwidget.beans.SearchWeatherEvent;
import com.huivip.gpsspeedwidget.speech.TTS;
import com.huivip.gpsspeedwidget.utils.PrefUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;

public class DateChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        GpsUtil gpsUtil=GpsUtil.getInstance(context);
        if(intent.getAction().equals(Intent.ACTION_TIME_TICK)){
            gpsUtil.registTimeTickSuccess=true;
        }
        if(gpsUtil.registTimeTickSuccess && intent.getAction().equals(Constant.UPDATE_DATE_EVENT_ACTION)){
            return;
        }
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
            if(PrefUtils.isPlayTime(context)){
                TTS tts=gpsUtil.getTts();
                tts.speak(text,true);
            }
            EventBus.getDefault().post(new SearchWeatherEvent());
        }
    }
}
