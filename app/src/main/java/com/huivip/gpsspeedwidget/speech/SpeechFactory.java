package com.huivip.gpsspeedwidget.speech;

import android.annotation.SuppressLint;
import android.content.Context;

public class SpeechFactory {
    public static String TEXTTTS="textTTS";
    public static String SBCTTS="SBCTTS";
    @SuppressLint("StaticFieldLeak")
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
        if(TEXTTTS.equalsIgnoreCase(type)){
            return TextSpeech.getInstance(context);
        } else if(SBCTTS.equalsIgnoreCase(type)){
            return SBCSpeech.getInstance(context);
        }
        return TextSpeech.getInstance(context);
    }
}
