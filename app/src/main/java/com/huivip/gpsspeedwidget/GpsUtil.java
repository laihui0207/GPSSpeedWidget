package com.huivip.gpsspeedwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.enums.*;
import com.amap.api.navi.model.*;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.huivip.gpsspeedwidget.listener.CatchRoadReceiver;
import com.huivip.gpsspeedwidget.speech.SpeechFactory;
import com.huivip.gpsspeedwidget.speech.TTS;
import com.huivip.gpsspeedwidget.utils.CycleQueue;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * @author sunlaihui
 */
public class GpsUtil implements AMapNaviListener {
    Context context;
    private String latitude;
    private String longitude;
    private float bearing;
    private double altitude=0.0D;
    private boolean isTurned = false;
    private String velocitaString = null;
    private Integer velocitaNumber;
    Double speed = 0D;
    Integer mphSpeed = Integer.valueOf(0);
    Integer kmhSpeed = Integer.valueOf(0);
    Integer limitSpeed = Integer.valueOf(0);
    Float limitDistance = 0F;
    String mphSpeedStr = "0";
    String kmhSpeedStr = "0";
    String velocita_prec = "ciao";
    Integer speedometerPercentage = Integer.valueOf(0);
    Integer c = Integer.valueOf(0);
    Integer speedAdjust = Integer.valueOf(0);
    String providerId = LocationManager.GPS_PROVIDER;
    boolean gpsEnabled = false;
    boolean gpsLocationStarted = false;
    boolean gpsLocationChanged = false;
    boolean serviceStarted = false;
    TimerTask locationScanTask;
    Timer locationTimer;
    AMapNavi aMapNavi;
    LocationManager locationManager;
    TTS tts;
    WeatherService weatherService;
    boolean limitSpeaked = false;
    Integer limitCounter = Integer.valueOf(0);
    boolean hasLimited = false;
    boolean aimlessStatred = false;
    boolean autoNavi_on_Frontend=false;
    boolean autoMapBackendProcessStarted=false;
    boolean catchRoadServiceStarted=false;
    final Handler locationHandler = new Handler();
    BroadcastReceiver broadcastReceiver;
    int limitDistancePercentage = 0;
    float distance = 0F;
    Location preLocation;
    int cameraType = 0;
    int cameraDistance = 0;
    int cameraSpeed = 0;
    String currentRoadName = "";
    int currentRoadType=-1;
    String nextRoadName = "";
    float nextRoadDistance = 0F;
    float totalLeftDistance = 0F;
    float totalLeftTime = 0F;
    int navi_turn_icon = -1;
    String cityName="";
    int naviFloatingStatus = 0; // 0 disabled 1 visible
    int autoNaviStatus = 0; // 0 no started  1 started
    CycleQueue<Location> locationVOCycleQueue=new CycleQueue<>(5);
    Location lastedRecoredLocation;
    Location catchRoadLocation;
    int recordLocationDistance=2;
    int catchRoadDistance=10;
    AlarmManager alarm ;
    int locationUpdateCount=0;
    NumberFormat localNumberFormat = NumberFormat.getNumberInstance();
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location paramAnonymousLocation) {
            GpsUtil.this.updateLocationData(paramAnonymousLocation);
        }

        @Override
        public void onStatusChanged(String s, int status, Bundle bundle) {
            if (status == LocationProvider.TEMPORARILY_UNAVAILABLE || status == LocationProvider.OUT_OF_SERVICE) {
                GpsUtil.this.updateLocationData(null);
            }
        }

        @Override
        public void onProviderDisabled(String paramAnonymousString) {
            GpsUtil.this.updateLocationData(null);
        }

        @Override
        public void onProviderEnabled(String paramAnonymousString) {
        }

    };

    private static GpsUtil instance;

    private GpsUtil(Context context) {
        this.context = context;
        Random random = new Random();
        c = random.nextInt();
        localNumberFormat.setMaximumFractionDigits(1);
        alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        tts = SpeechFactory.getInstance(context).getTTSEngine(PrefUtils.getTtsEngine(context));
    }

    public static GpsUtil getInstance(Context context) {
        if (instance == null) {
            synchronized (GpsUtil.class) {
                if (instance == null) {
                    instance = new GpsUtil(context);
                }
            }
        }
        return instance;
    }
    public void destory(){
        if(tts!=null){
            tts.release();
        }
        stopAimlessNavi();
        stopLocationService(true);
    }
    public void startLocationService() {
        if (serviceStarted) return;
        this.locationTimer = new Timer();
        speedAdjust = PrefUtils.getSpeedAdjust(context);
        Intent trackService=new Intent(context,NaviTrackService.class);
        this.locationScanTask = new TimerTask() {
            @Override
            public void run() {
                GpsUtil.this.locationHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        checkLocationData();
                        //Log.d("huivip","GPS UTIL Check Location");
                    }
                });
            }
        };
        Toast.makeText(context, "GPS服务开启", Toast.LENGTH_SHORT).show();
        this.locationTimer.schedule(this.locationScanTask, 0L, 100L);
        if (Utils.isNetworkConnected(context)) {
            startAimlessNavi();
            context.startService(trackService);
        } else {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = connectMgr.getActiveNetworkInfo();
                    if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                        if (!aimlessStatred && serviceStarted) {
                            startAimlessNavi();
                            if(PrefUtils.isEnableNAVIUploadGPSHistory(context)) {
                                context.startService(trackService);
                            }
                        }
                        context.getApplicationContext().unregisterReceiver(broadcastReceiver);
                    }
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            context.getApplicationContext().registerReceiver(broadcastReceiver, intentFilter);
        }
        Intent recordService = new Intent(context, RecordGpsHistoryService.class);
        context.startService(recordService);
        serviceStarted = true;
        weatherService=WeatherService.getInstance(context);
    }

    private void startAimlessNavi() {
        if (!PrefUtils.isWidgetActived(context) && !PrefUtils.isEnableFlatingWindow(context)) {
            return;
        }
        if (PrefUtils.isEnableAutoNaviService(context) && !aimlessStatred) {
            aMapNavi = AMapNavi.getInstance(context);
            aMapNavi.setIgnoreWifiCheck(true);
            aMapNavi.addAMapNaviListener(this);

            if(PrefUtils.getTtsEngine(context).equalsIgnoreCase(SpeechFactory.SDKTTS)){
                aMapNavi.setUseInnerVoice(true);
                aMapNavi.getNaviSetting().setUseOfflineVoice(true);
                aMapNavi.getNaviSetting().setIgnoreWifi(true);
                aMapNavi.setSoundQuality(SoundQuality.High_Quality);
            } else {
                aMapNavi.setUseInnerVoice(false);
            }

            if (PrefUtils.isOldDriverMode(context)) {
                aMapNavi.setBroadcastMode(BroadcastMode.CONCISE);
                aMapNavi.startAimlessMode(AimLessMode.CAMERA_DETECTED);
            } else {
                aMapNavi.setBroadcastMode(BroadcastMode.DETAIL);
                aMapNavi.startAimlessMode(AimLessMode.CAMERA_AND_SPECIALROAD_DETECTED);
            }
        }
    }

    private void stopAimlessNavi() {
        if (aMapNavi != null) {
            aMapNavi.stopAimlessMode();
            aMapNavi.removeAMapNaviListener(this);
            aMapNavi.destroy();
            aMapNavi = null;
            aimlessStatred = false;
        }
    }

    public void stopLocationService(boolean stop) {
        if (serviceStarted && ((!stop && !PrefUtils.isWidgetActived(context)) || (PrefUtils.isWidgetActived(context) && stop))) {
            if (this.locationTimer != null) {
                this.locationTimer.cancel();
                this.locationTimer.purge();
            }
            stopAimlessNavi();
            Intent recordService = new Intent(context, RecordGpsHistoryService.class);
            recordService.putExtra(RecordGpsHistoryService.EXTRA_CLOSE, true);
            context.startService(recordService);
            //if (tts != null) {
            //SpeechFactory.getInstance(context).getTTSEngine(PrefUtils.getTtsEngine(context)).release();
           // }
            serviceStarted = false;
            weatherService.stopLocation();
        }

    }

    public String getDirection() {
        String direction = "北";
        if (bearing >= 22.5 && bearing < 67.5) {
            direction = "东北";
        } else if (bearing >= 67.5 && bearing <= 112.5) {
            direction = "东";
        } else if (bearing > 112.5 && bearing <= 157.5) {
            direction = "东南";
        } else if (bearing >= 157.5 && bearing <= 202.5) {
            direction = "南";
        } else if (bearing >= 202.5 && bearing <= 247.5) {
            direction = "西南";
        } else if (bearing >= 247.5 && bearing <= 292.5) {
            direction = "西";
        } else if (bearing >= 292.5 && bearing <= 337.5) {
            direction = "西北";
        }
        return direction;
    }

    public boolean isAutoMapBackendProcessStarted() {
        return autoMapBackendProcessStarted;
    }

    public void setAutoMapBackendProcessStarted(boolean autoMapBackendProcessStarted) {
        this.autoMapBackendProcessStarted = autoMapBackendProcessStarted;
    }
    public void setAutoNavi_on_Frontend(boolean on_frontend){
        this.autoNavi_on_Frontend=on_frontend;
    }
    public boolean isAutoNavi_on_Frontend(){
        return this.autoNavi_on_Frontend;
    }
    public boolean isCatchRoadServiceStarted() {
        return catchRoadServiceStarted;
    }

    public void setCatchRoadServiceStarted(boolean catchRoadServiceStarted) {
        this.catchRoadServiceStarted = catchRoadServiceStarted;
    }

    public float getBearing() {
        return bearing;
    }

    public Double getSpeed() {
        return speed;
    }

    public String getDistance() {
        localNumberFormat.setMaximumFractionDigits(1);
        return localNumberFormat.format(distance / 1000) + "km";
    }

    public Integer getSpeedometerPercentage() {
        return speedometerPercentage;
    }

    private void updateLocationData(Location paramLocation) {
        if (paramLocation != null) {
            this.latitude = Double.toString(paramLocation.getLatitude());
            this.longitude = Double.toString(paramLocation.getLongitude());
            this.altitude=paramLocation.getAltitude();
            this.velocitaString = null;
            if (paramLocation.hasSpeed()) {
                this.velocitaNumber = Integer.valueOf((int) paramLocation.getSpeed());
                localNumberFormat.setMaximumFractionDigits(1);
                this.speed = Double.valueOf(paramLocation.getSpeed());
                this.velocitaString = localNumberFormat.format(this.speed);
                this.bearing = paramLocation.getBearing();
            }
            if (preLocation != null) {
                distance += preLocation.distanceTo(paramLocation);
            }
            // save location every 50 m for catch road service
            if(lastedRecoredLocation==null || paramLocation.distanceTo(lastedRecoredLocation)>recordLocationDistance){
                lastedRecoredLocation=paramLocation;
                //lastedRecoredLocation.setTime(System.currentTimeMillis());
                lastedRecoredLocation.setTime(System.currentTimeMillis()/1000);
                locationVOCycleQueue.push(paramLocation);
            }
            preLocation = paramLocation;
            // reset camera data every 4 second
            if(cameraDistance>0 && paramLocation.getSpeed()>15 && locationUpdateCount>40){
                setCameraDistance(0);
                setCameraSpeed(0);
                locationUpdateCount=0;
            }
            else  if(cameraDistance>0 && paramLocation.getSpeed()>15 ) {
                locationUpdateCount++;
            }

            // get catch road data every 500 m
            if (catchRoadLocation == null) {
                catchRoadLocation = paramLocation;
            } else if (paramLocation.distanceTo(catchRoadLocation) > catchRoadDistance) {
                Log.d("huivip", "Launch CatchRoad receiver");
                PendingIntent catchRoadIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, CatchRoadReceiver.class), 0);
                alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 300L, catchRoadIntent);
                catchRoadLocation=paramLocation;
                recordLocationDistance=10;
                catchRoadDistance=100;
            }
            if(PrefUtils.isEnableAutoGoHomeAfterNaviStarted(context) && kmhSpeed>0 && autoNavi_on_Frontend && naviFloatingStatus == Constant.Navi_Status_Started ){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("huivip","Auto Navi started will go home");
                        Utils.goHome(context);
                    }
                }, 20*1000);
            }

        } else {

            this.velocitaString = null;
            this.velocita_prec = "ciao";
            this.latitude = null;
        }
    }

    void checkLocationData() {
        try {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.getProvider(this.providerId) == null) {
                return;
            }
            gpsEnabled = locationManager.isProviderEnabled(this.providerId);
            Location localLocation = locationManager.getLastKnownLocation("gps");
            if (localLocation != null) {
                updateLocationData(localLocation);
            }
            locationManager.requestLocationUpdates(this.providerId, 1L, 1.0F, this.locationListener);
        } catch (SecurityException e) {
           // Toast.makeText(context, "GPS widget 需要GPS权限!", Toast.LENGTH_SHORT).show();
        }

        if ((this.velocitaString != null) && (gpsEnabled)) {
            if (!this.velocitaString.equals(this.velocita_prec)) {
                computeAndShowData();
                gpsLocationChanged = true;
                this.velocita_prec = this.velocitaString;
            } else {
                gpsLocationChanged = false;
            }
        }
    }

    void computeAndShowData() {

        mphSpeed = (int) (this.velocitaNumber.intValue() * 3.6D / 1.609344D);
        kmhSpeed = (int) (this.speed.doubleValue() * 3.6D);
        //kmhSpeed = (int) (this.speed.doubleValue() * 3.6D/1.609344D);
        if (speedAdjust != 0) {
            if (kmhSpeed > 0 && kmhSpeed > Math.abs(speedAdjust)) {
                kmhSpeed += speedAdjust;
            }
        }
        speedometerPercentage = Math.round((float) kmhSpeed / 240 * 100);
        // limit speak just say one times in one minutes
        if ((limitSpeed > 0 && kmhSpeed > limitSpeed && PrefUtils.isRoadLimitNotify(context)) ||
                (cameraSpeed>0 && kmhSpeed > cameraSpeed)) {
            hasLimited = true;
            if(limitCounter%10==0){
                if(PrefUtils.isEnableAudioService(context)) {
                    Utils.playBeeps();
                }
            }
            if (!limitSpeaked || limitCounter > 300) {
                limitSpeaked = true;
                limitCounter = 0;
                tts.speak("您已超速");
            }
            limitCounter++;
        } else {
            hasLimited = false;
            limitSpeaked = false;
            limitCounter = 0;
        }
    }

    public CycleQueue<Location> getLocationVOCycleQueue() {
        return locationVOCycleQueue;
    }

    public boolean isHasLimited() {
        return hasLimited;
    }

    public int getLimitDistancePercentage() {
        if (limitDistance > 0) {
            limitDistancePercentage = Math.round((300F - limitDistance) / 300 * 100);
        } else {
            limitDistancePercentage = 0;
        }
        return limitDistancePercentage;
    }

    public Integer getMphSpeed() {
        return mphSpeed;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getMphSpeedStr() {
        localNumberFormat.setMaximumFractionDigits(1);
        mphSpeedStr = localNumberFormat.format(mphSpeed);
        return mphSpeedStr;
    }

    public String getKmhSpeedStr() {
        localNumberFormat.setMaximumFractionDigits(1);
        kmhSpeedStr = localNumberFormat.format(kmhSpeed);
        return kmhSpeedStr;
    }

    public Integer getKmhSpeed() {
        return kmhSpeed;
    }

    public Integer getLimitSpeed() {
       if (cameraSpeed > 0) {
            return cameraSpeed;
        }
        return limitSpeed;
    }
    public void setLimitSpeed(Integer limit){
        limitSpeed=limit;
    }
    public void setTts(TTS tts) {
        this.tts = tts;
    }

    public Float getLimitDistance() {
        return limitDistance;
    }

    public boolean isGpsEnabled() {
        return gpsEnabled;
    }

    public boolean isGpsLocationStarted() {
        gpsLocationStarted = this.velocitaString != null;
        return gpsLocationStarted;
    }

    public int getCameraType() {
        return cameraType;
    }

    /*
       0：未知道路设施
   4：测速摄像头、测速雷达
   5：违章摄像头
   10:请谨慎驾驶
   11:有连续拍照
   12：铁路道口
   13：注意落石（左侧）
   14：事故易发地段
   15：易滑
   16：村庄
   18：前方学校
   19：有人看管的铁路道口
   20：无人看管的铁路道口
   21：两侧变窄
   22：向左急弯路
   23：向右急弯路
   24：反向弯路
   25：连续弯路
   26：左侧合流标识牌
   27：右侧合流标识牌
   28：监控摄像头
   29：专用道摄像头
   31：禁止超车
   36：右侧变窄
   37：左侧变窄
   38：窄桥
   39：左右绕行
   40：左侧绕行
   41：右侧绕行
   42：注意落石（右侧）
   43：傍山险路（左侧）
   44：傍山险路（右侧）
   47：上陡坡
   48：下陡坡
   49：过水路面
   50：路面不平
   52：慢行
   53：注意危险
   58：隧道
   59：渡口
   92:闯红灯
   93:应急车道
   94:非机动车道
   100：不绑定电子眼高发地
   101:车道违章
   102:超速违章
        */
    public String getCameraTypeName() {
        String name = "限速";
        if (getAutoNaviStatus() == Constant.Navi_Status_Started) {
            switch (cameraType) {
                case 0:
                    name = "限速";
                    break;
                case 1:
                    name = "监控拍照";
                    break;
                case 2:
                    name = "红灯拍照";
                    break;
                case 3:
                    name = "违章监控";
                    break;
                case 4:
                    name = "公交专用";
                    break;
                case 5:
                    name = "应急车道";
                    break;
                case 8:
                    name = "区间开始";
                    break;
                case 9:
                    name = "区间结束";
                    break;
                case 9999:
                    name= "道路限速";
                    break;
                default:
                    name = "限速";
            }
        } else {
            switch (cameraType) {
                case 4:
                case 102:
                    name = "限速";
                    break;
                case 11:
                case 28:
                case 29:
                    name = "监控拍照";
                    break;
                case 92:
                    name = "红灯拍照";
                    break;
                case 5:
                case 101:
                case 100:
                    name = "违章监控";
                    break;
                case 93:
                    name = "应急车道";
                    break;
                case 10:
                    name = "谨慎驾驶";
                    break;
                case 9999:
                    name= "道路限速";
                    break;
                default:
                    name = "限速";

            }
        }
        if(limitSpeed>0 && cameraType ==-1 && isCatchRoadServiceStarted()){
            name="道路限速";
        }
        return name;
    }
    public void setRoadType(int roadType){
        this.currentRoadType=roadType;
    }
    /*
    //0：高速公路
//1：国道
//2：省道
//3：县道
//4：乡公路
//5：县乡村内部道路
//6：主要大街、城市快速道
//7：主要道路
//8：次要道路
//9：普通道路
//10：非导航道路
     */
    public String getRoadTypeName(){
        String roadTypeName="";
        switch(currentRoadType) {
            case 0:
                roadTypeName="高速公路";
                break;
            case 1:
                roadTypeName="国道";
                break;
            case 2:
                roadTypeName="省道";
                break;
            case 3:
                roadTypeName="县道";
                break;
            case 4:
                roadTypeName="乡公路";
                break;
            case 5:
                roadTypeName="县乡村内部道路";
                break;
            case 6:
                roadTypeName="主要大街、城市快速道";
                break;
            case 7:
                roadTypeName="主要道路";
                break;
            case 8:
                roadTypeName="次要道路";
                break;
            case 9:
                roadTypeName="普通道路";
                break;
            case 10:
                roadTypeName="非导航道路";
                break;
        }
        return roadTypeName;
    }
    public void setCameraType(int cameraType) {
        this.cameraType = cameraType;
    }

    public int getCameraDistance() {
        return cameraDistance;
    }

    public void setCameraDistance(int cameraDistance) {
        this.cameraDistance = cameraDistance;
        this.limitDistance = cameraDistance * 1.0F;
    }

    public int getCameraSpeed() {
        return cameraSpeed;
    }
    public String getAltitude(){
        localNumberFormat.setMaximumFractionDigits(1);
        return localNumberFormat.format(altitude);
    }
    public void setCameraSpeed(int cameraSpeed) {
        this.cameraSpeed = cameraSpeed;
    }

    public String getCurrentRoadName() {
        if (!TextUtils.isEmpty(currentRoadName) && currentRoadName.length() > 4) {
            return currentRoadName.substring(0, 4);
        }
        return currentRoadName;
    }

    public void setCurrentRoadName(String currentRoadName) {
        this.currentRoadName = currentRoadName;
    }

    public String getNextRoadName() {
        return nextRoadName;
    }

    public void setNextRoadName(String nextRoadName) {
        this.nextRoadName = nextRoadName;
    }

    public String getNextRoadDistance() {
        localNumberFormat.setMaximumFractionDigits(1);
        if (nextRoadDistance > 1000) {
            return localNumberFormat.format(nextRoadDistance / 1000) + "公里";
        }
        return localNumberFormat.format(nextRoadDistance) + "米";
    }

    public void setNextRoadDistance(float nextRoadDistance) {
        this.nextRoadDistance = nextRoadDistance;
    }

    public String getTotalLeftDistance() {
        localNumberFormat.setMaximumFractionDigits(1);
        if (totalLeftDistance > 1000) {
            return localNumberFormat.format(totalLeftDistance / 1000) + "公里";
        }
        return localNumberFormat.format(totalLeftDistance) + "米";
    }

    public void setTotalLeftDistance(float totalLeftDistance) {
        this.totalLeftDistance = totalLeftDistance;
    }

    public String getTotalLeftTime() {
        localNumberFormat.setMaximumFractionDigits(0);
        if (totalLeftTime > 3600) {
            int hours = (int) totalLeftTime / 3600;
            int minutes = (int) ((totalLeftTime - hours * 3600) / 60);
            return hours + "小时" + minutes + "分钟";
        }
        return localNumberFormat.format(totalLeftTime / 60) + "分钟";
    }

    public void setTotalLeftTime(float totalLeftTime) {
        this.totalLeftTime = totalLeftTime;
    }

    public int getNavi_turn_icon() {
        return navi_turn_icon;
    }

    public void setNavi_turn_icon(int navi_turn_icon) {
        this.navi_turn_icon = navi_turn_icon;
    }

    public boolean isGpsLocationChanged() {
        return gpsLocationChanged;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public int getNaviFloatingStatus() {
        return naviFloatingStatus;
    }

    public void setNaviFloatingStatus(int naviFloatingStatus) {
        this.naviFloatingStatus = naviFloatingStatus;
    }

    public int getAutoNaviStatus() {
        return autoNaviStatus;
    }

    public void setAutoNaviStatus(int autoNaviStatus) {
        this.autoNaviStatus = autoNaviStatus;
    }

    String speakText = "";


    @Override
    public void onInitNaviFailure() {
        aimlessStatred = false;
    }

    @Override
    public void onInitNaviSuccess() {
        // BDTTS.speak("智能巡航服务开启");
        aimlessStatred = true;
        Toast.makeText(context, "智能巡航服务开启", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStartNavi(int naviType) {
        if (naviType == NaviType.CRUISE) {
            Log.d("huivip", "巡航模式开启");
        }
    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {
       /* if(cameraDistance>0 && aMapNaviLocation.getSpeed()>15 && locationUpdateCount>40){
            setCameraDistance(0);
            setCameraSpeed(0);
            locationUpdateCount=0;
        }
        else  if(cameraDistance>0 && aMapNaviLocation.getSpeed()>15 ) {
            locationUpdateCount++;
        } */
    }

    @Override
    public void onGetNavigationText(int i, String s) {

    }

    @Override
    public void onGetNavigationText(String s) {
        if(!PrefUtils.getTtsEngine(context).equalsIgnoreCase(SpeechFactory.SDKTTS)) {
            tts.speak(s);
        }
    }

    @Override
    public void onEndEmulatorNavi() {

    }

    @Override
    public void onArriveDestination() {

    }

    @Override
    public void onCalculateRouteFailure(int i) {

    }

    @Override
    public void onReCalculateRouteForYaw() {

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    @Override
    public void onArrivedWayPoint(int i) {

    }

    @Override
    public void onGpsOpenStatus(boolean b) {
        gpsEnabled = b;
    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {
        String roadName=naviInfo.getCurrentRoadName();
        if(!TextUtils.isEmpty(roadName)){
            setCurrentRoadName(roadName);
        }
    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {
    }

    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {
        for (AMapNaviCameraInfo aMapNaviCameraInfo : aMapNaviCameraInfos) {
            cameraType = aMapNaviCameraInfo.getCameraType();
            if (aMapNaviCameraInfo.getCameraSpeed() > 0 && aMapNaviCameraInfo.getCameraDistance() > 0) {
                setCameraDistance(aMapNaviCameraInfo.getCameraDistance());
                setCameraSpeed(aMapNaviCameraInfo.getCameraSpeed());
            }
        }
    }

    @Override
    public void updateIntervalCameraInfo(AMapNaviCameraInfo aMapNaviCameraInfo, AMapNaviCameraInfo
            aMapNaviCameraInfo1, int status) {
        if (status == CarEnterCameraStatus.ENTER) {
            setCameraType(aMapNaviCameraInfo.getCameraType());
            setCameraSpeed(aMapNaviCameraInfo.getCameraSpeed());
            setCameraDistance(aMapNaviCameraInfo.getCameraDistance());
        } else if (status == CarEnterCameraStatus.LEAVE) {
            setCameraType(aMapNaviCameraInfo1.getCameraType());
            setCameraSpeed(aMapNaviCameraInfo1.getCameraSpeed());
            setCameraDistance(aMapNaviCameraInfo1.getCameraDistance());
        }
    }

    @Override
    public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {

    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {

    }

    @Override
    public void hideCross() {

    }

    @Override
    public void showModeCross(AMapModelCross aMapModelCross) {

    }

    @Override
    public void hideModeCross() {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo aMapLaneInfo) {
        Toast.makeText(context,aMapLaneInfo.getLaneTypeIdArray().toString(),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void hideLaneInfo() {

    }

    @Override
    public void onCalculateRouteSuccess(int[] ints) {

    }

    @Override
    public void notifyParallelRoad(int i) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {
    }

    Integer[] broadcastTypes = {4, 5, 11, 28, 29, 93, 92, 101, 102};

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {
        if(getAutoNaviStatus()!=Constant.Navi_Status_Started) {
            for (AMapNaviTrafficFacilityInfo info : aMapNaviTrafficFacilityInfos) {
                cameraType = info.getBroadcastType();
                if (Arrays.asList(broadcastTypes).contains(info.getBroadcastType())) {
                    setCameraDistance(info.getDistance());
                    if (info.getLimitSpeed() > 0) {
                        setCameraSpeed(info.getLimitSpeed());
                    }
                }
            }
        }
    }

    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {

    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {
        Log.d("huivip", "Time:" + aimLessModeStat.getAimlessModeTime() + ",Distance:" + aimLessModeStat.getAimlessModeDistance());

    }

    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {
        /*if (!TextUtils.isEmpty(aimLessModeCongestionInfo.getRoadName())) {
            Toast.makeText(context, aimLessModeCongestionInfo.getRoadName(), Toast.LENGTH_SHORT).show();
        }*/
    }

    @Override
    public void onPlayRing(int status) {
        if (status == AMapNaviRingType.RING_EDOG || status == AMapNaviRingType.RING_CAMERA) {
            setCameraSpeed(0);
            limitDistance = 0F;
            cameraType=-1;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tts.speak("已通过");
                }
            }, 2000L);

        }
    }

    @Override
    public void onCalculateRouteSuccess(AMapCalcRouteResult aMapCalcRouteResult) {

    }

    @Override
    public void onCalculateRouteFailure(AMapCalcRouteResult aMapCalcRouteResult) {

    }

    @Override
    public void onNaviRouteNotify(AMapNaviRouteNotifyData aMapNaviRouteNotifyData) {

    }
}
