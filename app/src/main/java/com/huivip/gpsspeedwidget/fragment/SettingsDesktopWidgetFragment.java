package com.huivip.gpsspeedwidget.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;

import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.util.AppSettings;

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
        Preference time_timeSize=findPreference(getString(R.string.pref_key__widget_time_font_size));
        time_timeSize.setSummary("字体调整："+ AppSettings.get().getTimeWidgetTimeTextSize());
        Preference time_otherFontSize=findPreference(getString(R.string.pref_key__widget_time_other_font_size));
        time_otherFontSize.setSummary("字体调整："+AppSettings.get().getTimeWidgetOtherTextSize());


    }
}
