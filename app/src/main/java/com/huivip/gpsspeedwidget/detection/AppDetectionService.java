package com.huivip.gpsspeedwidget.detection;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.FloatingService;
import com.huivip.gpsspeedwidget.GpsUtil;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import java.util.Set;

public class AppDetectionService extends AccessibilityService {

    private static AppDetectionService INSTANCE;

    private Set<String> enabledApps;
    private GpsUtil gpsUtil;
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

        if (event.getPackageName() == null
                || event.getClassName() == null
                || enabledApps == null) {
            return;
        }
        ComponentName componentName = new ComponentName(
                event.getPackageName().toString(),
                event.getClassName().toString()
        );
        boolean isActivity = componentName.getPackageName().toLowerCase().contains("activity")
                || tryGetActivity(componentName) != null;

        if (!isActivity) {
            return;
        }
       // when in auto navi or auto navi lite app, temp disable audio service
       if(componentName.getPackageName().equalsIgnoreCase(Constant.AMAPAUTOLITEPACKAGENAME)
               || componentName.getPackageName().equalsIgnoreCase(Constant.AMAPAUTOPACKAGENAME)){
           PrefUtils.setEnableTempAudioService(getApplicationContext(),false);
       }
       else {
           gpsUtil=GpsUtil.getInstance(getApplicationContext());
           if(gpsUtil.getAutoNaviStatus()!=Constant.Navi_Status_Started) {
               PrefUtils.setEnableTempAudioService(getApplicationContext(), true);
           }
       }

        boolean shouldStopService = enabledApps.contains(componentName.getPackageName());
        PrefUtils.setOnDesktop(getApplicationContext(),shouldStopService);
        Intent floatService=new Intent(this, FloatingService.class);
        if(!PrefUtils.isEnableFlatingWindow(getApplicationContext())){
            floatService.putExtra(FloatingService.EXTRA_CLOSE, true);
        } else if(shouldStopService && PrefUtils.getShowFlatingOn(getApplicationContext()).equalsIgnoreCase(PrefUtils.SHOW_NO_DESKTOP)){
            floatService.putExtra(FloatingService.EXTRA_CLOSE, true);
        } else if(!shouldStopService && PrefUtils.getShowFlatingOn(getApplicationContext()).equalsIgnoreCase(PrefUtils.SHOW_ONLY_DESKTOP)){
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