package com.huivip.gpsspeedwidget.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import android.widget.Toast;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by fujiayi on 2017/5/19.
 */

public class FileUtil {

    // 创建一个临时目录，用于复制临时文件，如assets目录下的离线资源文件
    public static String createTmpDir(Context context) {
        String sampleDir = "GPS";
        String tmpDir = Environment.getExternalStorageDirectory().toString() + "/" + sampleDir;
        if (!FileUtil.makeDir(tmpDir)) {
            tmpDir = context.getExternalFilesDir(sampleDir).getAbsolutePath();
            if (!FileUtil.makeDir(sampleDir)) {
                throw new RuntimeException("create model resources dir failed :" + tmpDir);
            }
        }
        return tmpDir;
    }

    public static boolean fileCanRead(String filename) {
        File f = new File(filename);
        return f.canRead();
    }

    public static boolean makeDir(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            return file.mkdirs();
        } else {
            return true;
        }
    }

    public static void copyFromAssets(AssetManager assets, String source, String dest, boolean isCover)
            throws IOException {
        File file = new File(dest);
        if (isCover || (!isCover && !file.exists())) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = assets.open(source);
                String path = dest;
                fos = new FileOutputStream(path);
                byte[] buffer = new byte[1024];
                int size = 0;
                while ((size = is.read(buffer, 0, 1024)) >= 0) {
                    fos.write(buffer, 0, size);
                }
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }
                }
            }
        }
    }
    public static void uploadCrashLog(String logPath,String deviceId){

    }
    public static String loadLric(Context context,String songName,String artist){
        String content="";
        if(TextUtils.isEmpty(songName)) return content;
        String path =Environment.getExternalStorageDirectory().toString()+"/lyric/";
        File dir=new File(path);
        if(!dir.exists()){
            return content;
        }
        String fileName=songName.replace("/","_");
        if(!TextUtils.isEmpty(artist)){
            fileName+="_"+artist.replace("/","_");
        }
        fileName+=".lrc";
        String lrcFileName=path+fileName;
        File lrcFile=new File(lrcFileName);
        if(!lrcFile.exists()){
            return content;
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(lrcFileName));
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    line = br.readLine();
                }
                content = sb.toString();
            } finally {
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(TextUtils.isEmpty(content) && lrcFile!=null){
            lrcFile.delete();
        }
        return content;
    }
    public static void saveLric(Context context,String songName,String artist,String content){
        String path =Environment.getExternalStorageDirectory().toString()+"/lyric/";
        File dir=new File(path);
        if(!dir.exists()){
            dir.mkdirs();
        }
        String fileName=songName.replace("/","_");
        if(!TextUtils.isEmpty(artist)){
            fileName+="_"+artist.replace("/","_");
        }
        fileName+=".lrc";
        String lrcFileName=path+fileName;
        File lrcFile=new File(lrcFileName);
        if(lrcFile.exists()){
            lrcFile.delete();
        }
        if(TextUtils.isEmpty(content)){
            return;
        }
        try {
            FileWriter fileWriter = new FileWriter(lrcFile);
            fileWriter.write(content);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            Toast.makeText(context,"File create Error:"+e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    public static String saveLogToFile(String logContent) {
        String nameString="GPSPluginLog";
        StringBuffer sb = new StringBuffer();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        String result = logContent;
        sb.append(result);
        try {
            long timestamp = System.currentTimeMillis();
            String time = formatter.format(new Date());
            String fileName = nameString + "-" + time + "-" + timestamp
                    + ".log";
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                String path =Environment.getExternalStorageDirectory().toString()+"/huivip/";
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(path + fileName);
                fos.write(sb.toString().getBytes());
                fos.close();
            }
            return fileName;
        } catch (Exception e) {
            Log.e("huivip", "an error occured while writing file...", e);
        }
        return null;
    }

}
