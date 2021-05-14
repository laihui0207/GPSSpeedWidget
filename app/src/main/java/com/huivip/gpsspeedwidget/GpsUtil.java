package com.huivip.gpsspeedwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import com.huivip.gpsspeedwidget.beans.LaunchEvent;
import com.huivip.gpsspeedwidget.beans.LocationEnabledEvent;
import com.huivip.gpsspeedwidget.beans.PlayAudioEvent;
import com.huivip.gpsspeedwidget.listener.CatchRoadReceiver;
import com.huivip.gpsspeedwidget.service.AutoXunHangService;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.CycleQueue;
import com.huivip.gpsspeedwidget.utils.DateUtil;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;

import org.greenrobot.eventbus.EventBus;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author sunlaihui
 */
public class GpsUtil {
    Context context;
    private String latitude;
    private String longitude;
    private float bearing;
    private double altitude=0.0D;
    private String velocitaString = null;
    private Integer velocitaNumber;
    public boolean registTimeTickSuccess=false;
    Double speed = 0D;
    Integer mphSpeed = Integer.valueOf(0);
    Integer kmhSpeed = Integer.valueOf(0);
    Integer limitSpeed = Integer.valueOf(0);
    Integer limitDistance = 0;
    String mphSpeedStr = "0";
    String kmhSpeedStr = "0";
    String velocita_prec = "ciao";
    Integer speedometerPercentage = Integer.valueOf(0);
    Integer speedAdjust = Integer.valueOf(0);
    String providerId = LocationManager.GPS_PROVIDER;
    boolean gpsEnabled = false;
    boolean gpsLocationStarted = false;
    boolean gpsLocationChanged = false;
    public boolean serviceStarted = false;
    boolean firstLaunch=true;
    TimerTask locationScanTask;
    Timer locationTimer;
    LocationManager locationManager;
    boolean limitSpeaked = false;
    Integer limitCounter = Integer.valueOf(0);
    boolean hasLimited = false;
    boolean aimlessStatred = false;
    boolean autoNavi_on_Frontend=false;
    boolean autoMapBackendProcessStarted=false;
    boolean catchRoadServiceStarted=false;
    final Handler locationHandler = new Handler();
    int limitDistancePercentage = 0;
    float distance = 0F;
    long driveTime=0;
    long startTime=0;
    private int driveOutTimeCount=4;
    Location preLocation;
    int cameraType = 0;
    int cameraDistance = 0;
    int preCameraDistance=0;
    int cameraSpeed = 0;
    String currentRoadName = "";
    String preRoadName=null;
    String cityCode=null;
    int currentRoadType=-1;
    String nextRoadName = "";
    int nextRoadDistance = 0;
    int totalLeftDistance = 0;
    int totalLeftTime = 0 ;
    int navi_turn_icon = -1;
    String cityName="";
    int naviFloatingStatus = 0; // 0 disabled 1 visible
    int autoNaviStatus = 0; // 0 no started  1 started
    int autoXunHangStatus=0; // 0 no started 1 started
    CycleQueue<Location> locationVOCycleQueue=new CycleQueue<>(5);
    Location lastedRecoredLocation;
    Location catchRoadLocation;
    int recordLocationDistance=2;
    int catchRoadDistance=10;
    String homeSet;
    AlarmManager alarm ;
    boolean isNight=false;
    boolean isPlayAltitudeAlter=false;
    int altitudeAlterStart=2000;
    int altitudeAlterFrequency=100;
    int locationUpdateCount=0;
    NumberFormat localNumberFormat = NumberFormat.getNumberInstance();
    DecimalFormat decimalFormat=new DecimalFormat("0.0");
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
        localNumberFormat.setMaximumFractionDigits(1);
        localNumberFormat.setGroupingUsed(false);
        isPlayAltitudeAlter=AppSettings.get().isPlayAltitudeAlter();
        altitudeAlterStart=Integer.parseInt(PrefUtils.getAltitudeAlterStart(context));
        altitudeAlterFrequency=Integer.parseInt(PrefUtils.getAltitudeAlterFrequency(context));
        alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public static GpsUtil getInstance(Context context) {
        if (instance == null) {
            synchronized (GpsUtil.class) {
                if (instance == null) {
                    instance = new GpsUtil(AppObject.getContext());
                }
            }
        }
        return instance;
    }
    public void destory(){
        stopLocationService(true);
    }
    public void startLocationService() {
        if (serviceStarted) return;
        this.locationTimer = new Timer();
        speedAdjust = AppSettings.get().getSpeedAdjust();
        this.locationScanTask = new TimerTask() {
            @Override
            public void run() {
                locationHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        checkLocationData();
                    }
                });
            }
        };
        Toast.makeText(context, "GPS服务开启", Toast.LENGTH_SHORT).show();
        this.locationTimer.schedule(this.locationScanTask, 0L, 1000L);
       if(!Utils.isServiceRunning(context,AutoXunHangService.class.getName())) {
           /*Intent xunhangService = new Intent(context, AutoXunHangService.class);
           context.startService(xunhangService);*/
           EventBus.getDefault().post(new LaunchEvent(AutoXunHangService.class));
       }
       /* Intent recordService = new Intent(context, RecordGpsHistoryService.class);
        context.startService(recordService);*/
        serviceStarted = true;
    }
    public void stopLocationService(boolean stop) {
        if (serviceStarted && ((!stop && !PrefUtils.isWidgetActived(context)) || (PrefUtils.isWidgetActived(context) && stop))) {
            if (this.locationTimer != null) {
                this.locationTimer.cancel();
                this.locationTimer.purge();
            }
            LaunchEvent launchEvent = new LaunchEvent(AutoXunHangService.class);
            launchEvent.setToClose(true);
            EventBus.getDefault().post(launchEvent);

           /* Intent xunhangService=new Intent(context, AutoXunHangService.class);
            xunhangService.putExtra(AutoXunHangService.EXTRA_CLOSE,true);
            context.startService(xunhangService);*/

          /*  Intent recordService = new Intent(context, RecordGpsHistoryService.class);
            recordService.putExtra(RecordGpsHistoryService.EXTRA_CLOSE, true);
            context.startService(recordService);*/
            Toast.makeText(context, "GPS服务关闭", Toast.LENGTH_SHORT).show();
            serviceStarted = false;
        }

    }

    public String getDirection() {
        String direction = "正北";
        if (bearing >= 22.5 && bearing < 67.5) {
            direction = "东北";
        } else if (bearing >= 67.5 && bearing <= 112.5) {
            direction = "正东";
        } else if (bearing > 112.5 && bearing <= 157.5) {
            direction = "东南";
        } else if (bearing >= 157.5 && bearing <= 202.5) {
            direction = "正南";
        } else if (bearing >= 202.5 && bearing <= 247.5) {
            direction = "西南";
        } else if (bearing >= 247.5 && bearing <= 292.5) {
            direction = "正西";
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

    public boolean isNight() {
        return isNight;
    }
    public void setPlayAltitudeAlter(boolean enabled){
        this.isPlayAltitudeAlter=enabled;
    }

    public int getAltitudeAlterStart() {
        return altitudeAlterStart;
    }

    public void setAltitudeAlterStart(int altitudeAlterStart) {
        this.altitudeAlterStart = altitudeAlterStart;
    }

    public int getAltitudeAlterFrequency() {
        return altitudeAlterFrequency;
    }

    public void setAltitudeAlterFrequency(int altitudeAlterFrequency) {
        this.altitudeAlterFrequency = altitudeAlterFrequency;
    }

    public void setNight(boolean night) {
        isNight = night;
    }

    public float getBearing() {
        return bearing;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setHomeSet(String homeSet) {
        this.homeSet = homeSet;
    }

    public String getDistance() {
        localNumberFormat.setMaximumFractionDigits(1);
        return localNumberFormat.format(distance / 1000) + "km";
    }
    public String getDriveTimeString(){
        return DateUtil.formatDuring(driveTime);
    }
    public float getDriveTime(){
        return driveTime;
    }
    public Integer getSpeedometerPercentage() {
        return speedometerPercentage;
    }

    public boolean isServiceStarted() {
        return serviceStarted;
    }

    private void updateLocationData(Location paramLocation) {
        if (paramLocation != null) {
            this.latitude = Double.toString(paramLocation.getLatitude());
            this.longitude = Double.toString(paramLocation.getLongitude());
            this.altitude=paramLocation.getAltitude();
            this.velocitaString = null;
            if(startTime==0){
                startTime=paramLocation.getTime();
            }
            if (paramLocation.hasSpeed()) {
                this.velocitaNumber = Integer.valueOf((int) paramLocation.getSpeed());
                localNumberFormat.setMaximumFractionDigits(1);
                this.speed = Double.valueOf(paramLocation.getSpeed());
                this.velocitaString = localNumberFormat.format(this.speed);
                this.bearing = paramLocation.getBearing();
                if(firstLaunch){
                    EventBus.getDefault().post(new LocationEnabledEvent());
                    firstLaunch=false;
                }
            }
            if (preLocation != null) {
                distance += preLocation.distanceTo(paramLocation);
            }
            if(isPlayAltitudeAlter) {
                altitudeAlert();
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
            if(cameraDistance>0 && paramLocation.getSpeed()>10 && locationUpdateCount>20){
                setCameraDistance(0);
                setCameraSpeed(0);
                locationUpdateCount=0;
            }
            if(cameraDistance>0 && paramLocation.getSpeed()>10 ) {
                locationUpdateCount++;
            }

            // get catch road data every 500 m
            if (catchRoadLocation == null) {
                catchRoadLocation = paramLocation;
            } else if (paramLocation.distanceTo(catchRoadLocation) > catchRoadDistance) {
                if(getAutoNaviStatus()!=Constant.Navi_Status_Started) { // Auto Navi started then disable catch road service
                    PendingIntent catchRoadIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, CatchRoadReceiver.class), 0);
                    alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 300L, catchRoadIntent);
                    catchRoadLocation = paramLocation;
                    recordLocationDistance = 10;
                    catchRoadDistance = 1000;
                }
            }
            /*if(PrefUtils.isEnableAutoGoHomeAfterNaviStarted(context) && kmhSpeed>0 && autoNavi_on_Frontend && naviFloatingStatus == Constant.Navi_Status_Started ){
                x.task().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Utils.goHome(context);
                    }
                },20*1000);
            }*/

        } else {

            this.velocitaString = null;
            this.velocita_prec = "ciao";
            this.latitude = null;
        }
    }

    private void checkLocationData() {
        try {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager==null || locationManager.getProvider(this.providerId) == null) {
                return;
            }
            gpsEnabled = locationManager.isProviderEnabled(this.providerId);
            Location localLocation = locationManager.getLastKnownLocation("gps");
            if (localLocation != null) {
                updateLocationData(localLocation);
            }
            locationManager.requestLocationUpdates(this.providerId, 500L, 1.0F, this.locationListener);
        } catch (SecurityException e) {
           // Toast.makeText(context, "GPS widget 需要GPS权限!", Toast.LENGTH_SHORT).show();
        }
        if(startTime>0) {
            driveTime = System.currentTimeMillis() - startTime;
        }
        if(AppSettings.get().isEnablePlayoutTimeWarnAudio() && driveTime > 1000*3600*driveOutTimeCount){
            EventBus.getDefault().post(new PlayAudioEvent("请注意休息，不要疲劳驾驶",true));
            driveOutTimeCount++;
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

    private void computeAndShowData() {

        mphSpeed = (int) (this.velocitaNumber.intValue() * 3.6D / 1.609344D);
        kmhSpeed = (int) (this.speed.doubleValue() * 3.6D);
        if (AppSettings.get().isSpeedMPH()) {
            kmhSpeed = mphSpeed;
        }
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
                if(AppSettings.get().isEnableAudio()) {
                    Utils.playBeeps();
                }
            }
            if (!limitSpeaked || limitCounter > 300) {
                limitSpeaked = true;
                limitCounter = 0;
                EventBus.getDefault().post(new PlayAudioEvent(PrefUtils.getPrefOverSpeedTts(context),true));
                //tts.speak("您已超速");
            }
            limitCounter++;
        } else {
            hasLimited = false;
            limitSpeaked = false;
            limitCounter = 0;
        }
    }
    public void resetData(){
        if(driveTime>1000*3600*8) {
            this.startTime = System.currentTimeMillis();
            this.distance = 0;
            hasLimited = false;
        }
    }
    public void altitudeAlert(){
        if(altitude>=altitudeAlterStart) {
            playAltitude(altitudeAlterStart, altitudeAlterFrequency);
        }
    }
    private double pre_altitude=0;
    private void playAltitude(double alter_altitude,int frequency){
        double i_altitude = altitude - alter_altitude;
        if((int)i_altitude%frequency==0 && (int)altitude!=(int)pre_altitude){
            Toast.makeText(context,"当前海拔高度"+getAltitude(0)+"米",Toast.LENGTH_SHORT).show();
            EventBus.getDefault().post(new PlayAudioEvent("当前海拔高度"+getAltitude(0)+"米",true));
            pre_altitude=altitude;
        }
    }
    public CycleQueue<Location> getLocationVOCycleQueue() {
        return locationVOCycleQueue;
    }

    public boolean isHasLimited() {
        return hasLimited;
    }
    public int getLimitDistancePercentage() {
         if (limitDistance <300 && limitDistance>0) {
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

  /*  public String getMphSpeedStr() {
        localNumberFormat.setMaximumFractionDigits(1);
        mphSpeedStr = localNumberFormat.format(mphSpeed);
        return mphSpeedStr;
    }*/

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
    /*public void setTts(TTS tts) {
        this.tts = tts;
    }
    public TTS getTts(){
        return tts;
    }*/
    public Integer getLimitDistance() {
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
        if(cameraDistance<preCameraDistance){
            preCameraDistance=cameraDistance;
        }
        this.cameraDistance = cameraDistance;
        this.limitDistance = cameraDistance;
    }

    public int getCameraSpeed() {
        return cameraSpeed;
    }
    public String getAltitude(int point){
        localNumberFormat.setMaximumFractionDigits(point);
        return localNumberFormat.format(altitude);
    }
    public String getAltitude(){
       return getAltitude(1);
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
      /*  if(!TextUtils.isEmpty(currentRoadName) && !currentRoadName.equalsIgnoreCase(preRoadName) && !TextUtils.isEmpty(cityCode)){
            EventBus.getDefault().post(new SearchTrafficEvent(cityCode,currentRoadName, TrafficSearch.ROAD_LEVEL_NONAME_WAY));
        }*/
        if(preRoadName==null && !TextUtils.isEmpty(currentRoadName)){
            preRoadName=currentRoadName;
        }
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getNextRoadName() {
        return nextRoadName;
    }

    public void setNextRoadName(String nextRoadName) {
        this.nextRoadName = nextRoadName;
    }

    public String getNextRoadDistance() {
        if (nextRoadDistance > 1000) {
            return decimalFormat.format((float)nextRoadDistance/1000)+ "公里 后";
        }
        return nextRoadDistance + "米 后";
    }

    public void setNextRoadDistance(int nextRoadDistance) {
        this.nextRoadDistance = nextRoadDistance;
    }

    public String getTotalLeftDistance() {
        if (totalLeftDistance > 1000) {
            return decimalFormat.format(totalLeftDistance >> 10) + "公里";
        } else {
            return totalLeftDistance + "米";
        }
    }

    public void setTotalLeftDistance(int totalLeftDistance) {
        this.totalLeftDistance = totalLeftDistance;
    }

    public String getTotalLeftTime() {
        StringBuffer sb=new StringBuffer();
        if (totalLeftTime > 3600) {
            sb.append(totalLeftTime / 3600).append("小时");
             sb.append((totalLeftTime % 3600) / 60).append("分钟");
            return  sb.toString();
        } else {
            return sb.append(totalLeftTime / 60).append("分钟").toString();
        }
    }

    public void setTotalLeftTime(int totalLeftTime) {
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
    public void setAutoXunHangStatus(int autoXunHangStatus){
        this.autoXunHangStatus=autoXunHangStatus;
    }
}
