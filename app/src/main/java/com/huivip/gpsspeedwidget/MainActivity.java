package com.huivip.gpsspeedwidget;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.amap.api.maps.*;
import com.amap.api.maps.model.*;
import com.amap.api.navi.*;
import com.amap.api.navi.model.*;
import com.amap.api.navi.view.DriveWayView;
import com.amap.api.trace.LBSTraceClient;
import com.amap.api.trace.TraceListener;
import com.amap.api.trace.TraceLocation;
import com.amap.api.trace.TraceOverlay;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.huivip.gpsspeedwidget.utils.HttpUtils;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
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

        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.interval(2000);
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.setMyLocationEnabled(true);

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
                    //drawLine(msg);
                    drawLineAndFixPoint(msg);
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
                        } else {
                            DBUtil dbUtil=new DBUtil(getApplicationContext());
                            List<LocationVO> lastPoint=dbUtil.getLastedData();
                            if(lastPoint!=null && !lastPoint.isEmpty()){
                                JSONArray datas=new JSONArray();
                                JSONObject data=new JSONObject();
                                try {
                                    data.put("lng",lastPoint.get(0).getLng());
                                    data.put("lat",lastPoint.get(0).getLat());
                                    data.put("createTime",lastPoint.get(0).getCreateTime());
                                    datas.put(data);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Message message = Message.obtain();
                                message.obj = datas.toString();
                                message.arg1 = Constant.POINT;
                                lastedPositionHandler.handleMessage(message);
                            }
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
        AutoCompleteTextView textUid=findViewById(R.id.editText_UID);
        DeviceUuidFactory deviceUuidFactory=new DeviceUuidFactory(getApplicationContext());
        String deviceId=deviceUuidFactory.getDeviceUuid().toString();
        String deviceId_shortString=deviceId.substring(0,deviceId.indexOf("-"));
        PrefUtils.setDeviceIDString(getApplicationContext(),deviceId_shortString);
        String devices=PrefUtils.getDeviceIdStorage(getApplicationContext());
        Log.d("huivip","devices:"+devices);
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
                        } else {
                            DBUtil dbUtil=new DBUtil(getApplicationContext());
                            List<LocationVO> list=dbUtil.getBetweenDate(startDate,endDate);
                            if(list!=null && !list.isEmpty()){
                                JSONArray datas=new JSONArray();
                                for(LocationVO vo:list) {
                                    JSONObject data = new JSONObject();
                                    try {
                                        data.put("lng", vo.getLng());
                                        data.put("lat", vo.getLat());
                                        data.put("createTime", vo.getCreateTime());
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
                        }
                    }
                }).start();
            }
        };
        trackBtn.setOnClickListener(trackBtnListener);
        //setSystemUiVisibility(this,true);
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
        List<LatLng> latLngs = new ArrayList<>();
        LatLng lastedLatLng=null;
        LatLng firstLatLng=null;
        String dataResult=(String)msg.obj;
        try {
            if (dataResult != "-1") {
                CoordinateConverter converter = new CoordinateConverter(getApplicationContext());
                converter.from(CoordinateConverter.CoordType.GPS);
                JSONArray datas = new JSONArray(dataResult);
                for(int i=0;i<datas.length();i++){
                    JSONObject data=datas.getJSONObject(i);
                    LatLng latLng=new LatLng(data.getDouble("lat"),data.getDouble("lng"));
                    converter.coord(latLng);
                    lastedLatLng=converter.convert();
                    if(i==0){
                        firstLatLng=lastedLatLng;
                    }
                    latLngs.add(lastedLatLng);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        aMap.clear();
        aMap.addPolyline(new PolylineOptions().
                addAll(latLngs).setDottedLine(true).width(5).color(Color.argb(255, 58, 173, 211)));
        aMap.addMarker(new MarkerOptions().position(lastedLatLng).title("车辆位置").snippet("车辆最后的位置"));
        aMap.addMarker(new MarkerOptions().position(firstLatLng).title("车辆位置").snippet("车辆开始的位置"));
        CameraUpdate mCameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(lastedLatLng,13,0,0));
        aMap.moveCamera(mCameraUpdate);

    }
    private void drawLineAndFixPoint(Message msg) {
        LatLng lastedLatLng = null;
        LatLng firstLatLng = null;
        String dataResult = (String) msg.obj;
        String format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.CHINA);
        String startTime="";
        Map<String,List<TraceLocation>> lineDatas=new HashMap<>();
        CoordinateConverter converter = new CoordinateConverter(getApplicationContext());
        converter.from(CoordinateConverter.CoordType.GPS);
        try {
            if (dataResult != "-1") {

                JSONArray datas = new JSONArray(dataResult);
                List<TraceLocation> locations=null;
                for (int i = 0; i < datas.length(); i++) {
                    JSONObject data = datas.getJSONObject(i);
                    String lineId=data.getString("lineId");
                    if(lineDatas.containsKey(lineId)){
                        locations=lineDatas.get(lineId);
                    }
                    else {
                        locations=new ArrayList<>();
                    }

                    LatLng latLng=new LatLng(data.getDouble("lat"),data.getDouble("lng"));
                    converter.coord(latLng);
                    lastedLatLng=converter.convert();
                    if(i==0){
                        firstLatLng=lastedLatLng;
                        startTime=data.getString("createTime");
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
                    lineDatas.put(lineId,locations);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        aMap.clear(true);


       if(lineDatas!=null && lineDatas.size()>0){
           int i=10;
           for(String key: lineDatas.keySet()){
               List<TraceLocation> locations=lineDatas.get(key);
               LatLng latLng=new LatLng(locations.get(0).getLatitude(),locations.get(0).getLongitude());
               converter.coord(latLng);
               firstLatLng=converter.convert();
               aMap.addMarker(new MarkerOptions().position(firstLatLng).title("车辆位置").snippet("车辆开始的位置\n时间："+dateFormat.format(new Date(locations.get(0).getTime()))));
               mTraceClient.queryProcessedTrace(i++, locations,
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
        aMap.setMyLocationEnabled(false);
        if (lat != 0 && lng != 0) {
            LatLng latLng = new LatLng(lat, lng);
            CoordinateConverter converter = new CoordinateConverter(getApplicationContext());
            converter.from(CoordinateConverter.CoordType.GPS);
            converter.coord(latLng);
            LatLng desLatLng = converter.convert();
            final Marker marker = aMap.addMarker(new MarkerOptions().position(desLatLng).title("车辆位置").snippet("车辆最后的位置\n时间："+lastedPointTime));
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
        LatLng lastedLatLng=null;
        if(latLngs!=null && latLngs.size()>0){
            lastedLatLng=latLngs.get(latLngs.size()-1);
        }
        TraceOverlay mTraceOverlay = new TraceOverlay(aMap,latLngs);
        mTraceOverlay.setProperCamera(latLngs);
        mTraceOverlay.zoopToSpan();
        aMap.addMarker(new MarkerOptions().position(lastedLatLng).title("车辆位置").snippet("车辆最后的位置\n" +
                "总行程："+decimalFormat.format(distance/1000D)+"公里\n等待时间："+decimalFormat.format(watingtime/60d)+" 分钟"));
        Log.d("huviip","纠偏结束！");
    }

}
