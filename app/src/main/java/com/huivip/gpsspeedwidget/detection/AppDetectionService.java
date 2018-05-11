package com.huivip.gpsspeedwidget.detection;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import com.huivip.gpsspeedwidget.*;
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
        if(!PrefUtils.isEnableFlatingWindow(getApplicationContext())){
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
        boolean onAutoNavi=false;
        gpsUtil=GpsUtil.getInstance(getApplicationContext());
       // when in auto navi or auto navi lite app, temp disable audio service
       if(componentName.getPackageName().equalsIgnoreCase(Constant.AMAPAUTOLITEPACKAGENAME)
               || componentName.getPackageName().equalsIgnoreCase(Constant.AMAPAUTOPACKAGENAME)){
           PrefUtils.setEnableTempAudioService(getApplicationContext(),false);
           onAutoNavi=true;
       }
      /* else {
           gpsUtil=GpsUtil.getInstance(getApplicationContext());
           if(gpsUtil.getAutoNaviStatus()!=Constant.Navi_Status_Started) {
               PrefUtils.setEnableTempAudioService(getApplicationContext(), true);
           }
       }*/

        boolean onDesktop = enabledApps.contains(componentName.getPackageName());
        PrefUtils.setOnDesktop(getApplicationContext(),onDesktop);
        Intent floatService = new Intent(this, FloatingService.class);
        Intent AutoNavifloatService=new Intent(this,AutoNaviFloatingService.class);
        Intent meterFloatingService=new Intent(this,MeterFloatingService.class);
        if(!PrefUtils.isEnableFlatingWindow(getApplicationContext())){
            floatService.putExtra(FloatingService.EXTRA_CLOSE, true);
            AutoNavifloatService.putExtra(FloatingService.EXTRA_CLOSE, true);
            meterFloatingService.putExtra(FloatingService.EXTRA_CLOSE, true);
        }

        if(PrefUtils.getShowFlatingOn(getApplicationContext()).equalsIgnoreCase(PrefUtils.SHOW_NO_DESKTOP) && onDesktop){
            floatService.putExtra(FloatingService.EXTRA_CLOSE, true);
            AutoNavifloatService.putExtra(FloatingService.EXTRA_CLOSE, true);
            meterFloatingService.putExtra(FloatingService.EXTRA_CLOSE, true);
        }

        if(!onDesktop && PrefUtils.getShowFlatingOn(getApplicationContext()).equalsIgnoreCase(PrefUtils.SHOW_ONLY_DESKTOP)){
            floatService.putExtra(FloatingService.EXTRA_CLOSE, true);
            AutoNavifloatService.putExtra(FloatingService.EXTRA_CLOSE, true);
            meterFloatingService.putExtra(FloatingService.EXTRA_CLOSE, true);
        }
        if (PrefUtils.getShowFlatingOn(getApplicationContext()).equalsIgnoreCase(PrefUtils.SHOW_ONLY_AUTONAVI) &&
                (gpsUtil.getAutoNaviStatus()!=Constant.Navi_Status_Started || !onAutoNavi)){
            floatService.putExtra(FloatingService.EXTRA_CLOSE, true);
            AutoNavifloatService.putExtra(FloatingService.EXTRA_CLOSE, true);
            meterFloatingService.putExtra(FloatingService.EXTRA_CLOSE, true);
        }


        try {
            String floatingStyle=PrefUtils.getFloatingStyle(getApplicationContext());
            if(floatingStyle.equalsIgnoreCase(PrefUtils.FLOATING_DEFAULT)){
               /* meterFloatingService.putExtra(MeterFloatingService.EXTRA_CLOSE,true);
                AutoNavifloatService.putExtra(FloatingService.EXTRA_CLOSE, true);*/
                startService(floatService);
            } else if(floatingStyle.equalsIgnoreCase(PrefUtils.FLOATING_AUTONAVI)) {
               /* floatService.putExtra(FloatingService.EXTRA_CLOSE, true);
                meterFloatingService.putExtra(MeterFloatingService.EXTRA_CLOSE,true);*/
                startService(AutoNavifloatService);
            } else if(floatingStyle.equalsIgnoreCase(PrefUtils.FLOATING_METER)){
               /* AutoNavifloatService.putExtra(FloatingService.EXTRA_CLOSE, true);
                floatService.putExtra(FloatingService.EXTRA_CLOSE, true);*/
                startService(meterFloatingService);
            }
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