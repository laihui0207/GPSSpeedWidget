package com.huivip.gpsspeedwidget.activity.homeparts;

import android.graphics.Point;

import com.huivip.gpsspeedwidget.R;
import com.huivip.gpsspeedwidget.activity.HomeActivity;
import com.huivip.gpsspeedwidget.interfaces.DialogListener;
import com.huivip.gpsspeedwidget.manager.Setup;
import com.huivip.gpsspeedwidget.model.Item;
import com.huivip.gpsspeedwidget.util.Tool;

public class HpDesktopPickAction implements DialogListener.OnActionDialogListener {
    private HomeActivity _homeActivity;

    public HpDesktopPickAction(HomeActivity homeActivity) {
        _homeActivity = homeActivity;
    }

    public void onPickDesktopAction() {
        Setup.eventHandler().showPickAction(_homeActivity, this);
    }

    @Override
    public void onAdd(int type) {
        Point pos = _homeActivity.getDesktop().getCurrentPage().findFreeSpace();
        if (pos != null) {
            _homeActivity.getDesktop().addItemToCell(Item.newActionItem(type), pos.x, pos.y);
        } else {
            Tool.toast(_homeActivity, R.string.toast_not_enough_space);
        }
    }
}
