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

    public static String AUTONAVI_WEBSERVICE="http://restapi.amap.com/v3/autograsp?carid=%s&locations=%s&time=%s&direction=%s&speed=%s&output=json&key=%s";

}
