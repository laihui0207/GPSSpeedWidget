package com.huivip.gpsspeedwidget.view;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import java.io.File;

public class DigitalView extends AppCompatTextView {
    public DigitalView(Context context) {
        super(context);
    }

    public DigitalView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DigitalView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    private void init(Context context) {
        String file = "fonts" + File.separator + "digital.ttf";

        AssetManager assets = context.getAssets();
        Typeface font = Typeface.createFromAsset(assets, file);
        setTypeface(font,Typeface.BOLD);
    }
}
