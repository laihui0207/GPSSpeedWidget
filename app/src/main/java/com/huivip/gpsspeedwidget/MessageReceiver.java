package com.huivip.gpsspeedwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.huivip.gpsspeedwidget.utils.CrashHandler;
import com.huivip.gpsspeedwidget.utils.HttpUtils;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.util.*;

/**
 * @author sunlaihui
 */
public class MessageReceiver extends BroadcastReceiver {
    private int BULK_SIZE=120;
    @Override
    public void onReceive(final Context context, Intent intent) {
        new Thread() {
            @Override
            public void run() {
                try {
                    DeviceUuidFactory deviceUuidFactory = new DeviceUuidFactory(context);
                    CrashHandler.getInstance().init(context);
                    String deviceId = deviceUuidFactory.getDeviceUuid().toString();
                    Log.d("GPSWidget", "DeviceId:" + deviceId);
                    Date now = new Date();
                    DBUtil dbUtil = new DBUtil(context);
                    List<LocationVO> locationVOList = dbUtil.getFromDate(now);
                    if (null != locationVOList && locationVOList.size() > 0) {
                        if (locationVOList.size() >= BULK_SIZE) {
                            int counter = locationVOList.size() / BULK_SIZE;
                            int leftSize = locationVOList.size() % BULK_SIZE;
                            String result="";
                            for (int i = 0; i < counter; i++) {
                                List<LocationVO> tempList = locationVOList.subList(i * BULK_SIZE + 1, i * BULK_SIZE + 1 + BULK_SIZE);
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("deviceId", deviceId);
                                params.put("t", "gps");
                                params.put("data", jsonStringFromList(tempList));
                                result = HttpUtils.submitPostData(PrefUtils.getGPSRemoteUrl(context) + Constant.LBSPOSTGPSURL, params, "utf-8");
                            }
                            if(leftSize>0) {
                                List<LocationVO> tempList = locationVOList.subList(counter * BULK_SIZE + 1, counter * BULK_SIZE + leftSize);
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("deviceId", deviceId);
                                params.put("t", "gps");
                                params.put("data", jsonStringFromList(tempList));
                                result = HttpUtils.submitPostData(PrefUtils.getGPSRemoteUrl(context) + Constant.LBSPOSTGPSURL, params, "utf-8");
                                Log.d("GPSWidget", "Upload Data Result:" + result);
                            }
                            if (result != null && result.equalsIgnoreCase("Success")) {
                                dbUtil.delete(now);
                            }
                        } else {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("deviceId", deviceId);
                            params.put("t", "gps");
                            params.put("data", jsonStringFromList(locationVOList));
                            String result = HttpUtils.submitPostData(PrefUtils.getGPSRemoteUrl(context) + Constant.LBSPOSTGPSURL, params, "utf-8");
                            Log.d("GPSWidget", "Upload Data Result:" + result);
                            if (result != null && result.equalsIgnoreCase("Success")) {
                                dbUtil.delete(now);
                            }
                        }
                    }
                }catch (Exception e){
                    Log.d("huivip","upload data Error:"+e.getLocalizedMessage());
                }
                Log.d("huivip","Upload Data Finish");
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
                jsonVO.put("speed",vo.getSpeed());
                jsonVO.put("speedValue",vo.getSpeedValue());
                jsonVO.put("bearingValue",vo.getBearingValue());
                jsonVO.put("createTime",vo.getCreateTime());
                jsonVO.put("lineId",vo.getLineId());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(jsonVO);
        }
        return jsonArray.toString();
    }
}
