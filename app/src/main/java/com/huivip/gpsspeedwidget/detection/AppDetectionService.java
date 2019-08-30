package com.huivip.gpsspeedwidget.detection;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.view.accessibility.AccessibilityEvent;

import com.huivip.gpsspeedwidget.GpsUtil;
import com.huivip.gpsspeedwidget.service.BootStartService;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;

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

    public void updateDesktopApps() {
        enabledApps = PrefUtils.getApps(getApplicationContext());
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (enabledApps == null) {
            updateDesktopApps();
        }
        if(!Utils.isServiceRunning(getApplicationContext(), BootStartService.class.getName())){
            Intent bootService=new Intent(getApplicationContext(),BootStartService.class);
            bootService.putExtra(BootStartService.START_BOOT,true);
            startService(bootService);
        }
        if(!AppSettings.get().isEnableSpeed()){
            return ;
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
       /* if (componentName.getPackageName().equalsIgnoreCase(Constant.AMAPAUTOLITEPACKAGENAME)
                || componentName.getPackageName().equalsIgnoreCase(Constant.AMAPAUTOPACKAGENAME)) {
            gpsUtil=GpsUtil.getInstance(getApplicationContext());
            gpsUtil.setAutoNavi_on_Frontend(true);
        }*/

        boolean onDesktop = enabledApps.contains(componentName.getPackageName());
        PrefUtils.setOnDesktop(getApplicationContext(),onDesktop);
        Utils.startFloatingWindows(getApplicationContext(),true);
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
