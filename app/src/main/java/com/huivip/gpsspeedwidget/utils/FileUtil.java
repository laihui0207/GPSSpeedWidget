package com.huivip.gpsspeedwidget.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.widget.Toast;
import com.amap.api.trace.TraceLocation;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

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

    public static String createGPXFile(List<TraceLocation> data, String selectDate,Context context){
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
    }
}
