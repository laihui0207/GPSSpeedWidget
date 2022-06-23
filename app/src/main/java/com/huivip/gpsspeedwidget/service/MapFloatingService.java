package com.huivip.gpsspeedwidget.service;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.MapView;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.huivip.gpsspeedwidget.BuildConfig;
import com.huivip.gpsspeedwidget.GpsUtil;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.beans.DriveWayEvent;
import com.huivip.gpsspeedwidget.beans.GetDistrictEvent;
import com.huivip.gpsspeedwidget.beans.RoadLineEvent;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.CrashHandler;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.view.CarOverlay;
import com.huivip.gpsspeedwidget.view.DriveWayLinear;
import com.huivip.gpsspeedwidget.view.ImageWheelView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by laisun on 28/02/2018.
 */

public class MapFloatingService extends Service {
    public static final String EXTRA_CLOSE = "com.huivip.gpsspeedwidget.EXTRA_CLOSE";
    private WindowManager mWindowManager;
    private View mFloatingView;
    int windowHeight=0;
    //@BindView(R.id.map_floating)
    MapView mMapView ;
    AMap aMap=null;
    @BindView(R.id.textview_floating_map_title)
    TextView timeTextView;
    //TimeThread timeThread;
    private Marker carMarker;
    //LBSTraceClient mTraceClient=null;
    @BindView(R.id.button_map_close)
    Button closeButton;
    @BindView(R.id.textView_close_floating_map)
    TextView closeTextView;
    TimerTask locationScanTask;
    Timer locationTimer = new Timer();
    final Handler locationHandler = new Handler();
    GpsUtil gpsUtil;
    float mapZoom=16f;
    int mapMove=-100;
    @BindView(R.id.imageView_xunhang_roadLine)
    ImageView xunHang_roadLine;
    @BindView(R.id.imageView_daohang_roadLine)
    ImageView daoHang_roadLine;
    CoordinateConverter converter;
    // 是否需要跟随定位s
    private boolean isNeedFollow = true;
    boolean isLocated=false;
    BroadcastReceiver timeChangeBroadcastReceiver;
    CarOverlay carOverlay;
    @BindView(R.id.floating_map_driverWay)
    DriveWayLinear driveWayLinear;
    // 处理静止后跟随的timer
    private Timer needFollowTimer;
    // 屏幕静止DELAY_TIME之后，再次跟随
    private long DELAY_TIME = 5000;
    boolean focused=false;
  /*  private ServiceConnection mServiceConnection;
    RoadLineService.RoadLineBinder roadLineBinder;*/
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
        }
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if(hour>=17 || hour<7) {
            aMap.setMapType(AMap.MAP_TYPE_NIGHT);
        } else {
            aMap.setMapType(AMap.MAP_TYPE_NAVI);
        }
        EventBus.getDefault().post(new GetDistrictEvent());
        return Service.START_REDELIVER_INTENT;
    }
    private void onStop(){
        if(mFloatingView!=null && mWindowManager!=null){
            mWindowManager.removeView(mFloatingView);
        }
       // timeThread.running=false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate() {
        MapsInitializer.updatePrivacyShow(this,true,true);
        MapsInitializer.updatePrivacyAgree(this,true);
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
        mFloatingView = inflater.inflate(R.layout.floating_map_window, null);
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
        timeTextView.setOnTouchListener(new FloatingOnTouchListener());
        initMonitorPosition();
        //timeThread=new TimeThread(timeTextView);
        CrashHandler.getInstance().init(getApplicationContext());
        mMapView =  mFloatingView.findViewById(R.id.map_floating);
        Bundle savedInstanceState=new Bundle();
        if(mMapView!=null) {
            mMapView.onCreate(savedInstanceState);
            aMap = mMapView.getMap();
            if (gpsUtil.isNight()) {
                aMap.setMapType(AMap.MAP_TYPE_NIGHT);
            } else {
                aMap.setMapType(AMap.MAP_TYPE_NAVI);
            }
            carMarker = aMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(getBitmap(0f))).setFlat(true));
            aMap.setTrafficEnabled(true);
            aMap.showBuildings(true);
        }
        UiSettings mUiSettings=aMap.getUiSettings();
        mUiSettings.setCompassEnabled(false);
        mUiSettings.setZoomControlsEnabled(false);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setScaleControlsEnabled(true);
        closeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStop();
                stopSelf();
            }
        });
        aMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
            @Override
            public void onMapLoaded() {
                aMap.setPointToCenter(mMapView.getWidth()/2, mMapView.getHeight()-120);

                aMap.moveCamera(CameraUpdateFactory.zoomTo(16f));
                aMap.moveCamera(CameraUpdateFactory.changeTilt(60));
                carOverlay = new CarOverlay(getApplicationContext(), mMapView);
                carOverlay.draw(mMapView.getMap(), new LatLng(Double.parseDouble(gpsUtil.getLatitude()), Double.parseDouble(gpsUtil.getLongitude())),gpsUtil.getBearing());

            }
        });
        setMapInteractiveListener();
        converter = new CoordinateConverter(getApplicationContext());
        converter.from(CoordinateConverter.CoordType.GPS);
        this.locationScanTask = new TimerTask()
        {
            @Override
            public void run()
            {
                MapFloatingService.this.locationHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        MapFloatingService.this.checkLocationData();
                    }
                });
            }
        };
        this.locationTimer.schedule(this.locationScanTask, 0L, 1000L);
        EventBus.getDefault().register(this);
        updateTime();
        timeChangeBroadcastReceiver =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateTime();
            }
        };
        IntentFilter timeFilter=new IntentFilter(Intent.ACTION_TIME_TICK);
        timeFilter.addAction(Intent.ACTION_TIME_CHANGED);
        timeFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        getApplicationContext().registerReceiver(timeChangeBroadcastReceiver,timeFilter);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(timeChangeBroadcastReceiver !=null){
            getApplicationContext().unregisterReceiver(timeChangeBroadcastReceiver);
        }
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }

    }
    private void updateTime(){
        SimpleDateFormat sdf = new SimpleDateFormat(AppSettings.get().getTimeWindowDateFormat(), Locale.CHINA);
        String date = sdf.format(new Date());
        if(gpsUtil.isNight()){
            timeTextView.setTextColor(Color.WHITE);
        }
        else {
            timeTextView.setTextColor(Color.RED);
        }
        timeTextView.setText(date);
    }
    void checkLocationData() {
        if ((gpsUtil != null && gpsUtil.isGpsEnabled() && gpsUtil.isGpsLocationStarted()) || !isLocated) {
            if(gpsUtil==null){
                return;
            }
            if (TextUtils.isEmpty(gpsUtil.getLongitude()) || TextUtils.isEmpty(gpsUtil.getLatitude())) {
                return;
            }
            LatLng latLng = new LatLng(Double.parseDouble(gpsUtil.getLatitude()), Double.parseDouble(gpsUtil.getLongitude()));
            converter.coord(latLng);
            LatLng lastedLatLng = converter.convert();
            if(gpsUtil.getKmhSpeed()>60){
                mapZoom=14;
                mapMove=-(windowHeight*4/25);
            } else {
                mapZoom=16;
                mapMove=-((windowHeight-10)*4/25);
            }
            if (isNeedFollow) {
                mMapView.getMap().moveCamera(CameraUpdateFactory.zoomTo(mapZoom));
                if (carOverlay != null) {
                    carOverlay.draw(mMapView.getMap(), lastedLatLng, gpsUtil.getBearing());
                }
                isLocated=true;
            }

        }
    }
    @Subscribe(threadMode=ThreadMode.MAIN)
    public void showRoadLine(RoadLineEvent event) {
        if (event.isShowed()) {
            View vv = event.getRoadLineView();
            if (vv != null) {
                daoHang_roadLine.setImageDrawable(((ImageView) vv).getDrawable());
                daoHang_roadLine.setVisibility(View.VISIBLE);
            }
        } else {
            daoHang_roadLine.setVisibility(View.INVISIBLE);
        }
    }

    private Bitmap getBitmap(float bearing) {
        Bitmap bitmap = null;
        View view = View.inflate(this,R.layout.floating_map_navi_icon, null);
        ImageWheelView directionView=view.findViewById(R.id.imageview_direction);
        directionView.setRotation(360-bearing);
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        bitmap = view.getDrawingCache();
        return bitmap;
    }
    @Subscribe
    public void driveWayUpdate(DriveWayEvent driveWayEvent) {
        if (driveWayEvent.isEnable()) {
            driveWayLinear.setVisibility(View.VISIBLE);
            driveWayLinear.buildDriveWay(driveWayEvent.getLaneInfo());
        } else {
            driveWayLinear.hide();
            driveWayLinear.setVisibility(View.INVISIBLE);
        }

    }
    private void setMapInteractiveListener() {

        aMap.setOnMapTouchListener(new AMap.OnMapTouchListener() {

            @Override
            public void onTouch(MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 按下屏幕
                        // 如果timer在执行，关掉它
                        clearTimer();
                        // 改变跟随状态
                        isNeedFollow = false;
                        break;

                    case MotionEvent.ACTION_UP:
                        // 离开屏幕
                        startTimerSomeTimeLater();
                        break;

                    default:
                        break;
                }
            }
        });

    }
    /**
     * 取消timer任务
     */
    private void clearTimer() {
        if (needFollowTimer != null) {
            needFollowTimer.cancel();
            needFollowTimer = null;
        }
    }

    /**
     * 如果地图在静止的情况下
     */
    private void startTimerSomeTimeLater() {
        // 首先关闭上一个timer
        clearTimer();
        needFollowTimer = new Timer();
        // 开启一个延时任务，改变跟随状态
        needFollowTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                isNeedFollow = true;
            }
        }, DELAY_TIME);
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
                String[] xy = PrefUtils.getMapFloatingSolidLocation(getApplicationContext()).split(",");
                params.x = (int) Float.parseFloat(xy[0]);
                params.y = (int) Float.parseFloat(xy[1]);
                try {
                    mWindowManager.updateViewLayout(mFloatingView, params);
                } catch (IllegalArgumentException ignore) {
                }

                mFloatingView.setVisibility(View.VISIBLE);
                //windowHeight = params.height;
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
                   /* if(PrefUtils.isEnableNaviFloatingFixed(getApplicationContext())){
                        return true;
                    }*/
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
                        PrefUtils.setMapFloatingSolidLocation(getApplicationContext(), params.x, params.y);
                    }
                    return true;
            }
            return false;
        }
    }
}
