package com.huivip.gpsspeedwidget.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.huivip.gpsspeedwidget.R;

public class SettingsDesktopWidgetFragment extends SettingsBaseFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        addPreferencesFromResource(R.xml.preferences_desktop_widget);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        super.onSharedPreferenceChanged(sharedPreferences, key);
    }

    @Override
    public void updateSummaries() {
        super.updateSummaries();
    }
}
