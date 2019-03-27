package com.huivip.gpsspeedwidget.speech;

import android.content.Context;

public class SpeechFactory {
    public static String TAG="GPS";
    public static String BAIDUTTS="baiduTTS";
    public static String SIBICHITTS="SBCTTS";
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
        } else if(TEXTTTS.equalsIgnoreCase(type)){
            return TextSpeech.getInstance(context);
        } else if(SIBICHITTS.equalsIgnoreCase(type)){
            return SBCTTS.getInstance(context);
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
