package com.huivip.gpsspeedwidget.service;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.huivip.gpsspeedwidget.BuildConfig;
import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.GpsUtil;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.activity.ConfigurationActivity;
import com.huivip.gpsspeedwidget.activity.MainActivity;
import com.huivip.gpsspeedwidget.beans.AutoMapStatusUpdateEvent;
import com.huivip.gpsspeedwidget.beans.NaviInfoUpdateEvent;
import com.huivip.gpsspeedwidget.beans.RoadLineEvent;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.CrashHandler;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;
import com.huivip.gpsspeedwidget.view.SpeedWheel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class AutoNaviFloatingService extends Service {
    public static final String EXTRA_CLOSE = "com.huivip.gpsspeedwidget.EXTRA_CLOSE";
    public static final String TARGET="com.huivip.gpsSpeedWidget.speedFloating";
    private WindowManager mWindowManager;
    private View mFloatingView;
    @BindView(R.id.imageView_pointer)
    SpeedWheel speedWheelView;
    @BindView(R.id.SpeedText)
    TextView speedView;
    @BindView(R.id.imageview_nomove_night)
    ImageView moveImageView;
    @BindView(R.id.imageview_red_night)
    ImageView speedOveralView;
    @BindView(R.id.autoNavi_limitLayout)
    View limitView;
    @BindView(R.id.textView_autoNavi_distance)
    TextView limitDistanceTextView;
    @BindView(R.id.autoNavi_number_limit)
    TextView limitTextView;
    @BindView(R.id.textView_autoNavi_limit_label)
    TextView limitTypeTextView;
    @BindView(R.id.autoNavi_progressBarLimit)
    ProgressBar limitProgressBar;
    @BindView(R.id.textView_autonavi_direction)
    TextView directionTextView;
    @BindView(R.id.textView_autonavi_altitude)
    TextView altitudeTextView;
    @BindView(R.id.textView_autonavi_speedUnit)
    TextView speedUnitTextView;
    @BindView(R.id.navi_roadLine)
    ImageView roadLineView;
    TimerTask locationScanTask;
    Timer locationTimer = new Timer();
    final Handler locationHandler = new Handler();
    GpsUtil gpsUtil;
    boolean naviStarted=false;

    /* private ServiceConnection mServiceConnection;
    RoadLineService.RoadLineBinder roadLineBinder;*/
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null){
            boolean enableFloatingService= AppSettings.get().isEnableSpeed();
            boolean userClosedService=PrefUtils.isUserManualClosedService(getApplicationContext());
            if (!enableFloatingService || userClosedService || intent.getBooleanExtra(EXTRA_CLOSE, false)) {
                onStop();
                stopSelf();
                return super.onStartCommand(intent, flags, startId);
            }
            gpsUtil.startLocationService();
        }
        return Service.START_REDELIVER_INTENT;
    }
    private void onStop(){
        if(mFloatingView!=null && mWindowManager!=null){
            try {
                Utils.delayLaunchSelf(getApplicationContext(),TARGET,180000L,"START");
                mWindowManager.removeView(mFloatingView);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if(locationTimer!=null){
            locationTimer.cancel();
            locationTimer.purge();
        }
       /* if(gpsUtil!= null) {
            gpsUtil.stopLocationService(false);
        }*/
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
        if(AppSettings.get().isSpeedSmallShow()){
            mFloatingView = inflater.inflate(R.layout.floating_autonavi_small, null);
        } else{
            mFloatingView = inflater.inflate(R.layout.floating_autonavi, null);
        }
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
        //speedWheelView.setRotation((float)(50/100d*280f));
        initMonitorPosition();
        this.locationScanTask = new TimerTask()
        {
            @Override
            public void run()
            {
                locationHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        AutoNaviFloatingService.this.checkLocationData();
                        //showRoadLine();
                    }
                });
            }
        };
        /*this.roadLineTask=new TimerTask() {
            @Override
            public void run() {
                roadLineHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showRoadLine();
                    }
                });
            }
        };*/
        CrashHandler.getInstance().init(getApplicationContext());
       /* mServiceConnection=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                roadLineBinder= (RoadLineService.RoadLineBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        if(PrefUtils.isEnableSpeedRoadLine(getApplicationContext())) {
            getApplicationContext().bindService(new Intent(getApplicationContext(), RoadLineService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        }*/
        this.locationTimer.schedule(this.locationScanTask, 0L, 100L);
       // this.roadLineTimer.schedule(this.roadLineTask,0,1000L);
        speedView.setOnTouchListener(new FloatingOnTouchListener());
        EventBus.getDefault().register(this);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
    }

    @OnClick(value = {R.id.image_home_navi,R.id.image_company_navi,R.id.image_main_navi,R.id.image_close_navi,R.id.SpeedRelativeLayout})
    public void goHomeClick(View view){
        switch (view.getId()){
            case R.id.image_home_navi:
                sendBroadcast(sendAutoBroadCase(getApplicationContext(),10040,0));
                break;
            case R.id.image_company_navi:
                sendBroadcast(sendAutoBroadCase(getApplicationContext(),10040,1));
                break;
            case R.id.image_main_navi:
                Intent intent=new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            case R.id.image_close_navi:
                onStop();
                stopSelf();
                break;
            case R.id.SpeedRelativeLayout:
                Intent floatingMapIntent;
                Toast.makeText(getApplicationContext(), "click event",Toast.LENGTH_SHORT).show();
                if (gpsUtil.getAutoNaviStatus() == Constant.Navi_Status_Started) {
                    floatingMapIntent = new Intent(getApplicationContext(), AutoWidgetFloatingService.class);
                    if(Utils.isServiceRunning(getApplicationContext(),AutoWidgetFloatingService.class.getName())){
                        floatingMapIntent.putExtra(AutoWidgetFloatingService.EXTRA_CLOSE, true);
                    }
                } else {
                    floatingMapIntent = new Intent(getApplicationContext(), MapFloatingService.class);
                    if(Utils.isServiceRunning(getApplicationContext(),MapFloatingService.class.getName())){
                        floatingMapIntent.putExtra(MapFloatingService.EXTRA_CLOSE, true);
                    }
                }
                startService(floatingMapIntent);
                break;
        }
    }

    @Subscribe(threadMode= ThreadMode.MAIN)
    public void showRoadLine(RoadLineEvent event) {
        if (AppSettings.get().isShowRoadLineOnSpeed() && event.isShowed()) {
            View vv = event.getRoadLineView();
            if (vv != null) {
                roadLineView.setImageDrawable(((ImageView) vv).getDrawable());
                roadLineView.setVisibility(View.VISIBLE);
            }
        } else {
            roadLineView.setVisibility(View.INVISIBLE);
        }
    }
    private Intent sendAutoBroadCase(Context context, int key,int type){
        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", key);
        if(key==10040) {
            intent.putExtra("DEST", type);
            intent.putExtra("IS_START_NAVI", 0);
        }
        intent.putExtra("SOURCE_APP","GPSWidget");
        return intent;
    }
    void checkLocationData() {
        if (gpsUtil != null && gpsUtil.isGpsEnabled() && gpsUtil.isGpsLocationStarted()) {
            speedView.setText(gpsUtil.getKmhSpeedStr() + "");
            //speedWheelView.setRotation((float) (gpsUtil.getSpeedometerPercentage() / 100d * 280f));
            speedWheelView.setRotation(360-gpsUtil.getBearing());
            setSpeedOveral(gpsUtil.isHasLimited());
            directionTextView.setText(gpsUtil.getDirection() + "");
            altitudeTextView.setText("海拔： " + gpsUtil.getAltitude() + "米");

        } else {
            speedView.setText("...");
        }
    }
    public void setSpeedOveral(boolean speeding) {
        if(!AppSettings.get().isAmapShowLimit()){
            speedOveralView.setVisibility(View.GONE);
            return;
        }
        int colorRes = speeding ? R.color.red500 : R.color.primary_text_default_material_light;
        int color = ContextCompat.getColor(this, colorRes);
        speedView.setTextColor(color);
        speedOveralView.setVisibility(speeding ? View.VISIBLE : View.GONE);
        if(!naviStarted) {
            limitView.setVisibility((gpsUtil.getLimitDistance() > 0 || gpsUtil.getLimitSpeed() > 0) ? View.VISIBLE : View.GONE);
            limitTextView.setText(gpsUtil.getLimitSpeed() + "");
            setLimit(gpsUtil.getLimitDistancePercentage(), gpsUtil.getLimitDistance());
            limitTypeTextView.setText(gpsUtil.getCameraTypeName());
            if (TextUtils.isEmpty(gpsUtil.getCurrentRoadName())) {
                speedUnitTextView.setText("km/h");
            } else {
                speedUnitTextView.setText(gpsUtil.getCurrentRoadName());
            }
        }
    }
    @Subscribe
    public void updateNaviStatus(AutoMapStatusUpdateEvent event){
        this.naviStarted=event.isDaoHangStarted();
        if(!naviStarted){
            setLimit(0,0);
        }
    }
    @Subscribe
    public void updateNaviInfo(NaviInfoUpdateEvent event) {
        if (gpsUtil.getAutoNaviStatus() != Constant.Navi_Status_Started) return;
        speedUnitTextView.setText(event.getCurRoadName());
        int limitDistancePercentage=0;
        if (event.getLimitDistance()<300 && event.getLimitDistance()>0) {
            limitDistancePercentage = Math.round((300F -event.getLimitDistance() ) / 300 * 100);
        }
        limitView.setVisibility(event.getLimitDistance()>0   ? View.VISIBLE : View.GONE);
        if(event.getCameraSpeed()>0) {
            limitTextView.setText(event.getLimitSpeed() + "");
        }
        gpsUtil.setCameraSpeed(event.getLimitSpeed());
        setLimit(limitDistancePercentage,event.getLimitDistance());
        gpsUtil.setCameraType(event.getLimitType());
        limitTypeTextView.setText(gpsUtil.getCameraTypeName());
    }
    public void setLimit(int limitDistancePercentage,int limitDistance){
        limitDistanceTextView.setText(limitDistance+"");
        limitProgressBar.setProgress(limitDistancePercentage);
    }
   /* public void setSpeed(String speed) {
        if (PrefUtils.getShowSpeedometer(this) && speedView != null) {
           speedView.setText(speed);
        }
    }*/
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
                String[] split = PrefUtils.getFloatingLocation(getApplicationContext()).split(",");
                boolean left = Boolean.parseBoolean(split[0]);
                float yRatio = Float.parseFloat(split[1]);
                if(AppSettings.get().isSpeedAutoKeepSide() && !PrefUtils.isEnableSpeedFloatingFixed(getApplicationContext())) {
                    Point screenSize = new Point();
                    mWindowManager.getDefaultDisplay().getSize(screenSize);
                    params.x = left ? 0 : screenSize.x - mFloatingView.getWidth();
                    params.y = (int) (yRatio * screenSize.y + 0.5f);
                }
                else {
                    String[] xy=PrefUtils.getFloatingSolidLocation(getApplicationContext()).split(",");
                    params.x=(int)Float.parseFloat(xy[0]);
                    params.y=(int)Float.parseFloat(xy[1]);
                }
                try {
                    mWindowManager.updateViewLayout(mFloatingView, params);
                } catch (IllegalArgumentException ignore) {
                }

                mFloatingView.setVisibility(View.VISIBLE);

                mFloatingView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }
    private void animateViewToSideSlot() {
        Point screenSize = new Point();
        mWindowManager.getDefaultDisplay().getSize(screenSize);

        WindowManager.LayoutParams params = (WindowManager.LayoutParams) mFloatingView.getLayoutParams();
        int endX;
        if (params.x + mFloatingView.getWidth() / 2 >= screenSize.x / 2) {
            endX = screenSize.x - mFloatingView.getWidth();
        } else {
            endX = 0;
        }

        PrefUtils.setFloatingLocation(getApplicationContext(), (float) params.y / screenSize.y, endX == 0);
        ValueAnimator valueAnimator = ValueAnimator.ofInt(params.x, endX)
                .setDuration(300);
        valueAnimator.setInterpolator(new LinearOutSlowInInterpolator());
        valueAnimator.addUpdateListener(animation -> {
            WindowManager.LayoutParams params1 = (WindowManager.LayoutParams) mFloatingView.getLayoutParams();
            params1.x = (int) animation.getAnimatedValue();
            try {
                mWindowManager.updateViewLayout(mFloatingView, params1);
            } catch (IllegalArgumentException ignore) {
            }
        });

        valueAnimator.start();
    }
    private class FloatingOnTouchListener implements View.OnTouchListener {

        private float mInitialTouchX;
        private float mInitialTouchY;
        private int mInitialX;
        private int mInitialY;
        private long mStartClickTime;
        private boolean mIsClick;

        private AnimatorSet fadeAnimator;
        private float initialAlpha;
        private ValueAnimator fadeOut;
        private ValueAnimator fadeIn;

        public FloatingOnTouchListener() {
            final WindowManager.LayoutParams params = (WindowManager.LayoutParams) mFloatingView.getLayoutParams();
            fadeOut = ValueAnimator.ofFloat(params.alpha, 0.1F);
            fadeOut.setInterpolator(new FastOutSlowInInterpolator());
            fadeOut.setDuration(100);
            fadeOut.addUpdateListener(valueAnimator -> {
                params.alpha = (float) valueAnimator.getAnimatedValue();
                try {
                    mWindowManager.updateViewLayout(mFloatingView, params);
                } catch (IllegalArgumentException ignore) {
                }
            });
            fadeIn = fadeOut.clone();
            fadeIn.setFloatValues(0.1F, params.alpha);
            fadeIn.setStartDelay(5000);
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
                    if(PrefUtils.isEnableSpeedFloatingFixed(getApplicationContext())){
                        return true;
                    }
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
               /* case MotionEvent.ACTION_POINTER_UP:
                    Toast.makeText(getApplicationContext(),"双指单击关闭悬浮窗",Toast.LENGTH_SHORT).show();
                    onStop();
                    stopSelf();
                    return true;*/
                case MotionEvent.ACTION_UP:
                    if (mIsClick && System.currentTimeMillis() - mStartClickTime <= ViewConfiguration.getLongPressTimeout()) {
                      /*  if (fadeAnimator != null && fadeAnimator.isaMapstarted()) {
                            fadeAnimator.cancel();
                            params.alpha = initialAlpha;
                            try {
                                mWindowManager.updateViewLayout(mFloatingView, params);
                            } catch (IllegalArgumentException ignore) {
                            }
                        } else {
                            initialAlpha = params.alpha;

                            fadeAnimator = new AnimatorSet();
                            fadeAnimator.play(fadeOut).before(fadeIn);
                            fadeAnimator.start();
                            *//*Intent timeIntent =new Intent(getApplicationContext(),MapFloatingService.class);
                            startService(timeIntent);*//*

                        }*/
                        if(!Utils.isServiceRunning(getApplicationContext(), MapFloatingService.class.getName())) {
                            Intent timeIntent = new Intent(getApplicationContext(), MapFloatingService.class);
                            startService(timeIntent);
                        }
                    }
                    else {
                        if(AppSettings.get().isSpeedAutoKeepSide() && !PrefUtils.isEnableSpeedFloatingFixed(getApplicationContext())) {
                            animateViewToSideSlot();
                        } else {
                            PrefUtils.setFloatingSolidLocation(getApplicationContext(),params.x,params.y);
                        }

                    }
                    if(mIsClick && (event.getEventTime()- event.getDownTime())> ViewConfiguration.getLongPressTimeout()) {
                        if(PrefUtils.isEnableSpeedFloatingFixed(getApplicationContext())) {
                            Toast.makeText(getApplicationContext(), "取消悬浮窗口固定功能", Toast.LENGTH_SHORT).show();
                            PrefUtils.setEnableSpeedFloatingFixed(getApplicationContext(), false);
                        }
                        Intent configActivity=new Intent(getApplicationContext(), ConfigurationActivity.class);
                        configActivity.setFlags(FLAG_ACTIVITY_NEW_TASK);
                        startActivity(configActivity);
                    }
                    return true;
            }
            return false;
        }
    }
}
