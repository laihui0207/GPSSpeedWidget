package com.huivip.gpsspeedwidget.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.text.TextUtils;

import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.activity.HomeActivity;
import com.huivip.gpsspeedwidget.manager.Setup;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.util.Definitions;
import com.huivip.gpsspeedwidget.util.LauncherAction;
import com.huivip.gpsspeedwidget.utils.PrefUtils;
import com.nononsenseapps.filepicker.FilePickerActivity;

import net.gsantner.opoc.util.ContextUtils;
import net.gsantner.opoc.util.PermissionChecker;

public class SettingGPSWidgetFragment extends SettingsBaseFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        addPreferencesFromResource(R.xml.preferences_gps_widget);
    }
    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        HomeActivity homeActivity = HomeActivity._launcher;
        int key = new ContextUtils(homeActivity).getResId(ContextUtils.ResType.STRING, preference.getKey());
        switch (key) {
            case R.string.pref_key__auto_start_launch_select_other_apps:
                LauncherAction.RunAction(LauncherAction.Action.SelectApps, getActivity());
                return true;
            case R.string.pref_key__lyric_path:
                if (new PermissionChecker(getActivity()).doIfExtStoragePermissionGranted()) {
                    Intent i = new Intent(getActivity(), FilePickerActivity.class)
                            .putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true)
                            .putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
                    getActivity().startActivityForResult(i, Definitions.INTENT_LYRIC_PATH);
                }
                return true;
        }
        return false;
    }

    @Override
    public void updateSummaries() {
        Preference pre_delay_time=findPreference(getString(R.string.pref_key__auto_start_launch_other_app_delay_time));
        Preference pre_selectApps=findPreference(getString(R.string.pref_key__auto_start_launch_select_other_apps));
        pre_delay_time.setSummary("延时:"+ AppSettings.get().getDelayTimeBetweenLaunchOtherApp()+"秒");
        String selectApps = PrefUtils.getAutoLaunchAppsName(Setup.appContext());
        if(!TextUtils.isEmpty(selectApps)){
            pre_selectApps.setSummary(selectApps);
           /* String[] apps=selectApps.split(",");
            int index=0;
            for(String app:apps){
                TextView textView=new TextView(getApplicationContext());
                textView.setText(app+"   ");
                textView.setId(index);
                textView.setTextColor(Color.BLACK);
                textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                autoLaunchAppView.addView(textView,index++);
            }*/
        }
        super.updateSummaries();
    }
}
