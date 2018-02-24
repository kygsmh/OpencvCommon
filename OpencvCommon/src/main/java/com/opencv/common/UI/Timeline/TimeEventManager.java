package com.opencv.common.UI.Timeline;

/**
 * Created by Jason_Fang on 2016/9/19.
 */
public abstract class TimeEventManager <T extends TimeEvent>
{
    private static final String TAG = "TimeEventManager";
    public interface StatusLinstener<T extends TimeEvent>
    {
        // background thread.
        void onStatusChange(T newBehavior);
    }

    public abstract void setStatusLinstener(TimeEventManager.StatusLinstener<T> listener);

    public abstract void processEvent(T newTimeEvent);
}
