package com.huivip.gpsspeedwidget.utils;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.huivip.gpsspeedwidget.BuildConfig;
import com.huivip.gpsspeedwidget.GpsUtil;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.activity.MainActivity;
import com.huivip.gpsspeedwidget.lyric.LyricService;
import com.huivip.gpsspeedwidget.service.AutoNaviFloatingService;
import com.huivip.gpsspeedwidget.service.DefaultFloatingService;
import com.huivip.gpsspeedwidget.service.LyricFloatingService;
import com.huivip.gpsspeedwidget.service.MeterFloatingService;
import com.huivip.gpsspeedwidget.util.AppSettings;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public abstract class Utils {
    public static final String TRACK_NOTIF_CHANNEL = "track_visible_notif";
    public static final String TRACK_NOTIF_HIDDEN_CHANNEL = "track_hidden_notif";
    private static final String TRANSLATION_CHANNEL = "translation_notif";
    public static final int NOTIFICATION_ID = 6;
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
    public static boolean isServiceRunningInForeground(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }

            }
        }
        return false;
    }
    public static String getDefaultDesktop(Context context){
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        String defaultLauncher=packageManager.resolveActivity(intent,PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
        return defaultLauncher;
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
            //Log.d("huivip","Launcher:"+resolveInfo.activityInfo.packageName);
            //if(!"com.huivip.gpsspeedwidget".equalsIgnoreCase(resolveInfo.activityInfo.packageName)) {
                names.add(resolveInfo.activityInfo.packageName);
           // }
        }
        if(names!=null && names.size()>0) {
            PrefUtils.setDefaultLaunchApp(context,names.get(0));
        }
        return new HashSet<>(names);
    }
    public static View getViewByIds(View view, Object[] ids) {
        View r = view;
        for (int i = 0; i < ids.length; i++) {
            r = getViewById(r, ids[i]);
            if (r == null) {
                return null;
            }
        }
        return r;
    }

    public static View getViewById(View view, Object id) {
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View cc1 = vg.getChildAt(i);
                if (id instanceof Integer) {
                    if (id.equals(i)) {
                        return cc1;
                    }
                } else if (id instanceof String) {
                    if (cc1.toString().endsWith("id/" + id + "}")) {
                        return cc1;
                    }
                }
            }
        }
        return null;
    }
    public static View findlayoutViewById(View view,Object id){
      if(view instanceof ViewGroup){
          for (int i = 0, j = ((ViewGroup)view).getChildCount(); i < j; i++) {
              View child = ((ViewGroup) view).getChildAt(i);
              if(child instanceof ViewGroup){
                  View subView=findlayoutViewById(child,id);
                  if(subView!=null){
                      return subView;
                  }
              } else {
                      if (child.toString().endsWith("id/" + id + "}")) {
                          return child;
                      }
              }
          }
      } else if( view!=null && view.toString().endsWith("id/"+id+"}")){
          return view;
      }
      return null;
    }

    public static void goHome(Context context) {
        Intent paramIntent = new Intent("android.intent.action.MAIN");
        paramIntent.addCategory("android.intent.category.HOME");
        paramIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(paramIntent);
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
    public static int getLocalVersionCode(Context context) {
        PackageManager manager = context.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("huivip", "获取应用程序版本失败，原因：" + e.getMessage());
            return 0;
        }
        return info.versionCode;
    }
    public static int darken(int color, float factor) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        return Color.argb(a,
                Math.max((int)(r * factor), 0),
                Math.max((int)(g * factor), 0),
                Math.max((int)(b * factor), 0));
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
    public static void startFloatingWindows(Context context, boolean enabled){
        Intent defaultFloatingService=new Intent(context, DefaultFloatingService.class);
        Intent autoNaviFloatService=new Intent(context,AutoNaviFloatingService.class);
        Intent meterFloatingService=new Intent(context,MeterFloatingService.class);
        boolean needClose=false;
        if(enabled){
            GpsUtil gpsUtil=GpsUtil.getInstance(context.getApplicationContext());
            boolean onDesktop =PrefUtils.isOnDesktop(context);
            if(!AppSettings.get().isEnableSpeed()){
                defaultFloatingService.putExtra(DefaultFloatingService.EXTRA_CLOSE, true);
                autoNaviFloatService.putExtra(DefaultFloatingService.EXTRA_CLOSE, true);
                meterFloatingService.putExtra(DefaultFloatingService.EXTRA_CLOSE, true);
                needClose=true;
            }

            if(AppSettings.get().isSpeedWiddowNotShowOnDesktop() && onDesktop){
                defaultFloatingService.putExtra(DefaultFloatingService.EXTRA_CLOSE, true);
                autoNaviFloatService.putExtra(DefaultFloatingService.EXTRA_CLOSE, true);
                meterFloatingService.putExtra(DefaultFloatingService.EXTRA_CLOSE, true);
                needClose=true;
            }

           /* if(!onDesktop && PrefUtils.getShowFlatingOn(context).equalsIgnoreCase(PrefUtils.SHOW_ONLY_DESKTOP)){
                defaultFloatingService.putExtra(DefaultFloatingService.EXTRA_CLOSE, true);
                autoNaviFloatService.putExtra(DefaultFloatingService.EXTRA_CLOSE, true);
                meterFloatingService.putExtra(DefaultFloatingService.EXTRA_CLOSE, true);
                needClose=true;
            }*/
           /* if (PrefUtils.getShowFlatingOn(context).equalsIgnoreCase(PrefUtils.SHOW_ONLY_AUTONAVI) &&
                    !gpsUtil.isAutoNavi_on_Frontend()){
                defaultFloatingService.putExtra(DefaultFloatingService.EXTRA_CLOSE, true);
                autoNaviFloatService.putExtra(DefaultFloatingService.EXTRA_CLOSE, true);
                meterFloatingService.putExtra(DefaultFloatingService.EXTRA_CLOSE, true);
                needClose=true;
            }*/
            String floatingStyle=AppSettings.get().getSpeedFlattingStyle();
            if(floatingStyle.equalsIgnoreCase(PrefUtils.FLOATING_DEFAULT)){
                if(Utils.isServiceRunning(context,MeterFloatingService.class.getName())){
                    meterFloatingService.putExtra(MeterFloatingService.EXTRA_CLOSE, true);
                    context.startService(meterFloatingService);
                }
                if(Utils.isServiceRunning(context,AutoNaviFloatingService.class.getName())){
                    autoNaviFloatService.putExtra(DefaultFloatingService.EXTRA_CLOSE, true);
                    context.startService(autoNaviFloatService);
                }
                if(Utils.isServiceRunning(context, DefaultFloatingService.class.getName()) && needClose) {
                    defaultFloatingService.putExtra(DefaultFloatingService.EXTRA_CLOSE,true);
                    context.startService(defaultFloatingService);
                }
                if((!needClose && !Utils.isServiceRunning(context, DefaultFloatingService.class.getName())) ||
                        (PrefUtils.getShowFlatingOn(context).equalsIgnoreCase(PrefUtils.SHOW_ONLY_AUTONAVI) &&
                                gpsUtil.isAutoNavi_on_Frontend())){
                    context.startService(defaultFloatingService);
                }

            } else if(floatingStyle.equalsIgnoreCase(PrefUtils.FLOATING_AUTONAVI)) {
                if(Utils.isServiceRunning(context, DefaultFloatingService.class.getName())){
                    defaultFloatingService.putExtra(DefaultFloatingService.EXTRA_CLOSE, true);
                    context.startService(defaultFloatingService);
                }
                if(Utils.isServiceRunning(context,MeterFloatingService.class.getName())){
                    meterFloatingService.putExtra(MeterFloatingService.EXTRA_CLOSE,true);
                    context.startService(meterFloatingService);
                }
                if(Utils.isServiceRunning(context, AutoNaviFloatingService.class.getName()) && needClose) {
                    autoNaviFloatService.putExtra(DefaultFloatingService.EXTRA_CLOSE, true);
                    context.startService(autoNaviFloatService);
                }
                if((!Utils.isServiceRunning(context,AutoNaviFloatingService.class.getName()) && !needClose) ||
                        (PrefUtils.getShowFlatingOn(context).equalsIgnoreCase(PrefUtils.SHOW_ONLY_AUTONAVI) &&
                                gpsUtil.isAutoNavi_on_Frontend())){
                    context.startService(autoNaviFloatService);
                }
            } else if(floatingStyle.equalsIgnoreCase(PrefUtils.FLOATING_METER)){
                if(Utils.isServiceRunning(context, DefaultFloatingService.class.getName())){
                    defaultFloatingService.putExtra(DefaultFloatingService.EXTRA_CLOSE, true);
                    context.startService(defaultFloatingService);
                }
                if(Utils.isServiceRunning(context,AutoNaviFloatingService.class.getName())){
                    autoNaviFloatService.putExtra(DefaultFloatingService.EXTRA_CLOSE, true);
                    context.startService(autoNaviFloatService);
                }
                if(Utils.isServiceRunning(context,MeterFloatingService.class.getName()) && needClose) {
                     meterFloatingService.putExtra(MeterFloatingService.EXTRA_CLOSE,true);
                    context.startService(meterFloatingService);
                }
                if ((!Utils.isServiceRunning(context,MeterFloatingService.class.getName()) && !needClose) ||
                        (PrefUtils.getShowFlatingOn(context).equalsIgnoreCase(PrefUtils.SHOW_ONLY_AUTONAVI) &&
                                gpsUtil.isAutoNavi_on_Frontend())){
                    context.startService(meterFloatingService);
                }
            }

        }
        else {
            if(Utils.isServiceRunning(context, DefaultFloatingService.class.getName())){
                defaultFloatingService.putExtra(DefaultFloatingService.EXTRA_CLOSE, true);
                context.startService(defaultFloatingService);
            }
            if(Utils.isServiceRunning(context,AutoNaviFloatingService.class.getName())){
                autoNaviFloatService.putExtra(DefaultFloatingService.EXTRA_CLOSE, true);
                context.startService(autoNaviFloatService);
            }
            if(Utils.isServiceRunning(context,MeterFloatingService.class.getName())){
                meterFloatingService.putExtra(MeterFloatingService.EXTRA_CLOSE,true);
                context.startService(meterFloatingService);
            }
        }
    }

    public static void startService(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= 26 && !getDefaultDesktop(context).equalsIgnoreCase(context.getPackageName())) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }
    public static  String longToTimeString(long time){
        long totalSecond=time/1000;
        long hour=totalSecond/3600;
        long minute=(totalSecond-hour*3600)/60;
        long second=(totalSecond - hour*3600-minute*60);
        StringBuffer result=new StringBuffer();
        if(hour>0){
            result.append(hour).append(":");
        }
        result.append(minute).append(":").append(second);
        return result.toString();

    }
    public static int getPrimaryColor(Context context) {
        TypedValue primaryColor = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, primaryColor, true);
        return primaryColor.data;
    }
    public static Notification makeNotification(Context context, String artist, String track, long duration, boolean retentionNotif, boolean isPlaying) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean prefOverlay = sharedPref.getBoolean("pref_overlay", false) && (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context));
        int notificationPref = prefOverlay ? 2 : Integer.valueOf(sharedPref.getString("pref_notifications", "0"));

        Intent activityIntent = new Intent(context.getApplicationContext(), MainActivity.class)
                .setAction("com.geecko.QuickLyric.getLyrics")
                .putExtra("retentionNotif", retentionNotif)
                .putExtra("TAGS", new String[]{artist, track});
        Intent wearableIntent = new Intent("com.geecko.QuickLyric.SEND_TO_WEARABLE")
                .putExtra("artist", artist).putExtra("track", track).putExtra("duration", duration);
        final Intent overlayIntent = new Intent(context.getApplicationContext(), LyricFloatingService.class)
                .setAction(LyricFloatingService.CLICKED_FLOATING_ACTION);

        PendingIntent overlayPending = PendingIntent.getService(context.getApplicationContext(), 0, overlayIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent openAppPending = PendingIntent.getActivity(context.getApplicationContext(), 0, activityIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent wearablePending = PendingIntent.getBroadcast(context.getApplicationContext(), 8, wearableIntent, PendingIntent.FLAG_CANCEL_CURRENT);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(notificationPref == 1 && isPlaying ? TRACK_NOTIF_CHANNEL : TRACK_NOTIF_HIDDEN_CHANNEL,
                    "Lyric",
                    notificationPref == 1 && isPlaying ? NotificationManager.IMPORTANCE_LOW : NotificationManager.IMPORTANCE_MIN);
            notificationChannel.setShowBadge(false);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context.getApplicationContext(), notificationPref == 1 && isPlaying ? TRACK_NOTIF_CHANNEL : TRACK_NOTIF_HIDDEN_CHANNEL);
        NotificationCompat.Builder wearableNotifBuilder = new NotificationCompat.Builder(context.getApplicationContext(), TRACK_NOTIF_HIDDEN_CHANNEL);


        notifBuilder
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(String.format("%s - %s", artist, track))
                .setContentIntent(prefOverlay ? overlayPending : openAppPending)
                .setVisibility(-1) // Notification.VISIBILITY_SECRET
                .setGroup("Lyrics_Notification")
                .setColor(getPrimaryColor(context))
                .setShowWhen(false)
                .setGroupSummary(true);
        if (notificationPref == 2) {
            notifBuilder.setOngoing(true).setPriority(-2); // Notification.PRIORITY_MIN
            wearableNotifBuilder.setPriority(-2);
        } else
            notifBuilder.setPriority(-1); // Notification.PRIORITY_LOW

        Notification notif = notifBuilder.build();

        if (notificationPref == 2 || Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            notif.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        if (notificationPref == 1)
            notif.flags |= Notification.FLAG_AUTO_CANCEL;

        try {
            context.getPackageManager().getPackageInfo("com.google.android.wearable.app", PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        return notif;
    }
    public static void openNotificationWindows(Context context){
      try {
          Intent localIntent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
          //直接跳转到应用通知设置的代码：
          if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
              //localIntent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
              localIntent.putExtra("app_package", context.getPackageName());
              localIntent.putExtra("app_uid", context.getApplicationInfo().uid);
              localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          }/* else if (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            localIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            localIntent.addCategory(Intent.CATEGORY_DEFAULT);
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            localIntent.setData(Uri.parse("package:" + context.getPackageName()));
        }*/ else {
              //4.4以下没有从app跳转到应用通知设置页面的Action，可考虑跳转到应用详情页面,
              localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              if (Build.VERSION.SDK_INT >= 9) {
                  localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                  localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
              } else if (Build.VERSION.SDK_INT <= 8) {
                  localIntent.setAction(Intent.ACTION_VIEW);
                  localIntent.setClassName("com.android.settings", "com.android.setting.InstalledAppDetails");
                  localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
              }
          }
          context.startActivity(localIntent);
      } catch (Exception e){
          Log.d("huivip","Launch Notification Center failed");
      }
    }
/*    @TargetApi(Build.VERSION_CODES.KITKAT)*/
    public static boolean isNotificationEnabled(Context context) {
        ComponentName cn = new ComponentName(context, LyricService.class);
        String flat = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        final boolean enabled = flat != null && flat.contains(cn.flattenToString());
       return enabled;
    }
    private boolean isCallable(Context context,Intent intent) {
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
    public static boolean checkApplicationIfExists(Context context, String packageName) {
        if (packageName == null || "".equals(packageName)) {
            return false;
        }
        PackageInfo packageinfo = null;
        try {
            packageinfo = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            return false;
        }
        return true;
    }

}
