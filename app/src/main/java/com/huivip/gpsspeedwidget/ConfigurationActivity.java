package com.huivip.gpsspeedwidget;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import butterknife.BindView;
import com.huivip.gpsspeedwidget.detection.AppDetectionService;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.TTSUtil;
import com.huivip.gpsspeedwidget.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author sunlaihui
 */
public class ConfigurationActivity extends Activity {
    private int mAppWidgetId = 0 ;
    @BindView(R.id.EnableFalting)
    Button enableFloatingButton;
    @BindView(R.id.button_select_app)
    Button appSelectionButton;
    @BindView(R.id.enableOver)
    Button enableServiceButton;
    CheckBox enableFloatingWidnowCheckBox;
    private static final int REQUEST_LOCATION = 105;
    private static  final int REQUEST_STORAGE=106;
    private static final int REQUEST_PHONE=107;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configuration_activity);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {

            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

        }
        initPermission();
        CheckBox autoStartCheckBox=(CheckBox)findViewById(R.id.autoStart);
        CheckBox recordGPSCheckBox= (CheckBox) findViewById(R.id.recordGPS);
        CheckBox uploadGPSCheckBox=(CheckBox)findViewById(R.id.uploadGPSData);
        enableFloatingWidnowCheckBox=findViewById(R.id.enableFloatingWindow);
        CheckBox enableAudioCheckBox=findViewById(R.id.enableAudio);
        CheckBox enableAutoNaviCheckBox=findViewById(R.id.enableAutoNavi);
        boolean start=PrefUtils.isEnableAutoStart(this);
        autoStartCheckBox.setChecked(start);
        /*if(!Utils.isLocationPermissionGranted(this)) {
            askForPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_LOCATION);
        }*/
        boolean recordGPS=PrefUtils.isEnableRecordGPSHistory(getApplicationContext());
        boolean uploadGPSData=PrefUtils.isEnableUploadGPSHistory(getApplicationContext());
        boolean enableFloating=PrefUtils.isEnableFlatingWindow(getApplicationContext());
        boolean enableAudio=PrefUtils.isEnableAudioService(getApplicationContext());
        boolean enableAutoNavi=PrefUtils.isEnableAutoNaviService(getApplicationContext());
        recordGPSCheckBox.setChecked(recordGPS);
        uploadGPSCheckBox.setChecked(uploadGPSData);
        enableFloatingWidnowCheckBox.setChecked(enableFloating);
        enableAudioCheckBox.setChecked(enableAudio);
        enableAutoNaviCheckBox.setChecked(enableAutoNavi);
        autoStartCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton checkBoxButton, boolean b) {
                PrefUtils.setEnableAutoStart(getApplicationContext(),checkBoxButton.isChecked());
            }
        });
        recordGPSCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton checkBoxButton, boolean b) {
                if(!Utils.isStoragePermissionGranted(getApplicationContext())) {
                    askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_STORAGE);
                }
                PrefUtils.setRecordGPSHistory(getApplicationContext(),checkBoxButton.isChecked());
            }
        });
        uploadGPSCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton checkBoxButton, boolean b) {
                if(!Utils.isPhonePermissionGranted(getApplicationContext())) {
                    askForPermission(Manifest.permission.READ_PHONE_STATE, REQUEST_PHONE);
                }
                PrefUtils.setUploadGPSHistory(getApplicationContext(),checkBoxButton.isChecked());
            }
        });
        enableFloatingWidnowCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked()){

                }
                PrefUtils.setFlatingWindow(getApplicationContext(),compoundButton.isChecked());
            }
        });
        enableAudioCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton checkBoxButton, boolean b) {
                PrefUtils.setEnableAudioService(getApplicationContext(),checkBoxButton.isChecked());
            }
        });
        enableAutoNaviCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton checkBoxButton, boolean b) {
                PrefUtils.setEnableAutoNaviService(getApplicationContext(),checkBoxButton.isChecked());
            }
        });
        boolean overlayEnabled = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this);
        enableFloatingButton=findViewById(R.id.EnableFalting);
        enableFloatingButton.setOnClickListener(v -> {
            try {
                openSettings(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, BuildConfig.APPLICATION_ID);
            } catch (ActivityNotFoundException ignored) {
            }
        });
        enableServiceButton=findViewById(R.id.enableOver);
        enableServiceButton.setEnabled(overlayEnabled);
        enableServiceButton.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));

            } catch (ActivityNotFoundException e) {
            }
        });

        PrefUtils.setApps(getApplicationContext(), getDescktopPackageName());
        Button btnOk= (Button) findViewById(R.id.confirm);
        View.OnClickListener confirmListener  = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        };
        btnOk.setOnClickListener(confirmListener);

    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            invalidateStates();
        }
    }
    private void invalidateStates() {
        boolean overlayEnabled = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this);
        boolean serviceEnabled = Utils.isAccessibilityServiceEnabled(this, AppDetectionService.class);
        enableFloatingWidnowCheckBox.setEnabled(overlayEnabled && serviceEnabled);
        enableFloatingButton.setEnabled(!overlayEnabled);
        enableServiceButton.setEnabled(overlayEnabled && !serviceEnabled);
        boolean serviceReady=Utils.isServiceReady(this);
        Button btnOk= (Button) findViewById(R.id.confirm);
        if(serviceReady){
            btnOk.setEnabled(true);
        }
        else {
            btnOk.setEnabled(false);
        }
    }
    private void openSettings(String settingsAction, String packageName) {
        Intent intent = new Intent(settingsAction);
        intent.setData(Uri.parse("package:" + packageName));
        startActivity(intent);
    }
    private Set<String> getDescktopPackageName(){
        Set<String> names =new HashSet<>();
        PackageManager packageManager = this.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for(ResolveInfo resolveInfo : list){
            names.add(resolveInfo.activityInfo.packageName);
        }
        return names;
    }
    /*private boolean isNotificationAccessGranted() {
        return NotificationManagerCompat.getEnabledListenerPackages(ConfigurationActivity.this).contains(BuildConfig.APPLICATION_ID);
    }*/
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
       /* if(!Settings.System.canWrite(this)){
            Intent intentWriteSetting = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            intentWriteSetting.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(intentWriteSetting, 124);
        }*/
      /*  startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        openSettings(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, BuildConfig.APPLICATION_ID);*/
    }
    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(ConfigurationActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(ConfigurationActivity.this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(ConfigurationActivity.this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(ConfigurationActivity.this, new String[]{permission}, requestCode);
            }
        } else {
            //Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }
    }
}
