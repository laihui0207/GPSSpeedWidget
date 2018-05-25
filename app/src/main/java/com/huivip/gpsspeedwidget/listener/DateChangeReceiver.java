package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
        if(hour==0 && min==0){
            TTS tts=SpeechFactory.getInstance(context).getTTSEngine(PrefUtils.getTtsEngine(context));
            tts.speak("现在时间"+hour+"整");
            WeatherService.getInstance(context).getLocationCityWeather(true);
        }
    }
}
