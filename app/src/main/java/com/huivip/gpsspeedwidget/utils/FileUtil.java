package com.huivip.gpsspeedwidget.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

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
                    sb.append("\n");
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
    public static void deleteLric(Context context,String songName,String artist){
        String path =Environment.getExternalStorageDirectory().toString()+"/lyric/";
        File dir=new File(path);
        if(!dir.exists()){
            return;
        }
        String fileName=songName.replace("/","_");
        if(!TextUtils.isEmpty(artist)){
            fileName+="_"+artist.replace("/","_");;
        }
        fileName+=".lrc";
        String lrcFileName=path+fileName;
        File lrcFile=new File(lrcFileName);
        if(lrcFile.exists()){
            lrcFile.delete();
        }
    }
    public static void saveLric(Context context,String songName,String artist,String content){
        String path =Environment.getExternalStorageDirectory().toString()+"/lyric/";
        File dir=new File(path);
        if(!dir.exists()){
            dir.mkdirs();
        }
        String fileName=songName.replace("/","_");
        if(!TextUtils.isEmpty(artist)){
            fileName+="_"+artist.replace("/","_");;
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
            //Toast.makeText(context,"File create Error:"+e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
        }
    }
   /* public static String createGPXFile(List<TraceLocation> data, String selectDate,Context context){
        if(data==null && data.size()==0){
            return null;
        }
        String format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.CHINA);
        StringBuffer content=new StringBuffer();
        String trackElement="<trkpt lat=\"%s\" lon=\"%s\">\n" +
                "        <ele>%s</ele>\n" +
                "        <time>%s</time>\n" +
                "      </trkpt>";
        content.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n" +
                "\n" +
                "<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:gpxx=\"http://www.garmin.com/xmlschemas/GpxExtensions/v3\" xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v1\" creator=\"Oregon 400t\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd\">\n" +
                "  <metadata>\n" +
                "    <link href=\"http://www.garmin.com\">\n" +
                "      <text>GPS Plugin trace Data</text>\n" +
                "    </link>\n" +
                "    <time>"+selectDate+"</time>\n" +
                "  </metadata>");
        content.append("<trk>\n" +
                "    <name>GPS Plugin Trace</name>\n" +
                "    <trkseg>");
        for(TraceLocation location:data){
            content.append(String.format(trackElement,location.getLatitude(),location.getLongitude(),
                    location.getBearing(),dateFormat.format(location.getTime())));
        }
        content.append("</trkseg>\n" +
                "  </trk>\n" +
                "</gpx>");
        String gpxFilePath=createTmpDir(context)+"/gpx_"+selectDate.replaceAll("/","-")+".gpx";
        File gpxFile=new File(gpxFilePath);
        if(gpxFile.exists()){
            gpxFile.delete();
        }

        try {
            FileWriter fileWriter = new FileWriter(gpxFile);
            fileWriter.write(content.toString());
            fileWriter.flush();
            fileWriter.close();
            return gpxFilePath;
        } catch (IOException e) {
            Toast.makeText(context,"File create Error:"+e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
        }
        return null;
    }*/
   /* public static String saveLogToFile(String logContent) {
        String nameString="GPSPluginLog";
        StringBuffer sb = new StringBuffer();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        String result = logContent;
        L.d("Huivip Log to file", result);
        sb.append(result);
        try {
            //long timestamp = System.currentTimeMillis();
            String time = formatter.format(new Date());
            String fileName = nameString + "-" + time + ".log";
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                String path =Environment.getExternalStorageDirectory().toString()+"/huivip/";
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
*//*                Files.write(Paths.get(path+fileName),sb.toString().getBytes(), StandardOpenOption.APPEND);*//*
                FileWriter fw=new FileWriter(path+fileName,true);
                BufferedWriter bw=new BufferedWriter(fw);
                PrintWriter pw=new PrintWriter(bw);
                pw.println(sb.toString());
                pw.close();
                bw.close();
                fw.close();
               *//* FileOutputStream fos = new FileOutputStream(path + fileName);
                fos.write(sb.toString().getBytes());
                fos.close();*//*
            }
            return fileName;
        } catch (Exception e) {
            Log.e("huivip", "an error occured while writing file...", e);
        } finally {

        }
        return null;
    }
    public static void CleanTempFile(){
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            String path =Environment.getExternalStorageDirectory().toString()+"/huivip/";
            File dir = new File(path);
            if (!dir.exists()) {
                return;
            }
            if(dir.isDirectory()){
                File[] files=dir.listFiles();
                if(files.length< 20) return;
                if(files.length>200){
                    for(int i=0;i<200;i++){
                        File file=files[i];
                        if(file.exists() && !file.isDirectory()){
                            file.delete();
                        }
                    }
                }
                if(dir.listFiles()!=null && dir.listFiles().length>200){
                    CleanTempFile();
                }
                *//*for(File file:files){
                    if(file.exists()){
                        file.delete();
                    }
                }*//*
            }
        }
    }*/
}
