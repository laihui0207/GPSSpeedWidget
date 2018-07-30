package com.huivip.gpsspeedwidget.lyric;

import android.text.TextUtils;
import com.huivip.gpsspeedwidget.utils.HttpUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class GecimeKu {
    // http://gecimi.com/api/lyric/:song/:artist
   private static String gecimeURL="http://gecimi.com/api/lyric/%s";  // %s song name

    public static String downloadLyric(String songName,String artist){
        if(TextUtils.isEmpty(songName)) return "";
        String lrcString="";
        try {
            String requestUrl= String.format(gecimeURL, URLEncoder.encode(songName,"utf8"));
            if(!TextUtils.isEmpty(artist)){
                requestUrl+="/"+URLEncoder.encode(artist,"utf8");
            }
            String response=HttpUtils.getData(requestUrl);
            if(TextUtils.isEmpty(response)) return "";
            JSONObject resObj=new JSONObject(response);
            JSONArray resultsArray=resObj.getJSONArray("result");
            for(int i=0;i<resultsArray.length();i++){
                JSONObject obj=resultsArray.getJSONObject(i);
                String lrcurl=obj.getString("lrc");
                if(!TextUtils.isEmpty(lrcurl)){
                    lrcString=HttpUtils.getData(lrcurl);
                    if(!TextUtils.isEmpty(lrcString)){
                        return lrcString;
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }
}
