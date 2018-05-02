package com.huivip.gpsspeedwidget;

import android.Manifest;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.BindView;
import com.bumptech.glide.Glide;
import com.huivip.gpsspeedwidget.appselection.AppInfo;
import com.huivip.gpsspeedwidget.appselection.AppInfoIconLoader;
import com.huivip.gpsspeedwidget.appselection.AppSelectionActivity;
import com.huivip.gpsspeedwidget.detection.AppDetectionService;
import com.huivip.gpsspeedwidget.utils.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

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
    CheckBox enableNaviFloatingCheckBox;
    CheckBox enableShowFlattingOnDesktopCheckBox;
    EditText remoteUrlEditBox;
    RadioGroup floatingSelectGroup;
    RadioGroup floatingStyleGroup;
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
        if(!PrefUtils.getGPSRemoteUrl(getApplicationContext()).equalsIgnoreCase(Constant.LBSURL)) {
            remoteUrlEditBox.setText(PrefUtils.getGPSRemoteUrl(getApplicationContext()));
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
                if(!TextUtils.isEmpty(url.trim())){
                    PrefUtils.setGpsRemoteUrl(getApplicationContext(),url.trim());
                }
                else {
                    PrefUtils.setGpsRemoteUrl(getApplicationContext(),Constant.LBSURL);
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
                boolean serviceEnabled = Utils.isAccessibilityServiceEnabled(getApplicationContext(), AppDetectionService.class);
                PrefUtils.setEnableAccessibilityService(getApplicationContext(),serviceEnabled);
                EditText ttsVolume=findViewById(R.id.editText_audioVolume);
                String setedVolume=ttsVolume.getText().toString();
                PrefUtils.setAudioVolume(getApplicationContext(),Integer.parseInt(setedVolume));
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
        EditText audioVolumeEditText=findViewById(R.id.editText_audioVolume);
        audioVolumeEditText.setText(PrefUtils.getAudioVolume(getApplicationContext())+"");
        audioVolumeEditText.setFilters(new InputFilter[]{ new InputFilterMinMax(0, 30)});

        enableNaviFloatingCheckBox=findViewById(R.id.checkBox_navfloatiing);
        CheckBox naviAutoSoltCheckBox=findViewById(R.id.checkBox_Navi_autoSolt);
        enableNaviFloatingCheckBox.setChecked(PrefUtils.isEnableNaviFloating(getApplicationContext()));
        enableNaviFloatingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setEnableNaviFloating(getApplicationContext(),buttonView.isChecked());
                naviAutoSoltCheckBox.setEnabled(buttonView.isChecked());
            }
        });

        naviAutoSoltCheckBox.setChecked(PrefUtils.isNaviFloattingAutoSolt(getApplicationContext()));
        naviAutoSoltCheckBox.setEnabled(enableNaviFloatingCheckBox.isChecked());
        naviAutoSoltCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton checkBoxButton, boolean b) {
                PrefUtils.setNaviFloattingWindowsAutoSolt(getApplicationContext(),checkBoxButton.isChecked());
            }
        });
        CheckBox newDriverMode=findViewById(R.id.checkBox_navi_mode);
        newDriverMode.setChecked(PrefUtils.isNewDriverMode(getApplicationContext()));
        newDriverMode.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setNewDriverMode(getApplicationContext(),buttonView.isChecked());
            }
        });
        TextView appVersion=findViewById(R.id.textView_appVersion);
        appVersion.setText("当前版本："+BuildConfig.VERSION_NAME);
        Button checkUpdateButton=findViewById(R.id.button_update);
        final Handler AlterHandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.arg1==0) {
                    AlertDialog.Builder  mDialog = new AlertDialog.Builder(ConfigurationActivity.this);
                    mDialog.setTitle("版本检查");
                    mDialog.setMessage("已是最新版本，无需更新！");
                    mDialog.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            dialog.dismiss();
                        }
                    });
                    mDialog.create().show();
                }
                else if (msg.arg1==1){
                    AlertDialog.Builder  mDialog = new AlertDialog.Builder(ConfigurationActivity.this);
                    try {
                        JSONObject updateInfo=new JSONObject((String)msg.obj);
                        JSONObject data= (JSONObject) updateInfo.get("data");
                        mDialog.setTitle("版本升级");
                        mDialog.setMessage(data.getString("upgradeinfo")).setCancelable(true);
                        String updateUrl=data.getString("updateurl");
                        String appName=data.getString("appname");
                        mDialog.setPositiveButton("更新", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                HttpUtils.downLoadApk(ConfigurationActivity.this,updateUrl,appName);
                            }
                        }).setNegativeButton("不用了", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                   mDialog.create().show();
                }
            }
        };
        View.OnClickListener checkUpdateListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        String updateInfo=HttpUtils.getData(Constant.LBSURL+"/updateInfo");
                        try {
                            if(!TextUtils.isEmpty(updateInfo) && !updateInfo.equalsIgnoreCase("-1")) {
                                String currentVersion=Utils.getLocalVersion(getApplicationContext());
                                JSONObject infoObj = new JSONObject(updateInfo);
                                JSONObject data= (JSONObject) infoObj.get("data");
                                String updateVersion=data.getString("serverVersion");
                                Log.d("huivip","Current local Version:"+currentVersion+",Update Version:"+updateVersion);
                                if(currentVersion.equalsIgnoreCase(updateVersion)){
                                    Message message = Message.obtain();
                                    message.obj ="";
                                    message.arg1 = 0;
                                    AlterHandler.handleMessage(message);
                                } else {
                                    Message message = Message.obtain();
                                    message.obj =updateInfo;
                                    message.arg1 = 1;
                                    AlterHandler.handleMessage(message);
                                }

                            }
                            else {
                                Message message = Message.obtain();
                                message.obj ="";
                                message.arg1 = 0;
                                AlterHandler.handleMessage(message);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Looper.loop();
                    }
                }).start();
            }
        };
        checkUpdateButton.setOnClickListener(checkUpdateListener);
        Button feedbackButton=findViewById(R.id.button_feedback);
        feedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText inputText = new EditText(ConfigurationActivity.this);
                DeviceUuidFactory deviceUuidFactory=new DeviceUuidFactory(getApplicationContext());
                String deviceId=deviceUuidFactory.getDeviceUuid().toString();
                new AlertDialog.Builder(ConfigurationActivity.this).setTitle("请输入反馈内容").setIcon(
                        android.R.drawable.ic_dialog_info).setView(inputText).
                        setPositiveButton("提交", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String content=inputText.getText().toString();
                                if(!TextUtils.isEmpty(content.trim())){
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Map<String, String> params = new HashMap<String, String>();
                                            params.put("feedback_content",content);
                                            params.put("feedbacker_name",deviceId);
                                            HttpUtils.submitPostData(Constant.LBSURL+"/feedback",params,"utf-8");
                                        }
                                    }).start();
                                }
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();
            }
        });
       /* CheckBox floatingWindowStyleCheckbox=findViewById(R.id.checkBox_floating_style);
        floatingWindowStyleCheckbox.setChecked(PrefUtils.isEnableAutoNaviStyle(getApplicationContext()));
        floatingWindowStyleCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setEnableAutoNaviStyle(getApplicationContext(),buttonView.isChecked());
            }
        });*/
        floatingStyleGroup=findViewById(R.id.radioGroup_flattingStyleSelect);
        floatingStyleGroup.setEnabled(enableFloatingWidnowCheckBox.isChecked());
        floatingStyleGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int selectId=radioGroup.getCheckedRadioButtonId();
                switch (selectId) {
                    case R.id.radioButton_style_default:
                        PrefUtils.setFloattingStyle(getApplicationContext(),PrefUtils.FLOATING_DEFAULT);
                        break;
                    case R.id.radioButton_style_autonavi:
                        PrefUtils.setFloattingStyle(getApplicationContext(),PrefUtils.FLOATING_AUTONAVI);
                        break;
                    case R.id.radioButton_style_meter:
                        PrefUtils.setFloattingStyle(getApplicationContext(),PrefUtils.FLOATING_METER);
                        break;
                    default:
                        PrefUtils.setFloattingStyle(getApplicationContext(),PrefUtils.FLOATING_DEFAULT);
                }
            }
        });
        RadioButton styleDeafult=findViewById(R.id.radioButton_style_default);
        RadioButton styleAutoNavi=findViewById(R.id.radioButton_style_autonavi);
        RadioButton styleMeter=findViewById(R.id.radioButton_style_meter);
        String floatStyle=PrefUtils.getFloatingStyle(getApplicationContext());
        if(floatStyle.equalsIgnoreCase(PrefUtils.FLOATING_DEFAULT)){
            styleDeafult.setChecked(true);
        } else if(floatStyle.equalsIgnoreCase(PrefUtils.FLOATING_AUTONAVI)){
            styleAutoNavi.setChecked(true);
        } else if(floatStyle.equalsIgnoreCase(PrefUtils.FLOATING_METER)){
            styleMeter.setChecked(true);
        }
        Button buttonPay=findViewById(R.id.button_pay);
        buttonPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.dialog_pay,null);
                new AlertDialog.Builder(ConfigurationActivity.this).setTitle("打赏随意，多少都是一种支持").setView(layout)
                        .setPositiveButton("关闭", null).show();
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
        PrefUtils.setEnableAccessibilityService(getApplicationContext(),serviceEnabled);
        enableFloatingWidnowCheckBox.setEnabled(overlayEnabled);
        enableNaviFloatingCheckBox.setEnabled(overlayEnabled);
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
        //if(!Settings.System.canWrite(this)){
           /* Intent intentWriteSetting = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            intentWriteSetting.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(intentWriteSetting, 124);*/
       // }
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

