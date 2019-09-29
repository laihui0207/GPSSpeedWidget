package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.BadParcelableException;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.huivip.gpsspeedwidget.beans.KuWoStatusEvent;
import com.huivip.gpsspeedwidget.beans.MusicEvent;
import com.huivip.gpsspeedwidget.lyric.LyricService;
import com.huivip.gpsspeedwidget.service.TextFloatingService;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.Utils;

import org.greenrobot.eventbus.EventBus;

public class MediaNotificationReceiver extends BroadcastReceiver {
    private static final String KW_PLAYER_STATUS = "cn.kuwo.kwmusicauto.action.PLAYER_STATUS";
    AudioManager audioManager;
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null)
            try {
                extras.getInt("state");
            } catch (BadParcelableException e) {
                return;
            }
        if (extras == null || (extras.getString("artist") == null && extras.getString("track") == null &&
                extras.getString("play_music_name") ==null && extras.getString("play_music_artist") ==null )
        ) {
            return;
        }
        long position = extras.containsKey("position") && extras.get("position") instanceof Long ?
                extras.getLong("position") : -1;
        if (extras.get("position") instanceof Double)
            position = Double.valueOf(extras.getDouble("position")).longValue();
        boolean isPlaying = extras.getBoolean(extras.containsKey("playstate") ? "playstate" : "playing", true);
        Object durationObject = extras.get("duration");
        Long duration = durationObject == null ? -1 : durationObject instanceof Long ? (Long) durationObject :
                durationObject instanceof Double ? ((Double) durationObject).longValue() :
                        durationObject instanceof Float ? ((Float) durationObject).longValue() :
                                durationObject instanceof Integer ? (((Integer) durationObject).longValue() * 1000) :
                                        durationObject instanceof String ? (Double.valueOf((String) durationObject)).longValue() : -1;
        String player = extras.getString("player");
        String songName=intent.getStringExtra("track");
        String artistName=intent.getStringExtra("artist");
        String album=intent.getStringExtra("album");
        if(intent.getAction().equalsIgnoreCase(KW_PLAYER_STATUS)){
            songName=intent.getStringExtra("play_music_name");
            artistName=intent.getStringExtra("play_music_artist");
            album=intent.getStringExtra("play_music_album");
            EventBus.getDefault().post(new KuWoStatusEvent(true));
        }
        // zx music
        if(intent.getAction().equalsIgnoreCase("update.widget.update_proBar")){
            songName = intent.getStringExtra("curplaysong");
            position = intent.getIntExtra("proBarvalue", 0);
        }
        if("com.ijidou.card.music".equalsIgnoreCase(intent.getAction())){
            artistName = intent.getStringExtra("music_artist");
            songName = intent.getStringExtra("music_title");
        }
        if("com.ijidou.action.UPDATE_PROGRESS".equalsIgnoreCase(intent.getAction())){
            position=intent.getIntExtra("elapse", 0);
        }
        if (artistName != null && artistName.trim().startsWith("<") && artistName.trim().endsWith(">") && artistName.contains("unknown"))
            artistName = "";

        StringBuffer showString=new StringBuffer();
        if (artistName != null && artistName.trim().startsWith("<") && artistName.trim().endsWith(">") && artistName.contains("unknown")) {
            artistName = "";
        }
        if(!TextUtils.isEmpty(songName)){
            showString.append("歌名:"+songName).append("\n");
        }
        if(!TextUtils.isEmpty(artistName)){
            showString.append("歌手:"+artistName).append("\n");
        }
        /*if(!TextUtils.isEmpty(album)){
            showString.append("唱片:"+album).append("\n");
        }*/
        /*showString.append("长度:").append(longToTimeString(duration)).append("\n");
        if(isPlaying){
            showString.append("播放中").append("\n");
        } else {
            showString.append("播放暂停").append("\n");
        }
        if(position > -1){
            showString.append("当前:"+longToTimeString(position)+"\n");
        }*/
        SharedPreferences current = context.getSharedPreferences("current_music", Context.MODE_PRIVATE);
        String currentArtist = current.getString("artist", "");
        String currentTrack = current.getString("track", "");
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if(currentArtist!=null && currentArtist.equalsIgnoreCase(artistName) && audioManager.isMusicActive() ){
            return;
        }
        SharedPreferences.Editor editor = current.edit();
        editor.putString("artist", artistName);
        editor.putString("track", songName);
        editor.putString("player", player);
        if (!(currentArtist.equals(artistName) && currentTrack.equals(songName) && position == -1)){
            editor.putLong("position", position);
        }
        editor.putBoolean("playing", isPlaying);
        editor.putLong("duration", duration);
        if (isPlaying) {
            editor.putLong("startTime", System.currentTimeMillis());
        }
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT)
            editor.commit();
        else
            editor.apply();
        if (AppSettings.get().isLyricEnable()) {
            if (!TextUtils.isEmpty(showString.toString())) {
                Intent textFloat = new Intent(context, TextFloatingService.class);
                textFloat.putExtra(TextFloatingService.SHOW_TEXT, showString.toString());
                textFloat.putExtra(TextFloatingService.SHOW_TIME, 10);
                context.startService(textFloat);
            }
            if(!Utils.isServiceRunning(context,LyricService.class.getName())) {
                Intent lycService = new Intent(context, LyricService.class);
                context.startService(lycService);
            }
        }
        EventBus.getDefault().post(new MusicEvent(songName,artistName));
    }
}
