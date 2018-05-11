package com.huivip.gpsspeedwidget.speech;

import android.content.Context;

public class SpeechFactory {
    public static String BAIDUTTS="baiduTTS";
    public static String XUNFEITTS="xunfeiTTS";
    static SpeechFactory factory;
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
            return XFTTS.getInstance(mContext);
        } else {
            return BDTTS.getInstance(mContext);
        }
    }

}
