package com.example.v_zakudong.customcamerafunc;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by v_zakudong on 2017/5/22.
 */

public class App extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)){
            return;
        }
        LeakCanary.install(this);
    }
}
