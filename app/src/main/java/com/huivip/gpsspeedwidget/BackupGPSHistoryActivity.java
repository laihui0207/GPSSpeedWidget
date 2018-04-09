package com.huivip.gpsspeedwidget;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.huivip.gpsspeedwidget.utils.FTPUtils;

import java.io.File;

public class BackupGPSHistoryActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_gps);
        EditText ftpUrl=findViewById(R.id.editText_address);
        EditText ftpPort=findViewById(R.id.editText_port);
        EditText ftpUser=findViewById(R.id.editText_username);
        EditText ftpPassword=findViewById(R.id.editText_password);
        EditText ftpRemoteDir=findViewById(R.id.editText_remoteDir);

        TextView result=findViewById(R.id.textView_ftpresult);
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
                            result.setText("FTP 连接失败！");
                            return;
                        }
                       else{
                            result.setText("FTP 连接成功！");
                        }
                        status=ftp.uploadFile(remoteDir,dataDir.getAbsolutePath(),"GPSHistory3.db");
                        if(!status){
                            result.setText("FTP 上传失败！");
                            return;
                        }
                        else {
                            // save configure
                            result.setText("FTP 上传成功！");
                        }
                    }
                }).start();
            }
        });
    }

}
