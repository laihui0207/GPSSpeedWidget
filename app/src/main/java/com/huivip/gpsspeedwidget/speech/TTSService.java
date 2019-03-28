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
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    return true;
                } else {
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
      /*   if(mAudioManager==null){
             mAudioManager = (AudioManager) this.context.getSystemService(
                     Context.AUDIO_SERVICE);
         }*/
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
        }
    }
    @Override
    public void onAudioFocusChange(int focusChange) {
        if(AudioManager.AUDIOFOCUS_GAIN==focusChange){
            if(focusChange==AudioManager.AUDIOFOCUS_GAIN){

            }
        }
    }
    public void playAudio(String fileName){
        try {
            beforeSpeak();
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
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                    afterSpeak();
                    isPlaying=false;
                    /*File file=new File(fileName);
                    file.delete();*/
                    speakNext();
                    Log.d("GPS","MediaPlayer play finish!");
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

