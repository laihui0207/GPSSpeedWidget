package com.huivip.gpsspeedwidget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.*;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;
import com.huivip.gpsspeedwidget.beans.LrcBean;
import com.huivip.gpsspeedwidget.utils.LrcUtil;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LyricWidgetService extends Service {
    public static final String EXTRA_CLOSE="lyric.widget.close";
    public static String SONGNAME="lyric.songName";
    public static String ARTIST="lyric.artist";
    public static String POSITION="lyfic.position";
    public static String STATUS="lyric.status";
    public static String DURATION="lyric.duration";
    public static String LYRIC_CONTENT="lyric.content";
    AppWidgetManager manager;
    AudioManager audioManager;
    ComponentName lyricWidget;
    String lyric_content;
    long position;
    TimerTask lyricTask;
    final Handler lyricHandler = new Handler();
    long duration=0;
    Timer lyricTimer;
    long startTime;
    boolean started=false;
    private int currentPosition = 0;
    private int playercurrentMillis=0;
    private int lastPosition = 0;
    private List<LrcBean> list;
    private String currentLyricContentString="";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        this.manager = AppWidgetManager.getInstance(this);
        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        this.lyricWidget = new ComponentName(this,LyricWidgetProvider.class);
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
        this.lyricTimer.schedule(this.lyricTask, 0L, 500L);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getBooleanExtra(EXTRA_CLOSE, false)) {
            onStop();
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
        if (intent != null && !started) {

            lyric_content = intent.getStringExtra(LYRIC_CONTENT);
            Log.d("huivip","Lyric wiget:"+lyric_content);
            if (!TextUtils.isEmpty(lyric_content)) {
                list = LrcUtil.parseStr2List(lyric_content);
            } else {
                onStop();
                stopSelf();
                list=null;
            }
            duration = intent.getLongExtra(DURATION, 0);
            position = intent.getLongExtra(POSITION, 0);
            startTime=System.currentTimeMillis()-position;
            started=true;
            IntentFilter intentFilter = new IntentFilter( "com.huivip.widget.lyric.changed" );
            registerReceiver( myBroadcastReceiver , intentFilter);
        }

        return Service.START_REDELIVER_INTENT;
    }
    private void calTime(){
        if(!audioManager.isMusicActive()) return;
        playercurrentMillis=(int)(System.currentTimeMillis()-startTime);
        getCurrentPosition();
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.lyric_widget);
        if(list!=null && list.size()>0) {
            for (int i = 0; i < list.size(); i++) {
                if (i == currentPosition) {
                    currentLyricContentString = list.get(i).getLrc();

                }/* else {
                canvas.drawText(list.get(i).getLrc(), width / 2, height / 2 + 80 * i, gPaint);
            }*/
            }
            //Log.d("huivip","calTime:"+currentLyricContentString);
            remoteViews.setTextViewText(R.id.textView_lyric, currentLyricContentString);
        }
       /* else {
            remoteViews.setTextViewText(R.id.textView_lyric, "");
        }*/
        this.manager.updateAppWidget(this.lyricWidget, remoteViews);
    }
    @Override
    public void onDestroy() {
        started=false;
        super.onDestroy();
    }
    public void onStop(){
       /* if(lyricTimer!=null){
            lyricTimer.cancel();
        }
        if(lyricTask!=null){
            lyricTask.cancel();
        }
        list=null;*/
       started=false;
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
            Log.d("huivip","lyric widget get currentPosition failed");
            Log.d("huiivp",e.getLocalizedMessage());
        }
    }
    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent!=null && intent.getAction().equalsIgnoreCase("com.huivip.widget.lyric.changed")){
                lyric_content = intent.getStringExtra(LYRIC_CONTENT);
                Log.d("huivip","Lyric wiget:"+lyric_content);
                if (!TextUtils.isEmpty(lyric_content)) {
                    list = LrcUtil.parseStr2List(lyric_content);
                } else {
                    list=null;
                }
                position = intent.getLongExtra(POSITION, 0);
                startTime=System.currentTimeMillis()-position;
            }
        }

    };
}
