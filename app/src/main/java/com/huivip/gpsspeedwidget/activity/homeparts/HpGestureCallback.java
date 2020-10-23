/*
package com.huivip.gpsspeedwidget.activity.homeparts;

import android.content.Intent;
import android.util.Log;

import com.huivip.gpsspeedwidget.activity.HomeActivity;
import com.huivip.gpsspeedwidget.manager.Setup;
import com.huivip.gpsspeedwidget.util.AppSettings;
import com.huivip.gpsspeedwidget.util.LauncherAction;
import com.huivip.gpsspeedwidget.util.Tool;
import com.huivip.gpsspeedwidget.viewutil.DesktopGestureListener;
import com.huivip.gpsspeedwidget.widget.Desktop;

public class HpGestureCallback implements DesktopGestureListener.DesktopGestureCallback {
    private AppSettings _appSettings;

    HpGestureCallback(AppSettings appSettings) {
        _appSettings = appSettings;
    }

    @Override
    public boolean onDrawerGesture(Desktop desktop, DesktopGestureListener.Type event) {
        Object gesture = null;
        switch (event) {
            case SwipeUp:
                gesture = _appSettings.getGestureSwipeUp();
                break;
            case SwipeDown:
                gesture = _appSettings.getGestureSwipeDown();
                break;
            case SwipeLeft:
            case SwipeRight:
                break;
            case Pinch:
                gesture = _appSettings.getGesturePinch();
                break;
            case Unpinch:
                gesture = _appSettings.getGestureUnpinch();
                break;
            case DoubleTap:
                gesture = _appSettings.getGestureDoubleTap();
                break;
            default:
                Log.e(getClass().toString(), "gesture error");
        }
        if (gesture != null) {
            if (_appSettings.getGestureFeedback()) {
                Tool.vibrate(desktop);
            }
            if (gesture instanceof Intent) {
                Intent intent = (Intent) gesture;
                Tool.startApp(HomeActivity._launcher, Setup.appLoader().findApp(intent), null);
            } else if (gesture instanceof LauncherAction.ActionDisplayItem) {
                LauncherAction.RunAction((LauncherAction.ActionDisplayItem) gesture, desktop.getContext());
            }
            return true;
        }
        return false;
    }
}
*/
