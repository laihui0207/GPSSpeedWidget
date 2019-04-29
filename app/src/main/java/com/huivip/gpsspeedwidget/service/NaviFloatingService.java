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
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.text.TextUtils;
import android.view.*;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.huivip.gpsspeedwidget.BuildConfig;
import com.huivip.gpsspeedwidget.GpsUtil;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.activity.ConfigurationActivity;
import com.huivip.gpsspeedwidget.beans.TMCSegment;
import com.huivip.gpsspeedwidget.beans.TMCSegmentEvent;
import com.huivip.gpsspeedwidget.utils.CrashHandler;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.view.TmcSegmentView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by laisun on 28/02/2018.
 */

public class NaviFloatingService extends Service{
    public static final String EXTRA_CLOSE = "com.huivip.gpsspeedwidget.EXTRA_CLOSE";

    private WindowManager mWindowManager;
    private View mFloatingView;
    GpsUtil gpsUtil;
    TimerTask locationScanTask;
    Timer locationTimer = new Timer();
    final Handler locationHandler = new Handler();

    @BindView(R.id.textView_currentroad)
    TextView currentRoadTextView;
    @BindView(R.id.textView_nextroadname)
    TextView nextRoadNameTextView;
    @BindView(R.id.textView_nextdistance)
    TextView nextRoadDistanceTextView;

    @BindView(R.id.textView_totalleft)
    TextView naviLeftTextView;
    @BindView(R.id.imageView_turnicon)
    ImageView naveIconImageView;
    @BindView(R.id.navi_number_limit)
    TextView navicameraSpeedTextView;
    @BindView(R.id.textView_navi_distance)
    TextView navicameraDistanceTextView;
    @BindView(R.id.navi_progressBarLimit)
    ProgressBar limitDistanceProgressBar;
    @BindView(R.id.textView_navi_limit_label)
    TextView cameraTypeNameTextView;
    @BindView(R.id.navi_limit_view)
    View naviCameraView;
    @BindView(R.id.textView_autonavi_speedText)
    TextView speedTextView;
    @BindView(R.id.lukuanview)
    TmcSegmentView tmcSegmentView;
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
        }
        EventBus.getDefault().register(this);
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
        EventBus.getDefault().unregister(this);
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
        gpsUtil=GpsUtil.getInstance(getApplicationContext());
        mWindowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater inflater = LayoutInflater.from(this);
        mFloatingView = inflater.inflate(R.layout.floating_backend_navi, null);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                getWindowType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
       /* params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                | WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;*/
        params.alpha = PrefUtils.getOpacity(getApplicationContext()) / 100.0F;
        ButterKnife.bind(this, mFloatingView);
        mWindowManager.addView(mFloatingView, params);
        mFloatingView.setOnTouchListener( new FloatingOnTouchListener());
        initMonitorPosition();
        this.locationScanTask = new TimerTask()
        {
            @Override
            public void run()
            {
                NaviFloatingService.this.locationHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        NaviFloatingService.this.checkLocationData();
                        //Log.d("huivip","Float Service Check Location");
                    }
                });
            }
        };
        this.locationTimer.schedule(this.locationScanTask, 0L, 100L);
        CrashHandler.getInstance().init(getApplicationContext());
        /*naveIconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(sendAutoBroadCase(getApplicationContext(),10034,100));
            }
        });*/
        super.onCreate();
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
        if(!TextUtils.isEmpty(gpsUtil.getCurrentRoadName())){
            currentRoadTextView.setText(gpsUtil.getCurrentRoadName()+"");
        }
        if(!TextUtils.isEmpty(gpsUtil.getNextRoadName())){
            nextRoadNameTextView.setText(gpsUtil.getNextRoadName());
        }
        nextRoadDistanceTextView.setText(gpsUtil.getNextRoadDistance());
        naviLeftTextView.setText(gpsUtil.getTotalLeftDistance()+"/"+gpsUtil.getTotalLeftTime());
        if(gpsUtil.getNavi_turn_icon()>0) {
            naveIconImageView.setImageResource(getTurnIcon(gpsUtil.getNavi_turn_icon()));
        }
        if(gpsUtil.getLimitSpeed()>0){
            navicameraSpeedTextView.setText(gpsUtil.getLimitSpeed()+"");
        }
        else {
            navicameraSpeedTextView.setText("0");
        }
        if(gpsUtil.getLimitDistance()>0){
            navicameraDistanceTextView.setText(gpsUtil.getLimitDistance()+"米");
            limitDistanceProgressBar.setProgress(gpsUtil.getLimitDistancePercentage());
        }
        else {
            navicameraDistanceTextView.setText("0米");
        }
        cameraTypeNameTextView.setText(gpsUtil.getCameraTypeName());
        if(gpsUtil.getCameraType()!=-1){
            naviCameraView.setVisibility(View.VISIBLE);
        }
        else {
            naviCameraView.setVisibility(View.GONE);
        }
        speedTextView.setText(gpsUtil.getKmhSpeedStr());
        int colorRes = gpsUtil.isHasLimited() ? R.color.red500 : R.color.cardview_light_background;
        int color = ContextCompat.getColor(this, colorRes);
        speedTextView.setTextColor(color);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(TMCSegmentEvent event) {
        if (event == null || event.getTmcSegment() == null) return;
        TMCSegment tmcSegment = event.getTmcSegment();
        List<TmcSegmentView.SegmentModel> models = new ArrayList<>();
        for (TMCSegment.TmcInfo tmcInfo : tmcSegment.getTmc_info()) {
            models.add(new TmcSegmentView.SegmentModel()
                    .setDistance(tmcInfo.getTmc_segment_distance())
                    .setStatus(tmcInfo.getTmc_status())
                    .setNumber(tmcInfo.getTmc_segment_number()));
        }
        tmcSegmentView.setSegments(models);
    }
    private int getWindowType() {
        return WindowManager.LayoutParams.TYPE_SYSTEM_ALERT | WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY ;
    }
    private void openSettings(String settingsAction, String packageName) {
        Intent intent = new Intent(settingsAction);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("package:" + packageName));
        startActivity(intent);
    }
    private int getTurnIcon(int iconValue){
        int returnValue=-1;
        switch (iconValue) {
            case 1:
                returnValue = R.drawable.sou0_night;
                break;
            case 2:
                returnValue = R.drawable.sou2_night;
                break;
            case 3:
                returnValue = R.drawable.sou3_night;
                break;
            case 4:
                returnValue = R.drawable.sou4_night;
                break;
            case 5:
                returnValue = R.drawable.sou5_night;
                break;
            case 6:
                returnValue = R.drawable.sou6_night;
                break;
            case 7:
                returnValue = R.drawable.sou7_night;
                break;
            case 8:
                returnValue = R.drawable.sou8_night;
                break;
            case 9:
                returnValue = R.drawable.sou9_night;
                break;
            case 10:
                returnValue = R.drawable.sou10_night;
                break;
            case 11:
                returnValue = R.drawable.sou11_night;
                break;
            case 12:
                returnValue = R.drawable.sou12_night;
                break;
            case 13:
                returnValue = R.drawable.sou13_night;
                break;
            case 14:
                returnValue = R.drawable.sou14_night;
                break;
            case 15:
                returnValue = R.drawable.sou15_night;
                break;
            case 16:
                returnValue = R.drawable.sou16_night;
                break;
            case 17:
                returnValue = R.drawable.sou17_night;
                break;
            case 18:
                returnValue = R.drawable.sou18_night;
                break;
            case 19:
                returnValue = R.drawable.sou19_night;
                break;
            case 20:
                returnValue = R.drawable.sou20_night;
                break;
        }
        return returnValue;
    }
    private void initMonitorPosition() {
        if (mFloatingView == null) {
            return;
        }
        mFloatingView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                WindowManager.LayoutParams params = (WindowManager.LayoutParams) mFloatingView.getLayoutParams();
                String[] split = PrefUtils.getNaviFloatingLocation(getApplicationContext()).split(",");
                boolean left = Boolean.parseBoolean(split[0]);
                float yRatio = Float.parseFloat(split[1]);
                if(PrefUtils.isNaviFloattingAutoSolt(getApplicationContext()) && !PrefUtils.isEnableNaviFloatingFixed(getApplicationContext())) {
                    Point screenSize = new Point();
                    mWindowManager.getDefaultDisplay().getSize(screenSize);
                    params.x = left ? 0 : screenSize.x - mFloatingView.getWidth();
                    params.y = (int) (yRatio * screenSize.y + 0.5f);
                }
                else {
                    String[] xy=PrefUtils.getNaviFloatingSolidLocation(getApplicationContext()).split(",");
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

        PrefUtils.setNaviFloatingLocation(getApplicationContext(), (float) params.y / screenSize.y, endX == 0);
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
                    if(PrefUtils.isEnableNaviFloatingFixed(getApplicationContext())){
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
                    }
                    else if(mIsClick && System.currentTimeMillis() - mStartClickTime > 1000) {

                    }
                    else {
                        if(PrefUtils.isNaviFloattingAutoSolt(getApplicationContext()) && !PrefUtils.isEnableNaviFloatingFixed(getApplicationContext())) {
                             animateViewToSideSlot();
                        } else {
                            PrefUtils.setNaviFloatingSolidLocation(getApplicationContext(),params.x,params.y);
                        }
                    }
                    if(mIsClick && (event.getEventTime()- event.getDownTime())> ViewConfiguration.getLongPressTimeout()) {
                        if(PrefUtils.isEnableNaviFloatingFixed(getApplicationContext())) {
                            Toast.makeText(getApplicationContext(),"取消悬浮窗口固定功能",Toast.LENGTH_SHORT).show();
                            PrefUtils.setEnableNaviFloatingFixed(getApplicationContext(),false);
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
