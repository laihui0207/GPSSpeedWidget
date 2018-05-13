package com.huivip.gpsspeedwidget.speech;

import android.content.Context;
import android.text.TextUtils;

public class SpeechFactory {
    public static String BAIDUTTS="baiduTTS";
    public static String XUNFEITTS="xunfeiTTS";
    static SpeechFactory factory;
    String currentEngine;
    Context mContext;
    private SpeechFactory(Context context){
      this.mContext=context;
    }

    public static SpeechFactory getInstance(Context context){
        if(factory==null){
            factory=new SpeechFactory(context);
        }
        return factory;
    }

    public TTS getTTSEngine(String type){
        if(type==null) return null;
        if(XUNFEITTS.equalsIgnoreCase(type)){
            currentEngine=XUNFEITTS;
            return XFTTS.getInstance(mContext);
        } else {
            currentEngine=BAIDUTTS;
            return BDTTS.getInstance(mContext);
        }
    }
    public TTS resetEngine(String engineType){
        TTS tts=null;
        /*if(!engineType.equalsIgnoreCase(currentEngine)){
            tts=getTTSEngine(currentEngine);
            tts.release();
            tts=getTTSEngine(engineType);
            //currentEngine=engineType;
        } else if(!TextUtils.isEmpty(currentEngine)){
            tts=getTTSEngine(currentEngine);
        } else {
            currentEngine=engineType;*/
            tts=getTTSEngine(engineType);
        //}
        return tts;
    }
}
