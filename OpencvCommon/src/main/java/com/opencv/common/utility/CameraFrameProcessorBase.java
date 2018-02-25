package com.opencv.common.utility;

import android.hardware.Camera;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

//import android.util.Log;

/**
 * Created by Jason_Fang on 2017/4/14.
 */

public abstract class CameraFrameProcessorBase implements JavaCameraView.CvCameraViewListener2
{
    private static final String TAG = "CameraFrameProcessor";
    private Mat mCameraBuffer = null;
    private Mat mCameraTranspose = null;
    //private int mCameraIndex = CameraBridgeViewBase.CAMERA_ID_FRONT;
    //private int mCameraFacing = -1;
    private int mOpenedCameraHardwareIndex = -1;
    private Camera.CameraInfo mOpenedCameraInfo = new Camera.CameraInfo();

    public CameraFrameProcessorBase(int openedCameraHardwareIndex)
    {
        mOpenedCameraHardwareIndex = openedCameraHardwareIndex;
        Camera.getCameraInfo(openedCameraHardwareIndex, mOpenedCameraInfo);
    }

    @Override
    public void onCameraViewStarted(int cameraWidth, int cameraHeight)  //this is mFrameWidth/Height ( params.getPreviewSize().width  )
    {
        LogHelper.d(TAG, "[CameraViewImpl][onCameraViewStarted] (cameraWidth, cameraHeight) = (" + cameraWidth + ", " + cameraHeight + ")");
        mCameraBuffer = new Mat(cameraHeight, cameraWidth, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped()
    {
        LogHelper.d(TAG, "[CameraViewImpl][onCameraViewStopped]");
        if (null != mCameraBuffer)
            mCameraBuffer.release();
        if (null != mCameraTranspose)
            mCameraTranspose.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        Mat cameraFrame = inputFrame.rgba();

        if (null == mCameraTranspose)
        {
            mCameraTranspose = new Mat(cameraFrame.cols(), cameraFrame.rows(), cameraFrame.type());
        }

        cameraFrame = onCameraFrameRotated( mOpenedCameraInfo,
                                            cameraFrame,
                                            mCameraTranspose);
        //Face detection.
        // transfer cameraFrame(rgba) to gray Mat in the mCameraBuffer
        Imgproc.cvtColor(cameraFrame, mCameraBuffer, Imgproc.COLOR_RGBA2BGR);
        onProcessFrame(mCameraBuffer, cameraFrame);

        return cameraFrame;
    }



    protected Mat onCameraFrameRotated(Camera.CameraInfo cameraInfo,
                                       Mat srcCameraFrame,
                                       Mat transposeCameraFrame)
    {
        int natureOrientation = OrientationHelper.getInstance().getNatureOrientationDegree();
        int rotation = CameraHelper.getRotationDegree(natureOrientation, mOpenedCameraInfo);
        LogHelper.d(TAG, "[camerarotate] na = " + natureOrientation + ", caculate = " + rotation + ", f = " + (null == mOpenedCameraInfo?null:mOpenedCameraInfo.facing));
        return CameraHelper.rotateClockwise(rotation, cameraInfo.facing, srcCameraFrame, transposeCameraFrame);
        //return srcCameraFrame;
    }



    public abstract void onProcessFrame(Mat srcCameraFrame, Mat dstCameraFrame);


}
