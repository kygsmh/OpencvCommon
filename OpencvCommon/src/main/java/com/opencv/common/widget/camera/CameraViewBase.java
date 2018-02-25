package com.opencv.common.widget.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;

import com.opencv.common.opencv.JavaCameraViewEnhanced_3_2_0;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.util.List;

/**
 * Created by Jason_Fang on 2017/5/4.
 */

public class CameraViewBase extends JavaCameraViewEnhanced_3_2_0
{
    private static final String TAG = "CameraViewBase";

    private int mOpenedCameraHardwareIndex = -1;
    protected Camera.CameraInfo mOpenedCameraInfo = null;
    //private int mCameraFacing = -1;

    public CameraViewBase(Context context, int cameraId) {
        super(context, cameraId);
    }

    public CameraViewBase(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    /**
     * Override the camera open rule.
     * User can assign the camera but if open fail, it will try to find a best camera to open.
     * */

    protected boolean initializeCamera(int width, int height) {
        Log.d(TAG, "Initialize java camera");
        boolean result = true;
        synchronized (this) {
            mCamera = null;

            if (mCameraIndex == CAMERA_ID_ANY) {
                Log.d(TAG, "Trying to open camera with old open()");
                mCamera = openAWorkableCamera();
//                try {
//                    mCamera = Camera.open();
//                }
//                catch (Exception e){
//                    Log.e(TAG, "Camera is not available (in use or does not exist): " + e.getLocalizedMessage());
//                }
//
//                if(mCamera == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
//                    boolean connected = false;
//                    for (int camIdx = 0; camIdx < Camera.getNumberOfCameras(); ++camIdx) {
//                        Log.d(TAG, "Trying to open camera with new open(" + Integer.valueOf(camIdx) + ")");
//                        try {
//                            mCamera = Camera.open(camIdx);
//                            connected = true;
//                        } catch (RuntimeException e) {
//                            Log.e(TAG, "Camera #" + camIdx + "failed to open: " + e.getLocalizedMessage());
//                        }
//                        if (connected) break;
//                    }
//                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    int localCameraIndex = mCameraIndex;
                    if (mCameraIndex == CAMERA_ID_BACK) {
                        Log.i(TAG, "Trying to open back camera");
                        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                        for (int camIdx = 0; camIdx < Camera.getNumberOfCameras(); ++camIdx) {
                            Camera.getCameraInfo( camIdx, cameraInfo );
                            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                                localCameraIndex = camIdx;
                                break;
                            }
                        }
                    } else if (mCameraIndex == CAMERA_ID_FRONT) {
                        Log.i(TAG, "Trying to open front camera");
                        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                        for (int camIdx = 0; camIdx < Camera.getNumberOfCameras(); ++camIdx) {
                            Camera.getCameraInfo( camIdx, cameraInfo );
                            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                                localCameraIndex = camIdx;
                                break;
                            }
                        }
                    }
                    if (localCameraIndex == CAMERA_ID_BACK) {
                        Log.e(TAG, "Back camera not found!");
                    } else if (localCameraIndex == CAMERA_ID_FRONT) {
                        Log.e(TAG, "Front camera not found!");
                    } else {
                        Log.d(TAG, "Trying to open camera with new open(" + Integer.valueOf(localCameraIndex) + ")");
                        try {
                            mCamera = Camera.open(localCameraIndex);
                            mOpenedCameraHardwareIndex = localCameraIndex;
                            saveCameraInfo(mOpenedCameraHardwareIndex);
                        } catch (RuntimeException e) {
                            Log.e(TAG, "Camera #" + localCameraIndex + "failed to open: " + e.getLocalizedMessage());
                        }
                    }
                }
            }

            if (mCamera == null)
                return false;

            /* Now set camera parameters */
            try {
                Camera.Parameters params = mCamera.getParameters();
                Log.d(TAG, "getSupportedPreviewSizes()");
                List<Camera.Size> sizes = params.getSupportedPreviewSizes();

                if (sizes != null) {
                    /* Select the size that fits surface considering maximum size allowed */
                    Size frameSize = calculateCameraFrameSize(sizes, new JavaCameraSizeAccessor(), width, height);

                    params.setPreviewFormat(ImageFormat.NV21);
                    Log.d(TAG, "Set preview size to " + Integer.valueOf((int)frameSize.width) + "x" + Integer.valueOf((int)frameSize.height));
                    params.setPreviewSize((int)frameSize.width, (int)frameSize.height);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && !Build.MODEL.equals("GT-I9100"))
                        params.setRecordingHint(true);

                    List<String> FocusModes = params.getSupportedFocusModes();
                    if (FocusModes != null && FocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
                    {
                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    }

                    mCamera.setParameters(params);
                    params = mCamera.getParameters();

                    mFrameWidth = params.getPreviewSize().width;
                    mFrameHeight = params.getPreviewSize().height;

                    if ((getLayoutParams().width == ViewGroup.LayoutParams.MATCH_PARENT) && (getLayoutParams().height == ViewGroup.LayoutParams.MATCH_PARENT))
                        mScale = Math.min(((float)height)/mFrameHeight, ((float)width)/mFrameWidth);
                    else
                        mScale = 0;

                    if (mFpsMeter != null) {
                        mFpsMeter.setResolution(mFrameWidth, mFrameHeight);
                    }

                    int size = mFrameWidth * mFrameHeight;
                    size  = size * ImageFormat.getBitsPerPixel(params.getPreviewFormat()) / 8;
                    mBuffer = new byte[size];

                    mCamera.addCallbackBuffer(mBuffer);
                    mCamera.setPreviewCallbackWithBuffer(this);

                    mFrameChain = new Mat[2];
                    mFrameChain[0] = new Mat(mFrameHeight + (mFrameHeight/2), mFrameWidth, CvType.CV_8UC1);
                    mFrameChain[1] = new Mat(mFrameHeight + (mFrameHeight/2), mFrameWidth, CvType.CV_8UC1);

                    AllocateCache();

                    mCameraFrame = new JavaCameraFrame[2];
                    mCameraFrame[0] = new JavaCameraFrame(mFrameChain[0], mFrameWidth, mFrameHeight);
                    mCameraFrame[1] = new JavaCameraFrame(mFrameChain[1], mFrameWidth, mFrameHeight);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        mSurfaceTexture = new SurfaceTexture(MAGIC_TEXTURE_ID);
                        mCamera.setPreviewTexture(mSurfaceTexture);
                    } else
                        mCamera.setPreviewDisplay(null);

                    /* Finally we are ready to start the preview */
                    Log.d(TAG, "startPreview");
                    mCamera.startPreview();
                }
                else
                    result = false;
            } catch (Exception e) {
                result = false;
                e.printStackTrace();
            }
        }

        return result;
    }

    private void saveCameraInfo(int openedCameraHardwareIndex) {
        mOpenedCameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(openedCameraHardwareIndex, mOpenedCameraInfo);
    }

    protected Camera openAWorkableCamera()
    {
        if (null != mCamera)
            return mCamera;

        Camera camera = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

        // 1. find the front camera first.
        for (int camIdx = 0; camIdx < Camera.getNumberOfCameras(); ++camIdx)
        {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
            {
                camera = openCameraWithoutException(camIdx);
                if (null != camera)
                {
                    mOpenedCameraHardwareIndex = camIdx;
                    saveCameraInfo(mOpenedCameraHardwareIndex);
                    return camera;
                }
            }
        }

        // 2. find the back camera then.
        for (int camIdx = 0; camIdx < Camera.getNumberOfCameras(); ++camIdx) {
            Camera.getCameraInfo( camIdx, cameraInfo );
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                camera = openCameraWithoutException(camIdx);
                if (null != camera)
                {
                    mOpenedCameraHardwareIndex = camIdx;
                    saveCameraInfo(mOpenedCameraHardwareIndex);
                    return camera;
                }
                break;
            }
        }

        return null;
    }

    private Camera openCameraWithoutException(int cameraIndex)
    {
        Camera camera = null;
        try
        {
            camera = Camera.open(cameraIndex);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            camera = null;
        }
        return camera;
    }

    protected int getCameraFacing()
    {
        if (null != mOpenedCameraInfo)
            return mOpenedCameraInfo.facing;
        else
            return -1;
    }

    protected int getOpenedCameraHardwareIndex()
    {
        return mOpenedCameraHardwareIndex;
    }
}
