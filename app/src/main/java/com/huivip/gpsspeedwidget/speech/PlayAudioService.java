package com.huivip.gpsspeedwidget.speech;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

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
    private AudioTrack audioTrack;
    PlayBinder playBinder;

    public  class PlayBinder extends Binder {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public void play(String fileName, MediaPlayer.OnCompletionListener listener){
            playAudio(fileName,listener);
        }
        public void playByAudioTrack(String fileName,MediaPlayer.OnCompletionListener listener){
            playAudioByAudioTrack(fileName,listener);
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
        if(!PrefUtils.isEnableAudioVolumeDepress(getApplicationContext())){
            return true;
        }
        int focusType = -1;
        int streamType = -1;
        if (PrefUtils.isEnableAudioMixService(getApplicationContext())) {
            focusType = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK;
        } else {
            focusType = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
        }
        streamType = AudioManager.STREAM_MUSIC;
        int result = -1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mFocusRequest = new AudioFocusRequest.Builder(focusType)
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                     .setAcceptsDelayedFocusGain(true)
                    .setWillPauseWhenDucked(true)
                    .setOnAudioFocusChangeListener(this)
                    .build();
            result = mAudioManager.requestAudioFocus(mFocusRequest);
        } else {
            result = mAudioManager.requestAudioFocus(this, streamType, focusType);
        }
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
        }
    }
    @Override
    public void onAudioFocusChange(int focusChange) {
       /* switch (focusChange){
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

        }*/
    }

    public void playAudioByAudioTrack(String fileName, MediaPlayer.OnCompletionListener listener) {
        int bufferSize = AudioTrack.getMinBufferSize(16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        int type = AudioManager.STREAM_MUSIC;
       /* if(!PrefUtils.isEnableAudioMixService(getApplicationContext())) {
            type=AudioManager.STREAM_VOICE_CALL;
        }*/
        if (audioTrack == null)
            audioTrack = new AudioTrack(type,
                    16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
        //边读边播
        byte[] buffer = new byte[bufferSize];
        int volume = PrefUtils.getAudioVolume(getApplicationContext());
        float realVolume=volume / 100f;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            audioTrack.setVolume(realVolume);
        } else {
            audioTrack.setStereoVolume(realVolume,realVolume);
        }
        try {
            if (beforeSpeak()) {
                audioTrack.play();
                FileInputStream fis = null;
                fis = new FileInputStream(fileName);
                while (fis.available() > 0) {
                    int readCount = fis.read(buffer);
                    if (readCount == -1) {
                        break;
                    }
                    int writeResult = audioTrack.write(buffer, 0, readCount);
                    if (writeResult >= 0) {
                        //success
                    } else {
                        //fail
                        //丢掉这一块数据
                        continue;
                    }
                }
                audioTrack.stop();
                audioTrack.flush();
                //if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_STOPPED) {
                listener.onCompletion(null);
                //}
                audioTrack.release();
                audioTrack = null;
                fis.close();
                afterSpeak();
                if (!PrefUtils.isEnableCacheAudioFile(getApplicationContext())) {
                    File file = new File(fileName);
                    file.delete();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {

        }
    }
    public void playAudio(String fileName, MediaPlayer.OnCompletionListener listener){
        try {
            mediaPlayer.reset();
            FileInputStream fis = new FileInputStream(fileName);
            mediaPlayer.setDataSource(fis.getFD());
            int volume=PrefUtils.getAudioVolume(getApplicationContext());
            mediaPlayer.setVolume(volume/100f,volume/100f);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
                if (PrefUtils.isEnableAudioMixService(getApplicationContext())) {
                    attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
                } else {
                    attrBuilder.setLegacyStreamType(AudioManager.STREAM_VOICE_CALL);
                }
                mediaPlayer.setAudioAttributes(attrBuilder.build());
            } else {
                if (PrefUtils.isEnableAudioMixService(getApplicationContext())) {
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                } else {
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
                }
            }
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
