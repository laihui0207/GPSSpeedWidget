package com.huivip.gpsspeedwidget.speech;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import com.huivip.gpsspeedwidget.utils.PrefUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PlayAudioService extends Service implements AudioManager.OnAudioFocusChangeListener {
    private AudioManager mAudioManager;
    private final String TAG = "GPS";
    AudioFocusRequest mFocusRequest;
    private MediaPlayer mediaPlayer;
    PlayBinder playBinder;

    public  class PlayBinder extends Binder {
        public void play(String fileName,MediaPlayer.OnCompletionListener listener){
            playAudio(fileName,listener);
        }
        public PlayAudioService getService(){
            return PlayAudioService.this;
        }
        boolean isPlaying(){
            return mediaPlayer.isPlaying();
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,"Bind Play audio service");
        return playBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer=new MediaPlayer();
        playBinder = new PlayBinder();
    }

    protected boolean requestAudioFocus() {
        // 获取系统音乐服务状态
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getSystemService(
                    Context.AUDIO_SERVICE);
        }
        int focusType = -1;
        int streamType = -1;
        if (PrefUtils.isEnableAudioMixService(getApplicationContext())) {
            focusType = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK;
            streamType = AudioManager.STREAM_MUSIC;
        } else {
            focusType = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
            //streamType = AudioManager.STREAM_VOICE_CALL;
            streamType = AudioManager.STREAM_MUSIC;
        }
        int result = -1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mFocusRequest = new AudioFocusRequest.Builder(focusType)
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                    // .setAcceptsDelayedFocusGain(true)
                    //.setWillPauseWhenDucked(true)
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
        return false;
    }

    protected boolean beforeSpeak(){
        return requestAudioFocus();
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
                int volume=PrefUtils.getAudioVolume(getApplicationContext());
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
    public void playAudio(String fileName,MediaPlayer.OnCompletionListener listener){
        try {
            mediaPlayer.reset();
            FileInputStream fis = new FileInputStream(fileName);
            mediaPlayer.setDataSource(fis.getFD());
            int volume=PrefUtils.getAudioVolume(getApplicationContext());
            mediaPlayer.setVolume(volume/100f,volume/100f);
           /* AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
            if(PrefUtils.isEnableAudioMixService(getApplicationContext())) {
                attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
            } else {
                attrBuilder.setLegacyStreamType(AudioManager.STREAM_VOICE_CALL);
            }
            mediaPlayer.setAudioAttributes(attrBuilder.build());*/
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    afterSpeak();
                    if (!PrefUtils.isEnableCacheAudioFile(getApplicationContext())) {
                        File file = new File(fileName);
                        file.delete();
                    }
                    listener.onCompletion(mp);
                    //speakNext();
                    Log.d("GPS","MediaPlayer play finish!");
                }
            });
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if(beforeSpeak()){
                        mediaPlayer.start();
                    }
                }
            });

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
