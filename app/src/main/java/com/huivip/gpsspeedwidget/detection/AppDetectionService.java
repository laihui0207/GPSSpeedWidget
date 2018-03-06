package com.huivip.gpsspeedwidget.detection;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
/*import com.crashlytics.android.Crashlytics;*/
import com.huivip.gpsspeedwidget.BuildConfig;
import com.huivip.gpsspeedwidget.FloatingService;
import com.huivip.gpsspeedwidget.GpsSpeedService;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
/*import com.pluscubed.velociraptor.BuildConfig;*/
/*import com.pluscubed.velociraptor.limit.LimitService;*/
import timber.log.Timber;

import java.util.List;
import java.util.Set;

public class AppDetectionService extends AccessibilityService {

    public static final String GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps";
    public static final String GMAPS_BOTTOM_CONTAINER_ID = "com.google.android.apps.maps:id/bottommapoverlay_container";
    public static final String GMAPS_SPEED_LIMIT_TEXT = "SPEED LIMIT";
    private static AppDetectionService INSTANCE;

    private Set<String> enabledApps;

    public static AppDetectionService get() {
        return INSTANCE;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        INSTANCE = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        INSTANCE = null;
    }

    public void updateSelectedApps() {
        enabledApps = PrefUtils.getApps(getApplicationContext());
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (enabledApps == null) {
            updateSelectedApps();
        }

      /*  if (event.getPackageName() == null
                || event.getClassName() == null
                || enabledApps == null) {
            return;
        }*/

        ComponentName componentName = new ComponentName(
                event.getPackageName().toString(),
                event.getClassName().toString()
        );
        Log.d("huivip","PackageName:"+event.getPackageName().toString()+",className:"+componentName.getClassName());
      /*  boolean isActivity = componentName.getPackageName().toLowerCase().contains("activity")
                || tryGetActivity(componentName) != null;

        if (!isActivity) {
            return;
        }*/

        boolean shouldStopService = enabledApps.contains(componentName.getPackageName());
        Log.d("huivip","Should Stop:"+shouldStopService);
        Intent floatService=new Intent(this, FloatingService.class);
        if (shouldStopService) {
            floatService.putExtra(FloatingService.EXTRA_CLOSE, true);
        }

        try {
            startService(floatService);
        } catch (Exception e) {
            Log.d("huivip","Start Floating server Failed"+e.getMessage());
        }
    }

    private ActivityInfo tryGetActivity(ComponentName componentName) {
        try {
            return getPackageManager().getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Override
    public void onInterrupt() {
    }
}