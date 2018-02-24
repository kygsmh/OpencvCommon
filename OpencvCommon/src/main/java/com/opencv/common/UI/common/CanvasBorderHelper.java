package com.opencv.common.UI.common;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by Jason_Fang on 2016/9/14.
 */
public class CanvasBorderHelper
{
    public static void drawBorder (Canvas canvas)
    {
        drawBorder(canvas, 0, 0);
    }
    public static void drawBorder(Canvas canvas, int borderWidth, int borderColor)
    {
        if (0 == borderColor || 0 == borderWidth)
        {
            borderWidth = 10;
            borderColor = 0xff8fdd51; //green
        }

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(borderColor);
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        //  畫四個邊
        // left
        canvas.drawRect(0, 0, borderWidth, height, paint);
        // top
        canvas.drawRect(0, 0, width, borderWidth, paint);
        // right
        canvas.drawRect(width - borderWidth, 0, width, height, paint);
        // down
        canvas.drawRect(0, height - borderWidth, width, height, paint);
    }
}
