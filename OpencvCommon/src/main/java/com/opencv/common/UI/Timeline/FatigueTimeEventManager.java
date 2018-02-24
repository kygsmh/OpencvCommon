package com.opencv.common.UI.Timeline;

import android.util.Log;

/**
 * Created by Jason_Fang on 2016/9/6.
 */
public class FatigueTimeEventManager extends TimeEventManager<FatigueTimeEvent>
{
    private static final String TAG = "FatigueTimeEventManager";
    private static boolean DEBUG = false;

    private TimeEventManager.StatusLinstener mStatusLinstener = null;
    private FatigueTimeEvent mLatestFatigueTimeEvent = new FatigueTimeEvent(FatigueTimeEvent.TYPE_NORMAL);


    public void setStatusLinstener(TimeEventManager.StatusLinstener<FatigueTimeEvent> listener)
    {
        mStatusLinstener = listener;
    }

    public void processEvent(FatigueTimeEvent newFatigueTimeEvent)
    {
        TimeEventManager.StatusLinstener statusLinstener  = this.mStatusLinstener;

        if (null == newFatigueTimeEvent || null == statusLinstener) {
            if (DEBUG) Log.d(TAG, TAG + " [NG]add filter, newEvent is " + newFatigueTimeEvent + ", statusLinstener = " + statusLinstener);
            return;
        }

        if (null == mLatestFatigueTimeEvent)
        {
            // the first data.
            if (DEBUG) Log.d(TAG, TAG + " AddFilter [first event], (time, type) = (" + newFatigueTimeEvent.getTime() + ", " + newFatigueTimeEvent.getType() + "), first event");
            mLatestFatigueTimeEvent = newFatigueTimeEvent;
            statusLinstener.onStatusChange(newFatigueTimeEvent);

            return;
        }


        if (newFatigueTimeEvent.getTime() > mLatestFatigueTimeEvent.getTime())
        {
            // new event coming.
            if (DEBUG) Log.d(TAG, TAG + " AddFilter [update time], (time, type) = (" + newFatigueTimeEvent.getTime() + ", " + newFatigueTimeEvent.getType() + ")");
            mLatestFatigueTimeEvent = newFatigueTimeEvent;
            statusLinstener.onStatusChange(newFatigueTimeEvent);

            return;
        }
        else if (newFatigueTimeEvent.getTime() == mLatestFatigueTimeEvent.getTime())
        {
            if (newFatigueTimeEvent.getType() > mLatestFatigueTimeEvent.getType())
            {
                // update the more significant event.
                if (DEBUG) Log.d(TAG, TAG + " AddFilter [update type], (time, type) = (" + newFatigueTimeEvent.getTime() + ", " + newFatigueTimeEvent.getType() + ")");
                mLatestFatigueTimeEvent = newFatigueTimeEvent;
                statusLinstener.onStatusChange(newFatigueTimeEvent);

                return;
            }
            else
            {
                // ignore the less important event.
                if (DEBUG) Log.d(TAG, TAG + " AddFilter [ignore type], (time, type) = (" + newFatigueTimeEvent.getTime() + ", " + newFatigueTimeEvent.getType() + ")");
            }
        }
        else
        {
            //ignore. newFatigueTimeEvent.getTime() < mLatestFatigueTimeEvent.getTime()
            if (DEBUG) Log.d(TAG, TAG + " AddFilter [ignore older time], (time, type) = (" + newFatigueTimeEvent.getTime() + ", " + newFatigueTimeEvent.getType() + ")");
        }
    }


}
