package com.huivip.gpsspeedwidget;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.amap.api.maps.*;
import com.amap.api.maps.model.*;
import com.amap.api.trace.LBSTraceClient;
import com.amap.api.trace.TraceListener;
import com.amap.api.trace.TraceLocation;
import com.amap.api.trace.TraceOverlay;
import com.huivip.gpsspeedwidget.utils.HttpUtils;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author sunlaihui
 */
public class MainActivity extends Activity implements TraceListener {
    MapView mMapView = null;
    LBSTraceClient mTraceClient=null;
    Calendar myCalendar = Calendar.getInstance();
    String selectDateStr="";
    AMap aMap=null;
    String myFormat = "MM/dd/yyyy";
    DeviceUuidFactory deviceUuidFactory;
    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.CHINA);
    String format = "yyyy-MM-dd HH:mm:ss";
    SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.CHINA);
    Map<String,List<TraceLocation>> lineDatas=new HashMap<>();
    int totalDistance=0;
    List<TraceLocation> startPoints;
    List<TraceLocation> endPoints;
    NumberFormat localNumberFormat = NumberFormat.getNumberInstance();
    DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel(sdf.format(myCalendar.getTime()));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceUuidFactory = new DeviceUuidFactory(getApplicationContext());
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        aMap = mMapView.getMap();
        mTraceClient = LBSTraceClient.getInstance(this.getApplicationContext());
        initPermission();
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.interval(2000);
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.setMyLocationEnabled(true);
        aMap.setTrafficEnabled(true);
        UiSettings mUiSettings=aMap.getUiSettings();
        mUiSettings.setCompassEnabled(true);
        Button lastedPosition= (Button) findViewById(R.id.lastedBtn);
        final Handler lastedPositionHandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                MyLocationStyle myLocationStyle = new MyLocationStyle();
                myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
                aMap.setMyLocationStyle(myLocationStyle);
                if(msg.arg1==Constant.POINT) {
                    drawPoint(msg);
                }
                else if (msg.arg1==Constant.LINE){
                    drawLine(msg);
                    //drawLineAndFixPoint(msg);
                }
            }
        };
        View.OnClickListener lastedButtonClickLister=new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String deviceId = deviceUuidFactory.getDeviceUuid().toString();//"d9990887-4fae-3cb8-a53a-f95293300290";//
                        EditText textUid=findViewById(R.id.editText_UID);
                        String inputUid=textUid.getText().toString();
                        if(inputUid!=null && !inputUid.trim().equalsIgnoreCase("")){
                            deviceId=inputUid;
                        }
                        saveDeviceIdString(deviceId);
                        if(PrefUtils.isEnableRecordGPSHistory(getApplicationContext()) && PrefUtils.isEnableUploadGPSHistory(getApplicationContext())) {
                            String getLastedURL = "";
                            getLastedURL = PrefUtils.getGPSRemoteUrl(getApplicationContext()) + String.format(Constant.LBSGETLASTEDPOSTIONURL, deviceId);
                            String dataResult = HttpUtils.getData(getLastedURL);
                            Log.d("GPSWidget", "URL:" + getLastedURL + ",Result:" + dataResult);

                            Message message = Message.obtain();
                            message.obj = dataResult;
                            message.arg1 = Constant.POINT;
                            lastedPositionHandler.handleMessage(message);
                        } else if(PrefUtils.isEnableRecordGPSHistory(getApplicationContext())){
                            DBUtil dbUtil=new DBUtil(getApplicationContext());
                            List<LocationVO> lastPoint=dbUtil.getLastedData();
                            if(lastPoint!=null && !lastPoint.isEmpty()){
                                JSONArray datas=new JSONArray();
                                JSONObject data=new JSONObject();
                                try {
                                    data.put("lng",lastPoint.get(0).getLng());
                                    data.put("lat",lastPoint.get(0).getLat());
                                    data.put("createTime",dateFormat.format(new Date(lastPoint.get(0).getCreateTime())));
                                    datas.put(data);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Message message = Message.obtain();
                                message.obj = datas.toString();
                                message.arg1 = Constant.POINT;
                                lastedPositionHandler.handleMessage(message);
                            }
                        } else {
                            //Toast.makeText(getApplicationContext(),"没有打开行车轨迹记录开关",Toast.LENGTH_SHORT).show();
                        }

                    }
                }).start();
            }
        };
        lastedPosition.setOnClickListener(lastedButtonClickLister);
        Button configButton=findViewById(R.id.button_config);
        configButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this,ConfigurationActivity.class)));
        EditText edittext= (EditText) findViewById(R.id.selectDate);

        edittext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                        new DatePickerDialog(MainActivity.this, dateListener, myCalendar
                                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                    }
        });
        Button testbutton=(Button)findViewById(R.id.button_test);
        testbutton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,AudioTestActivity.class));
            }
        });
        Button backupButton=(Button)findViewById(R.id.button_backup);
        backupButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,BackupGPSHistoryActivity.class));
            }
        });
        AutoCompleteTextView textUid=findViewById(R.id.editText_UID);
        DeviceUuidFactory deviceUuidFactory=new DeviceUuidFactory(getApplicationContext());
        String deviceId=deviceUuidFactory.getDeviceUuid().toString();
        String deviceId_shortString=deviceId.substring(0,deviceId.indexOf("-"));
        PrefUtils.setDeviceIDString(getApplicationContext(),deviceId_shortString);
        String devices=PrefUtils.getDeviceIdStorage(getApplicationContext());
        if(devices!=null && devices.length()>0) {
            String[] devicesArray=devices.split(",");
            if(devicesArray!=null && devicesArray.length>0){
                textUid.setText(devicesArray[0]);
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.select_dialog_item,devicesArray);
            textUid.setAdapter(arrayAdapter);
        }
        else {
            textUid.setText(deviceId_shortString);
        }

        Button trackBtn= (Button) findViewById(R.id.TrackBtn);
        View.OnClickListener trackBtnListener=new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                if(selectDateStr==null || selectDateStr.equalsIgnoreCase("")){
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String startTime="";
                        Date startDate=null;
                        Date endDate=null;
                        String endTime="";
                        String deviceId = deviceUuidFactory.getDeviceUuid().toString();//"d9990887-4fae-3cb8-a53a-f95293300290";//
                        EditText textUid=findViewById(R.id.editText_UID);
                        String inputUid=textUid.getText().toString();
                        if(inputUid!=null && !inputUid.trim().equalsIgnoreCase("")){
                            deviceId=inputUid;
                        }
                        try {
                            Date selectDate=sdf.parse(selectDateStr);
                            Calendar calendar=Calendar.getInstance();
                            calendar.setTime(selectDate);
                            calendar.set(Calendar.MINUTE,0);
                            calendar.set(Calendar.HOUR,0);
                            calendar.set(Calendar.SECOND,0);
                            startTime=Long.toString(calendar.getTimeInMillis());
                            startDate=calendar.getTime();
                            calendar.add(Calendar.DAY_OF_MONTH,1);
                            endTime=Long.toString(calendar.getTimeInMillis());
                            endDate=calendar.getTime();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        if(PrefUtils.isEnableRecordGPSHistory(getApplicationContext()) && PrefUtils.isEnableUploadGPSHistory(getApplicationContext())) {
                            String dataUrl = "";
                            saveDeviceIdString(deviceId);
                            dataUrl = PrefUtils.getGPSRemoteUrl(getApplicationContext()) + String.format(Constant.LBSGETDATA, deviceId, startTime, endTime);
                            String dataResult = HttpUtils.getData(dataUrl);
                            Log.d("GPSWidget", "URL:" + dataUrl);

                            Message message = Message.obtain();
                            message.arg1 = Constant.LINE;
                            message.obj = dataResult;
                            lastedPositionHandler.handleMessage(message);
                        } else if(PrefUtils.isEnableRecordGPSHistory(getApplicationContext())) {
                            DBUtil dbUtil=new DBUtil(getApplicationContext());
                            List<LocationVO> list=dbUtil.getBetweenDate(startDate,endDate);
                            if(list!=null && !list.isEmpty()){
                                JSONArray datas=new JSONArray();
                                for(LocationVO vo:list) {
                                    JSONObject data = new JSONObject();
                                    try {
                                        data.put("lng", vo.getLng());
                                        data.put("lat", vo.getLat());
                                        data.put("createTime", dateFormat.format(new Date(vo.getCreateTime())));
                                        data.put("bearingValue",vo.getBearingValue());
                                        data.put("speedValue",vo.getSpeedValue());
                                        data.put("lineId",vo.getLineId());
                                        data.put("speed",vo.getSpeed());
                                        datas.put(data);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                Message message = Message.obtain();
                                message.obj = datas.toString();
                                message.arg1 = Constant.LINE;
                                lastedPositionHandler.handleMessage(message);
                            }
                        } else {
                            //Toast.makeText(getApplicationContext(),"没有打开行车轨迹记录开关",Toast.LENGTH_SHORT).show();
                        }
                    }
                }).start();
            }
        };
        trackBtn.setOnClickListener(trackBtnListener);
        //setSystemUiVisibility(this,true);
        Button buttonPay=findViewById(R.id.button_paymain);
        buttonPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.dialog_pay,null);
                new AlertDialog.Builder(MainActivity.this).setTitle("打赏随意，多少都是一种支持").setView(layout)
                        .setPositiveButton("关闭", null).show();
            }
        });
        startFloationgWindows(true);
    }
    private void startFloationgWindows(boolean enabled){
        Intent defaultFloatingService=new Intent(this,FloatingService.class);
        Intent autoNavifloatService=new Intent(this,AutoNaviFloatingService.class);
        Intent meterFloatingService=new Intent(this,MeterFloatingService.class);
        if(enabled){
            String floatingStyle=PrefUtils.getFloatingStyle(getApplicationContext());
            if(floatingStyle.equalsIgnoreCase(PrefUtils.FLOATING_DEFAULT)){
                if(Utils.isServiceRunning(getApplicationContext(),MeterFloatingService.class.getName())){
                    meterFloatingService.putExtra(MeterFloatingService.EXTRA_CLOSE,true);
                    startService(meterFloatingService);
                }
                if(Utils.isServiceRunning(getApplicationContext(),AutoNaviFloatingService.class.getName())){
                    autoNavifloatService.putExtra(FloatingService.EXTRA_CLOSE, true);
                    startService(autoNavifloatService);
                }
                startService(defaultFloatingService);

            } else if(floatingStyle.equalsIgnoreCase(PrefUtils.FLOATING_AUTONAVI)) {
                if(Utils.isServiceRunning(getApplicationContext(),FloatingService.class.getName())){
                    defaultFloatingService.putExtra(FloatingService.EXTRA_CLOSE, true);
                    startService(defaultFloatingService);
                }
                if(Utils.isServiceRunning(getApplicationContext(),MeterFloatingService.class.getName())){
                    meterFloatingService.putExtra(MeterFloatingService.EXTRA_CLOSE,true);
                    startService(meterFloatingService);
                }
                startService(autoNavifloatService);
            } else if(floatingStyle.equalsIgnoreCase(PrefUtils.FLOATING_METER)){
                if(Utils.isServiceRunning(getApplicationContext(),FloatingService.class.getName())){
                    defaultFloatingService.putExtra(FloatingService.EXTRA_CLOSE, true);
                    startService(defaultFloatingService);
                }
                if(Utils.isServiceRunning(getApplicationContext(),AutoNaviFloatingService.class.getName())){
                    autoNavifloatService.putExtra(FloatingService.EXTRA_CLOSE, true);
                    startService(autoNavifloatService);
                }
                startService(meterFloatingService);
            }

        }
        else {
            if(Utils.isServiceRunning(getApplicationContext(),FloatingService.class.getName())){
                defaultFloatingService.putExtra(FloatingService.EXTRA_CLOSE, true);
                startService(defaultFloatingService);
            }
            if(Utils.isServiceRunning(getApplicationContext(),AutoNaviFloatingService.class.getName())){
                autoNavifloatService.putExtra(FloatingService.EXTRA_CLOSE, true);
                startService(autoNavifloatService);
            }
            if(Utils.isServiceRunning(getApplicationContext(),MeterFloatingService.class.getName())){
                meterFloatingService.putExtra(MeterFloatingService.EXTRA_CLOSE,true);
                startService(meterFloatingService);
            }
        }
    }
    private void saveDeviceIdString(String deviceString){
        String storedDevices=PrefUtils.getDeviceIdStorage(getApplicationContext());
        if(storedDevices==null || storedDevices.equalsIgnoreCase("")){
            PrefUtils.setDeviceIDStorage(getApplicationContext(),deviceString);
        } else {
            String[] strArray=storedDevices.split(",");
            List<String> stringList =new ArrayList<>();
            for(String dId:strArray){
                if(!dId.equalsIgnoreCase(deviceString)) {
                    stringList.add(dId);
                }
            }
            String deviceStr=deviceString+",";
            for(String str:stringList){
                deviceStr+=str+",";
            }
            if(deviceStr.length()>1){
                deviceStr=deviceStr.substring(0,deviceStr.length()-1);
            }
            PrefUtils.setDeviceIDStorage(getApplicationContext(),deviceStr);
        }
    }
    private void drawLine(Message msg){

        LatLng lastedLatLng=null;
        String lastedTime="";
        String firstTime="";
        LatLng firstLatLng=null;
        String dataResult=(String)msg.obj;
        lineDatas=new HashMap<>();
        List<LatLng> totalDatas=new ArrayList<>();
        Map<String,List<TraceLocation>> tempMap=new HashMap<>();
        try {
            if (dataResult != "-1") {
                CoordinateConverter converter = new CoordinateConverter(getApplicationContext());
                converter.from(CoordinateConverter.CoordType.GPS);
                JSONArray datas = new JSONArray(dataResult);

                List<TraceLocation> latLngs = new ArrayList<>();
                for (int i = 0; i < datas.length(); i++) {
                    JSONObject data = datas.getJSONObject(i);
                    String lineId=data.getString("lineId");
                    if(tempMap.containsKey(lineId)){
                        latLngs=tempMap.get(lineId);
                    }
                    else {
                        latLngs=new ArrayList<>();
                    }

                    TraceLocation location = new TraceLocation();
                    LatLng latLng=new LatLng(data.getDouble("lat"),data.getDouble("lng"));
                    converter.coord(latLng);
                    lastedLatLng=converter.convert();
                    totalDatas.add(lastedLatLng);
                    location.setLatitude(lastedLatLng.latitude);
                    location.setLongitude(lastedLatLng.longitude);
                    if(!data.isNull("speedValue")){
                        location.setSpeed(data.getLong("speedValue")*3.6F);
                    }
                    if(!data.isNull("bearValue")){
                        location.setBearing(data.getLong("bearingValue"));
                    }
                    location.setTime(dateFormat.parse(data.getString("createTime")).getTime());
                    latLngs.add(location);
                    tempMap.put(lineId,latLngs);
                }

                if(tempMap!=null && tempMap.size()>0){
                    int i=0;
                    for(String key:tempMap.keySet()){
                        lineDatas.put(i+"",tempMap.get(key));
                        i++;
                    }
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        float totalDistance=getDistance(totalDatas);
        aMap.clear();
        for(String key:lineDatas.keySet()) {
            List<TraceLocation> locationList=lineDatas.get(key);
            List<LatLng> dataList=new ArrayList<>();
            int i=0;
            for(TraceLocation location: locationList){
                lastedLatLng=new LatLng(location.getLatitude(),location.getLongitude());
                lastedTime=dateFormat.format(location.getTime());
                if(i==0){
                    firstTime=lastedTime;
                    firstLatLng=lastedLatLng;
                }
                dataList.add(lastedLatLng);
                i++;
                //aMap.addMarker(new MarkerOptions().position(lastedLatLng).title("车辆位置").snippet("时间：" + lastedTime+"\n速度："+location.getSpeed()/3.6F+"km/h").alpha(30F)).hideInfoWindow();

            }
            TraceOverlay mTraceOverlay = new TraceOverlay(aMap, dataList);
            mTraceOverlay.setProperCamera(dataList);
            mTraceOverlay.zoopToSpan();
            localNumberFormat.setMaximumFractionDigits(1);
            Marker endMarker = aMap.addMarker(new MarkerOptions().position(lastedLatLng).title("车辆位置")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.end))
                    .snippet("车辆停驶的位置\n时间:" + lastedTime + "\n此段行程:" + localNumberFormat.format(getDistance(dataList) / 1000) + "公里\n当天总行程："+localNumberFormat.format(totalDistance/1000)+"公里"));
            endMarker.setClickable(true);
            endMarker.showInfoWindow();
            aMap.addMarker(new MarkerOptions().position(firstLatLng).title("车辆位置").icon(BitmapDescriptorFactory.fromResource(R.drawable.start)).snippet("车辆开始的位置\n时间：" + firstTime));
            CameraUpdate mCameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(lastedLatLng, 13, 0, 0));
            aMap.moveCamera(mCameraUpdate);

        }
        AMap.OnMarkerClickListener markerClickListener = new AMap.OnMarkerClickListener() {
            // marker 对象被点击时回调的接口
            // 返回 true 则表示接口已响应事件，否则返回false
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(marker.isInfoWindowShown()){
                    marker.hideInfoWindow();
                }
                else {
                    marker.showInfoWindow();
                }
                return true;
            }
        };
        aMap.setOnMarkerClickListener(markerClickListener);

       /* MultiPointOverlayOptions overlayOptions = new MultiPointOverlayOptions();
        //overlayOptions.icon(bitmapDescriptor);//设置图标
        overlayOptions.anchor(0.5f,0.5f); //设置锚点
        MultiPointOverlay multiPointOverlay = aMap.addMultiPointOverlay(overlayOptions);
        List<MultiPointItem> list = new ArrayList<MultiPointItem>();
        for(LatLng latLng: totalDatas){
            MultiPointItem multiPointItem = new MultiPointItem(latLng);
            list.add(multiPointItem);
        }
        multiPointOverlay.setItems(list);//将规范化的点集交给海量点管理对象设置，待加载完毕即可看到海量点信息
        AMap.OnMultiPointClickListener multiPointClickListener = new AMap.OnMultiPointClickListener() {
            // 海量点中某一点被点击时回调的接口
            // 返回 true 则表示接口已响应事件，否则返回false
            @Override
            public boolean onPointClick(MultiPointItem pointItem) {
                return false;
            }
        };
        // 绑定海量点点击事件
        aMap.setOnMultiPointClickListener(multiPointClickListener);*/
    }
    private void initPermission() {
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.
            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.System.canWrite(this)){
                Intent intentWriteSetting = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                intentWriteSetting.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intentWriteSetting, 124);
             }
        }
    }
    private void drawLineAndFixPoint(Message msg) {
        String dataResult = (String) msg.obj;
        lineDatas=new HashMap<>();
        totalDistance=0;
        startPoints=new ArrayList<>();
        endPoints=new ArrayList<>();
        CoordinateConverter converter = new CoordinateConverter(getApplicationContext());
        converter.from(CoordinateConverter.CoordType.GPS);
        Map<String,List<TraceLocation>> tempMap=new HashMap<>();
        try {
            if (dataResult != "-1") {

                JSONArray datas = new JSONArray(dataResult);
                List<TraceLocation> locations=null;
                for (int i = 0; i < datas.length(); i++) {
                    JSONObject data = datas.getJSONObject(i);
                    String lineId=data.getString("lineId");
                    if(tempMap.containsKey(lineId)){
                        locations=tempMap.get(lineId);
                    }
                    else {
                        locations=new ArrayList<>();
                    }

                    TraceLocation location = new TraceLocation();
                    location.setLatitude(data.getDouble("lat"));
                    location.setLongitude(data.getDouble("lng"));
                    if(!data.isNull("speedValue")){
                        location.setSpeed(data.getLong("speedValue")*3.6F);
                    }
                    if(!data.isNull("bearValue")){
                        location.setBearing(data.getLong("bearingValue"));
                    }
                    location.setTime(dateFormat.parse(data.getString("createTime")).getTime());
                    locations.add(location);
                    tempMap.put(lineId,locations);
                }

                if(tempMap!=null && tempMap.size()>0){
                    int i=0;
                    for(String key:tempMap.keySet()){
                        lineDatas.put(i+"",tempMap.get(key));
                        i++;
                    }
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        aMap.clear(true);


       if(lineDatas!=null && lineDatas.size()>0){
           for(String key: lineDatas.keySet()){
               List<TraceLocation> locations=lineDatas.get(key);
               mTraceClient.queryProcessedTrace(Integer.parseInt(key), locations,
                       LBSTraceClient.TYPE_GPS, this);
           }
       }
    }

    private void drawPoint(Message msg) {
        double lat = 0;
        double lng = 0;
        String lastedPointTime="";
        String dataResult = (String) msg.obj;
        try {
            if (dataResult != "-1") {
                JSONArray datas = new JSONArray(dataResult);
                JSONObject data = datas.getJSONObject(0);
                if (null != data) {
                    lat = data.getDouble("lat");
                    lng = data.getDouble("lng");
                    lastedPointTime=data.getString("createTime");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        aMap.clear();
        aMap.setMyLocationEnabled(false);
        if (lat != 0 && lng != 0) {
            LatLng latLng = new LatLng(lat, lng);
            CoordinateConverter converter = new CoordinateConverter(getApplicationContext());
            converter.from(CoordinateConverter.CoordType.GPS);
            converter.coord(latLng);
            LatLng desLatLng = converter.convert();
            aMap.addMarker(new MarkerOptions().position(desLatLng).title("车辆位置").snippet("车辆最后的位置\n时间："+lastedPointTime)).showInfoWindow();
            CameraUpdate mCameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(desLatLng,18,30,0));

            aMap.moveCamera(mCameraUpdate);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mMapView!=null)
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mMapView!=null){
            mMapView.onDestroy();
        }
    }
    private void updateLabel(String dateString) {
        EditText edittext= (EditText) findViewById(R.id.selectDate);
        selectDateStr=dateString;
        edittext.setText(dateString);
    }

    @Override
    public void onRequestFailed(int i, String s) {
        Log.d("huivip","amp draw line failed:"+s);
    }

    @Override
    public void onTraceProcessing(int lineId, int index, List<LatLng> list) {
        Log.d("huivip","lineId:"+lineId);

    }
    @Override
    public void onFinished(int lineID, List<LatLng> latLngs, int distance, int watingtime) {
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        TraceOverlay mTraceOverlay = new TraceOverlay(aMap, latLngs);
        mTraceOverlay.setProperCamera(latLngs);
        mTraceOverlay.zoopToSpan();

        if (lineDatas != null) {
            totalDistance += distance;
            List<TraceLocation> locations = lineDatas.get(lineID+"");
            LatLng latLng = latLngs.get(0);
            LatLng lastedLatLng = latLngs.get(latLngs.size() - 1);
            startPoints.add(locations.get(0));
            aMap.addMarker(new MarkerOptions().position(latLng)
                    .title("车辆位置").icon(BitmapDescriptorFactory.fromResource(R.drawable.start))
                    .snippet("车辆开始的位置\n时间:" + dateFormat.format(new Date(locations.get(0).getTime()))));

            endPoints.add(locations.get(locations.size()-1));
            aMap.addMarker(new MarkerOptions().position(lastedLatLng).title("车辆位置")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.end)).snippet("车辆最后的位置\n" +
                            "行程：" + decimalFormat.format(distance / 1000D) + "公里\n时间:"
                            + dateFormat.format(new Date(locations.get(locations.size() - 1).getTime())) + "\n总行程：" + decimalFormat.format(totalDistance / 1000D) + "公里")).showInfoWindow();
        }
        Log.d("huviip", "纠偏结束！");
    }
    private float getDistance(List<LatLng> list){
        float distance=0F;
        if(list==null || list.size()==0) return distance;
        Location preLocation=null;
        for(LatLng latLng:list){
            if(preLocation==null){
                preLocation=new Location("");
                preLocation.setLatitude(latLng.latitude);
                preLocation.setLongitude(latLng.longitude);
            } else {
                Location currentLocation=new Location("");
                currentLocation.setLatitude(latLng.latitude);
                currentLocation.setLongitude(latLng.longitude);
                distance+=preLocation.distanceTo(currentLocation);
                preLocation=currentLocation;
            }
        }
        return  distance;
    }
}
