package com.huivip.gpsspeedwidget.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.Toast;

import com.huivip.gpsspeedwidget.beans.GetDistrictEvent;
import com.huivip.gpsspeedwidget.beans.LocationEvent;
import com.huivip.gpsspeedwidget.beans.PlayAudioEvent;
import com.huivip.gpsspeedwidget.listener.AutoLaunchSystemConfigReceiver;
import com.huivip.gpsspeedwidget.listener.GoToHomeReceiver;
import com.huivip.gpsspeedwidget.listener.WeatherServiceReceiver;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.xutils.x;

public class LaunchHandlerService extends Service {

    AlarmManager alarm;
    boolean started=false;
    boolean autoMapLaunched=false;

    public LaunchHandlerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean start = AppSettings.get().getAutoStart();
        alarm = (AlarmManager) getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);

        if (start && !started) {
            String apps = PrefUtils.getAutoLaunchApps(getApplicationContext());
            if (AppSettings.get().isEnableLaunchOtherApp() && !TextUtils.isEmpty(apps)) {
                String[] autoApps = apps.split(",");
                if (autoApps.length > 0) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int delayTime = AppSettings.get().getDelayTimeBetweenLaunchOtherApp();
                            for (String packageName : autoApps) {
                                try {
                                    Thread.sleep(delayTime * 1000 + 500L);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                Intent launchIntent = getApplicationContext().getPackageManager().getLaunchIntentForPackage(packageName);
                                if (launchIntent != null && !Utils.isServiceRunning(getApplicationContext(), packageName)) {
                                    launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    getApplicationContext().startActivity(launchIntent);//null pointer check in case package name was not found

                                }
                            }
                            if (AppSettings.get().isReturnHomeAfterLaunchOtherApp()) {
                                AlarmManager alarm = (AlarmManager) getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);
                                PendingIntent gotoHomeIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
                                        new Intent(getApplicationContext(), GoToHomeReceiver.class), 0);
                                alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000L, gotoHomeIntent);
                            }

                        }
                    }).start();
                } else if (AppSettings.get().isReturnHomeAfterLaunchOtherApp()) {
                    AlarmManager alarm = (AlarmManager) getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);
                    PendingIntent gotoHomeIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
                            new Intent(getApplicationContext(), GoToHomeReceiver.class), 0);
                    alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000L, gotoHomeIntent);
                }
            }
            if (AppSettings.get().isPlayWeather()) {
                PendingIntent weatherServiceIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
                        new Intent(getApplicationContext(), WeatherServiceReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
                alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000L, weatherServiceIntent);
            }
            PendingIntent autoLaunchIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
                    new Intent(getApplicationContext(), AutoLaunchSystemConfigReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
            alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000L, autoLaunchIntent);
            x.task().postDelayed(this::getDistrictFromAuto, 30000);
            x.task().postDelayed(this::getDistrictFromAuto, 60000);
            x.task().postDelayed(()->{
                if(!autoMapLaunched){
                    launchAutoMap();
                }
            },120000);
            if (AppSettings.get().isEnablePlayWarnAudio() && !started) {
                x.task().postDelayed(() -> {
                    EventBus.getDefault().post(new PlayAudioEvent(PrefUtils.getPrefLaunchAlterTts(getApplicationContext()), true));
                }, 5000);
            }
            started=true;
        }
        return super.onStartCommand(intent, flags, startId);
    }
    @Subscribe
    public void getDistrict(GetDistrictEvent event){
        getDistrictFromAuto();
    }
    @Subscribe
    public void locationStatus(LocationEvent event){
        if(event.getDistrict()!=null && event.getEventFrom().equals("AutoMap")){
            autoMapLaunched=true;
        }
    }
    private void getDistrictFromAuto() {
        x.task().postDelayed(() -> {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.setPackage("com.autonavi.amapauto");
            intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
            intent.putExtra("KEY_TYPE", 10029);
            sendBroadcast(intent);
        }, 1000 * 10);
    }
    private void launchAutoMap(){
        String pkgName_auto = "com.autonavi.amapauto";
        String pkgName_lite="com.autonavi.amapautolite";
        String pkgName=pkgName_auto;
        boolean autoInstalled=false;
        if(Utils.checkApplicationIfExists(getApplicationContext(),pkgName)) {
            autoInstalled=true;
        } else if(Utils.checkApplicationIfExists(getApplicationContext(),pkgName_lite)) {
            autoInstalled=true;
            pkgName=pkgName_lite;
        } else {
            Toast.makeText(getApplicationContext(),"没有找到高德地图",Toast.LENGTH_SHORT).show();
        }
        if(autoInstalled) {
            Intent launchIntent = new Intent();
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchIntent.setComponent(
                    new ComponentName(pkgName,
                            "com.autonavi.auto.remote.fill.UsbFillActivity"));
            startActivity(launchIntent);
            String finalPkgName = pkgName;
            x.task().postDelayed(() -> {
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                intent.setPackage(finalPkgName);
                intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
                intent.putExtra("KEY_TYPE", 10031);
                sendBroadcast(intent);
            },10000);

        }

    }
}