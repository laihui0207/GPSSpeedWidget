package com.huivip.gpsspeedwidget.service;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Service;
import android.appwidget.AppWidgetHost;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.huivip.gpsspeedwidget.BuildConfig;
import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.GpsUtil;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.utils.CrashHandler;
import com.huivip.gpsspeedwidget.utils.PrefUtils;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by laisun on 28/02/2018.
 */

public class AutoWidgetFloatingService extends Service {
    public static final String EXTRA_CLOSE = "com.huivip.gpsspeedwidget.EXTRA_CLOSE";
    public static final String DRIVE_WAY = "com.huivip.gpssppeedWidget.drive_way";
    private WindowManager mWindowManager;
    private View mFloatingView;
    GpsUtil gpsUtil;
    @BindView(R.id.autoDriveWayView)
    LinearLayout driveWayView;
    @BindView(R.id.imageView_autoNvi_close)
    ImageView closeImage;
    @BindView(R.id.imageView_autoNvi_move)
    ImageView moveImage;
    AppWidgetHost appWidgetHost;
    TimerTask locationScanTask;
    Timer locationTimer = new Timer();
    Handler handler = new Handler();
    private ServiceConnection mServiceConnection;
    RoadLineService.RoadLineBinder roadLineBinder;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.getBooleanExtra(EXTRA_CLOSE, false) || !PrefUtils.isEnableAutoWidgetFloatingWidow(getApplicationContext())
                /*|| gpsUtil.getAutoNaviStatus()!=Constant.Navi_Status_Started*/) {
                onStop();
                stopSelf();
                return super.onStartCommand(intent, flags, startId);
            }
        }
        return Service.START_REDELIVER_INTENT;
    }

    private void onStop() {
        if (this.locationTimer != null) {
            locationTimer.cancel();
            locationTimer.purge();
        }
        if (mFloatingView != null && mWindowManager != null) {
            mWindowManager.removeView(mFloatingView);
        }

    }

    private void showPluginContent() {
       if(roadLineBinder!=null){
           View view=roadLineBinder.getWidgetView();
           if(driveWayView.getChildCount()>0) {
               driveWayView.removeAllViews();
           }
           if(view!=null){
               if(view.getParent()!=null) {
                   ((ViewGroup) view.getParent()).removeView(view);
               }
               driveWayView.addView(view);
           }
       }
    }

    @Override
    public void onCreate() {
        if (!PrefUtils.isEnableDrawOverFeature(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), "需要打开GPS插件的悬浮窗口权限", Toast.LENGTH_LONG).show();
            try {
                openSettings(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, BuildConfig.APPLICATION_ID);
            } catch (ActivityNotFoundException ignored) {
            }
            return;
        }
        appWidgetHost = new AppWidgetHost(getApplicationContext(), Constant.APP_WIDGET_HOST_ID);
        gpsUtil = GpsUtil.getInstance(getApplicationContext());
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater inflater = LayoutInflater.from(this);
        mFloatingView = inflater.inflate(R.layout.floating_auto_widget, null);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                getWindowType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        // params.alpha = 0.9f;//PrefUtils.getOpacity(getApplicationContext()) / 100.0F;
        ButterKnife.bind(this, mFloatingView);
        mWindowManager.addView(mFloatingView, params);
        driveWayView.setOnTouchListener(new FloatingOnTouchListener());
        initMonitorPosition();
        mServiceConnection=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                roadLineBinder= (RoadLineService.RoadLineBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        getApplicationContext().bindService(new Intent(getApplicationContext(), RoadLineService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        this.locationScanTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        showPluginContent();
                    }
                });
            }
        };
        this.locationTimer.schedule(this.locationScanTask, 0L, 1000L);
        CrashHandler.getInstance().init(getApplicationContext());

        //moveImage.setOnTouchListener(new FloatingOnTouchListener());
       // EventBus.getDefault().register(this);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /*if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }*/
    }

    @OnClick(value = {R.id.imageView_autoNvi_close})
    public void onCloseEvent(View view) {
        onStop();
        stopSelf();
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
                String[] split = PrefUtils.getDriveWayFloatingLocation(getApplicationContext()).split(",");
                boolean left = Boolean.parseBoolean(split[0]);
                float yRatio = Float.parseFloat(split[1]);
                /*if (PrefUtils.isNaviFloattingAutoSolt(getApplicationContext()) && !PrefUtils.isEnableNaviFloatingFixed(getApplicationContext())) {
                    Point screenSize = new Point();
                    mWindowManager.getDefaultDisplay().getSize(screenSize);
                    params.x = left ? 0 : screenSize.x - mFloatingView.getWidth();
                    params.y = (int) (yRatio * screenSize.y + 0.5f);
                } else {*/
                    String[] xy = PrefUtils.getDriveWayFloatingSolidLocation(getApplicationContext()).split(",");
                    params.x = (int) Float.parseFloat(xy[0]);
                    params.y = (int) Float.parseFloat(xy[1]);
               /* }*/
                try {
                    mWindowManager.updateViewLayout(mFloatingView, params);
                } catch (IllegalArgumentException ignore) {
                }

                mFloatingView.setVisibility(View.VISIBLE);

                mFloatingView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    /* private void animateViewToSideSlot() {
         Point screenSize = new Point();
         mWindowManager.getDefaultDisplay().getSize(screenSize);

         WindowManager.LayoutParams params = (WindowManager.LayoutParams) mFloatingView.getLayoutParams();
         int endX;
         if (params.x + mFloatingView.getWidth() / 2 >= screenSize.x / 2) {
             endX = screenSize.x - mFloatingView.getWidth();
         } else {
             endX = 0;
         }

         PrefUtils.setDriveWayFloatingLocation(getApplicationContext(), (float) params.y / screenSize.y, endX == 0);
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
     }*/
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
                    } else {
                        PrefUtils.setDriveWayFloatingSolidLocation(getApplicationContext(), params.x, params.y);
                    }
                    return true;
            }
            return false;
        }
    }
}
