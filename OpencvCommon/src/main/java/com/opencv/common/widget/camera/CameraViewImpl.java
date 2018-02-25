package com.opencv.common.widget.camera;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import com.opencv.common.utility.CameraFrameProcessorBase;
import com.opencv.common.utility.CameraHelper;
import com.opencv.common.utility.IRobotEyeDisplayBitmapCallback;
import com.opencv.common.utility.OrientationHelper;
import com.opencv.common.widget.IFrameCallback;
import com.opencv.common.R;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.util.List;


/**
 * Created by Jason_Fang on 2016/9/14.
 */
public class CameraViewImpl extends CameraViewBase
{
    private static final String TAG = "CameraImpl";

    private IFrameCallback mFrameCallback;                      // call back to the user;
    private CameraHelper mCameraHelper = new CameraHelper();
    private CvCameraViewListener2 mInterceptListener = null;

    private Point mCameraMaxSize = new Point(CameraConstants.CAMERA_MAX_WIDTH, CameraConstants.CAMERA_MAX_HEIGHT);  // x means width, y means height
    private Point mCameraMinSize = new Point(CameraConstants.CAMERA_MIN_WIDTH, CameraConstants.CAMERA_MIN_HEIGHT);

    private boolean mIsDisplayCameraPreview = true;


    class CameraFrameProcessor extends CameraFrameProcessorBase
    {
        public CameraFrameProcessor(int openedCameraHardwareIndex) {
            super(openedCameraHardwareIndex);
        }

        @Override
        public void onProcessFrame(Mat srcCameraFrame, Mat dstCameraFrame) {
            IFrameCallback callback = mFrameCallback;
            if (null != callback)
                callback.onProcessFrame(srcCameraFrame, dstCameraFrame);

        }
    }


    public CameraViewImpl(Context context, int cameraId)
    {
        super(context, cameraId);
        init(context, null);
    }

    public CameraViewImpl(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs)
    {

        // TRANSPARENT
        //setZOrderOnTop(true);  change the z-order will make the view always at the top of whole view.
        setZOrderMediaOverlay(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);


        // initCameraSize
        initCameraSize(context, attrs);
    }

    @Override
    protected boolean connectCamera(int width, int height)
    {
        boolean retval = super.connectCamera(width, height);
        if (retval)
            setCvCameraViewListener(new CameraFrameProcessor(getOpenedCameraHardwareIndex()));
        return retval;
    }

    private void initCameraSize (Context context, AttributeSet attrs)
    {
        TypedArray typedArray = null;
        try {
            typedArray = context.getTheme().obtainStyledAttributes(attrs,
                    R.styleable.RobotEyeView,
                    0, 0);

            mCameraMaxSize.x = typedArray.getInt(R.styleable.RobotEyeView_maxCameraWidth, CameraConstants.CAMERA_MAX_WIDTH);
            mCameraMaxSize.y = typedArray.getInt(R.styleable.RobotEyeView_maxCameraHeight, CameraConstants.CAMERA_MAX_HEIGHT);

            mCameraMinSize.x = typedArray.getInt(R.styleable.RobotEyeView_minCameraWidth, CameraConstants.CAMERA_MIN_WIDTH);
            mCameraMinSize.y = typedArray.getInt(R.styleable.RobotEyeView_minCameraHeight, CameraConstants.CAMERA_MIN_HEIGHT);

            Log.d(TAG, "[initCameraSize] Max = " + mCameraMaxSize.toString());
            Log.d(TAG, "[initCameraSize] Min = " + mCameraMinSize.toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if (null != typedArray)
                typedArray.recycle();
        }
    }



    public void setFrameCallback(IFrameCallback callback)
    {
        synchronized (CameraViewImpl.class) {
            mFrameCallback = callback;
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        super.surfaceCreated(holder);
        if (null != mCameraHelper)
            mCameraHelper.init(getContext());
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        super.surfaceChanged(holder, format, width, height);
        if (null != mCameraHelper)
            mCameraHelper.updateDisplaySize(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        if (null != mCameraHelper)
            mCameraHelper.deInit();
        super.surfaceDestroyed(holder);
    }

    @Override
    public void setCvCameraViewListener(CvCameraViewListener2 listener)
    {
        mInterceptListener = listener;
        super.setCvCameraViewListener(listener);
    }

    @Override
    protected void disconnectCamera()
    {
        super.disconnectCamera();
        mCameraHelper.releaseResource();
    }

    @Override
    protected void deliverAndDrawFrame(CvCameraViewFrame frame)
    {
        Mat modified;

        if (mInterceptListener != null) {
            modified = mInterceptListener.onCameraFrame(frame);
        } else {
            modified = frame.rgba();
        }

        boolean bmpValid = true;
        Bitmap cacheBitmap = null;
        if (modified != null) {
            try {
                cacheBitmap = mCameraHelper.convert(modified);
                Utils.matToBitmap(modified, cacheBitmap);
            } catch(Exception e) {
                android.util.Log.e(TAG, "Mat type: " + modified);
                android.util.Log.e(TAG, "Bitmap type: " + (null == cacheBitmap?null:cacheBitmap.getWidth()) + "*" + (null == cacheBitmap?null:cacheBitmap.getHeight()));
                android.util.Log.e(TAG, "Utils.matToBitmap() throws an exception: " + e.getMessage());
                bmpValid = false;
            }
        }

        if (bmpValid && cacheBitmap != null) {
            Canvas canvas = getHolder().lockCanvas();
            if (canvas != null) {
                // clear bitmap
                canvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR);
                if (mIsDisplayCameraPreview && null != mCameraHelper)
                    mCameraHelper.draw(canvas, cacheBitmap);

                if (mFpsMeter != null) {
                    mFpsMeter.measure();
                    mFpsMeter.draw(canvas, 20, 30);
                }
                getHolder().unlockCanvasAndPost(canvas);
            }
        }
    }


    public void showHideCameraPreview(boolean isShowCameraPreview)
    {
        mIsDisplayCameraPreview = isShowCameraPreview;
    }

    /***
    @Override
    protected Size calculateCameraFrameSize(List<?> supportedSizes, ListItemAccessor accessor, int surfaceWidth, int surfaceHeight)
    {
        setupCameraMaxSizeLimit((List<Camera.Size>)supportedSizes, accessor, surfaceWidth, surfaceHeight);
        Size cameraSize = super.calculateCameraFrameSize(supportedSizes, accessor, mMaxWidth, mMaxHeight);
        return cameraSize;
    }
    **/

    /**
     * This helper method can be called by subclasses to select camera preview size.
     * It goes over the list of the supported preview sizes and selects the maximum one which
     * fits both values set via setMaxFrameSize() and surface frame allocated for this view
     * @param supportedSizes
     * @param surfaceWidth
     * @param surfaceHeight
     * @return optimal frame size
     */
    private static final int MY_MAX_UNSPECIFIED = -1;
    @Override
    protected Size calculateCameraFrameSize(List<?> supportedSizes, ListItemAccessor accessor, int surfaceWidth, int surfaceHeight)
    {
        // 1. setup Max
        setupCameraMaxSizeLimit((List<Camera.Size>)supportedSizes, accessor, surfaceWidth, surfaceHeight);

        // 2. do original behavior
        int calcWidth = 0;
        int calcHeight = 0;

        int maxAllowedWidth = (mMaxWidth != MY_MAX_UNSPECIFIED && mMaxWidth < surfaceWidth)? mMaxWidth : surfaceWidth;
        int maxAllowedHeight = (mMaxHeight != MY_MAX_UNSPECIFIED && mMaxHeight < surfaceHeight)? mMaxHeight : surfaceHeight;

        for (Object size : supportedSizes) {
            int width = accessor.getWidth(size);
            int height = accessor.getHeight(size);

            if (width <= maxAllowedWidth && height <= maxAllowedHeight) {
                if (width >= calcWidth && height >= calcHeight) {
                    calcWidth = (int) width;
                    calcHeight = (int) height;
                }
            }
        }

        Log.d(TAG, "CCF stage1 result = " + calcWidth + " x " + calcHeight);

        // 3. if camera cannot find, try another way to calcuate.
        if (calcWidth <= 0 || calcHeight <= 0)
        {
            calcWidth = 0;
            calcHeight = 0;
            // we cannot find a camera which smaller than the maxAllowed width/height.
            // Instead, try to find a camera which close to the maxAllowed width/height.
            int minDiff = Integer.MAX_VALUE;
            for (Object size : supportedSizes)
            {
                int width = accessor.getWidth(size);
                int height = accessor.getHeight(size);
                int diff = Math.abs((width * height) - (maxAllowedWidth * maxAllowedHeight));
                if (diff < minDiff)
                {
                    minDiff = diff;
                    calcWidth = width;
                    calcHeight = height;
                }
            }
        }
        Log.d(TAG, "CCF stage2 result = " + calcWidth + " x " + calcHeight);

        return new Size(calcWidth, calcHeight);
    }







    private void setupCameraMaxSizeLimit(List<Camera.Size> cameraSupportedSizes, ListItemAccessor accessor, int surfaceWidth, int surfaceHeight)
    {
        Camera.Size size = mCameraHelper.getSuggestCameraSize(  cameraSupportedSizes, accessor,
                                                                mCameraMaxSize.x, mCameraMaxSize.y,
                                                                mCameraMinSize.x, mCameraMinSize.y,
                                                                surfaceWidth, surfaceHeight);
        mMaxWidth = size.width;
        mMaxHeight = size.height;
    }

    @Override
    protected void AllocateCache()
    {
        // Do not remove the function - AllocateCache()
    }

    public void switchCamera()
    {
        disableView();
        if (Camera.CameraInfo.CAMERA_FACING_FRONT == getCameraFacing())
        {
            mCameraIndex = CAMERA_ID_BACK;
        }
        else
        {
            mCameraIndex = CAMERA_ID_FRONT;
        }
        enableView();
    }

    public boolean takePicture (Camera.ShutterCallback shutter,
                      Camera.PictureCallback raw,
                      Camera.PictureCallback jpeg)
    {

        correctCameraRotation();
        if (null != mCamera) {
            mCamera.takePicture(shutter, raw, jpeg);
            return true;
        }
        else
            return false;
    }

    private boolean correctCameraRotation()
    {
        int degree = CameraHelper.getRotationDegree(OrientationHelper.getInstance().getNatureOrientationDegree(), mOpenedCameraInfo);
        if (degree == OrientationHelper.ILLEGAL_ORIENTATION) {
            Log.e(TAG, "rotation degree NG.");
            return false;
        }

        Camera camera = mCamera;
        if (null == camera) {
            Log.e(TAG, "mCamera NG.");
            return false;
        }

        Camera.Parameters parameters = camera.getParameters();
        if (null == parameters)
        {
            Log.e(TAG, "mCamera.getParameters() NG.");
            return false;
        }

        parameters.setRotation(degree);
        Log.d(TAG, "[correctCameraRotation] degree = " + degree);
        camera.setParameters(parameters);
        return true;
    }

    /*********************************************************************************************
     *
     *  RobotVision -> update face location to RobotEye
     *  RobotEye -> translate location and callback the translated coordinate to user
     *
     **/

    public void updateFaceLocation(long currentTimeStamp, PointF leftTop, PointF rightDown)
    {
        if (null != mCameraHelper)
            mCameraHelper.updateFaceLocation(currentTimeStamp, leftTop, rightDown);
    }

    public void enableFaceCoordinateUpdate()
    {
        /**
         * Retrieve coordinate from display bitmap.
         * */
        mCameraHelper.setRobotEyeDisplayBitmapCallback(new IRobotEyeDisplayBitmapCallback()
        {
            @Override
            public void onFaceCoordinateUpdate(PointF leftTop, PointF rightDown)
            {
                IFrameCallback callback = mFrameCallback;
                if (null != callback)
                {
                    callback.onRobotEyeFaceCoordinateUpdate(leftTop, rightDown);
                }
            }
        });
        //mFrameCallback.onRobotEyeFaceCoordinateUpdate();
    }

    /**
     *
     *  RobotVision -> update face location to RobotEye
     *  RobotEye -> translate location and callback the translated coordinate to user
     *
     *********************************************************************************************/


}
