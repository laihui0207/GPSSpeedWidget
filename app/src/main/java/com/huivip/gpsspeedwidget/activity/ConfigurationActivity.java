package com.huivip.gpsspeedwidget.activity;

import android.Manifest;
import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.huivip.gpsspeedwidget.BuildConfig;
import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.DeviceUuidFactory;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.appselection.AppInfo;
import com.huivip.gpsspeedwidget.appselection.AppInfoIconLoader;
import com.huivip.gpsspeedwidget.appselection.AppSelectionActivity;
import com.huivip.gpsspeedwidget.beans.PlayAudioEvent;
import com.huivip.gpsspeedwidget.detection.AppDetectionService;
import com.huivip.gpsspeedwidget.manager.Setup;
import com.huivip.gpsspeedwidget.service.DefaultFloatingService;
import com.huivip.gpsspeedwidget.service.LyricFloatingService;
import com.huivip.gpsspeedwidget.service.NaviTrackService;
import com.huivip.gpsspeedwidget.service.RealTimeFloatingService;
import com.huivip.gpsspeedwidget.service.RoadLineFloatingService;
import com.huivip.gpsspeedwidget.speech.SpeechFactory;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.utils.FTPUtils;
import com.huivip.gpsspeedwidget.utils.FileUtil;
import com.huivip.gpsspeedwidget.utils.HttpUtils;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.utils.Utils;
import com.huivip.gpsspeedwidget.utils.WifiUtils;
import com.judemanutd.autostarter.AutoStartPermissionHelper;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author sunlaihui
 */
public class ConfigurationActivity extends Activity {
    private int mAppWidgetId = 0 ;
    @BindView(R.id.EnableFalting)
    Button enableFloatingButton;
   /* @BindView(R.id.button_select_app)
    Button appSelectionButton*/;
    @BindView(R.id.enableOver)
    Button enableServiceButton;
    CheckBox enableFloatingWidnowCheckBox;
    CheckBox enableNaviFloatingCheckBox;
    CheckBox enableShowFlattingOnDesktopCheckBox;
    EditText remoteUrlEditBox;
    RadioGroup floatingSelectGroup;
    RadioGroup floatingStyleGroup;
    RadioButton onlyDesktopButton;
    RadioButton noDesktopButton;
    RadioButton onAutoNaviButton;
    private Handler handler=null;
    private List<String> mAppList;
    String resultText="";
    private static final int REQUEST_LOCATION = 105;
    private static  final int REQUEST_STORAGE=106;
    private static final int REQUEST_PHONE=107;
    AppWidgetHost appWidgetHost;
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
        AppSettings appSettings= Setup.appSettings();
        appWidgetHost = new AppWidgetHost(getApplicationContext(), Constant.APP_WIDGET_HOST_ID);
        handler=new Handler();
        CheckBox autoStartCheckBox=(CheckBox)findViewById(R.id.autoStart);
        autoStartCheckBox.setChecked(appSettings.getAutoStart());
        CheckBox recordGPSCheckBox= (CheckBox) findViewById(R.id.recordGPS);
        recordGPSCheckBox.setChecked(PrefUtils.isEnableRecordGPSHistory(getApplicationContext()));
        CheckBox uploadGPSCheckBox=(CheckBox)findViewById(R.id.uploadGPSData);
        uploadGPSCheckBox.setChecked(PrefUtils.isEnableUploadGPSHistory(getApplicationContext()));
        CheckBox cleanDataCheckBox = findViewById(R.id.checkBox_cleanData);
        cleanDataCheckBox.setChecked(PrefUtils.isEnableAutoCleanGPSHistory(getApplicationContext()));
        if(PrefUtils.isEnableDrawOverFeature(getApplicationContext())){
            if(PrefUtils.isAppFirstRun(getApplicationContext())) {
                PrefUtils.setFlatingWindow(getApplicationContext(), true);
                PrefUtils.setEnableNaviFloating(getApplicationContext(), true);
                PrefUtils.setAppFirstRun(getApplicationContext(),false);
            }
            Utils.startFloatingWindows(getApplicationContext(),true);
        }
        ButterKnife.bind(this);

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
        CheckBox XFEngineCheckBox=findViewById(R.id.checkBox_xftts);
        //XFEngineCheckBox.setChecked(PrefUtils.getTtsEngine(getApplicationContext()).equalsIgnoreCase(SpeechFactory.SIBICHITTS));
        XFEngineCheckBox.setEnabled(enableAudioCheckBox.isChecked());
        CheckBox natviAudioCheckBox=findViewById(R.id.checkBox_natviAudio);
        natviAudioCheckBox.setChecked(SpeechFactory.SDKTTS.equalsIgnoreCase(PrefUtils.getTtsEngine(getApplicationContext())));
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
        cleanDataCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setAutoCleanGPSHistory(getApplicationContext(),buttonView.isChecked());
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
                    cleanDataCheckBox.setChecked(false);
                    cleanDataCheckBox.setEnabled(false);
                    PrefUtils.setAutoCleanGPSHistory(getApplicationContext(),false);
                }
                else {
                    remoteUrlEditBox.setEnabled(false);
                    cleanDataCheckBox.setEnabled(true);
                }

                PrefUtils.setUploadGPSHistory(getApplicationContext(),checkBoxButton.isChecked());
            }
        });
        TextView selectedAMAPPlugin=findViewById(R.id.textView_AMAPPluginId);
              int selectedPluginId=  PrefUtils.getSelectAMAPPLUGIN(getApplicationContext());
        if(selectedPluginId!=-1){
            selectedAMAPPlugin.setText("id:"+selectedPluginId);
        }
        Button selectAMAP=findViewById(R.id.button_selectAmapPlugin);
        selectAMAP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(appWidgetHost==null) {
                        Toast.makeText(getApplicationContext(),"系统不支持原生插件",Toast.LENGTH_LONG).show();
                        return;
                    }

                    int widgetId = appWidgetHost.allocateAppWidgetId();
                    Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
                    pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
                    startActivityForResult(pickIntent, Constant.SELECT_AMAP_PLUGIN_REQUEST_CODE);
                } catch (ActivityNotFoundException e){
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
                    for(int id=0;id<500;id++) {
                        AppWidgetProviderInfo popupWidgetInfo = appWidgetManager.getAppWidgetInfo(id);
                        final View amapView = appWidgetHost.createView(getApplicationContext(), id, popupWidgetInfo);
                        View vv = Utils.getViewByIds(amapView, new Object[]{"widget_container", "daohang_container", 0, "gongban_daohang_right_blank_container", "daohang_widget_image"});
                        if(vv!=null && vv instanceof ImageView){
                            Toast.makeText(getApplicationContext(), "已选择高德插件:"+id, Toast.LENGTH_LONG).show();
                            PrefUtils.setSelectAmapPluginId(getApplicationContext(),id);
                            break;
                        }
                    }
                    TextView selectedAMAPPlugin=findViewById(R.id.textView_AMAPPluginId);
                    int selectedPluginId=  PrefUtils.getSelectAMAPPLUGIN(getApplicationContext());
                    if(selectedPluginId!=-1){
                        selectedAMAPPlugin.setText("已选择高德插件id:"+selectedPluginId);
                    } else {
                        selectedAMAPPlugin.setText("未选择高德插件");
                    }
                }
            }
        });
        CheckBox naviTrackServiceCheckbox=findViewById(R.id.checkBox_naviTrack);
        naviTrackServiceCheckbox.setChecked(PrefUtils.isEnableNAVIUploadGPSHistory(getApplicationContext()));
        naviTrackServiceCheckbox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setUploadNAVIGPSHistory(getApplicationContext(),buttonView.isChecked());
                Intent trackService=new Intent(getApplicationContext(), NaviTrackService.class);
                if(buttonView.isChecked()){
                    if(!Utils.isServiceRunning(getApplicationContext(),NaviTrackService.class.getName())){
                        startService(trackService);
                    }
                } else {
                    if(Utils.isServiceRunning(getApplicationContext(),NaviTrackService.class.getName())){
                        stopService(trackService);
                    }
                }
            }
        });
        enableFloatingWidnowCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                floatingSelectGroup.setEnabled(compoundButton.isChecked());
                autoSoltCheckBox.setEnabled(compoundButton.isChecked());
                PrefUtils.setFlatingWindow(getApplicationContext(),compoundButton.isChecked());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(compoundButton.isChecked() ){
                            Utils.startFloatingWindows(getApplicationContext(),true);
                        }
                        else {
                            Utils.startFloatingWindows(getApplicationContext(),false);
                        }
                    }
                }).start();

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
                XFEngineCheckBox.setEnabled(checkBoxButton.isChecked());
                PrefUtils.setEnableAudioService(getApplicationContext(),checkBoxButton.isChecked());
                /*if(checkBoxButton.isChecked()) {
                    checkIfCanIncreaseMusic();
                }*/
            }
        });
        audidMixCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PrefUtils.setEnableAudioMixService(getApplicationContext(),compoundButton.isChecked());
               /* BDTTS bdtts =BDTTS.getInstance(getApplicationContext());
                bdtts.release();*/
            }
        });

        XFEngineCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String speakText = "";
                PrefUtils.setTTSEngineType(getApplicationContext(), SpeechFactory.BAIDUTTS);
                speakText = "使用百度语音";
                SeekBar seekBarVolume = findViewById(R.id.seekBar_audioVolume);
                int volume = seekBarVolume.getProgress();
                PrefUtils.setAudioVolume(getApplicationContext(), volume);
                //TTS tts = SpeechFactory.getInstance(getApplicationContext()).getTTSEngine(PrefUtils.getTtsEngine(getApplicationContext()));
                /*                tts.speak(speakText);*/
               // GpsUtil.getInstance(getApplicationContext()).setTts(tts);
            }
        });

        natviAudioCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(buttonView.isChecked()){
                    PrefUtils.setTTSEngineType(getApplicationContext(),SpeechFactory.SDKTTS);
                    XFEngineCheckBox.setChecked(false);
                }
                else {
                    PrefUtils.setTTSEngineType(getApplicationContext(),SpeechFactory.BAIDUTTS);
                }
            }
        });
        Button testAudoButton=findViewById(R.id.button_testAudio);
        testAudoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SeekBar seekBarVolume=findViewById(R.id.seekBar_audioVolume);
                int volume=seekBarVolume.getProgress();
                PrefUtils.setAudioVolume(getApplicationContext(),volume);
                //TTS tts=SpeechFactory.getInstance(getApplicationContext()).getTTSEngine(PrefUtils.getTtsEngine(getApplicationContext()));
                EventBus.getDefault().post(new PlayAudioEvent("北京第三区交通委提醒您：道路千万条，安全第一条。行车不规范，亲人两行泪。",true));
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
        CheckBox showNaviLimitCheckbox=findViewById(R.id.checkBox_navi_showLimit);
        showNaviLimitCheckbox.setChecked(PrefUtils.getShowNaviLimits(getApplicationContext()));
        showNaviLimitCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PrefUtils.setShowNaviLimits(getApplicationContext(),compoundButton.isChecked());
            }
        });
        CheckBox cacheAudioFileCheckBox=findViewById(R.id.checkBox_cacheAudioFile);
        cacheAudioFileCheckBox.setChecked(PrefUtils.isEnableCacheAudioFile(getApplicationContext()));
        cacheAudioFileCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setEnableCacheAudioFile(getApplicationContext(),buttonView.isChecked());
            }
        });
        CheckBox floatingSmallType=findViewById(R.id.checkBox_smallType);
        floatingSmallType.setChecked(PrefUtils.isShowSmallFloatingStyle(getApplicationContext()));
        floatingSmallType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setShowSmallFloatingStyle(getApplicationContext(),buttonView.isChecked());
            }
        });
        CheckBox audioVolumeDepressCheckBox=findViewById(R.id.checkBox_audioDepress);
        audioVolumeDepressCheckBox.setChecked(PrefUtils.isEnableAudioVolumeDepress(getApplicationContext()));
        audioVolumeDepressCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setEnableAudioVolumeDepress(getApplicationContext(),buttonView.isChecked());
            }
        });
        CheckBox floatingHideWhenStop=findViewById(R.id.checkBox_hideFloating);
        floatingHideWhenStop.setChecked(PrefUtils.isHideFlatingWindowWhenStop(getApplicationContext()));
        floatingHideWhenStop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setHideFlatingWindowWhenStop(getApplicationContext(),buttonView.isChecked());
            }
        });
        CheckBox floatingHideWhenOnNavi=findViewById(R.id.checkBox_hideFloating_onAutoNavi);
        floatingHideWhenOnNavi.setChecked(PrefUtils.isHideFloatingWidowOnNaviApp(getApplicationContext()));
        floatingHideWhenOnNavi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setHideFloatingWidowOnNaviApp(getApplicationContext(),buttonView.isChecked());
            }
        });
        CheckBox autoGoHomeAfterNaviCheckBox=findViewById(R.id.checkBox_returnHome_afterAutoStart);
        autoGoHomeAfterNaviCheckBox.setChecked(PrefUtils.isEnableAutoGoHomeAfterNaviStarted(getApplicationContext()));
        autoGoHomeAfterNaviCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setEnableAutoGoHomeAfterNaviStarted(getApplicationContext(),buttonView.isChecked());
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
        CheckBox notifyRoadLimitCheckBox=findViewById(R.id.checkBox_roadLimitNotify);
        notifyRoadLimitCheckBox.setChecked(PrefUtils.isRoadLimitNotify(getApplicationContext()));
        notifyRoadLimitCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setRoadLimitNotify(getApplicationContext(),buttonView.isChecked());
            }
        });
        CheckBox gpsUseMPHCheckbox=findViewById(R.id.checkbox_speedType_mph);
        gpsUseMPHCheckbox.setChecked(PrefUtils.isEnableGPSUseMPH(getApplicationContext()));
        gpsUseMPHCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setEnableGPSUseMPH(getApplicationContext(),buttonView.isChecked());
            }
        });

        CheckBox enableLyricCheckBox=findViewById(R.id.checkBox_lyric);
        enableLyricCheckBox.setChecked(PrefUtils.isLyricEnabled(getApplicationContext()));
        enableLyricCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setLyricEnabled(getApplicationContext(),buttonView.isChecked());
                if(buttonView.isChecked()){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        if (!Utils.isNotificationEnabled(getApplicationContext())) {
                            Utils.openNotificationWindows(getApplicationContext());
                        }
                    }
                } else {
                    if (Utils.isServiceRunning(getApplicationContext(), LyricFloatingService.class.getName())) {
                        Intent lycFloatingService = new Intent(getApplicationContext(), LyricFloatingService.class);
                        lycFloatingService.putExtra(LyricFloatingService.EXTRA_CLOSE, true);
                        startService(lycFloatingService);
                    }
                }
            }
        });
        CheckBox autoWidgetFloatingCheckBox=findViewById(R.id.checkBox_Auto_widget_floating);
        autoWidgetFloatingCheckBox.setEnabled(PrefUtils.getSelectAMAPPLUGIN(getApplicationContext())!=-1);
        autoWidgetFloatingCheckBox.setChecked(PrefUtils.isEnableAutoWidgetFloatingWidow(getApplicationContext()));
        autoWidgetFloatingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setEnableAutoWidgetFloatingWidow(getApplicationContext(),buttonView.isChecked());
            }
        });
        CheckBox onlyShowAutoWidgetOnTurnCheckBox=findViewById(R.id.checkBox_Auto_widget_floating_only);
        onlyShowAutoWidgetOnTurnCheckBox.setChecked(PrefUtils.isEnableAutoWidgetFloatingWidowOnlyTurn(getApplicationContext()));
        onlyShowAutoWidgetOnTurnCheckBox.setEnabled(PrefUtils.getSelectAMAPPLUGIN(getApplicationContext())!=-1);
        onlyShowAutoWidgetOnTurnCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setEnableAutoWidgetFloatingWidowOnlyTurn(getApplicationContext(),buttonView.isChecked());
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
                int selectId = radioGroup.getCheckedRadioButtonId();
                switch (selectId) {
                    case R.id.radioButton_all:
                        PrefUtils.setShowFlattingOn(getApplicationContext(), PrefUtils.SHOW_ALL);
                        break;
                    case R.id.radioButton_onlyHome:
                        PrefUtils.setShowFlattingOn(getApplicationContext(), PrefUtils.SHOW_ONLY_DESKTOP);
                        break;
                    case R.id.fradioButton_noHome:
                        PrefUtils.setShowFlattingOn(getApplicationContext(), PrefUtils.SHOW_NO_DESKTOP);
                        break;
                    case R.id.radioButton_autonavi:
                        PrefUtils.setShowFlattingOn(getApplicationContext(),PrefUtils.SHOW_ONLY_AUTONAVI);
                   /* default:
                        PrefUtils.setShowFlattingOn(getApplicationContext(), PrefUtils.SHOW_ALL);*/
                }
            }
        });
        RadioButton allShowButton=findViewById(R.id.radioButton_all);
        onlyDesktopButton=findViewById(R.id.radioButton_onlyHome);
        noDesktopButton=findViewById(R.id.fradioButton_noHome);
        onAutoNaviButton=findViewById(R.id.radioButton_autonavi);
        String floatShowOn=PrefUtils.getShowFlatingOn(getApplicationContext());
        if(PrefUtils.isEnableAccessibilityService(getApplicationContext())) {
            if (floatShowOn.equalsIgnoreCase(PrefUtils.SHOW_ONLY_DESKTOP)) {
                onlyDesktopButton.setChecked(true);
            } else if (floatShowOn.equalsIgnoreCase(PrefUtils.SHOW_NO_DESKTOP)) {
                noDesktopButton.setChecked(true);
            } else if (floatShowOn.equalsIgnoreCase(PrefUtils.SHOW_ALL)) {
                allShowButton.setChecked(true);
            } else if (floatShowOn.equalsIgnoreCase(PrefUtils.SHOW_ONLY_AUTONAVI)) {
                onAutoNaviButton.setChecked(true);
            }
        } else if (floatShowOn.equalsIgnoreCase(PrefUtils.SHOW_ONLY_AUTONAVI)) {
                onAutoNaviButton.setChecked(true);
            //allShowButton.setChecked(true);
        }
        onlyDesktopButton.setEnabled(PrefUtils.isEnableAccessibilityService(getApplicationContext()));
        noDesktopButton.setEnabled(PrefUtils.isEnableAccessibilityService(getApplicationContext()));
        //onAutoNaviButton.setEnabled(PrefUtils.isEnableAccessibilityService(getApplicationContext()));
        Glide.get(this)
                .register(AppInfo.class, InputStream.class, new AppInfoIconLoader.Factory());
        PrefUtils.setApps(getApplicationContext(), Utils.getDesktopPackageName(getApplicationContext()));
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
                EditText delayTimeEditText=findViewById(R.id.editText_delay_started);
                String delayTimeValue=delayTimeEditText.getText().toString();
                if(TextUtils.isEmpty(delayTimeValue)){
                    delayTimeValue="0";
                }
                PrefUtils.setDelayStartOtherApp(getApplicationContext(),Integer.parseInt(delayTimeValue));
                boolean serviceEnabled = Utils.isAccessibilityServiceEnabled(getApplicationContext(), AppDetectionService.class);
                PrefUtils.setEnableAccessibilityService(getApplicationContext(),serviceEnabled);
               /* EditText ttsVolume=findViewById(R.id.editText_audioVolume);
                String setAudioVolume=ttsVolume.getText().toString();
                if(TextUtils.isEmpty(setAudioVolume)){
                    setAudioVolume="5";
                }
                PrefUtils.setAudioVolume(getApplicationContext(),Integer.parseInt(setAudioVolume));*/
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        };
        EditText delayTimeEditText=findViewById(R.id.editText_delay_started);
        delayTimeEditText.setText(PrefUtils.getDelayStartOtherApp(getApplicationContext())+"");
        btnOk.setOnClickListener(confirmListener);
        TextView uidView=findViewById(R.id.textView_uid);
        DeviceUuidFactory deviceUuidFactory=new DeviceUuidFactory(getApplicationContext());
        String deviceId=deviceUuidFactory.getDeviceUuid().toString();
        uidView.setText("本机ID: "+deviceId.substring(0,deviceId.indexOf("-")));
        CheckBox autoMuted=findViewById(R.id.checkBox_autoMuted);
        autoMuted.setChecked(PrefUtils.isEnableAutoMute(getApplicationContext()));
        autoMuted.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setEnableAutoMute(getApplicationContext(),buttonView.isChecked());
            }
        });
        CheckBox autoLaunchHotSpotCheckBox=findViewById(R.id.checkBox_autoWifi);
        autoLaunchHotSpotCheckBox.setEnabled(WifiUtils.checkMobileAvalible(getApplicationContext()));
        autoLaunchHotSpotCheckBox.setChecked(PrefUtils.isAutoLauchHotSpot(getApplicationContext()));
        autoLaunchHotSpotCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.System.canWrite(ConfigurationActivity.this)) {
                        Intent intentWriteSetting = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                                Uri.parse("package:" + getPackageName()));
                        //intentWriteSetting.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivityForResult(intentWriteSetting, 1240);
                    } else {
                        if (buttonView.isChecked()) {
                            setWifiConfig(buttonView);
                        } else {
                            autoLaunchChanged(buttonView);
                        }
                    }
                } else {
                    if (buttonView.isChecked()) {
                        setWifiConfig(buttonView);
                    } else {
                        autoLaunchChanged(buttonView);
                    }
                }
            }
            private void setWifiConfig(CompoundButton buttonView){
                LayoutInflater layoutInflater = LayoutInflater.from(ConfigurationActivity.this);
                View promptView = layoutInflater.inflate(R.layout.dialog_wifi_setting, null);
                android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(ConfigurationActivity.this);
                alertDialogBuilder.setView(promptView);
                final EditText nameEditText = (EditText) promptView.findViewById(R.id.input_wifiName);
                nameEditText.setText(PrefUtils.getAutoLauchHotSpotName(getApplicationContext()));
                final EditText passwordEditText = (EditText) promptView.findViewById(R.id.input_wifiPassword);
                passwordEditText.setText(PrefUtils.getAutoLauchHotSpotPassword(getApplicationContext()));
                alertDialogBuilder.setCancelable(false)
                        .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String wifiName = nameEditText.getText().toString();
                                String wifiPassword = passwordEditText.getText().toString();
                                if(wifiPassword.length()<8){
                                    Toast.makeText(getApplicationContext(),"密码最低要求8位以上",Toast.LENGTH_LONG).show();
                                    return;
                                }
                                if (TextUtils.isEmpty(wifiName) || TextUtils.isEmpty(wifiPassword)) {
                                    wifiName = Constant.WIFI_USERNAME;
                                    wifiPassword = Constant.WIFI_PASSWORD;
                                }
                                PrefUtils.setAutoLaunchHotSpotName(getApplicationContext(),wifiName);
                                PrefUtils.setAutoLaunchHotSpotPassword(getApplicationContext(),wifiPassword);
                                autoLaunchChanged(buttonView);
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("取消",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                final android.app.AlertDialog alert = alertDialogBuilder.create();
                alert.show();
            }
            private void autoLaunchChanged(CompoundButton buttonView) {
                String wifiName=PrefUtils.getAutoLauchHotSpotName(getApplicationContext());
                String wifiPassword=PrefUtils.getAutoLauchHotSpotPassword(getApplicationContext());
                if (buttonView.isChecked()) {
                    boolean enabled = WifiUtils.switchWifiHotspot(getApplicationContext(), wifiName, wifiPassword, true);
                    if (enabled) {
                        PrefUtils.setAutoLaunchHotSpot(getApplicationContext(), buttonView.isChecked());
                        Toast.makeText(getApplicationContext(), "移动热点已启动:" + wifiName+ ",密码:" + wifiPassword, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "移动热点启动失败！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    boolean enabled = WifiUtils.switchWifiHotspot(getApplicationContext(), wifiName, wifiPassword, false);
                    if (enabled) {
                        Toast.makeText(getApplicationContext(), "移动热点已关闭！", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "移动热点关闭失败！", Toast.LENGTH_SHORT).show();
                    }
                    PrefUtils.setAutoLaunchHotSpot(getApplicationContext(), buttonView.isChecked());
                }
            }
        });
        EditText speedAdjustEditText=findViewById(R.id.editText_speedadjust);
        speedAdjustEditText.setFilters(new InputFilter[]{ new InputFilterMinMax(-100, 100)});
        if(PrefUtils.getSpeedAdjust(getApplicationContext())!=0){
            speedAdjustEditText.setText(PrefUtils.getSpeedAdjust(getApplicationContext())+"");
        }

        CheckBox flattingDirectionCheckbox=findViewById(R.id.checkBox_h_direction);
        flattingDirectionCheckbox.setChecked(PrefUtils.isFloattingDirectionHorizontal(getApplicationContext()));
        Intent floatService=new Intent(this, DefaultFloatingService.class);
        flattingDirectionCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PrefUtils.setFloattingDirectionHorizontal(getApplicationContext(),compoundButton.isChecked());
                floatService.putExtra(DefaultFloatingService.EXTRA_CLOSE, true);
                startService(floatService);
                floatService.removeExtra(DefaultFloatingService.EXTRA_CLOSE);
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
        AudioManager audioManager= (AudioManager) getSystemService(Context.AUDIO_SERVICE);
       /* EditText audioVolumeEditText=findViewById(R.id.editText_audioVolume);
        audioVolumeEditText.setText(PrefUtils.getAudioVolume(getApplicationContext())+"");
        audioVolumeEditText.setFilters(new InputFilter[]{ new InputFilterMinMax(0, 100)}); //audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)*/
        SeekBar volumeSeekBar=findViewById(R.id.seekBar_audioVolume);
        volumeSeekBar.setProgress(PrefUtils.getAudioVolume(getApplicationContext()));
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                PrefUtils.setAudioVolume(getApplicationContext(),progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
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
        newDriverMode.setChecked(PrefUtils.isOldDriverMode(getApplicationContext()));
        newDriverMode.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setOldDriverMode(getApplicationContext(),buttonView.isChecked());
            }
        });
        TextView appVersion=findViewById(R.id.textView_appVersion);
        appVersion.setText("当前版本："+BuildConfig.VERSION_NAME);
        Button checkUpdateButton=findViewById(R.id.button_update);

        View.OnClickListener checkUpdateListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        String updateInfo=HttpUtils.getData(Constant.LBSURL+"/updateInfo?type=full");
                        try {
                            if(!TextUtils.isEmpty(updateInfo) && !updateInfo.equalsIgnoreCase("-1")) {
                                String currentVersion=Utils.getLocalVersion(ConfigurationActivity.this);
                                int currentVersionCode=Utils.getLocalVersionCode(getApplicationContext());
                                JSONObject infoObj = new JSONObject(updateInfo);
                                JSONObject data= (JSONObject) infoObj.get("data");
                                String updateVersion=data.getString("serverVersion");
                                int updateVersionCode=data.getInt("serverVersionCode");
                                Message message = Message.obtain();
                                message.obj =updateInfo;
                                if(currentVersionCode!=0 && updateVersionCode!=0){
                                    if(updateVersionCode>currentVersionCode){
                                        message.arg1 = 1;
                                        AlterHandler.handleMessage(message);
                                    } else {
                                        message.arg1 = 0;
                                        AlterHandler.handleMessage(message);
                                    }
                                } else {
                                    if (currentVersion.equalsIgnoreCase(updateVersion)) {
                                        message.arg1 = 0;
                                        AlterHandler.handleMessage(message);
                                    } else {
                                        message.arg1 = 1;
                                        AlterHandler.handleMessage(message);
                                    }
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
            final Handler AlterHandler=new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    if(msg.arg1==0) {
                        AlertDialog.Builder  mDialog = new AlertDialog.Builder(new ContextThemeWrapper(ConfigurationActivity.this,R.style.Theme_AppCompat_DayNight));
                        mDialog.setTitle("版本检查");
                        mDialog.setMessage("已是最新版本，无需更新！");
                        mDialog.setPositiveButton("关闭",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.dismiss();
                            }
                        }).setNegativeButton("重装", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                JSONObject updateInfo= null;
                                try {
                                    updateInfo = new JSONObject((String)msg.obj);
                                    JSONObject data= (JSONObject) updateInfo.get("data");
                                    String updateUrl=data.getString("updateurl");
                                    String appName=data.getString("appname");
                                    HttpUtils.downLoadApk(ConfigurationActivity.this,updateUrl,appName);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        mDialog.create().show();
                    }
                    else if (msg.arg1==1){
                        AlertDialog.Builder  mDialog = new AlertDialog.Builder(new ContextThemeWrapper(ConfigurationActivity.this,R.style.Theme_AppCompat_DayNight));
                        try {
                            JSONObject updateInfo = new JSONObject((String)msg.obj);
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
        };
        checkUpdateButton.setOnClickListener(checkUpdateListener);
        Button feedbackButton=findViewById(R.id.button_feedback);
        feedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText inputText = new EditText(ConfigurationActivity.this);
                DeviceUuidFactory deviceUuidFactory=new DeviceUuidFactory(getApplicationContext());
                String deviceId=deviceUuidFactory.getDeviceUuid().toString();
                new AlertDialog.Builder(new ContextThemeWrapper(ConfigurationActivity.this,R.style.Theme_AppCompat_DayNight)).setTitle("请输入反馈内容").setIcon(
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
                Utils.startFloatingWindows(getApplicationContext(),true);
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
                new AlertDialog.Builder(new ContextThemeWrapper(ConfigurationActivity.this,R.style.Theme_AppCompat_DayNight)).setTitle("打赏随意，多少都是一种支持").setView(layout)
                        .setPositiveButton("关闭", null).show();
            }
        });
        CheckBox playTimeCheckBox=findViewById(R.id.checkBox_playtime);
        playTimeCheckBox.setChecked(PrefUtils.isPlayTime(getApplicationContext()));
        playTimeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setPlayTime(getApplicationContext(),buttonView.isChecked());
            }
        });
        CheckBox playWeathereCheckBox=findViewById(R.id.checkBox_playweather);
        playWeathereCheckBox.setChecked(PrefUtils.isPlayWeather(getApplicationContext()));
        playWeathereCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setPlayWeather(getApplicationContext(),buttonView.isChecked());
            }
        });
        CheckBox playWarnCheckBox=findViewById(R.id.checkBox_playWarn);
        playWarnCheckBox.setChecked(PrefUtils.isPlayWarn(getApplicationContext()));
        playWarnCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setPlayWarn(getApplicationContext(),buttonView.isChecked());
            }
        });
        CheckBox naviFloatingFixedCheckbox=findViewById(R.id.checkBox_navi_fixed_position);
        naviFloatingFixedCheckbox.setChecked(PrefUtils.isEnableNaviFloatingFixed(getApplicationContext()));
        naviFloatingFixedCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setEnableNaviFloatingFixed(getApplicationContext(),buttonView.isChecked());
            }
        });
        CheckBox speedFloatingFixedCheckBox=findViewById(R.id.checkBox_speed_fixed_position);
        speedFloatingFixedCheckBox.setChecked(PrefUtils.isEnableSpeedFloatingFixed(getApplicationContext()));
        speedFloatingFixedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setEnableSpeedFloatingFixed(getApplicationContext(),buttonView.isChecked());
            }
        });
        CheckBox lyricFloatingFixedCheckBox=findViewById(R.id.lyric_fixed);
        lyricFloatingFixedCheckBox.setChecked(PrefUtils.isEnableLyricFloatingFixed(getApplicationContext()));
        lyricFloatingFixedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setEnableLyricFloatingFixed(getApplicationContext(),buttonView.isChecked());
            }
        });
        CheckBox speedRoadLineCheckBox=findViewById(R.id.speed_roadLine);
        speedRoadLineCheckBox.setChecked(PrefUtils.isEnableSpeedRoadLine(getApplicationContext()));
        speedRoadLineCheckBox.setEnabled(PrefUtils.getSelectAMAPPLUGIN(getApplicationContext())!=-1);
        speedRoadLineCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setEnableSpeedRoadLine(getApplicationContext(),buttonView.isChecked());
            }
        });
        CheckBox roadLineCheckBox=findViewById(R.id.checkBox_roadLine);
        roadLineCheckBox.setEnabled(PrefUtils.getSelectAMAPPLUGIN(getApplicationContext())!=-1);
        roadLineCheckBox.setChecked(PrefUtils.isEnableRoadLineFloating(getApplicationContext()));
        roadLineCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setEnableRoadLineFloating(getApplicationContext(),buttonView.isChecked());
                Intent roadLine=new Intent(getApplicationContext(), RoadLineFloatingService.class);
                if(!buttonView.isChecked()){
                    roadLine.putExtra(RoadLineFloatingService.EXTRA_CLOSE,true);
                    getApplicationContext().startService(roadLine);
                } else {
                    getApplicationContext().startService(roadLine);
                }
            }
        });
        CheckBox roadLineFloatingFixedCheckBox=findViewById(R.id.roadline_fixed);
        roadLineFloatingFixedCheckBox.setEnabled(PrefUtils.getSelectAMAPPLUGIN(getApplicationContext())!=-1);
        roadLineFloatingFixedCheckBox.setChecked(PrefUtils.isEnableRoadLineFloatingFixed(getApplicationContext()));
        roadLineFloatingFixedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setEnableRoadLineFloatingFixed(getApplicationContext(),buttonView.isChecked());
                Intent roadLineFloattingService = new Intent(getApplicationContext(),RoadLineFloatingService.class);
                roadLineFloattingService.putExtra(RoadLineFloatingService.EXTRA_Fixed,true);
                getApplicationContext().startService(roadLineFloattingService);
            }
        });
        CheckBox showAddressCheckBox=findViewById(R.id.checkBox_showAddress);
        showAddressCheckBox.setChecked(PrefUtils.isShowAddressWhenStop(getApplicationContext()));
        showAddressCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setShowAddressWhenStop(getApplicationContext(),buttonView.isChecked());
            }
        });
        CheckBox showNotificationCheckBox=findViewById(R.id.checkBox_notifiction);
        showNotificationCheckBox.setChecked(PrefUtils.isShowNotification(getApplicationContext()));
        showNotificationCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //WeatherService service= WeatherService.getInstance(getApplicationContext());
                //service.stopLocation();
                PrefUtils.setShowNotification(getApplicationContext(),buttonView.isChecked());
                //service.startLocation();

            }
        });
        CheckBox goToHomeCheckBox=findViewById(R.id.checkBox_gotoHome);
        goToHomeCheckBox.setChecked(PrefUtils.isGoToHomeAfterAutoLanuch(getApplicationContext()));
        goToHomeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setGoToHomeAfterAutoLanuch(getApplicationContext(),buttonView.isChecked());
            }
        });
        CheckBox enableTimeFloating=findViewById(R.id.checkBox_timeFloating);
        enableTimeFloating.setChecked(PrefUtils.isEnableTimeFloatingWidow(getApplicationContext()));
        enableTimeFloating.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.setEnableTimeFloatingWidow(getApplicationContext(),buttonView.isChecked());
                if(buttonView.isChecked()){
                    if(!Utils.isServiceRunning(getApplicationContext(),RealTimeFloatingService.class.getName())){
                        Intent timefloating=new Intent(getApplicationContext(),RealTimeFloatingService.class);
                        startService(timefloating);
                    }
                }
                else {
                    if(Utils.isServiceRunning(getApplicationContext(),RealTimeFloatingService.class.getName())){
                        Intent timefloating=new Intent(getApplicationContext(), RealTimeFloatingService.class);
                        timefloating.putExtra(RealTimeFloatingService.EXTRA_CLOSE,true);
                        startService(timefloating);
                    }
                }
            }
        });
        Button uploadLogButton=findViewById(R.id.button_uploadLog);
        uploadLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String logDir=Environment.getExternalStorageDirectory().toString()+"/huivip/";
                File dir=new File(logDir);
                if(!dir.exists()){
                    Toast.makeText(getApplicationContext(),"没有异常日志需要上传！",Toast.LENGTH_SHORT).show();
                } else if(dir.isDirectory()) {
                    File[] files=dir.listFiles();
                    if(files.length==0) {
                        Toast.makeText(getApplicationContext(), "没有异常日志需要上传！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            FTPUtils ftp=FTPUtils.getInstance();
                            ftp.initFTPSetting("home.huivip.com.cn",21,"laihui","pass");
                            ftp.uploadDirectory("/sda1/gps/"+deviceId.substring(0,deviceId.indexOf("-")),logDir);
                            File dir=new File(logDir);
                            if(dir.exists()){
                                FileFilter filter = new FileFilter() {
                                    @Override
                                    public boolean accept(File pathname) {
                                        return pathname.isFile() && pathname.getName().indexOf(".log")>-1;
                                    }
                                };

                                for(File file:dir.listFiles(filter)){
                                        if(file.exists()){
                                            file.delete();
                                        }
                                }
                                dir.delete();
                            }
                            resultText="日志上传成功，请联系作者查询问题，并提供本机Id:"+deviceId.substring(0,deviceId.indexOf("-"));
                            handler.post(runnableUi);
                        }
                    }).start();
                }

            }
            Runnable   runnableUi=new  Runnable(){
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),resultText,Toast.LENGTH_LONG).show();
                }

            };
        });
        //checkIfCanIncreaseMusic();
        AutoStartPermissionHelper.getInstance().getAutoStartPermission(getApplicationContext());

    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
       AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constant.SELECT_AMAP_PLUGIN_REQUEST_CODE: {
                    int id = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
                    String widgetName=data.getStringExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER);
                    boolean check = false;
                    if (id > 0) {
                        AppWidgetProviderInfo popupWidgetInfo = appWidgetManager.getAppWidgetInfo(id);
                        final View amapView = appWidgetHost.createView(this, id, popupWidgetInfo);
                        View vv = Utils.getViewByIds(amapView, new Object[]{"widget_container", "daohang_container", 0, "gongban_daohang_right_blank_container", "daohang_widget_image"});
                        if (vv instanceof ImageView) {
                            check = true;
                        }
                    }
                    if (check) {
                        PrefUtils.setSelectAmapPluginId(getApplicationContext(),id);
                    } else {
                        PrefUtils.setSelectAmapPluginId(getApplicationContext(),-1);
                    }
                    break;
                }
                default:
                    break;
            }
            TextView selectedAMAPPlugin=findViewById(R.id.textView_AMAPPluginId);
            int selectedPluginId=  PrefUtils.getSelectAMAPPLUGIN(getApplicationContext());
            if(selectedPluginId!=-1){
                selectedAMAPPlugin.setText("已选择高德插件id:"+selectedPluginId);
            } else {
                selectedAMAPPlugin.setText("未选择高德插件");
            }
        }
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        TextView selectedAMAPPlugin=findViewById(R.id.textView_AMAPPluginId);
        int selectedPluginId=  PrefUtils.getSelectAMAPPLUGIN(getApplicationContext());
        if(selectedPluginId!=-1){
            selectedAMAPPlugin.setText("已选择高德插件id:"+selectedPluginId);
        } else {
            selectedAMAPPlugin.setText("未选择高德插件");
        }
        if (hasFocus) {
            invalidateStates();
        }
    }

    private void checkIfCanIncreaseMusic(){
        AudioManager audioManager= (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int systemVolume=audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        int musicVolume=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int voiceVolume=audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        boolean ifSeparatedAudio=false;
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,2,0);
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,1,0);
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,3,0);
        int musicVolumeCurrent=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int voiceVolumeCurrent=audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        if(musicVolumeCurrent==2 && voiceVolumeCurrent==1){
            ifSeparatedAudio=true;
        }
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,systemVolume,0);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,musicVolume,0);
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,voiceVolume,0);
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
        onlyDesktopButton.setEnabled(PrefUtils.isEnableAccessibilityService(getApplicationContext()));
        noDesktopButton.setEnabled(PrefUtils.isEnableAccessibilityService(getApplicationContext()));
        //onAutoNaviButton.setEnabled(PrefUtils.isEnableAccessibilityService(getApplicationContext()));
        boolean serviceReady=Utils.isServiceReady(this);
        CheckBox autoLaunchHotSpotCheckBox=findViewById(R.id.checkBox_autoWifi);
        autoLaunchHotSpotCheckBox.setEnabled(WifiUtils.checkMobileAvalible(getApplicationContext()));
        autoLaunchHotSpotCheckBox.setChecked(PrefUtils.isAutoLauchHotSpot(getApplicationContext()));
        CheckBox roadLineCheckBox=findViewById(R.id.checkBox_roadLine);
        roadLineCheckBox.setEnabled(PrefUtils.getSelectAMAPPLUGIN(getApplicationContext())!=-1);
        CheckBox roadLineFloatingFixedCheckBox=findViewById(R.id.roadline_fixed);
        roadLineFloatingFixedCheckBox.setEnabled(PrefUtils.getSelectAMAPPLUGIN(getApplicationContext())!=-1);
        CheckBox autoWidgetFloatingCheckBox=findViewById(R.id.checkBox_Auto_widget_floating);
        autoWidgetFloatingCheckBox.setEnabled(PrefUtils.getSelectAMAPPLUGIN(getApplicationContext())!=-1);
        CheckBox onlyShowAutoWidgetOnTurnCheckBox=findViewById(R.id.checkBox_Auto_widget_floating_only);
        onlyShowAutoWidgetOnTurnCheckBox.setEnabled(PrefUtils.getSelectAMAPPLUGIN(getApplicationContext())!=-1);
        CheckBox speedRoadLineCheckBox=findViewById(R.id.speed_roadLine);
        speedRoadLineCheckBox.setChecked(PrefUtils.isEnableSpeedRoadLine(getApplicationContext()));
        speedRoadLineCheckBox.setEnabled(PrefUtils.getSelectAMAPPLUGIN(getApplicationContext())!=-1);
        //mAppList
        String selectApps = PrefUtils.getAutoLaunchAppsName(getApplicationContext());
        /*if (mAppList != null && mAppList.size() > 0) {
            selectApps=mAppList.toString();
        }*/
        LinearLayout autoLaunchAppView=findViewById(R.id.autoStartAppView);
        //TextView selectAppsTextView = findViewById(R.id.textView_selectApps);
        //selectAppsTextView.setText("已选择:"+selectApps);
        if(!TextUtils.isEmpty(selectApps)){
            autoLaunchAppView.removeAllViews();
            String[] apps=selectApps.split(",");
            int index=0;
            for(String app:apps){
                TextView textView=new TextView(getApplicationContext());
                textView.setText(app+"   ");
                textView.setId(index);
                textView.setTextColor(Color.BLACK);
                textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                autoLaunchAppView.addView(textView,index++);
            }
        } else {
            autoLaunchAppView.removeAllViews();
        }
        Button btnOk= (Button) findViewById(R.id.confirm);
        if(serviceReady){
            btnOk.setEnabled(true);
        }
        else {
            btnOk.setEnabled(false);
        }
    }
    @OnClick(value = R.id.button_download_offlineMap)
    public void downloadOffLineMap(View view){
        startActivity(new Intent(this.getApplicationContext(),
                com.amap.api.maps.offlinemap.OfflineMapActivity.class));
    }
    private void openSettings(String settingsAction, String packageName) {
        Intent intent = new Intent(settingsAction);
        intent.setData(Uri.parse("package:" + packageName));
        startActivity(intent);
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
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.System.canWrite(this)){
                Intent intentWriteSetting = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                //intentWriteSetting.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intentWriteSetting, 124);
            }
        }

        boolean overlayEnabled = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this);
        if(overlayEnabled && !PrefUtils.isEnableAccessibilityService(getApplicationContext())){

            if(upgradeRootPermission(BuildConfig.APPLICATION_ID)) {
                 Settings.Secure.putString(getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,BuildConfig.APPLICATION_ID+"/"+AppDetectionService.class.getName());
            Settings.Secure.putString(getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED, "1");
                //updateAccessibility(BuildConfig.APPLICATION_ID + "/" + AppDetectionService.class.getName());
            }
        }*/
    }
    private void updateAccessibility(String serviceName) {
        // Parse the enabled services.
        Set<ComponentName> enabledServices = getEnabledServicesFromSettings(getApplicationContext(),serviceName);
        if(null == enabledServices) {
            return;
        }
        // Determine enabled services and accessibility state.
        ComponentName toggledService = ComponentName.unflattenFromString(serviceName);
        final boolean accessibilityEnabled;
        // Enabling at least one service enables accessibility.
        accessibilityEnabled = true;
        enabledServices.add(toggledService);
        // Update the enabled services setting.
        StringBuilder enabledServicesBuilder = new StringBuilder();
        // Keep the enabled services even if they are not installed since we
        // have no way to know whether the application restore process has
        // completed. In general the system should be responsible for the
        // clean up not settings.
        for (ComponentName enabledService : enabledServices) {
            enabledServicesBuilder.append(enabledService.flattenToString());
            enabledServicesBuilder.append(":");
        }
        final int enabledServicesBuilderLength = enabledServicesBuilder.length();
        if (enabledServicesBuilderLength > 0) {
            enabledServicesBuilder.deleteCharAt(enabledServicesBuilderLength - 1);
        }
        Settings.Secure.putString(getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                enabledServicesBuilder.toString());

        // Update accessibility enabled.
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_ENABLED, accessibilityEnabled ? 1 : 0);
    }

    private static Set<ComponentName> getEnabledServicesFromSettings(Context context,String serviceName) {
        String enabledServicesSetting = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabledServicesSetting == null) {
            enabledServicesSetting = "";
        }
        Set<ComponentName> enabledServices = new HashSet<ComponentName>();
        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
        colonSplitter.setString(enabledServicesSetting);
        while (colonSplitter.hasNext()) {
            String componentNameString = colonSplitter.next();
            ComponentName enabledService = ComponentName.unflattenFromString(
                    componentNameString);
            if (enabledService != null) {
                if(enabledService.flattenToString().equals(serviceName)) {
                    return null;
                }
                enabledServices.add(enabledService);
            }
        }
        return enabledServices;
    }
    public static boolean upgradeRootPermission(String pkgCodePath) {
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd="chmod 777 " + pkgCodePath+"; cp -r /data/app/com.huivip.gpsspeedwidget* /system/app/";
            process = Runtime.getRuntime().exec("su"); //切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
           return  process.waitFor()==0;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
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

