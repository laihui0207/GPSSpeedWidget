package com.huivip.gpsspeedwidget.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.util.AppSettings;

public class SettingsDockFragment extends SettingsBaseFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        addPreferencesFromResource(R.xml.preferences_dock);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        super.onSharedPreferenceChanged(sharedPreferences, key);
        AppSettings.get().setAppRestartRequired(true);

    }
}
