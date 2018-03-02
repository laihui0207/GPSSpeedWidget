package com.huivip.gpsspeedwidget.utils;

import android.util.Log;
import com.huivip.gpsspeedwidget.LocationVO;
import org.json.JSONArray;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.net.URLEncoder;

/**
 * @author sunlaihui
 */
public class HttpUtils {
    public static String submitPostData(String strUrlPath,Map<String, String> params, String encode) {
        HttpURLConnection httpURLConnection=null;
        try {
            byte[] data = getRequestData(params, encode).toString().getBytes("UTF-8");
            //String urlPath = "http://192.168.1.9:80/JJKSms/RecSms.php";
            URL url = new URL(strUrlPath);

            httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setConnectTimeout(10000);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setFixedLengthStreamingMode(data.length);
            OutputStream outputStream = new BufferedOutputStream(httpURLConnection.getOutputStream());
            outputStream.write(data);
            outputStream.flush();
            outputStream.close();

            int response = httpURLConnection.getResponseCode();
            if(response == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = httpURLConnection.getInputStream();
                return dealResponseResult(inputStream);
            }
        } catch (IOException e) {
            return "err: " + e.getMessage().toString();
        }finally {
           if(httpURLConnection!=null) {
               httpURLConnection.disconnect();
           }
        }
        return "-1";
    }
    public static String getData(String strUrlPath){
        HttpURLConnection httpURLConnection=null;
        try {
            URL url = new URL(strUrlPath);
            Log.d("GPSWidget","URL:"+strUrlPath);
            httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setConnectTimeout(10000);
            httpURLConnection.setRequestMethod("GET");

            int response = httpURLConnection.getResponseCode();
            if(response == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = httpURLConnection.getInputStream();
                return dealResponseResult(inputStream);
            }
        } catch (IOException e) {
            return "err: " + e.getMessage().toString();
        }finally {
            if(httpURLConnection!=null) {
                httpURLConnection.disconnect();
            }
        }
        return "-1";
    }
    public static StringBuffer getRequestData(Map<String, String> params, String encode) {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            for(Map.Entry<String, String> entry : params.entrySet()) {
                stringBuffer.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), encode))
                        .append("&");
            }
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer;
    }

    public static String dealResponseResult(InputStream inputStream) {
        String resultData = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        try {
            while((len = inputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultData = new String(byteArrayOutputStream.toByteArray());
        return resultData;
    }

    public static void main(String[] args){
        List<LocationVO> locationVOList=new ArrayList<>();
        LocationVO locationVO=new LocationVO();
        locationVO.setLat("112123");
        locationVO.setLng("34234");
        locationVO.setCreateTime((new Date()).getTime());
        locationVOList.add(locationVO);
        JSONArray jsonArray = new JSONArray(locationVOList);

        Map<String, String> params = new HashMap<String, String>();
        params.put("deviceid", "001");
        params.put("t", "gps");
        params.put("data", jsonArray.toString());
       String result= HttpUtils.submitPostData("http://localhost:2345/gps", params, "utf-8");
       System.out.println(result);

    }

}
