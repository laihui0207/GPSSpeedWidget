package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BadParcelableException;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.huivip.gpsspeedwidget.LyricFloatingService;
import com.huivip.gpsspeedwidget.TextFloatingService;
import com.huivip.gpsspeedwidget.lyric.LyricService;
import com.huivip.gpsspeedwidget.utils.Utils;

public class MediaNotificationReceiver extends BroadcastReceiver {
    private String preSongName;
    private static boolean spotifyPlaying = false;
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        Log.d("huivip",intent.getAction());
        Log.d("huivip",extras.toString());
        if (extras != null)
            try {
                extras.getInt("state");
            } catch (BadParcelableException e) {
                return;
            }
        if (extras == null || extras.getInt("state") > 1 //Tracks longer than 20min are presumably not songs
                || (extras.getString("artist") == null && extras.getString("track") == null)
        ) {
            return;
        }
        long position = extras.containsKey("position") && extras.get("position") instanceof Long ?
                extras.getLong("position") : -1;
        if (extras.get("position") instanceof Double)
            position = Double.valueOf(extras.getDouble("position")).longValue();
        Log.d("huivip_position","current position:"+position);
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
        if (intent.getAction().equals("com.amazon.mp3.metachanged")) {
            artistName = extras.getString("com.amazon.mp3.artist");
            songName = extras.getString("com.amazon.mp3.track");
        } else if (intent.getAction().equals("com.spotify.music.metadatachanged"))
            isPlaying = spotifyPlaying;
        else if (intent.getAction().equals("com.spotify.music.playbackstatechanged"))
            spotifyPlaying = isPlaying;

        if (artistName != null && artistName.trim().startsWith("<") && artistName.trim().endsWith(">") && artistName.contains("unknown"))
            artistName = "";

        if (!TextUtils.isEmpty(player) && player.contains("youtube") && (TextUtils.isEmpty(artistName) || TextUtils.isEmpty(songName) ||
                (!artistName.contains("VEVO") && !songName.contains("-")))) {
            return;
        }
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
        if(!TextUtils.isEmpty(album)){
            showString.append("唱片:"+album).append("\n");
        }
        showString.append("长度:").append(longToTimeString(duration)).append("\n");
        if(isPlaying){
            showString.append("播放中").append("\n");
        } else {
            showString.append("播放暂停").append("\n");
        }
        if(position > -1){
            showString.append("当前:"+longToTimeString(position)+"\n");
        }
        Log.d("huivip",showString.toString());
        if(!TextUtils.isEmpty(showString.toString())){
/*            Toast.makeText(context,showString.toString(),Toast.LENGTH_LONG).show();*/
            Intent textFloat=new Intent(context,TextFloatingService.class);
            textFloat.putExtra(TextFloatingService.SHOW_TEXT,showString.toString());
            textFloat.putExtra(TextFloatingService.SHOW_TIME,10);
            context.startService(textFloat);
        }
/*        if(songName!=null && !songName.equalsIgnoreCase(preSongName)) {*/
            if(Utils.isServiceRunning(context,LyricFloatingService.class.getName()) && !isPlaying){
                Intent lycFloatingService = new Intent(context, LyricFloatingService.class);
                intent.putExtra(LyricFloatingService.EXTRA_CLOSE,true);
                context.startService(lycFloatingService);
            }
            if(isPlaying) {
                Intent lycFloatingService = new Intent(context, LyricFloatingService.class);
                lycFloatingService.putExtra(LyricFloatingService.SONGNAME, songName);
                lycFloatingService.putExtra(LyricFloatingService.ARTIST, artistName);
                if (position > -1) {
                    lycFloatingService.putExtra(LyricFloatingService.POSITION, position);
                }
                lycFloatingService.putExtra(LyricFloatingService.DURATION, duration);
                context.startService(lycFloatingService);
            }
           /* preSongName=songName;
        } else if(!isPlaying){
            Intent lycFloatingService = new Intent(context, LyricFloatingService.class);
            intent.putExtra(LyricFloatingService.EXTRA_CLOSE,true);
            context.startService(lycFloatingService);
        }*/
    }
    private String longToTimeString(long time){
        long totalSecond=time/1000;
        long hour=totalSecond/3600;
        long minute=(totalSecond-hour*3600)/60;
        long second=(totalSecond - hour*3600-minute*60);
        StringBuffer result=new StringBuffer();
        if(hour>0){
            result.append(hour).append(":");
        }
        result.append(minute).append(":").append(second);
        return result.toString();

    }
}
