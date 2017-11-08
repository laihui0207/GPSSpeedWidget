package com.huivip.gpsspeedwidget;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import com.amap.api.maps.*;
import com.amap.api.maps.model.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author sunlaihui
 */
public class MainActivity extends Activity {
    MapView mMapView = null;
    Calendar myCalendar = Calendar.getInstance();
    String selectDateStr="";
    AMap aMap=null;
    String myFormat = "MM/dd/yyyy"; //In which you need put here
    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.CHINA);
    DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub

            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);


            //edittext.setText(sdf.format(myCalendar.getTime()));
            updateLabel(sdf.format(myCalendar.getTime()));
        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        aMap = mMapView.getMap();

        /*MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.interval(2000);
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.setMyLocationEnabled(true);*/

        Button lastedPosition= (Button) findViewById(R.id.lastedBtn);
        final Handler lastedPositionHandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.arg1==Constant.POINT) {
                    drawPoint(msg);
                }
                else if (msg.arg1==Constant.LINE){
                   drawLine(msg);
                }
            }
        };
        View.OnClickListener lastedButtonClickLister=new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String getLastedURL="";
                        DeviceUuidFactory deviceUuidFactory=new DeviceUuidFactory(getApplicationContext());
                        String deviceId=deviceUuidFactory.getDeviceUuid().toString();
                        getLastedURL=Constant.LBSURL+String.format(Constant.LBSGETLASTEDPOSTIONURL,deviceId);
                        String dataResult=HttpUtils.getData(getLastedURL);
                        Log.d("GPSWidget","URL:"+getLastedURL+",Result:"+dataResult);

                        Message message=Message.obtain();
                        message.obj=dataResult;
                        message.arg1=Constant.POINT;
                        lastedPositionHandler.handleMessage(message);

                    }
                }).start();
            }
        };
        lastedPosition.setOnClickListener(lastedButtonClickLister);


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
                        String endTime="";
                        try {
                            Date selectDate=sdf.parse(selectDateStr);
                            Calendar calendar=Calendar.getInstance();
                            calendar.setTime(selectDate);
                            calendar.set(Calendar.MINUTE,0);
                            calendar.set(Calendar.HOUR,0);
                            calendar.set(Calendar.SECOND,0);
                            startTime=Long.toString(calendar.getTimeInMillis());
                            calendar.add(Calendar.DAY_OF_MONTH,1);
                            endTime=Long.toString(calendar.getTimeInMillis());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        String dataUrl="";
                        DeviceUuidFactory deviceUuidFactory=new DeviceUuidFactory(getApplicationContext());
                        String deviceId="d9990887-4fae-3cb8-a53a-f95293300290";//deviceUuidFactory.getDeviceUuid().toString();//"d9990887-4fae-3cb8-a53a-f95293300290";//
                        dataUrl=Constant.LBSURL+String.format(Constant.LBSGETDATA,deviceId,startTime,endTime);
                        String dataResult=HttpUtils.getData(dataUrl);
                        Log.d("GPSWidget","URL:"+dataUrl+",Result:"+dataResult);

                        Message message=Message.obtain();
                        message.arg1=Constant.LINE;
                        message.obj=dataResult;
                        lastedPositionHandler.handleMessage(message);
                    }
                }).start();
            }
        };
        trackBtn.setOnClickListener(trackBtnListener);
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
    private void drawPoint(Message msg) {
        double lat = 0;
        double lng = 0;
        String dataResult = (String) msg.obj;
        try {
            if (dataResult != "-1") {
                JSONArray datas = new JSONArray(dataResult);
                JSONObject data = datas.getJSONObject(0);
                if (null != data) {
                    lat = data.getDouble("lat");
                    lng = data.getDouble("lng");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (lat != 0 && lng != 0) {
            LatLng latLng = new LatLng(lat, lng);
            CoordinateConverter converter = new CoordinateConverter(getApplicationContext());
            converter.from(CoordinateConverter.CoordType.GPS);
            converter.coord(latLng);
            LatLng desLatLng = converter.convert();
            final Marker marker = aMap.addMarker(new MarkerOptions().position(desLatLng).title("车辆位置").snippet("车辆最后的位置"));
            CameraUpdate mCameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(desLatLng,18,30,0));
            aMap.moveCamera(mCameraUpdate);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }
    private void updateLabel(String dateString) {
        EditText edittext= (EditText) findViewById(R.id.selectDate);
        selectDateStr=dateString;
        edittext.setText(dateString);
    }
}
