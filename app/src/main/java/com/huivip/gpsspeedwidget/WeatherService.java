package com.huivip.gpsspeedwidget;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.huivip.gpsspeedwidget.utils.HttpUtils;
import com.huivip.gpsspeedwidget.speech.SpeechFactory;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.ToastUtil;
import com.huivip.gpsspeedwidget.utils.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherService implements AMapLocationListener {
    String cityName;
    String adCode;
    String address;
    String pre_adCode;
    String pre_address;
    String deviceId;
    Context context;
    String resultText;
    AMapLocationClient mLocationClient = null;
    GpsUtil gpsUtil;
    private Handler handler=null;
    boolean isLocationStarted=false;
    private static WeatherService instance;
    private static final String NOTIFICATION_CHANNEL_NAME = "BackgroundLocation";
    private NotificationManager notificationManager = null;
    boolean isCreateChannel = false;
    AMapLocation lastedLocation;
    AlarmManager alarm ;
    boolean running=false;
    private WeatherService(Context context){
        this.context=context;
        handler=new Handler();
        gpsUtil=GpsUtil.getInstance(context.getApplicationContext());
        mLocationClient = new AMapLocationClient(context);
        mLocationClient.setLocationListener(this);
        startLocation();
        DeviceUuidFactory deviceUuidFactory=new DeviceUuidFactory(context);
        deviceId=deviceUuidFactory.getDeviceUuid().toString();
        alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }
    public static WeatherService getInstance(Context context){
        if(instance==null){
            instance=new WeatherService(context);
        }
        return instance;
    }
    public void startLocation(){

        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.Transport);
        //mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);
        mLocationOption.setInterval(5000);
        mLocationOption.setLocationCacheEnable(false);
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.startLocation();
        isLocationStarted = true;
        if (PrefUtils.isShowNotification(context)) {
            mLocationClient.enableBackgroundLocation(2001, buildNotification());
        }
    }
    public void stopLocation(){
        mLocationClient.stopLocation();
        isLocationStarted=false;
        if (android.os.Build.VERSION.SDK_INT >= 27 || PrefUtils.isShowNotification(context)) {
            mLocationClient.disableBackgroundLocation(true);
        }
    }
    public void setCityName(String cityName){
        this.cityName=cityName;
        Toast.makeText(context,"当前所在:"+cityName,Toast.LENGTH_SHORT).show();
        //searchWeather();
    }
    public void searchWeather(){
        if(TextUtils.isEmpty(adCode)){
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                String result=HttpUtils.getData(String.format(Constant.LBSSEARCHWEATHER,PrefUtils.getAmapWebKey(context), adCode));
                if(!TextUtils.isEmpty(result)){
                    try {
                        JSONObject resultObj=new JSONObject(result);
                        if("1".equalsIgnoreCase(resultObj.getString("status"))
                                && "10000".equalsIgnoreCase(resultObj.getString("infocode"))){
                            JSONArray lives=resultObj.getJSONArray("lives");
                            //if(lives==null || lives.length()==0) return;
                            JSONObject cityWeather=lives.getJSONObject(0);
                            resultText = "当前:" + cityWeather.getString("city") + ",天气：" + cityWeather.getString("weather") +
                                    ",气温:" + cityWeather.getString("temperature") + "°,"
                                    + cityWeather.getString("winddirection") + "风" +cityWeather.getString("windpower") + "级," +
                                    "湿度" + cityWeather.getString("humidity") + "%";
                            if(PrefUtils.isPlayWeather(context)) {
                                /*SpeechFactory.getInstance(context)
                                        .getTTSEngine(PrefUtils.getTtsEngine(context))
                                        .speak(resultText, true);*/
                                gpsUtil.getTts().speak(resultText,true);
                                handler.post(runnableUi);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    Runnable   runnableUi=new  Runnable(){
        @Override
        public void run() {
            String showStr="";
            if(!TextUtils.isEmpty(address)){
                showStr+="当前地址:"+address;
            }
            if(!TextUtils.isEmpty(resultText)){
                if(TextUtils.isEmpty(address)){
                    showStr= resultText;
                } else {
                    showStr+="\n\n"+ resultText;
                }
            }
            if(!TextUtils.isEmpty(showStr)) {
                Intent textFloat=new Intent(context,TextFloatingService.class);
                textFloat.putExtra(TextFloatingService.SHOW_TEXT,showStr);
                textFloat.putExtra(TextFloatingService.SHOW_TIME,10);
                context.startService(textFloat);
                //ToastUtil.show(context,showStr,100000);
                //Toast.makeText(context, showStr, Toast.LENGTH_LONG).show();
            }
            resultText =null;
        }

    };


    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                //Log.d("huivip",aMapLocation.toString());
                if(!TextUtils.isEmpty(aMapLocation.getCity())) {
                    cityName=aMapLocation.getCity();
                    adCode =aMapLocation.getAdCode();
                    gpsUtil.setCityName(cityName);
                    //Toast.makeText(context,cityName+ adCode,Toast.LENGTH_SHORT).show();
                }
                if(!TextUtils.isEmpty(aMapLocation.getAdCode())){
                    //district=aMapLocation.getDistrict();
                    if(TextUtils.isEmpty(pre_adCode)){
                        pre_adCode =adCode;
                    } else if (!adCode.equalsIgnoreCase(pre_adCode)){
                        searchWeather();
                        pre_adCode =adCode;
                    }
                }
                if(!running && gpsUtil.getKmhSpeed()>0 && PrefUtils.isHideFlatingWindowWhenStop(context)){
                    Utils.startFloatingWindows(context.getApplicationContext(),true);
                }
                if(lastedLocation==null || aMapLocation.distanceTo(lastedLocation)>50){
                    if(lastedLocation!=null && lastedLocation!=aMapLocation) {
                        running=true;
                    }
                    lastedLocation=aMapLocation;
                }

                //Toast.makeText(context,aMapLocation.toString(),Toast.LENGTH_SHORT).show();
                if(!TextUtils.isEmpty(aMapLocation.getStreet()) && aMapLocation.getLocationType() == 1){
                    if(!gpsUtil.isAutoMapBackendProcessStarted() && !gpsUtil.isCatchRoadServiceStarted() &&
                            (TextUtils.isEmpty(gpsUtil.getCurrentRoadName()) ||
                                    !aMapLocation.getStreet().equalsIgnoreCase(gpsUtil.getCurrentRoadName()))){
                        gpsUtil.setCurrentRoadName(aMapLocation.getStreet());
                    }
                    address=aMapLocation.getAddress();
                }
                if (gpsUtil.getSpeed() == 0 && running) {
                    running = false;
                    if (PrefUtils.isHideFlatingWindowWhenStop(context)) {
                        Utils.startFloatingWindows(context.getApplicationContext(), false);
                    }
                    if(PrefUtils.isShowAddressWhenStop(context)) {
                        if (!address.equalsIgnoreCase(pre_address)) {
                            pre_address = address;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if(gpsUtil.getSpeed()==0) {
                                        /*SpeechFactory.getInstance(context)
                                                .getTTSEngine(PrefUtils.getTtsEngine(context))
                                                .speak(address, true);*/
                                        gpsUtil.getTts().speak(address,true);
                                        handler.post(runnableUi);
                                    }
                                }
                            }, 3000);
                        }
                    }
                }
            }else {
                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Log.e("AmapError","location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
        //mLocationClient.stopLocation();
    }
    @SuppressLint("NewApi")
    private Notification buildNotification() {

        Notification.Builder builder = null;
        Notification notification = null;
        if(android.os.Build.VERSION.SDK_INT >= 26) {
            //Android O上对Notification进行了修改，如果设置的targetSDKVersion>=26建议使用此种方式创建通知栏
            if (null == notificationManager) {
                notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            }
            String channelId = context.getPackageName();
            if(!isCreateChannel) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId,
                        NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.enableLights(true);//是否在桌面icon右上角展示小圆点
                notificationChannel.setLightColor(Color.BLUE); //小圆点颜色
                notificationChannel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
                notificationManager.createNotificationChannel(notificationChannel);
                isCreateChannel = true;
            }
            builder = new Notification.Builder(context, channelId);
        } else {
            builder = new Notification.Builder(context);
        }
        builder.setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("GPS速度插件")
                .setSmallIcon(R.drawable.ic_speedometer_notif)
                .setContentText("正在后台运行")
                .setWhen(System.currentTimeMillis());

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            notification = builder.build();
        } else {
            return builder.getNotification();
        }
        return notification;
    }
}
