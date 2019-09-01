package com.huivip.gpsspeedwidget.speech;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.huivip.gpsspeedwidget.beans.PlayAudioEvent;
import com.huivip.gpsspeedwidget.beans.TTSEngineChangeEvent;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.CrashHandler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class AudioService extends Service {
    TTS tts;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
       // tts=SpeechFactory.getInstance(getApplicationContext()).getTTSEngine(PrefUtils.getTtsEngine(getApplicationContext()));
        CrashHandler.getInstance().init(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(tts==null){
            tts=SpeechFactory.getInstance(getApplicationContext()).getTTSEngine(AppSettings.get().getAudioEngine());
        }
        return Service.START_REDELIVER_INTENT;
    }
    @Subscribe
    public void playTTS(PlayAudioEvent event){
        if(tts==null){
            tts=SpeechFactory.getInstance(getApplicationContext()).getTTSEngine(AppSettings.get().getAudioEngine());
        }
        if(AppSettings.get().isEnableAudio()) {
            tts.speak(event.getText(), event.isForce());
        }
    }
    @Subscribe
    public void changeTTSEngine(TTSEngineChangeEvent engineChangeEvent){
        tts.release();
        tts=SpeechFactory.getInstance(getApplicationContext()).getTTSEngine(AppSettings.get().getAudioEngine());
        tts.initTTS();
    }
    @Override
    public void onDestroy() {
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }
}