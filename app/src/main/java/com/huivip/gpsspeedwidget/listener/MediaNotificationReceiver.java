package com.huivip.gpsspeedwidget.listener;

import android.content.*;
import android.content.pm.PackageManager;
import android.os.BadParcelableException;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.huivip.gpsspeedwidget.TextFloatingService;
import com.huivip.gpsspeedwidget.lyric.LyricService;
import com.huivip.gpsspeedwidget.lyric.LyricServiceLowVersion;
import com.huivip.gpsspeedwidget.utils.PrefUtils;

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

        if (artistName != null && artistName.trim().startsWith("<") && artistName.trim().endsWith(">") && artistName.contains("unknown"))
            artistName = "";

       /* if (!TextUtils.isEmpty(player) && player.contains("youtube") && (TextUtils.isEmpty(artistName) || TextUtils.isEmpty(songName) ||
                (!artistName.contains("VEVO") && !songName.contains("-")))) {
            return;
        }*/
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
        }
        showString.append("长度:").append(longToTimeString(duration)).append("\n");
        if(isPlaying){
            showString.append("播放中").append("\n");
        } else {
            showString.append("播放暂停").append("\n");
        }
        if(position > -1){
            showString.append("当前:"+longToTimeString(position)+"\n");
        }*/
        Log.d("huivip", showString.toString());
        SharedPreferences current = context.getSharedPreferences("current_music", Context.MODE_PRIVATE);
        String currentArtist = current.getString("artist", "");
        String currentTrack = current.getString("track", "");

        SharedPreferences.Editor editor = current.edit();
        editor.putString("artist", artistName);
        editor.putString("track", songName);
        editor.putString("player", player);
        if (!(currentArtist.equals(artistName) && currentTrack.equals(songName) && position == -1))
            editor.putLong("position", position);
        editor.putBoolean("playing", isPlaying);
        editor.putLong("duration", duration);
        if (isPlaying) {
            editor.putLong("startTime", System.currentTimeMillis());
        }
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT)
            editor.commit();
        else
            editor.apply();
        if (PrefUtils.isLyricEnabled(context)) {
            if (!TextUtils.isEmpty(showString.toString())) {
                /*            Toast.makeText(context,showString.toString(),Toast.LENGTH_LONG).show();*/
                Intent textFloat = new Intent(context, TextFloatingService.class);
                textFloat.putExtra(TextFloatingService.SHOW_TEXT, showString.toString());
                textFloat.putExtra(TextFloatingService.SHOW_TIME, 10);
                context.startService(textFloat);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Intent lycService = new Intent(context, LyricService.class);
                lycService.putExtra(LyricService.SONGNAME, songName);
                lycService.putExtra(LyricService.ARTIST, artistName);
                lycService.putExtra(LyricService.STATUS, isPlaying);
                lycService.putExtra(LyricService.DURATION, duration);
                context.startService(lycService);
            } else {
                Intent lycServiceLowVersion = new Intent(context, LyricServiceLowVersion.class);
                lycServiceLowVersion.putExtra(LyricServiceLowVersion.SONGNAME, songName);
                lycServiceLowVersion.putExtra(LyricServiceLowVersion.ARTIST, artistName);
                lycServiceLowVersion.putExtra(LyricServiceLowVersion.STATUS, isPlaying);
                lycServiceLowVersion.putExtra(LyricServiceLowVersion.DURATION, duration);
                context.startService(lycServiceLowVersion);
            }
        }
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
    public static void disableBroadcastReceiver(Context context) {
        int flag=(PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
        ComponentName component=new ComponentName(context.getApplicationContext(), MediaNotificationReceiver.class);
        context.getPackageManager().setComponentEnabledSetting(component, flag, PackageManager.DONT_KILL_APP);
    }
}
