package com.huivip.gpsspeedwidget;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.*;
import butterknife.BindView;
import com.bumptech.glide.Glide;
import com.huivip.gpsspeedwidget.appselection.AppInfo;
import com.huivip.gpsspeedwidget.appselection.AppInfoIconLoader;
import com.huivip.gpsspeedwidget.appselection.AppSelectionActivity;
import com.huivip.gpsspeedwidget.detection.AppDetectionService;
import com.huivip.gpsspeedwidget.utils.FileUtil;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.TTSUtil;
import com.huivip.gpsspeedwidget.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
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
    CheckBox enableShowFlattingOnDesktopCheckBox;
    EditText remoteUrlEditBox;
    RadioGroup floatingSelectGroup;
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
        autoStartCheckBox.setChecked(PrefUtils.isEnableAutoStart(getApplicationContext()));
        CheckBox recordGPSCheckBox= (CheckBox) findViewById(R.id.recordGPS);
        recordGPSCheckBox.setChecked(PrefUtils.isEnableRecordGPSHistory(getApplicationContext()));
        CheckBox uploadGPSCheckBox=(CheckBox)findViewById(R.id.uploadGPSData);
        uploadGPSCheckBox.setChecked(PrefUtils.isEnableUploadGPSHistory(getApplicationContext()));

        enableFloatingWidnowCheckBox=findViewById(R.id.enableFloatingWindow);
        enableFloatingWidnowCheckBox.setChecked(PrefUtils.isEnableFlatingWindow(getApplicationContext()));
        //enableShowFlattingOnDesktopCheckBox=findViewById(R.id.checkBox_showondescktop);
        //enableShowFlattingOnDesktopCheckBox.setChecked(PrefUtils.isEnableShowFlatingOnDesktop(getApplicationContext()));
        remoteUrlEditBox=findViewById(R.id.editText_remoteURL);
        if(!PrefUtils.getGPSHistoryServerURL(getApplicationContext()).equalsIgnoreCase(Constant.LBSURL)) {
            remoteUrlEditBox.setText(PrefUtils.getGPSHistoryServerURL(getApplicationContext()));
        } else {
            remoteUrlEditBox.setText("");
        }
        CheckBox enableAudioCheckBox=findViewById(R.id.enableAudio);
        enableAudioCheckBox.setChecked(PrefUtils.isEnableAudioService(getApplicationContext()));
        CheckBox audidMixCheckBox=findViewById(R.id.checkBox_mix);
        audidMixCheckBox.setChecked(PrefUtils.isEnableAudioMixService(getApplicationContext()));
        audidMixCheckBox.setEnabled(enableAudioCheckBox.isChecked());

        CheckBox enableAutoNaviCheckBox=findViewById(R.id.enableAutoNavi);
        enableAutoNaviCheckBox.setChecked(PrefUtils.isEnableAutoNaviService(getApplicationContext()));
        remoteUrlEditBox.setEnabled(uploadGPSCheckBox.isChecked());

        CheckBox autoSoltCheckBox=findViewById(R.id.checkBox_autoSolt);
        autoSoltCheckBox.setChecked(PrefUtils.isFloattingAutoSolt(getApplicationContext()));
        autoSoltCheckBox.setEnabled(enableFloatingWidnowCheckBox.isChecked());
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
                if(checkBoxButton.isChecked()) {
                    if (!Utils.isPhonePermissionGranted(getApplicationContext())) {
                        askForPermission(Manifest.permission.READ_PHONE_STATE, REQUEST_PHONE);
                    }
                    remoteUrlEditBox.setEnabled(true);
                }
                else {
                    remoteUrlEditBox.setEnabled(false);
                }

                PrefUtils.setUploadGPSHistory(getApplicationContext(),checkBoxButton.isChecked());
            }
        });
        enableFloatingWidnowCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                floatingSelectGroup.setEnabled(compoundButton.isChecked());
                autoSoltCheckBox.setEnabled(compoundButton.isChecked());
                PrefUtils.setFlatingWindow(getApplicationContext(),compoundButton.isChecked());
            }
        });
        /*enableShowFlattingOnDesktopCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PrefUtils.setEnableShowFlattingOnDesktop(getApplicationContext(),compoundButton.isChecked());
            }
        });*/

        enableAudioCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton checkBoxButton, boolean b) {
                audidMixCheckBox.setEnabled(checkBoxButton.isChecked());
                PrefUtils.setEnableAudioService(getApplicationContext(),checkBoxButton.isChecked());
                if(checkBoxButton.isChecked()) {
                    checkIfCanIncreaseMusic();
                }
            }
        });
        audidMixCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PrefUtils.setEnableAudioMixService(getApplicationContext(),compoundButton.isChecked());
                TTSUtil ttsUtil=TTSUtil.getInstance(getApplicationContext());
                ttsUtil.release();
                ttsUtil.initTTs();
            }
        });
        enableAutoNaviCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton checkBoxButton, boolean b) {
                PrefUtils.setEnableAutoNaviService(getApplicationContext(),checkBoxButton.isChecked());
            }
        });
        autoSoltCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PrefUtils.setFloattingWindowsAutoSolt(getApplicationContext(),compoundButton.isChecked());
            }
        });
        CheckBox showLimitCheckbox=findViewById(R.id.checkBox_showLimit);
        showLimitCheckbox.setChecked(PrefUtils.getShowLimits(getApplicationContext()));
        showLimitCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PrefUtils.setShowLimits(getApplicationContext(),compoundButton.isChecked());
            }
        });
        CheckBox showSpeedCheckBox=findViewById(R.id.checkBox_showSpeed);
        showSpeedCheckBox.setChecked(PrefUtils.getShowSpeedometer(getApplicationContext()));
        showSpeedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PrefUtils.setShowSpeedometer(getApplicationContext(),compoundButton.isChecked());
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
        floatingSelectGroup=findViewById(R.id.radioGroup_flatingSelect);
        floatingSelectGroup.setEnabled(enableFloatingWidnowCheckBox.isChecked());
        floatingSelectGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int selectId=radioGroup.getCheckedRadioButtonId();
                switch (selectId) {
                    case R.id.radioButton_all:
                        PrefUtils.setShowFlattingOn(getApplicationContext(),PrefUtils.SHOW_ALL);
                        break;
                    case R.id.radioButton_onlyHome:
                        PrefUtils.setShowFlattingOn(getApplicationContext(),PrefUtils.SHOW_ONLY_DESKTOP);
                        break;
                    case R.id.fradioButton_noHome:
                        PrefUtils.setShowFlattingOn(getApplicationContext(),PrefUtils.SHOW_NO_DESKTOP);
                        break;
                        default:
                            PrefUtils.setShowFlattingOn(getApplicationContext(),PrefUtils.SHOW_NO_DESKTOP);
                }
            }
        });
        Glide.get(this)
                .register(AppInfo.class, InputStream.class, new AppInfoIconLoader.Factory());
        PrefUtils.setApps(getApplicationContext(), getDescktopPackageName());
        Button btnOk= (Button) findViewById(R.id.confirm);
        View.OnClickListener confirmListener  = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                EditText urlText=findViewById(R.id.editText_remoteURL);
                String url=urlText.getText().toString();
                if(url!=null && !url.equalsIgnoreCase("")){
                    PrefUtils.setGpsRemoteUrl(getApplicationContext(),url.trim());
                }
                try {
                    String destPath = FileUtil.createTmpDir(getApplicationContext());
                    FileUtil.copyFromAssets(getApplicationContext().getAssets(), "GPSHistory.js", destPath+"/"+"GPSHistory.js", false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                EditText speedAdjustEditText=findViewById(R.id.editText_speedadjust);
                String adjustValue=speedAdjustEditText.getText().toString();
                if(adjustValue!=null && !adjustValue.equalsIgnoreCase("")){
                    PrefUtils.setSpeedAdjust(getApplicationContext(),Integer.parseInt(adjustValue));
                }
                /*EditText ttsVolume=findViewById(R.id.editText_audioVolume);
                String setedVolume=ttsVolume.getText().toString();
                PrefUtils.setTtsVolume(getApplicationContext(),Integer.parseInt(setedVolume));*/
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        };

        btnOk.setOnClickListener(confirmListener);
        TextView uidView=findViewById(R.id.textView_uid);
        DeviceUuidFactory deviceUuidFactory=new DeviceUuidFactory(getApplicationContext());
        String deviceId=deviceUuidFactory.getDeviceUuid().toString();
        uidView.setText("本机ID: "+deviceId.substring(0,deviceId.indexOf("-")));

        RadioButton allShowButton=findViewById(R.id.radioButton_all);
        RadioButton onlyDesktopButton=findViewById(R.id.radioButton_onlyHome);
        RadioButton noDesktopButton=findViewById(R.id.fradioButton_noHome);

        String floatShowOn=PrefUtils.getShowFlatingOn(getApplicationContext());
        if(floatShowOn.equalsIgnoreCase(PrefUtils.SHOW_ONLY_DESKTOP)){
            onlyDesktopButton.setChecked(true);
        } else if(floatShowOn.equalsIgnoreCase(PrefUtils.SHOW_NO_DESKTOP)){
            noDesktopButton.setChecked(true);
        } else if(floatShowOn.equalsIgnoreCase(PrefUtils.SHOW_ALL)){
            allShowButton.setChecked(true);
        }
        EditText speedAdjustEditText=findViewById(R.id.editText_speedadjust);
        speedAdjustEditText.setFilters(new InputFilter[]{ new InputFilterMinMax(-5, 5)});
        if(PrefUtils.getSpeedAdjust(getApplicationContext())!=0){
            speedAdjustEditText.setText(PrefUtils.getSpeedAdjust(getApplicationContext())+"");
        }

        CheckBox flattingDirectionCheckbox=findViewById(R.id.checkBox_h_direction);
        flattingDirectionCheckbox.setChecked(PrefUtils.isFloattingDirectionHorizontal(getApplicationContext()));
        Intent floatService=new Intent(this, FloatingService.class);
        flattingDirectionCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PrefUtils.setFloattingDirectionHorizontal(getApplicationContext(),compoundButton.isChecked());
                floatService.putExtra(FloatingService.EXTRA_CLOSE, true);
                startService(floatService);
                floatService.removeExtra(FloatingService.EXTRA_CLOSE);
                startService(floatService);
            }
        });
        Button selectAppButton=findViewById(R.id.button_selectApp);
        selectAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ConfigurationActivity.this,AppSelectionActivity.class));
            }
        });

    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            invalidateStates();
        }
    }
    private void checkIfCanIncreaseMusic(){
        AudioManager audioManager= (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int systemVolume=audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        int musicVolume=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        boolean ifSeparatedAudio=false;
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,2,0);
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,1,0);
        int systemVolumeCurrent=audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        if(systemVolumeCurrent!=2 && systemVolumeCurrent==1){
            Log.d("huivip","Separated");
            ifSeparatedAudio=true;
        }
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,systemVolume,0);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,musicVolume,0);
        PrefUtils.setseparatedVolume(getApplicationContext(),ifSeparatedAudio);
    }
    private void invalidateStates() {
        boolean overlayEnabled = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this);
        boolean serviceEnabled = Utils.isAccessibilityServiceEnabled(this, AppDetectionService.class);
        enableFloatingWidnowCheckBox.setEnabled(overlayEnabled && serviceEnabled);
        enableFloatingButton.setEnabled(!overlayEnabled);
        enableServiceButton.setEnabled(overlayEnabled && !serviceEnabled);
        //enableShowFlattingOnDesktopCheckBox.setEnabled(enableFloatingWidnowCheckBox.isChecked());
        floatingSelectGroup.setEnabled(enableFloatingWidnowCheckBox.isChecked());
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
        if(!Settings.System.canWrite(this)){
            Intent intentWriteSetting = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            intentWriteSetting.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(intentWriteSetting, 124);
        }
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
    public class InputFilterMinMax implements InputFilter {

        private int min, max;

        public InputFilterMinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public InputFilterMinMax(String min, String max) {
            this.min = Integer.parseInt(min);
            this.max = Integer.parseInt(max);
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                if(source.toString().equalsIgnoreCase("-")){
                    return null;
                }
                int input = Integer.parseInt(dest.toString() + source.toString());
                if (isInRange(min, max, input))
                    return null;
            } catch (NumberFormatException nfe) { }
            return "";
        }

        private boolean isInRange(int a, int b, int c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }

    }
}

