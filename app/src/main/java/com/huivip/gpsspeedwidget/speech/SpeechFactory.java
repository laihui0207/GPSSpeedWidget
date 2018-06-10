package com.huivip.gpsspeedwidget.speech;

import android.content.Context;

public class SpeechFactory {
    public static String BAIDUTTS="baiduTTS";
    public static String XUNFEITTS="xunfeiTTS";
    public static String TEXTTTS="textTTS";
    public static String SDKTTS="SDKTTS";
    static SpeechFactory factory;
    Context context;
    private SpeechFactory(Context context){
      this.context =context;
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
            return XFTTS.getInstance(context);
        } if(TEXTTTS.equalsIgnoreCase(type)){
            return TextSpeech.getInstance(context);
        }
        else {
            return BDTTS.getInstance(context);
        }
    }
}
