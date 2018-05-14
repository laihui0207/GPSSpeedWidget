package com.huivip.gpsspeedwidget.speech;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import com.iflytek.cloud.Setting;


public class AudioFocusService extends Service {
    public static final String EXTRA_STOP="stop_Audio_focus_service";
    private AudioManager mAudioManager;
    private static boolean vIsActive=false;
    private final String TAG = "huivip";
    AudioFocusRequest mFocusRequest;
    private TTSOnAudioFocusChangeListener mListener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mAudioManager = (AudioManager) getApplicationContext().getSystemService(
                Context.AUDIO_SERVICE);
        if(vIsActive)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mAudioManager.abandonAudioFocusRequest(mFocusRequest);
            } else {
                mAudioManager.abandonAudioFocus(mListener);
            }
        }
        Log.d("huivip","Audio Sevice destory!");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        // 获取系统音乐服务
        if(intent.getBooleanExtra(EXTRA_STOP,false)){
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        // 获取系统音乐服务状态
        vIsActive=mAudioManager.isMusicActive();
        mListener = new TTSOnAudioFocusChangeListener();
        if(vIsActive) {//播放状态
            Log.d(TAG,"in Music!");
            AudioAttributes mPlaybackAttributes = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mPlaybackAttributes = new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .build();
                int result=-1;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                            .setAudioAttributes(mPlaybackAttributes)
                            .setAcceptsDelayedFocusGain(true)
                            .setWillPauseWhenDucked(true)
                            //.setOnAudioFocusChangeListener(this, mListener)
                            .build();
                    result = mAudioManager.requestAudioFocus(mFocusRequest);

                } else {
                    result = mAudioManager.requestAudioFocus(mListener,AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                }
                Log.d("huivip","Audio request Focus:"+result);
               /* Toast.makeText(getApplicationContext(),
                        "音频焦点:"+result+"=="+AudioManager.AUDIOFOCUS_REQUEST_GRANTED,Toast.LENGTH_SHORT)
                        .show();*/
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                {
                    Log.d(TAG, "requestAudioFocus successfully.");
                }
                else
                {
                    Log.d(TAG, "requestAudioFocus failed.");
                }

            }

        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    public class TTSOnAudioFocusChangeListener implements AudioManager.OnAudioFocusChangeListener {
        @Override
        public void onAudioFocusChange(int focusChange) {
            // TODO Auto-generated method stub

        }
    }
}
