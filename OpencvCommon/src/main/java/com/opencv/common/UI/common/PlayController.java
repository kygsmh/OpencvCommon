package com.opencv.common.UI.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.opencv.common.R;
import com.opencv.common.UI.util.Log;

/**
 * Created by Jason_Fang on 2016/9/20.
 */
public class PlayController extends ImageView
{
    private static final String TAG = "PlayController";
    public interface StatusListener
    {
        void onPlayControllerStateChange(PlayController view, boolean newState);
    }

    private StatusListener mStatusListener = null;
    private static final int[] PLAY_CONTROLLER_STATE = {R.attr.isPlaying};

    private boolean mIsPlaying = false;

    public PlayController(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public PlayController(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public PlayController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    public PlayController(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        TypedArray typedArray = null;
        try {
            typedArray = context.getTheme().obtainStyledAttributes(attrs,
                    R.styleable.PlayController,
                    0, 0);
            mIsPlaying = typedArray.getBoolean(R.styleable.PlayController_isPlaying, false);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if (null != typedArray)
                typedArray.recycle();
        }

        this.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                toggle();
            }
        });
    }

    public void setListener(StatusListener listener)
    {
        mStatusListener = listener;
    }

    public boolean isPlaying()
    {
        return mIsPlaying;
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace)
    {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (mIsPlaying)
        {
            mergeDrawableStates(drawableState, PLAY_CONTROLLER_STATE);
        }
        return drawableState;
    }

    public void toggle()
    {
        setPlaying(!mIsPlaying);
    }

    private void setPlaying(boolean newState)
    {
        if (mIsPlaying != newState)
        {
            Log.d(TAG, "[setPlaying] previous state = " + mIsPlaying + ", newState = " + newState);
            mIsPlaying = newState;
            refreshDrawableState();

            if (null != mStatusListener)
            {
                mStatusListener.onPlayControllerStateChange(this, newState);
            }
        }
    }

}
