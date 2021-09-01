package com.huivip.gpsspeedwidget.fragment;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.amap.api.maps.offlinemap.OfflineMapActivity;
import com.huivip.gpsspeedwidget.BuildConfig;
import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.GpsUtil;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.beans.AutoMapStatusUpdateEvent;
import com.huivip.gpsspeedwidget.beans.FloatWindowsLaunchEvent;
import com.huivip.gpsspeedwidget.beans.PlayAudioEvent;
import com.huivip.gpsspeedwidget.beans.TTSEngineChangeEvent;
import com.huivip.gpsspeedwidget.detection.AppDetectionService;
import com.huivip.gpsspeedwidget.manager.Setup;
import com.huivip.gpsspeedwidget.service.AltitudeFloatingService;
import com.huivip.gpsspeedwidget.service.BootStartService;
import com.huivip.gpsspeedwidget.service.LyricFloatingService;
import com.huivip.gpsspeedwidget.service.RealTimeFloatingService;
import com.huivip.gpsspeedwidget.service.RoadLineFloatingService;
import com.huivip.gpsspeedwidget.service.RoadLineService;
import com.huivip.gpsspeedwidget.speech.AudioService;
import com.huivip.gpsspeedwidget.speech.SpeechFactory;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.util.Definitions;
import com.huivip.gpsspeedwidget.util.LauncherAction;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;
import com.huivip.gpsspeedwidget.utils.WifiUtils;
import com.nononsenseapps.filepicker.FilePickerActivity;

import net.gsantner.opoc.util.ContextUtils;
import net.gsantner.opoc.util.PermissionChecker;

import org.greenrobot.eventbus.EventBus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class SettingGPSWidgetFragment extends SettingsBaseFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        addPreferencesFromResource(R.xml.preferences_gps_widget);
    }
    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        int key = new ContextUtils(getContext()).getResId(ContextUtils.ResType.STRING, preference.getKey());
        switch (key) {
            case R.string.pref_key__auto_start_launch_select_other_apps:
                LauncherAction.RunAction(LauncherAction.Action.SelectApps, getContext());
                return true;
            case R.string.pref_key__lyric_path:
                if (new PermissionChecker(getActivity()).doIfExtStoragePermissionGranted()) {
                    Intent i = new Intent(getContext(), FilePickerActivity.class)
                            .putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true)
                            .putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
                    getActivity().startActivityForResult(i, Definitions.INTENT_LYRIC_PATH);
                }
                return true;
            case R.string.pref_key__Audio_test:
                EventBus.getDefault().post(new PlayAudioEvent("欢迎使用本插件，有钱的捧个钱场，没钱的也尽量捧个钱场！", true));
                break;
            case R.string.pref_key__Road_line_plugin_select:
                AppWidgetHost appWidgetHost = new AppWidgetHost(getContext(), Constant.APP_WIDGET_HOST_ID);
                int widgetId = appWidgetHost.allocateAppWidgetId();
                Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
                pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
                getActivity().startActivityForResult(pickIntent, Constant.SELECT_AMAP_PLUGIN_REQUEST_CODE);
                break;
            case R.string.pref_key__download_offline_map:
                startActivity(new Intent(getActivity(),
                        OfflineMapActivity.class));
                break;
            case R.string.pref_key__wifi_hotpot_setting:
                setWifiConfig();
                break;
            case R.string.pref_key__over_speed_tts_setting:
                setOverSpeedTTS();
                break;
            case R.string.pref_key__Tracker_self_server_url:
                setTrackerServerUrl();
                break;
            case R.string.pref_key__Altitude_alter_config:
                setAltitudeAlterConfig();
                break;
            case R.string.pref_key__overdraw_permission:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    openSettings(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, BuildConfig.APPLICATION_ID);
                }
                break;
            case R.string.pref_key__accessibility_permission:
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                break;
            case R.string.pref_key__Amap_widget_content:
                EventBus.getDefault().post(new AutoMapStatusUpdateEvent(true).setXunHangStarted(true));
                break;
        }
        return false;
    }
    private void openSettings(String settingsAction, String packageName) {
        Intent intent = new Intent(settingsAction);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("package:" + packageName));
        startActivity(intent);
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        super.onSharedPreferenceChanged(sharedPreferences, key);
        if(key.equalsIgnoreCase(getString(R.string.pref_key__auto_start_time_window))) {
            if (AppSettings.get().isEnableTimeWindow()) {
                if (!Utils.isServiceRunning(getContext(), RealTimeFloatingService.class.getName())) {
                    Intent timefloating = new Intent(getContext(), RealTimeFloatingService.class);
                    getContext().startService(timefloating);
                }
            } else {
                if (Utils.isServiceRunning(getContext(), RealTimeFloatingService.class.getName())) {
                    Intent timefloating = new Intent(getContext(), RealTimeFloatingService.class);
                    timefloating.putExtra(RealTimeFloatingService.EXTRA_CLOSE, true);
                    getContext().startService(timefloating);
                }
            }
        }
        if(key.equalsIgnoreCase(getString(R.string.pref_key__auto_start_altitude_window))) {
            if (AppSettings.get().isEnableAltitudeWindow()) {
                if (!Utils.isServiceRunning(getContext(), AltitudeFloatingService.class.getName())) {
                    Intent altitudeFloating = new Intent(getContext(), AltitudeFloatingService.class);
                    getContext().startService(altitudeFloating);
                }
            } else {
                if (Utils.isServiceRunning(getContext(), AltitudeFloatingService.class.getName())) {
                    Intent altitudeFloating = new Intent(getContext(), AltitudeFloatingService.class);
                    altitudeFloating.putExtra(AltitudeFloatingService.EXTRA_CLOSE, true);
                    getContext().startService(altitudeFloating);
                }
            }
        }
        if(key.equalsIgnoreCase(getString(R.string.pref_key__lyric_enable))) {
            if (AppSettings.get().isLyricEnable()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (!Utils.isNotificationEnabled(getContext())) {
                        Utils.openNotificationWindows(getContext());
                    }
                }
            } else {
                if (Utils.isServiceRunning(getContext(), LyricFloatingService.class.getName())) {
                    Intent lycFloatingService = new Intent(getContext(), LyricFloatingService.class);
                    lycFloatingService.putExtra(LyricFloatingService.EXTRA_CLOSE, true);
                    getContext().startService(lycFloatingService);
                }
            }
        }
        if(key.equalsIgnoreCase(getString(R.string.pref_key__Audio_play_alter_altitude))){
            GpsUtil gpsUtil=GpsUtil.getInstance(getContext());
            gpsUtil.setPlayAltitudeAlter(AppSettings.get().isPlayAltitudeAlter());
        }
        if(key.equalsIgnoreCase(getString(R.string.pref_key__auto_start))){
            if(AppSettings.get().getAutoStart() && !Utils.isServiceRunning(getContext(),BootStartService.class.getName())){
                Intent bootService=new Intent(getContext(), BootStartService.class);
                bootService.putExtra(BootStartService.START_BOOT,true);
                Utils.startService(getContext(),bootService, true);
            }
        }
        if(key.equalsIgnoreCase(getString(R.string.pref_key__speed_enable))) {
            if (AppSettings.get().isEnableSpeed()) {
                //Utils.startFloatingWindows(getContext(), true);
                EventBus.getDefault().post(new FloatWindowsLaunchEvent(true));
            } else {
                //Utils.startFloatingWindows(getContext(), false);
                EventBus.getDefault().post(new FloatWindowsLaunchEvent(false));

            }
        }
        if (key.equals(getString(R.string.pref_key__Road_line_enable))) {
            Intent roadLineService = new Intent(getContext(), RoadLineService.class);
            getContext().startService(roadLineService);
        }
        if(key.equalsIgnoreCase(getString(R.string.pref_key__Road_line_floating_window_enable))){
            Intent roadLineService = new Intent(getContext(), RoadLineFloatingService.class);
            Log.d("huivip","Road line floating:"+AppSettings.get().isEnableRoadLineFloatingWindow());
            roadLineService.putExtra(RoadLineFloatingService.EXTRA_CLOSE,AppSettings.get().isEnableRoadLineFloatingWindow());
            getContext().startService(roadLineService);
        }
        if(key.equalsIgnoreCase(getString(R.string.pref_key__auto_start_wifi_hotpot))) {
            autoLaunchChanged(AppSettings.get().isEnableWifiHotpot());
        }
        AppSettings.get().setAppRestartRequired(false);

    }

    @Override
    public void updateSummaries() {
        Preference overLayPermission=findPreference(getString(R.string.pref_key__overdraw_permission));
        boolean overlayEnabled = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(getContext());
        if(overlayEnabled){
            overLayPermission.setSummary("权限已附");
           // overLayPermission.setEnabled(false);
        }
        Preference accessibilityPermission=findPreference(getString(R.string.pref_key__accessibility_permission));
        boolean serviceEnabled = Utils.isAccessibilityServiceEnabled(getContext(), AppDetectionService.class);
        if(serviceEnabled){
            accessibilityPermission.setSummary("权限已附");
           // accessibilityPermission.setEnabled(false);
        }
        Preference pre_delay_time=findPreference(getString(R.string.pref_key__auto_start_launch_other_app_delay_time));
        Preference pre_selectApps=findPreference(getString(R.string.pref_key__auto_start_launch_select_other_apps));
        pre_delay_time.setSummary("延时:"+ AppSettings.get().getDelayTimeBetweenLaunchOtherApp()+"秒");
        String selectApps = PrefUtils.getAutoLaunchAppsName(Setup.appContext());
        if(!TextUtils.isEmpty(selectApps)){
            pre_selectApps.setSummary(selectApps);
        }
        Preference pre_audio_engine=findPreference(getString(R.string.pref_key__Audio_engine));
        //Preference baiDuSpeaker=findPreference(getString(R.string.pref_key__Audio_engine_baidu_speaker));
        String audioEngine=AppSettings.get().getAudioEngine();
      /*  if(SpeechFactory.BAIDUTTS.equalsIgnoreCase(audioEngine)){
            pre_audio_engine.setSummary("百度语音");
            baiDuSpeaker.setVisible(true);
            String speaker=AppSettings.get().getAudioBaiDuSpeaker();
            switch(speaker) {
                case "0":
                  baiDuSpeaker.setSummary("度小美（标准女声）");
                  break;
                case "1":
                    baiDuSpeaker.setSummary("度小宇（标准男声）");
                    break;
                case "3":
                    baiDuSpeaker.setSummary("度逍遥（情感男声）");
                    break;
                case "4":
                    baiDuSpeaker.setSummary("度丫丫（情感儿童声）");
                    break;
                case "5":
                    baiDuSpeaker.setSummary("度小娇（情感女声）");
                    break;
               *//* case "103":
                    baiDuSpeaker.setSummary("度米朵（情感儿童声）");
                    break;
                case "106":
                    baiDuSpeaker.setSummary("度博文（情感男声");
                    break;
                case "110":
                    baiDuSpeaker.setSummary("度小童（情感儿童声");
                    break;
                case "111":
                    baiDuSpeaker.setSummary("度小萌（情感女声");
                    break;*//*
            }
        } else if(SpeechFactory.SIBICHITTS.equalsIgnoreCase(audioEngine)){
            pre_audio_engine.setSummary("思必驰语音");
            baiDuSpeaker.setVisible(false);
        } else*/ if(SpeechFactory.SDKTTS.equalsIgnoreCase(audioEngine)){
            pre_audio_engine.setSummary("高德内置语音");
            //baiDuSpeaker.setVisible(false);
        } else if (SpeechFactory.TEXTTTS.equalsIgnoreCase(audioEngine)){
            pre_audio_engine.setSummary("系统内置TTS");
            //baiDuSpeaker.setVisible(false);
        }
        Preference audioStream=findPreference(getString(R.string.pref_key__Audio_stream_type));
        audioStream.setSummary("音频通道："+AppSettings.get().getAudioStreamType());
        Preference audioPlayType=findPreference(getString(R.string.pref_key__Audio_play_type));
        audioPlayType.setSummary(AppSettings.get().getAudioPlayType()==1 ? "语音引擎管理播放" : "插件管理播放");
        EventBus.getDefault().post(new TTSEngineChangeEvent());
        Preference pre_speedFlatting_Stylee=findPreference(getString(R.string.pref_key__speed_style));
        Preference default_speed_show=findPreference(getString(R.string.pref_key__speed_default_speed_show));
        Preference default_limit_show=findPreference(getString(R.string.pref_key__speed_default_limit_show));
        Preference default_horizontal_show=findPreference(getString(R.string.pref_key__speed_default_horizontal_show));
        Preference aMap_limit_show=findPreference(getString(R.string.pref_key__speed_AutoMap_limit_show));
        String speedFlattingStyle=AppSettings.get().getSpeedFlattingStyle();
        switch (speedFlattingStyle){
            case "0":
                pre_speedFlatting_Stylee.setSummary("双圈样式");
                default_horizontal_show.setVisible(true);
                default_speed_show.setVisible(true);
                default_limit_show.setVisible(true);
                aMap_limit_show.setVisible(false);
                break;
            case "1":
                pre_speedFlatting_Stylee.setSummary("高德样式");
                default_horizontal_show.setVisible(false);
                default_speed_show.setVisible(false);
                default_limit_show.setVisible(false);
                aMap_limit_show.setVisible(true);
                break;
            case "2":
                pre_speedFlatting_Stylee.setSummary("仪表样式");
                default_horizontal_show.setVisible(false);
                default_speed_show.setVisible(false);
                default_limit_show.setVisible(false);
                aMap_limit_show.setVisible(false);
                break;
        }
        if(AppSettings.get().isEnableAudio() && !Utils.isServiceRunning(getContext(), AudioService.class.getName())){
            Intent audioService=new Intent(getContext(),AudioService.class);
            getContext().startService(audioService);
        }
        Preference roadLineFontSize=findPreference(getString(R.string.pref_key__speed_road_line_font_size));
        roadLineFontSize.setSummary("字体大小:"+AppSettings.get().getRoadLineRoadNameSize());
        Preference dateTimeFormat=findPreference(getString(R.string.pref_key__auto_start_time_window_dateFormat));
        Preference dateTimeFontColor=findPreference(getString(R.string.pref_key__time_window_font_color));
        if(AppSettings.get().isEnableTimeWindow()) {
            dateTimeFontColor.setVisible(true);
            //dateTimeFormat.setVisible(true);
            DateFormat sdf = new SimpleDateFormat(AppSettings.get().getTimeWindowDateFormat());
            dateTimeFormat.setSummary(sdf.format(new Date()));
        } else {
            dateTimeFontColor.setVisible(false);
            //dateTimeFormat.setVisible(false);
        }
        Preference aMapPlugin=findPreference(getString(R.string.pref_key__Road_line_plugin_select));
        if(AppSettings.get().getAmapPluginId()!=-1){
            aMapPlugin.setSummary("已选择");
        } else {
            aMapPlugin.setSummary("请选择高德小部件（4*3）");
        }
        Preference setWifi=findPreference(getString(R.string.pref_key__wifi_hotpot_setting));
        if(AppSettings.get().isEnableWifiHotpot()){
            setWifi.setVisible(true);
        } else {
            setWifi.setVisible(false);
        }
        Preference floatLyricFontSize=findPreference(getString(R.string.pref_key__lyric_music_font_size));
        floatLyricFontSize.setSummary("字体调整:"+AppSettings.get().getMusicLyricFontSize());
        Preference floatAltitudeFontSize=findPreference(getString(R.string.pref_key__altitude_font_size));
        floatAltitudeFontSize.setSummary("字体调整:"+AppSettings.get().getAltitudeFontSize());
      /*  Preference overSpeedTTS=findPreference(getString(R.string.pref_key__over_speed_tts_setting));
        overSpeedTTS.setSummary("当前语音："+PrefUtils.getPrefOverSpeedTts(getContext()));*/
        Preference alterAltitudeConfig=findPreference(getString(R.string.pref_key__Altitude_alter_config));
        alterAltitudeConfig.setSummary("播报起始高度:"+PrefUtils.getAltitudeAlterStart(getContext())+"米,频率每"+PrefUtils.getAltitudeAlterFrequency(getContext())+"米播报一次");
        super.updateSummaries();
    }
    private void setWifiConfig(){
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View promptView = layoutInflater.inflate(R.layout.dialog_wifi_setting, null);
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(promptView);
        final EditText nameEditText = (EditText) promptView.findViewById(R.id.input_wifiName);
        nameEditText.setText(PrefUtils.getAutoLauchHotSpotName(getContext()));
        final EditText passwordEditText = (EditText) promptView.findViewById(R.id.input_wifiPassword);
        passwordEditText.setText(PrefUtils.getAutoLauchHotSpotPassword(getContext()));
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String wifiName = nameEditText.getText().toString();
                        String wifiPassword = passwordEditText.getText().toString();
                        if(wifiPassword.length()<8){
                            Toast.makeText(getContext(),"密码最低要求8位以上",Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (TextUtils.isEmpty(wifiName) || TextUtils.isEmpty(wifiPassword)) {
                            wifiName = Constant.WIFI_USERNAME;
                            wifiPassword = Constant.WIFI_PASSWORD;
                        }
                        PrefUtils.setAutoLaunchHotSpotName(getContext(),wifiName);
                        PrefUtils.setAutoLaunchHotSpotPassword(getContext(),wifiPassword);
                        //autoLaunchChanged(buttonView);
                        dialog.cancel();
                    }
                })
                .setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        final android.app.AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
    private void setAltitudeAlterConfig(){
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View promptView = layoutInflater.inflate(R.layout.dialog_altitude_alter_setting, null);
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(promptView);
        final EditText alterStartEditText = (EditText) promptView.findViewById(R.id.input_altitude_start);
        alterStartEditText.setText(PrefUtils.getAltitudeAlterStart(getContext()));
        final EditText alterSequenceEditText = (EditText) promptView.findViewById(R.id.input_altitude_sequence);
        alterSequenceEditText.setText(PrefUtils.getAltitudeAlterFrequency(getContext()));
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String start = alterStartEditText.getText().toString();
                        String frequency = alterSequenceEditText.getText().toString();

                        if (TextUtils.isEmpty(start) || TextUtils.isEmpty(frequency)) {
                            start = Constant.ALTITUDE_ALTER_START;
                            frequency = Constant.ALTITUDE_ALTER_FREQUENCY;
                        }
                        PrefUtils.setAltitudeAlterStart(getContext(),start);
                        PrefUtils.setAltitudeAlterFrequency(getContext(),frequency);
                        GpsUtil gpsUtil=GpsUtil.getInstance(getContext());
                        gpsUtil.setAltitudeAlterStart(Integer.parseInt(start));
                        gpsUtil.setAltitudeAlterFrequency(Integer.parseInt(frequency));
                        //autoLaunchChanged(buttonView);
                        dialog.cancel();
                    }
                })
                .setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        final android.app.AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
    private void setOverSpeedTTS(){
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View promptView = layoutInflater.inflate(R.layout.dialog_overspeed_tts_setting, null);
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(promptView);
        final EditText ttsValueEditText = (EditText) promptView.findViewById(R.id.input_overSpeedTTSName);
        ttsValueEditText.setText(PrefUtils.getPrefOverSpeedTts(getContext()));
        final EditText launcherAlterEditText = (EditText)promptView.findViewById(R.id.input_launchAlterTTSName);
        launcherAlterEditText.setText(PrefUtils.getPrefLaunchAlterTts(getContext()));
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String ttsValue = ttsValueEditText.getText().toString();
                        if (TextUtils.isEmpty(ttsValue)) {
                            ttsValue = Constant.OVER_SPEED_TTS;
                        }
                        PrefUtils.setPrefOverSpeedTts(getContext(),ttsValue);
                        String launchAlterTTSValue = launcherAlterEditText.getText().toString();
                        if (TextUtils.isEmpty(launchAlterTTSValue)) {
                            launchAlterTTSValue = Constant.LAUNCH_ALTER_TTS;
                        }
                        PrefUtils.setPreLaunchAlterTts(getContext(),launchAlterTTSValue);
                        dialog.cancel();
                    }
                })
                .setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        final android.app.AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
    private void setTrackerServerUrl(){
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View promptView = layoutInflater.inflate(R.layout.dialog_server_url_setting, null);
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(promptView);
        final EditText nameEditText = (EditText) promptView.findViewById(R.id.input_serverUrl);
        nameEditText.setText(PrefUtils.getGPSRemoteUrl(getContext()));
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String serverUrl= nameEditText.getText().toString();
                        if (TextUtils.isEmpty(serverUrl)) {
                            serverUrl = PrefUtils.getGPSRemoteUrl(getContext());
                        }
                        PrefUtils.setGpsRemoteUrl(getContext(),serverUrl);
                        dialog.cancel();
                    }
                })
                .setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        final android.app.AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
    private void autoLaunchChanged(boolean enable) {
        String wifiName=PrefUtils.getAutoLauchHotSpotName(getContext());
        String wifiPassword=PrefUtils.getAutoLauchHotSpotPassword(getContext());
        if (enable) {
            boolean enabled = WifiUtils.switchWifiHotspot(getContext(), wifiName, wifiPassword, true);
            if (enabled) {
                Toast.makeText(getContext(), "移动热点已启动:" + wifiName+ ",密码:" + wifiPassword, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "移动热点启动失败！", Toast.LENGTH_SHORT).show();
            }
        } else {
            boolean enabled = WifiUtils.switchWifiHotspot(getContext(), wifiName, wifiPassword, false);
            if (enabled) {
                Toast.makeText(getContext(), "移动热点已关闭！", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "移动热点关闭失败！", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
