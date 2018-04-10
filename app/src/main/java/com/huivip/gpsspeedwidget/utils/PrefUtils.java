package com.huivip.gpsspeedwidget.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import com.huivip.gpsspeedwidget.Constant;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public abstract class PrefUtils {

    public static final int STYLE_US = 0;
    public static final int STYLE_INTERNATIONAL = 1;
    private static final String PREF_METRIC = "pref_metric";
    private static final String PREF_SIGN_STYLE = "pref_international";
    private static final String PREF_FLOATING_LOCATION = "pref_floating_location";
    private static final String PREF_TOLERANCE_PERCENT = "pref_overspeed";
    private static final String PREF_TOLERANCE_CONSTANT = "pref_tolerance_constant";
    private static final String PREF_TOLERANCE_MODE = "pref_tolerance_mode";
    private static final String PREF_OPACITY = "pref_opacity";
    private static final String PREF_SPEEDOMETER = "pref_speedometer";
    private static final String PREF_LIMITS = "pref_limits";
    private static final String PREF_BEEP = "pref_beep";
    private static final String PREF_DEBUGGING = "pref_debugging";
    private static final String PREF_APPS = "pref_apps";
    private static final String PREF_AUTO_LAUNCH_APPS = "pref_Auto_apps";
    private static final String PREF_FIRSTRUN = "pref_initial";
    private static final String PREF_VERSION_CODE = "pref_version_code";
    private static final String PREF_SPEEDLIMIT_SIZE = "pref_speedlimit_size";
    private static final String PREF_SPEEDOMETER_SIZE = "pref_speedometer_size";
    private static final String PREF_GMAPS_ONLY_NAVIGATION = "pref_gmaps_only_nav";
    private static final String PREF_TERMS_ACCEPTED = "pref_terms_accepted";
    public static final String PREFS_NAME = "GPSWidgetAutoLaunch";
    public static final String DEVICE_ID_PREFS_NAME = "deviceID";
    public static final String AUTO_START_PREFS_NAME="AutoStart";
    public static final String RECORD_GPS_HISTORY_PREFS_NAME="recordGpsHistory";
    public static final String UPLOAD_GPS_HISTORY_PREFS_NAME="uploadGpsHistory";
    public static final String ENABLE_FLATING_WINDOW="com.huivip.Enable.fating.History";
    public static final String ENABLE_AUDIO_SERVICE="com.huivip.enable.AudioService";
    public static final String ENABLE_AUDIO_MIX="com.huivip.enable.AudioMix";
    public static final String ENABLE_TEMP_AUDIO_SERVICE="com.huivip.enable.temp.AudioService";
    public static final String ENABLE_AUTONAVI_SERVICE="com.huivip.enable.autoNavi.Service";
    public static final String WIDGET_ACTIVED="com.huivip.widget.actived";
    public static final String GPS_REMOTE_URL="com.huivip.widget.remoteUrl";
    public static final String SHOW_FLATTING_ON="com.huivip.widget.showFlatingOn";
    public static final String FLOATTING_WINDOWS_AUTO_SOLT="com.huivip.wdiget.floatting.autoslot";
    public static final String FLOATING_WINDWS_DIRECTION_horizontal="com.huivip.widget.Direction";
    public static final String PREF_GPS_SPEED_ADJUST="com.huivip.widget.speed.adjust";
    public static final String FLOATTING_WINDOW_XY="com.huivip.widget.xy";
    public static final String SEPARATED_VOLUME ="com.huivip.widget.separated.volume";
    public static final String AUDIO_VOLUME="com.huivipo.widget.audio.volume";
    public static final String USER_CLOSED_SERVER="com.huivip.widget.Close.serviced";
    public static final String ENABLE_ACCESSIBILITY_SEVICE="com.huivip.widget.cant.enalble.Accessibility.serviced";
    public static final String ENABLE_WATCH_WIDGET="com.huivip.wdiget.watch.enabled";
    public static final String ENABLE_NUMBER_WIDGET="com.huivip.wdiget.number.enabled";
    public static final String DEVICEID_STORAGE="com.huivip.deviceId.String";
    public static final String CURRENT_DEVICEID="com.huivip.deviceId";
    public static final String ACTIVITY_ON_DESKTOP="com.huivip.widget.onDesktop";
    public static final String SHOW_ALL="0";
    public static final String SHOW_ONLY_DESKTOP="2";
    public static final String SHOW_NO_DESKTOP="1";
    public static final String FTP_URL="com.huivip.ftp.url";
    public static final String FTP_PORT="com.huivip.ftp.port";
    public static final String FTP_USER="com.huivip.ftp.user";
    public static final String FTP_PASSWORD="com.huivip.ftp.password";
    public static final String FTP_PATH="com.huivip.ftp.path";
    public static final String FTP_AUTO_BACKUP="com.huivip.ftp.auto.backup";

    private static SharedPreferences.Editor edit(Context context) {
        return getSharedPreferences(context).edit();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setFirstRun(Context context, boolean firstRun) {
        edit(context).putBoolean(PREF_FIRSTRUN, firstRun).apply();
    }
    public static boolean isFirstRun(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_FIRSTRUN, true);
    }
    public static String getGPSRemoteUrl(Context context){
        return getSharedPreferences(context).getString(GPS_REMOTE_URL,Constant.LBSURL);
    }
    public static void setGpsRemoteUrl(Context context,String url){
        edit(context).putString(GPS_REMOTE_URL,url).apply();
    }
    public static void setTermsAccepted(Context context, boolean firstRun) {
        edit(context).putBoolean(PREF_TERMS_ACCEPTED, firstRun).apply();
    }
    public static void setDeviceIDStorage(Context context,String value){
        edit(context).putString(DEVICEID_STORAGE,value).apply();
    }
    public static String getDeviceIdStorage(Context context){
        return getSharedPreferences(context).getString(DEVICEID_STORAGE,"");
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
    public static void setShowFlattingOn(Context context,String show){
        edit(context).putString(SHOW_FLATTING_ON, show).apply();
    }
    public static String getShowFlatingOn(Context context){
        return getSharedPreferences(context).getString(SHOW_FLATTING_ON, SHOW_NO_DESKTOP);
    }
    public static void setRecordGPSHistory(Context context,boolean recordHistory){
        edit(context).putBoolean(RECORD_GPS_HISTORY_PREFS_NAME, recordHistory).apply();
    }
    public static boolean isEnableRecordGPSHistory(Context context){
        return getSharedPreferences(context).getBoolean(RECORD_GPS_HISTORY_PREFS_NAME, false);
    }
    public static void setUploadGPSHistory(Context context,boolean uploadHistory){
        edit(context).putBoolean(UPLOAD_GPS_HISTORY_PREFS_NAME, uploadHistory).apply();
    }
    public static boolean isEnableUploadGPSHistory(Context context){
        return getSharedPreferences(context).getBoolean(UPLOAD_GPS_HISTORY_PREFS_NAME, false);
    }
    public static boolean isFloattingAutoSolt(Context context){
        return getSharedPreferences(context).getBoolean(FLOATTING_WINDOWS_AUTO_SOLT, true);
    }
    public static void setFloattingWindowsAutoSolt(Context context,boolean autoSolt){
        edit(context).putBoolean(FLOATTING_WINDOWS_AUTO_SOLT, autoSolt).apply();
    }
    public static boolean isOnDesktop(Context context){
        return getSharedPreferences(context).getBoolean(ACTIVITY_ON_DESKTOP, true);
    }
    public static void setOnDesktop(Context context,boolean onDesktop){
        edit(context).putBoolean(ACTIVITY_ON_DESKTOP, onDesktop).apply();
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
        return getSharedPreferences(context).getBoolean(ENABLE_ACCESSIBILITY_SEVICE, false);
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
        return getSharedPreferences(context).getBoolean(ENABLE_AUDIO_SERVICE, false);
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
    public static boolean isEnableAutoNaviService(Context context){
        return getSharedPreferences(context).getBoolean(ENABLE_AUTONAVI_SERVICE, true);
    }
    public static void setEnableAutoNaviService(Context context,boolean enableService){
        edit(context).putBoolean(ENABLE_AUTONAVI_SERVICE, enableService).apply();
    }
    public static boolean isTermsAccepted(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_TERMS_ACCEPTED, false);
    }
    public static void setSpeedAdjust(Context context, int value) {
        edit(context).putInt(PREF_GPS_SPEED_ADJUST, value).apply();
    }

    public static int getSpeedAdjust(Context context) {
        return getSharedPreferences(context).getInt(PREF_GPS_SPEED_ADJUST, 0);
    }

    public static void setAudioVolume(Context context, int value) {
        edit(context).putInt(AUDIO_VOLUME, value).apply();
    }

    public static int getAudioVolume(Context context) {
        return getSharedPreferences(context).getInt(AUDIO_VOLUME, 0);
    }

    public static void setseparatedVolume(Context context, boolean value) {
        edit(context).putBoolean(SEPARATED_VOLUME, value).apply();
    }

    public static boolean isSeparatedVolume(Context context) {
        return getSharedPreferences(context).getBoolean(SEPARATED_VOLUME, false);
    }


    public static void setEnabledWatchWidget(Context context, boolean value) {
        edit(context).putBoolean(ENABLE_WATCH_WIDGET, value).apply();
    }

    public static boolean isEnabledWatchWidget(Context context) {
        return getSharedPreferences(context).getBoolean(ENABLE_WATCH_WIDGET, false);
    }
    public static void setEnabledNumberWidget(Context context, boolean value) {
        edit(context).putBoolean(ENABLE_NUMBER_WIDGET, value).apply();
    }

    public static boolean isEnabledNumberWidget(Context context) {
        return getSharedPreferences(context).getBoolean(ENABLE_NUMBER_WIDGET, false);
    }

    public static void setVersionCode(Context context, int versionCode) {
        edit(context).putInt(PREF_VERSION_CODE, versionCode).apply();
    }

    public static int getVersionCode(Context context) {
        return getSharedPreferences(context).getInt(PREF_VERSION_CODE, 0);
    }
    public static void setFlatingWindow(Context context, boolean flattingWindow) {
        edit(context).putBoolean(ENABLE_FLATING_WINDOW, flattingWindow).apply();
    }
    public static boolean isEnableFlatingWindow(Context context){
        return getSharedPreferences(context).getBoolean(ENABLE_FLATING_WINDOW, false);
    }
    public static void setFloatingSolidLocation(Context context, float x, float y) {
        edit(context).putString(FLOATTING_WINDOW_XY, x + "," + y).apply();
    }

    public static String getFloatingSolidLocation(Context context) {
        return getSharedPreferences(context).getString(FLOATTING_WINDOW_XY, "0,0");
    }
    public static void setFloatingLocation(Context context, float screenYRatio, boolean left) {
        edit(context).putString(PREF_FLOATING_LOCATION, left + "," + screenYRatio).apply();
    }

    public static String getFloatingLocation(Context context) {
        return getSharedPreferences(context).getString(PREF_FLOATING_LOCATION, "true,0");
    }

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

    public static void setUseMetric(Context context, boolean metric) {
        edit(context).putBoolean(PREF_METRIC, metric).apply();
    }

    @SuppressWarnings("WrongConstant")
    @SignStyle
    public static int getSignStyle(Context context) {
        int styleDefault;
        Locale current = Locale.getDefault();
        if (current.equals(Locale.US) || current.equals(Locale.CANADA)) {
            styleDefault = STYLE_US;
        } else {
            styleDefault = STYLE_INTERNATIONAL;
        }

        return getSharedPreferences(context).getInt(PREF_SIGN_STYLE, styleDefault);
    }

    public static void setSignStyle(Context context, @SignStyle int style) {
        edit(context).putInt(PREF_SIGN_STYLE, style).apply();
    }

    public static int getSpeedingPercent(Context context) {
        return getSharedPreferences(context).getInt(PREF_TOLERANCE_PERCENT, 0);
    }

    public static void setSpeedingPercent(Context context, int amount) {
        edit(context).putInt(PREF_TOLERANCE_PERCENT, amount).apply();
    }

    public static int getSpeedingConstant(Context context) {
        return getSharedPreferences(context).getInt(PREF_TOLERANCE_CONSTANT, 0);
    }

    public static void setOpacity(Context context, int amount) {
        edit(context).putInt(PREF_OPACITY, amount).apply();
    }

    public static int getOpacity(Context context) {
        return getSharedPreferences(context).getInt(PREF_OPACITY, 100);
    }

    public static void setSpeedingConstant(Context context, int amount) {
        edit(context).putInt(PREF_TOLERANCE_CONSTANT, amount).apply();
    }

    public static boolean getToleranceMode(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_TOLERANCE_MODE, true);
    }

    public static void setToleranceMode(Context context, boolean and) {
        edit(context).putBoolean(PREF_TOLERANCE_MODE, and).apply();
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

    public static boolean isDebuggingEnabled(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_DEBUGGING, false);
    }

    public static void setDebugging(Context context, boolean debugging) {
        edit(context).putBoolean(PREF_DEBUGGING, debugging).apply();
    }

    public static boolean isBeepAlertEnabled(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_BEEP, true);
    }

    public static void setBeepAlertEnabled(Context context, boolean beep) {
        edit(context).putBoolean(PREF_BEEP, beep).apply();
    }

    public static Set<String> getApps(Context context) {
        return new HashSet<>(getSharedPreferences(context).getStringSet(PREF_APPS, new HashSet<String>()));
    }

    public static void setApps(Context context, Set<String> packageNames) {
        edit(context).putStringSet(PREF_APPS, packageNames).apply();
    }
    public static Set<String> getAutoLaunchApps(Context context) {
        return new HashSet<>(getSharedPreferences(context).getStringSet(PREF_AUTO_LAUNCH_APPS, new HashSet<String>()));
    }

    public static void setAutoLaunchApps(Context context, Set<String> packageNames) {
        edit(context).putStringSet(PREF_AUTO_LAUNCH_APPS, packageNames).apply();
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
    @IntDef({STYLE_US, STYLE_INTERNATIONAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SignStyle {
    }
}
