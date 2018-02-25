package com.opencv.common.utility;

import android.graphics.PointF;

/**
 * Created by Jason_Fang on 2017/11/21.
 */

public interface IRobotEyeDisplayBitmapCallback {
    void onFaceCoordinateUpdate(PointF leftTop, PointF rightDown);
}
