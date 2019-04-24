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
    public static int  XunHang_Status_Started=1;
    public static int XunHang_Status_Ended=0;
    public static int LINE=1;
    public static int POINT=0;

    public static String AUTONAVI_CATCH_ROAD_WEBSERVICE="http://restapi.amap.com/v3/autograsp?carid=%s&locations=%s&time=%s&direction=%s&speed=%s&output=json&key=%s";
    public static String LBSSEARCHWEATHER="http://restapi.amap.com/v3/weather/weatherInfo?key=%s&city=%s";
    public static String AUTONAVI_WEB_KEY="5303c7587d2ae8725d2abde74abee79d";
    public static String AUTONAVI_WEB_KEY2="fa74250f4a56fe716a6de1a5d6ec68c3";
    public static String WIFI_USERNAME="车载Wi-Fi";
    public static String WIFI_PASSWORD="88888888";
    public static String AUTONAVI_WEB_KEY_TRACK_SERVICE_ID="15017";
    public static String AUTONAVI_WEB_KEY_TRACK_SERVICE_NAME="GPSWidgetService";
    public static String AUTONAVI_WEB_KEY2_TRACK_SERVICE_ID="15618";
    public static String AUTONAVI_WEB_KEY2_TRACK_SERVICE_NAME="GPSWidgetService";

    public static final String SBC_API_KEY="e5167536c47acada2a942c9c5c9835b1";
    public static final String SBC_PRODUCT_ID="278580229";
    public static final String SBC_PRODUCT_KEY="bbe19c74b62e6f2225854b41594adead";
    public static final String SBC_PRODUCT_SECERT="04afe298a6abf257791da306d6933895";
    //wakeup res
    public static final String WAKEUP_RES = "wakeup_aifar_comm_20180104.bin";
    //vad module res
    public static final String VAD_RES = "vad_aihome_v0.7.bin";
    //local asr module res
    public static final String EBNFC_RES = "ebnfc.aicar.1.2.0.bin";
    public static final String EBNFR_RES = "ebnfr.aicar.1.3.0.bin";
    //local tts module res
    public static final String TTS_DICT_RES = "aitts_sent_dict_idx_2.0.4_20180806.db";
    public static final String TTS_DICT_MD5 = "aitts_sent_dict_idx_2.0.4_20180806.db.md5sum";
    public static final String TTS_FRONT_RES = "local_front.bin";
    public static final String TTS_FRONT_RES_MD5 = "local_front.bin.md5sum";
    public static final String TTS_BACK_RES_LUCY = "lucyf_common_param_ce_local.v2.004.bin";
    public static final String TTS_BACK_RES_LUCY_MD5 = "lucyf_common_param_ce_local.v2.004.bin.md5sum";
    public static final String TTS_BACK_RES_ZHILING = "zhilingf_common_back_ce_local.v2.1.0.bin";
    public static final String TTS_BACK_RES_ZHILING_MD5 = "zhilingf_common_back_ce_local.v2.1.0.bin.md5sum";
    public static final String TTS_BACK_RES_XIJUN = "xijunm_common_back_ce_local.v2.1.0.bin";
    public static final String TTS_BACK_RES_XIJUN_MD5 = "xijunm_common_back_ce_local.v2.1.0.bin.md5sum";
}
