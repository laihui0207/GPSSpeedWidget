package com.huivip.gpsspeedwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

/**
 * @author sunlaihui
 */
public class ConfigurationActivity extends Activity {
    private int mAppWidgetId = 0 ;

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

}
