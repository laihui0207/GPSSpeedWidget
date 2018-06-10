package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.huivip.gpsspeedwidget.WeatherService;
import com.huivip.gpsspeedwidget.speech.SpeechFactory;
import com.huivip.gpsspeedwidget.speech.TTS;
import com.huivip.gpsspeedwidget.utils.PrefUtils;

import java.util.Calendar;

public class DateChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        Log.d("huvivip","Get Time Tick:"+min);
        if(min==0 || min==30){
            TTS tts=SpeechFactory.getInstance(context).getTTSEngine(PrefUtils.getTtsEngine(context));
            String text="";
            if(min==0){
                text="整点报时：当前时间:"+hour+"点整";
            }
            else {
                text="半点报时：当前时间:"+hour+"点"+min+"分";
            }
            if(PrefUtils.isPlayTime(context)){
                tts.speak(text,true);
            }
            WeatherService.getInstance(context).searchWeather();
        }
    }
}
