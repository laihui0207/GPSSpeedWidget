package com.huivip.gpsspeedwidget.service;

import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.huivip.gpsspeedwidget.BuildConfig;
import com.huivip.gpsspeedwidget.GpsUtil;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.beans.RoadLineEvent;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.CrashHandler;
import com.huivip.gpsspeedwidget.utils.PrefUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
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

public class RoadLineFloatingService extends Service{
    public static final String EXTRA_CLOSE = "com.huivip.gpsspeedwidget.EXTRA_CLOSE";
    public static final String EXTRA_Fixed = "com.huivip.gpsspeedwidget.EXTRA_FIXED";
    private WindowManager mWindowManager;
    private View mFloatingView;
    @BindView(R.id.roadline_control)
    View controlView;
    GpsUtil gpsUtil;
    @BindView(R.id.imageView_floating_roadLine)
    ImageView roadLineView;
    TimerTask timerTask;
    Timer timer = new Timer();
    @BindView(R.id.textView_roadLine_roadName)
    TextView roadName;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
        gpsUtil=GpsUtil.getInstance(getApplicationContext());
        mWindowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater inflater = LayoutInflater.from(this);
        mFloatingView = inflater.inflate(R.layout.floating_roadline_window, null);
        int flag= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        if(AppSettings.get().isRoadLineFixed()){
            flag=flag | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        }
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
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
        EventBus.getDefault().register(this);
        CrashHandler.getInstance().init(getApplicationContext());
        timerTask=new TimerTask() {
            @Override
            public void run() {
                x.task().autoPost(()->{
                    if(TextUtils.isEmpty(gpsUtil.getCurrentRoadName())){
                        roadName.setVisibility(View.GONE);
                    } else {
                        roadName.setVisibility(View.VISIBLE);
                        roadName.setText(gpsUtil.getCurrentRoadName());
                    }
                });
            }
        };
        timer.schedule(timerTask,0L,1000L);
        super.onCreate();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null){
            if (intent.getBooleanExtra(EXTRA_CLOSE, false) || !AppSettings.get().isEnableRoadLineFloatingWindow()) {
                onStop();
                stopSelf();
                return super.onStartCommand(intent, flags, startId);
            }
            if(intent.getBooleanExtra(EXTRA_Fixed,false)){
                WindowManager.LayoutParams params = (WindowManager.LayoutParams) mFloatingView.getLayoutParams();
                int flag=WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE ;
                if(PrefUtils.isEnableRoadLineFloatingFixed(getApplicationContext())){
                    flag = flag | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                }
                params.flags=flag;
                mFloatingView.setLayoutParams(params);
                mWindowManager.updateViewLayout(mFloatingView, params);
            }
            if(!PrefUtils.isEnableRoadLineFloatingFixed(getApplicationContext())) {
                hideControlView();
            } else {
                controlView.setVisibility(View.INVISIBLE);
            }

        }
        return Service.START_REDELIVER_INTENT;
    }
    private void onStop(){
        if(mFloatingView!=null && mWindowManager!=null){
            try {
                mWindowManager.removeView(mFloatingView);
            }catch (Exception e){

            }
        }
    }
    @Subscribe(threadMode= ThreadMode.MAIN)
    public void showRoadLine(RoadLineEvent event) {
        if (event.isShowed()) {
            View vv = event.getRoadLineView();
            if (vv != null) {
                roadLineView.setImageDrawable(((ImageView) vv).getDrawable());
                roadLineView.setScaleType(ImageView.ScaleType.FIT_XY);
                roadLineView.setVisibility(View.VISIBLE);
            }
        } else {
            roadLineView.setVisibility(View.INVISIBLE);
        }
    }
    @OnClick(value = {R.id.imageView_roadLine_floating_fixed,R.id.imageView_roadLine_floating_close})
    public void clickHandler(View view){
        switch (view.getId()){
            case R.id.imageView_roadLine_floating_fixed:
                WindowManager.LayoutParams params = (WindowManager.LayoutParams) mFloatingView.getLayoutParams();
                params.flags=WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                mFloatingView.setLayoutParams(params);
                controlView.setVisibility(View.INVISIBLE);
                mWindowManager.updateViewLayout(mFloatingView, params);
                //PrefUtils.setEnableRoadLineFloatingFixed(getApplicationContext(),true);
                AppSettings.get().setRoadLineFixed(true);
                break;
            case R.id.imageView_roadLine_floating_close:
                onStop();
                stopSelf();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
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
                    String[] xy=PrefUtils.getRoadLineFloatingSolidLocation(getApplicationContext()).split(",");
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
                    PrefUtils.setRoadLineFloatingSolidLocation(getApplicationContext(), params.x, params.y);
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
