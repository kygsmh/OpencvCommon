package com.opencv.common.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.opencv.common.R;

/**
 * Created by Jason_Fang on 2017/11/21.
 */

public class DecorationView extends RelativeLayout
{
    public DecorationView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public DecorationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public DecorationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private Context mContext = null;
    private PointF mFaceCoordinateLeftTop = null;
    private PointF mFaceCoordinateRightDown = null;
    private float mFaceWidth = 0;
    private float mFaceHeight = 0;


    int[] mFaceHighlightInnerBorderColor = new int[5];


    int mFaceHighlightInnerBorderSize = 0;


    private void init(Context context, AttributeSet attrs, int defStyleAttr)
    {
        mContext = context;
        setWillNotDraw(false);
        mFaceHighlightInnerBorderSize = (int)mContext.getResources().getDimension(R.dimen.face_highlight_inner_border);
        mFaceHighlightInnerBorderColor[0] = mContext.getResources().getColor(R.color.face_highlight_inner_background_01);
        mFaceHighlightInnerBorderColor[1] = mContext.getResources().getColor(R.color.face_highlight_inner_background_02);
        mFaceHighlightInnerBorderColor[2] = mContext.getResources().getColor(R.color.face_highlight_inner_background_03);
        mFaceHighlightInnerBorderColor[3] = mContext.getResources().getColor(R.color.face_highlight_inner_background_04);
        mFaceHighlightInnerBorderColor[4] = mContext.getResources().getColor(R.color.face_highlight_inner_background_05);
    }
    public void updateFaceCoordinate(PointF leftTop, PointF rightDown)
    {
        mFaceCoordinateLeftTop = leftTop;
        mFaceCoordinateRightDown = rightDown;
        if (null != leftTop && null != rightDown)
        {
            mFaceWidth = rightDown.x - leftTop.x;
            mFaceHeight = rightDown.y - leftTop.y;
        }
        else
        {
            mFaceWidth = 0;
            mFaceHeight = 0;
        }
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        PointF leftTop = mFaceCoordinateLeftTop;
        PointF rightDown = mFaceCoordinateRightDown;
        if (null == leftTop || null == rightDown)
            return;

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(mFaceHighlightInnerBorderSize);
        paint.setStyle(Paint.Style.STROKE);

        // draw inner circle
        float centerX = (leftTop.x + rightDown.x)/2.0f;
        float centerY = (leftTop.y + rightDown.y)/2.0f;
        float innerWidth = rightDown.x - leftTop.x;
        float innerHeight = rightDown.y - leftTop.y;
        float innerRadius = Math.max(innerWidth, innerHeight) / 2.0f;

        SweepGradient gradient =  new SweepGradient(centerX,centerY, this.mFaceHighlightInnerBorderColor, null);
        paint.setShader(gradient);

        canvas.drawCircle(centerX, centerY, innerRadius, paint);

    }

    public PointF[] getAnimatorCoordinateAndSize()
    {
        PointF leftTop = mFaceCoordinateLeftTop;
        PointF rightDown = mFaceCoordinateRightDown;
        if (null == leftTop || null == rightDown)
            return null;


        PointF center = new PointF((leftTop.x + rightDown.x)/2, (leftTop.y+rightDown.y)/2);
        float innerWidth = mFaceWidth;
        float innerHeight = mFaceHeight;
        float radius = Math.max(innerWidth, innerHeight) / 2.0f;

        float animatorSize = Math.max(innerWidth, innerHeight)/3;

        double animatorDegree = Math.PI / 6;        // 30 degree
        PointF animatorCoordinate = new PointF(0,0);
        animatorCoordinate.x = center.x + (float)(Math.cos(animatorDegree) * radius) - animatorSize/2;
        animatorCoordinate.y = center.y - (float)(Math.sin(animatorDegree) * radius) - animatorSize/2;

        PointF pAnimatorSize = new PointF(animatorSize, animatorSize);

        PointF[] result = new PointF[2];
        result[0] = animatorCoordinate;
        result[1] = pAnimatorSize;

        return result;


    }

}
