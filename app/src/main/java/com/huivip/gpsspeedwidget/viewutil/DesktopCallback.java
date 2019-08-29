package com.huivip.gpsspeedwidget.viewutil;

import android.view.View;

import com.huivip.gpsspeedwidget.interfaces.ItemHistory;
import com.huivip.gpsspeedwidget.model.Item;

public interface DesktopCallback extends ItemHistory {
    boolean addItemToPoint(Item item, int x, int y);

    boolean addItemToPage(Item item, int page);

    boolean addItemToCell(Item item, int x, int y);

    void removeItem(View view, boolean animate);
}
