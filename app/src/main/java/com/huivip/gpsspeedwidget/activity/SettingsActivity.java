package com.huivip.gpsspeedwidget.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;

import com.huivip.gpsspeedwidget.Constant;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.beans.AutoCheckUpdateEvent;
import com.huivip.gpsspeedwidget.fragment.SettingsBaseFragment;
import com.huivip.gpsspeedwidget.fragment.SettingsMasterFragment;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.util.BackupHelper;
import com.huivip.gpsspeedwidget.util.Definitions;
import com.huivip.gpsspeedwidget.utils.HttpUtils;
import com.nononsenseapps.filepicker.Utils;

import net.gsantner.opoc.util.ContextUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends ThemeActivity implements SettingsBaseFragment.OnPreferenceStartFragmentCallback {
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    public void onCreate(Bundle b) {
        // must be applied before setContentView
        super.onCreate(b);
        ContextUtils contextUtils = new ContextUtils(this);
        contextUtils.setAppLanguage(_appSettings.getLanguage());

        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        toolbar.setTitle(R.string.pref_title__settings);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white_24px));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setBackgroundColor(_appSettings.getPrimaryColor());

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_holder, new SettingsMasterFragment()).commit();

        // if system exit is called the app will open settings activity again
        // this pushes the user back out to the home activity
        if (_appSettings.getAppRestartRequired()) {
            startActivity(new Intent(this, HomeActivity.class));
        }
        //EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void checkUpdate(AutoCheckUpdateEvent event){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                String updateInfo= HttpUtils.getData(Constant.LBSURL+"/updateInfo?type=full");
                Log.d("huivip","check update:"+updateInfo);
                try {
                    if(!TextUtils.isEmpty(updateInfo) && !updateInfo.equalsIgnoreCase("-1")) {
                        String currentVersion= com.huivip.gpsspeedwidget.utils.Utils.getLocalVersion(getApplicationContext());
                        int currentVersionCode=com.huivip.gpsspeedwidget.utils.Utils.getLocalVersionCode(getApplicationContext());
                        JSONObject infoObj = new JSONObject(updateInfo);
                        JSONObject data= (JSONObject) infoObj.get("data");
                        String updateVersion=data.getString("serverVersion");
                        int updateVersionCode=data.getInt("serverVersionCode");
                        Message message = Message.obtain();
                        event.setUpdateIfo(updateInfo);
                        if(event.getHostActivity()==null){
                            event.setHostActivity(HomeActivity._launcher);
                        }
                        message.obj = event;
                        if(currentVersionCode!=0 && updateVersionCode!=0){
                            if(updateVersionCode>currentVersionCode){
                                message.arg1 = 1;
                                AlterHandler.handleMessage(message);
                            } else if(!event.isAutoCheck()) {
                                message.arg1 = 0;
                                AlterHandler.handleMessage(message);
                            }
                        } else {
                            if (currentVersion.equalsIgnoreCase(updateVersion)) {
                                if(!event.isAutoCheck()) {
                                    message.arg1 = 0;
                                    AlterHandler.handleMessage(message);
                                }
                            } else {
                                message.arg1 = 1;
                                AlterHandler.handleMessage(message);
                            }
                        }

                    }
                   /* else {
                        Message message = Message.obtain();
                        message.obj ="";
                        message.arg1 = 0;
                        AlterHandler.handleMessage(message);
                    }*/
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Looper.loop();
            }
        }).start();
    }
    @SuppressLint("HandlerLeak")
    final Handler AlterHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Log.d("huivip","check update:"+msg.arg1);
            if(msg.arg1==0) {
                AlertDialog.Builder  mDialog = new AlertDialog.Builder(new ContextThemeWrapper(((AutoCheckUpdateEvent)(msg.obj)).getHostActivity(),R.style.Theme_AppCompat_DayNight));
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
                            updateInfo = new JSONObject(((AutoCheckUpdateEvent)msg.obj).getUpdateIfo());
                            JSONObject data= (JSONObject) updateInfo.get("data");
                            String updateUrl=data.getString("updateurl");
                            String appName=data.getString("appname");
                            HttpUtils.downLoadApk(((AutoCheckUpdateEvent)(msg.obj)).getHostActivity(),updateUrl,appName);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                mDialog.create().show();
                Log.d("huivip","check update to show dialog:"+msg.arg1);
            }
            else if (msg.arg1==1){
                AlertDialog.Builder  mDialog = new AlertDialog.Builder(new ContextThemeWrapper(((AutoCheckUpdateEvent)(msg.obj)).getHostActivity(),R.style.Theme_AppCompat_DayNight));
                try {
                    JSONObject updateInfo = new JSONObject(((AutoCheckUpdateEvent)msg.obj).getUpdateIfo());
                    JSONObject data= (JSONObject) updateInfo.get("data");
                    mDialog.setTitle("版本升级");
                    mDialog.setMessage(data.getString("upgradeinfo")).setCancelable(true);
                    String updateUrl=data.getString("updateurl");
                    String appName=data.getString("appname");
                    mDialog.setPositiveButton("更新", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            HttpUtils.downLoadApk(((AutoCheckUpdateEvent)(msg.obj)).getHostActivity(),updateUrl,appName);
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
    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference preference) {
        Fragment fragment = Fragment.instantiate(this, preference.getFragment(), preference.getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_holder, fragment)
                .addToBackStack(fragment.getTag())
                .commit();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            List<Uri> files = Utils.getSelectedFilesFromResult(data);
            switch (requestCode) {
                case Definitions.INTENT_BACKUP:
                    BackupHelper.backupConfig(this, new File(Utils.getFileForUri(files.get(0)).getAbsolutePath() + "/gpsWidget.zip").toString());
                    break;
                case Definitions.INTENT_RESTORE:
                    BackupHelper.restoreConfig(this, Utils.getFileForUri(files.get(0)).toString());
                    System.exit(0);
                    break;
                case Definitions.INTENT_LYRIC_PATH:
                    AppSettings.get().setLyricPath(Utils.getFileForUri(files.get(0)).getAbsolutePath() +"/");
                    Log.d("huivip",AppSettings.get().getLyricPath());
                    break;
                case Constant.SELECT_AMAP_PLUGIN_REQUEST_CODE:
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                    AppWidgetHost appWidgetHost = new AppWidgetHost(this, Constant.APP_WIDGET_HOST_ID);
                    int id = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
                    boolean check = false;
                    if (id > 0) {
                       /* AppWidgetProviderInfo popupWidgetInfo = appWidgetManager.getAppWidgetInfo(id);
                        final View amapView = appWidgetHost.createView(this, id, popupWidgetInfo);
                        View vv = com.huivip.gpsspeedwidget.utils.Utils.getViewByIds(amapView, new Object[]{"widget_container", "daohang_container", 0, "gongban_daohang_right_blank_container", "daohang_widget_image"});
                        if (vv instanceof ImageView) {
                            check = true;
                        }
                    }
                    if (check) {*/
                        AppSettings.get().setAmapPluginId(id);
                    } else {
                        AppSettings.get().setAmapPluginId(-1);
                    }
                    break;

            }
        }
    }

}
