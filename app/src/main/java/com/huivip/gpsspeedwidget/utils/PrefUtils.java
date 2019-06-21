package com.huivip.gpsspeedwidget.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.DeviceUuidFactory;
import com.huivip.gpsspeedwidget.detection.AppDetectionService;
import com.huivip.gpsspeedwidget.speech.SpeechFactory;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public abstract class PrefUtils {

    private static final String PREF_METRIC = "pref_metric";
    private static final String PREF_FLOATING_LOCATION = "pref_floating_location";
    private static final String PREF_NAVI_FLOATING_LOCATION= "pref_navi_floating_location";
  /*  private static final String PREF_DRIVE_FLOATING_LOCATION= "pref_driveWay_floating_location";
    private static final String PREF_TIME_FLOATING_LOCATION= "pref_time_floating_location";
    private static final String PREF_TEXT_FLOATING_LOCATION= "pref_text_floating_location";*/
    private static final String PREF_OPACITY = "pref_opacity";
    private static final String PREF_SPEEDOMETER = "pref_speedometer";
    private static final String PREF_LIMITS = "pref_limits";
    private static final String PREF_SHOW_SMALL_FLOATING= "pref_small_floating_style";
    private static final String PREF_HIDE_FLOATING_WHEN_STOP= "pref_hide_floating_window_when_stop";
    private static final String PREF_APPS = "pref_apps";
    private static final String PREF_DEFAULT_LAUNCHE_APP= "pref_Default_launch_apps";
    private static final String AUTO_FLOATTING_STYLE = "com.huivip.Floating.style";
    private static final String PREF_DELAY_AUTO_START = "com.huivip.delay.started";
/*    private static final String PREF_DELAY_AUTO_START_INDEX= "com.huivip.delay.started.index";*/
    private static final String PREF_DELAY_AUTO_START_GOHOME= "com.huivip.delay.started.goToHome";
    private static final String PREF_AUTO_LAUNCH_APPS = "pref_Auto_apps";
    private static final String PREF_AUTO_LAUNCH_APPS_NAME = "pref_Auto_apps_Names";
/*    private static final String PREF_TERMS_ACCEPTED = "pref_terms_accepted";*/
    public static final String AUTO_START_PREFS_NAME="AutoStart";
    public static final String PREF_AUTO_LAUNCH_WIFI_HOTSPOT="AutoStart.Wifi.hotspot";
    public static final String PREF_AUTO_LAUNCH_WIFI_HOTSPOT_NAME="AutoStart.Wifi.hotspot.name";
    public static final String PREF_AUTO_LAUNCH_WIFI_HOTSPOT_PASSWORD="AutoStart.Wifi.hotspot.password";
    public static final String RECORD_GPS_HISTORY_PREFS_NAME="recordGpsHistory";
    public static final String GPS_SPEED_TYPE_MPH_PREFS_NAME="com.huivip.use.mph";
    public static final String UPLOAD_GPS_HISTORY_PREFS_NAME="uploadGpsHistory";
    public static final String UPLOAD_GPS_HISTORY_NAVI_PREFS_NAME="uploadNAVIGpsHistory";
    public static final String AUTO_CLEAN_GPS_HISTORY_PREFS_NAME="AutoCleanGpsHistory";
    public static final String ENABLE_FLATING_WINDOW="com.huivip.Enable.fating.History";
    public static final String ENABLED_LYRIC_PREFS_NAME="com.huivip.Enable.fating.Lyric";
    public static final String ENABLE_AUDIO_SERVICE="com.huivip.enable.AudioService";
    public static final String ENABLE_TIME_FLOATING_WINDOW="com.huivip.enable.Time.FloatingWindow";
    public static final String ENABLE_AUTO_WIDGET_FLOATING_WINDOW="com.huivip.enable.auto.widget.FloatingWindow";
    public static final String ENABLE_AUTO_WIDGET_FLOATING_WINDOW_ONLY_TURN ="com.huivip.enable.xunhang.widget.FloatingWindow.onlyTurn";
    public static final String ENABLE_HIDE_FLOATING_WINDOW_ON_NAVI="com.huivip.enable.hide.FloatingWindow.OnNaviApp";
    public static final String ENABLE_AUTO_GOHOME_AFTER_NAVI_STARTED="com.huivip.enable.auto.goHome.afterNavi";
    public static final String ENABLE_AUDIO_MIX="com.huivip.enable.AudioMix";
    public static final String ENABLE_AUDIO_VOLUME_DEPRESS="com.huivip.enable.Audio.volume.depress";
    public static final String ENABLE_TEMP_AUDIO_SERVICE="com.huivip.enable.temp.AudioService";
    public static final String ENABLE_AUTONAVI_SERVICE="com.huivip.enable.autoNavi.Service";
    public static final String WIDGET_ACTIVED="com.huivip.widget.actived";
    public static final String GPS_REMOTE_URL="com.huivip.widget.remoteUrl";
    public static final String SHOW_FLATTING_ON="com.huivip.widget.showFlatingOn";
    public static final String FLOATTING_WINDOWS_AUTO_SOLT="com.huivip.wdiget.floatting.autoslot";
    public static final String NAVI_FLOATTING_WINDOWS_AUTO_SOLT="com.huivip.wdiget.floatting.navi.autoslot";
    public static final String FLOATING_WINDWS_DIRECTION_horizontal="com.huivip.widget.Direction";
    public static final String PREF_GPS_SPEED_ADJUST="com.huivip.widget.speed.adjust";
    public static final String FLOATTING_WINDOW_XY="com.huivip.widget.xy";
    public static final String DRIVE_WAY_FLOATTING_WINDOW_XY="com.huivip.widget.xy";
    public static final String NAVI_FLOATTING_WINDOW_XY="com.huivip.widget.navi.xy";
    public static final String TIME_FLOATTING_WINDOW_XY="com.huivip.widget.Time.xy";
    public static final String MAP_FLOATTING_WINDOW_XY="com.huivip.widget.Map.xy";
    public static final String TEXT_FLOATTING_WINDOW_XY="com.huivip.widget.Text.xy";
    public static final String LYRC_FLOATTING_WINDOW_XY="com.huivip.widget.lyrc.xy";
    public static final String ROADLINE_FLOATTING_WINDOW_XY="com.huivip.widget.roadLine.xy";
    public static final String SEPARATED_VOLUME ="com.huivip.widget.separated.volume";
    public static final String AUDIO_VOLUME="com.huivipo.widget.audio.volume";
    public static final String CACHE_AUDIO_FILE="com.huivipo.widget.audio.cache.file";
    public static final String AUDIO_AUTO_MUTE="com.huivipo.widget.audio.auto.mute";
    public static final String USER_CLOSED_SERVER="com.huivip.widget.Close.serviced";
    public static final String ENABLE_ACCESSIBILITY_SEVICE="com.huivip.widget.cant.enalble.Accessibility.serviced";
    public static final String ENABLE_WATCH_WIDGET="com.huivip.wdiget.watch.enabled";
    public static final String ENABLE_NUMBER_WIDGET="com.huivip.wdiget.number.enabled";
    public static final String DEVICEID_STORAGE="com.huivip.deviceId.String";
    public static final String CURRENT_DEVICEID="com.huivip.deviceId";
    public static final String ACTIVITY_ON_DESKTOP="com.huivip.widget.onDesktop";
    public static final String APP_FIRST_RUN="com.huivip.widget.firstRun";
    public static final String SPEED_SHOW_ADDRESS_WHEN_STOP="com.huivip.widget.ShowAddress";
    public static final String SPEED_SHOW_NOTIFICATION="com.huivip.widget.ShowNotification";
    public static final String SHOW_ALL="0";
    public static final String SHOW_ONLY_DESKTOP="2";
    public static final String SHOW_NO_DESKTOP="1";
    public static final String SHOW_ONLY_AUTONAVI="3";
    public static final String TTS_ENGINE="com.huivip.TTS.Type";
    private static final String APP_PLAY_TIME="com.huivip.play.time";
    private static final String APP_PLAY_WEATHER="com.huivip.play.weather";
    private static final String APP_PLAY_WARN="com.huivip.play.warn";
    private static final String NOTIFY_ROAD_LIMIT="com.huivip.Limit.Notify";
    private static final String SELECT_AMAP_PLUGIN_ID="com.huivip.select.amap.plugin";

    public static final String FLOATING_DEFAULT="0";
    public static final String FLOATING_METER="2";
    public static final String FLOATING_AUTONAVI="1";
    public static final String FTP_URL="com.huivip.ftp.url";
    public static final String FTP_PORT="com.huivip.ftp.port";
    public static final String FTP_USER="com.huivip.ftp.user";
    public static final String FTP_PASSWORD="com.huivip.ftp.password";
    public static final String FTP_PATH="com.huivip.ftp.path";
    public static final String FTP_AUTO_BACKUP="com.huivip.ftp.auto.backup";
    static final String ENABLE_NAVI_FLOATING_WINDOWS="com.huivip.navi.floating";
    static final String NAVI_MODE_NEW_DRIVER="com.huivip.navi.mode.newDriver";
    static final String NAVI_FLOATTING_FIXED_POSITION="com.huivip.navi.fixed.position";
    static final String SPEED_FLOATTING_FIXED_POSITION="com.huivip.speed.fixed.position";
    static final String LYRIC_FLOATTING_FIXED_POSITION="com.huivip.lyric.fixed.position";
    static final String ROADLINE_FLOATTING_FIXED_POSITION="com.huivip.roaldLine.fixed.position";
    static final String ROADLINE_FLOATTING="com.huivip.roaldLine.enabled";
    static final String ROADLINE_SPEED="com.huivip.roaldLine.speed.enabled";

    private static SharedPreferences.Editor edit(Context context) {
        return getSharedPreferences(context).edit();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String getGPSRemoteUrl(Context context){
        String url=getSharedPreferences(context).getString(GPS_REMOTE_URL,Constant.LBSURL);
        if(!url.startsWith("http")){
            url="http://"+url;
        }
        return url;
    }
    public static void setGpsRemoteUrl(Context context,String url){
        edit(context).putString(GPS_REMOTE_URL,url).apply();
    }
    public static void setDeviceIDStorage(Context context,String value){
        edit(context).putString(DEVICEID_STORAGE,value).apply();
    }
    public static String getDeviceIdStorage(Context context){
        return getSharedPreferences(context).getString(DEVICEID_STORAGE,"");
    }
    public static int getSelectAMAPPLUGIN(Context context){
        return getSharedPreferences(context).getInt(SELECT_AMAP_PLUGIN_ID,-1);
    }
    public static void setSelectAmapPluginId(Context context,int selectAMAPPlugin){
        edit(context).putInt(SELECT_AMAP_PLUGIN_ID,selectAMAPPlugin).apply();
    }
    public static String getShortDeviceId(Context context){
        String deviceId =(new DeviceUuidFactory(context)).getDeviceId();
        setDeviceIDString(context,deviceId);
        return deviceId.substring(0,deviceId.indexOf("-"));
    }
    public static void setDeviceIDString(Context context,String value){
        edit(context).putString(CURRENT_DEVICEID,value).apply();
    }
    public static String getDeviceIdString(Context context){
        return getSharedPreferences(context).getString(CURRENT_DEVICEID,"");
    }
    public static void setEnableAutoStart(Context context,boolean autoStart){
        edit(context).putBoolean(AUTO_START_PREFS_NAME, autoStart).apply();
    }
    public static boolean isEnableAutoStart(Context context){
        return getSharedPreferences(context).getBoolean(AUTO_START_PREFS_NAME, true);
    }
    public static void setTTSEngineType(Context context,String style){
        edit(context).putString(TTS_ENGINE, style).apply();
    }
    public static String getTtsEngine(Context context){
        return getSharedPreferences(context).getString(TTS_ENGINE, SpeechFactory.BAIDUTTS);
    }
    public static String getAmapWebKey(Context context){
        String deviceId=getShortDeviceId(context);
        String lastChar=deviceId.substring(deviceId.length()-1);
        if(lastChar.matches("\\d+(?:\\.\\d+)?")){
            return Constant.AUTONAVI_WEB_KEY;
        }
        return Constant.AUTONAVI_WEB_KEY2;
    }
    public static String getAmapTrackServiceID(Context context){
        String deviceId=getShortDeviceId(context);
        String lastChar=deviceId.substring(deviceId.length()-1);
        if(lastChar.matches("\\d+(?:\\.\\d+)?")){
            return Constant.AUTONAVI_WEB_KEY_TRACK_SERVICE_ID;
        }
        return Constant.AUTONAVI_WEB_KEY2_TRACK_SERVICE_ID;
    }
    public static String getAmapTrackServiceID(String deviceId){
        //String deviceId=getShortDeviceId(context);
        String lastChar=deviceId.substring(deviceId.length()-1);
        if(lastChar.matches("\\d+(?:\\.\\d+)?")){
            return Constant.AUTONAVI_WEB_KEY_TRACK_SERVICE_ID;
        }
        return Constant.AUTONAVI_WEB_KEY2_TRACK_SERVICE_ID;
    }
    public static void setFloattingStyle(Context context,String style){
        edit(context).putString(AUTO_FLOATTING_STYLE, style).apply();
    }
    public static String getFloatingStyle(Context context){
        return getSharedPreferences(context).getString(AUTO_FLOATTING_STYLE, FLOATING_DEFAULT);
    }
    public static void setShowFlattingOn(Context context,String show){
        edit(context).putString(SHOW_FLATTING_ON, show).apply();
    }
    public static String getShowFlatingOn(Context context){
        return getSharedPreferences(context).getString(SHOW_FLATTING_ON, SHOW_ALL);
    }
    public static void setRecordGPSHistory(Context context,boolean recordHistory){
        edit(context).putBoolean(RECORD_GPS_HISTORY_PREFS_NAME, recordHistory).apply();
    }
    public static boolean isEnableRecordGPSHistory(Context context){
        return getSharedPreferences(context).getBoolean(RECORD_GPS_HISTORY_PREFS_NAME, false);
    }
    public static void setEnableGPSUseMPH(Context context,boolean recordHistory){
        edit(context).putBoolean(GPS_SPEED_TYPE_MPH_PREFS_NAME, recordHistory).apply();
    }
    public static boolean isEnableGPSUseMPH(Context context){
        return getSharedPreferences(context).getBoolean(GPS_SPEED_TYPE_MPH_PREFS_NAME, false);
    }
    public static void setEnableTimeFloatingWidow(Context context, boolean value){
        edit(context).putBoolean(ENABLE_TIME_FLOATING_WINDOW, value).apply();
    }
    public static boolean isEnableTimeFloatingWidow(Context context){
        return getSharedPreferences(context).getBoolean(ENABLE_TIME_FLOATING_WINDOW, false);
    }
    public static void setEnableAutoWidgetFloatingWidow(Context context, boolean value){
        edit(context).putBoolean(ENABLE_AUTO_WIDGET_FLOATING_WINDOW, value).apply();
    }
    public static boolean isEnableAutoWidgetFloatingWidow(Context context){
        return getSharedPreferences(context).getBoolean(ENABLE_AUTO_WIDGET_FLOATING_WINDOW, false);
    }
    public static void setEnableAutoWidgetFloatingWidowOnlyTurn(Context context, boolean value){
        edit(context).putBoolean(ENABLE_AUTO_WIDGET_FLOATING_WINDOW_ONLY_TURN, value).apply();
    }
    public static boolean isEnableAutoWidgetFloatingWidowOnlyTurn(Context context){
        return getSharedPreferences(context).getBoolean(ENABLE_AUTO_WIDGET_FLOATING_WINDOW_ONLY_TURN, false);
    }
    public static void setEnableAutoMute(Context context, boolean value){
        edit(context).putBoolean(AUDIO_AUTO_MUTE, value).apply();
    }
    public static boolean isEnableAutoMute(Context context){
        return getSharedPreferences(context).getBoolean(AUDIO_AUTO_MUTE, true);
    }
    public static void setEnableCacheAudioFile(Context context, boolean value){
        edit(context).putBoolean(CACHE_AUDIO_FILE, value).apply();
    }
    public static boolean isEnableCacheAudioFile(Context context){
        return getSharedPreferences(context).getBoolean(CACHE_AUDIO_FILE, true);
    }
    public static void setHideFloatingWidowOnNaviApp(Context context, boolean value){
        edit(context).putBoolean(ENABLE_HIDE_FLOATING_WINDOW_ON_NAVI, value).apply();
    }
    public static boolean isHideFloatingWidowOnNaviApp(Context context){
        return getSharedPreferences(context).getBoolean(ENABLE_HIDE_FLOATING_WINDOW_ON_NAVI, true);
    }
    public static void setEnableAutoGoHomeAfterNaviStarted(Context context, boolean value){
        edit(context).putBoolean(ENABLE_AUTO_GOHOME_AFTER_NAVI_STARTED, value).apply();
    }
    public static boolean isEnableAutoGoHomeAfterNaviStarted(Context context){
        return getSharedPreferences(context).getBoolean(ENABLE_AUTO_GOHOME_AFTER_NAVI_STARTED, false);
    }
    public static void setPlayTime(Context context,boolean value){
        edit(context).putBoolean(APP_PLAY_TIME, value).apply();
    }
    public static boolean isPlayTime(Context context){
        return getSharedPreferences(context).getBoolean(APP_PLAY_TIME, true);
    }
    public static void setPlayWeather(Context context,boolean value){
        edit(context).putBoolean(APP_PLAY_WEATHER, value).apply();
    }
    public static boolean isPlayWeather(Context context){
        return getSharedPreferences(context).getBoolean(APP_PLAY_WEATHER, true);
    }
    public static void setPlayWarn(Context context,boolean value){
        edit(context).putBoolean(APP_PLAY_WARN, value).apply();
    }
    public static boolean isPlayWarn(Context context){
        return getSharedPreferences(context).getBoolean(APP_PLAY_WARN, false);
    }
    public static void setAppFirstRun(Context context,boolean firstRun){
        edit(context).putBoolean(APP_FIRST_RUN, firstRun).apply();
    }
    public static boolean isAppFirstRun(Context context){
        return getSharedPreferences(context).getBoolean(APP_FIRST_RUN, true);
    }
    public static void setRoadLimitNotify(Context context,boolean value){
        edit(context).putBoolean(NOTIFY_ROAD_LIMIT, value).apply();
    }
    public static boolean isRoadLimitNotify(Context context){
        return getSharedPreferences(context).getBoolean(NOTIFY_ROAD_LIMIT, false);
    }
    public static void setUploadGPSHistory(Context context,boolean uploadHistory){
        edit(context).putBoolean(UPLOAD_GPS_HISTORY_PREFS_NAME, uploadHistory).apply();
    }
    public static boolean isEnableUploadGPSHistory(Context context){
        return getSharedPreferences(context).getBoolean(UPLOAD_GPS_HISTORY_PREFS_NAME, false);
    }
    public static void setUploadNAVIGPSHistory(Context context,boolean uploadHistory){
        edit(context).putBoolean(UPLOAD_GPS_HISTORY_NAVI_PREFS_NAME, uploadHistory).apply();
    }
    public static boolean isEnableNAVIUploadGPSHistory(Context context){
        return getSharedPreferences(context).getBoolean(UPLOAD_GPS_HISTORY_NAVI_PREFS_NAME, false);
    }
    public static void setAutoCleanGPSHistory(Context context,boolean cleanHistory){
        edit(context).putBoolean(AUTO_CLEAN_GPS_HISTORY_PREFS_NAME, cleanHistory).apply();
    }
    public static boolean isEnableAutoCleanGPSHistory(Context context){
        return getSharedPreferences(context).getBoolean(AUTO_CLEAN_GPS_HISTORY_PREFS_NAME, true);
    }
    public static void setLyricEnabled(Context context,boolean cleanHistory){
        edit(context).putBoolean(ENABLED_LYRIC_PREFS_NAME, cleanHistory).apply();
    }
    public static boolean isLyricEnabled(Context context){
        return getSharedPreferences(context).getBoolean(ENABLED_LYRIC_PREFS_NAME, false);
    }
    public static boolean isFloattingAutoSolt(Context context){
        return getSharedPreferences(context).getBoolean(FLOATTING_WINDOWS_AUTO_SOLT, true);
    }
    public static void setFloattingWindowsAutoSolt(Context context,boolean autoSolt){
        edit(context).putBoolean(FLOATTING_WINDOWS_AUTO_SOLT, autoSolt).apply();
    }
    public static boolean isNaviFloattingAutoSolt(Context context){
        return getSharedPreferences(context).getBoolean(NAVI_FLOATTING_WINDOWS_AUTO_SOLT, true);
    }
    public static void setNaviFloattingWindowsAutoSolt(Context context,boolean autoSolt){
        edit(context).putBoolean(NAVI_FLOATTING_WINDOWS_AUTO_SOLT, autoSolt).apply();
    }
    public static boolean isOnDesktop(Context context){
        return getSharedPreferences(context).getBoolean(ACTIVITY_ON_DESKTOP, true);
    }
    public static void setOnDesktop(Context context,boolean onDesktop){
        edit(context).putBoolean(ACTIVITY_ON_DESKTOP, onDesktop).apply();
    }
    public static boolean isAutoLauchHotSpot(Context context){
        return getSharedPreferences(context).getBoolean(PREF_AUTO_LAUNCH_WIFI_HOTSPOT, false);
    }
    public static void setAutoLaunchHotSpot(Context context,boolean enable){
        edit(context).putBoolean(PREF_AUTO_LAUNCH_WIFI_HOTSPOT, enable).apply();
    }
    public static String getAutoLauchHotSpotName(Context context){
        return getSharedPreferences(context).getString(PREF_AUTO_LAUNCH_WIFI_HOTSPOT_NAME, Constant.WIFI_USERNAME);
    }
    public static void setAutoLaunchHotSpotName(Context context,String name){
        edit(context).putString(PREF_AUTO_LAUNCH_WIFI_HOTSPOT_NAME, name).apply();
    }
    public static String getAutoLauchHotSpotPassword(Context context){
        return getSharedPreferences(context).getString(PREF_AUTO_LAUNCH_WIFI_HOTSPOT_PASSWORD, Constant.WIFI_PASSWORD);
    }
    public static void setAutoLaunchHotSpotPassword(Context context,String password){
        edit(context).putString(PREF_AUTO_LAUNCH_WIFI_HOTSPOT_PASSWORD, password).apply();
    }
    public static boolean isOldDriverMode(Context context){
        return getSharedPreferences(context).getBoolean(NAVI_MODE_NEW_DRIVER, false);
    }
    public static void setOldDriverMode(Context context, boolean newer){
        edit(context).putBoolean(NAVI_MODE_NEW_DRIVER, newer).apply();
    }
    public static boolean isWidgetActived(Context context){
        return getSharedPreferences(context).getBoolean(WIDGET_ACTIVED, false);
    }
    public static void setWidgetActived(Context context,boolean widgetActived){
        edit(context).putBoolean(WIDGET_ACTIVED, widgetActived).apply();
    }
    public static boolean isEnableTempAudioService(Context context){
        return getSharedPreferences(context).getBoolean(ENABLE_TEMP_AUDIO_SERVICE, true);
    }
    public static void setEnableTempAudioService(Context context,boolean enableService){
        edit(context).putBoolean(ENABLE_TEMP_AUDIO_SERVICE, enableService).apply();
    }
    public static boolean isUserManualClosedService(Context context){
        return getSharedPreferences(context).getBoolean(USER_CLOSED_SERVER, false);
    }
    public static void setUserManualClosedServer(Context context,boolean closed){
        edit(context).putBoolean(USER_CLOSED_SERVER, closed).apply();
    }
    public static boolean isEnableAccessibilityService(Context context){
        //return getSharedPreferences(context).getBoolean(ENABLE_ACCESSIBILITY_SEVICE, false);
        return Utils.isAccessibilityServiceEnabled(context, AppDetectionService.class);
    }
    public static void setEnableAccessibilityService(Context context,boolean enable){
        edit(context).putBoolean(ENABLE_ACCESSIBILITY_SEVICE, enable).apply();
    }
    public static boolean isFloattingDirectionHorizontal(Context context){
        return getSharedPreferences(context).getBoolean(FLOATING_WINDWS_DIRECTION_horizontal, false);
    }
    public static void setFloattingDirectionHorizontal(Context context,boolean value){
        edit(context).putBoolean(FLOATING_WINDWS_DIRECTION_horizontal, value).apply();
    }
    public static boolean isEnableAudioService(Context context){
        return getSharedPreferences(context).getBoolean(ENABLE_AUDIO_SERVICE, true);
    }
    public static void setEnableAudioService(Context context,boolean enableService){
        edit(context).putBoolean(ENABLE_AUDIO_SERVICE, enableService).apply();
    }
    public static boolean isEnableAudioMixService(Context context){
        return getSharedPreferences(context).getBoolean(ENABLE_AUDIO_MIX, false);
    }
    public static void setEnableAudioMixService(Context context,boolean enableService){
        edit(context).putBoolean(ENABLE_AUDIO_MIX, enableService).apply();
    }
    public static boolean isEnableAudioVolumeDepress(Context context){
        return getSharedPreferences(context).getBoolean(ENABLE_AUDIO_VOLUME_DEPRESS, false);
    }
    public static void setEnableAudioVolumeDepress(Context context,boolean enableService){
        edit(context).putBoolean(ENABLE_AUDIO_VOLUME_DEPRESS, enableService).apply();
    }
    public static boolean isEnableAutoNaviService(Context context){
        return getSharedPreferences(context).getBoolean(ENABLE_AUTONAVI_SERVICE, true);
    }
    public static void setEnableAutoNaviService(Context context,boolean enableService){
        edit(context).putBoolean(ENABLE_AUTONAVI_SERVICE, enableService).apply();
    }
   /* public static boolean isTermsAccepted(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_TERMS_ACCEPTED, false);
    }*/
    public static void setSpeedAdjust(Context context, int value) {
        edit(context).putInt(PREF_GPS_SPEED_ADJUST, value).apply();
    }

    public static int getSpeedAdjust(Context context) {
        return getSharedPreferences(context).getInt(PREF_GPS_SPEED_ADJUST, 0);
    }
    public static void setDelayStartOtherApp( Context context, int value) {
        edit(context).putInt(PREF_DELAY_AUTO_START, value).apply();
    }

    public static int getDelayStartOtherApp(Context context) {
        return getSharedPreferences(context).getInt(PREF_DELAY_AUTO_START, 0);
    }

  /*  public static void setOtherAppIndex( Context context, int value) {
        edit(context).putInt(PREF_DELAY_AUTO_START_INDEX, value).apply();
    }

    public static int getOtherAppIndex(Context context) {
        return getSharedPreferences(context).getInt(PREF_DELAY_AUTO_START_INDEX, 0);
    }*/
    public static boolean isGoToHomeAfterAutoLanuch(Context context){
        return getSharedPreferences(context).getBoolean(PREF_DELAY_AUTO_START_GOHOME, false);
    }
    public static void setGoToHomeAfterAutoLanuch(Context context,boolean closed){
        edit(context).putBoolean(PREF_DELAY_AUTO_START_GOHOME, closed).apply();
    }
    public static void setAudioVolume(Context context, int value) {
        edit(context).putInt(AUDIO_VOLUME, value).apply();
    }

    public static int getAudioVolume(Context context) {
        return getSharedPreferences(context).getInt(AUDIO_VOLUME, 80);
    }

    public static void setseparatedVolume(Context context, boolean value) {
        edit(context).putBoolean(SEPARATED_VOLUME, value).apply();
    }

   /* public static boolean isSeparatedVolume(Context context) {
        return getSharedPreferences(context).getBoolean(SEPARATED_VOLUME, false);
    }*/

    public static void setShowAddressWhenStop(Context context, boolean value) {
        edit(context).putBoolean(SPEED_SHOW_ADDRESS_WHEN_STOP, value).apply();
    }

    public static boolean isShowAddressWhenStop(Context context) {
        return getSharedPreferences(context).getBoolean(SPEED_SHOW_ADDRESS_WHEN_STOP, false);
    }

    public static void setShowNotification(Context context, boolean value) {
        edit(context).putBoolean(SPEED_SHOW_NOTIFICATION, value).apply();
    }

    public static boolean isShowNotification(Context context) {
        return getSharedPreferences(context).getBoolean(SPEED_SHOW_NOTIFICATION, true);
    }
    public static void setEnabledWatchWidget(Context context, boolean value) {
        edit(context).putBoolean(ENABLE_WATCH_WIDGET, value).apply();
    }

   /* public static boolean isEnabledWatchWidget(Context context) {
        return getSharedPreferences(context).getBoolean(ENABLE_WATCH_WIDGET, false);
    }*/
    public static void setEnabledNumberWidget(Context context, boolean value) {
        edit(context).putBoolean(ENABLE_NUMBER_WIDGET, value).apply();
    }

   /* public static boolean isEnabledNumberWidget(Context context) {
        return getSharedPreferences(context).getBoolean(ENABLE_NUMBER_WIDGET, false);
    }
*/

    public static void setFlatingWindow(Context context, boolean flattingWindow) {
        edit(context).putBoolean(ENABLE_FLATING_WINDOW, flattingWindow).apply();
    }
    public static boolean isEnableFlatingWindow(Context context){
        return getSharedPreferences(context).getBoolean(ENABLE_FLATING_WINDOW, false);
    }
    public static void setFloatingSolidLocation(Context context, float x, float y) {
        edit(context).putString(FLOATTING_WINDOW_XY, x + "," + y).apply();
    }
    public static void setDriveWayFloatingSolidLocation(Context context, float x, float y) {
        edit(context).putString(DRIVE_WAY_FLOATTING_WINDOW_XY, x + "," + y).apply();
    }
    public static void setNaviFloatingSolidLocation(Context context, float x, float y) {
        edit(context).putString(NAVI_FLOATTING_WINDOW_XY, x + "," + y).apply();
    }
    public static void setTimeFloatingSolidLocation(Context context, float x, float y) {
        edit(context).putString(TIME_FLOATTING_WINDOW_XY, x + "," + y).apply();
    }
    public static String getFloatingSolidLocation(Context context) {
        return getSharedPreferences(context).getString(FLOATTING_WINDOW_XY, "0,0");
    }
    public static String getDriveWayFloatingSolidLocation(Context context) {
        return getSharedPreferences(context).getString(DRIVE_WAY_FLOATTING_WINDOW_XY, "0,0");
    }
    public static void setFloatingLocation(Context context, float screenYRatio, boolean left) {
        edit(context).putString(PREF_FLOATING_LOCATION, left + "," + screenYRatio).apply();
    }
    public static String getNaviFloatingSolidLocation(Context context) {
        return getSharedPreferences(context).getString(NAVI_FLOATTING_WINDOW_XY, "0,0");
    }
    public static String getTimeFloatingSolidLocation(Context context) {
        return getSharedPreferences(context).getString(TIME_FLOATTING_WINDOW_XY, "0,0");
    }
    public static String getMapFloatingSolidLocation(Context context) {
        return getSharedPreferences(context).getString(MAP_FLOATTING_WINDOW_XY, "0,0");
    }
    public static void setMapFloatingSolidLocation(Context context, float x, float y) {
        edit(context).putString(MAP_FLOATTING_WINDOW_XY, x + "," + y).apply();
    }

    public static String getTextFloatingSolidLocation(Context context) {
        return getSharedPreferences(context).getString(TEXT_FLOATTING_WINDOW_XY, "0,0");
    }
    public static void setTextFloatingSolidLocation(Context context, float x, float y) {
        edit(context).putString(TEXT_FLOATTING_WINDOW_XY, x + "," + y).apply();
    }
    public static String getLyrcFloatingSolidLocation(Context context) {
        return getSharedPreferences(context).getString(LYRC_FLOATTING_WINDOW_XY, "0,0");
    }
    public static void setLyrcFloatingSolidLocation(Context context, float x, float y) {
        edit(context).putString(LYRC_FLOATTING_WINDOW_XY, x + "," + y).apply();
    }
    public static String getRoadLineFloatingSolidLocation(Context context) {
        return getSharedPreferences(context).getString(ROADLINE_FLOATTING_WINDOW_XY, "0,0");
    }
    public static void setRoadLineFloatingSolidLocation(Context context, float x, float y) {
        edit(context).putString(ROADLINE_FLOATTING_WINDOW_XY, x + "," + y).apply();

    }
        public static void setNaviFloatingLocation(Context context, float screenYRatio, boolean left) {
        edit(context).putString(PREF_NAVI_FLOATING_LOCATION, left + "," + screenYRatio).apply();
    }
   /* public static void setDriveWayFloatingLocation(Context context, float screenYRatio, boolean left) {
        edit(context).putString(PREF_DRIVE_FLOATING_LOCATION, left + "," + screenYRatio).apply();
    }*/
    public static String getFloatingLocation(Context context) {
        return getSharedPreferences(context).getString(PREF_FLOATING_LOCATION, "true,0");
    }
    public static String getNaviFloatingLocation(Context context) {
        return getSharedPreferences(context).getString(PREF_NAVI_FLOATING_LOCATION, "true,0");
    }
   /* public static String getDriveWayFloatingLocation(Context context) {
        return getSharedPreferences(context).getString(PREF_DRIVE_FLOATING_LOCATION, "true,0");
    }
    public static String getTimeFloatingLocation(Context context) {
        return getSharedPreferences(context).getString(PREF_TIME_FLOATING_LOCATION, "true,0");
    }
    public static String getTextFloatingLocation(Context context) {
        return getSharedPreferences(context).getString(PREF_TEXT_FLOATING_LOCATION, "true,0");
    }*/
    public static boolean getUseMetric(Context context) {
        boolean metricDefault;
        Locale current = Locale.getDefault();
        if (current.equals(Locale.US) ||
                current.equals(Locale.UK) ||
                current.getISO3Country().equalsIgnoreCase("mmr")) {
            metricDefault = false;
        } else {
            metricDefault = true;
        }
        return getSharedPreferences(context).getBoolean(PREF_METRIC, metricDefault);
    }


  /*  public static void setOpacity(Context context, int amount) {
        edit(context).putInt(PREF_OPACITY, amount).apply();
    }*/

    public static int getOpacity(Context context) {
        return getSharedPreferences(context).getInt(PREF_OPACITY, 100);
    }

    public static boolean getShowSpeedometer(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_SPEEDOMETER, true);
    }

    public static void setShowSpeedometer(Context context, boolean show) {
        edit(context).putBoolean(PREF_SPEEDOMETER, show).apply();
    }

    public static boolean getShowLimits(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_LIMITS, true);
    }

    public static void setShowLimits(Context context, boolean show) {
        edit(context).putBoolean(PREF_LIMITS, show).apply();
    }
    public static boolean isShowSmallFloatingStyle(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_SHOW_SMALL_FLOATING, false);
    }

    public static void setShowSmallFloatingStyle(Context context, boolean show) {
        edit(context).putBoolean(PREF_SHOW_SMALL_FLOATING, show).apply();
    }
    public static boolean isHideFlatingWindowWhenStop(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_HIDE_FLOATING_WHEN_STOP, false);
    }

    public static void setHideFlatingWindowWhenStop(Context context, boolean hide) {
        edit(context).putBoolean(PREF_HIDE_FLOATING_WHEN_STOP,hide).apply();
    }
    public static boolean isEnableNaviFloating(Context context) {
        return getSharedPreferences(context).getBoolean(ENABLE_NAVI_FLOATING_WINDOWS, false);
    }

    public static void setEnableNaviFloating(Context context, boolean show) {
        edit(context).putBoolean(ENABLE_NAVI_FLOATING_WINDOWS, show).apply();
    }

    public static boolean isEnableNaviFloatingFixed(Context context) {
        return getSharedPreferences(context).getBoolean(NAVI_FLOATTING_FIXED_POSITION, false);
    }

    public static void setEnableNaviFloatingFixed(Context context, boolean show) {
        edit(context).putBoolean(NAVI_FLOATTING_FIXED_POSITION, show).apply();
    }
    public static boolean isEnableSpeedFloatingFixed(Context context) {
        return getSharedPreferences(context).getBoolean(SPEED_FLOATTING_FIXED_POSITION, false);
    }

    public static void setEnableSpeedFloatingFixed(Context context, boolean show) {
        edit(context).putBoolean(SPEED_FLOATTING_FIXED_POSITION, show).apply();
    }
    public static boolean isEnableLyricFloatingFixed(Context context) {
        return getSharedPreferences(context).getBoolean(LYRIC_FLOATTING_FIXED_POSITION, false);
    }

    public static void setEnableLyricFloatingFixed(Context context, boolean show) {
        edit(context).putBoolean(LYRIC_FLOATTING_FIXED_POSITION, show).apply();
    }
    public static boolean isEnableRoadLineFloatingFixed(Context context) {
        return getSharedPreferences(context).getBoolean(ROADLINE_FLOATTING_FIXED_POSITION, false);
    }

    public static void setEnableRoadLineFloatingFixed(Context context, boolean show) {
        edit(context).putBoolean(ROADLINE_FLOATTING_FIXED_POSITION, show).apply();
    }
    public static boolean isEnableRoadLineFloating(Context context) {
        return getSharedPreferences(context).getBoolean(ROADLINE_FLOATTING, true);
    }

    public static void setEnableRoadLineFloating(Context context, boolean show) {
        edit(context).putBoolean(ROADLINE_FLOATTING, show).apply();
    }
    public static boolean isEnableSpeedRoadLine(Context context) {
        return getSharedPreferences(context).getBoolean(ROADLINE_SPEED, true);
    }

    public static void setEnableSpeedRoadLine(Context context, boolean show) {
        edit(context).putBoolean(ROADLINE_SPEED, show).apply();
    }
    public static Set<String> getApps(Context context) {
        return new HashSet<>(getSharedPreferences(context).getStringSet(PREF_APPS, new HashSet<String>()));
    }

    public static void setApps(Context context, Set<String> packageNames) {
        edit(context).putStringSet(PREF_APPS, packageNames).apply();
    }
  /*  public static String getDefaultLanuchApp(Context context) {
        return getSharedPreferences(context).getString(PREF_DEFAULT_LAUNCHE_APP,"");
    }*/

    public static void setDefaultLaunchApp(Context context, String packageName) {
        edit(context).putString(PREF_DEFAULT_LAUNCHE_APP, packageName).apply();
    }
    public static String getAutoLaunchApps(Context context) {
        return getSharedPreferences(context).getString(PREF_AUTO_LAUNCH_APPS, "");
    }

    public static void setAutoLaunchApps(Context context,String packageNames) {
        edit(context).putString(PREF_AUTO_LAUNCH_APPS, packageNames).apply();
    }
    public static String getAutoLaunchAppsName(Context context) {
        return getSharedPreferences(context).getString(PREF_AUTO_LAUNCH_APPS_NAME, "");
    }

    public static void setAutoLaunchAppsName(Context context, String packageNames) {
        edit(context).putString(PREF_AUTO_LAUNCH_APPS_NAME, packageNames).apply();
    }
    public static void setFtpUrl(Context context,String url){
        edit(context).putString(FTP_URL,url).apply();
    }
    public static String getFTPUrl(Context context){
        return getSharedPreferences(context).getString(FTP_URL,"");
    }
    public static void setFtpPort(Context context,String port){
        edit(context).putString(FTP_PORT,port).apply();
    }
    public static String getFTPPort(Context context){
        return getSharedPreferences(context).getString(FTP_PORT,"21");
    }
    public static void setFtpUser(Context context,String user){
        edit(context).putString(FTP_USER,user).apply();
    }
    public static String getFTPUser(Context context){
        return getSharedPreferences(context).getString(FTP_USER,"");
    }
    public static void setFtpPassword(Context context,String password){
        edit(context).putString(FTP_PASSWORD,password).apply();
    }
    public static String getFTPPassword(Context context){
        return getSharedPreferences(context).getString(FTP_PASSWORD,"");
    }
    public static void setFtpPath(Context context,String path){
        edit(context).putString(FTP_PATH,path).apply();
    }
    public static String getFTPPath(Context context){
        return getSharedPreferences(context).getString(FTP_PATH,"");
    }
    public static void setFtpAutoBackup(Context context,boolean auto){
        edit(context).putBoolean(FTP_AUTO_BACKUP,auto).apply();
    }
    public static boolean isFTPAutoBackup(Context context){
        return getSharedPreferences(context).getBoolean(FTP_AUTO_BACKUP,false);
    }
    public static boolean isEnbleDrawOverFeature(Context context){
        boolean overlayEnabled = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context);
        return overlayEnabled;
    }
}
