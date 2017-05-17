package com.example.v_zakudong.customcamerafunc.instance;

import android.hardware.Camera;

/**
 * Created by v_zakudong on 2017/5/3.
 */

public class CameraInstance {
    private static volatile Camera camera;
    private CameraInstance() {
    }

    public static Camera getInstance(){
        if (camera==null){
            synchronized (CameraInstance.class){
                if (camera==null){
                    camera = Camera.open();
                }
            }
        }
        return camera;
    }
}
