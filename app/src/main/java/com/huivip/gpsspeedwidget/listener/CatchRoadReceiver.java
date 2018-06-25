package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.GpsUtil;
import com.huivip.gpsspeedwidget.utils.CycleQueue;
import com.huivip.gpsspeedwidget.utils.HttpUtils;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CatchRoadReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //  DBUtil dbUtil=new DBUtil(context);
        // List<LocationVO> lastPoint=dbUtil.getLastedData("5");
        String latlng = "";
        String dateStr = "";
        String bearingStr = "";
        String speedStr = "";
        GpsUtil gpsUtil = GpsUtil.getInstance(context);
        Log.d("huivip","Catch Road Started");
        if (!gpsUtil.isGpsLocationChanged()) {
            Log.d("huivip","Catch Road: GPS is no Ready, quit!");
            return;
        }
        CycleQueue<Location> cycleQueue = gpsUtil.getLocationVOCycleQueue();
        Log.d("huivip","location list:"+cycleQueue.get().length);
        if (cycleQueue.get() != null && cycleQueue.get().length > 0) {
            for (Object vo : cycleQueue.get()) {
                if (vo == null) continue;
                Location location = (Location) vo;
                latlng += location.getLongitude() + "," + location.getLatitude() + "|";
                dateStr += location.getTime() + ",";
                bearingStr += location.getBearing() + ",";
                speedStr += location.getSpeed() + ",";
            }
            if (!TextUtils.isEmpty(latlng)) {
                latlng = latlng.substring(0, latlng.length() - 1);
            }
            if (!TextUtils.isEmpty(dateStr)) {
                dateStr = dateStr.substring(0, dateStr.length() - 1);
            }
            if (!TextUtils.isEmpty(bearingStr)) {
                bearingStr = bearingStr.substring(0, bearingStr.length() - 1);
            }
            if (!TextUtils.isEmpty(speedStr)) {
                speedStr = speedStr.substring(0, speedStr.length() - 1);
            }
        }
        if(TextUtils.isEmpty(latlng)){
            Log.d("huivip","Road locaton is empty, quit");
            return ;
        }
        String catchRoadUrl = String.format(Constant.AUTONAVI_CATCH_ROAD_WEBSERVICE, PrefUtils.getDeviceIdString(context), latlng, dateStr, bearingStr, speedStr, Constant.AUTONAVI_WEB_KEY);
        Log.d("huivip","catch Road:"+catchRoadUrl);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String resultContent = HttpUtils.getData(catchRoadUrl);
                try {
                    JSONObject data = new JSONObject(resultContent);
                    if ("1".equalsIgnoreCase(data.getString("status"))) {
                        JSONArray roads = data.getJSONArray("roads");
                        if (roads.length() > 1) {
                            JSONObject lastRoads = roads.getJSONObject(roads.length() - 1);
                            String roadName = lastRoads.getString("roadname");
                            String roadSpeed = lastRoads.getString("maxspeed");
                            if (!TextUtils.isEmpty(roadName) && !TextUtils.isEmpty(roadSpeed)) {
                                gpsUtil.setCurrentRoadName(roadName);
                                gpsUtil.setCameraSpeed(Integer.parseInt(roadSpeed));
                            }

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}


