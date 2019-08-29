package com.huivip.gpsspeedwidget.interfaces;

import android.view.View;

import com.huivip.gpsspeedwidget.model.Item;

public interface ItemHistory {
    void setLastItem(Item item, View view);

    void revertLastItem();

    void consumeLastItem();
}
