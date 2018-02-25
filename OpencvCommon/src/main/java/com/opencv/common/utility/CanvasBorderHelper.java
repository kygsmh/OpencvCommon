package com.opencv.common.utility;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

/**
 * Created by Jason_Fang on 2016/9/14.
 */
public class CanvasBorderHelper
{
    public static void drawBorder (Canvas canvas)
    {
        drawBorder(canvas, 0, 0);
    }

    public static final Paint getDefaultPaint(int paintColor)
    {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(paintColor);
        return paint;
    }

    public static void drawCircle(Canvas canvas, PointF leftTop, PointF rightDown, int borderWidth, int borderColor)
    {
        if (null == canvas || null == leftTop || null == rightDown)
            return;

        if (0 == borderColor || 0 == borderWidth)
        {
            borderWidth = 10;
            borderColor = 0xff8fdd51; //green
        }

        Paint paint = getDefaultPaint(borderColor);
        paint.setStrokeWidth(borderWidth);
        paint.setStyle(Paint.Style.STROKE);
        float centerX = (leftTop.x + rightDown.x)/2.0f;
        float centerY = (leftTop.y + rightDown.y)/2.0f;
        float width = rightDown.x - leftTop.x;
        float height = rightDown.y - leftTop.y;
        float radius = Math.max(width, height) / 2.0f;

        canvas.drawCircle(centerX, centerY, radius, paint);
    }

    public static void drawRect(Canvas canvas, PointF leftTop, PointF rightDown, int borderWidth, int borderColor)
    {
        if (null == canvas || null == leftTop || null == rightDown)
            return;

        if (0 == borderColor || 0 == borderWidth)
        {
            borderWidth = 10;
            borderColor = 0xff8fdd51; //green
        }

        Paint paint = getDefaultPaint(borderColor);
        paint.setStrokeWidth(borderWidth);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(leftTop.x, leftTop.y, rightDown.x, rightDown.y, paint);
    }

    public static void drawBorder(Canvas canvas, int borderWidth, int borderColor)
    {
        if (0 == borderColor || 0 == borderWidth)
        {
            borderWidth = 10;
            borderColor = 0xff8fdd51; //green
        }

        Paint paint = getDefaultPaint(borderColor);
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
