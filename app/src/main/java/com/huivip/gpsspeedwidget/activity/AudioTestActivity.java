package com.huivip.gpsspeedwidget.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.utils.CrashHandler;

public class AudioTestActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_test);

        Button saveButton=findViewById(R.id.button_save);
        saveButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
            }
        });
        Button returnButton=findViewById(R.id.button_return);
        returnButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        CrashHandler.getInstance().init(getApplicationContext());
    }
}
