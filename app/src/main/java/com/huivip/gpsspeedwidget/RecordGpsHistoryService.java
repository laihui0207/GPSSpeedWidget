package com.huivip.gpsspeedwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import com.huivip.gpsspeedwidget.utils.PrefUtils;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author sunlaihui
 */
public class RecordGpsHistoryService extends Service{
    public static final String EXTRA_CLOSE = "com.huivip.gpsrecordService.EXTRA_CLOSE";
    Long lineId=0L;
    GpsUtil gpsUtil;
    AlarmManager alarm;
    Timer recordGPSTimer=new Timer();
    TimerTask recordGPSTask;
    PendingIntent uploadMessageSender;
    final Handler recordGPSHandler=new Handler();
    @Override
    public void onCreate() {
        gpsUtil=GpsUtil.getInstance(getApplicationContext());
        this.lineId=System.currentTimeMillis();

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent !=null && intent.getBooleanExtra(EXTRA_CLOSE,false)){
            stopSelf();
            return super.onStartCommand(intent,flags,startId);
        }
        boolean recordGPS= PrefUtils.isEnableRecordGPSHistory(this);
        boolean uploadGPS=PrefUtils.isEnableUploadGPSHistory(this);

        if(recordGPS) {
            if(uploadGPS) {
                Intent broadcastIntent = new Intent(this, MessageReceiver.class);
                broadcastIntent.setAction(Constant.UPLOADACTION);
                uploadMessageSender = PendingIntent.getBroadcast(this, 0, broadcastIntent, 0);
                alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 60 * 1000, uploadMessageSender);
            }
            this.recordGPSTask = new TimerTask()
            {
                @Override
                public void run()
                {
                    RecordGpsHistoryService.this.recordGPSHandler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if(gpsUtil.isGpsLocationStarted() && gpsUtil.isGpsEnabled() && gpsUtil.getMphSpeed()>0  ) {
                                DBUtil dbUtil=new DBUtil(getApplicationContext());
                                dbUtil.insert(gpsUtil.getLongitude(),gpsUtil.getLatitude(),gpsUtil.getKmhSpeedStr()
                                        ,gpsUtil.getSpeed(),gpsUtil.getBearing(),new Date(),lineId);
                            }
                            //Log.d("huivip","Record gps data!");
                        }
                    });
                }
            };
            this.recordGPSTimer.schedule(this.recordGPSTask, 0L, 2000L);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if(alarm!=null){
            alarm.cancel(uploadMessageSender);
        }
        if(recordGPSTimer!=null){
            recordGPSTimer.cancel();
            recordGPSTimer.purge();
        }

        super.onDestroy();
    }
}
