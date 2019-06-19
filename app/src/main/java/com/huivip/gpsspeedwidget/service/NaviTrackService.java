package com.huivip.gpsspeedwidget.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.amap.api.track.AMapTrackClient;
import com.amap.api.track.ErrorCode;
import com.amap.api.track.OnTrackLifecycleListener;
import com.amap.api.track.TrackParam;
import com.amap.api.track.query.model.AddTerminalRequest;
import com.amap.api.track.query.model.AddTerminalResponse;
import com.amap.api.track.query.model.AddTrackRequest;
import com.amap.api.track.query.model.AddTrackResponse;
import com.amap.api.track.query.model.QueryTerminalRequest;
import com.amap.api.track.query.model.QueryTerminalResponse;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.activity.MainActivity;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.SimpleOnTrackLifecycleListener;
import com.huivip.gpsspeedwidget.utils.SimpleOnTrackListener;

public class NaviTrackService extends Service {
    private String TAG = "GpSWidget";
    private static final String CHANNEL_ID_SERVICE_RUNNING = "CHANNEL_ID_SERVICE_RUNNING";
    boolean isServiceRunning;
    boolean isGatherRunning;
    private AMapTrackClient aMapTrackClient;
    private long terminalId;
    private long trackId;
    private long serviceId;
    private String TERMINAL_NAME;
    private boolean uploadToTrack = true;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        serviceId = Long.parseLong(PrefUtils.getAmapTrackServiceID(getApplicationContext()));
        TERMINAL_NAME = "Track_" + PrefUtils.getShortDeviceId(getApplicationContext());
        aMapTrackClient = new AMapTrackClient(getApplicationContext());
        aMapTrackClient.setInterval(1  , 20);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startTrack();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (isServiceRunning) {
            aMapTrackClient.stopTrack(new TrackParam(serviceId, terminalId), new SimpleOnTrackLifecycleListener());
        }
        super.onDestroy();
    }

    private OnTrackLifecycleListener onTrackListener = new SimpleOnTrackLifecycleListener() {
        @Override
        public void onBindServiceCallback(int status, String msg) {
        }

        @Override
        public void onStartTrackCallback(int status, String msg) {
            if (status == ErrorCode.TrackListen.START_TRACK_SUCEE || status == ErrorCode.TrackListen.START_TRACK_SUCEE_NO_NETWORK) {
                // 成功启动
                Toast.makeText(getApplicationContext(), "启动轨迹服务成功", Toast.LENGTH_SHORT).show();
                isServiceRunning = true;
                aMapTrackClient.setTrackId(trackId);
                aMapTrackClient.startGather(onTrackListener);
            } else if (status == ErrorCode.TrackListen.START_TRACK_ALREADY_STARTED) {
                // 已经启动
                Toast.makeText(getApplicationContext(), "轨迹服务已经启动", Toast.LENGTH_SHORT).show();
                isServiceRunning = true;
            }
        }

        @Override
        public void onStopTrackCallback(int status, String msg) {
            if (status == ErrorCode.TrackListen.STOP_TRACK_SUCCE) {
                // 成功停止
                Toast.makeText(getApplicationContext(), "停止轨迹服务成功", Toast.LENGTH_SHORT).show();
                isServiceRunning = false;
                isGatherRunning = false;
                /*                updateBtnStatus();*/
            } else {
              /*  Toast.makeText(getApplicationContext(),
                        "error onStopTrackCallback, status: " + status + ", msg: " + msg,
                        Toast.LENGTH_LONG).show();*/

            }
        }

        @Override
        public void onStartGatherCallback(int status, String msg) {
            if (status == ErrorCode.TrackListen.START_GATHER_SUCEE) {
                Toast.makeText(getApplicationContext(), "定位采集开启成功", Toast.LENGTH_SHORT).show();
                isGatherRunning = true;
                /*                updateBtnStatus();*/
            } else if (status == ErrorCode.TrackListen.START_GATHER_ALREADY_STARTED) {
                Toast.makeText(getApplicationContext(), "定位采集已经开启", Toast.LENGTH_SHORT).show();
                isGatherRunning = true;
                /*                updateBtnStatus();*/
            } else {
                /*Toast.makeText(getApplicationContext(),
                        "error onStartGatherCallback, status: " + status + ", msg: " + msg,
                        Toast.LENGTH_LONG).show();*/
            }
        }

        @Override
        public void onStopGatherCallback(int status, String msg) {
            if (status == ErrorCode.TrackListen.STOP_GATHER_SUCCE) {
                Toast.makeText(getApplicationContext(), "定位采集停止成功", Toast.LENGTH_SHORT).show();
                isGatherRunning = false;
                /*                updateBtnStatus();*/
            } else {
               /* Toast.makeText(getApplicationContext(),
                        "error onStopGatherCallback, status: " + status + ", msg: " + msg,
                        Toast.LENGTH_LONG).show();*/
            }
        }
    };

    private void startTrack() {
        // 先根据Terminal名称查询Terminal ID，如果Terminal还不存在，就尝试创建，拿到Terminal ID后，
        // 用Terminal ID开启轨迹服务
        aMapTrackClient.queryTerminal(new QueryTerminalRequest(serviceId, TERMINAL_NAME), new SimpleOnTrackListener() {
            @Override
            public void onQueryTerminalCallback(QueryTerminalResponse queryTerminalResponse) {
                if (queryTerminalResponse.isSuccess()) {
                    if (queryTerminalResponse.isTerminalExist()) {
                        // 当前终端已经创建过，直接使用查询到的terminal id
                        terminalId = queryTerminalResponse.getTid();
                        aMapTrackClient.addTrack(new AddTrackRequest(serviceId, terminalId), new SimpleOnTrackListener() {
                            @Override
                            public void onAddTrackCallback(AddTrackResponse addTrackResponse) {
                                if (addTrackResponse.isSuccess()) {
                                    // trackId需要在启动服务后设置才能生效，因此这里不设置，而是在startGather之前设置了track id
                                    trackId = addTrackResponse.getTrid();
                                    TrackParam trackParam = new TrackParam(serviceId, terminalId);
                                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        trackParam.setNotification(createNotification());
                                    }
                                    aMapTrackClient.startTrack(trackParam, onTrackListener);
                                } else {
                                    Toast.makeText(getApplicationContext(), "网络请求失败，" + addTrackResponse.getErrorMsg(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        // 当前终端是新终端，还未创建过，创建该终端并使用新生成的terminal id
                        aMapTrackClient.addTerminal(new AddTerminalRequest(TERMINAL_NAME, serviceId), new SimpleOnTrackListener() {
                            @Override
                            public void onCreateTerminalCallback(AddTerminalResponse addTerminalResponse) {
                                if (addTerminalResponse.isSuccess()) {
                                    terminalId = addTerminalResponse.getTid();
                                    TrackParam trackParam = new TrackParam(serviceId, terminalId);
                                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        trackParam.setNotification(createNotification());
                                    }
                                    aMapTrackClient.startTrack(trackParam, onTrackListener);
                                } else {
/*                                    Toast.makeText(getApplicationContext(), "网络请求失败，" + addTerminalResponse.getErrorMsg(), Toast.LENGTH_SHORT).show();*/
                                }
                            }
                        });
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "网络请求失败，" + queryTerminalResponse.getErrorMsg(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 在8.0以上手机，如果app切到后台，系统会限制定位相关接口调用频率
     * 可以在启动轨迹上报服务时提供一个通知，这样Service启动时会使用该通知成为前台Service，可以避免此限制
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private Notification createNotification() {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_SERVICE_RUNNING, "app service", NotificationManager.IMPORTANCE_LOW);
            nm.createNotificationChannel(channel);
            builder = new Notification.Builder(getApplicationContext(), CHANNEL_ID_SERVICE_RUNNING);
        } else {
            builder = new Notification.Builder(getApplicationContext());
        }
        Intent nfIntent = new Intent(getApplicationContext(), MainActivity.class);
        nfIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        builder.setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, nfIntent, 0))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("猎鹰sdk运行中")
                .setContentText("猎鹰sdk运行中");
        Notification notification = builder.build();
        return notification;
    }

}
