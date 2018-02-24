package com.opencv.common.UI.util;

import android.content.Context;
import android.view.OrientationEventListener;

/**
 * Created by Jason_Fang on 2016/9/14.
 */
public class OrientationHelper
{
    private static final String TAG = "OrientationHelper";
    public static final int    ILLEGAL_ORIENTATION = -1;
    private static OrientationHelper mInstance = null;
    private static OrientationEventListener     mOrientationEventListener = null;
    private int                                 mCurrentSensorOrientationDegree = ILLEGAL_ORIENTATION;          //0, 90, 270 ,360
    public static final int ROTATION_0 = 0;
    public static final int ROTATION_90 = 90;
    public static final int ROTATION_180 = 180;
    public static final int ROTATION_270 = 270;

    private OrientationHelper(){}

    public static OrientationHelper instance()
    {
        synchronized (OrientationHelper.class)
        {
            if (null == mInstance)
            {
                mInstance = new OrientationHelper();
            }
        }
        return mInstance;
    }

    public void init(Context context)
    {
        synchronized (OrientationHelper.class) {
            if (null == mOrientationEventListener) {
                mOrientationEventListener = new OrientationEventListenerImpl(context.getApplicationContext());
                boolean canDetectOrientation = mOrientationEventListener.canDetectOrientation();
                if (canDetectOrientation)
                {
                    mOrientationEventListener.enable();
                }
                Log.d2(TAG, "[init] canDetectOrientation = " + canDetectOrientation);
            }
        }
    }

    public void deInit()
    {
        synchronized (OrientationHelper.class) {
            if (null != mOrientationEventListener) {
                mOrientationEventListener.disable();
                mOrientationEventListener = null;
                mCurrentSensorOrientationDegree = -1;
                Log.d2(TAG, "[deInit]");
            }
        }
    }

    public int getSensorDirection()
    {
        return mCurrentSensorOrientationDegree;
    }

    private class OrientationEventListenerImpl extends OrientationEventListener
    {
        public OrientationEventListenerImpl(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int natureOrientation)
        {
            /**
             * phone sensor = 0,    nature = 270
             * phone sensor = 90,   nature = 0
             * phone sensor = 180,  nature = 90
             * phone sensor = 270,  nature = 180
             * phone sensor = nature + 90.
             *
             * for example.
             * phone sensor = 0
             * -----------
             * |         | homekey
             * -----------
             *
             * phone sensor = 90
             * |---|
             * |   |
             * |   |
             * |---|
             * homekey
             *
             *
             * */
            if (natureOrientation>=315 || natureOrientation<45){
                mCurrentSensorOrientationDegree = ROTATION_90;
            }else if (natureOrientation>=45 && natureOrientation<135){
                mCurrentSensorOrientationDegree = ROTATION_180;
            }else if (natureOrientation>=135 && natureOrientation<225){
                mCurrentSensorOrientationDegree = ROTATION_270;
            }else if (natureOrientation>=225 && natureOrientation<315){
                mCurrentSensorOrientationDegree = ROTATION_0;
            }
            Log.v(TAG, "[onOrientationChanged] orientation = " + mCurrentSensorOrientationDegree + ", natureOrientation = " + natureOrientation);
        }
    }

}
