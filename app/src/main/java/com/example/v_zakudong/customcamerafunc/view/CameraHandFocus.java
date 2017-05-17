package com.example.v_zakudong.customcamerafunc.view;

import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.Log;
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by v_zakudong on 2017/5/4.
 * 触摸聚焦功能
 * 相机预览中心是(0,0),左上角是(-1000,-1000),右下角是(1000,1000)
 * 矩形聚焦区域
 */

/**
 * 在setFocusAreas()同时修改相机对焦模式为macro等，待对焦完毕后，再将对焦模式修改为用户之前定义
*/
public class CameraHandFocus {
    public static void handleFocus(MotionEvent event, Camera camera){
        final Camera.Parameters parameters = camera.getParameters();
        Camera.Size previewSize = parameters.getPreviewSize();
        Rect focusRect = calcculateTapArea(event.getX(),event.getY(),1f,previewSize);
        camera.cancelAutoFocus();
        if (parameters.getMaxNumFocusAreas()>0){
            List<Camera.Area>focusArea = new ArrayList<>();
            focusArea.add(new Camera.Area(focusRect,800));
            parameters.setFocusAreas(focusArea);
        }else {
            Log.d("TAG", "focus areas not supported");
        }
        final String currentFocusMode = parameters.getFocusMode();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
        camera.setParameters(parameters);
        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success){
                    Camera.Parameters params = camera.getParameters();
                    params.setFocusMode(currentFocusMode);
                    camera.setParameters(params);
                }
            }
        });

    }

    /**
     * 接收触摸点的坐标，返回转换后坐标介于[-1000,1000]之间
    */
    private static Rect calcculateTapArea(float x,float y,float coefficient,Camera.Size previewSize){
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize*coefficient).intValue();
        int centerX = (int)(x/previewSize.width-1000);
        int centerY = (int)(y/previewSize.height-1000);
        int left =clamp(centerX-areaSize/2,-1000,1000);
        int top =clamp(centerY-areaSize/2,-1000,1000);
        RectF rectF = new RectF(left,top,left+areaSize,top+areaSize);
        return new Rect(Math.round(rectF.left),Math.round(rectF.top),Math.round(rectF.right),Math.round(rectF.bottom));
    }

    private static int clamp(int x,int min,int max){
        if (x>max){
            return max;
        }
        if (x<min){
            return min;
        }
        return x;
    }
}
