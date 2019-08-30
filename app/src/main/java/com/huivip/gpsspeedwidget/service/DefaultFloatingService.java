package com.huivip.gpsspeedwidget.service;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huivip.gpsspeedwidget.BuildConfig;
import com.huivip.gpsspeedwidget.GpsUtil;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.activity.ConfigurationActivity;
import com.huivip.gpsspeedwidget.beans.RoadLineEvent;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.CrashHandler;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import devlight.io.library.ArcProgressStackView;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by laisun on 28/02/2018.
 */

public class DefaultFloatingService extends Service {
    public static final String EXTRA_CLOSE = "com.huivip.gpsspeedwidget.EXTRA_CLOSE";

    private WindowManager mWindowManager;
    private View mFloatingView;
    @BindView(R.id.limit)
    View mLimitView;
    @BindView(R.id.speedometer)
    View mSpeedometerView;
    GpsUtil gpsUtil;
    @BindView(R.id.arcview)
    ArcProgressStackView mArcView;
    @BindView(R.id.arcviewLimit)
    ArcProgressStackView mLimitArcView;
    @BindView(R.id.speed)
    TextView mSpeedometerText;
    @BindView(R.id.limit_text)
    TextView mLimitText;
    @BindView(R.id.textView_number_direction)
    TextView mSpeedDirectionText;
    @BindView(R.id.textView_floating_distance)
    TextView mFloatingimitDistance;
    @BindView(R.id.limit_show)
    TextView limitShowLabel;
    @BindView(R.id.speedUnits)
    TextView speedUnitTextView;
    @BindView(R.id.textView_default_altitude)
    TextView textViewAltitude;
    @BindView(R.id.textView_currentRoadName)
    TextView textViewCurrentRoadName;
    @BindView(R.id.imageView_default_xunhang_roadLIne)
    ImageView xunHang_roadLine;
    @BindView(R.id.imageView_default_daohang_roadLIne)
    ImageView daoHang_roadLine;
    private ServiceConnection mServiceConnection;
    RoadLineService.RoadLineBinder roadLineBinder;
   /* @BindView(R.id.floating_close)
    ImageView closeImage;*/
    TimerTask locationScanTask;
    TimerTask roadLineTask;
    Timer locationTimer = new Timer();
    Timer roadLineTimer = new Timer();
    final Handler locationHandler = new Handler();
    final Handler roadLineHandler = new Handler();
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
    public void onDestroy() {
        super.onDestroy();
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
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
            mFloatingView = inflater.inflate(R.layout.floating_default_limit_small, null);
        }
        else {
            mFloatingView = inflater.inflate(R.layout.floating_default_limit, null);
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
        mSpeedometerText.setOnTouchListener(new FloatingOnTouchListener());
        boolean isShowLimit=AppSettings.get().isDefaultSpeedShowLimit();
        if(mLimitArcView!=null) {
            mLimitView.setVisibility(isShowLimit ? View.VISIBLE : View.GONE);
        }
        boolean isShowSpeed=AppSettings.get().isDefaultSpeedShowSpeed();
        if(mSpeedometerView!=null) {
            mSpeedometerView.setVisibility(isShowSpeed ? View.VISIBLE : View.GONE);
        }
        if(AppSettings.get().isDefaultSpeedhorizontalShow()) {
            RelativeLayout.LayoutParams speedLayout = (RelativeLayout.LayoutParams) mSpeedometerView.getLayoutParams();
            speedLayout.addRule(RelativeLayout.RIGHT_OF, R.id.limit);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                speedLayout.removeRule(RelativeLayout.BELOW);
            }
            mSpeedometerView.setLayoutParams(speedLayout);
        } else {
            RelativeLayout.LayoutParams speedLayout = (RelativeLayout.LayoutParams) mSpeedometerView.getLayoutParams();
            speedLayout.addRule(RelativeLayout.BELOW, R.id.limit);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                speedLayout.removeRule(RelativeLayout.RIGHT_OF);
            }
            mSpeedometerView.setLayoutParams(speedLayout);
        }
        initMonitorPosition();
        final ArrayList<ArcProgressStackView.Model> models = new ArrayList<>();
        models.add(new ArcProgressStackView.Model("", 0,
                ContextCompat.getColor(this, R.color.colorPrimary800),
                ContextCompat.getColor(this, R.color.colorAccent)));
        mArcView.setTextColor(ContextCompat.getColor(this, android.R.color.transparent));
        mArcView.setInterpolator(new FastOutSlowInInterpolator());
        mArcView.setModels(models);

        final ArrayList<ArcProgressStackView.Model> limitModels = new ArrayList<>();
        limitModels.add(new ArcProgressStackView.Model("", 0,
                        ContextCompat.getColor(this, R.color.red500),
                ContextCompat.getColor(this, R.color.colorPrimary800)));
        mLimitArcView.setTextColor(ContextCompat.getColor(this, android.R.color.transparent));
        mLimitArcView.setInterpolator(new FastOutSlowInInterpolator());
        mLimitArcView.setModels(limitModels);
        //setLimit(30);
        this.locationScanTask = new TimerTask()
        {
            @Override
            public void run()
            {
                locationHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        DefaultFloatingService.this.checkLocationData();
                        //showRoadLine();
                    }
                });
            }
        };
        this.locationTimer.schedule(this.locationScanTask, 0L, 100L);
     /*   mServiceConnection=new ServiceConnection() {
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
        EventBus.getDefault().register(this);
        CrashHandler.getInstance().init(getApplicationContext());
        super.onCreate();
    }
    @OnClick(value = {R.id.floating_close,R.id.speed})
    public void onViewClick(View view){
        switch (view.getId()){
            case R.id.floating_close:
                onStop();
                stopSelf();
                break;
            case R.id.speed:
                if(!Utils.isServiceRunning(getApplicationContext(), MapFloatingService.class.getName())) {
                    Intent floatingMapIntent = new Intent(getApplicationContext(), MapFloatingService.class);
                    startService(floatingMapIntent);
                }
                break;
        }
    }
    private void openSettings(String settingsAction, String packageName) {
        Intent intent = new Intent(settingsAction);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("package:" + packageName));
        startActivity(intent);
    }

    void checkLocationData() {
        if (gpsUtil != null && gpsUtil.isGpsEnabled() && gpsUtil.isGpsLocationStarted()) {
            //if(gpsUtil.isGpsLocationChanged()){
            setSpeed(gpsUtil.getKmhSpeedStr(), gpsUtil.getSpeedometerPercentage());
            mLimitText.setText(Integer.toString(gpsUtil.getLimitSpeed()));
            setSpeeding(gpsUtil.isHasLimited());
            setLimit(gpsUtil.getLimitDistancePercentage());
            if (gpsUtil.getCameraType() > -1) {
                limitShowLabel.setText(gpsUtil.getCameraTypeName());
            } else {
                limitShowLabel.setText("限速");
            }
            mSpeedDirectionText.setText(gpsUtil.getDirection());
            if (TextUtils.isEmpty(gpsUtil.getCurrentRoadName())) {
                speedUnitTextView.setText("km/h");
            } else {
                speedUnitTextView.setText(gpsUtil.getCurrentRoadName());
            }
            textViewCurrentRoadName.setText(gpsUtil.getCurrentRoadName());
            textViewAltitude.setText("海拔" + gpsUtil.getAltitude() + "米");
            // }
        } else {
            mSpeedometerText.setText("--");
        }
    }

/*    private void showRoadLine() {
      if(roadLineBinder!=null){
          View vv=roadLineBinder.getRoadLineView();
          if(vv!=null){
              daoHang_roadLine.setImageDrawable(((ImageView)vv).getDrawable());
              daoHang_roadLine.setVisibility(View.VISIBLE);
          } else {
              daoHang_roadLine.setVisibility(View.INVISIBLE);
          }
      }
    }*/
    @Subscribe(threadMode= ThreadMode.MAIN)
    public void showRoadLine(RoadLineEvent event) {
        if (AppSettings.get().isShowRoadLineOnSpeed() && event.isShowed()) {
            View vv = event.getRoadLineView();
            if (vv != null) {
                daoHang_roadLine.setImageDrawable(((ImageView) vv).getDrawable());
                daoHang_roadLine.setVisibility(View.VISIBLE);
            }
        } else {
            daoHang_roadLine.setVisibility(View.INVISIBLE);
        }
    }
    private int getWindowType() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                WindowManager.LayoutParams.TYPE_PHONE;
    }
    public void setLimit(int percentOfLimit){
        if(PrefUtils.getShowLimits(this)) {
            mLimitArcView.getModels().get(0).setProgress(percentOfLimit);
            mLimitArcView.animateProgress();
            if(gpsUtil.getLimitDistance()>0){
                mFloatingimitDistance.setText(gpsUtil.getLimitDistance()+"米");
            }
            else {
                mFloatingimitDistance.setText(gpsUtil.getDistance());
            }
        }
    }
    public void setSpeed(String speed, int percentOfWarning) {
        if (PrefUtils.getShowSpeedometer(this) && mSpeedometerText != null) {
            mSpeedometerText.setText(speed);
            mArcView.getModels().get(0).setProgress(percentOfWarning);
            mArcView.animateProgress();
        }
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
                if(PrefUtils.isFloattingAutoSolt(getApplicationContext()) && !PrefUtils.isEnableSpeedFloatingFixed(getApplicationContext())) {
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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mFloatingView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
    }

    public void setSpeeding(boolean speeding) {
        int colorRes = speeding ? R.color.red500 : R.color.primary_text_default_material_light;
        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN){
            colorRes = speeding ? R.color.red500 : R.color.cardview_light_background;
        }
        int color = ContextCompat.getColor(this, colorRes);
        mSpeedometerText.setTextColor(color);
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
        private boolean tempMove=false;
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
                case MotionEvent.ACTION_POINTER_UP:
                        Toast.makeText(getApplicationContext(),"双指单击关闭悬浮窗",Toast.LENGTH_SHORT).show();
                        onStop();
                        stopSelf();
                    return true;
                case MotionEvent.ACTION_UP:
                    if (mIsClick && System.currentTimeMillis() - mStartClickTime <= ViewConfiguration.getLongPressTimeout()) {
                       /* if (fadeAnimator != null && fadeAnimator.isStarted()) {
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
                        }*/
                        if(!Utils.isServiceRunning(getApplicationContext(), MapFloatingService.class.getName())) {
                            Intent floatingMapIntent = new Intent(getApplicationContext(), MapFloatingService.class);
                            startService(floatingMapIntent);
                        }
                    }
                    else {
                        if(PrefUtils.isFloattingAutoSolt(getApplicationContext()) && !PrefUtils.isEnableSpeedFloatingFixed(getApplicationContext())) {
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
                    return false;
            }
            return false;
        }
    }
}
