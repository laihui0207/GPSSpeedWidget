package com.huivip.gpsspeedwidget.speech;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.navi.AMapNavi;
import com.huivip.gpsspeedwidget.utils.CrashHandler;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;

import java.util.LinkedList;

/**
 * 当前DEMO的播报方式是队列模式。其原理就是依次将需要播报的语音放入链表中，播报过程是从头开始依次往后播报。
 * <p>
 * 导航SDK原则上是不提供语音播报模块的，如果您觉得此种播报方式不能满足你的需求，请自行优化或改进。
 */
public class XFTTS extends TTSService implements SynthesizerListener {

    /**
     * 请替换您自己申请的ID。
     */
    private final String appId = "5a9ca05c";

    public static XFTTS ttsManager;
    private SpeechSynthesizer mTts;
    private boolean isPlaying = false;
    private LinkedList<String> wordList = new LinkedList();
    private final int TTS_PLAY = 1;
    private final int CHECK_TTS_PLAY = 2;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TTS_PLAY:
                    synchronized (mTts) {
                        if (!isPlaying && mTts != null && wordList.size() > 0) {
                            isPlaying = true;
                            String playtts = wordList.removeFirst();
                            if (mTts == null) {
                                createSynthesizer();
                            }

                            mTts.startSpeaking(playtts, new SynthesizerListener() {
                                @Override
                                public void onCompleted(SpeechError arg0) {
                                    handler.obtainMessage(1).sendToTarget();
                                    //afterSpeak();
                                    isPlaying=false;
                                }

                                @Override
                                public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
                                }

                                @Override
                                public void onBufferProgress(int arg0, int arg1, int arg2, String arg3) {
                                    // 合成进度
                                    isPlaying = true;
                                }

                                @Override
                                public void onSpeakBegin() {
                                    //开始播放
                                    //beforeSpeak();
                                    initTTS();
                                }

                                @Override
                                public void onSpeakPaused() {
                                    isPlaying=false;
                                }

                                @Override
                                public void onSpeakProgress(int arg0, int arg1, int arg2) {
                                    //播放进度
                                    isPlaying = true;
                                }

                                @Override
                                public void onSpeakResumed() {
                                    //继续播放
                                    isPlaying = true;
                                }
                            });
                        }
                    }
                    break;
                case CHECK_TTS_PLAY:
                    if (!isPlaying) {
                        handler.obtainMessage(1).sendToTarget();
                    }
                    break;
            }

        }
    };

    private XFTTS(Context context) {
        this.context=context;
        SpeechUtility.createUtility(context, SpeechConstant.APPID + "=" + appId);
        CrashHandler.getInstance().init(context);
        if (mTts == null) {
            createSynthesizer();
        }
    }

    private void createSynthesizer() {
        mTts = SpeechSynthesizer.createSynthesizer(context,
                new InitListener() {
                    @Override
                    public void onInit(int errorcode) {
                        if (ErrorCode.SUCCESS == errorcode) {
                        } else {
                            //Toast.makeText(context, "语音合成初始化失败!" + errorcode, Toast.LENGTH_SHORT).show();
                            Log.d("huivip_XF","Init Failed,Error Code:"+errorcode);
                        }
                    }
                });
    }

    public static XFTTS getInstance(Context context) {
        if (ttsManager == null) {
            ttsManager = new XFTTS(context);
        }
        return ttsManager;
    }

    public void stop() {
       /* if (wordList != null) {
            wordList.clear();
        }*/
        if (mTts != null) {
            mTts.stopSpeaking();
        }
        /*isPlaying = false;*/
    }

    public void release() {
        /*if (wordList != null) {
            wordList.clear();
        }*/
        if (mTts != null) {
            mTts.destroy();
        }
    }

    public void initTTS() {
        //设置发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaomei");
        //设置语速,值范围：[0, 100],默认值：50
        mTts.setParameter(SpeechConstant.SPEED, "55");
        //设置音量
        mTts.setParameter(SpeechConstant.VOLUME, PrefUtils.getAudioVolume(context)+"");
        //设置语调
        mTts.setParameter(SpeechConstant.PITCH, "tts_pitch");
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS,"false");
        mTts.setParameter(SpeechConstant.STREAM_TYPE,"AudioManager.STREAM_VOICE_CALL");
       /* if(PrefUtils.isEnableAudioMixService(mContext)){
            mTts.setParameter(SpeechConstant.STREAM_TYPE,"AudioManager.STREAM_MUSIC");
        } else {
            mTts.setParameter(SpeechConstant.STREAM_TYPE,"AudioManager.STREAM_VOICE_CALL");
        }*/

    }

    @Override
    public void speak(String arg1) {
        speak(arg1,false);
    }

    @Override
    public void speak(String text, boolean force) {
        if (PrefUtils.isEnableAudioService(context) && (force || PrefUtils.isEnableTempAudioService(context) )) {
            /*if (wordList != null)
                wordList.addLast(arg1);
            else {
                wordList=new LinkedList<>();
                wordList.add(arg1);
            }
            handler.obtainMessage(CHECK_TTS_PLAY).sendToTarget();*/
            mTts.startSpeaking(text,this);
        }

    }

    @Override
    public void onSpeakBegin() {
        initTTS();
       // beforeSpeak();
    }

    @Override
    public void onBufferProgress(int i, int i1, int i2, String s) {

    }

    @Override
    public void onSpeakPaused() {

    }

    @Override
    public void onSpeakResumed() {

    }

    @Override
    public void onSpeakProgress(int i, int i1, int i2) {

    }

    @Override
    public void onCompleted(SpeechError speechError) {
       // afterSpeak();
    }

    @Override
    public void onEvent(int i, int i1, int i2, Bundle bundle) {

    }
}
