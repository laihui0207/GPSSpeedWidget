package com.huivip.gpsspeedwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import com.huivip.gpsspeedwidget.listener.AutoFTPBackupReceiver;
import com.huivip.gpsspeedwidget.listener.AutoLaunchSystemConfigReceiver;
import com.huivip.gpsspeedwidget.listener.ThirdSoftLaunchReceiver;
import com.huivip.gpsspeedwidget.listener.WeatherServiceReceiver;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;

import java.util.Set;

public class BootStartService extends Service {
    public static String START_BOOT="FromSTARTBOOT";
    boolean started=false;
    AlarmManager alarm;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        alarm = (AlarmManager) getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null && !started){
            boolean start = PrefUtils.isEnableAutoStart(getApplicationContext());
            if(start) {
                started=true;
/*                if(intent.getBooleanExtra(START_BOOT,false)) {*/
                    PendingIntent thirdIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), ThirdSoftLaunchReceiver.class), 0);
                    alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,  300L, thirdIntent);

                    PendingIntent autoLaunchIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), AutoLaunchSystemConfigReceiver.class), 0);
                    alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 5000L, autoLaunchIntent);

                    PendingIntent autoFtpBackupIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), AutoFTPBackupReceiver.class), 0);
                    alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 120000L, autoFtpBackupIntent);
/*                }*/

                PendingIntent weatcherServiceIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), WeatherServiceReceiver.class), 0);
                alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 60000L, weatcherServiceIntent);

                if(PrefUtils.isWidgetActived(getApplicationContext())) {
                    Intent service = new Intent(getApplicationContext(), GpsSpeedService.class);
                    service.putExtra(GpsSpeedService.EXTRA_AUTOBOOT, true);
                    startService(service);
                }

                PrefUtils.setEnableTempAudioService(getApplicationContext(), true);
                if(!PrefUtils.isEnableAccessibilityService(getApplicationContext())){
                    PrefUtils.setShowFlattingOn(getApplicationContext(),PrefUtils.SHOW_ALL);
                }
                if(PrefUtils.isEnableTimeFloationgWidow(getApplicationContext())){
                    if(!Utils.isServiceRunning(getApplicationContext(),RealTimeFloatingService.class.getName())){
                        Intent timeFloating=new Intent(getApplicationContext(),RealTimeFloatingService.class);
                        startService(timeFloating);
                    }
                }
                if (PrefUtils.getShowFlatingOn(getApplicationContext()).equalsIgnoreCase(PrefUtils.SHOW_ALL)) {
                    Utils.startFloationgWindows(getApplicationContext(),true);
                }
                if(!PrefUtils.isWidgetActived(getApplicationContext()) && !PrefUtils.isEnableFlatingWindow(getApplicationContext())){
                    GpsUtil.getInstance(getApplicationContext()).startLocationService();
                }
                Set<String> desktopPackages=Utils.getDesktopPackageName(getApplicationContext());
                PrefUtils.setApps(getApplicationContext(),desktopPackages);
                PackageManager packageManager = getApplicationContext().getPackageManager();
                Intent intentLauncher = new Intent(Intent.ACTION_MAIN);
                intentLauncher.addCategory(Intent.CATEGORY_HOME);
                String selectDefaultLauncher=packageManager.resolveActivity(intentLauncher,PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
                String defaultLaunch = PrefUtils.getDefaultLanuchApp(getApplicationContext());
                Log.d("huivip","Default launch:"+defaultLaunch+",Select launcher:"+selectDefaultLauncher);
                if (!TextUtils.isEmpty(defaultLaunch) && "com.huivip.gpsspeedwidget".equalsIgnoreCase(selectDefaultLauncher)) {
                    if (!Utils.isServiceRunning(getApplicationContext(), defaultLaunch)) {
                        Log.d("huivip","Default launch No Running,then start default launch:"+defaultLaunch);
                        Intent launchIntent = getApplicationContext().getPackageManager().getLaunchIntentForPackage(defaultLaunch);
                        if (launchIntent != null) {
                            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getApplicationContext().startActivity(launchIntent);//null pointer check in case package name was not found
                        }
                    }
                }

            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
