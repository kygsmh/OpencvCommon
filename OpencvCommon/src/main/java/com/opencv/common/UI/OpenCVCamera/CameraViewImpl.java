package com.opencv.common.UI.OpenCVCamera;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

import com.opencv.common.UI.common.CanvasBorderHelper;
import com.opencv.common.UI.common.ViewHelper;
import com.opencv.common.UI.util.Log;
import com.opencv.common.UI.util.OrientationHelper;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import opencv_3_1_0.org.opencv.android.JavaCameraView310Enhance;

/**
 * Created by Jason_Fang on 2016/9/14.
 */
public class CameraViewImpl extends JavaCameraView310Enhance implements JavaCameraView310Enhance.CvCameraViewListener2
{
    private static final String TAG = "CameraImpl";
    private static final String PREF_KEY_CAMERA_SETTING = "pref_key_camera_setting";
    private static final String PREF_KEY_CAMERA_WIDTH = "pref_key_camera_width";
    private static final String PREF_KEY_CAMERA_HEIGHT = "pref_key_camera_height";


    private CvCameraViewListener2 mInterceptListener = null;    // the same instance with CameraBridgeViewBase_3_1_0.mListener
    private Size mSurfaceSize = new Size();                     // generally this would be the layout size.
    private Size mDisplayBitmapWithoutScaleSize = new Size();   // This will follow the orientation. The size did not contain scale.
    private double mScaleValue = 1;                             // Deprecated mScale and do NOT use it. The mScaleValue = 1 means no scale.
    private int mScaleStrategy = CameraViewConstants.CAMERA_BITMAP_FIT_LONG_EADGE;

    private HashMap<String, Bitmap> mBitmapPool = new HashMap<String, Bitmap>(3);   //key = width x height, value = bitmap

    private Point mOrientationMatTouchPoint = new Point(0, 0);         // the point is correct to the lib which coordinate is the same with the input for the lib.
    private boolean mResetSwitch = false;

    private float mCameraSizeRatio = 0.625f;
    private IProcessCallback mProcessCallback = null;

    static
    {
        try {
            System.loadLibrary("opencv_java");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        catch (Error e)
        {
            e.printStackTrace();
        }
    }

    public CameraViewImpl(Context context, int cameraId)
    {
        super(context, cameraId);
        init(context);
    }

    public CameraViewImpl(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    public void init(Context context)
    {
        Log.d2(TAG, "[CameraViewImpl][init]");
        setVisibility(View.VISIBLE);
        setCvCameraViewListener(this);
        setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);     //use layout attribute.
        setOnTouchListener(new CameraViewTouchListener());

        // let view could be overlap
        setZOrderMediaOverlay(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);

        // use default
        mCameraSizeRatio = 0.625f;   //Float.valueOf(context.getString(R.string.camera_size_relative_to_device_long_edge));

    }

    public void setProcessCallback(IProcessCallback eyeTracker)
    {
        mProcessCallback = eyeTracker;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // surface holder callback
    //

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        super.surfaceChanged(holder, format, width, height);
        mSurfaceSize.width = width;
        mSurfaceSize.height = height;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
    }

    //
    // surface holder callback
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////



    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // opencv_3_1_0.org.opencv.android.JavaCameraView_3_1_0.CvCameraViewListener2
    //

//    private Mat mCameraRgbaBuffer = null;
    private Mat mCameraGrayBuffer = null;
    private Mat mCameraTranspose = null;

    @Override
    public void onCameraViewStarted(int cameraWidth, int cameraHeight)  //this is mFrameWidth/Height
    {
        Log.d2(TAG, "[CameraViewImpl][onCameraViewStarted] (cameraWidth, cameraHeight) = (", cameraWidth, ", ", cameraHeight + ")");
        mCameraGrayBuffer = new Mat(cameraHeight, cameraWidth, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped()
    {
        Log.d2(TAG, "[CameraViewImpl][onCameraViewStopped]");
        if (null != mCameraGrayBuffer)
            mCameraGrayBuffer.release();
        if (null != mCameraTranspose)
            mCameraTranspose.release();
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame)
    {
        Log.v(TAG, "[CameraViewImpl][onCameraFrame] + mOrientationMatTouchPoint = " + (mResetSwitch? mOrientationMatTouchPoint.toString():null));
        Mat cameraFrame = inputFrame.rgba();

        if (null == mCameraTranspose)
        {
            mCameraTranspose = new Mat(cameraFrame.cols(), cameraFrame.rows(), cameraFrame.type());
        }

        cameraFrame = rotateBySensorOrientationAndFlip(cameraFrame);

        //Face detection.
        // transfer cameraFrame(rgba) to gray Mat in the mCameraGrayBuffer
        Imgproc.cvtColor(cameraFrame, mCameraGrayBuffer, Imgproc.COLOR_RGBA2GRAY);

        if (null != mProcessCallback)
            mProcessCallback.processFrame(mCameraGrayBuffer, cameraFrame, this.mResetSwitch, this.mOrientationMatTouchPoint, 1);

        return cameraFrame;
    }

    //
    // opencv_3_1_0.org.opencv.android.JavaCameraView_3_1_0.CvCameraViewListener2
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // flipCode=0 means flip x-axis
    // flipCode=1 means flip y-axis
    // flipCode=-1 means flip x and y axis.
    // when do transpose the pics will rotate reverse clockwise 90.
    private Mat rotateBySensorOrientationAndFlip(Mat mat)
    {
        int currentSensorOrientation = OrientationHelper.instance().getSensorDirection();
        switch (currentSensorOrientation)
        {
            case OrientationHelper.ROTATION_0:
            {
                Core.flip(mat, mat, 1);
                return mat;
            }
            case OrientationHelper.ROTATION_90:
            {
                Core.transpose(mat, mCameraTranspose);
                Core.flip(mCameraTranspose, mCameraTranspose, -1);
                return mCameraTranspose;
            }
            case OrientationHelper.ROTATION_180:
            {
                Core.flip(mat, mat, 0);
                return mat;
            }
            case OrientationHelper.ROTATION_270:
            {
                Core.transpose(mat, mCameraTranspose);
                return mCameraTranspose;
            }
            default:
            {
                return mat;
            }
        }
    }

    @Override
    protected boolean initializeCamera(int width, int height)
    {
        /**
         * trigger by enableView at onResume.
         * the width, height comes from onEnterStartedState() and it is the view getWidth/getHeight.
         * */
        Log.d(TAG, "[initializeCamera] viewWidth = " + width + ", viewHeight = " + height);
        boolean retVal = super.initializeCamera(width, height); // this will decide the mFrameWidth/Height and the original mScale.
        return retVal;
    }

    @Override
    public void setCvCameraViewListener(CvCameraViewListener2 listener)
    {
        // Due to CvCameraViewListener2 mListener is private, so intercept the listener.
        Log.d(TAG, "[CameraViewImpl][setCvCameraViewListener] intercept");
        mInterceptListener = listener;
        super.setCvCameraViewListener(listener);
    }

    private void updateScaleValue()
    {
        //Compare surface size and the bitmap size then use the mScaleStrategy to decide the mScaleValue.
        //Call this function must make sure the mSurfaceSize and mDisplayBitmapWithoutScaleSize are already updated and correct.
        if (0 == mDisplayBitmapWithoutScaleSize.width || 0 == mDisplayBitmapWithoutScaleSize.height) {
            Log.d(TAG, "[CameraViewImpl] camera mFrameHeight/Height not ready.");
            return;
        }

        switch (mScaleStrategy)
        {
            case CameraViewConstants.CAMERA_BITMAP_FIT_SHORT_EADGE:
            {
                //mScale = Math.min(((float)viewHeight)/mFrameHeight, ((float)viewWidth)/mFrameWidth);
                //mScale = Math.min(((float)viewHeight)/getCameraOrientationHeight(), ((float)viewWidth)/getCameraOrientationWidth());
                mScaleValue = Math.min(((double)mSurfaceSize.height)/ mDisplayBitmapWithoutScaleSize.height, ((double)mSurfaceSize.width)/ mDisplayBitmapWithoutScaleSize.width);

                break;
            }
            case CameraViewConstants.CAMERA_BITMAP_FIT_LONG_EADGE:
            {
                //mScale = Math.max(((float)viewHeight)/mFrameHeight, ((float)viewWidth)/mFrameWidth);
                //mScale = Math.max(((float)viewHeight)/getCameraOrientationHeight(), ((float)viewWidth)/getCameraOrientationWidth());
                mScaleValue = Math.max(((double)mSurfaceSize.height)/ mDisplayBitmapWithoutScaleSize.height, ((double)mSurfaceSize.width)/ mDisplayBitmapWithoutScaleSize.width);
                break;
            }
            default:
            {
                //CameraViewConstants.CAMERA_BITMAP_NO_SCALE
                this.mScaleValue = 1;
                break;
            }
        }
        Log.v(TAG, "[CameraViewImpl][updateScaleValue] mScaleStrategy = " + mScaleStrategy + ", mScale = " + mScaleValue);
    }
    private Bitmap converMat2Bitmap(Mat modifiedMat)
    {

        // Currently the modifiedMat is the original raw data frame which orientation is correct.
        // This mat width/height is correct to display the raw data frame.

        int desiredWidth = modifiedMat.width();
        int desiredHeight = modifiedMat.height();
        String searchKey = String.valueOf(desiredWidth) + "x" + String.valueOf(desiredHeight);
        HashMap<String, Bitmap> bitmapPool = mBitmapPool;

        Bitmap bitmap = bitmapPool.get(searchKey);
        if (null == bitmap)
        {
            bitmap = Bitmap.createBitmap(desiredWidth, desiredHeight, Bitmap.Config.ARGB_8888);
            if (null != bitmap)
                bitmapPool.put(searchKey, bitmap);

            Log.v(TAG, "[CameraViewImpl][converMat2Bitmap] create new bitmap (w, h) = " + searchKey);
        }
        else
        {
            Log.v(TAG, "[CameraViewImpl][converMat2Bitmap] re-use bitmap (w, h) = " + searchKey);
        }

        // Now the display bitmap width/height are already decide.
        mDisplayBitmapWithoutScaleSize.width = modifiedMat.width();     // rotated mat.
        mDisplayBitmapWithoutScaleSize.height = modifiedMat.height();   // rotated mat.

        return bitmap;
    }
    private void releaseBitmapPool()
    {
        HashMap<String, Bitmap> bitmapPool = mBitmapPool;
        if (null != bitmapPool)
        {
            for (Map.Entry<String, Bitmap> entry : bitmapPool.entrySet())
            {
                String key = entry.getKey();
                Bitmap bitmap = entry.getValue();
                if (null != bitmap)
                    bitmap.recycle();
                Log.d2(TAG, "[CameraViewImpl][releaseBitmapPool] release bmp = " + key);
            }
            bitmapPool.clear();
        }
    }

    @Override
    protected void AllocateCache()
    {
        //Override the base class method and use my cache only.
        //Dynamicall allocate when needed.

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
        Bitmap cacheBitmap = null;      //Come from the pools. the size was decide by the Mat.width/height (same with camera, mFrameWidth/Height)
        if (modified != null) {
            try {
                cacheBitmap = converMat2Bitmap(modified);   // now the bitmap size/orientation is already correct.
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
                Log.v(TAG, "[CameraViewImpl] canvas (w, h) = (" + canvas.getWidth() + ", " + canvas.getHeight() + ")");
                Log.v(TAG, "[CameraViewImpl] original bitmap(not scale) (w, h) = (" + cacheBitmap.getWidth() + ", " + cacheBitmap.getHeight() + ")");

                canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);

                // Dynamically update the scale value.
                updateScaleValue();

                if (CameraViewConstants.DEBUG_CAMERA) {
                    canvas.drawColor(Color.YELLOW);
                }

                // scale and center the bitmap on the surfaceview.
                canvas.drawBitmap(cacheBitmap, new Rect(0,0,cacheBitmap.getWidth(), cacheBitmap.getHeight()),
                        new Rect((int)((canvas.getWidth() - mScaleValue*cacheBitmap.getWidth()) / 2),
                                (int)((canvas.getHeight() - mScaleValue*cacheBitmap.getHeight()) / 2),
                                (int)((canvas.getWidth() - mScaleValue*cacheBitmap.getWidth()) / 2 + mScaleValue*cacheBitmap.getWidth()),
                                (int)((canvas.getHeight() - mScaleValue*cacheBitmap.getHeight()) / 2 + mScaleValue*cacheBitmap.getHeight())), null);


                if (CameraViewConstants.DEBUG_CAMERA) {
                    CanvasBorderHelper.drawBorder(canvas);
                }

                if (mFpsMeter != null) {
                    mFpsMeter.measure();
                    mFpsMeter.draw(canvas, 20, 30);
                }
                getHolder().unlockCanvasAndPost(canvas);
            }
        }
    }

    @Override
    protected Size calculateCameraFrameSize(List<?> supportedSizes, ListItemAccessor accessor, int surfaceWidth, int surfaceHeight)
    {
        dumpCameraInfo(supportedSizes, accessor);
        Log.d(TAG, "[calculateCameraFrameSize] surface (w, h) = " + surfaceWidth + ", " + surfaceHeight + ")");

        /**
         * Adjust camera Max size limit.
         * */
        setupCameraMaxSizeLimit((List<Camera.Size>)supportedSizes, accessor);

        /**
         * Because we do not re-initialize the camera when re-layout or rotate.
         * So we try to get a square camera which can best fit for portrait and landscape.
         * We ignore the layout width/height but use mMaxWidth/mMaxHeight to find the best fit camera instead.
         * */
        Size cameraSize = super.calculateCameraFrameSize(supportedSizes, accessor, mMaxWidth, mMaxHeight);
        return cameraSize;
    }



    private Size getCameraSizeFromPreference(SharedPreferences cameraSettings)
    {
        if (null == cameraSettings)
            return null;

        int cameraWidth = cameraSettings.getInt(PREF_KEY_CAMERA_WIDTH, -1);
        int cameraHeight = cameraSettings.getInt(PREF_KEY_CAMERA_HEIGHT, -1);

        if (cameraWidth <= 0 || cameraHeight <= 0)
            return null;

        Log.d(TAG, "[calculateCameraFrameSize] pref hit. camera (w, h) = " + cameraWidth + ", " + cameraHeight + ")");
        return new Size(cameraWidth, cameraHeight);
    }

    private void saveCameraSizeToPreference(SharedPreferences cameraSettings, Size cameraSize)
    {
        if (null == cameraSize || null == cameraSettings)
            return;

            SharedPreferences.Editor editor = cameraSettings.edit();
            editor.putInt(PREF_KEY_CAMERA_WIDTH, (int)cameraSize.width);
            editor.putInt(PREF_KEY_CAMERA_HEIGHT, (int)cameraSize.height);
            editor.commit();
            Log.d(TAG, "[calculateCameraFrameSize] save pref. camera (w, h) = " + cameraSize.width + ", " + cameraSize.height + ")");

    }

    @Override
    protected void disconnectCamera()
    {
        Log.d(TAG, "[CameraViewImpl][disconnectCamera]");
        super.disconnectCamera();
        releaseBitmapPool();
    }

    private class CameraViewTouchListener implements View.OnTouchListener
    {
        Point mSurfaceViewTouchPoint = new Point(0, 0); // the point is relative to the surfaceview.
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            switch (event.getAction() & MotionEvent.ACTION_MASK)
            {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                {
                    caculateSurfaceViewTouchPoint2MatPoint(event);
                    mOrientationMatTouchPoint.x = mSurfaceViewTouchPoint.x;
                    mOrientationMatTouchPoint.y = mSurfaceViewTouchPoint.y;
                    mResetSwitch = true;
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                {
                    mResetSwitch = false;
                    break;
                }
                case MotionEvent.ACTION_MOVE:
                {
                    break;
                }
            }
            return true;
        }

        private void caculateSurfaceViewTouchPoint2MatPoint(MotionEvent event)
        {
            // CAMERA_BITMAP_NO_SCALE, CAMERA_BITMAP_FIT_SHORT_EADGE, CAMERA_BITMAP_FIT_LONG_EADGE use the same algorithm.

            mSurfaceViewTouchPoint.x = event.getX();
            mSurfaceViewTouchPoint.y = event.getY();

            if (CameraViewConstants.DEBUG_CAMERA)
            {
                Log.d(TAG, "[CameraViewImpl] Frame (w, h) ( = " + mFrameWidth + ", " + mFrameHeight + ")");
                Log.d(TAG, "[CameraViewImpl] SurfaceView (w, h) ( = " + mSurfaceSize.width + ", " + mSurfaceSize.height + ")");
                Log.d(TAG, "[CameraViewImpl] mScaleValue = " + mScaleValue + ", strategy = " + mScaleStrategy);
                Log.d(TAG, "[CameraViewImpl] DisplayBitmapWithoutScaleSize (w, h) ( = " + mDisplayBitmapWithoutScaleSize.width + ", " + mDisplayBitmapWithoutScaleSize.height + ")");
                Log.d(TAG, "[CameraViewImpl] Real-DisplayBitmapView (w, h) ( = " + mScaleValue * mDisplayBitmapWithoutScaleSize.width + ", " + mScaleValue * mDisplayBitmapWithoutScaleSize.height + ")");
                Log.d(TAG, "[CameraViewImpl] left-top (left, top) ( = " + (mSurfaceSize.width - mScaleValue * mDisplayBitmapWithoutScaleSize.width) / 2 + ", " + (mSurfaceSize.height - mScaleValue * mDisplayBitmapWithoutScaleSize.height) / 2 + ")");
                Log.d(TAG, "[CameraViewImpl] SurfaceView TouchPoint (x, y) = (" + mSurfaceViewTouchPoint.x + ", " + mSurfaceViewTouchPoint.y + ")");
            }

            mSurfaceViewTouchPoint.x = (mSurfaceViewTouchPoint.x - ((mSurfaceSize.width - mScaleValue * mDisplayBitmapWithoutScaleSize.width)/2)) / mScaleValue;
            mSurfaceViewTouchPoint.y = (mSurfaceViewTouchPoint.y - ((mSurfaceSize.height - mScaleValue * mDisplayBitmapWithoutScaleSize.height)/2)) / mScaleValue;
        }
    }// end private class CameraViewTouchListener implements View.OnTouchListener


    private void dumpCameraInfo(List<?> supportedSizes, ListItemAccessor accessor)
    {
        for (Object size : supportedSizes) {
            int width = accessor.getWidth(size);
            int height = accessor.getHeight(size);
            Log.d(TAG, "[dumpCameraInfo] (w, h) = (" + width + ", " + height + ")");
        }
    }

    private void setupCameraMaxSizeLimit(List<Camera.Size> cameraSupportedSizes, ListItemAccessor accessor)
    {
        int cameraViewLongEdge = getCameraViewLongEdge();
        int caculateInitCameraThreshold = caculateInitCameraThreshold(cameraSupportedSizes, accessor);
        mMaxWidth = Math.min(caculateInitCameraThreshold, cameraViewLongEdge);
        mMaxHeight = Math.min(caculateInitCameraThreshold, cameraViewLongEdge);

        Log.d(TAG, "[cameraInfo][setupCameraMaxSizeLimit] " +
                ", mMaxWidth = " + mMaxWidth +
                ", mMaxHeight = " + mMaxHeight +
                ", cameraViewLongEdge = " + cameraViewLongEdge +
                ", caculateInitCameraThreshold = " + caculateInitCameraThreshold +
                ", deviceLong = " + ViewHelper.getDeviceLongEdge(getContext()) +
                ", deviceShort = " + ViewHelper.getDeviceShortEdge(getContext()) +
                ", density = " + ViewHelper.getDensity(getContext()));
    }

    private int getCameraViewLongEdge()
    {
        int cameraLongEdge = (int)(ViewHelper.getDeviceLongEdge(getContext()) * mCameraSizeRatio);    ////  long edge * 5/8
        int cameraShortEdge = ViewHelper.getDeviceLongEdge(getContext());
        int aveCameraEdge = (cameraLongEdge + cameraShortEdge)/2;
        // Due to the bitmap will scale to fit the view in portrait/landscape. Use square to constraint the camera for minimal scale.
        Log.d(TAG, "[cameraInfo][getCameraViewLongEdge] (cameraLongEdge, cameraShortEdge) = (" + cameraLongEdge + ", " + cameraShortEdge + ")" +
                    ", aveCameraEdge = " + aveCameraEdge);
        return aveCameraEdge;
    }

    private static final int PARAMETER_RATIO = 3;   //experimental parameters.
    private int caculateInitCameraThreshold(List<Camera.Size> cameraSupportedSizes, ListItemAccessor accessor)
    {
        List<Camera.Size> supportedSize = new LinkedList<Camera.Size>(cameraSupportedSizes);

        if (supportedSize.size() == 0)
            return -1;

        Collections.sort(supportedSize, new CameraSizeComparable());
        Camera.Size maxAreaSize = supportedSize.get(0);
        int desiredAreaSize = maxAreaSize.width * maxAreaSize.height / PARAMETER_RATIO;

        Camera.Size result = maxAreaSize;

        // Find the first area which is smaller than desiredAreaSize
        for (Camera.Size size : supportedSize)
        {
            int area = size.width * size.height;
            if (area > desiredAreaSize)
                continue;
            else
            {
                result = size;
                break;
            }
        }

        Log.d(TAG, "[cameraInfo][caculateInitCameraThreshold] (w, h) = (" + result.width + ", " + result.height + ")");
        return Math.max(result.width, result.height);

    }


    private class CameraSizeComparable implements Comparator<Camera.Size>
    {
        @Override
        public int compare(Camera.Size size1, Camera.Size size2) {
            int size1Area = size1.width * size1.height;
            int size2Area = size2.width * size2.height;

            // 1. Compare area first.
            if (size1Area != size2Area)
            {
                return size2Area - size1Area;
            }

            // 2. When area is the same, compare the width and height
            if (size1.width != size2.width) {
                return size2.width - size1.width;

            } else {
                return size2.height - size1.height;
            }
        }
    }// end CameraSizeComparable

}
