package com.huivip.gpsspeedwidget.speech;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import com.huivip.gpsspeedwidget.utils.PrefUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

public abstract class TTSService implements TTS,AudioManager.OnAudioFocusChangeListener {
    private AudioManager mAudioManager;
    private static boolean vIsActive=false;
    private final String TAG = "huivip";
    AudioFocusRequest mFocusRequest;
    Context context;
    private MediaPlayer mediaPlayer  ;
    boolean isPlaying=false;
    protected LinkedList<String> wordList = new LinkedList();
    protected final int TTS_PLAY = 1;
    protected final int CHECK_TTS_PLAY = 2;
    protected boolean requestAudioFocus() {
        // 获取系统音乐服务状态
        if(mAudioManager==null){
            mAudioManager = (AudioManager) this.context.getSystemService(
                    Context.AUDIO_SERVICE);
        }
        vIsActive = mAudioManager.isMusicActive();
        if (vIsActive) {//播放状态
           // Log.d(TAG, "in Music!");
            AudioAttributes mPlaybackAttributes = null;
            int focusType=-1;
            int streamType=-1;
            if(PrefUtils.isEnableAudioMixService(context)){
                focusType=AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK;
                streamType = AudioManager.STREAM_MUSIC;
            }
             else {
                focusType = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
                streamType = AudioManager.STREAM_VOICE_CALL;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mPlaybackAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(streamType)
                        .build();
                int result = -1;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mFocusRequest = new AudioFocusRequest.Builder(focusType)
                            .setAudioAttributes(mPlaybackAttributes)
                            .setAcceptsDelayedFocusGain(true)
                            .setWillPauseWhenDucked(true)
                            .setOnAudioFocusChangeListener(this)
                            .build();
                    result = mAudioManager.requestAudioFocus(mFocusRequest);

                } else {
                    result = mAudioManager.requestAudioFocus(this, streamType, focusType);
                }
                Log.d("huivip", "Audio request Focus:" + result);
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    return true;
                }
            }


        }
        return false;
    }

    protected void beforeSpeak(){
        requestAudioFocus();
    }

    protected void afterSpeak() {
        if (/*vIsActive &&*/ mAudioManager!=null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if(mFocusRequest!=null) {
                    mAudioManager.abandonAudioFocusRequest(mFocusRequest);
                }
            } else {
                mAudioManager.abandonAudioFocus(this);
            }
            Log.d("GPS","Abandon Audio forces");
        }
    }
    @Override
    public void onAudioFocusChange(int focusChange) {
        Log.d("GPS_Audio","focus change:"+focusChange);
       switch (focusChange){
           case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
           case AudioManager.AUDIOFOCUS_GAIN:
           case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
               Log.d("GPS_Audio","GET Audio Focus");
               if(!mediaPlayer.isPlaying()){
                   mediaPlayer.start();
               }
               int volume=PrefUtils.getAudioVolume(context);
               mediaPlayer.setVolume(volume/100f,volume/100f);
               break;
           case AudioManager.AUDIOFOCUS_LOSS:
               if(mediaPlayer.isPlaying()){
                   mediaPlayer.stop();
               }
               mediaPlayer.release();
               mediaPlayer=null;
               break;
           case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                }
                break;
           case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
               Log.d("GPS_Audio","Loss Audio Focus");
               if(mediaPlayer.isPlaying()){
                   mediaPlayer.setVolume(0.1f,0.1f);
               }
               break;

       }
    }
    public void playAudio(String fileName){
        try {
            mediaPlayer = new MediaPlayer();
            FileInputStream fis = new FileInputStream(fileName);
            mediaPlayer.setDataSource(fis.getFD());
            int volume=PrefUtils.getAudioVolume(context);
            mediaPlayer.setVolume(volume/100f,volume/100f);
            AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
            if(PrefUtils.isEnableAudioMixService(context)) {
                attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
            } else {
                attrBuilder.setLegacyStreamType(AudioManager.STREAM_VOICE_CALL);
            }
            mediaPlayer.setAudioAttributes(attrBuilder.build());
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                    //if(PrefUtils.isEnableAudioMixService(context)){
                        afterSpeak();
                    //}
                    isPlaying=false;
                    if (!PrefUtils.isEnableCacheAudioFile(context)) {
                        File file = new File(fileName);
                        file.delete();
                    }
                    speakNext();
                    Log.d("GPS","MediaPlayer play finish!");
                }
            });
            beforeSpeak();
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

