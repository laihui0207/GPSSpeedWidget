package com.huivip.gpsspeedwidget.utils;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import com.huivip.gpsspeedwidget.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public abstract class Utils {

    public static boolean isAccessibilityServiceEnabled(Context context, Class<? extends AccessibilityService> c) {
        int accessibilityEnabled = 0;
        final String service = BuildConfig.APPLICATION_ID + "/" + c.getName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getContentResolver(), android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                splitter.setString(settingValue);
                while (splitter.hasNext()) {
                    String accessibilityService = splitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }

        //Accessibility is disabled

        return false;
    }

    public static int convertDpToPx(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static int convertMphToKmh(int speed) {
        return (int) (speed * 1.609344 + 0.5d);
    }

    public static int convertKmhToMph(int speed) {
        return (int) (speed / 1.609344 + 0.5d);
    }

    public static int compare(int lhs, int rhs) {
        return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
    }

    public static void playBeeps() {
        playTone();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                playTone();
            }
        }, 300);
    }

    private static void playTone() {
        try {
            ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
            toneGen1.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100);
        } catch (RuntimeException ignored) {
        }
    }

    public static String getUnitText(Context context) {
        return getUnitText(context, "");
    }

    public static String getUnitText(Context context, String amount) {
        return PrefUtils.getUseMetric(context) ? context.getString(R.string.kmph, amount) : context.getString(R.string.mph, amount).trim();
    }

  /*  public static void updateFloatingServicePrefs(Context context) {
        if (context != null && isServiceReady(context)) {
            Intent intent = new Intent(context, LimitService.class);
            intent.putExtra(LimitService.EXTRA_PREF_CHANGE, true);
            context.startService(intent);
        }
    }*/
  public static boolean isServiceRunning(Context context, String serviceClassName){
      final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
      final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE); //这个value取任意大于1的值，但返回的列表大小可能比这个值小。
      for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
          String name = runningServiceInfo.service.getClassName();
          if (name.equals(serviceClassName)){
              return true;
          }
      }
      return false;
  }
    public static Set<String> getDesktopPackageName(Context context){
        List<String> names =new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        String defaultLauncher=packageManager.resolveActivity(intent,PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
        Log.d("huivip","Default Launcher:"+defaultLauncher);
        if(!"com.huivip.gpsspeedwidget".equalsIgnoreCase(defaultLauncher)){
            PrefUtils.setDefaultLaunchApp(context,defaultLauncher);
        }
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for(ResolveInfo resolveInfo : list){
            Log.d("huivip","Launcher:"+resolveInfo.activityInfo.packageName);
            if(!"com.huivip.gpsspeedwidget".equalsIgnoreCase(resolveInfo.activityInfo.packageName)) {
                names.add(resolveInfo.activityInfo.packageName);
            }
        }
        if(names!=null && names.size()>0) {
            PrefUtils.setDefaultLaunchApp(context,names.get(0));
        }
        return new HashSet<>(names);
    }
    public static void goHome(Context context){
        PackageManager packageManager = context.getPackageManager();
        Intent intentLauncher = new Intent(Intent.ACTION_MAIN);
        intentLauncher.addCategory(Intent.CATEGORY_HOME);
        String selectDefaultLauncher=packageManager.resolveActivity(intentLauncher,PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
        String defaultLaunch = PrefUtils.getDefaultLanuchApp(context);
        Log.d("huivip","Default launch:"+defaultLaunch+",Select launcher:"+selectDefaultLauncher);
        if (!TextUtils.isEmpty(defaultLaunch) && "com.huivip.gpsspeedwidget".equalsIgnoreCase(selectDefaultLauncher)) {
            Intent launchIntent =context.getPackageManager().getLaunchIntentForPackage(defaultLaunch);
            if (launchIntent != null) {
                Log.d("huivip","Launch default Launcher:"+defaultLaunch);
                launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchIntent);
            }
            else {
                Log.d("huivip","Launch intent is NULL");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d("huivip","Return back!");
                            Instrumentation inst= new Instrumentation();
                            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        } else {
            Intent paramIntent = new Intent("android.intent.action.MAIN");
            paramIntent.addCategory("android.intent.category.HOME");
            paramIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(paramIntent);
        }
    }
    public static boolean isServiceReady(Context context) {
        boolean permissionGranted =
                isLocationPermissionGranted(context);
        @SuppressLint({"NewApi", "LocalSuppress"}) boolean overlayEnabled = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context);
        return permissionGranted && overlayEnabled;
    }

    public static boolean isLocationPermissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }
    public static boolean isStoragePermissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }
    public static boolean isPhonePermissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED;
    }
    public static void installApk(Context mContext, File file) {
        Uri fileUri = Uri.fromFile(file);
        Intent it = new Intent();
        it.setAction(Intent.ACTION_VIEW);
        it.setDataAndType(fileUri, "application/vnd.android.package-archive");
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 防止打不开应用
        mContext.startActivity(it);
    }
    public static String getLocalVersion(Context context) {
        PackageManager manager = context.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("huivip", "获取应用程序版本失败，原因：" + e.getMessage());
            return "";
        }

        return info.versionName;
    }
    public static int levenshteinDistance(CharSequence lhs, CharSequence rhs) {
        int len0 = lhs.length() + 1;
        int len1 = rhs.length() + 1;

        // the array of distances
        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++) cost[i] = i;

        // dynamically computing the array of distances

        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {
            // initial cost of skipping prefix in String s1
            newcost[0] = j;

            // transformation cost for each letter in s0
            for (int i = 1; i < len0; i++) {
                // matching current letters in both strings
                int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;

                // computing cost for each transformation
                int cost_replace = cost[i - 1] + match;
                int cost_insert = cost[i] + 1;
                int cost_delete = newcost[i - 1] + 1;

                // keep minimum cost
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }

            // swap cost/newcost arrays
            int[] swap = cost;
            cost = newcost;
            newcost = swap;
        }

        // the distance is the cost for transforming all letters in both strings
        return cost[len0 - 1];
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
    public static void startFloationgWindows(Context context,boolean enabled){
        Intent defaultFloatingService=new Intent(context,FloatingService.class);
        Intent autoNavifloatService=new Intent(context,AutoNaviFloatingService.class);
        Intent meterFloatingService=new Intent(context,MeterFloatingService.class);
        if(enabled){
            String floatingStyle=PrefUtils.getFloatingStyle(context);
            if(floatingStyle.equalsIgnoreCase(PrefUtils.FLOATING_DEFAULT)){
                if(Utils.isServiceRunning(context,MeterFloatingService.class.getName())){
                    meterFloatingService.putExtra(MeterFloatingService.EXTRA_CLOSE,true);
                    context.startService(meterFloatingService);
                }
                if(Utils.isServiceRunning(context,AutoNaviFloatingService.class.getName())){
                    autoNavifloatService.putExtra(FloatingService.EXTRA_CLOSE, true);
                    context.startService(autoNavifloatService);
                }
                if(!Utils.isServiceRunning(context,FloatingService.class.getName())) {
                    context.startService(defaultFloatingService);
                }

            } else if(floatingStyle.equalsIgnoreCase(PrefUtils.FLOATING_AUTONAVI)) {
                if(Utils.isServiceRunning(context,FloatingService.class.getName())){
                    defaultFloatingService.putExtra(FloatingService.EXTRA_CLOSE, true);
                    context.startService(defaultFloatingService);
                }
                if(Utils.isServiceRunning(context,MeterFloatingService.class.getName())){
                    meterFloatingService.putExtra(MeterFloatingService.EXTRA_CLOSE,true);
                    context.startService(meterFloatingService);
                }
                if(!Utils.isServiceRunning(context,AutoNaviFloatingService.class.getName())) {
                    context.startService(autoNavifloatService);
                }
            } else if(floatingStyle.equalsIgnoreCase(PrefUtils.FLOATING_METER)){
                if(Utils.isServiceRunning(context,FloatingService.class.getName())){
                    defaultFloatingService.putExtra(FloatingService.EXTRA_CLOSE, true);
                    context.startService(defaultFloatingService);
                }
                if(Utils.isServiceRunning(context,AutoNaviFloatingService.class.getName())){
                    autoNavifloatService.putExtra(FloatingService.EXTRA_CLOSE, true);
                    context.startService(autoNavifloatService);
                }
                if(!Utils.isServiceRunning(context,MeterFloatingService.class.getName())) {
                    context.startService(meterFloatingService);
                }
            }

        }
        else {
            if(Utils.isServiceRunning(context,FloatingService.class.getName())){
                defaultFloatingService.putExtra(FloatingService.EXTRA_CLOSE, true);
                context.startService(defaultFloatingService);
            }
            if(Utils.isServiceRunning(context,AutoNaviFloatingService.class.getName())){
                autoNavifloatService.putExtra(FloatingService.EXTRA_CLOSE, true);
                context.startService(autoNavifloatService);
            }
            if(Utils.isServiceRunning(context,MeterFloatingService.class.getName())){
                meterFloatingService.putExtra(MeterFloatingService.EXTRA_CLOSE,true);
                context.startService(meterFloatingService);
            }
        }
    }
}
