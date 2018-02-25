package com.opencv.common.widget;

import android.graphics.PointF;

import org.opencv.core.Mat;

/**
 * Created by Jason_Fang on 2017/4/25.
 */

public interface IFrameCallback {
    void onProcessFrame(Mat srcCameraFrame, Mat dstCameraFrame);

    /**
     * RobotEye must bind with RobotVision
     * */
    void onRobotEyeFaceCoordinateUpdate(final PointF leftTop, final PointF rightDown);
}
