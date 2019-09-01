package com.huivip.gpsspeedwidget.fragment;

import android.os.Bundle;

import com.huivip.gpsspeedwidget.R;

public class SettingsDockFragment extends SettingsBaseFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        addPreferencesFromResource(R.xml.preferences_dock);
    }
}
