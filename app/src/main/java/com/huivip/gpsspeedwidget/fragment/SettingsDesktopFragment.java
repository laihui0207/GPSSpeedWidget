package com.huivip.gpsspeedwidget.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;

import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.util.LauncherAction;

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
        }
        return false;
    }
}
