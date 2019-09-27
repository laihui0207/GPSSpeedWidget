package com.huivip.gpsspeedwidget.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.beans.LrcBean;
import com.huivip.gpsspeedwidget.beans.LyricContentEvent;
import com.huivip.gpsspeedwidget.utils.LrcUtil;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.widget.LyricWidget;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LyricWidgetService extends Service {
    public static final String EXTRA_CLOSE="lyric.widget.close";
    public static String POSITION="lyfic.position";
    public static String LYRIC_CONTENT="lyric.content";
    AppWidgetManager manager;
    AudioManager audioManager;
    ComponentName lyricWidget;
    private Paint gPaint;
    private Paint hPaint;
    String lyric_content;
    long position;
    TimerTask lyricTask;
    final Handler lyricHandler = new Handler();
    Timer lyricTimer;
    long startTime;
    boolean started=false;
    private int currentPosition = 0;
    private int playercurrentMillis=0;
    private List<LrcBean> list;
    private String currentLyricContentString="";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onCreate() {
        this.manager = AppWidgetManager.getInstance(this);
        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        this.lyricWidget = new ComponentName(this, LyricWidget.class);
        lyricTimer = new Timer();
        this.lyricTask = new TimerTask() {
            @Override
            public void run() {
                LyricWidgetService.this.lyricHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        calTime();
                    }
                });
            }
        };
        this.lyricTimer.schedule(this.lyricTask, 0L, 1000L);
        EventBus.getDefault().register(this);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!PrefUtils.isLyricWidgetEnable(getApplicationContext()) || (intent != null && intent.getBooleanExtra(EXTRA_CLOSE, false))) {
            onStop();
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
        return Service.START_REDELIVER_INTENT;
    }
    private void calTime(){
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.lyric_widget);
        if(!audioManager.isMusicActive()){
            onStop();
            stopSelf();
            return;
        }
        playercurrentMillis=(int)(System.currentTimeMillis()-startTime);
        getCurrentPosition();
        if(list!=null && list.size()>0) {
            for (int i = 0; i < list.size(); i++) {
                if (i == currentPosition) {
                    currentLyricContentString = list.get(i).getLrc();
                }
            }
            remoteViews.setTextViewText(R.id.textView_lyric, currentLyricContentString);
        }
        this.manager.updateAppWidget(this.lyricWidget, remoteViews);

    }
    @Override
    public void onDestroy() {
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }
    public void onStop(){
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.lyric_widget);
        remoteViews.setTextViewText(R.id.textView_lyric,"");
        this.manager.updateAppWidget(this.lyricWidget,remoteViews);
    }
    private void getCurrentPosition() {
        try {
            int currentMillis =playercurrentMillis;
            if(list!=null && list.size()>0) {
                if (currentMillis < list.get(0).getStart()) {
                    currentPosition = 0;
                    return;
                }
                if (currentMillis > list.get(list.size() - 1).getStart()) {
                    currentPosition = list.size() - 1;
                    return;
                }
                for (int i = 0; i < list.size(); i++) {
                    if (currentMillis >= list.get(i).getStart() && currentMillis < list.get(i).getEnd()) {
                        currentPosition = i;
                        return;
                    }
                }
            }
            else {
                currentPosition=0;
            }
        } catch (Exception e) {
        }
    }
    @Subscribe
    public void updateSong(LyricContentEvent event){
        lyric_content=event.getContent();
        position=event.getPosition();
        if (!TextUtils.isEmpty(lyric_content)) {
            list = LrcUtil.parseStr2List(lyric_content);
        } else {
            list=null;
        }
        startTime=System.currentTimeMillis()-position;
    }
}
