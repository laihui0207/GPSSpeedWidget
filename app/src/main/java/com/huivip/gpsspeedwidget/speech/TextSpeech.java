package com.huivip.gpsspeedwidget.speech;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;

import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.PrefUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TextSpeech extends TTSService implements TextToSpeech.OnInitListener {
    private String TAG="huivip_TxtTTS";
    TextToSpeech mts;
    private boolean inited=false;
    static TextSpeech instance;
    private TextSpeech(Context context){
        super(context);
        if(mts==null){
            mts=new TextToSpeech(context,this);
        }
    }
    public static TextSpeech getInstance(Context context){
        if(instance==null){
            instance=new TextSpeech(context);
        }
        return instance;
    }
    @Override
    public void speak(String text) {
        speak(text,false);
    }

    @Override
    public void speak(String text, boolean force) {
        if(inited) {
            if (AppSettings.get().isEnableAudio() && (force || PrefUtils.isEnableTempAudioService(context))) {
                //beforeSpeak();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Map<String,String> params=new HashMap<>();
                    params.put("KEY_PARAM_STREAM","STREAM_MUSIC");
                    params.put("KEY_PARAM_VOLUME", AppSettings.get().getAudioVolume()/100f+"");
                    mts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                } else {
                    mts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                }
               // afterSpeak();
            }
        }
    }

    @Override
    public void stop() {
        mts.stop();

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
        mts.shutdown();
        mts=null;
    }

    @Override
    public void initTTS() {

    }

    @Override
    public void auth() {

    }

    @Override
    public void onInit(int status) {
        if(status==TextToSpeech.SUCCESS) {
            int result = mts.setLanguage(Locale.CHINA);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                inited = false;
            } else {
                inited = true;
            }
        } else {
            inited=false;
        }
    }
}
