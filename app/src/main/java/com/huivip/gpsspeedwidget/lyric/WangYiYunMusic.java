package com.huivip.gpsspeedwidget.lyric;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.huivip.gpsspeedwidget.beans.MusicEvent;
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
    private static Bitmap bitmap;
    public static MusicEvent downloadLyric(String songName, String artist) {
        String content=null;
        String songId=null;
        MusicEvent res=new MusicEvent(songName,artist);
        if(TextUtils.isEmpty(songName)) return res;
        Map<String,String> params=new HashMap<>();
        params.put("s",songName);
        params.put("limit","2");
        params.put("offset","0");
        params.put("type","1");
        String songDetail=HttpUtils.submitPostData(SEARCH_URL,params,"utf-8");
        if(TextUtils.isEmpty(songDetail)) return res;
        try {
            JSONObject jsonObject=new JSONObject(songDetail);
            if(jsonObject.getString("code").equalsIgnoreCase("200")){
                JSONObject result=jsonObject.getJSONObject("result");
                JSONArray songs =result.getJSONArray("songs");
                if(songs.length()>0){
                    songId=((JSONObject)songs.get(0)).getString("id");
                    JSONObject album=((JSONObject) songs.get(0)).getJSONObject("album");
                    if(TextUtils.isEmpty(songId)){
                        songId=((JSONObject)songs.get(1)).getString("id");
                        album=((JSONObject) songs.get(1)).getJSONObject("album");
                    }
                    if(TextUtils.isEmpty(songId)) return res;
                    res.setMusicCover(album.getString("picUrl"));
                    String lyrcResponse=HttpUtils.getData(String.format(LYRC_URL, songId));
                    if(TextUtils.isEmpty(lyrcResponse)) return res;
                    JSONObject lyrcDetail=new JSONObject(lyrcResponse);
                    JSONObject lyrcObject=lyrcDetail.getJSONObject("lrc");
                    content=lyrcObject.getString("lyric");
                    res.setLyricContent(content);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return res;
    }
}
