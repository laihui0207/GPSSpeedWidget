package com.huivip.gpsspeedwidget.speech;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.IBinder;

import java.util.LinkedList;

public abstract class TTSService implements TTS {
    private final String TAG = "huivip";
    PlayAudioService.PlayBinder playBinder;
    private ServiceConnection mServiceConnection;
    Context context;
    protected boolean customPlayer=false;

    public TTSService(Context context) {
        this.context = context;
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                playBinder = (PlayAudioService.PlayBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        context.bindService(new Intent(context, PlayAudioService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    boolean isPlaying=false;
    protected LinkedList<String> wordList = new LinkedList();
    protected final int TTS_PLAY = 1;
    protected final int CHECK_TTS_PLAY = 2;

    public void playAudio(String fileName){
        isPlaying=true;
        if(playBinder!=null) {
            playBinder.playByAudioTrack(fileName, new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    isPlaying = false;
                    speakNext();
                }
            });
        }
    }
}

