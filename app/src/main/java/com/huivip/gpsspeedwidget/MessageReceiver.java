package com.huivip.gpsspeedwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                Date now = new Date();
                DBUtil dbUtil = new DBUtil(context);
                List<LocationVO> locationVOList = dbUtil.getFromDate(now);
                if (null != locationVOList && locationVOList.size() > 0) {
                    JSONArray jsonArray = new JSONArray();
                    for(LocationVO vo:locationVOList){
                        JSONObject jsonvo=new JSONObject();
                        try {
                            jsonvo.put("lng",vo.getLng());
                            jsonvo.put("lat",vo.getLat());
                            jsonvo.put("createTime",vo.getCreateTime());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        jsonArray.put(jsonvo);
                    }
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("deviceid", "001");
                    params.put("t", "gps");
                    params.put("data", jsonArray.toString());
                    String result = HttpUtils.submitPostData(Constant.LBSPOSTURL, params, "utf-8");
                    Log.d("GPSWidget","Upload Data Result:"+result);
                    dbUtil.delete(now);
                }
            }
        }.start();
    }
}
