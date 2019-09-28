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
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.beans.MusicAlbumUpdateEvent;
import com.huivip.gpsspeedwidget.beans.MusicEvent;
import com.huivip.gpsspeedwidget.music.AllSupportMusicAppActivity;
import com.huivip.gpsspeedwidget.music.MusicRemoteControllerService;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.util.Tool;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;
import com.huivip.gpsspeedwidget.widget.MusicWidget;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.common.Callback;
import org.xutils.image.ImageOptions;
import org.xutils.x;

public class MusicControllerService extends Service {
    public static String INTENT_ACTION = "com.huivip.widget.music.controller";
    BroadcastReceiver musicControllerReceiver;
    MusicRemoteControllerService musicRemoteControllerService;
    AppWidgetManager manager;
    AppWidgetHost appWidgetHost;
    ComponentName musicWidget;
    RemoteViews remoteViews;
    boolean appStarted=false;
    String currentSongName=null;
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
                                    },3000);
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    musicRemoteControllerService.registerRemoteController();
                }
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
        this.remoteViews = new RemoteViews(getPackageName(), R.layout.music_vertical_widget);
        remoteViews.setImageViewResource(R.id.v_music_background,R.drawable.fenmian);
        this.manager.updateAppWidget(this.musicWidget, this.remoteViews);
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
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void updateMusic(MusicEvent event) {
        currentSongName=event.getSongName();
        this.remoteViews = new RemoteViews(getPackageName(), R.layout.music_vertical_widget);
        if(!TextUtils.isEmpty(event.getSongName())) {
            remoteViews.setTextViewText(R.id.v_music_songName, event.getSongName());
            remoteViews.setTextColor(R.id.v_music_songName, AppSettings.get().getMusicWidgetFontColor());
            int textSize = Integer.parseInt(AppSettings.get().getMusicWidgetFontSize());
            remoteViews.setTextViewTextSize(R.id.v_music_songName, TypedValue.COMPLEX_UNIT_SP, 20 + textSize);
            //appStarted=true;
            remoteViews.setTextViewText(R.id.v_music_artistName, event.getArtistName());
            remoteViews.setTextColor(R.id.v_music_artistName, AppSettings.get().getMusicWidgetFontColor());
            remoteViews.setTextViewTextSize(R.id.v_music_artistName, TypedValue.COMPLEX_UNIT_SP, 10 + textSize);
        }
        if(event.getCover()!=null) {
            remoteViews.setImageViewBitmap(R.id.v_music_background, getRoundedCornerBitmap(event.getCover(), Tool.dp2px(20)));
        }
        this.manager.updateAppWidget(this.musicWidget, this.remoteViews);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            updatePlayButton(musicRemoteControllerService.isPlaying());
        }
    }
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void updateAlubm(MusicAlbumUpdateEvent event){
        if(!event.getSongName().equalsIgnoreCase(currentSongName)){
            return;
        }
        if(event.getPicUrl()!=null){
           ImageOptions imageOptions = new ImageOptions.Builder()
                    //.setSize(300,600)
                    .setRadius(20)
                    // 如果ImageView的大小不是定义为wrap_content, 不要crop.
                    .setCrop(true)
                    // 加载中或错误图片的ScaleType
                    //.setPlaceholderScaleType(ImageView.ScaleType.MATRIX)
                    .setImageScaleType(ImageView.ScaleType.CENTER_CROP)
                    //设置加载过程中的图片
                    .setLoadingDrawableId(R.drawable.fenmian)
                    //设置加载失败后的图片
                    .setFailureDrawableId(R.drawable.fenmian)
                    //设置使用缓存
                    .setUseMemCache(true)
                    //设置支持gif
                    .setIgnoreGif(false)
                    //设置显示圆形图片
                    .setCircular(false)
                    .setSquare(true)
                    .build();
            x.image().loadDrawable(event.getPicUrl(), imageOptions, new Callback.CommonCallback<Drawable>() {
                @Override
                public void onSuccess(Drawable result) {
                    remoteViews = new RemoteViews(getPackageName(), R.layout.music_vertical_widget);
                    remoteViews.setImageViewBitmap(R.id.v_music_background, getRoundedCornerBitmap(Tool.drawableToBitmap(result),Tool.dp2px(20)));
                    manager.updateAppWidget(musicWidget, remoteViews);
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {

                }

                @Override
                public void onCancelled(CancelledException cex) {

                }

                @Override
                public void onFinished() {

                }
            });

        } else if(event.getCover()!=null){
            this.remoteViews = new RemoteViews(getPackageName(), R.layout.music_vertical_widget);
            remoteViews.setImageViewBitmap(R.id.v_music_background, getRoundedCornerBitmap(event.getCover(), Tool.dp2px(20)));
            this.manager.updateAppWidget(this.musicWidget, this.remoteViews);
        }
    }
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        if(bitmap==null) return null;
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
