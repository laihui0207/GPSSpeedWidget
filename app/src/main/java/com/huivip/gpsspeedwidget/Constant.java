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
    public static final String LBSREGISTER="/register?deviceId=%s&regTime=%s&lng=%s&lat=%s&versionName=%s&buildNumber=%s";
    public static final String CrashLogDir="";

    public static final String AMAPAUTOPACKAGENAME="com.autonavi.amapauto";
    public static final String AMAPAUTOLITEPACKAGENAME="com.autonavi.amapautolite";
    public static final String UPLOADACTION="com.huivip.recordGpsHistory.start";

    public static final String AMAP_SEND_ACTION="AUTONAVI_STANDARD_BROADCAST_SEND";
    public static final String AMAP_RECEIVE_ACTION="AUTONAVI_STANDARD_BROADCAST_RECV";
    public static final String UPDATE_SEGMENT_EVENT_ACTION="com.huivip.update.segment.event";
    public static int Navi_Floating_Enabled=1;
    public static int Navi_Floating_Disabled=0;
    public static int Navi_Status_Started=1;
    public static  int Navi_Status_Ended=0;
    public static int  XunHang_Status_Started=1;
    public static int XunHang_Status_Ended=0;
    public static int LINE=1;
    public static int POINT=0;

    public static final int APP_WIDGET_HOST_ID = 0x200;
    public static final int SELECT_AMAP_PLUGIN_REQUEST_CODE=2001;

    public static String AUTONAVI_CATCH_ROAD_WEBSERVICE="http://restapi.amap.com/v3/autograsp?carid=%s&locations=%s&time=%s&direction=%s&speed=%s&output=json&key=%s";
    public static String LBSSEARCHWEATHER="http://restapi.amap.com/v3/weather/weatherInfo?key=%s&city=%s";
    public static String LBS_SEARCH_CIRCLE_TRAFFIC="https://restapi.amap.com/v3/traffic/status/circle?key=%s&location=%s&radius=2000";
    public static String LBS_SEARCH_ROAD_TRAFFIC="https://restapi.amap.com/v3/traffic/status/road?key=%s&name=%s&adcode=%s";
    public static String AUTONAVI_WEB_KEY="5303c7587d2ae8725d2abde74abee79d";
    public static String AUTONAVI_WEB_KEY2="fa74250f4a56fe716a6de1a5d6ec68c3";
    public static String WIFI_USERNAME="车载Wi-Fi";
    public static String OVER_SPEED_TTS="您已超速！";
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
    public static final String MUSICPLAYER = "huivip.select.musicplayer";

    public static class NaviInfoConstant {
        //导航类型，对应的值为int类型
        //：GPS导航
        //1：模拟导航
        public static final String TYPE = "TYPE";

        //当前道路名称，对应的值为String类型

        public static final String CUR_ROAD_NAME = "CUR_ROAD_NAME";

        //下一道路名，对应的值为String类型
        public static final String NEXT_ROAD_NAME = "NEXT_ROAD_NAME";


        //距离最近服务区的距离，对应的值为int类型，单位：米
        public static final String SAPA_DIST = "SAPA_DIST";

        //服务区类型，对应的值为int类型
        //0：高速服务区
        //1：其他高速服务设施（收费站等）

        public static final String SAPA_TYPE = "SAPA_TYPE";

        //距离最近的电子眼距离，对应的值为int类型，单位：米
        public static final String CAMERA_DIST = "CAMERA_DIST";

        //电子眼类型，对应的值为int类型
        //0 测速摄像头
        //1为监控摄像头
        //2为闯红灯拍照
        //3为违章拍照
        //4为公交专用道摄像头
        //5为应急车道摄像头

        public static final String CAMERA_TYPE = "CAMERA_TYPE";

        //电子眼限速度，对应的值为int类型，无限速则为0，单位：公里/小时
        public static final String CAMERA_SPEED = "CAMERA_SPEED";
        public static final String CAMERA_DISTANCE="CAMERA_DIST";

        //下一个将要路过的电子眼编号，若为-1则对应的道路上没有电子眼，对应的值为int类型
        public static final String CAMERA_INDEX = "CAMERA_INDEX";

        //导航转向图标，对应的值为int类型
        public static final String ICON = "ICON";

        //路径剩余距离，对应的值为int类型，单位：米
        public static final String ROUTE_REMAIN_DIS = "ROUTE_REMAIN_DIS";

        //路径剩余时间，对应的值为int类型，单位：秒
        public static final String ROUTE_REMAIN_TIME = "ROUTE_REMAIN_TIME";

        //当前导航段剩余距离，对应的值为int类型，单位：米
        public static final String SEG_REMAIN_DIS = "SEG_REMAIN_DIS";

        //当前导航段剩余时间，对应的值为int类型，单位：秒
        public static final String SEG_REMAIN_TIME = "SEG_REMAIN_TIME";

        //当前位置的前一个形状点号，对应的值为int类型，从0开始
        public static final String CUR_POINT_NUM = "CUR_POINT_NUM";

        //环岛出口序号，对应的值为int类型，从0开始.
        //1.x版本：只有在icon为11和12时有效，其余为无效值0
        //2.x版本：只有在icon为11、12、17、18时有效，其余为无效值0
        public static final String ROUND_ABOUT_NUM = "ROUNG_ABOUT_NUM";

        //路径总距离，对应的值为int类型，单位：米
        public static final String ROUTE_ALL_DIS = "ROUTE_ALL_DIS";

        //路径总时间，对应的值为int类型，单位：秒
        public static final String ROUTE_ALL_TIME = "ROUTE_ALL_TIME";

        //当前车速，对应的值为int类型，单位：公里/小时
        public static final String CUR_SPEED = "CUR_SPEED";

        //红绿灯个数，对应的值为int类型
        public static final String TRAFFIC_LIGHT_NUM = "TRAFFIC_LIGHT_NUM";

        //服务区个数，对应的值为int类型
        public static final String SAPA_NUM = "SAPA_NUM";

        //下一个服务区名称，对应的值为String类型
        public static final String SAPA_NAME = "SAPA_NAME";

        //当前道路类型，对应的值为int类型
        //0：高速公路
        //1：国道
        //2：省道
        //3：县道
        //4：乡公路
        //5：县乡村内部道路
        //6：主要大街、城市快速道
        //7：主要道路
        //8：次要道路
        //9：普通道路
        //10：非导航道路

        public static final String ROAD_TYPE = "ROAD_TYPE";
    }

}
