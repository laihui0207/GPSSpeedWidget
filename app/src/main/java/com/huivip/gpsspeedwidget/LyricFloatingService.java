package com.huivip.gpsspeedwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.huivip.gpsspeedwidget.listener.DelayTaskReceiver;
import com.huivip.gpsspeedwidget.lyric.GecimeKu;
import com.huivip.gpsspeedwidget.lyric.MockTimeThread;
import com.huivip.gpsspeedwidget.lyric.WangYiYunMusic;
import com.huivip.gpsspeedwidget.utils.CrashHandler;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.view.LrcView;
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager;

import java.util.Timer;
import java.util.TimerTask;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by laisun on 28/02/2018.
 */

public class LyricFloatingService extends Service{
    public static final String EXTRA_CLOSE = "com.huivip.gpsspeedwidget.EXTRA_CLOSE";
    public static final String EXTRA_HIDE="com.huivip.gpsspeed.EXTRA_HIDE";
    public static final String EXTRA_SHOW="com.huivip.gpsspeed.EXTRA_SHOW";
    public static final String SHOW_TIME ="TextFlating_ShowTime";
    public static final String SHOW_TEXT ="TextFlating_ShowText";
    public static final String TARGET="LYRICFLoating";
    public static String SONGNAME="lyric.songName";
    public static String ARTIST="lyric.artist";
    public static String POSITION="lyfic.position";
    public static String STATUS="lyric.status";
    public static String DURATION="lyric.duration";
    private WindowManager mWindowManager;
    private View mFloatingView;
    long startTime;
    AudioManager audioManager;
    String lyrcContent;
    String songName="";
    String artistName;
    MockTimeThread mock;
    TimerTask lyricTask;
    long duration=0;
    Timer lyricTimer;
    final Handler lyricHandler = new Handler();
    boolean isShowing=false;
    @BindView(R.id.lrc_floatting_view)
    LrcView lrcView;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent!=null){
            if (intent.getBooleanExtra(EXTRA_CLOSE, false) || !PrefUtils.isEnableNaviFloating(getApplicationContext())) {
                onStop();
                stopSelf();
                return super.onStartCommand(intent, flags, startId);
            }
            if(intent.getIntExtra(SHOW_TIME,0)>0){
                int delayTimeStop=intent.getIntExtra(SHOW_TIME,0);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                       onStop();
                       stopSelf();
                    }
                }, delayTimeStop*1000+300);
            }
            if(!audioManager.isMusicActive()){
                onStop();
                stopSelf();
            }
            String inputSongName = intent.getStringExtra(SONGNAME);
            String inputArtistName = intent.getStringExtra(ARTIST);
            long position=intent.getLongExtra(POSITION,0L);
            duration=intent.getLongExtra(DURATION,-1L);
            startTime=System.currentTimeMillis()+1000+position;
            mock=new MockTimeThread(lrcView);
            if(!TextUtils.isEmpty(inputSongName) &&(songName==null || !inputSongName.equalsIgnoreCase(songName))){
                lrcView.setLrc(null);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // lyrcContent = GecimeKu.downloadLyric(inputSongName, inputArtistName);
                        lyrcContent = WangYiYunMusic.downloadLyric(inputSongName, inputArtistName);
                        Message msg = new Message();
                        mHandler.sendMessage(msg);
                        songName=inputSongName;
                    }
                }).start();

            }
            isShowing = true;

        }
        this.lyricTask = new TimerTask() {
            @Override
            public void run() {
                LyricFloatingService.this.lyricHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        calTime();
                        //Log.d("huivip","GPS UTIL Check Location");
                    }
                });
            }
        };
        this.lyricTimer.schedule(this.lyricTask, 0L, 500L);
        return Service.START_REDELIVER_INTENT;
    }
    private void calTime(){
        long position=System.currentTimeMillis() - startTime;
        lrcView.setPlayercurrentMillis((int) position);
        if(duration>0 && (position+1000)>=duration){
            onStop();
            stopSelf();
        }
        if(!audioManager.isMusicActive()){
            onStop();
            stopSelf();
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(TextUtils.isEmpty(lyrcContent)) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(LyricFloatingService.this.lyricTimer!=null){
                            LyricFloatingService.this.lyricTimer.cancel();
                        }
                       onStop();
                       stopSelf();
                    }
                }, 3000L);
            }
            else {
                lrcView.setLrc(lyrcContent);
                //mock.reset();
                lrcView.init();
               /* if(!mock.isAlive()) {
                    mock.start();
                }*/

            }
            super.handleMessage(msg);
        }
    };
    private void onStop(){
        /*if(mock!=null){
            mock.reset();
            mock.setRunning(false);
        }*/
        if(mFloatingView!=null && mWindowManager!=null){
            try {
                mWindowManager.removeView(mFloatingView);
            }catch (Exception e){

            }
        }

        isShowing=false;
    }

    @Override
    public void onCreate() {
        if(!PrefUtils.isEnbleDrawOverFeature(getApplicationContext())){
            Toast.makeText(getApplicationContext(),"需要打开GPS插件的悬浮窗口权限",Toast.LENGTH_LONG).show();
            try {
                openSettings(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, BuildConfig.APPLICATION_ID);
            } catch (ActivityNotFoundException ignored) {
            }
            return;
        }
        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        mWindowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater inflater = LayoutInflater.from(this);
        mFloatingView = inflater.inflate(R.layout.floating_lyrc_window, null);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                getWindowType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.alpha = PrefUtils.getOpacity(getApplicationContext()) / 100.0F;
        ButterKnife.bind(this, mFloatingView);
        mWindowManager.addView(mFloatingView, params);
        mFloatingView.setOnTouchListener( new FloatingOnTouchListener());
        initMonitorPosition();
        CrashHandler.getInstance().init(getApplicationContext());
       /* lrcView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onStop();
                stopSelf();
                return false;
            }
        });*/
        lyricTimer = new Timer();

        super.onCreate();
    }

    private int getWindowType() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                WindowManager.LayoutParams.TYPE_PHONE;
    }
    private void openSettings(String settingsAction, String packageName) {
        Intent intent = new Intent(settingsAction);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("package:" + packageName));
        startActivity(intent);
    }
    private void initMonitorPosition() {
        if (mFloatingView == null) {
            return;
        }
        mFloatingView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                WindowManager.LayoutParams params = (WindowManager.LayoutParams) mFloatingView.getLayoutParams();
                    String[] xy=PrefUtils.getLyrcFloatingSolidLocation(getApplicationContext()).split(",");
                    params.x=(int)Float.parseFloat(xy[0]);
                    params.y=(int)Float.parseFloat(xy[1]);
                try {
                    mWindowManager.updateViewLayout(mFloatingView, params);
                } catch (IllegalArgumentException ignore) {
                }

                mFloatingView.setVisibility(View.VISIBLE);

                mFloatingView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private class FloatingOnTouchListener implements View.OnTouchListener {

        private float mInitialTouchX;
        private float mInitialTouchY;
        private int mInitialX;
        private int mInitialY;
        private long mStartClickTime;
        private boolean mIsClick;

        public FloatingOnTouchListener() {
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            final WindowManager.LayoutParams params = (WindowManager.LayoutParams) mFloatingView.getLayoutParams();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mInitialTouchX = event.getRawX();
                    mInitialTouchY = event.getRawY();

                    mInitialX = params.x;
                    mInitialY = params.y;

                    mStartClickTime = System.currentTimeMillis();

                    mIsClick = true;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float dX = event.getRawX() - mInitialTouchX;
                    float dY = event.getRawY() - mInitialTouchY;
                    if ((mIsClick && (Math.abs(dX) > 10 || Math.abs(dY) > 10))
                            || System.currentTimeMillis() - mStartClickTime > ViewConfiguration.getLongPressTimeout()) {
                        mIsClick = false;
                    }

                    if (!mIsClick) {
                        params.x = (int) (dX + mInitialX);
                        params.y = (int) (dY + mInitialY);

                        try {
                            mWindowManager.updateViewLayout(mFloatingView, params);
                        } catch (IllegalArgumentException ignore) {
                        }
                    } /*else {
                        onStop();
                        stopSelf();
                    }*/
                    return true;
                case MotionEvent.ACTION_UP:
                    PrefUtils.setLyrcFloatingSolidLocation(getApplicationContext(), params.x, params.y);
                    return true;
            }
            return false;
        }
    }
}
