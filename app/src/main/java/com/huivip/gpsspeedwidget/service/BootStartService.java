package com.huivip.gpsspeedwidget.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.amap.api.location.AMapLocationClient;
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
import com.huivip.gpsspeedwidget.AppObject;
import com.huivip.gpsspeedwidget.DeviceUuidFactory;
import com.huivip.gpsspeedwidget.GpsUtil;
import com.huivip.gpsspeedwidget.beans.AudioTempMuteEvent;
import com.huivip.gpsspeedwidget.beans.AutoCheckUpdateEvent;
import com.huivip.gpsspeedwidget.beans.AutoMapStatusUpdateEvent;
import com.huivip.gpsspeedwidget.beans.BootEvent;
import com.huivip.gpsspeedwidget.beans.FloatWindowsLaunchEvent;
import com.huivip.gpsspeedwidget.beans.GetDistrictEvent;
import com.huivip.gpsspeedwidget.beans.LaunchEvent;
import com.huivip.gpsspeedwidget.beans.LocationEvent;
import com.huivip.gpsspeedwidget.beans.PlayAudioEvent;
import com.huivip.gpsspeedwidget.listener.AutoLaunchSystemConfigReceiver;
import com.huivip.gpsspeedwidget.listener.AutoMapBoardReceiver;
import com.huivip.gpsspeedwidget.listener.BootStartReceiver;
import com.huivip.gpsspeedwidget.listener.DateChangeReceiver;
import com.huivip.gpsspeedwidget.listener.GoToHomeReceiver;
import com.huivip.gpsspeedwidget.listener.MediaNotificationReceiver;
import com.huivip.gpsspeedwidget.listener.NetWorkConnectChangedReceiver;
import com.huivip.gpsspeedwidget.listener.ScreenOnReceiver;
import com.huivip.gpsspeedwidget.listener.SwitchReceiver;
import com.huivip.gpsspeedwidget.listener.WeatherServiceReceiver;
import com.huivip.gpsspeedwidget.speech.AudioService;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.CrashHandler;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.SimpleOnTrackLifecycleListener;
import com.huivip.gpsspeedwidget.utils.SimpleOnTrackListener;
import com.huivip.gpsspeedwidget.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.xutils.x;


public class BootStartService extends Service {
    public static String START_BOOT = "FromSTARTBOOT";
    public static String START_RESUME = "FromHomeResume";
    boolean started = false;
    boolean autoMapLaunched=false;
    NetWorkConnectChangedReceiver netWorkConnectChangedReceiver;
    ScreenOnReceiver screenOnReceiver;
    boolean isServiceRunning;
    boolean isGatherRunning=false;
    private AMapTrackClient aMapTrackClient;
    private long terminalId;
    private long trackId;
    private long serviceId;
    private String TERMINAL_NAME;
    private Notification notification;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        CrashHandler.getInstance().init(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int NOTIFICATION_ID = (int) (System.currentTimeMillis() % 10000);
            notification =Utils.buildNotification(getApplicationContext(), Integer.toString(NOTIFICATION_ID));
            startForeground(NOTIFICATION_ID, notification);
        }
        EventBus.getDefault().register(this);
        registerBoardCast(getApplicationContext());
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        boolean start = AppSettings.get().getAutoStart();
        autoMapLaunched=false;
        if (intent != null) {
            boolean boot_from_resume = intent.getBooleanExtra(START_RESUME, false);
            boolean boot_from_start = intent.getBooleanExtra(START_BOOT, false);
            if (start && (!started || boot_from_resume || boot_from_start)) {
                started = true;
                if (netWorkConnectChangedReceiver == null) {
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                    netWorkConnectChangedReceiver = new NetWorkConnectChangedReceiver();
                    getApplicationContext().registerReceiver(netWorkConnectChangedReceiver, intentFilter);
                }
                if(boot_from_start || boot_from_resume){
                    GpsUtil gpsUtil = GpsUtil.getInstance(getApplicationContext());
                    gpsUtil.resetData();
                }
                if (AppSettings.get().isEnableAudio()) {
                    Intent audioService = new Intent(getApplicationContext(), AudioService.class);
                    Utils.startService(getApplicationContext(), audioService);
                }
                PrefUtils.setTempMuteAudioService(getApplicationContext(), false);

                if (AppSettings.get().isEnableTimeWindow()) {
                    Intent timeFloating = new Intent(getApplicationContext(), RealTimeFloatingService.class);
                    Utils.startService(getApplicationContext(), timeFloating);
                }
                if (AppSettings.get().isEnableRoadLine()) {
                    Intent roadLineService = new Intent(getApplicationContext(), RoadLineService.class);
                    Utils.startService(getApplicationContext(), roadLineService);
                    if (AppSettings.get().isEnableRoadLineFloatingWindow()) {
                        Intent roadLineFloatingService = new Intent(getApplicationContext(), RoadLineFloatingService.class);
                        Utils.startService(getApplicationContext(), roadLineFloatingService);
                    }
                }
                if (AppSettings.get().isEnableSpeed()) {
                    Utils.startFloatingWindows(getApplicationContext(), true);
                }

                if (AppSettings.get().isEnableAltitudeWindow()) {
                    Intent altitudeWindowService = new Intent(getApplicationContext(), AltitudeFloatingService.class);
                    Utils.startService(getApplicationContext(), altitudeWindowService);
                }
                Intent weatherService = new Intent(getApplicationContext(), WeatherService.class);
                Utils.startService(getApplicationContext(), weatherService);


                if (Utils.isNetworkConnected(getApplicationContext())) {
                    if (AppSettings.get().isEnableXunHang()){
                        Intent xunHangService = new Intent(getApplicationContext(), AutoXunHangService.class);
                        Utils.startService(getApplicationContext(), xunHangService);
                    }
                    if (AppSettings.get().isEnableTracker()){
                        Intent trackService = new Intent(getApplicationContext(), NaviTrackService.class);
                        Utils.startService(getApplicationContext(), trackService);
                    }
                    if (AppSettings.get().isEnableRecord()) {
                        startTrack();
                       /* Intent trackService = new Intent(getApplicationContext(), RecordGpsHistoryService.class);
                        Utils.startService(getApplicationContext(), trackService);*/
                    }
                    new Thread(() -> Utils.registerSelf(getApplicationContext())).start();
                    if (AppSettings.get().isAutoCheckUpdate()) {
                        x.task().postDelayed(() -> {
                            EventBus.getDefault().post(new AutoCheckUpdateEvent().setAutoCheck(true));
                        }, 1000 * 60);
                    }
                }
                EventBus.getDefault().post(new AudioTempMuteEvent(false));
                if (PrefUtils.isSpeedNumberVWidgetEnable(getApplicationContext())) {
                    Intent widgetService = new Intent(getApplicationContext(), SpeedNumberVerticalService.class);
                    Utils.startService(getApplicationContext(), widgetService);
                }
                if (PrefUtils.isTimeHWidgetEnable(getApplicationContext())) {
                    Intent widgetService = new Intent(getApplicationContext(), TimeWidgetService.class);
                    Utils.startService(getApplicationContext(), widgetService);
                }
                if (PrefUtils.isTimeVWidgetEnable(getApplicationContext())){
                    Intent widgetService = new Intent(getApplicationContext(), TimeWidgetVerticalService.class);
                    Utils.startService(getApplicationContext(), widgetService);
                }
                if (PrefUtils.isSpeedMeterWidgetEnable(getApplicationContext())){
                    Intent bootService = new Intent(getApplicationContext(), GpsSpeedMeterService.class);
                    Utils.startService(getApplicationContext(), bootService);
                }
                if (PrefUtils.isMusicWidgetEnable(getApplicationContext())){
                    Intent widgetService = new Intent(getApplicationContext(), MusicControllerService.class);
                    Utils.startService(getApplicationContext(), widgetService);
                }
            }
            EventBus.getDefault().post(new BootEvent(true));
           /* if(!Utils.isServiceRunning(getApplicationContext(),LaunchHandlerService.class.getName()))
            {*/
                Intent launchHandlerService=new Intent(getApplicationContext(), LaunchHandlerService.class);
                Utils.startService(getApplicationContext(),launchHandlerService);
           // }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (netWorkConnectChangedReceiver != null) {
            getApplicationContext().unregisterReceiver(netWorkConnectChangedReceiver);
        }
        if (screenOnReceiver != null) {
            getApplicationContext().unregisterReceiver(screenOnReceiver);
        }
        stopForeground(true);
    }

    @Subscribe
    public void launchFloatWindow(FloatWindowsLaunchEvent event){
        Utils.startFloatingWindows(getApplicationContext(),event.isEnable());
    }
    @Subscribe
    public void launchService(LaunchEvent event) {
        Intent mService = new Intent(AppObject.getContext(), event.getServiceClass());
        if(event.getExtentParameters()!=null && event.getExtentParameters().size()>0){
            for(String key:event.getExtentParameters().keySet()){
                mService.putExtra(key,event.getExtentParameters().get(key));
            }
        }
        if (event.getDelaySeconds() > 0) {
            new Handler().postDelayed(() -> {
                if (event.isToClose()) {
                    AppObject.getContext().stopService(mService);
                } else {
                    AppObject.getContext().startService(mService);
                }
            }, event.getDelaySeconds() * 1000);
        } else {
            if (event.isToClose()) {
                AppObject.getContext().stopService(mService);
            } else {
                AppObject.getContext().startService(mService);
            }
        }
    }

    @Subscribe
    public void launchAutoWidgetFloatingWindow(AutoMapStatusUpdateEvent event) {
        if (AppSettings.get().isShowAmapWidgetContent()) {
            if (event.isXunHangStarted()) {
                Intent autoWidgetFloatingService = new Intent(AppObject.getContext(), AutoWidgetFloatingService.class);
                AppObject.getContext().startService(autoWidgetFloatingService);
            }
        } else {
            Intent autoWidgetFloatingService = new Intent(AppObject.getContext(), AutoWidgetFloatingService.class);
            autoWidgetFloatingService.putExtra(AutoWidgetFloatingService.EXTRA_CLOSE, true);
            AppObject.getContext().startService(autoWidgetFloatingService);
        }
    }

    private void startTrack() {
        serviceId = Long.parseLong(PrefUtils.getAmapTrackServiceID(getApplicationContext()));
        TERMINAL_NAME = "Track_" + PrefUtils.getShortDeviceId(getApplicationContext());
        AMapLocationClient.updatePrivacyShow(getApplicationContext(),true,true);
        AMapLocationClient.updatePrivacyAgree(getApplicationContext(),true);
        try {
            aMapTrackClient = new AMapTrackClient(getApplicationContext());
        }catch (Exception e) {

        }
        if(aMapTrackClient!=null){
            aMapTrackClient.setInterval(5  , 60);
        }
        // 先根据Terminal名称查询Terminal ID，如果Terminal还不存在，就尝试创建，拿到Terminal ID后，
        // 用Terminal ID开启轨迹服务
        if(aMapTrackClient==null) return;
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
                                        trackParam.setNotification(notification);
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
                                        trackParam.setNotification(notification);
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


    private void registerBoardCast(Context context) {
        String[] actions = new String[]{"com.android.music.metachanged",
                "com.android.music.statuschanged",
                "com.android.music.musicservicecommand",
                "com.android.music.updateprogress",
                "com.kugou.android.music.metachanged",
                "com.tencent.qqmusic.widgetupdate",
                "cn.flyaudio.media.action.TRACK_DETAILS",
                "cn.kuwo.kwmusicauto.action.PLAYER_STATUS",
                "cn.kuwo.kwmusicauto.action.OPEN_DESKLYRIC",
                "com.miui.player.metachanged",
                "com.android.music.playstatechanged",
                "com.android.music.playbackcomplete",
                "com.android.music.queuechanged",
                "fm.last.android.metachanged",
                "com.nullsoft.winamp.playstatechanged",
                "update.widget.update_proBar",
                "update.widget.playbtnstate",
                "com.ijidou.card.music",
                "com.ijidou.action.UPDATE_PROGRESS",
                "com.tencent.qqmusiccar.action.PLAY_COMMAND_SEND_FOR_THIRD"
        };
        IntentFilter intentFilter = new IntentFilter();
        for (String action : actions) {
            intentFilter.addAction(action);
        }
        context.registerReceiver(new MediaNotificationReceiver(), intentFilter);

        IntentFilter switchFiliter = new IntentFilter();
        switchFiliter.addAction(SwitchReceiver.SWITCH_EVENT);
        context.registerReceiver(new SwitchReceiver(), switchFiliter);

        IntentFilter autoMapFiliter = new IntentFilter();
        autoMapFiliter.addAction("AUTONAVI_STANDARD_BROADCAST_SEND");
        context.registerReceiver(new AutoMapBoardReceiver(), autoMapFiliter);
        IntentFilter bootstrapFiliter = new IntentFilter();
        bootstrapFiliter.addAction("android.intent.action.USER_PRESENT");
        bootstrapFiliter.addAction("android.intent.action.BOOT_COMPLETED");
        bootstrapFiliter.addAction("autochips.intent.action.QB_POWERON");
        bootstrapFiliter.addAction("xy.android.acc.on");
        bootstrapFiliter.addAction("com.nwd.action.ACTION_MCU_STATE_CHANGE");
        bootstrapFiliter.addAction("com.unisound.intent.action.DO_SLEEP");
        context.registerReceiver(new BootStartReceiver(), bootstrapFiliter);

        DateChangeReceiver receiver=new DateChangeReceiver();
        IntentFilter timeFilter=new IntentFilter(Intent.ACTION_TIME_TICK);
        timeFilter.addAction(Intent.ACTION_TIME_CHANGED);
        timeFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        context.getApplicationContext().registerReceiver(receiver,timeFilter);
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
                if(aMapTrackClient!=null){
                    aMapTrackClient.setTrackId(trackId);
                    aMapTrackClient.startGather(onTrackListener);
                }
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
            }
        }

        @Override
        public void onStartGatherCallback(int status, String msg) {
            if (status == ErrorCode.TrackListen.START_GATHER_SUCEE) {
                Toast.makeText(getApplicationContext(), "定位采集开启成功", Toast.LENGTH_SHORT).show();
                isGatherRunning = true;
            } else if (status == ErrorCode.TrackListen.START_GATHER_ALREADY_STARTED) {
                Toast.makeText(getApplicationContext(), "定位采集已经开启", Toast.LENGTH_SHORT).show();
                isGatherRunning = true;
            }
        }

        @Override
        public void onStopGatherCallback(int status, String msg) {
            if (status == ErrorCode.TrackListen.STOP_GATHER_SUCCE) {
                Toast.makeText(getApplicationContext(), "定位采集停止成功", Toast.LENGTH_SHORT).show();
                isGatherRunning = false;
            }
        }
    };
}
