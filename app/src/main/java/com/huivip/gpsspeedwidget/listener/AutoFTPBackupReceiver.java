package com.huivip.gpsspeedwidget.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.huivip.gpsspeedwidget.DBUtil;
import com.huivip.gpsspeedwidget.utils.FTPUtils;
import com.huivip.gpsspeedwidget.utils.FileUtil;
import com.huivip.gpsspeedwidget.utils.PrefUtils;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

public class AutoFTPBackupReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(PrefUtils.isEnableAutoCleanGPSHistory(context)){
            cleanData(context);
        }
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
    }
    private void cleanData(Context context){
        if(PrefUtils.isEnableAutoCleanGPSHistory(context)){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Calendar calendar=Calendar.getInstance();
                    calendar.add(Calendar.MONTH,-1);
                    Date fromDate=calendar.getTime();
                    DBUtil dbUtil= new DBUtil(context);
                    dbUtil.delete(fromDate);
                }
            }).start();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileUtil.CleanTempFile();
            }
        }).start();
    }
}
