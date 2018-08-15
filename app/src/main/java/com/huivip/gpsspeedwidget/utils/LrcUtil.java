package com.huivip.gpsspeedwidget.utils;


import android.text.TextUtils;
import com.huivip.gpsspeedwidget.beans.LrcBean;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 王松 on 2016/10/21.
 */

public class LrcUtil {
    /**
     * 传入的参数为标准歌词字符串
     * @param lrcStr
     * @return
     */
    private static String uploader;
    public static List<LrcBean> parseStr2List(String lrcStr) {
        List<LrcBean> list = new ArrayList<>();
        if(TextUtils.isEmpty(lrcStr)) {
            return list;
        }
        String lrcText = lrcStr.replaceAll("&#58;", ":")
                .replaceAll("&#10;", "\n")
                .replaceAll("&#46;", ".")
                .replaceAll("&#32;", " ")
                .replaceAll("&#45;", "-")
                .replaceAll("&#13;", "\r").replaceAll("&#39;", "'");
        String[] split = lrcText.split("\n");
        for (int i = 0; i < split.length; i++) {
            String lrc = split[i];
           /* if (lrc.contains(".")) {*/
                /*String min = lrc.substring(lrc.indexOf("[") + 1, lrc.indexOf("[") + 3);
                String seconds = lrc.substring(lrc.indexOf(":") + 1, lrc.indexOf(":") + 3);
                String mills = lrc.substring(lrc.indexOf(".") + 1, lrc.indexOf(".") + 3);

                long startTime = Long.valueOf(min) * 60 * 1000 + Long.valueOf(seconds) * 1000 + Long.valueOf(mills) * 10;*/
                String[] arr=parseLine(lrc);
                if(arr==null){
                    continue;
                }
                /*if (arr.length == 1) {
                    String last = texts.remove(texts.size() - 1);
                    texts.add(last + arr[0]);
                    continue;
                }*/
                for (int j = 0; j < arr.length - 1; j++) {
                    LrcBean lrcBean = new LrcBean();
                    lrcBean.setStart(Long.parseLong(arr[j]));
                    lrcBean.setLrc(arr[arr.length - 1]);
                    list.add(lrcBean);
                    if (list.size() > 1) {
                        list.get(list.size() - 2).setEnd(Long.parseLong(arr[j]));
                    }
                    if (i == split.length - 1) {
                        list.get(list.size() - 1).setEnd(Long.parseLong(arr[j]) + 100000);
                    }
                   /* mTimes.add(Long.parseLong(arr[i]));
                    texts.add(arr[arr.length - 1]);*/
                }
               /* if(arr.length>1) {

                    String text = lrc.substring(lrc.indexOf("]") + 1);
                    if (text == null || "".equals(text)) {
                        text = "music";
                    }
                    LrcBean lrcBean = new LrcBean();
                    lrcBean.setStart(startTime);
                    lrcBean.setLrc(text);
                    list.add(lrcBean);
                    if (list.size() > 1) {
                        list.get(list.size() - 2).setEnd(startTime);
                    }
                    if (i == split.length - 1) {
                        list.get(list.size() - 1).setEnd(startTime + 100000);
                    }
                }*/
           /* }*/
        }
        return list;
    }
    private static String[] parseLine(String line) {
        Matcher matcher = Pattern.compile("\\[.+\\].+").matcher(line);
        if (!matcher.matches() || line.contains("By:")) {
            if (line.contains("[by:") && line.length() > 6)
                uploader = line.substring(5, line.length() - 1);
            return null;
        }

        if (line.endsWith("]"))
            line += " ";
        line = line.replaceAll("\\[", "");
        String[] result = line.split("\\]");
        try {
            for (int i = 0; i < result.length - 1; ++i)
                result[i] = String.valueOf(parseTime(result[i]));
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
            return null;
        }

        return result;
    }
    private static Long parseTime(String time) {
        String[] min = time.split(":");
        String[] sec;
        if (!min[1].contains("."))
            min[1] += ".00";
        sec = min[1].split("\\.");
        sec[1] = sec[1].replaceAll("\\D+", "").replaceAll("\r", "").replaceAll("\n", "").trim();
        if (sec[1].length() > 3)
            sec[1] = sec[1].substring(0,3);

        long minInt = Long.parseLong(min[0].replaceAll("\\D+", "")
                .replaceAll("\r", "").replaceAll("\n", "").trim());
        long secInt = Long.parseLong(sec[0].replaceAll("\\D+", "")
                .replaceAll("\r", "").replaceAll("\n", "").trim());
        long milInt = Long.parseLong(sec[1]);

        return minInt * 60 * 1000 + secInt * 1000 + milInt * Double.valueOf(Math.pow(10, 3 - sec[1].length())).longValue();
    }

}