package com.opencv.common.UI.Timeline;

/**
 * Created by Jason_Fang on 2016/9/19.
 */
class TimeEvent
{
    protected long mFormalizedTime = 0;
    public TimeEvent()
    {
        mFormalizedTime = TimeConfig.getFormatedTime();
    }
    public long getTime()
    {
        //This will return formated time in TimeBar
        return mFormalizedTime;
    }

    public void setTime(long time)
    {
        mFormalizedTime = TimeConfig.getFormatedTime(time);
    }

    @Override
    public String toString()
    {
        return "time = " + mFormalizedTime;
    }

    @Override
    public boolean equals(Object others)
    {
        if (this == others) return true;
        if (others == null || getClass() != others.getClass()) return false;

        TimeEvent othersTimeBarEvent = (TimeEvent) others;

        return (getTime() == othersTimeBarEvent.getTime());
    }
}
