package com.opencv.common.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.widget.ImageView;

import java.lang.ref.SoftReference;

//import android.util.Log;

/**
 * Created by Jason_Fang on 2017/4/18.
 */

public class AnimationHelper
{
    static final String TAG = "AnimationHelper";

    private Handler mUIHandler = new Handler();
    private SoftReference<ImageView> mHostImageView;
    private Context mContext;
    private AnimationFrameController mCurrentAnimationFrameController = null;


    public interface AnimateCallback {
        void onAnimationStart();
        void onAnimationFrameChanged(int index);
        void onAnimationEnd(boolean interrupt);
    }

    // Animation clock
    private class AnimationFrameController implements Runnable
    {
        boolean mIsStop = false;
        int[] mFrameResIds = null;
        long mTotalDuration = 0;
        long mEachFrameDuration = 0;
        int mCurrentFrameIndex = 0;
        AnimateCallback mAnimateCallback = null;
        private Bitmap mRecycleBitmap;

        AnimationFrameController (int[] resIds,
                                  long totalDuration,
                                  AnimateCallback callback)
        {
            mFrameResIds = resIds.clone();
            mTotalDuration = totalDuration;
            mEachFrameDuration = totalDuration / resIds.length;
            mAnimateCallback = callback;
        }

        int getFrameResId (int frameIndex) {
            return mFrameResIds[frameIndex];
        }

        void forceStop()
        {
            mIsStop = true;
            if (null != mAnimateCallback) {
                LogHelper.d(TAG, "[" + mCurrentAnimationFrameController.hashCode() + "][onAnimationEnd] inturrupt = true");
                mAnimateCallback.onAnimationEnd(true);
            }
        }

        @Override
        public void run()
        {
            // decode frame
            FrameDecoder task = new FrameDecoder(mCurrentFrameIndex, this);
            task.execute();

            // post runnable to process next frame.
            mCurrentFrameIndex++;
            if (!mIsStop && mCurrentFrameIndex < mFrameResIds.length)
                mUIHandler.postDelayed(this, mEachFrameDuration);
        }
    }

    public boolean isPlaying() {
        return mCurrentAnimationFrameController != null;
    }

    public AnimationHelper(ImageView hostImageView)
    {
        mContext = hostImageView.getContext();
        mHostImageView = new SoftReference<ImageView>(hostImageView);
    }

    public synchronized void start(int[] resIds, long totalDuration, AnimateCallback callback) {
        if (null == resIds || resIds.length == 0)
            return;

        // previous controller exist.
        if (null != mCurrentAnimationFrameController)
        {
            mCurrentAnimationFrameController.forceStop();
            mCurrentAnimationFrameController = null;
        }

        // create new controller
        mCurrentAnimationFrameController = new AnimationFrameController(resIds, totalDuration, callback);
        if (null != callback) {
            LogHelper.d(TAG, "[" + mCurrentAnimationFrameController.hashCode() + "][onAnimationStart]");
            callback.onAnimationStart();
        }

        mUIHandler.post(mCurrentAnimationFrameController);

    }

    private void onFrameReady(AnimationFrameController controller, int decodeFrameIndex, Drawable frame)
    {
        if (null == controller)
            return;

        if (mCurrentAnimationFrameController != controller)     // controller has changed.
        {
            LogHelper.d("AnimateCallback", "ignore frame " + decodeFrameIndex);
            return;
        }

        ImageView imageView = mHostImageView.get();
        if(frame != null && null != imageView)
            imageView.setImageDrawable(frame);

        AnimateCallback callback = controller.mAnimateCallback;

        if (null != callback) {
            LogHelper.d(TAG, "[" + mCurrentAnimationFrameController.hashCode() + "][onAnimationFrameChanged] idx = " + decodeFrameIndex);
            callback.onAnimationFrameChanged(decodeFrameIndex);
        }


        if (decodeFrameIndex == controller.mFrameResIds.length - 1) {
            if (null != callback) {
                LogHelper.d(TAG, "[" + mCurrentAnimationFrameController.hashCode() + "][onAnimationEnd] inturrupt = false");
                callback.onAnimationEnd(false);
            }

            // AnimationEnd then clear the controller.
            mCurrentAnimationFrameController = null;
        }
    }


    private class FrameDecoder extends AsyncTask<Void, Void, Drawable>
    {

        private int mDecodeFrameIndex = 0;
        private AnimationFrameController mAnimationFrameController = null;
        FrameDecoder(int frameIndex, AnimationFrameController controller)
        {
            mDecodeFrameIndex = frameIndex;
            mAnimationFrameController = controller;
        }

        @Override
        protected Drawable doInBackground(Void... params)
        {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
                return mContext.getResources().getDrawable(mAnimationFrameController.getFrameResId(mDecodeFrameIndex));
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            BitmapDrawable drawable = null;
            synchronized (AnimationHelper.class)
            {
                if (mAnimationFrameController.mRecycleBitmap != null)
                    options.inBitmap = mAnimationFrameController.mRecycleBitmap;
                mAnimationFrameController.mRecycleBitmap = BitmapFactory.decodeResource(mContext.getResources(), mAnimationFrameController.getFrameResId(mDecodeFrameIndex), options);
                drawable = new BitmapDrawable(mContext.getResources(), mAnimationFrameController.mRecycleBitmap);
            }
            return drawable;
        }

        @Override
        protected void onPostExecute(Drawable frame) {
            super.onPostExecute(frame);
            onFrameReady(mAnimationFrameController, mDecodeFrameIndex, frame);
        }
    }


}
