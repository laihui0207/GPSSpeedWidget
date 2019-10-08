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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.amap.api.maps.offlinemap.OfflineMapActivity;
import com.huivip.gpsspeedwidget.BuildConfig;
import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.beans.PlayAudioEvent;
import com.huivip.gpsspeedwidget.beans.TTSEngineChangeEvent;
import com.huivip.gpsspeedwidget.detection.AppDetectionService;
import com.huivip.gpsspeedwidget.manager.Setup;
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
                EventBus.getDefault().post(new PlayAudioEvent("测试语音，欢迎使用本插件", true));
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
            case R.string.pref_key__overdraw_permission:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    openSettings(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, BuildConfig.APPLICATION_ID);
                }
                break;
            case R.string.pref_key__accessibility_permission:
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
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
        if(key.equalsIgnoreCase(getString(R.string.pref_key__speed_enable))) {
            if (AppSettings.get().isEnableSpeed()) {
                Utils.startFloatingWindows(getContext(), true);
            } else {
                Utils.startFloatingWindows(getContext(), false);
            }
        }
        if (key.equals(getString(R.string.pref_key__Road_line_enable))) {
            Intent roadLineService = new Intent(getContext(), RoadLineService.class);
            getContext().startService(roadLineService);
        }
        if(key.equalsIgnoreCase(getString(R.string.pref_key__Road_line_floating_window_enable))){
            Intent roadLineService = new Intent(getContext(), RoadLineFloatingService.class);
            getContext().startService(roadLineService);
        }
        if(key.equalsIgnoreCase(getString(R.string.pref_key__auto_start_wifi_hotpot))) {
            autoLaunchChanged(AppSettings.get().isEnableWifiHotpot());
        }

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
        Preference baiDuSpeaker=findPreference(getString(R.string.pref_key__Audio_engine_baidu_speaker));
        String audioEngine=AppSettings.get().getAudioEngine();
        if(SpeechFactory.BAIDUTTS.equalsIgnoreCase(audioEngine)){
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
                case "103":
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
                    break;
            }
        } /*else if(SpeechFactory.SIBICHITTS.equalsIgnoreCase(audioEngine)){
            pre_audio_engine.setSummary("思必驰语音");
            baiDuSpeaker.setVisible(false);
        }*/
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
