package com.huivip.gpsspeedwidget;

/**
 * @author sunlaihui
 */
public class Constant {
    public static final String LBSURL="http://home.huivip.com.cn:8090";
    public static final String LBSPOSTGPSURL="/gps";
    public static final String LBSGETLASTEDPOSTIONURL="/lasted?deviceId=%s";
    public static final String LBSGETDATADATEURL="/dates?deviceId=%s";
    public static final String LBSGETDATA="/data?deviceId=%s&startTime=%s&endTime=%s";
    public static final String LBSREGISTER="/register?deviceId=%s&regTime=%s&lng=%s&lat=%s&city=%s";
    public static final String CrashLogDir="";

    public static final String AMAPAUTOPACKAGENAME="com.autonavi.amapauto";
    public static final String AMAPAUTOLITEPACKAGENAME="com.autonavi.amapautolite";
    public static final String UPLOADACTION="com.huivip.recordGpsHistory.start";

    public static final String AMAP_SEND_ACTION="AUTONAVI_STANDARD_BROADCAST_SEND";
    public static final String AMAP_RECEIVE_ACTION="AUTONAVI_STANDARD_BROADCAST_RECV";

    public static int Navi_Floating_Enabled=1;
    public static int Navi_Floating_Disabled=0;
    public static int Navi_Status_Started=1;
    public static  int Navi_Status_Ended=0;
    public static int LINE=1;
    public static int POINT=0;

    public static String AUTONAVI_CATCH_ROAD_WEBSERVICE="http://restapi.amap.com/v3/autograsp?carid=%s&locations=%s&time=%s&direction=%s&speed=%s&output=json&key=%s";
    public static String LBSSEARCHWEATHER="http://restapi.amap.com/v3/weather/weatherInfo?key=%s&city=%s";
    public static String AUTONAVI_WEB_KEY="5303c7587d2ae8725d2abde74abee79d";
    public static String AUTONAVI_WEB_KEY2="fa74250f4a56fe716a6de1a5d6ec68c3";
    public static String AUTONAVI_WEB_KEY_TRACK_SERVICE_ID="15017";
    public static String AUTONAVI_WEB_KEY_TRACK_SERVICE_NAME="GPSWidgetService";
    public static String AUTONAVI_WEB_KEY2_TRACK_SERVICE_ID="15618";
    public static String AUTONAVI_WEB_KEY2_TRACK_SERVICE_NAME="GPSWidgetService";
}
