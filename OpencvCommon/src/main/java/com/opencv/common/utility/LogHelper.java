package com.opencv.common.utility;

import android.util.Log;

/**
 * Created by Jason_Fang on 2017/11/23.
 */

public class LogHelper {

    private static int mCurrentLogLevel = Log.WARN;

    public static void setLogLevel(int logLevel)
    {
        mCurrentLogLevel = logLevel;
    }

    public static int getLogLevel(){
        return mCurrentLogLevel;
    }

    public static boolean isLoggable(String tag, int checkLevel)
    {
        if (null == tag || tag.length() > 23)
        {
            tag = tag.substring(0, 23);
        }

        boolean isLoggable = android.util.Log.isLoggable(tag, mCurrentLogLevel);
        return isLoggable && (checkLevel > mCurrentLogLevel);
    }

    public static int v(String tag, String msg)
    {
        return (isLoggable(tag, Log.VERBOSE)?android.util.Log.v(tag, msg):0);
    }


    public static int v(String tag, String msg, Throwable tr) {
        return (isLoggable(tag, Log.VERBOSE)?android.util.Log.v(tag, msg, tr):0);
    }


    public static int d(String tag, String msg) {
        return (isLoggable(tag, Log.DEBUG)?android.util.Log.d(tag, msg):0);
    }


    public static int d(String tag, String msg, Throwable tr) {
        return (isLoggable(tag, Log.DEBUG)?android.util.Log.d(tag, msg, tr):0);
    }


    public static int i(String tag, String msg) {
        return (isLoggable(tag, Log.INFO)?android.util.Log.i(tag, msg):0);
    }


    public static int i(String tag, String msg, Throwable tr) {
        return (isLoggable(tag, Log.INFO)?android.util.Log.i(tag, msg, tr):0);
    }


    public static int w(String tag, String msg) {
        return (isLoggable(tag, Log.WARN)?android.util.Log.w(tag, msg):0);
    }


    public static int w(String tag, String msg, Throwable tr) {
        return (isLoggable(tag, Log.WARN)?android.util.Log.w(tag, msg, tr):0);
    }

    public static int w(String tag, Throwable tr) {
        return (isLoggable(tag, Log.WARN)?android.util.Log.w(tag, tr):0);
    }

    public static int e(String tag, String msg) {
        return (isLoggable(tag, Log.ERROR)?android.util.Log.e(tag, msg):0);
    }


    public static int e(String tag, String msg, Throwable tr) {
        return (isLoggable(tag, Log.ERROR)?android.util.Log.e(tag, msg, tr):0);
    }

    public static int wtf(String tag, String msg) {
        return (isLoggable(tag, Log.ERROR)?android.util.Log.wtf(tag, msg):0);
    }
}
