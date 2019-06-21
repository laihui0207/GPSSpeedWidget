package com.huivip.gpsspeedwidget.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.utils.CrashHandler;
import com.huivip.gpsspeedwidget.utils.FTPUtils;
import com.huivip.gpsspeedwidget.utils.PrefUtils;

import java.io.File;

public class BackupGPSHistoryActivity extends Activity {
    private Handler handler=null;
    private String resultText="";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_gps);
        EditText ftpUrl=findViewById(R.id.editText_address);
        ftpUrl.setText(PrefUtils.getFTPUrl(getApplicationContext()));
        EditText ftpPort=findViewById(R.id.editText_port);
        ftpPort.setText(PrefUtils.getFTPPort(getApplicationContext()));
        EditText ftpUser=findViewById(R.id.editText_username);
        ftpUser.setText(PrefUtils.getFTPUser(getApplicationContext()));
        EditText ftpPassword=findViewById(R.id.editText_password);
        ftpPassword.setText(PrefUtils.getFTPPassword(getApplicationContext()));
        EditText ftpRemoteDir=findViewById(R.id.editText_remoteDir);
        ftpRemoteDir.setText(PrefUtils.getFTPPath(getApplicationContext()));

        TextView result=findViewById(R.id.textView_ftpresult);
        handler=new Handler();
        CheckBox autoBackupCheckBox=findViewById(R.id.checkBox_autobackup);
        autoBackupCheckBox.setChecked(PrefUtils.isFTPAutoBackup(getApplicationContext()));
        autoBackupCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setFtpAutoBackup(getApplicationContext(),isChecked);
            }
        });
        CrashHandler.getInstance().init(getApplicationContext());
        Button saveAndBackup=findViewById(R.id.button_saveandbackup);
        saveAndBackup.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                result.setText("");
                String address=ftpUrl.getText().toString();
                String port=ftpPort.getText().toString();
                String user=ftpUser.getText().toString();
                String password=ftpPassword.getText().toString();
                String remoteDir=ftpRemoteDir.getText().toString();

                if(TextUtils.isEmpty(address)){
                    result.setText("ftp地址不能为空！");
                    return;
                }
                if(TextUtils.isEmpty(port)){
                    result.setText("端口不能为空！");
                    return;
                }
                if(TextUtils.isEmpty(remoteDir)){
                    result.setText("存储路径不能为空！");
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        File dataDir=getDatabasePath("GPSHistory.db");
                        Log.d("huivip","File Path:"+dataDir.getAbsolutePath());
                        FTPUtils ftp=FTPUtils.getInstance();
                        boolean status=ftp.initFTPSetting(address,Integer.parseInt(port),user,password);
                        if(!status){
                            resultText=resultText+"FTP 连接失败！\n";
                            handler.post(runnableUi);
                            return;
                        }
                       else{
                            resultText=resultText+"FTP 连接成功！";
                            handler.post(runnableUi);
                        }
                        status=ftp.uploadFile(remoteDir,dataDir.getAbsolutePath(),"GPSHistory.db");
                        if(!status){
                            resultText=resultText+"FTP 上传失败！";
                            handler.post(runnableUi);
                            return;
                        }
                        else {
                            // save configure
                            resultText=resultText+"FTP 上传成功！";
                            handler.post(runnableUi);
                            PrefUtils.setFtpUrl(getApplicationContext(),address);
                            PrefUtils.setFtpPort(getApplicationContext(),port);
                            PrefUtils.setFtpUser(getApplicationContext(),user);
                            PrefUtils.setFtpPassword(getApplicationContext(),password);
                            PrefUtils.setFtpPath(getApplicationContext(),remoteDir);
                        }
                    }
                }).start();
            }
            Runnable   runnableUi=new  Runnable(){
                @Override
                public void run() {
                    //更新界面
                    result.setText("the Content is:"+resultText);
                }

            };
        });
        Button returnButton=findViewById(R.id.button_backup_return);
        returnButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Button dowloadButton=findViewById(R.id.button_download);
        dowloadButton.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                result.setText("");
                String address=ftpUrl.getText().toString();
                String port=ftpPort.getText().toString();
                String user=ftpUser.getText().toString();
                String password=ftpPassword.getText().toString();
                String remoteDir=ftpRemoteDir.getText().toString();

                if(TextUtils.isEmpty(address)){
                    result.setText("ftp地址不能为空！");
                    return;
                }
                if(TextUtils.isEmpty(port)){
                    result.setText("端口不能为空！");
                    return;
                }
                if(TextUtils.isEmpty(remoteDir)){
                    result.setText("存储路径不能为空！");
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        File dataDir=getDatabasePath("GPSHistory.db");
                        String fileName="GPSHistory.db";
                        //Log.d("huivip","File Path:"+dataDir.getAbsolutePath());
                        FTPUtils ftp=FTPUtils.getInstance();
                        boolean status=ftp.initFTPSetting(address,Integer.parseInt(port),user,password);
                        if(!status){
                            resultText=resultText+"FTP 连接失败！\n";
                            handler.post(runnableUi);
                            return;
                        }
                        else{
                            resultText=resultText+"FTP 连接成功！";
                            handler.post(runnableUi);
                        }
                        //status=ftp.uploadFile(remoteDir,dataDir.getAbsolutePath(),"GPSHistory.db");
                        status=ftp.downLoadFile(remoteDir,dataDir.getAbsolutePath(),fileName);
                        if(!status){
                            resultText=resultText+"FTP下载失败";
                            handler.post(runnableUi);
                            return;
                        }
                        else {
                            // save configure
                            resultText=resultText+"FTP 下载成功！";
                            handler.post(runnableUi);
                            PrefUtils.setFtpUrl(getApplicationContext(),address);
                            PrefUtils.setFtpPort(getApplicationContext(),port);
                            PrefUtils.setFtpUser(getApplicationContext(),user);
                            PrefUtils.setFtpPassword(getApplicationContext(),password);
                            PrefUtils.setFtpPath(getApplicationContext(),remoteDir);
                        }
                    }
                }).start();
            }
            Runnable   runnableUi=new  Runnable(){
                @Override
                public void run() {
                    //更新界面
                    result.setText("the Content is:"+resultText);
                }

            };
        });

    }

}
