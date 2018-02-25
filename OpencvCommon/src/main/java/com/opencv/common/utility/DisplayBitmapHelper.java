package com.opencv.common.utility;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.view.MotionEvent;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;

import java.util.HashMap;
import java.util.Map;

//import android.util.Log;

/**
 * Created by Jason_Fang on 2017/4/18.
 */

class DisplayBitmapHelper
{
    private static final String TAG = "[DisplayBitmapHelper]";
    private Size mSurfaceSize = new Size();                     // generally this would be the layout (surface) size.
    private Size mDisplayBitmapWithoutScaleSize = new Size();   // This will follow the orientation. The size did not contain scale.
    private double mScaleValue = 1;
    private int mScaleStrategy = CameraHelper.CAMERA_BITMAP_FIT_LONG_EADGE;
    private HashMap<String, Bitmap> mBitmapPool = new HashMap<String, Bitmap>(3);   //key = width x height, value = bitmap


    public DisplayBitmapHelper() {}

    public void init() {
        // lazy init the pool when needed.
    }

    public void deInit() {
        releaseBitmapPool();
    }


    public void updateDisplaySize(int width, int height)
    {
        mSurfaceSize.width = width;
        mSurfaceSize.height = height;
    }

    public void drawBitmap(Canvas canvas, Bitmap cacheBitmap)
    {
        // 1. update scale value.
        updateScaleValue();

        // 2. use scale value to draw bitmap.
        internalDrawBitmap(canvas, cacheBitmap);

    }

    // scale and center the bitmap on the surfaceview.
    private void internalDrawBitmap(Canvas canvas, Bitmap cacheBitmap) {


        Rect srcBitmapDrawRange = new Rect(0,0,cacheBitmap.getWidth(), cacheBitmap.getHeight());
        Rect dstLocationOnScreen = new Rect((int)((canvas.getWidth() - mScaleValue*cacheBitmap.getWidth()) / 2),
                                            (int)((canvas.getHeight() - mScaleValue*cacheBitmap.getHeight()) / 2),
                                            (int)((canvas.getWidth() - mScaleValue*cacheBitmap.getWidth()) / 2 + mScaleValue*cacheBitmap.getWidth()),
                                            (int)((canvas.getHeight() - mScaleValue*cacheBitmap.getHeight()) / 2 + mScaleValue*cacheBitmap.getHeight()));

        canvas.drawBitmap(  cacheBitmap,
                            srcBitmapDrawRange,
                            dstLocationOnScreen,
                            null);

        /*****************************************************************************************
         *
         * Face coordinate callback
         *
         */
        IRobotEyeDisplayBitmapCallback displayCB = mRobotEyeDisplayBitmapCallback;
        if (null != displayCB)
        {
            PointF[] faceCoordinate = coordinateTranslate(canvas, cacheBitmap);
            if (null != faceCoordinate && faceCoordinate.length >=2)
            {
                //CanvasBorderHelper.drawCircle(canvas, faceCoordinate[0], faceCoordinate[1], 0, 0);
                displayCB.onFaceCoordinateUpdate(faceCoordinate[0], faceCoordinate[1]);
            }
            else
            {
                displayCB.onFaceCoordinateUpdate(null, null);
            }
        }
        /**
         *
         * Face coordinate callback
         *
         ****************************************************************************************/



        if (CameraHelper.DEBUG_CAMERA) {
            LogHelper.d(TAG, "[JV] mScaleValue = " + mScaleValue);
            LogHelper.d(TAG, "[JV] cacheBitmap (w, h) = (" + cacheBitmap.getWidth() + ", " + cacheBitmap.getHeight() + ")");  // src pic size
            LogHelper.d(TAG, "[JV] canvas (w, h) = (" + canvas.getWidth() + ", " + canvas.getHeight() + ")");                 // target view size.
            LogHelper.d(TAG, "[JV] srcBitmapDrawRange = " + srcBitmapDrawRange.toString());
            LogHelper.d(TAG, "[JV] dstLocationOnScreen = " + dstLocationOnScreen.toString());

            CanvasBorderHelper.drawBorder(canvas);
        }
    }

    private PointF[] coordinateTranslate(Canvas canvas, Bitmap cacheBitmap)
    {

        PointF tmpLeftTop = mCameraFrameFaceLocationLeftTop;
        PointF tmpRightDown = mCameraFrameFaceLocationRightDown;

        dumpLeftTopRightDown("coordinateTranslate_Before ", tmpLeftTop, tmpRightDown);

        if (null == tmpLeftTop || null == tmpRightDown)
        {
            // No face detected.
            return null;
        }

        PointF outLeftTop = new PointF(tmpLeftTop.x, tmpLeftTop.y);
        PointF outRightDown = new PointF(tmpRightDown.x, tmpRightDown.y);;

        // found the face.
        outLeftTop.x = (float) mScaleValue * outLeftTop.x;
        outLeftTop.y = (float) mScaleValue * outLeftTop.y;

        outRightDown.x = (float) mScaleValue * outRightDown.x;
        outRightDown.y = (float) mScaleValue * outRightDown.y;

        // origin coordinate has been shift
        float shiftX = (float)((canvas.getWidth() - mScaleValue*cacheBitmap.getWidth()) / 2);
        float shiftY = (float)((canvas.getHeight() - mScaleValue*cacheBitmap.getHeight()) / 2);
        outLeftTop.x = outLeftTop.x + shiftX;
        outLeftTop.y = outLeftTop.y + shiftY;

        outRightDown.x = outRightDown.x + shiftX;
        outRightDown.y = outRightDown.y + shiftY;

        PointF[] result = new PointF[2];
        result[0] = outLeftTop;
        result[1] = outRightDown;

        dumpLeftTopRightDown("coordinateTranslate_After ", result[0], result[1]);
        return result;
    }

    //Dynamically update the scale value.
    private void updateScaleValue()
    {
        //Compare surface size and the bitmap size then use the mScaleStrategy to decide the mScaleValue.
        //Call this function must make sure the mSurfaceSize and mDisplayBitmapWithoutScaleSize are already updated and correct.
        if (0 == mDisplayBitmapWithoutScaleSize.width || 0 == mDisplayBitmapWithoutScaleSize.height) {
            LogHelper.d(TAG, "[CameraViewImpl] camera mFrameHeight/Height not ready.");
            return;
        }

        switch (mScaleStrategy)
        {
            case CameraHelper.CAMERA_BITMAP_FIT_SHORT_EADGE:
            {
                //mScale = Math.min(((float)viewHeight)/mFrameHeight, ((float)viewWidth)/mFrameWidth);
                //mScale = Math.min(((float)viewHeight)/getCameraOrientationHeight(), ((float)viewWidth)/getCameraOrientationWidth());
                mScaleValue = Math.min(((double)mSurfaceSize.height)/ mDisplayBitmapWithoutScaleSize.height, ((double)mSurfaceSize.width)/ mDisplayBitmapWithoutScaleSize.width);

                break;
            }
            case CameraHelper.CAMERA_BITMAP_FIT_LONG_EADGE:
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
        LogHelper.v(TAG, "[CameraViewImpl][updateScaleValue] mScaleStrategy = " + mScaleStrategy + ", mScale = " + mScaleValue);
    }

    public Bitmap converMat2Bitmap(Mat modifiedMat)
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

            LogHelper.v(TAG, "[CameraViewImpl][converMat2Bitmap] create new bitmap (w, h) = " + searchKey);
        }
        else
        {
            LogHelper.v(TAG, "[CameraViewImpl][converMat2Bitmap] re-use bitmap (w, h) = " + searchKey);
        }

        // Now the display bitmap width/height are already decide.
        mDisplayBitmapWithoutScaleSize.width = modifiedMat.width();     // rotated mat.
        mDisplayBitmapWithoutScaleSize.height = modifiedMat.height();   // rotated mat.

        return bitmap;
    }
    public void releaseBitmapPool()
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
                LogHelper.d(TAG, "[CameraViewImpl][releaseBitmapPool] release bmp = " + key);
            }
            bitmapPool.clear();
        }
    }

    PointF mCameraFrameFaceLocationLeftTop = null;
    PointF mCameraFrameFaceLocationRightDown = null;

    synchronized void updateFaceLocation(long timeStamp, PointF leftTop, PointF rightDown)
    {
        mCameraFrameFaceLocationLeftTop = leftTop;
        mCameraFrameFaceLocationRightDown = rightDown;

        dumpLeftTopRightDown("display-updateFaceLocation", mCameraFrameFaceLocationLeftTop, mCameraFrameFaceLocationRightDown);
    }


    public Point caculateSurfaceViewTouchPoint2MatPoint(MotionEvent event)
    {
        // CAMERA_BITMAP_NO_SCALE, CAMERA_BITMAP_FIT_SHORT_EADGE, CAMERA_BITMAP_FIT_LONG_EADGE use the same algorithm.

        Point surfaceViewTouchPoint = new Point(0, 0); // the point is relative to the surfaceview.
        surfaceViewTouchPoint.x = event.getX();
        surfaceViewTouchPoint.y = event.getY();

//            if (CameraViewConstants.DEBUG_CAMERA)
//            {
//                Log.d(TAG, "[CameraViewImpl] Frame (w, h) ( = " + mFrameWidth + ", " + mFrameHeight + ")");
//                Log.d(TAG, "[CameraViewImpl] SurfaceView (w, h) ( = " + mSurfaceSize.width + ", " + mSurfaceSize.height + ")");
//                Log.d(TAG, "[CameraViewImpl] mScaleValue = " + mScaleValue + ", strategy = " + mScaleStrategy);
//                Log.d(TAG, "[CameraViewImpl] DisplayBitmapWithoutScaleSize (w, h) ( = " + mDisplayBitmapWithoutScaleSize.width + ", " + mDisplayBitmapWithoutScaleSize.height + ")");
//                Log.d(TAG, "[CameraViewImpl] Real-DisplayBitmapView (w, h) ( = " + mScaleValue * mDisplayBitmapWithoutScaleSize.width + ", " + mScaleValue * mDisplayBitmapWithoutScaleSize.height + ")");
//                Log.d(TAG, "[CameraViewImpl] left-top (left, top) ( = " + (mSurfaceSize.width - mScaleValue * mDisplayBitmapWithoutScaleSize.width) / 2 + ", " + (mSurfaceSize.height - mScaleValue * mDisplayBitmapWithoutScaleSize.height) / 2 + ")");
//                Log.d(TAG, "[CameraViewImpl] SurfaceView TouchPoint (x, y) = (" + mSurfaceViewTouchPoint.x + ", " + mSurfaceViewTouchPoint.y + ")");
//            }

        surfaceViewTouchPoint.x = (surfaceViewTouchPoint.x - ((mSurfaceSize.width - mScaleValue * mDisplayBitmapWithoutScaleSize.width)/2)) / mScaleValue;
        surfaceViewTouchPoint.y = (surfaceViewTouchPoint.y - ((mSurfaceSize.height - mScaleValue * mDisplayBitmapWithoutScaleSize.height)/2)) / mScaleValue;

        return surfaceViewTouchPoint;
    }

    private void dumpLeftTopRightDown(String tag, PointF leftTop, PointF rightDown)
    {
        float width = 0;
        float height = 0;
        if (null != leftTop || null != rightDown)
        {
            width = rightDown.x - leftTop.x;
            height = rightDown.y - leftTop.y;
        }

        LogHelper.d(tag, "leftTop = " + (null == leftTop?null:leftTop.toString()) + "\trightDown = " + (null == rightDown?null:rightDown.toString())
                + "\t(w, h) = (" + width + ", " + height + ")");

    }

    /**
     * Notify the RobotEye coordinate to user.
     * */
    IRobotEyeDisplayBitmapCallback mRobotEyeDisplayBitmapCallback = null;
    public void setRobotEyeDisplayBitmapCallback(IRobotEyeDisplayBitmapCallback callabck){
        mRobotEyeDisplayBitmapCallback = callabck;
    }

}
