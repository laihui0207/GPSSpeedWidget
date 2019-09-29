package com.huivip.gpsspeedwidget.lyric;

import android.text.TextUtils;

public class QQMusic {
    private static String url="https://c.y.qq.com/soso/fcgi-bin/client_search_cp?aggr=1&cr=1&flag_qc=0&p=1&n=%s&w=%s";
    public static String downloadLyric(String songName,String artist) {
        String content="";
        if(TextUtils.isEmpty(songName)) return content;

        return content;
    }
}
