package com.huivip.gpsspeedwidget.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.traffic.CircleTrafficQuery;
import com.amap.api.services.traffic.RoadTrafficQuery;
import com.amap.api.services.traffic.TrafficSearch;
import com.amap.api.services.traffic.TrafficStatusInfo;
import com.amap.api.services.traffic.TrafficStatusResult;
import com.huivip.gpsspeedwidget.beans.SearchTrafficEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

@SuppressLint("Registered")
public class SearchTrafficService extends Service implements TrafficSearch.OnTrafficSearchListener {
    public static String EXTRA_CLOSE="close";
    TrafficSearch trafficSearch ;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        trafficSearch = new TrafficSearch(this);
        trafficSearch.setTrafficSearchListener(this);
    }

    @Override
    public void onDestroy() {
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getBooleanExtra(EXTRA_CLOSE,false)){
            stopSelf();
            return onStartCommand(intent,flags,startId);
        }
        return START_REDELIVER_INTENT;
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void searchTraffic(SearchTrafficEvent event){
        if(event.getQueryType().equalsIgnoreCase(SearchTrafficEvent.CIRCRL_QUERY)){
            LatLonPoint point=new LatLonPoint(event.getLatitude(),event.getLongitude());
            CircleTrafficQuery query=new CircleTrafficQuery(point,event.getRadius(),event.getRoadLevel());
            try {
                trafficSearch.loadTrafficByCircle(query);
            } catch (AMapException e) {
                e.printStackTrace();
            }
        } else if(event.getQueryType().equalsIgnoreCase(SearchTrafficEvent.ROAD_QUERY)){
            RoadTrafficQuery query=new RoadTrafficQuery(event.getRoadName(),event.getAdCode(),event.getRoadLevel());
            try {
                trafficSearch.loadTrafficByRoad(query);
            } catch (AMapException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onRoadTrafficSearched(TrafficStatusResult trafficStatusResult, int errorCode) {
        if(errorCode!=1000) {
            Log.d("GPSWidget","TrafficSearch failed,errorCode:"+errorCode);
            return;
        }
        Toast.makeText(getApplicationContext(), trafficStatusResult.getDescription(), Toast.LENGTH_LONG).show();
        List<TrafficStatusInfo> list=trafficStatusResult.getRoads();
        String resultString="";
        for(TrafficStatusInfo info:list){
            resultString+=info.getName()+info.getStatus()+",";
        }
        Toast.makeText(getApplicationContext(), resultString, Toast.LENGTH_LONG).show();
    }
}
