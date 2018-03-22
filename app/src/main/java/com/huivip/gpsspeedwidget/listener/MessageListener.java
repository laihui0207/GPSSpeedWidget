package com.huivip.gpsspeedwidget.listener;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.huivip.gpsspeedwidget.MainHandlerConstant;
import com.huivip.gpsspeedwidget.utils.PrefUtils;

/**
 * SpeechSynthesizerListener 简单地实现，仅仅记录日志
 * Created by fujiayi on 2017/5/19.
 */

public class MessageListener implements SpeechSynthesizerListener, MainHandlerConstant {
    private static final String TAG = "MessageListener";
    Context context;
    AudioManager am;
    int currentSystemVolume;
    int currentVoiceCallVolume;
    int currentMusicVolume;
    /**
     * 播放开始，每句播放开始都会回调
     *
     * @param utteranceId
     */
    @Override
    public void onSynthesizeStart(String utteranceId) {
        sendMessage("准备开始合成,序列号:" + utteranceId);
    }

    public MessageListener(Context context) {
        this.context = context;
        am= (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * 语音流 16K采样率 16bits编码 单声道 。
     *
     * @param utteranceId
     * @param bytes       二进制语音 ，注意可能有空data的情况，可以忽略
     * @param progress    如合成“百度语音问题”这6个字， progress肯定是从0开始，到6结束。 但progress无法和合成到第几个字对应。
     */
    @Override
    public void onSynthesizeDataArrived(String utteranceId, byte[] bytes, int progress) {
        //  Log.i(TAG, "合成进度回调, progress：" + progress + ";序列号:" + utteranceId );
    }

    /**
     * 合成正常结束，每句合成正常结束都会回调，如果过程中出错，则回调onError，不再回调此接口
     *
     * @param utteranceId
     */
    @Override
    public void onSynthesizeFinish(String utteranceId) {
        sendMessage("合成结束回调, 序列号:" + utteranceId);

    }

    @Override
    public void onSpeechStart(String utteranceId) {
        currentSystemVolume=am.getStreamVolume(AudioManager.STREAM_SYSTEM);
        currentVoiceCallVolume=am.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        currentMusicVolume=am.getStreamVolume(AudioManager.STREAM_MUSIC);

        if(PrefUtils.isSeparatedVolume(context)){
            Log.d("huivip","increase music");
            am.setStreamVolume(AudioManager.STREAM_MUSIC,currentMusicVolume/2,0);
            am.setSpeakerphoneOn(true);
            //am.setStreamVolume(AudioManager.STREAM_VOICE_CALL,voiceVolume,0);
        }

        sendMessage("播放开始回调, 序列号:" + utteranceId);
    }

    /**
     * 播放进度回调接口，分多次回调
     *
     * @param utteranceId
     * @param progress    如合成“百度语音问题”这6个字， progress肯定是从0开始，到6结束。 但progress无法保证和合成到第几个字对应。
     */
    @Override
    public void onSpeechProgressChanged(String utteranceId, int progress) {
        //  Log.i(TAG, "播放进度回调, progress：" + progress + ";序列号:" + utteranceId );
    }

    /**
     * 播放正常结束，每句播放正常结束都会回调，如果过程中出错，则回调onError,不再回调此接口
     *
     * @param utteranceId
     */
    @Override
    public void onSpeechFinish(String utteranceId) {
        sendMessage("播放结束回调, 序列号:" + utteranceId);
        am.setSpeakerphoneOn(false);
        //am.setStreamVolume(AudioManager.STREAM_VOICE_CALL,currentVoiceCallVolume,0);
        am.setStreamVolume(AudioManager.STREAM_MUSIC,currentMusicVolume,0);
        am.setStreamVolume(AudioManager.STREAM_SYSTEM,currentSystemVolume,0);
    }

    /**
     * 当合成或者播放过程中出错时回调此接口
     *
     * @param utteranceId
     * @param speechError 包含错误码和错误信息
     */
    @Override
    public void onError(String utteranceId, SpeechError speechError) {
        sendErrorMessage("错误发生：" + speechError.description + "，错误编码："
                + speechError.code + "，序列号:" + utteranceId);


      /*  if(currentVoiceCallVolume!=0){
            am.setStreamVolume(AudioManager.STREAM_VOICE_CALL,currentVoiceCallVolume,0);
        }*/
        if(currentMusicVolume!=0){
            am.setStreamVolume(AudioManager.STREAM_MUSIC,currentMusicVolume,0);
        }
        if(currentSystemVolume!=0){
            am.setStreamVolume(AudioManager.STREAM_SYSTEM,currentSystemVolume,0);
        }
    }

    private void sendErrorMessage(String message) {
        sendMessage(message, true);
    }


    private void sendMessage(String message) {
        sendMessage(message, false);
    }
    
    protected void sendMessage(String message, boolean isError) {
        if (isError) {
            Log.e(TAG, message);
        } else {
            Log.i(TAG, message);
        }

    }
}
