package com.huivip.gpsspeedwidget.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @author sunlaihui
 */
public class HttpUtils {
    public static String submitPostData(String strUrlPath,Map<String, String> params, String encode) {
        HttpURLConnection httpURLConnection=null;
        try {
            byte[] data = getRequestData(params, encode).toString().getBytes("UTF-8");
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
            Log.d("huivip","post get data error:"+e.getLocalizedMessage());
            return "-1";
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
    private static File downloadFile(String path,String appName ,ProgressDialog pd,String localPath) throws Exception {
        // 如果相等的话表示当前的sdcard挂载在手机上并且是可用的
       /* if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {*/
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            // 获取到文件的大小
            pd.setMax(conn.getContentLength());
            InputStream is = conn.getInputStream();
            String fileName =localPath+"/"+appName;
            Log.d("huivip","download file:"+fileName);
            File file = new File(fileName);
            // 目录不存在创建目录
            if (!file.getParentFile().exists())
                file.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(file);
            BufferedInputStream bis = new BufferedInputStream(is);
            byte[] buffer = new byte[1024];
            int len;
            int total = 0;
            while ((len = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
                total += len;
                // 获取当前下载量
                pd.setProgress(total);
            }
            fos.close();
            bis.close();
            is.close();
            Log.d("huivip","Download finish!");
            return file;
      /*  } else {
            throw new IOException("未发现有SD卡");
        }*/
    }
    public static void downLoadApk(final Context mContext,final String downURL,final String appName ) {

        final ProgressDialog pd; // 进度条对话框
        pd = new ProgressDialog(mContext);
        pd.setCancelable(true);// 必须一直下载完，不可取消
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMessage("正在下载安装包，请稍后");
        pd.setTitle("版本升级");
        pd.show();
        String localPath=FileUtil.createTmpDir(mContext);
        new Thread() {
            @Override
            public void run() {
                try {
                    File file = downloadFile(downURL,appName, pd,localPath);
                    sleep(3000);
                    Utils.installApk(mContext, file);
                    // 结束掉进度条对话框
                    pd.dismiss();
                } catch (Exception e) {
                    pd.dismiss();

                }
            }
        }.start();
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
}
