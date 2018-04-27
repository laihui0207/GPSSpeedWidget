package com.huivip.gpsspeedwidget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

@SuppressLint("AppCompatCustomView")
public class SpeedWheel extends ImageView
{
    private Resources mResources;
    Paint mBorderPaint = new Paint(1);

    public SpeedWheel(Context paramContext)
    {
        this(paramContext, null);
    }

    public SpeedWheel(Context paramContext, AttributeSet paramAttributeSet)
    {
        super(paramContext, paramAttributeSet);
        this.mResources = paramContext.getResources();
        this.mBorderPaint.setStrokeWidth(this.mResources.getDisplayMetrics().density * 0.0F);
        this.mBorderPaint.setStyle(Paint.Style.STROKE);
        this.mBorderPaint.setColor(-1);
        this.mBorderPaint.setAlpha(0);
        setImageResource(R.drawable.index_bg_move);
    }
}

