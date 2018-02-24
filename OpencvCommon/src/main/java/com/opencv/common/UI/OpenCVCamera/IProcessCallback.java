package com.opencv.common.UI.OpenCVCamera;

import org.opencv.core.Mat;
import org.opencv.core.Point;

/**
 * Created by Jason_Fang on 2018/2/22.
 */

public interface IProcessCallback {
    public boolean processFrame(Mat inputImage, Mat outputImage, boolean resetSwitch, Point touchPoint, int scale);
}
