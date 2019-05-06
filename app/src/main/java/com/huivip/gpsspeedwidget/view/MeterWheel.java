package com.huivip.gpsspeedwidget.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.huivip.gpsspeedwidget.R;

@SuppressLint("AppCompatCustomView")
public class MeterWheel extends ImageView
{
    private Resources mResources;
    Paint mBorderPaint = new Paint(1);

    public MeterWheel(Context paramContext)
    {
        this(paramContext, null);
    }

    public MeterWheel(Context paramContext, AttributeSet paramAttributeSet)
    {
        super(paramContext, paramAttributeSet);
        this.mResources = paramContext.getResources();
        this.mBorderPaint.setStrokeWidth(this.mResources.getDisplayMetrics().density * 0.0F);
        this.mBorderPaint.setStyle(Paint.Style.STROKE);
        this.mBorderPaint.setColor(-1);
        this.mBorderPaint.setAlpha(0);
        setImageResource(R.drawable.alt_0);
    }
}

