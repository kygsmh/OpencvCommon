package com.opencv.common.utility;

import android.hardware.Camera;
import org.opencv.android.CameraBridgeViewBase;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Jason_Fang on 2017/4/14.
 */

class CameraSizeHelper
{
    public static final String TAG = "CameraSizeHelper";

    static Camera.Size getSuggestCameraSize(List<Camera.Size> cameraSupportedSizes,
                                      CameraBridgeViewBase.ListItemAccessor accessor,
                                      int maxCameraWidth,
                                      int maxCameraHeight,
                                      int minCameraWidth,
                                      int minCameraHeight,
                                      int cameraViewWidth,
                                      int cameraViewHeight)
    {
        LinkedList<Camera.Size> supportedSize = new LinkedList<Camera.Size>(cameraSupportedSizes);
        Collections.sort(supportedSize, new CameraSizeComparable());


        Camera.Size maxCameraSize = getCameraLimitedSize(supportedSize, maxCameraWidth, maxCameraHeight);
        Camera.Size minCameraSize = getCameraLimitedSize(supportedSize, minCameraWidth, minCameraHeight);


        int desiredCameraArea = cameraViewWidth * cameraViewHeight;
        int maxCameraArea = maxCameraWidth * maxCameraHeight;
        int minCameraArea = minCameraWidth * minCameraHeight;


        if (desiredCameraArea >= maxCameraArea)
            return maxCameraSize;

        if (desiredCameraArea <= minCameraArea)
            return minCameraSize;

        return getCameraLimitedSize(supportedSize, cameraViewWidth, cameraViewHeight);

    }

    private static Camera.Size getCameraLimitedSize (LinkedList<Camera.Size> sortedList, int width, int height)
    {
        Camera.Size result = getCameraByWidthHeight(sortedList, width, height);
        if (result != null)
            return result;

        return getApproximateCameraByArea(sortedList, width, height);
    }

    // Exactly match.
    private static Camera.Size getCameraByWidthHeight (LinkedList<Camera.Size> sortedList, int width, int height) {
        for (Camera.Size size : sortedList) {
            if (size.width == width && size.height == height)
                return size;
        }
        return null;
    }

    private static Camera.Size getApproximateCameraByArea (LinkedList<Camera.Size> sortedList, int width, int height) {
        int desiredArea = width * height;
        Camera.Size largerSize = sortedList.get(0);
        Camera.Size smallerSize = sortedList.get(sortedList.size() - 1);

        for (Camera.Size size : sortedList)
        {
            int area = size.width * size.height;
            if (area > desiredArea)
            {
                largerSize = size;
            }
            else
            {
                smallerSize = size;
                break;
            }
        }

        // smallerSize <= desiredArea < largerSize in the sortedList
        int smallerArea = smallerSize.width * smallerSize.height;
        int largerArea = largerSize.width * largerSize.height;

        if (Math.abs(desiredArea - smallerArea) > Math.abs(desiredArea - largerArea))
            return largerSize;
        else
            return smallerSize;
    }


    private static class CameraSizeComparable implements Comparator<Camera.Size>
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
    }
}
