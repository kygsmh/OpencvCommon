package com.opencv.common.UI.Timeline;

/**
 * Created by Jason_Fang on 2016/9/8.
 */
class TimeConfig
{
    public static final int     UNIT_TIME = 1000;   // in millisecond, thie means for each UNIT_TIME will occupy a cell(unit)

    public static final int     SHORT_SEPARATOR_TIME_UNIT = UNIT_TIME;
    public static final int     LONG_SEPARATOR_TIME_UNIT = UNIT_TIME * 10;  //Long must be multiple of short.



    public static long getNowTime()
    {
        //return SystemClock.elapsedRealtime();
        return System.currentTimeMillis();
    }

    public static long getFormatedTime()
    {
        return getFormatedTime(getNowTime());
    }

    public static long getFormatedTime(long time)
    {
        return time - time % TimeConfig.UNIT_TIME;
    }
}
