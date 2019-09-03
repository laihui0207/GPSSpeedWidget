package com.huivip.gpsspeedwidget.service;

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
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.huivip.gpsspeedwidget.BuildConfig;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.CrashHandler;
import com.huivip.gpsspeedwidget.utils.FileUtil;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.view.LrcView;

import org.xutils.x;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by laisun on 28/02/2018.
 */

public class LyricFloatingService extends Service{
    public static final String EXTRA_CLOSE = "com.huivip.gpsspeedwidget.EXTRA_CLOSE";
    public static final String EXTRA_HIDE="com.huivip.gpsspeed.EXTRA_HIDE";
    public static final String EXTRA_FIXED="com.huivip.gpsspeed.EXTRA_FIXED";
    public static final String EXTRA_SHOW="com.huivip.gpsspeed.EXTRA_SHOW";
    public static final String UPDATE_LYRICS_ACTION="GPSWIDGET_UPDATE_LYRICS";
    public static final String CLICKED_FLOATING_ACTION = "clicked_action";
    public static final String SHOW_TIME ="TextFlating_ShowTime";
    public static final String SHOW_TEXT ="TextFlating_ShowText";
    public static final String TARGET="LYRICFLoating";
    public static String SONGNAME="lyric.songName";
    public static String ARTIST="lyric.artist";
    public static String POSITION="lyfic.position";
    public static String STATUS="lyric.status";
    public static String DURATION="lyric.duration";
    public static String LYRIC_CONTENT="lyric.content";
    private WindowManager mWindowManager;
    private View mFloatingView;
    long startTime;
    AudioManager audioManager;
    String lyrcContent;
    String songName;
    String artistName;
    TimerTask lyricTask;
    long duration=0;
    Timer lyricTimer;
    boolean isKuwoPlayer;
    final Handler lyricHandler = new Handler();
    boolean isShowing=false;
    @BindView(R.id.lrc_floatting_view)
    LrcView lrcView;
    @BindView(R.id.lyric_control)
    View controlView;
    @BindView(R.id.layout_lyric)
    ViewGroup lyricView;
    WindowManager.LayoutParams params;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent!=null){
            if (intent.getBooleanExtra(EXTRA_CLOSE, false)) {
                onStop();
                stopSelf();
                return super.onStartCommand(intent, flags, startId);
            }
            lyrcContent=intent.getStringExtra(LYRIC_CONTENT);//FileUtil.loadLyric(getApplicationContext(),inputSongName,inputArtistName);
            long position=intent.getLongExtra(POSITION,0L);
            duration=intent.getLongExtra(DURATION,-1L);
            songName=intent.getStringExtra(SONGNAME);
            artistName=intent.getStringExtra(ARTIST);
            startTime=System.currentTimeMillis()-position;//-1000;
            lrcView.setLrc(lyrcContent);
            lrcView.setHighLineColor(AppSettings.get().getLyricFontColor());
            lrcView.init();
            isShowing = true;
            if(!AppSettings.get().isLyricFixed()) {
                hideControlView();
            } else {
                controlView.setVisibility(View.INVISIBLE);
            }

        }
        this.lyricTask = new TimerTask() {
            @Override
            public void run() {
                LyricFloatingService.this.lyricHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        calTime();
                    }
                });
            }
        };
        this.lyricTimer.schedule(this.lyricTask, 0L, 500L);
        return Service.START_REDELIVER_INTENT;
    }

    private void calTime() {
        long position = System.currentTimeMillis() - startTime;
       /* if(isKuwoPlayer){
            position = mKwapi.getCurrentPos();
        }*/
        lrcView.setPlayercurrentMillis((int) position);
        if (duration > 0 && (position + 1000) >= duration) {
            onStop();
            stopSelf();
        }
        if(!audioManager.isMusicActive()){
            onStop();
            stopSelf();
        }
    }
    private void onStop(){
        if(mFloatingView!=null && mWindowManager!=null){
            try {
                mWindowManager.removeView(mFloatingView);
            }catch (Exception e){

            }
        }
        isShowing=false;
    }
    @OnClick(value = {R.id.imageView_lyrc_floating_close,R.id.imageView_lyrc_floating_fixed,R.id.imageView_lyrc_floating_delete})
    public void closeFloating(View view){
        switch (view.getId()){
            case R.id.imageView_lyrc_floating_close:
                onStop();
                stopSelf();
                break;
            case R.id.imageView_lyrc_floating_fixed:
                params.flags=WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                mFloatingView.setLayoutParams(params);
                controlView.setVisibility(View.INVISIBLE);
                mWindowManager.updateViewLayout(mFloatingView, params);
                //PrefUtils.setEnableLyricFloatingFixed(getApplicationContext(),true);
                AppSettings.get().setLyricFixed(true);
                break;
            case R.id.imageView_lyrc_floating_delete:
                FileUtil.deleteLyric(getApplicationContext(),songName,artistName);
                break;
        }
    }
    public static void sendIntent(Context context, Intent intent) {
        context.sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        if(!PrefUtils.isEnableDrawOverFeature(getApplicationContext())){
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
        int flag= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        if(AppSettings.get().isLyricFixed()){
            flag=flag | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        }
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                getWindowType(),
                flag,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.alpha = PrefUtils.getOpacity(getApplicationContext()) / 100.0F;
        ButterKnife.bind(this, mFloatingView);
        mWindowManager.addView(mFloatingView, params);
        mFloatingView.setOnTouchListener( new FloatingOnTouchListener());
        initMonitorPosition();
        CrashHandler.getInstance().init(getApplicationContext());
        lyricTimer = new Timer();
        super.onCreate();
    }
    //遍历设置字体
    public static void changeViewSize(ViewGroup viewGroup, int screenWidth, int screenHeight) {//传入Activity顶层Layout,屏幕宽,屏幕高
        int adjustFontSize = adjustFontSize(screenWidth, screenHeight);
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View v = viewGroup.getChildAt(i);
            if (v instanceof ViewGroup) {
                changeViewSize((ViewGroup) v, screenWidth, screenHeight);
            } else if (v instanceof Button) {//按钮加大这个一定要放在TextView上面，因为Button也继承了TextView
                ((Button) v).setTextSize(adjustFontSize + 2);
            } else if (v instanceof TextView) {
                ((TextView) v).setTextSize(adjustFontSize);
            }
        }
    }


    //获取字体大小
    public static int adjustFontSize(int screenWidth, int screenHeight) {
        screenWidth=screenWidth>screenHeight?screenWidth:screenHeight;
        /**
         * 1. 在视图的 onsizechanged里获取视图宽度，一般情况下默认宽度是320，所以计算一个缩放比率
         rate = (float) w/320   w是实际宽度
         2.然后在设置字体尺寸时 paint.setTextSize((int)(8*rate));   8是在分辨率宽为320 下需要设置的字体大小
         实际字体大小 = 默认字体大小 x  rate
         */
        int rate = (int)(5*(float) screenWidth/320); //我自己测试这个倍数比较适合，当然你可以测试后再修改
        return rate<15?15:rate; //字体太小也不好看的
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
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    controlView.setVisibility(View.VISIBLE);
                    hideControlView();
                    PrefUtils.setLyrcFloatingSolidLocation(getApplicationContext(), params.x, params.y);
                    return true;
            }
            return false;
        }
    }
    private void hideControlView(){
        x.task().postDelayed(new Runnable() {
            @Override
            public void run() {
                controlView.setVisibility(View.INVISIBLE);
            }
        }, 5000);
    }
}
