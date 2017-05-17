package com.example.v_zakudong.customcamerafunc.util;


import android.hardware.Camera;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by v_zakudong on 2017/4/25.
 */

public class CameraUtil {

    public static Camera.Size getProperSize(List<Camera.Size> sizeList, int width,int height) {
        Collections.sort(sizeList, new SizeCompator());
        /*float resultRatio;
        float displayRatio= ((float) height /width);*/
        Camera.Size resultSize = null;
        for (Camera.Size size : sizeList) {
//            resultRatio = ((float) size.width) / size.height;
//            if (resultRatio == displayRatio) {
//                resultSize = size;
//            }
            if (size.width==width&&size.height==height){
                resultSize = size;
            }
        }
        if (resultSize == null) {
            for (Camera.Size size : sizeList) {
                float tempRatio = ((float) size.width) / size.height;
                if (tempRatio == 4f / 3) {
                    resultSize = size;
                }
            }
        }
        return resultSize;
    }

    static class SizeCompator implements Comparator<Camera.Size> {

        @Override
        public int compare(Camera.Size size1, Camera.Size size2) {
            if (size1.width < size2.width || size1.width == size2.width && size1.height < size2.height) {
                return -1;
            } else if (!(size1.width == size2.width && size1.height == size2.height)) {
                return 1;
            }
            return 0;
        }
    }
}
