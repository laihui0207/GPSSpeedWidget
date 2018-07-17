package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;
import com.huivip.gpsspeedwidget.TextFloatingService;

public class MediaNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String songName=intent.getStringExtra("track");
        String artistName=intent.getStringExtra("artist");
        String album=intent.getStringExtra("album");
        StringBuffer showString=new StringBuffer();

        if(!TextUtils.isEmpty(songName)){
            showString.append("歌名:"+songName).append("\n");
        }
        if(!TextUtils.isEmpty(artistName)){
            showString.append("歌手:"+artistName).append("\n");
        }
        if(!TextUtils.isEmpty(album)){
            showString.append("唱片:"+album).append("\n");
        }
        if(!TextUtils.isEmpty(showString.toString())){
/*            Toast.makeText(context,showString.toString(),Toast.LENGTH_LONG).show();*/
            Intent textFloat=new Intent(context,TextFloatingService.class);
            textFloat.putExtra(TextFloatingService.SHOW_TEXT,showString.toString());
            textFloat.putExtra(TextFloatingService.SHOW_TIME,10);
            context.startService(textFloat);
        }
    }
}
