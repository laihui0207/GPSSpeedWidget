package com.huivip.gpsspeedwidget.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.preference.Preference;

import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.activity.MoreInfoActivity;
import com.huivip.gpsspeedwidget.util.AppSettings;

import net.gsantner.opoc.util.ContextUtils;

import java.util.Locale;

import static com.huivip.gpsspeedwidget.widget.AppDrawerController.Mode.GRID;
import static com.huivip.gpsspeedwidget.widget.AppDrawerController.Mode.PAGE;

public class SettingsMasterFragment extends SettingsBaseFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        addPreferencesFromResource(R.xml.preferences_master);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        super.onPreferenceTreeClick(preference);
        int key = new ContextUtils(getContext()).getResId(ContextUtils.ResType.STRING, preference.getKey());
        if (key == R.string.pref_key__about) {
            startActivity(new Intent(getActivity(), MoreInfoActivity.class));
            return true;
        }
        if(key == R.string.pref_key__system_setting) {
            startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
            return true;
        }
        return false;
    }

    @Override
    public void updateSummaries() {
        Preference categoryDesktop = findPreference(getString(R.string.pref_key__cat_desktop));
        Preference categoryDock = findPreference(getString(R.string.pref_key__cat_dock));
        Preference categoryAppDrawer = findPreference(getString(R.string.pref_key__cat_app_drawer));
        Preference categoryAppearance = findPreference(getString(R.string.pref_key__cat_appearance));
        Preference gpsPlugin=findPreference(getString(R.string.pref_key__gps_plugin));

        categoryDesktop.setSummary(String.format(Locale.ENGLISH, "%s: %d x %d", getString(R.string.pref_title__size), AppSettings.get().getDesktopColumnCount(), AppSettings.get().getDesktopRowCount()));
        categoryDock.setSummary(String.format(Locale.ENGLISH, "%s: %d x %d", getString(R.string.pref_title__size), AppSettings.get().getDockColumnCount(), AppSettings.get().getDockRowCount()));
        categoryAppearance.setSummary(String.format(Locale.ENGLISH, "Icons: %ddp", AppSettings.get().getIconSize()));
        gpsPlugin.setSummary(AppSettings.get().getAutoStart() ? "开机启动" : "已关闭");
        switch (AppSettings.get().getDrawerStyle()) {
            case GRID:
                categoryAppDrawer.setSummary(String.format("%s: %s", getString(R.string.pref_title__style), getString(R.string.vertical_scroll_drawer)));
                break;
            case PAGE:
            default:
                categoryAppDrawer.setSummary(String.format("%s: %s", getString(R.string.pref_title__style), getString(R.string.horizontal_paged_drawer)));
                break;
        }
    }
}
