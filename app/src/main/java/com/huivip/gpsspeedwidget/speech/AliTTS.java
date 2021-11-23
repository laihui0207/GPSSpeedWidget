package com.huivip.gpsspeedwidget.speech;

import android.annotation.SuppressLint;
import android.content.Context;

import com.amap.api.maps.AMapException;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.NaviSetting;
import com.amap.api.navi.TTSPlayListener;
import com.amap.api.navi.enums.AMapNaviControlMusicVolumeMode;
import com.huivip.gpsspeedwidget.util.AppSettings;

public class AliTTS extends TTSService implements TTSPlayListener {
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
        if(aMapNavi!=null){
            aMapNavi.playTTS(text,force);
        }
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
        if(aMapNavi!=null){
            aMapNavi.destroy();
        }
    }

    @Override
    public void initTTS() {
        NaviSetting.updatePrivacyShow(context, true, true);
        NaviSetting.updatePrivacyAgree(context, true);
        try {
            aMapNavi=AMapNavi.getInstance(context);
        } catch (AMapException e) {
            e.printStackTrace();
        }
        if(aMapNavi!=null){
            aMapNavi.setUseInnerVoice(true,false);
            aMapNavi.addTTSPlayListener(this);
            aMapNavi.setListenToVoiceDuringCall(false);
            if(AppSettings.get().isAudioMusicDuck()) {
                aMapNavi.setControlMusicVolumeMode(AMapNaviControlMusicVolumeMode.MUSIC_VOLUME_MODE_DEPRESS);
            } else {
                aMapNavi.setControlMusicVolumeMode(AMapNaviControlMusicVolumeMode.MUSIC_VOLUME_MODE_PAUSE);
            }
            aMapNavi.startSpeak();
        }
    }

    @Override
    public void auth() {

    }

    @Override
    public String createAudio(String text) {
        return null;
    }

    @Override
    public void onPlayStart(String s) {

    }

    @Override
    public void onPlayEnd(String s) {

    }
}

