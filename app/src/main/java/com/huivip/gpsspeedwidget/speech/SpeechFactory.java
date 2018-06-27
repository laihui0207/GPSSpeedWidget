package com.huivip.gpsspeedwidget.speech;

import android.content.Context;
import android.content.IntentFilter;

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
    public static void registCallInEvent(){
        /*IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothHeadsetClient.ACTION_CALL_CHANGED);
        filter.addAction(BluetoothHeadsetClient.ACTION_AUDIO_STATE_CHANGED);
        registerReceiver(broadcastReceiver, filter);*/
    }
}
