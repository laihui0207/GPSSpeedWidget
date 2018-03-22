package com.huivip.gpsspeedwidget;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.huivip.gpsspeedwidget.utils.TTSUtil;

public class AudioTestActivity extends Activity {
    AudioManager audioManager;
    EditText systemEditText;
    EditText musicEditText;
    EditText ringEditText;
    EditText voiceCallEditText;
    EditText alarmEditText;
    EditText notificationText;
/*    EditText accEditText;*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_test);
        audioManager= (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        TextView systemMaxView=findViewById(R.id.textView_maxSystem);
        systemMaxView.setText("System: current:"+audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM)+",max:"+audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM));
        TextView musicTextView=findViewById(R.id.textView_maxMusic);
        musicTextView.setText("Music: current:"+audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)+",max:"+audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        TextView ringTextView=findViewById(R.id.textView_maxRing);
        ringTextView.setText("Ring: current:"+audioManager.getStreamVolume(AudioManager.STREAM_RING)+",max:"+audioManager.getStreamMaxVolume(AudioManager.STREAM_RING));
        TextView voiceCallTextView=findViewById(R.id.textView_voiceCall);
        voiceCallTextView.setText("VoiceCall: current:"+audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL)+",max:"+audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL));
        TextView alarmTextView=findViewById(R.id.textView_Alarm);
        alarmTextView.setText("Alarm Current:"+audioManager.getStreamVolume(AudioManager.STREAM_ALARM)+",max:"+audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
        TextView NotificationTextView=findViewById(R.id.textView_Notification);
        NotificationTextView.setText("Notification Current:"+audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)+",max:"+audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION));
       /* TextView accTextView=findViewById(R.id.textView_ACC);
        accTextView.setText("ACC Current:"+audioManager.getStreamVolume(AudioManager.STREAM_ACCESSIBILITY)+",max:"+audioManager.getStreamMaxVolume(AudioManager.STREAM_ACCESSIBILITY));*/
        systemEditText=findViewById(R.id.editText_system);
        musicEditText=findViewById(R.id.editText_music);
        ringEditText=findViewById(R.id.editText_ring);
        voiceCallEditText=findViewById(R.id.editText_voiceCall);
        alarmEditText=findViewById(R.id.editText_Alarm);
        notificationText=findViewById(R.id.editText_Notification);
/*        accEditText=findViewById(R.id.editText_ACC);*/
        Button buttonSystem=findViewById(R.id.button_system);
        buttonSystem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,Integer.parseInt(systemEditText.getText().toString()),AudioManager.FLAG_SHOW_UI);
            }
        });
        Button buttonMusic=findViewById(R.id.button_music);
        buttonMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,Integer.parseInt(musicEditText.getText().toString()),AudioManager.FLAG_SHOW_UI);
            }
        });
        Button buttonRing=findViewById(R.id.button_ring);
        buttonRing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioManager.setStreamVolume(AudioManager.STREAM_RING,Integer.parseInt(ringEditText.getText().toString()),AudioManager.FLAG_SHOW_UI);
            }
        });
        Button buttonVoiceCall=findViewById(R.id.button_voiceCall);
        buttonVoiceCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,Integer.parseInt(voiceCallEditText.getText().toString()),AudioManager.FLAG_SHOW_UI);
            }
        });
        Button alarmButton=findViewById(R.id.button_Alarm);
        alarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM,Integer.parseInt(alarmEditText.getText().toString()),AudioManager.FLAG_SHOW_UI);
            }
        });
        Button notificationButton=findViewById(R.id.button_Notification);
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,Integer.parseInt(notificationText.getText().toString()),AudioManager.FLAG_SHOW_UI);
            }
        });
       /* Button accButton=findViewById(R.id.button_ACC);
        accButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioManager.setStreamVolume(AudioManager.STREAM_ACCESSIBILITY,Integer.parseInt(accEditText.getText().toString()),AudioManager.FLAG_SHOW_UI);
            }
        });*/
        Button testButton=findViewById(R.id.button_testtts);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TTSUtil ttsUtil=TTSUtil.getInstance(getApplicationContext());
                ttsUtil.speak("你好，语音测试成功");
            }
        });

        Button saveButton=findViewById(R.id.button_save);
        saveButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
               reloadVolume();
            }
        });
        reloadVolume();
    }
    private void reloadVolume(){
        systemEditText.setText(audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM)+"");
        musicEditText.setText(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)+"");
        ringEditText.setText(audioManager.getStreamVolume(AudioManager.STREAM_RING)+"");
        voiceCallEditText.setText(audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL)+"");
        alarmEditText.setText(audioManager.getStreamVolume(AudioManager.STREAM_ALARM)+"");
        notificationText.setText(audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)+"");
    }
}
