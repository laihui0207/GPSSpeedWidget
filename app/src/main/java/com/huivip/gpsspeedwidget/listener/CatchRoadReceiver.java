package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.text.TextUtils;

import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.model.LatLng;
import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.GpsUtil;
import com.huivip.gpsspeedwidget.utils.CycleQueue;
import com.huivip.gpsspeedwidget.utils.HttpUtils;
import com.huivip.gpsspeedwidget.utils.PrefUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;

public class CatchRoadReceiver extends BroadcastReceiver {
    NumberFormat localNumberFormat = NumberFormat.getNumberInstance();
    @Override
    public void onReceive(Context context, Intent intent) {
        //  DBUtil dbUtil=new DBUtil(context);
        // List<LocationVO> lastPoint=dbUtil.getLastedData("5");
        String latlng = "";
        String dateStr = "";
        String bearingStr = "";
        String speedStr = "";
        GpsUtil gpsUtil = GpsUtil.getInstance(context.getApplicationContext());
       /* if (!gpsUtil.isGpsLocationChanged()) {
            return;
        }*/

        CoordinateConverter converter = new CoordinateConverter(context);
        converter.from(CoordinateConverter.CoordType.GPS);
        CycleQueue<Location> cycleQueue = gpsUtil.getLocationVOCycleQueue();
        if (cycleQueue!=null && cycleQueue.get() != null && cycleQueue.get().length > 3) {
            for (Object vo : cycleQueue.get()) {
                if (vo == null) continue;
                Location location = (Location) vo;
                LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
                converter.coord(latLng);
                LatLng lastedLatLng=converter.convert();
                latlng += lastedLatLng.longitude+ "," + lastedLatLng.latitude + "|";
                dateStr += location.getTime() + ",";
                localNumberFormat.setMaximumIntegerDigits(1);
                bearingStr += localNumberFormat.format(location.getBearing()) + ",";
                speedStr += localNumberFormat.format(location.getSpeed()) + ",";
            }
            if (!TextUtils.isEmpty(latlng) && latlng.length()>1) {
                latlng = latlng.substring(0, latlng.length() - 1);
            }
            if (!TextUtils.isEmpty(dateStr) && dateStr.length()>1) {
                dateStr = dateStr.substring(0, dateStr.length() - 1);
            }
            if (!TextUtils.isEmpty(bearingStr) && bearingStr.length()>1) {
                bearingStr = bearingStr.substring(0, bearingStr.length() - 1);
            }
            if (!TextUtils.isEmpty(speedStr) && speedStr.length()>1) {
                speedStr = speedStr.substring(0, speedStr.length() - 1);
            }
        }
        if(TextUtils.isEmpty(latlng) || latlng.split("|").length<3){
            return ;
        }
        String catchRoadUrl = String.format(Constant.AUTONAVI_CATCH_ROAD_WEBSERVICE, PrefUtils.getDeviceIdString(context),
                latlng, dateStr, bearingStr, speedStr, PrefUtils.getAmapWebKey(context));
        //String catchRoadUrl="http://restapi.amap.com/v3/autograsp?carid=abcd123456&locations=117.1350502968,31.8210904697|117.1381402016,31.8211451673|117.1404147148,31.8210357720&time=1434077500,1434077501,1434077510&direction=358.95,359.26,359.12&speed=1,1,2&output=json&key=5303c7587d2ae8725d2abde74abee79d&extensions=all";
        new Thread(new Runnable() {
            @Override
            public void run() {
                String resultContent = HttpUtils.getData(catchRoadUrl);
                try {
                    JSONObject data = new JSONObject(resultContent);
                    if ("1".equalsIgnoreCase(data.getString("status"))) {
                        JSONArray roads = data.getJSONArray("roads");
                        if (roads.length() > 1) {
                            gpsUtil.setCatchRoadServiceStarted(true);
                            String name="";
                            int limit=0;
                            int level=0;
                            for(int i=0;i<roads.length();i++) {
                                JSONObject lastRoads = roads.getJSONObject(i);
                                int roadSpeed = lastRoads.getInt("maxspeed");
                                if(roadSpeed>0) {
                                    String roadName = lastRoads.getString("roadname");
                                    int roadLevel = lastRoads.getInt("roadlevel");
                                    if (!TextUtils.isEmpty(roadName) && !roadName.equalsIgnoreCase("[]")) {
                                        // gpsUtil.setCurrentRoadName(roadName);
                                        name = roadName;
                                    }
                                    if (roadSpeed != -1) {
                                        // gpsUtil.setLimitSpeed(roadSpeed);
                                        limit = roadSpeed;
                                        //gpsUtil.setCameraType(9999);
                                    }
                                    if (roadLevel > 0) {
                                        level = roadLevel;
                                    }
                                }
                            }
                            if(!TextUtils.isEmpty(name)){
                                gpsUtil.setCurrentRoadName(name);
                            }
                            if(limit>0){
                                gpsUtil.setLimitSpeed(limit);
                                gpsUtil.setCameraType(9999);
                            }
                            if(level>0){
                                gpsUtil.setRoadType(level);
                            }

                        }
                        else {
                            gpsUtil.setCatchRoadServiceStarted(false);
                            gpsUtil.setLimitSpeed(0);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}


