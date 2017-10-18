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

/**
 * @author sunlaihui
 */
public class ConfigurationActivity extends Activity {
    private int mAppWidgetId = 0 ;
    public static final String PREFS_NAME = "GPSWidgetAutoLaunch";

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

        CheckBox checkBox=(CheckBox)findViewById(R.id.checkBox);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean start=settings.getBoolean("start",true);
        checkBox.setChecked(start);
        CheckBox.OnCheckedChangeListener checkedChangeListener=new CheckBox.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton checkBoxButton, boolean b) {
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                if(checkBoxButton.isChecked()){
                    editor.putBoolean("start", true);
                }
                else {
                    editor.putBoolean("start", false);
                }
                editor.commit();
            }
        };

        checkBox.setOnCheckedChangeListener(checkedChangeListener);
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
