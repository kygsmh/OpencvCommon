package com.opencv.common.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.hardware.Camera;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.util.List;

import static android.view.OrientationEventListener.ORIENTATION_UNKNOWN;

/**
 * Created by Jason_Fang on 2017/4/14.
 */

public class CameraHelper
{
    public static final boolean DEBUG_CAMERA = false;
    public static final int CAMERA_BITMAP_NO_SCALE          = 0;
    public static final int CAMERA_BITMAP_FIT_SHORT_EADGE   = 1 + CAMERA_BITMAP_NO_SCALE;
    public static final int CAMERA_BITMAP_FIT_LONG_EADGE    = 2 + CAMERA_BITMAP_NO_SCALE;

    private DisplayBitmapHelper mDisplayBitmapHelper = new DisplayBitmapHelper();

// Let app decide which lib they want to use.
//    private static final String LIB_NAME_OPEN_CV = "opencv_java3";
//    static
//    {
//        try {
//            System.loadLibrary(LIB_NAME_OPEN_CV);
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//        catch (Error e)
//        {
//            e.printStackTrace();
//        }
//    }


    public CameraHelper(){}

    public void init(Context context)
    {
        OrientationHelper.getInstance().init(context);
        mDisplayBitmapHelper.init();
    }

    public void deInit()
    {
        mDisplayBitmapHelper.deInit();
        OrientationHelper.getInstance().deInit();
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Camera frame rotate helper
    //

    // flipCode=0 means flip x-axis
    // flipCode=1 means flip y-axis
    // flipCode=-1 means flip x and y axis.
    public static Mat rotateClockwise(int orientation, int facing, Mat srcMat, Mat transposeMat)
    {
        switch (orientation)
        {
            case OrientationHelper.ROTATION_0:
            {
                if (Camera.CameraInfo.CAMERA_FACING_BACK == facing)        // front camera need to flip y for any degree.
                {
                    // nothing
                }
                else
                {
                    //Front camera
                    Core.flip(srcMat, srcMat, 1);   // front camera need to flip y for any degree.
                }
                return srcMat;
            }
            case OrientationHelper.ROTATION_90:
            {
                Core.transpose(srcMat, transposeMat);

                if (Camera.CameraInfo.CAMERA_FACING_BACK == facing)
                {
                    // back camera need to flip y
                    Core.flip(transposeMat, transposeMat, 1);
                }
                else
                {
                    // back need to flip y. So flip y and y then do nothing.
                }
                return transposeMat;
            }
            case OrientationHelper.ROTATION_180:
            {
                if (Camera.CameraInfo.CAMERA_FACING_BACK == facing)
                {
                    Core.flip(srcMat, srcMat, -1);      // flip x + y
                }
                else
                {
                    Core.flip(srcMat, srcMat, 0);       // base on front, flip x + y and flip y then flip x
                }

                return srcMat;
            }
            case OrientationHelper.ROTATION_270:
            {
                Core.transpose(srcMat, transposeMat);

                if (Camera.CameraInfo.CAMERA_FACING_BACK == facing)
                {
                    Core.flip(transposeMat, transposeMat, 0);   // flip x
                }
                else
                {
                    Core.flip(transposeMat, transposeMat, -1);  // flip x + y
                }
                return transposeMat;
            }
            default:
            {
                return srcMat;
            }
        }
    }



//    // flipCode=0 means flip x-axis
//    // flipCode=1 means flip y-axis
//    // flipCode=-1 means flip x and y axis.
//    // when do transpose the pics will rotate reverse clockwise 90.
//    public static Mat rotateBackCamera(Mat srcMat, Mat transposeMat)
//    {
//        int currentSensorOrientation = OrientationHelper.getInstance().getSensorDirection();
//
//        switch (currentSensorOrientation)
//        {
//            case OrientationHelper.ROTATION_0:
//            {
//                return srcMat;
//            }
//            case OrientationHelper.ROTATION_90:
//            {
//                Core.transpose(srcMat, transposeMat);
//                Core.flip(transposeMat, transposeMat, 1);
//                return transposeMat;
//            }
//            case OrientationHelper.ROTATION_180:
//            {
//                Core.flip(srcMat, srcMat, -1);
//                return srcMat;
//            }
//            case OrientationHelper.ROTATION_270:
//            {
//                Core.transpose(srcMat, transposeMat);
//                Core.flip(transposeMat, transposeMat, 0);
//                return transposeMat;
//            }
//            default:
//            {
//                return srcMat;
//            }
//        }
//    }
//
//    // flipCode=0 means flip x-axis
//    // flipCode=1 means flip y-axis
//    // flipCode=-1 means flip x and y axis.
//    // when do transpose the pics will rotate reverse clockwise 90.
//    public static Mat rotateFrontCamera(Mat srcMat, Mat transposeMat)
//    {
//        int currentSensorOrientation = OrientationHelper.getInstance().getSensorDirection();
//        switch (currentSensorOrientation)
//        {
//            case OrientationHelper.ROTATION_0:
//            {
//                Core.flip(srcMat, srcMat, 1);
//                return srcMat;
//            }
//            case OrientationHelper.ROTATION_90:
//            {
//                Core.transpose(srcMat, transposeMat);
//                Core.flip(transposeMat, transposeMat, -1);
//                return transposeMat;
//            }
//            case OrientationHelper.ROTATION_180:
//            {
//                Core.flip(srcMat, srcMat, 0);
//                return srcMat;
//            }
//            case OrientationHelper.ROTATION_270:
//            {
//                Core.transpose(srcMat, transposeMat);
//                return transposeMat;
//            }
//            default:
//            {
//                return srcMat;
//            }
//        }
//    }

    //
    // Camera frame rotate helper
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public Camera.Size getSuggestCameraSize(    List<Camera.Size> cameraSupportedSizes,
                                              CameraBridgeViewBase.ListItemAccessor accessor,
                                              int maxCameraWidth,
                                              int maxCameraHeight,
                                              int minCameraWidth,
                                              int minCameraHeight,
                                              int cameraViewWidth,
                                              int cameraViewHeight)
    {
        return CameraSizeHelper.getSuggestCameraSize(cameraSupportedSizes,
                accessor,
                maxCameraWidth,
                maxCameraHeight,
                minCameraWidth,
                minCameraHeight,
                cameraViewWidth,
                cameraViewHeight);
    }







    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // DisplayBitmapHelper
    //


    public void updateDisplaySize(int width, int height)
    {
        if (null != mDisplayBitmapHelper)
            mDisplayBitmapHelper.updateDisplaySize(width, height);
    }


    public Bitmap convert(Mat modifiedMat)
    {
        if (null != mDisplayBitmapHelper)
            return mDisplayBitmapHelper.converMat2Bitmap(modifiedMat);
        else
            return null;
    }

    public void draw(Canvas canvas, Bitmap cacheBitmap)
    {
        if (null != mDisplayBitmapHelper)
            mDisplayBitmapHelper.drawBitmap(canvas, cacheBitmap);
    }

    public void releaseResource()
    {
        if (null != mDisplayBitmapHelper)
            mDisplayBitmapHelper.releaseBitmapPool();
    }
    //
    // DisplayBitmapHelper
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////




    /**
     *
     * NatureDegree                 0       90      180     270
     * getRotationDegree(Back)      0       90      180     270
     * getRotationDegree(Front)     0       270     180     90
     * */
    public static int getRotationDegree(int orientation, android.hardware.Camera.CameraInfo cameraInfo)
    {
        if (orientation == ORIENTATION_UNKNOWN) return 0;

        orientation = (orientation + 45) / 90 * 90;
        int rotation = 0;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (cameraInfo.orientation - orientation + 360) % 360;
        } else {  // back-facing camera
            rotation = (cameraInfo.orientation + orientation) % 360;
        }
        return rotation;
    }


    /*********************************************************************************************
     *
     *  RobotVision -> update face location to RobotEye
     *  RobotEye -> translate location and callback the translated coordinate to user
     *
     **/

    public void updateFaceLocation(long currentTimeStamp, PointF leftTop, PointF rightDown)
    {
        DisplayBitmapHelper displayBitmapHelper = mDisplayBitmapHelper;
        if (null != displayBitmapHelper){
            displayBitmapHelper.updateFaceLocation(currentTimeStamp, leftTop, rightDown);
        }
    }

    public void setRobotEyeDisplayBitmapCallback(IRobotEyeDisplayBitmapCallback iRobotEyeDisplayBitmapCallback) {
        DisplayBitmapHelper displayBitmapHelper = mDisplayBitmapHelper;
        if (null != displayBitmapHelper){
            displayBitmapHelper.setRobotEyeDisplayBitmapCallback(iRobotEyeDisplayBitmapCallback);
        }
    }


    /**
     *
     *  RobotVision -> update face location to RobotEye
     *  RobotEye -> translate location and callback the translated coordinate to user
     *
     *********************************************************************************************/

}
