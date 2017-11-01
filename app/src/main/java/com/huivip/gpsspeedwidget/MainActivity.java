package com.huivip.gpsspeedwidget;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author sunlaihui
 */
public class MainActivity extends Activity {
    MapView mMapView = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        final AMap aMap = mMapView.getMap();
       /* MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        //myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW);
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。*/

        Button lastedPostion= (Button) findViewById(R.id.lastedBtn);
        final Handler lastedPostionHandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                double lat=0;
                double lng=0;
                String dataResult= (String) msg.obj;
                try {
                    if(dataResult!="-1") {
                        JSONArray datas=new JSONArray(dataResult);
                        JSONObject data = datas.getJSONObject(0);
                        if (null != data) {
                            lat = data.getDouble("lat");
                            lng = data.getDouble("lng");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(lat!=0 && lng!=0) {
                    LatLng latLng = new LatLng(lat, lng);
                    CoordinateConverter converter = new CoordinateConverter(getApplicationContext());
                    converter.from(CoordinateConverter.CoordType.GPS);
                    converter.coord(latLng);
                    LatLng desLatLng = converter.convert();
                    final Marker marker = aMap.addMarker(new MarkerOptions().position(desLatLng).title("车辆位置").snippet("车辆最后的位置"));
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
                        lastedPostionHandler.handleMessage(message);

                    }
                }).start();
            }
        };
        lastedPostion.setOnClickListener(lastedButtonClickLister);
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
}
