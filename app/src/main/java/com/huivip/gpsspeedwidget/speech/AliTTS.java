package com.huivip.gpsspeedwidget.speech;

import android.annotation.SuppressLint;
import android.content.Context;

import com.amap.api.navi.AMapNavi;
import com.huivip.gpsspeedwidget.util.AppSettings;

public class AliTTS extends TTSService {
    @SuppressLint("StaticFieldLeak")
    private static AliTTS tts=null;
    AMapNavi aMapNavi;

    private final Context context;
    public static AliTTS getInstance(Context context){
        if(tts==null){
            tts=new AliTTS(context);
        }
        return tts;
    }
    private AliTTS(Context context) {
        super(context);
        this.context=context;
        initTTS();
    }

    @Override
    public void speak(String text) {
        speak(text,false);
    }

    @Override
    public void speak(String text, boolean force) {
        aMapNavi.playTTS(text,force);
    }

    @Override
    public void stop() {

    }

    @Override
    public void speakNext() {

    }

    @Override
    public void synthesize(String text) {

    }

    @Override
    public void synthesize(String text, boolean force) {

    }

    @Override
    public void release() {
        aMapNavi.destroy();
    }

    @Override
    public void initTTS() {
        aMapNavi=AMapNavi.getInstance(context);
        aMapNavi.setUseInnerVoice(true,false);
        aMapNavi.startSpeak();
    }

    @Override
    public void auth() {

    }

    @Override
    public String createAudio(String text) {
        return null;
    }
}
