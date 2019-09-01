package com.huivip.gpsspeedwidget.fragment;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.huivip.gpsspeedwidget.DeviceUuidFactory;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.activity.HomeActivity;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.util.DatabaseHelper;
import com.huivip.gpsspeedwidget.util.Definitions;
import com.huivip.gpsspeedwidget.utils.FTPUtils;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.huivip.gpsspeedwidget.viewutil.DialogHelper;
import com.nononsenseapps.filepicker.FilePickerActivity;

import net.gsantner.opoc.util.ContextUtils;
import net.gsantner.opoc.util.PermissionChecker;

import java.io.File;
import java.io.FileFilter;

public class SettingsMiscellaneousFragment extends SettingsBaseFragment {
    private Handler handler = null;
    private String resultText;
    private String deviceId;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        addPreferencesFromResource(R.xml.preferences_miscellaneous);
        handler = new Handler();
        DeviceUuidFactory deviceUuidFactory = new DeviceUuidFactory(getContext());
        deviceId = deviceUuidFactory.getDeviceUuid().toString();
    }

    @Override
    public void updateSummaries() {
        super.updateSummaries();
        Preference uploadLogPre=findPreference(getString(R.string.pref_key__upload_log));
        uploadLogPre.setSummary("上传异常日志给作者，帮助分析系统异常,ID:"+ PrefUtils.getShortDeviceId(getContext()));
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        HomeActivity homeActivity = HomeActivity._launcher;
        int key = new ContextUtils(homeActivity).getResId(ContextUtils.ResType.STRING, preference.getKey());
        switch (key) {
            case R.string.pref_key__backup:
                if (new PermissionChecker(getActivity()).doIfExtStoragePermissionGranted()) {
                    Intent i = new Intent(getActivity(), FilePickerActivity.class)
                            .putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true)
                            .putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
                    getActivity().startActivityForResult(i, Definitions.INTENT_BACKUP);
                }
                return true;
            case R.string.pref_key__restore:
                if (new PermissionChecker(getActivity()).doIfExtStoragePermissionGranted()) {
                    Intent i = new Intent(getActivity(), FilePickerActivity.class)
                            .putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false)
                            .putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                    getActivity().startActivityForResult(i, Definitions.INTENT_RESTORE);
                }
                return true;
            case R.string.pref_key__upload_log: {
                uploadErrorLog();
                return true;
            }
            case R.string.pref_key__reset_settings:
                DialogHelper.alertDialog(getActivity(), getString(R.string.pref_title__reset_settings), getString(R.string.are_you_sure), new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        try {
                            PackageInfo p = getActivity().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
                            String dataDir = p.applicationInfo.dataDir;
                            new File(dataDir + "/shared_prefs/app.xml").delete();
                            System.exit(0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                return true;
            case R.string.pref_key__reset_database:
                DialogHelper.alertDialog(getActivity(), getString(R.string.pref_title__reset_database), getString(R.string.are_you_sure), new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        DatabaseHelper db = HomeActivity._db;
                        db.onUpgrade(db.getWritableDatabase(), 1, 1);
                        AppSettings.get().setAppFirstLaunch(true);
                        System.exit(0);
                    }
                });
                return true;
            case R.string.pref_key__restart:
                homeActivity.recreate();
                getActivity().finish();
                return true;
        }
        return false;
    }

    private void uploadErrorLog() {
        String logDir = Environment.getExternalStorageDirectory().toString() + "/huivip/";

        File dir = new File(logDir);
        if (!dir.exists()) {
            Toast.makeText(getContext(), "没有异常日志需要上传！", Toast.LENGTH_SHORT).show();
        } else if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files.length == 0) {
                Toast.makeText(getContext(), "没有异常日志需要上传！", Toast.LENGTH_SHORT).show();
                return;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    FTPUtils ftp = FTPUtils.getInstance();
                    ftp.initFTPSetting("home.huivip.com.cn", 21, "laihui", "pass");
                    ftp.uploadDirectory("/sda1/gps/" + deviceId.substring(0, deviceId.indexOf("-")), logDir);
                    File dir = new File(logDir);
                    if (dir.exists()) {
                        FileFilter filter = new FileFilter() {
                            @Override
                            public boolean accept(File pathname) {
                                return pathname.isFile() && pathname.getName().indexOf(".log") > -1;
                            }
                        };

                        for (File file : dir.listFiles(filter)) {
                            if (file.exists()) {
                                file.delete();
                            }
                        }
                        dir.delete();
                    }
                    resultText = "日志上传成功，请联系作者查询问题，并提供本机Id:" + deviceId.substring(0, deviceId.indexOf("-"));
                    handler.post(runnableUi);
                }
            }).start();
        }

    }

    Runnable runnableUi = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getContext(), resultText, Toast.LENGTH_LONG).show();
        }

    };
}
