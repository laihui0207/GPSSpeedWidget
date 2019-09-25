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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.beans.MusicEvent;
import com.huivip.gpsspeedwidget.music.AllSupportMusicAppActivity;
import com.huivip.gpsspeedwidget.music.MusicRemoteControllerService;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;
import com.huivip.gpsspeedwidget.widget.MusicWidget;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class MusicControllerService extends Service {
    public static String INTENT_ACTION = "com.huivip.widget.music.controller";
    BroadcastReceiver musicControllerReceiver;
    MusicRemoteControllerService musicRemoteControllerService;
    AppWidgetManager manager;
    AppWidgetHost appWidgetHost;
    ComponentName musicWidget;
    RemoteViews remoteViews;
    boolean appStarted=false;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        musicControllerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int key = intent.getIntExtra("key", -1);
                if (key > 0) {
                    if (PrefUtils.getSelectMusicPlayer(getApplicationContext()) == null) {
                        Intent selectMusicPlayer = new Intent(getApplicationContext(), AllSupportMusicAppActivity.class);
                        selectMusicPlayer.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(selectMusicPlayer);
                    } else {

                        if (musicRemoteControllerService != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                if(!appStarted && !Utils.isServiceRunning(getApplicationContext(),PrefUtils.getSelectMusicPlayer(getApplicationContext()))){
                                    startApp(PrefUtils.getSelectMusicPlayer(getApplicationContext()));
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            musicRemoteControllerService.sendMusicKeyEvent(key);
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    updatePlayButton(musicRemoteControllerService.isPlaying());
                                                }
                                            }, 1000);
                                        }
                                    },2000);
                                } else {
                                    musicRemoteControllerService.sendMusicKeyEvent(key);
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            updatePlayButton(musicRemoteControllerService.isPlaying());
                                        }
                                    }, 1000);
                                }
                            }
                        }
                    }
                }
            }
        };
        bindService(new Intent(this, MusicRemoteControllerService.class), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MusicRemoteControllerService.RCBinder binder = (MusicRemoteControllerService.RCBinder) service;
                musicRemoteControllerService = binder.getService();
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, Context.BIND_AUTO_CREATE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_ACTION);
        registerReceiver(musicControllerReceiver, filter);
        EventBus.getDefault().register(this);
        this.manager = AppWidgetManager.getInstance(this);
        appWidgetHost = new AppWidgetHost(getApplicationContext(), Constant.APP_WIDGET_HOST_ID);
        this.musicWidget = new ComponentName(this, MusicWidget.class);

    }
    private void startApp(String appPkg) {
        try {
            Intent intent = this.getPackageManager().getLaunchIntentForPackage(appPkg);
            startActivity(intent);
            appStarted=true;
        } catch (Exception e) {
            Toast.makeText(this, "应用未安装，启动失败", Toast.LENGTH_LONG).show();
            appStarted=false;
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
    private void updatePlayButton(boolean playing){
        this.remoteViews = new RemoteViews(getPackageName(), R.layout.music_vertical_widget);
        if(playing){
            remoteViews.setImageViewResource(R.id.v_button_play,R.drawable.ic_pause);
        } else {
            remoteViews.setImageViewResource(R.id.v_button_play,R.drawable.ic_play);
        }
        this.manager.updateAppWidget(this.musicWidget, this.remoteViews);
    }
    @Subscribe
    public void updateMusic(MusicEvent event) {
        this.remoteViews = new RemoteViews(getPackageName(), R.layout.music_vertical_widget);
        remoteViews.setTextViewText(R.id.v_music_songName, event.getSongName());
        remoteViews.setTextColor(R.id.v_music_songName, AppSettings.get().getMusicWidgetFontColor());
        int textSize = Integer.parseInt(AppSettings.get().getMusicWidgetFontSize());
        remoteViews.setTextViewTextSize(R.id.v_music_songName, TypedValue.COMPLEX_UNIT_SP, 20 + textSize);
        appStarted=true;
        remoteViews.setTextViewText(R.id.v_music_artistName, event.getArtistName());
        remoteViews.setTextColor(R.id.v_music_artistName, AppSettings.get().getMusicWidgetFontColor());
        remoteViews.setTextViewTextSize(R.id.v_music_artistName, TypedValue.COMPLEX_UNIT_SP, 10 + textSize);
        remoteViews.setImageViewBitmap(R.id.v_music_background, getRoundedCornerBitmap(event.getCover(),20));
        this.manager.updateAppWidget(this.musicWidget, this.remoteViews);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            updatePlayButton(musicRemoteControllerService.isPlaying());
        }
    }
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}
