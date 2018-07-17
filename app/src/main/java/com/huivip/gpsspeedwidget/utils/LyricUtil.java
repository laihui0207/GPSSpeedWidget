package com.huivip.gpsspeedwidget.utils;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;


public class LyricUtil {
    private static String mCommonParameter;
    public static final String SECRET_KEY = "ylzsxkwm";
    public static final String VERSION_CODE ="6.4.8.0";
    public static final String VERSION_NAME = "kwplayerhd_ar_" + VERSION_CODE;
    public static final String INSTALL_SOURCE="kwplayer_ar_6.4.8.0_kw.apk";
    // param: songname=xxxx&artist=xxxx&filename=&duration=second*1000&req=2&lrcx=1&rid=x&encode=utf8
    public static String createURL(String paramString) {
        String createURLString = "";
        StringBuilder localStringBuilder = new StringBuilder();
        localStringBuilder.append(getCommonParams());
        localStringBuilder.append(paramString);
        String param = localStringBuilder.toString();
        System.out.println(param);
        try {
            String paramDesString = DES.encryptDES(param, SECRET_KEY);
           // String paramBase64String = new String(Base64.decode(paramDesString));
            createURLString = "http://mobi.kuwo.cn/mobi.s?f=kuwo&q=" + paramDesString;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return createURLString;
    }
    private static String getCommonParams()
    {
        if ((mCommonParameter == null) || ("".equals(mCommonParameter)))
        {
            StringBuilder localStringBuilder = new StringBuilder();
            localStringBuilder.append("user=").append(randDeviceId());
            localStringBuilder.append("&prod=").append(VERSION_NAME);
            localStringBuilder.append("&corp=kuwo");
            localStringBuilder.append("&source=").append(INSTALL_SOURCE);
            localStringBuilder.append("&");
            mCommonParameter = localStringBuilder.toString();
        }
        return mCommonParameter;
    }
    private static String randDeviceId()
    {
        Random localRandom = new Random(System.currentTimeMillis());
        StringBuilder localStringBuilder = new StringBuilder();
        int j = localRandom.nextInt(5);
        int i = j;
        if (j == 0) {
            i = 1;
        }
        i *= 10000;
        localStringBuilder.append(i + localRandom.nextInt(i));
        i = (localRandom.nextInt(5) + 5) * 100000;
        localStringBuilder.append(i + localRandom.nextInt(i));
        return localStringBuilder.toString();
    }
    public static String downloadFromSongMi(String songname,String artist){
        StringBuffer sb=new StringBuffer("http://gecimi.com/api/lyric/");
       // if(TextUtils.isEmpty(songname)) return "";
        try {
            sb.append(URLEncoder.encode(songname,"utf8"));
            if(null!=artist){
                sb.append("/").append(URLEncoder.encode(artist,"utf8"));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
   /* public static void main(String[] artgs){
        StringBuffer sb=new StringBuffer();
        sb.append("type=lyric");
        try {
            String songname=URLEncoder.encode("大海", "utf-8");
            sb.append("&songname=").append(songname);
            sb.append("&artist=").append(URLEncoder.encode("张雨生","utf-8"));
            sb.append("&filename=").append("&duration=").append("&req=2&lrcx=1&rid=40926193&encode=utf8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String paramString=sb.toString();
        String downloadURl=LyricUtil.createURL(paramString);
        downloadURl=LyricUtil.downloadFromSongMi("大海",null);
        System.out.println(downloadURl);
        String content=HttpUtils.getData(downloadURl);
        System.out.println(content);
    }*/
}
