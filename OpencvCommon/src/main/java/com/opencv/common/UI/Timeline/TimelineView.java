package com.opencv.common.UI.Timeline;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;

import com.opencv.common.R;
import com.opencv.common.UI.common.SurfaceViewBase;

import org.opencv.core.Size;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by Jason_Fang on 2016/9/3.
 */
public class TimelineView extends SurfaceViewBase
{
    private static final String TAG = "TimelineView";

    private Handler mRenderThreadHandler = null;
    private LinkedList<FatigueTimeEvent> mLinkedList = new LinkedList<FatigueTimeEvent>();



    // Paints
    private static final SparseArray<Paint> mPaints = new SparseArray<Paint>(3);
    private Paint mSeparatorPaint = null;
    private Paint mTimeTextPaint = null;
    //private Paint mTextBlockBackground = null;
    private Paint.FontMetrics mTimeTextFontMetrics = null;


    private DateFormat mDateFormat = null;
    private Date mDate = new Date();



    // layout dimensions.

    // dimension define.
    private static float TIME_UNIT_WIDTH_IN_PIXEL = 10;                    // default is 10, in px
    private static float UNIT_TIME_SEPARATOR_WIDTH_IN_PIXEL = 0.5f;        // default is 0.5, in px

    // dimension variable.
    private RectF mTimeBar_px = null;                                   // the full time bar width and height
    private RectF mTimeTextBlock = null;
    private RectF mTimePictureBlock = null;

    private Size mTimeUnit_px = null;                                  // A time unit width and height.
    private Size mTimeSeparator_px = null;                             // A separator width and height.

    private boolean mPostStartFlag = false;



    private void initResourceValue()
    {
        Resources resources = getContext().getResources();

        TIME_UNIT_WIDTH_IN_PIXEL = resources.getDimension(R.dimen.timeline_unit_width_in_dp);
        UNIT_TIME_SEPARATOR_WIDTH_IN_PIXEL = resources.getDimension(R.dimen.timeline_separator_width_in_dp);

        // 0. use surface w/h as TimeBar w/h
        mTimeBar_px = new RectF(0.0f, 0.0f, getSurfaceWidth(), getSurfaceHeight());

        // 1. allocate the picture block.  timeunit_relative_to_timebar_height_percentage% of TimeBar H
        float pictureBlockHeight = mTimeBar_px.height() * resources.getInteger(R.integer.picture_block_relative_to_timebar_height_percentage) /100;
        mTimePictureBlock = new RectF(0.0f, mTimeBar_px.height() - pictureBlockHeight, mTimeBar_px.width(), mTimeBar_px.height());

        // 2. the rest of picture part is the text block
        float textBlockHeight = mTimeBar_px.height() - pictureBlockHeight;
        mTimeTextBlock = new RectF(0.0f, 0.0f, mTimeBar_px.width(), textBlockHeight);

        // 3. TimeUnit is boundary in the TimePictureBlock.
        mTimeUnit_px = new Size((int)TIME_UNIT_WIDTH_IN_PIXEL, (int)mTimePictureBlock.height());

        // 4. Separator is boundary in the TimePictureBlock
        mTimeSeparator_px = new Size((int)UNIT_TIME_SEPARATOR_WIDTH_IN_PIXEL, (int)(mTimePictureBlock.height() * resources.getInteger(R.integer.timeseparator_relative_to_timepictureblock_height_percentage) / 100));

        Log.d(TAG,  "timeline Unit(px) = (" + ", " + mTimeUnit_px +
                    "), timeline separator (px) = (" + ", " + mTimeSeparator_px + ")");

        this.mDateFormat = new SimpleDateFormat(resources.getString(R.string.timebar_timeformat));

    }

    public TimelineView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public TimelineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public TimelineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    public void init(Context context, AttributeSet attrs, int defStyleAttr)
    {
        preparePaints(context);
    }

    private void preparePaints(Context context)
    {
        Resources resources = context.getResources();
        Paint aPaint = new Paint();
        aPaint.setColor(resources.getColor(R.color.timeline_status_normal));
        mPaints.append(FatigueTimeEvent.TYPE_NORMAL, aPaint);

        aPaint = new Paint();
        aPaint.setColor(resources.getColor(R.color.timeline_status_semi_fatigue));
        mPaints.append(FatigueTimeEvent.TYPE_SEMI_FATIGUE, aPaint);

        aPaint = new Paint();
        aPaint.setColor(resources.getColor(R.color.timeline_status_fatigue));
        mPaints.append(FatigueTimeEvent.TYPE_FATIGUE, aPaint);


        mSeparatorPaint = new Paint();
        mSeparatorPaint.setColor(context.getResources().getColor(R.color.timeline_separator));

        mTimeTextPaint = new Paint();
        mTimeTextPaint.setAntiAlias(true);
        mTimeTextPaint.setTextSize(resources.getDimensionPixelSize(R.dimen.timeline_text_font_size));
        mTimeTextPaint.setColor(resources.getColor(R.color.timeline_text));
        mTimeTextFontMetrics = mTimeTextPaint.getFontMetrics();

//        mTextBlockBackground = new Paint();
//        mTextBlockBackground.setColor(resources.getColor(R.color.timeline_text_block_background));
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        super.surfaceChanged(holder, format, width, height);
        initResourceValue();
    }

    @Override
    protected void onRenderThreadReady(Handler renderThreadHandler) {
        super.onRenderThreadReady(renderThreadHandler);
        //UI thread;
        mRenderThreadHandler = renderThreadHandler;

        if (this.mPostStartFlag)
        {
            mRenderThreadHandler.sendMessage(Message.obtain(mRenderThreadHandler, MSG_START));
            mPostStartFlag = false;
        }
    }

    @Override
    protected void onRenderThreadDestroy()
    {
        mRenderThreadHandler = null;
        super.onRenderThreadDestroy();
    }

    @Override
    protected void onDrawInBackground(Canvas canvas)
    {
        // 1. time unit
        //drawTimeUnit(canvas);

        // 1. background
        canvas.drawColor(this.getContext().getResources().getColor(R.color.timeline_background));

        // 2. progress bar

        long now = TimeConfig.getFormatedTime();

        if (isStop())
        {
            now = this.mTimeBarStopEvent.getTime();
        }

        drawTextBlock(canvas, now);
        drawPictureBlock(canvas, now);

    }

    private void drawTextBlock(Canvas canvas, long now)
    {

        /**
         * Logic :
         *      start from left, and the time will be now.
         *      each time add a unit_time and judge whether you have to draw the separator.
         * */

        //canvas.drawRect(mTimeTextBlock.left, mTimeTextBlock.top, mTimeTextBlock.right, mTimeTextBlock.bottom, mTextBlockBackground);


        float left = mTimePictureBlock.left;
        for (long separatorTime = now;
             left <= mTimePictureBlock.right ;
             separatorTime = separatorTime - TimeConfig.UNIT_TIME)      //Becareful, the left side is the past time so subtract the TimeConfig.UNIT_TIME
        {
            if (0 == separatorTime % TimeConfig.LONG_SEPARATOR_TIME_UNIT) {
                float separatorLeft = left;
                float separatorTop = mTimeTextBlock.bottom;
                drawTextVerticalAbove_HorizontalCenter(canvas, separatorLeft, separatorTop, convertTimeToString(separatorTime));
            }
            left = left + (int)mTimeUnit_px.width;
        }


    }

    private void drawTextVerticalAbove_HorizontalCenter(Canvas canvas, float x, float y, String text)
    {
        Rect bounds = new Rect();
        mTimeTextPaint.getTextBounds(text, 0, text.length(), bounds);
        float textLeft  = x - bounds.width() / 2;
        float textTop   = y - bounds.height();
        canvas.drawText(text, textLeft, textTop, mTimeTextPaint);

    }

    private void drawPictureBlock(Canvas canvas, long now)
    {
        try
        {
            canvas.save();
            canvas.translate(mTimePictureBlock.left, mTimePictureBlock.top);

            drawTimeBarFromOrigin(canvas, now);
            drawSeparatorFromOrigin(canvas, now);

        }
        catch(Exception e)
        {

        }
        finally {
            canvas.restore();
        }


    }

    private void drawTimeBarFromOrigin(Canvas canvas, long now)
    {
        // The following will draw from the (0,0), in other words, you have translate the canvas to the correct place
        // This must draw in the picture block
        LinkedList<FatigueTimeEvent> list = mLinkedList;
        int size = list.size();
        if (DEBUG) Log.d(TAG, TAG + " render frame now = " + now + ", list.size = " + size);
        for (Iterator<FatigueTimeEvent> iter = list.iterator(); iter.hasNext();)
        {
            FatigueTimeEvent data = iter.next();
            long interval = now - data.getTime();       //TBD
            if (interval >= 0)
            {
                long unitOffset = interval / TimeConfig.UNIT_TIME;     //畫在第幾個格子
                // change unit to pixel
                float left = (int)mTimeUnit_px.width * unitOffset;
                float top = 0;
                float right = left + (int)mTimeUnit_px.width;
                float bottom = mTimePictureBlock.bottom;

                if (left < mTimePictureBlock.right)
                {
                    // unit is still visible.
                    canvas.drawRect(left, top, right, bottom, mPaints.get(data.getType()));
                }
                else
                {
                    // in-visible
                    if (DEBUG) Log.d(TAG, TAG + " onDrawInBackground data = " + data.toString());
                    iter.remove();
                }
            }
        }
    }

    private void drawSeparatorFromOrigin(Canvas canvas, long now)
    {
        // The following will draw from the (0,0), in other words, you have translate the canvas to the correct place
        // This must draw in the picture block
        // Separator is relative to pictureBlock


        /**
         * Logic :
         *      start from left, and the time will be now.
         *      each time add a unit_time and judge whether you have to draw the separator.
         * */

        float left = mTimePictureBlock.left;

        for (long separatorTime = now;
             left <= mTimePictureBlock.right ;
             separatorTime = separatorTime - TimeConfig.UNIT_TIME)      //Becareful, the left side is the past time so subtract the TimeConfig.UNIT_TIME
        {
            if (0 == separatorTime % TimeConfig.LONG_SEPARATOR_TIME_UNIT) {

                float separatorVerticalTop = 0;
                float separatorVerticalBottom = mTimePictureBlock.height();
                float right = left + (int)mTimeSeparator_px.width;
                canvas.drawRect(left, separatorVerticalTop, right, separatorVerticalBottom, mSeparatorPaint);

            } else if (0 == separatorTime % TimeConfig.SHORT_SEPARATOR_TIME_UNIT) {
                float pictureBlockHeight = mTimePictureBlock.height();
                float separatorVerticalTop = (pictureBlockHeight - (int)mTimeSeparator_px.height) / 2.0f;
                float separatorVerticalBottom = pictureBlockHeight - separatorVerticalTop;
                float right = left + (int)mTimeSeparator_px.width;
                canvas.drawRect(left, separatorVerticalTop, right, separatorVerticalBottom, mSeparatorPaint);
            }
            left = left + (int)mTimeUnit_px.width;
        }
    }


    private void clear()
    {
        Handler renderThreadHandler = mRenderThreadHandler;
        if (null != renderThreadHandler) {
            renderThreadHandler.sendMessage(Message.obtain(renderThreadHandler, MSG_CLEAR_DATA));
        }
    }

    public boolean isStop()
    {
        return (null != mTimeBarStopEvent);
    }


    public void add(FatigueTimeEvent newFatigueTimeEvent)
    {
        sendEvent(newFatigueTimeEvent);
    }


    private void sendEvent(FatigueTimeEvent fatigueTimeEvent)
    {
        Handler renderThreadHandler = mRenderThreadHandler;
        if (null != renderThreadHandler) {
            Message msg = Message.obtain(renderThreadHandler, MSG_UPDATE_DATA, fatigueTimeEvent);
            renderThreadHandler.sendMessage(msg);
        }
    }

    public void start()
    {
        if (null != mRenderThreadHandler) {
            mRenderThreadHandler.sendMessage(Message.obtain(mRenderThreadHandler, MSG_START));
        }
        else
        {
            // start comes before the render ready.
            mPostStartFlag = true;
        }
    }

    public void stop()
    {
        mPostStartFlag = false;
        if (null != mRenderThreadHandler)
            mRenderThreadHandler.sendMessage(Message.obtain(mRenderThreadHandler, MSG_STOP));
    }

    public static final int MSG_UPDATE_DATA = SurfaceViewBase.MSG_RENDER_THREAD_CUSTOM_BEGIN + 1;
    public static final int MSG_CLEAR_DATA = SurfaceViewBase.MSG_RENDER_THREAD_CUSTOM_BEGIN + 2;
    public static final int MSG_START = SurfaceViewBase.MSG_RENDER_THREAD_CUSTOM_BEGIN + 3;
    public static final int MSG_STOP = SurfaceViewBase.MSG_RENDER_THREAD_CUSTOM_BEGIN + 4;

    private FatigueTimeEvent mTimeBarStopEvent = new FatigueTimeEvent(FatigueTimeEvent.TYPE_NORMAL);    //Default let the timeline stop.


    @Override
    protected boolean onMessageInBackground(Message msg) {
        boolean processed = super.onMessageInBackground(msg);

        switch (msg.what)
        {
            case MSG_START:
            {
                mTimeBarStopEvent = null;
                break;
            }
            case MSG_STOP:
            {
                mTimeBarStopEvent = new FatigueTimeEvent(FatigueTimeEvent.TYPE_NORMAL);
                clear();
                break;
            }
            case MSG_CLEAR_DATA:
            {
                mLinkedList.clear();
                break;
            }
            case MSG_UPDATE_DATA:
            {
                if (isStop())
                    return true;
                FatigueTimeEvent newData = (FatigueTimeEvent) msg.obj;
                if (0 == mLinkedList.size())
                {
                    mLinkedList.addFirst(newData);
                    if (DEBUG) Log.d(TAG, TAG + " updateData - size 0 insert, type = " + newData.getType() + ", time = " + newData.getTime());
                }
                else
                {

                    //Judge update the event or add.
                    FatigueTimeEvent firstData = mLinkedList.get(0);
                    if (firstData.getTime() == newData.getTime())
                    {
                        // add() already compare the type. only more significant event will coming.
                        mLinkedList.removeFirst();
                        mLinkedList.addFirst(newData);
                        if (DEBUG) Log.d(TAG, TAG + " updateData - time same[Update] firstType " + firstData.getType() + ", newType = " + newData.getType() + ", time = " + newData.getTime());
                    }
                    else
                    {
                        mLinkedList.addFirst(newData);
                        if (DEBUG) Log.d(TAG, TAG + " updateData - time diff, add first, , newType = " + newData.getType() + ", time = " + newData.getTime());
                    }

                }

                processed = true;
                //dumpList();
                break;
            }
        }
        return processed;
    }

    private void dumpList()
    {
        if (DEBUG) Log.d(TAG, TAG + " dumpList size = " + mLinkedList.size());
        int i = 0;
        for (FatigueTimeEvent data : mLinkedList)
        {
            Log.d(TAG, TAG + " dumpList i = " + data.toString());
            ++i;
        }
    }

    @Override
    protected int getRenderDelay()
    {
        return TimeConfig.UNIT_TIME / 4;
    }


    private String convertTimeToString(long currentTimeMillis)
    {
        mDate.setTime(currentTimeMillis);
        return this.mDateFormat.format(mDate);
    }


}
