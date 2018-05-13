package com.huivip.gpsspeedwidget.speech;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.navi.AMapNavi;
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
public class XFTTS implements TTS {

    /**
     * 请替换您自己申请的ID。
     */
    private final String appId = "5a9ca05c";

    public static XFTTS ttsManager;
    private Context mContext;
    private SpeechSynthesizer mTts;
    private boolean isPlaying = false;
    private LinkedList<String> wordList = new LinkedList();
    private final int TTS_PLAY = 1;
    private final int CHECK_TTS_PLAY = 2;
    AudioManager am;
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
                                    AMapNavi.setTtsPlaying(isPlaying = false);
                                    handler.obtainMessage(1).sendToTarget();
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
                                    initTTS();
                                    int audioVolume=PrefUtils.getAudioVolume(mContext);
/*                                    if(PrefUtils.isEnableAudioMixService(mContext)){*/
                                        am.setStreamVolume(AudioManager.STREAM_MUSIC,audioVolume,0);
                                   /* }
                                    else {
                                        am.setStreamVolume(AudioManager.STREAM_VOICE_CALL,audioVolume,0);
                                    }*/
                                    AMapNavi.setTtsPlaying(isPlaying = true);
                                }

                                @Override
                                public void onSpeakPaused() {
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

    private XFTTS(Context mContext) {
        //mContext = context.getApplicationContext();
        this.mContext=mContext;
        //SpeechUtility.createUtility(SpeechApp.this, "appid=" + getString(R.string.app_id));
        SpeechUtility.createUtility(mContext, SpeechConstant.APPID + "=" + appId);
        if (mTts == null) {
            Log.d("huvip","create Speed utility");
            createSynthesizer();
        }
        am= (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    private void createSynthesizer() {
        mTts = SpeechSynthesizer.createSynthesizer(mContext,
                new InitListener() {
                    @Override
                    public void onInit(int errorcode) {
                        if (ErrorCode.SUCCESS == errorcode) {
                        } else {
                            Toast.makeText(mContext, "语音合成初始化失败!" + errorcode, Toast.LENGTH_SHORT);
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
        if (wordList != null) {
            wordList.clear();
        }
        if (mTts != null) {
            mTts.stopSpeaking();
        }
        isPlaying = false;
    }

    public void release() {
        if (wordList != null) {
            wordList.clear();
        }
        if (mTts != null) {
            mTts.destroy();
        }
    }

    @Override
    public void initTTS() {
        //设置发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        //设置语速,值范围：[0, 100],默认值：50
        mTts.setParameter(SpeechConstant.SPEED, "55");
        //设置音量
        mTts.setParameter(SpeechConstant.VOLUME, "tts_volume");
        //设置语调
        mTts.setParameter(SpeechConstant.PITCH, "tts_pitch");
        /*if(!PrefUtils.isEnableAudioMixService(mContext)){
            Log.d("huivip","Audio use voice Call");
            mTts.setParameter(SpeechConstant.STREAM_TYPE,"AudioManager.STREAM_VOICE_CALL");
        } else {
            mTts.setParameter(SpeechConstant.STREAM_TYPE,"AudioManager.STREAM_MUSIC");
        }*/
    }

    @Override
    public void speak(String arg1) {
        if (PrefUtils.isEnableAudioService(mContext) && PrefUtils.isEnableTempAudioService(mContext)) {
            if (wordList != null)
                wordList.addLast(arg1);
            handler.obtainMessage(CHECK_TTS_PLAY).sendToTarget();
        }
    }
}
