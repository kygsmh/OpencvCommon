package com.opencv.common.utility;

//import android.util.Log;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;

/**
 * Created by Jason_Fang on 2017/5/23.
 */

public class Utilities {
    private static final String TAG = "Utilities";
    public static boolean save2File(String savedFolder, String savedFileName, Mat inputMat)
    {
        try
        {
            File folder = new File (savedFolder);
            if (!folder.exists())
                folder.mkdirs();

            String filePath = savedFolder + "/" + savedFileName;
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            LogHelper.d(TAG, "path = " + file.getPath());
            return Imgcodecs.imwrite(file.getPath(), inputMat);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
}
