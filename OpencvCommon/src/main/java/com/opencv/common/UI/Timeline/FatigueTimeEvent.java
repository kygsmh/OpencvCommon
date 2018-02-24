package com.opencv.common.UI.Timeline;

/**
 * Created by Jason_Fang on 2016/9/4.
 */
public class FatigueTimeEvent extends TimeEvent
{
    public static final int TYPE_INSUFFICIENT_DATA      = 0;
    public static final int TYPE_UNKNOWN                = 1;
    public static final int TYPE_NORMAL                 = 2;
    public static final int TYPE_SEMI_FATIGUE           = 4;
    public static final int TYPE_FATIGUE                = 8;
    public static final int TYPE_TUTORIAL               = 16;

    private int mType = 0;

    public FatigueTimeEvent(int type)
    {
        super();
        mType = type;
    }

    public int getType(){return mType;}

    @Override
    public String toString()
    {
        return "(time, type) = (" + mFormalizedTime + ", " + mType + ")";
    }

    @Override
    public boolean equals(Object others)
    {
        if (this == others) return true;
        if (others == null || getClass() != others.getClass()) return false;

        FatigueTimeEvent othersFatigueTimeEvent = (FatigueTimeEvent) others;

        return (getTime() == othersFatigueTimeEvent.getTime() && getType() == othersFatigueTimeEvent.getType());
    }
}
