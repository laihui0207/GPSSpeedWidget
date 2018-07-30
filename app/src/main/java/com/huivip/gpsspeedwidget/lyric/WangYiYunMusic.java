package com.huivip.gpsspeedwidget.lyric;

import android.text.TextUtils;
import com.huivip.gpsspeedwidget.utils.HttpUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class WangYiYunMusic {
    private static final String SEARCH_URL="http://music.163.com/api/search/pc";
    private static final String SEARCH_PARAM="s=%s&limit=10&offset=0&type=1";
    private static final String LYRC_URL="http://music.163.com/api/song/lyric?os=pc&id=%s&lv=-1&kv=-1&tv=-1";
    public static String downloadLyric(String songName,String artist) {
        String content=null;
        String songId=null;
        if(TextUtils.isEmpty(songName)) return content;
        Map<String,String> params=new HashMap<>();
        params.put("s",songName);
        params.put("limit","10");
        params.put("offset","0");
        params.put("type","1");
        String songDetail=HttpUtils.submitPostData(SEARCH_URL,params,"utf-8");
        if(TextUtils.isEmpty(songDetail)) return content;
        try {
            JSONObject jsonObject=new JSONObject(songDetail);
            if(jsonObject.getString("code").equalsIgnoreCase("200")){
                JSONObject result=jsonObject.getJSONObject("result");
                JSONArray songs =result.getJSONArray("songs");
                if(songs.length()>0){
                    songId=((JSONObject)songs.get(0)).getString("id");
                    if(TextUtils.isEmpty(songId)) return content;
                    String lyrcResponse=HttpUtils.getData(String.format(LYRC_URL, songId));
                    if(TextUtils.isEmpty(lyrcResponse)) return content;
                    JSONObject lyrcDetail=new JSONObject(lyrcResponse);
                    JSONObject lyrcObject=lyrcDetail.getJSONObject("lrc");
                    content=lyrcObject.getString("lyric");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return content;
    }
}
