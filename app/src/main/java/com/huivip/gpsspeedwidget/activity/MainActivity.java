package com.huivip.gpsspeedwidget.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.*;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import com.huivip.gpsspeedwidget.service.BootStartService;
import com.huivip.gpsspeedwidget.utils.DeviceUuidFactory;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.utils.PrefUtils;

import java.util.*;

/**
 * @author sunlaihui
 */
public class MainActivity extends Activity {
    DeviceUuidFactory deviceUuidFactory;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceUuidFactory = new DeviceUuidFactory(getApplicationContext());
        String deviceId=deviceUuidFactory.getDeviceId();
        String deviceId_shortString=deviceId.substring(0,deviceId.indexOf("-"));
        PrefUtils.setDeviceIDString(getApplicationContext(),deviceId_shortString);
        setContentView(R.layout.activity_main);
        initPermission();
        Button configButton=findViewById(R.id.button_config);
        configButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ConfigurationActivity.class)));

        Button testbutton=(Button)findViewById(R.id.button_test);
        testbutton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
/*                startActivity(new Intent(MainActivity.this,AudioTestActivity.class));*/
            }
        });
        Button backupButton=(Button)findViewById(R.id.button_backup);
        backupButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, BackupGPSHistoryActivity.class));
            }
        });
        Button buttonPay=findViewById(R.id.button_paymain);
        buttonPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.dialog_pay,null);
                new AlertDialog.Builder(MainActivity.this).setTitle("打赏随意，多少都是一种支持").setView(layout)
                        .setPositiveButton("关闭", null).show();
            }
        });
        startFloatingWindows(true);
    }
    private void startFloatingWindows(boolean enabled){
           Intent bootStartService=new Intent(getApplicationContext(), BootStartService.class);
           startService(bootStartService);
    }
    private void initPermission() {
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.
            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.System.canWrite(this)){
                Intent intentWriteSetting = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                intentWriteSetting.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intentWriteSetting, 124);
             }
        }*/
    }
}
