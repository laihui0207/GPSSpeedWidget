package com.huivip.gpsspeedwidget;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.view.*;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import devlight.io.library.ArcProgressStackView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by laisun on 28/02/2018.
 */

public class FloatingService extends Service{
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
    TimerTask locationScanTask;
    Timer locationTimer = new Timer();
    final Handler locationHandler = new Handler();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null){
            boolean enableFloatingService=PrefUtils.isEnableFlatingWindow(getApplicationContext());
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
            mWindowManager.removeView(mFloatingView);
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
        gpsUtil=GpsUtil.getInstance(getApplicationContext());
        mWindowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater inflater = LayoutInflater.from(this);
        mFloatingView = inflater.inflate(R.layout.floating_international, null);
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
        boolean isShowLimit=PrefUtils.getShowLimits(getApplicationContext());
        mLimitView.setVisibility(isShowLimit ? View.VISIBLE : View.GONE);
        boolean isShowSpeed=PrefUtils.getShowSpeedometer(getApplicationContext());
        mSpeedometerView.setVisibility(isShowSpeed ? View.VISIBLE : View.GONE);
        if(PrefUtils.isFloattingDirectionHorizontal(getApplicationContext())) {
            RelativeLayout.LayoutParams speedLayout = (RelativeLayout.LayoutParams) mSpeedometerView.getLayoutParams();
            speedLayout.addRule(RelativeLayout.RIGHT_OF, R.id.limit);
            speedLayout.removeRule(RelativeLayout.BELOW);
            mSpeedometerView.setLayoutParams(speedLayout);
        } else {
            RelativeLayout.LayoutParams speedLayout = (RelativeLayout.LayoutParams) mSpeedometerView.getLayoutParams();
            speedLayout.addRule(RelativeLayout.BELOW, R.id.limit);
            speedLayout.removeRule(RelativeLayout.RIGHT_OF);
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
                FloatingService.this.locationHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        FloatingService.this.checkLocationData();
                        //Log.d("huivip","Float Service Check Location");
                    }
                });
            }
        };
        this.locationTimer.schedule(this.locationScanTask, 0L, 100L);
        super.onCreate();
    }

    void checkLocationData() {
        if (gpsUtil!=null && gpsUtil.isGpsEnabled() && gpsUtil.isGpsLocationStarted() ) {
            if(gpsUtil.isGpsLocationChanged()){
                setSpeed(gpsUtil.getKmhSpeedStr(),gpsUtil.getSpeedometerPercentage());
                mLimitText.setText(Integer.toString(gpsUtil.getLimitSpeed()));
                setSpeeding(gpsUtil.isHasLimited());
                setLimit(gpsUtil.getLimitDistancePercentage());

                mSpeedDirectionText.setText(gpsUtil.getDirection());
            }
        }
        else {
           mSpeedometerText.setText("--");
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
                mFloatingimitDistance.setText(Float.toString(gpsUtil.getLimitDistance())+"米");
            }
            else {
                mFloatingimitDistance.setText("");
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
                if(PrefUtils.isFloattingAutoSolt(getApplicationContext())) {
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

    public void setSpeeding(boolean speeding) {
        int colorRes = speeding ? R.color.red500 : R.color.primary_text_default_material_light;
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
                    if (mIsClick && System.currentTimeMillis() - mStartClickTime <= ViewConfiguration.getLongPressTimeout()) {
                        if (fadeAnimator != null && fadeAnimator.isStarted()) {
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
                        }
                    } else {
                        if(PrefUtils.isFloattingAutoSolt(getApplicationContext())) {
                             animateViewToSideSlot();
                        } else {
                            PrefUtils.setFloatingSolidLocation(getApplicationContext(),params.x,params.y);
                        }
                    }
                    return true;
            }
            return false;
        }
    }
}
