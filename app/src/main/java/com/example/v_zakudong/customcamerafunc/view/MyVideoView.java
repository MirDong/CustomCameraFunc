package com.example.v_zakudong.customcamerafunc.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

import static android.view.View.MeasureSpec.EXACTLY;

/**
 * Created by v_zakudong on 2017/5/8.
 */

public class MyVideoView extends VideoView{
    public MyVideoView(Context context) {
        super(context);
    }

    public MyVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode==EXACTLY&&heightMode==EXACTLY){
            setMeasuredDimension(width,height);
        }else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
