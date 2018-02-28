package com.huivip.gpsspeedwidget;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import butterknife.BindView;
import com.huivip.gpsspeedwidget.utils.Utils;

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
    private static final int REQUEST_LOCATION = 105;
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

        CheckBox autoStartCheckBox=(CheckBox)findViewById(R.id.autoStart);
        CheckBox recordGPSCheckBox= (CheckBox) findViewById(R.id.recordGPS);
        CheckBox uploadGPSCheckBox=(CheckBox)findViewById(R.id.uploadGPSData);
        SharedPreferences settings = getSharedPreferences(Constant.PREFS_NAME, 0);
        boolean start=settings.getBoolean(Constant.AUTO_START_PREFS_NAME,true);
        autoStartCheckBox.setChecked(start);
        boolean recordGPS=settings.getBoolean(Constant.RECORD_GPS_HISTORY_PREFS_NAME,false);
        boolean uploadGPSData=settings.getBoolean(Constant.UPLOAD_GPS_HISTORY_PREFS_NAME,false);
        recordGPSCheckBox.setChecked(recordGPS);
        uploadGPSCheckBox.setChecked(uploadGPSData);
        CheckBox.OnCheckedChangeListener checkedChangeListener=new CheckBox.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton checkBoxButton, boolean b) {
                SharedPreferences settings = getSharedPreferences(Constant.PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                if(checkBoxButton.isChecked()){
                    editor.putBoolean(Constant.AUTO_START_PREFS_NAME, true);
                }
                else {
                    editor.putBoolean(Constant.AUTO_START_PREFS_NAME, false);
                }
                editor.commit();
            }
        };

        autoStartCheckBox.setOnCheckedChangeListener(checkedChangeListener);

        CheckBox.OnCheckedChangeListener recordGPSCheckedChangeListener=new CheckBox.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton checkBoxButton, boolean b) {
                SharedPreferences settings = getSharedPreferences(Constant.PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                if(checkBoxButton.isChecked()){
                    editor.putBoolean(Constant.RECORD_GPS_HISTORY_PREFS_NAME, true);
                }
                else {
                    editor.putBoolean(Constant.RECORD_GPS_HISTORY_PREFS_NAME, false);
                }
                editor.commit();
            }
        };
        recordGPSCheckBox.setOnCheckedChangeListener(recordGPSCheckedChangeListener);
        CheckBox.OnCheckedChangeListener upLoadGPSCheckedChangeListener=new CheckBox.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton checkBoxButton, boolean b) {
                SharedPreferences settings = getSharedPreferences(Constant.PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                if(checkBoxButton.isChecked()){
                    editor.putBoolean(Constant.UPLOAD_GPS_HISTORY_PREFS_NAME, true);
                }
                else {
                    editor.putBoolean(Constant.UPLOAD_GPS_HISTORY_PREFS_NAME, false);
                }
                editor.commit();
            }
        };
        uploadGPSCheckBox.setOnCheckedChangeListener(upLoadGPSCheckedChangeListener);
/*        appSelectionButton.setOnClickListener(v -> startActivity(new Intent(ConfigurationActivity.this, AppSelectionActivity.class)));*/
        enableServiceButton=findViewById(R.id.enableOver);
        enableServiceButton.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            } catch (ActivityNotFoundException e) {
                //Snackbar.make(enableServiceButton, R.string.open_settings_failed_accessibility, Snackbar.LENGTH_LONG).show();
            }
        });
        enableFloatingButton=findViewById(R.id.EnableFalting);
        enableFloatingButton.setOnClickListener(v -> {
            try {
                //Open the current default browswer App Info page
                openSettings(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, BuildConfig.APPLICATION_ID);
            } catch (ActivityNotFoundException ignored) {
                //Snackbar.make(enableFloatingButton, R.string.open_settings_failed_overlay, Snackbar.LENGTH_LONG).show();
            }
        });
        Button enableLocationButton=findViewById(R.id.button_enable_location);
        enableLocationButton.setOnClickListener(v -> ActivityCompat.requestPermissions(ConfigurationActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION));
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
        boolean permissionGranted = Utils.isLocationPermissionGranted(this);
        @SuppressLint({"NewApi", "LocalSuppress"})
        boolean overlayEnabled = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this);
    }
    private void openSettings(String settingsAction, String packageName) {
        Intent intent = new Intent(settingsAction);
        intent.setData(Uri.parse("package:" + packageName));
        startActivity(intent);
    }
}
