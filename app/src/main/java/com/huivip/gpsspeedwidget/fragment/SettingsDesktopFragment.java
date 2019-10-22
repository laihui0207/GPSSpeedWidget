package com.huivip.gpsspeedwidget.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.preference.Preference;

import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.util.LauncherAction;
import com.huivip.gpsspeedwidget.utils.Utils;

import net.gsantner.opoc.util.ContextUtils;

public class SettingsDesktopFragment extends SettingsBaseFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        addPreferencesFromResource(R.xml.preferences_desktop);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        super.onSharedPreferenceChanged(sharedPreferences, key);
        AppSettings.get().setAppRestartRequired(true);

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        int key = new ContextUtils(getContext()).getResId(ContextUtils.ResType.STRING, preference.getKey());
        switch (key) {
            case R.string.pref_key__minibar:
                LauncherAction.RunAction(LauncherAction.Action.EditMinibar, getActivity());
                return true;
            case R.string.pref_key__default_home:
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    final Intent intent = new Intent(Settings.ACTION_HOME_SETTINGS);
                    startActivity(intent);
                }
                else {
                    final Intent intent = new Intent(Settings.ACTION_SETTINGS);
                    startActivity(intent);
                }
                return true;
        }
        return false;
    }

    @Override
    public void updateSummaries() {
        super.updateSummaries();
        Preference defaultHome=findPreference(getString(R.string.pref_key__default_home));
        defaultHome.setSummary("当前已选择:"+Utils.getDefaultDesktopName(getContext()));
    }
}
