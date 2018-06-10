package com.huivip.gpsspeedwidget.speech;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

public abstract class TTSService implements TTS,AudioManager.OnAudioFocusChangeListener {
    private AudioManager mAudioManager;
    private static boolean vIsActive=false;
    private final String TAG = "huivip";
    AudioFocusRequest mFocusRequest;
    Context context;

    protected boolean requestAudioFocus() {
        // 获取系统音乐服务状态
        if(mAudioManager==null){
            mAudioManager = (AudioManager) this.context.getSystemService(
                    Context.AUDIO_SERVICE);
        }
        vIsActive = mAudioManager.isMusicActive();
        if (vIsActive) {//播放状态
            Log.d(TAG, "in Music!");
            AudioAttributes mPlaybackAttributes = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mPlaybackAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build();
                int result = -1;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                            .setAudioAttributes(mPlaybackAttributes)
                            .setAcceptsDelayedFocusGain(true)
                            .setWillPauseWhenDucked(true)
                            .setOnAudioFocusChangeListener(this)
                            .build();
                    result = mAudioManager.requestAudioFocus(mFocusRequest);

                } else {
                    result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
                }
                Log.d("huivip", "Audio request Focus:" + result);
                if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
                    return true;
                }
                else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        mFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                                .setAudioAttributes(mPlaybackAttributes)
                                .setAcceptsDelayedFocusGain(true)
                                .setWillPauseWhenDucked(true)
                                .setOnAudioFocusChangeListener(this)
                                .build();
                        result = mAudioManager.requestAudioFocus(mFocusRequest);

                    } else {
                        result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                    }
                    return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
                }
            }

        }
        return false;
    }

    protected void beforeSpeak(){
         if(mAudioManager==null){
             mAudioManager = (AudioManager) this.context.getSystemService(
                     Context.AUDIO_SERVICE);
         }
        requestAudioFocus();
    }

    protected void afterSpeak() {
        if (vIsActive) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mAudioManager.abandonAudioFocusRequest(mFocusRequest);
            } else {
                mAudioManager.abandonAudioFocus(this);
            }
        }
    }
    @Override
    public void onAudioFocusChange(int focusChange) {
        if(AudioManager.AUDIOFOCUS_GAIN==focusChange){
            if(focusChange==AudioManager.AUDIOFOCUS_GAIN){

            }
        }
    }
}

