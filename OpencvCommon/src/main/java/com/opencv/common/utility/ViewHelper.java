package com.opencv.common.utility;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import org.opencv.core.Size;

//import android.util.Log;

/**
 * Created by Jason_Fang on 2016/9/4.
 */
public class ViewHelper
{
    private static final String TAG = "ViewHelper";
    private static Size mDisplayMetricsSize = null;
    public static float getDensity(Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        LogHelper.d(TAG, "density = " + metrics.density);
        return metrics.density;
    }

    public static float toPixel(Context context, float dp)
    {
        float pixel = dp * getDensity(context);
        return pixel;
    }

    public static float toDp(Context context, float pixel)
    {
        float dp = pixel / getDensity(context);
        return dp;
    }

    public static int getDeviceLongEdge(Context context)
    {
        Size matricsSize = getDisplayMetricsSize(context);
        return Math.max((int)matricsSize.width, (int)matricsSize.height);
    }

    public static int getDeviceShortEdge(Context context)
    {
        Size matricsSize = getDisplayMetricsSize(context);
        return Math.min((int)matricsSize.width, (int)matricsSize.height);
    }

    public static Size getDisplayMetricsSize(Context context)
    {
        synchronized (ViewHelper.class) {
            if (null != mDisplayMetricsSize) {
                LogHelper.d(TAG, "[getScreenResolution] (w, h) = (" + mDisplayMetricsSize.width + ", " + mDisplayMetricsSize.height + ")");
                return mDisplayMetricsSize;
            }

            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            mDisplayMetricsSize = new Size((int)metrics.widthPixels, (int)metrics.heightPixels);
        }
        LogHelper.d(TAG, "[getScreenResolution] (w, h) = (" + mDisplayMetricsSize.width + ", " + mDisplayMetricsSize.height + ")");
        return mDisplayMetricsSize;

    }
}
