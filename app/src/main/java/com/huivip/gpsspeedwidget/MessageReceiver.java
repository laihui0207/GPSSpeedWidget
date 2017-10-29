package com.huivip.gpsspeedwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * @author sunlaihui
 */
public class MessageReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        GpsUtil gpsUtil = new GpsUtil();
        gpsUtil.setContext(context);

        new Thread() {
            @Override
            public void run() {
                DeviceUuidFactory deviceUuidFactory=new DeviceUuidFactory(context);
                String deviceId=deviceUuidFactory.getDeviceUuid().toString();
                Log.d("GPSWidget","DeviceId:"+deviceId);
                Date now = new Date();
                DBUtil dbUtil = new DBUtil(context);
                List<LocationVO> locationVOList = dbUtil.getFromDate(now);
                if (null != locationVOList && locationVOList.size() > 0) {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("deviceid", deviceId);
                    params.put("t", "gps");
                    params.put("data", jsonStringFromList(locationVOList));
                    String result = HttpUtils.submitPostData(Constant.LBSPOSTURL, params, "utf-8");
                    Log.d("GPSWidget","Upload Data Result:"+result);
                    dbUtil.delete(now);
                }
            }
        }.start();
    }
    private String jsonStringFromList(List<LocationVO> locationVOList){
        JSONArray jsonArray = new JSONArray();
        for(LocationVO vo:locationVOList){
            JSONObject jsonVO=new JSONObject();
            try {
                jsonVO.put("lng",vo.getLng());
                jsonVO.put("lat",vo.getLat());
                jsonVO.put("createTime",vo.getCreateTime());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(jsonVO);
        }
        return jsonArray.toString();
    }
}
