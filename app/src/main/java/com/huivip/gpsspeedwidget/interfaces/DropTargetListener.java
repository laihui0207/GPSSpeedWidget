package com.huivip.gpsspeedwidget.interfaces;

import android.graphics.PointF;
import android.view.View;

import com.huivip.gpsspeedwidget.model.Item;
import com.huivip.gpsspeedwidget.util.DragAction;

public interface DropTargetListener {
    View getView();

    boolean onStart(DragAction.Action action, PointF location, boolean isInside);

    void onStartDrag(DragAction.Action action, PointF location);

    void onDrop(DragAction.Action action, PointF location, Item item);

    void onMove(DragAction.Action action, PointF location);

    void onEnter(DragAction.Action action, PointF location);

    void onExit(DragAction.Action action, PointF location);

    void onEnd();
}