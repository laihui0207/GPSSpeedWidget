package com.huivip.gpsspeedwidget.speech;

import android.content.Context;

public class SpeechFactory {
    public static String TAG="GPS";
    public static String SIBICHITTS="SBCTTS";
    public static String TEXTTTS="textTTS";
    public static String SDKTTS="aliTTS";
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
        } else if(SIBICHITTS.equalsIgnoreCase(type)){
            return SBCTTS.getInstance(context);
        }else if(SDKTTS.equalsIgnoreCase(type)){
            return AliTTS.getInstance(context);
        }
        return null;
    }
}
