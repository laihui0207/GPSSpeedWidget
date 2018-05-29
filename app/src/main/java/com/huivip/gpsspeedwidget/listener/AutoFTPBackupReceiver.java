package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.DeviceUuidFactory;
import com.huivip.gpsspeedwidget.GpsUtil;
import com.huivip.gpsspeedwidget.utils.FTPUtils;
import com.huivip.gpsspeedwidget.utils.HttpUtils;
import com.huivip.gpsspeedwidget.utils.PrefUtils;

import java.io.File;
import java.nio.file.attribute.GroupPrincipal;
import java.util.Date;

public class AutoFTPBackupReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!PrefUtils.isFTPAutoBackup(context)) {
            return;
        }
        Thread backupThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String address = PrefUtils.getFTPUrl(context);
                if (TextUtils.isEmpty(address)) return;
                String port = PrefUtils.getFTPPort(context);
                String user = PrefUtils.getFTPUser(context);
                String password = PrefUtils.getFTPPassword(context);
                String remoteDir = PrefUtils.getFTPPath(context);
                File dataDir = context.getDatabasePath("GPSHistory.db");
                FTPUtils ftp = FTPUtils.getInstance();
                boolean status = ftp.initFTPSetting(address, Integer.parseInt(port), user, password);
                if (!status) {
                    return;
                } else {
                }
                status = ftp.uploadFile(remoteDir, dataDir.getAbsolutePath(), "GPSHistory.db");
                if (!status) {
                    return;
                }
            }
        });
        backupThread.start();
        GpsUtil gpsUtil=GpsUtil.getInstance(context);
        String registUrl=Constant.LBSURL+Constant.LBSREGISTER;
        DeviceUuidFactory deviceUuidFactory=new DeviceUuidFactory(context);
        String deviceId=deviceUuidFactory.getDeviceUuid().toString();
        HttpUtils.getData(String.format(registUrl,deviceId,(new Date()).getTime(),gpsUtil.getLatitude(),gpsUtil.getLongitude(),""));
    }
}
