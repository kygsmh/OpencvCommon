package com.opencv.common.UI.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by Jason_Fang on 2016/9/3.
 */
public class SurfaceViewBase extends SurfaceView implements SurfaceHolder.Callback
{
    protected boolean DEBUG = false;
    private static final String TAG = "SurfaceViewBase";
    private SurfaceHolder mSurfaceHolder = null;
    private RenderThread mRenderThread = null;
    private RenderThreadHandler mRenderThreadHandler = null;
    private int mSurfaceWidth = 0;
    private int mSurfaceHeight = 0;

    protected static final Paint mClearPaints = new Paint();
    static
    {
        mClearPaints.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    public SurfaceViewBase(Context context) {
        super(context);
        init(context, null, 0);
    }

    public SurfaceViewBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public SurfaceViewBase(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr)
    {
        if (DEBUG) Log.d(TAG, TAG + " init");

        setLayerType(View.LAYER_TYPE_SOFTWARE, new Paint());        //TBD, disable hardware acceleration???
        mSurfaceHolder = this.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        setZOrderOnTop(true);                                       //TBD


    }



    protected void onRenderThreadReady(Handler renderThreadHandler)
    {
        // In RenderThread thread.
    }

    protected void onRenderThreadDestroy()
    {

    }

    protected void onDrawInBackground(Canvas canvas)
    {
        if (DEBUG) Log.d(TAG, TAG + " onDrawInBackground ++" + Thread.currentThread().getName());
        canvas.drawColor(Color.BLACK);
    }

    protected boolean onMessageInBackground(Message msg)
    {
        if (DEBUG) Log.d(TAG, TAG + " onMessageInBackground ++" + Thread.currentThread().getName());
        boolean processed = false;

        switch (msg.what)
        {
            case MSG_RENDER_THREAD_DO_RENDER:
            {
                reDrawInBackground();
                processed = true;
                break;
            }
        }
        return processed;
    }

    private void reDrawInBackground()
    {
        if (DEBUG) Log.d(TAG, TAG + " reDrawInBackground ++" + Thread.currentThread().getName());
        Canvas canvas = null;
        try
        {
            synchronized (mSurfaceHolder)
            {
                canvas = mSurfaceHolder.lockCanvas();
                if (null != canvas) {
                    canvas.drawPaint(mClearPaints);     // clear    TBD
                    onDrawInBackground(canvas);
                }
            }
        }
        catch (Exception e)
        {
            if (DEBUG) Log.e(TAG, TAG + " reDrawInBackground ex = " + e);
            e.printStackTrace();
        }
        finally {
            if (null != canvas)
            {
                mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
        if (DEBUG) Log.d(TAG, TAG + " reDrawInBackground --");
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        if (DEBUG) Log.d(TAG, TAG + " surfaceCreated");
        if (null == mRenderThread) {

            mSurfaceWidth = Math.abs(holder.getSurfaceFrame().right - holder.getSurfaceFrame().left);   //TBD
            mSurfaceHeight = Math.abs(holder.getSurfaceFrame().bottom - holder.getSurfaceFrame().top);

            mRenderThread = new RenderThread(TAG + "_RenderThread");
            mRenderThread.start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (DEBUG) Log.d(TAG, TAG + " surfaceChanged format = " + format + ", w = " + width + ", h = " + height);
        mSurfaceWidth = width;
        mSurfaceHeight = height;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (DEBUG) Log.d(TAG, TAG + " surfaceDestroyed");
        if (null != mRenderThread) {
            mRenderThread.finish();
            mRenderThread = null;
        }
    }

    protected float getSurfaceWidth()
    {
        return this.mSurfaceWidth;
    }

    protected float getSurfaceHeight()
    {
        return this.mSurfaceHeight;
    }
    private static final int RENDER_DELAY = 100;
    protected int getRenderDelay()
    {
        // Cannot so fast it will cause the msgQ very large and delay others custom events process speed.
        return RENDER_DELAY;
    }

    private class RenderThread extends HandlerThread
    {
        public RenderThread(String name)
        {
            super(name);
        }

        @Override
        protected void onLooperPrepared()
        {
            //bk
            super.onLooperPrepared();
            mRenderThreadHandler = new RenderThreadHandler(getLooper());

            // Notify render thread ready.
            onRenderThreadReady(mRenderThreadHandler);
            // Start to render.
            mRenderThreadHandler.sendEmptyMessageDelayed(MSG_RENDER_THREAD_DO_RENDER, getRenderDelay());
        }

        public void finish()
        {
            if (DEBUG) Log.d(TAG, TAG + " finish");
            onRenderThreadDestroy();
            mRenderThreadHandler.removeCallbacksAndMessages(null);
            this.quit();
            mRenderThreadHandler = null;
        }
    }

    private static final int MSG_RENDER_THREAD_DO_RENDER = 1;
    public static final int MSG_RENDER_THREAD_CUSTOM_BEGIN = 1000;

    class RenderThreadHandler extends Handler
    {
        RenderThreadHandler(Looper looper)
        {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (null == mRenderThread)
            {
                if (DEBUG) Log.d(TAG, TAG + " thread already exit.");
                return;
            }

            onMessageInBackground(msg);

            //reDraw each time.
            switch(msg.what)
            {
                case MSG_RENDER_THREAD_DO_RENDER:
                {
                    if (DEBUG) Log.d(TAG, TAG + " RENDER_DELAY = " + getRenderDelay());
                    if (null != mRenderThread)
                        sendEmptyMessageDelayed(MSG_RENDER_THREAD_DO_RENDER, getRenderDelay());
                }

            }
        }
    }


}
