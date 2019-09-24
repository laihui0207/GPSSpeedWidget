package com.huivip.gpsspeedwidget.service;

import android.app.Service;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;

import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.beans.MusicEvent;
import com.huivip.gpsspeedwidget.music.AllSupportMusicAppActivity;
import com.huivip.gpsspeedwidget.music.MusicRemoteControllerService;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.widget.MusicWidget;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class MusicControllerService extends Service {
    public static String INTENT_ACTION="com.huivip.widget.music.controller";
    BroadcastReceiver musicControllerReceiver;
    MusicRemoteControllerService musicRemoteControllerService;
    AppWidgetManager manager;
    AppWidgetHost appWidgetHost;
    ComponentName musicWidget;
    RemoteViews remoteViews;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        musicControllerReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int key=intent.getIntExtra("key",-1);
                Log.d("huivip","Get Key:"+key);
                if(key>0){
                    if(PrefUtils.getSelectMusicPlayer(getApplicationContext())==null){
                        Intent selectMusicPlayer=new Intent(getApplicationContext(), AllSupportMusicAppActivity.class);
                        selectMusicPlayer.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(selectMusicPlayer);
                    } else {
                        //Toast.makeText(getApplicationContext(),PrefUtils.getSelectMusicPlayer(getApplicationContext()),Toast.LENGTH_LONG).show();
                        if(musicRemoteControllerService!=null){
                            musicRemoteControllerService.sendMusicKeyEvent(key);
                        }
                    }
                }
            }
        };
        bindService(new Intent(this, MusicRemoteControllerService.class), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MusicRemoteControllerService.RCBinder binder=(MusicRemoteControllerService.RCBinder)service;
                musicRemoteControllerService=binder.getService();

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        },Context.BIND_AUTO_CREATE);
        IntentFilter filter=new IntentFilter();
        filter.addAction(INTENT_ACTION);
        registerReceiver(musicControllerReceiver,filter);
        EventBus.getDefault().register(this);
        this.manager = AppWidgetManager.getInstance(this);
        appWidgetHost = new AppWidgetHost(getApplicationContext(), Constant.APP_WIDGET_HOST_ID);
        this.musicWidget = new ComponentName(this, MusicWidget.class);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe
    public void updateMusic(MusicEvent event) {
        this.remoteViews = new RemoteViews(getPackageName(), R.layout.music_vertical_widget);
        remoteViews.setTextViewText(R.id.v_music_songName, event.getSongName());
        remoteViews.setTextViewText(R.id.v_music_artistName, event.getArtistName());
        remoteViews.setImageViewBitmap(R.id.v_music_background,event.getCover());
        this.manager.updateAppWidget(this.musicWidget, this.remoteViews);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}
