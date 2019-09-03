package com.huivip.gpsspeedwidget.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

import com.huivip.gpsspeedwidget.AppObject;
import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.manager.Setup;
import com.huivip.gpsspeedwidget.speech.SpeechFactory;
import com.huivip.gpsspeedwidget.widget.AppDrawerController;
import com.huivip.gpsspeedwidget.widget.PagerIndicator;

import net.gsantner.opoc.preference.SharedPreferencesPropertyBackend;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class AppSettings extends SharedPreferencesPropertyBackend {
    public AppSettings(Context context) {
        super(context, "app");
    }

    public static AppSettings get() {
        return new AppSettings(AppObject.get());
    }

    public int getDesktopColumnCount() {
        return getInt(R.string.pref_key__desktop_columns, 5);
    }
    public boolean getAutoStart(){
        return getBool(R.string.pref_key__auto_start,false);
    }
    public boolean isEnableWifiHotpot(){
        return getBool(R.string.pref_key__auto_start_wifi_hotpot,false);
    }
    public boolean isEnablePlayWarnAudio(){
        return getBool(R.string.pref_key__auto_start_play_warn,false);
    }
    public int getDesktopRowCount() {
        return getInt(R.string.pref_key__desktop_rows, 6);
    }

    public int getDesktopIndicatorMode() {
        return getIntOfStringPref(R.string.pref_key__desktop_indicator_style, PagerIndicator.Mode.DOTS);
    }
    public boolean isEnableTimeWindow(){
        return getBool(R.string.pref_key__auto_start_time_window,false);
    }
    public String getTimeWindowDateFormat(){
        return getString(R.string.pref_key__auto_start_time_window_dateFormat,"HH:mm");
    }
    public int getTimeWindowTextColor(){
        return getInt(R.string.pref_key__time_window_font_color,R.color.blue);
    }
    public boolean isEnableXunHang(){
        return getBool(R.string.pref_key__XunHang_enable,false);
    }
    public boolean isEnableDaoHang(){
        return getBool(R.string.pref_key__DaoHang_enable,false);
    }
    public boolean isEnableTracker(){
        return getBool(R.string.pref_key__Tracker_enable,false);
    }
    public boolean isEnableRoadLine(){
        return getBool(R.string.pref_key__Road_line_enable,false);
    }
    public int getAmapPluginId(){
        return getInt(R.string.pref_key__Road_line_plugin_select,-1);
    }
    public void setAmapPluginId(int id){
        setInt(R.string.pref_key__Road_line_plugin_select,id);
    }
    public boolean isShowRoadLineOnSpeed(){
        return getBool(R.string.pref_key__speed_road_line_show,false);
    }
    public boolean isRoadLineFixed(){
        return getBool(R.string.pref_key__road_line_fixed,false);
    }
    public void setRoadLineFixed(boolean value){
        setBool(R.string.pref_key__road_line_fixed,value);
    }
    public boolean isShowAmapWidgetContent(){
        return getBool(R.string.pref_key__Amap_widget_content,false);
    }
    public boolean isOnlyCrossShowWidgetContent(){
        return getBool(R.string.pref_key__Amap_widget_show_only_cross,true);
    }
    public boolean isEnableSpeed(){
        return getBool(R.string.pref_key__speed_enable,false);
    }
    public String getSpeedFlattingStyle(){
        return getString(R.string.pref_key__speed_style,"0");
    }
    public boolean isDefaultSpeedShowSpeed(){
        return getBool(R.string.pref_key__speed_default_speed_show,true);
    }
    public boolean isDefaultSpeedShowLimit(){
        return getBool(R.string.pref_key__speed_default_limit_show,true);
    }
    public boolean isDefaultSpeedhorizontalShow(){
        return getBool(R.string.pref_key__speed_default_horizontal_show,false);
    }
    public boolean isAmapShowLimit(){
        return getBool(R.string.pref_key__speed_AutoMap_limit_show,true);
    }
    public boolean isCloseFlattingOnStop(){
        return getBool(R.string.pref_key__speed_close_stop,false);
    }
    public boolean isCloseFlattingOnAmap(){
        return  getBool(R.string.pref_key__speed_close_Amap,false);
    }
    public boolean isSpeedSmallShow(){
        return  getBool(R.string.pref_key__speed_small_style,false);
    }
    public boolean isSpeedAutoKeepSide(){
        return getBool(R.string.pref_key__speed_auto_keep_side,true);
    }
    public boolean isSpeedMPH(){
        return  getBool(R.string.pref_key__speed_mph,false);
    }
    public int getSpeedAdjust(){
        return getInt(R.string.pref_key__speed_adjust,0);
    }
    public void setLyricPath(String path){
        setString(R.string.pref_key__lyric_path,path);
    }
    public String getLyricPath(){
      return getString(R.string.pref_key__lyric_path, Environment.getExternalStorageDirectory().toString()+"/lyric/");
    }
    public void setLyricEnable(boolean value){
        setBool(R.string.pref_key__lyric_enable,value);
    }
    public boolean isLyricEnable(){
        return getBool(R.string.pref_key__lyric_enable,false);
    }
    public boolean isLyricFixed(){
        return getBool(R.string.pref_key__lyric_fixed,false);
    }
    public void setLyricFixed(boolean value){
        setBool(R.string.pref_key__lyric_fixed,value);
    }
    public int getLyricFontColor(){
        return getInt(R.string.pref_key__lyric_font_color,R.color.blue);
    }
    public boolean isEnableLaunchOtherApp(){
        return getBool(R.string.pref_key__auto_start_launch_other_app_enable,false);
    }
    public boolean isEnableAudio(){
        return getBool(R.string.pref_key__Audio_enable,false);
    }
    public String getAudioEngine(){
        return getString(R.string.pref_key__Audio_engine, SpeechFactory.BAIDUTTS);
    }
    public String getAudioBaiDuSpeaker(){
        return getString(R.string.pref_key__Audio_engine_baidu_speaker,"0");
    }
    public int getAudioVolume(){
        return getInt(R.string.pref_key__Audio_volume,50);
    }
    public boolean isAudioMix(){
        return getBool(R.string.pref_key__Audio_mixed,false);
    }
    public boolean isAudioMusicDuck(){
        return getBool(R.string.pref_key__Audio_music_duck,false);
    }
    public boolean isPlayTime(){
        return getBool(R.string.pref_key__Audio_play_time,true);
    }
    public boolean isPlayWeather(){
        return getBool(R.string.pref_key__Audio_play_weather,true);
    }
    public boolean isPlayAddressOnStop(){
        return getBool(R.string.pref_key__Audio_play_Address,false);
    }
    public boolean isReturnHomeAfterLaunchOtherApp(){
        return getBool(R.string.pref_key__auto_start_launch_other_app_return_desktop,false);
    }
    public int getDelayTimeBetweenLaunchOtherApp(){
        return getIntOfStringPref(R.string.pref_key__auto_start_launch_other_app_delay_time,0);
    }
    public boolean getDesktopRotate() {
        return getBool(R.string.pref_key__desktop_rotate, true);
    }

    public boolean getDesktopShowGrid() {
        return getBool(R.string.pref_key__desktop_show_grid, true);
    }

    public boolean getDesktopFullscreen() {
        return getBool(R.string.pref_key__desktop_fullscreen, false);
    }

    public boolean getDesktopShowIndicator() {
        return getBool(R.string.pref_key__desktop_show_position_indicator, true);
    }

    public boolean getDesktopShowLabel() {
        return getBool(R.string.pref_key__desktop_show_label, true);
    }

    public boolean getSearchBarEnable() {
        return getBool(R.string.pref_key__search_bar_enable, false);
    }

    public String getSearchBarBaseURI() {
        return getString(R.string.pref_key__search_bar_base_uri, R.string.pref_default__search_bar_base_uri);
    }

    public boolean getSearchBarForceBrowser() {
        return getBool(R.string.pref_key__search_bar_force_browser, false);
    }

    public boolean getSearchBarShouldShowHiddenApps() {
        return getBool(R.string.pref_key__search_bar_show_hidden_apps, false);
    }

    @SuppressLint("SimpleDateFormat")
    public SimpleDateFormat getUserDateFormat() {
        String line1 = getString(R.string.pref_key__date_bar_date_format_custom_1, rstr(R.string.pref_default__date_bar_date_format_custom_1));
        String line2 = getString(R.string.pref_key__date_bar_date_format_custom_2, rstr(R.string.pref_default__date_bar_date_format_custom_2));

        try {
            return new SimpleDateFormat((line1 + "'\n'" + line2).replace("''", ""), Locale.getDefault());
        } catch (Exception ex) {
            return new SimpleDateFormat("'Invalid pattern''\n''Invalid Pattern'");
        }
    }

    public int getDesktopDateMode() {
        return getIntOfStringPref(R.string.pref_key__date_bar_date_format_type, 1);
    }

    public int getDesktopDateTextColor() {
        return getInt(R.string.pref_key__date_bar_date_text_color, Color.WHITE);
    }

    public int getDesktopBackgroundColor() {
        return getInt(R.string.pref_key__desktop_background_color, Color.TRANSPARENT);
    }

    public int getDesktopFolderColor() {
        return getInt(R.string.pref_key__desktop_folder_color, Color.WHITE);
    }

    public int getFolderLabelColor() {
        return getInt(R.string.pref_key__desktop_folder_label_color, Color.BLACK);
    }

    public int getDesktopInsetColor() {
        return getInt(R.string.pref_key__desktop_inset_color, ContextCompat.getColor(_context, R.color.transparent));
    }

    public int getMinibarBackgroundColor() {
        return getInt(R.string.pref_key__minibar_background_color, ContextCompat.getColor(_context, R.color.colorPrimary));
    }

    public int getDesktopIconSize() {
        return getIconSize();
    }

    public boolean getDockEnable() {
        return getBool(R.string.pref_key__dock_enable, true);
    }

    public int getDockColumnCount() {
        return getInt(R.string.pref_key__dock_columns, 5);
    }

    public int getDockRowCount() {
        return getInt(R.string.pref_key__dock_rows, 1);
    }

    public boolean getDockShowLabel() {
        return getBool(R.string.pref_key__dock_show_label, false);
    }

    public int getDockColor() {
        return getInt(R.string.pref_key__dock_background_color, Color.TRANSPARENT);
    }

    public int getDockIconSize() {
        return getIconSize();
    }

    public int getDrawerColumnCount() {
        return getInt(R.string.pref_key__drawer_columns, 5);
    }

    public int getDrawerRowCount() {
        return getInt(R.string.pref_key__drawer_rows, 6);
    }

    public int getDrawerStyle() {
        return getIntOfStringPref(R.string.pref_key__drawer_style, AppDrawerController.Mode.GRID);
    }

    public boolean getDrawerShowCardView() {
        return getBool(R.string.pref_key__drawer_show_card_view, true);
    }

    public boolean getDrawerRememberPosition() {
        return getBool(R.string.pref_key__drawer_remember_position, true);
    }

    public boolean getDrawerShowIndicator() {
        return getBool(R.string.pref_key__drawer_show_position_indicator, true);
    }

    public boolean getDrawerShowLabel() {
        return getBool(R.string.pref_key__drawer_show_label, true);
    }

    public int getDrawerBackgroundColor() {
        return getInt(R.string.pref_key__drawer_background_color, rcolor(R.color.darkTransparent));
    }

    public int getDrawerCardColor() {
        return getInt(R.string.pref_key__drawer_card_color, Color.WHITE);
    }

    public int getDrawerLabelColor() {
        return getInt(R.string.pref_key__drawer_label_color, Color.WHITE);
    }

    public int getDrawerFastScrollColor() {
        return getInt(R.string.pref_key__drawer_fast_scroll_color, ContextCompat.getColor(Setup.appContext(), R.color.materialRed));
    }

    public boolean getGestureFeedback() {
        return getBool(R.string.pref_key__gesture_feedback, false);
    }

    public boolean getGestureDockSwipeUp() {
        return getBool(R.string.pref_key__gesture_quick_swipe, true);
    }

    public Object getGestureDoubleTap() {
        return getGesture(R.string.pref_key__gesture_double_tap);
    }

    public Object getGestureSwipeUp() {
        return getGesture(R.string.pref_key__gesture_swipe_up);
    }

    public Object getGestureSwipeDown() {
        return getGesture(R.string.pref_key__gesture_swipe_down);
    }

    public Object getGesturePinch() {
        return getGesture(R.string.pref_key__gesture_pinch);
    }

    public Object getGestureUnpinch() {
        return getGesture(R.string.pref_key__gesture_unpinch);
    }

    public Object getGesture(int key) {
        // return either ActionItem or Intent
        String result = getString(key, "");
        Object gesture = LauncherAction.getActionItem(result);
        // no action was found so it must be an intent string
        if (gesture == null) {
            gesture = Tool.getIntentFromString(result);
            if (AppManager.getInstance(_context).findApp((Intent) gesture) == null) gesture = null;
        }
        // reset the setting if invalid value
        if (gesture == null) {
            setString(key, null);
        }
        return gesture;
    }

    public String getTheme() {
        return getString(R.string.pref_key__theme, "0");
    }

    public int getPrimaryColor() {
        return getInt(R.string.pref_key__primary_color, _context.getResources().getColor(R.color.colorPrimary));
    }

    public int getIconSize() {
        return getInt(R.string.pref_key__icon_size, 48);
    }

    public String getIconPack() {
        return getString(R.string.pref_key__icon_pack, "");
    }

    public void setIconPack(String value) {
        setString(R.string.pref_key__icon_pack, value);
    }

    public int getAnimationSpeed() {
        // invert the value because it is used as a duration
        return 100 - getInt(R.string.pref_key__overall_animation_speed_modifier, 70);
    }

    public String getLanguage() {
        return getString(R.string.pref_key__language, "");
    }

    // internal preferences below here
    public boolean getMinibarEnable() {
        return getBool(R.string.pref_key__minibar_enable, true);
    }

    public void setMinibarEnable(boolean value) {
        setBool(R.string.pref_key__minibar_enable, value);
    }

    public ArrayList<LauncherAction.ActionDisplayItem> getMinibarArrangement() {
        ArrayList<String> minibarString = getStringList(R.string.pref_key__minibar_items);
        ArrayList<LauncherAction.ActionDisplayItem> minibarObject = new ArrayList<>();
        for (String action : minibarString) {
            LauncherAction.ActionDisplayItem item = LauncherAction.getActionItem(action);
            if (item != null) {
                minibarObject.add(item);
            }
        }
        if (minibarObject.isEmpty()) {
            for (LauncherAction.ActionDisplayItem item : LauncherAction.actionDisplayItems) {
                if (LauncherAction.defaultArrangement.contains(item._action)) {
                    minibarObject.add(item);
                }
            }
            setMinibarArrangement(minibarString);
        }
        return minibarObject;
    }

    public void setMinibarArrangement(ArrayList<String> value) {
        setStringList(R.string.pref_key__minibar_items, value);
    }

    public boolean getSearchUseGrid() {
        return getBool(R.string.pref_key__desktop_search_use_grid, false);
    }

    public void setSearchUseGrid(boolean enabled) {
        setBool(R.string.pref_key__desktop_search_use_grid, enabled);
    }

    public ArrayList<String> getHiddenAppsList() {
        return getStringList(R.string.pref_key__hidden_apps);
    }

    public void setHiddenAppsList(ArrayList<String> value) {
        setStringList(R.string.pref_key__hidden_apps, value);
    }

    public int getDesktopPageCurrent() {
        return getInt(R.string.pref_key__desktop_current_position, 0);
    }

    public void setDesktopPageCurrent(int value) {
        setInt(R.string.pref_key__desktop_current_position, value);
    }

    public boolean getDesktopLock() {
        return getBool(R.string.pref_key__desktop_lock, false);
    }

    public void setDesktopLock(boolean value) {
        setBool(R.string.pref_key__desktop_lock, value);
    }

    public boolean getAppRestartRequired() {
        return getBool(R.string.pref_key__queue_restart, false);
    }

    @SuppressLint("ApplySharedPref")
    public void setAppRestartRequired(boolean value) {
        // MUST be committed
        _prefApp.edit().putBoolean(_context.getString(R.string.pref_key__queue_restart), value).commit();
    }

    @SuppressLint("ApplySharedPref")
    public void setAppShowIntro(boolean value) {
        // MUST be committed
        _prefApp.edit().putBoolean(_context.getString(R.string.pref_key__show_intro), value).commit();
    }

    public boolean getAppFirstLaunch() {
        return getBool(R.string.pref_key__first_start, true);
    }

    @SuppressLint("ApplySharedPref")
    public void setAppFirstLaunch(boolean value) {
        // MUST be committed
        _prefApp.edit().putBoolean(_context.getString(R.string.pref_key__first_start), value).commit();
    }
}
