package com.example.v_zakudong.customcamerafunc.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.v_zakudong.customcamerafunc.LargeImageActivity;
import com.example.v_zakudong.customcamerafunc.R;
import com.example.v_zakudong.customcamerafunc.util.FileComparator;
import com.example.v_zakudong.customcamerafunc.view.CameraHandFocus;
import com.example.v_zakudong.customcamerafunc.view.MutiShapeView;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoRecorderFragment extends Fragment implements SurfaceHolder.Callback, View.OnClickListener {
    private SurfaceView surface;
    private Button recorder;
    private Button stop;
    private SurfaceHolder holder;
    private SharedPreferences sp_solution;
    private int temp_width;
    private int temp_height;
    private int width;
    private int height;
    private String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private MediaRecorder mediaRecorder;
    private boolean isRecording;
    private Camera camera;
    private static final int DEFAULT_WIDTH = 1280;
    private static final int DEFAULT_HEIGHT = 720;
    private View view;
    private RelativeLayout video_container;
    private TextView minute;
    private TextView second;
    private int minute_value = 0;
    private int second_value = 0;
    private boolean isTimerCount = false;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (isTimerCount){
                if (second_value==59){
                    second_value = 0;
                    minute_value++;
                    second.setText("0"+second_value);
                    if (minute_value>9){
                        minute.setText(""+minute_value);
                    }else {
                        minute.setText("0"+minute_value);
                    }
                }else {
                    second_value++;
                    //判断分针
                    if (minute_value>9){
                        minute.setText(""+minute_value);
                        //判断秒针
                        if (second_value>9){
                            second.setText(""+second_value);
                        }else {
                            second.setText("0"+second_value);
                        }
                    }else {
                        minute.setText("0"+minute_value);
                        //判断秒针
                        if (second_value>9){
                            second.setText(""+second_value);
                        }else {
                            second.setText("0"+second_value);
                        }
                    }
                }
                //持续计时
                handler.sendEmptyMessageDelayed(100,1000);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_video_recorder, container, false);
        surface = (SurfaceView) view.findViewById(R.id.surface);
        recorder = (Button) view.findViewById(R.id.recorder);
        minute = (TextView) view.findViewById(R.id.minute);
        second = (TextView) view.findViewById(R.id.second);
        stop = (Button) view.findViewById(R.id.stop);
        video_container = (RelativeLayout) view.findViewById(R.id.video_container);
        surface.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getPointerCount() == 1) {
                    CameraHandFocus.handleFocus(event, camera);
                }
                return true;
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        addVideoView();
        setVideoResolution();
    }

    //读取设置的分辨率
    private void setVideoResolution() {
        sp_solution = getActivity().getSharedPreferences("sp_resolution", Context.MODE_PRIVATE);
        String settings = sp_solution.getString("resolution", "480×640");
        String[] strings = settings.split("×");
        temp_width = Integer.valueOf(strings[1]);
        temp_height = Integer.valueOf(strings[0]);
    }

    //视频缩略图
    private void addVideoView() {
        if (video_container != null) {
            File file_list = new File(rootPath + File.separator + "Camera/video");
            if (!file_list.exists()) {
                file_list.mkdirs();
            }
            File[] files = file_list.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    String temp = pathname.getName().toLowerCase();
                    if (temp.endsWith(".mp4")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            List<File> pictures = null;
            if (files.length >= 1) {
                //排序
                pictures = Arrays.asList(files);
                Collections.sort(pictures, new FileComparator());
                video_container.setVisibility(View.VISIBLE);
                video_container.removeAllViews();
                final File file = pictures.get(pictures.size() - 1);
                Bitmap bitmap_temp = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Images.Thumbnails.MICRO_KIND);
                Bitmap bitmap = ThumbnailUtils.extractThumbnail(bitmap_temp, 200, 200);
//                Bitmap bitmap = ImageUtil.getBitmapBySize(file.getPath(), 200, 200);
                /*ImageView image = new ImageView(getActivity());
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                image.setLayoutParams(new LinearLayout.LayoutParams(200, 210));
                image.setImageBitmap(bitmap);*/
                /*image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showLargeVedio();
                    }
                });
                video_container.addView(image);*/
                MutiShapeView shapeView = new MutiShapeView(getActivity());
                shapeView.setImageBitmap(bitmap);
                shapeView.setShape(MutiShapeView.SHAPE_CIRCLE);
                shapeView.setBorderColor(Color.CYAN);
                shapeView.setBorderWidth(2);
                shapeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showLargeVedio();
                    }
                });
                video_container.addView(shapeView);
            } else {
                video_container.setVisibility(View.GONE);
            }
        }
    }

    private void showLargeVedio() {
        Intent intent = new Intent(getActivity(), LargeImageActivity.class);
        intent.putExtra("type", 2);
        startActivity(intent);
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    //初始化相机预览
    private void initCamera() {
        if (view != null) {
            holder = surface.getHolder();
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            holder.addCallback(this);
            //设置分辨率
            holder.setFixedSize(720, 1280);
            if (camera == null) {
                camera = Camera.open();
            }
            camera.lock();
            Camera.Parameters parameters = camera.getParameters();
            List<Camera.Size> sizes = parameters.getSupportedVideoSizes();
            for (Camera.Size size : sizes) {
                if (size.width == temp_width && size.height == temp_height) {
                    width = temp_width;
                    height = temp_height;
                    Log.d("TAG", "视频分辨率:----------- " + size.width + "*" + size.height);
                }
            }
            if (width == 0 && height == 0) {
                width = DEFAULT_WIDTH;
                height = DEFAULT_HEIGHT;
                Toast.makeText(getActivity(), "该手机不支持此分辨率，采用默认分辨率", Toast.LENGTH_SHORT).show();
            }
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            parameters.setPreviewSize(width, height);
            camera.setParameters(parameters);
            camera.setDisplayOrientation(90);
            holder.setKeepScreenOn(true);

            recorder.setOnClickListener(this);
            stop.setOnClickListener(this);
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        holder.removeCallback(this);
        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        initCamera();
        addVideoView();
    }

    @Override
    public void onPause() {
        super.onPause();
        //释放相机资源
        if (camera != null) {
            holder.removeCallback(this);
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //开始录制
            case R.id.recorder:
                if (!isRecording) {
                    recorderVideo();
                } else {
                    //停止计时
                    isTimerCount = false;
                    minute_value = 0;
                    second_value = 0;
                    minute.setText("0"+minute_value);
                    second.setText("0"+second_value);
                    mediaRecorder.stop();
                    mediaRecorder.release();
                    mediaRecorder = null;
//                    recorder.setEnabled(true);
//                    stop.setEnabled(false);
                    recorder.setText("开始录制");
                    isRecording = false;
                    addVideoView();
                    Toast.makeText(getActivity(), "已保存至" + rootPath + File.separator + "Camera/video" + "下", Toast.LENGTH_SHORT).show();
                }
                break;
            /*    break;
            //停止录制
            case R.id.stop:*/
        }
    }

    //开始录制
    public void recorderVideo() {
        initRecorder();
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            //开始计时
            isTimerCount = true;
            handler.sendEmptyMessageDelayed(100, 1000);
//            recorder.setEnabled(false);
            recorder.setText("停止录制");
            stop.setEnabled(true);
            isRecording = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //初始化MediaRecorder
    public void initRecorder() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(getActivity(), "SD卡没有安装", Toast.LENGTH_SHORT).show();
            return;
        }
        CharSequence name = DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance()) + "_" + width + "×" + height + ".mp4";
        String videoPath = rootPath + File.separator + "Camera/video" + File.separator + name;
//        String videoPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/test.mp4";
        mediaRecorder = new MediaRecorder();
        mediaRecorder.reset();
        camera.unlock();
        mediaRecorder.setCamera(camera);
        //设置视频声音来源
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //设置视频来源
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        //设置视频输出格式
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        //设置声音的编码格式
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        //设置视频的编码格式
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        //设置录制视频的宽高
//        mediaRecorder.setVideoSize(Integer.valueOf(width),Integer.valueOf(height));
        mediaRecorder.setVideoSize(width, height);
        //设置视频保存文件
        mediaRecorder.setOutputFile(videoPath);
        //视频采样率 每秒40帧
        mediaRecorder.setVideoFrameRate(40);
        mediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);
        //设置录制后的视频旋转90度至竖直方向
        mediaRecorder.setOrientationHint(90);
        //设置视频显示到SurfaceView上
        mediaRecorder.setPreviewDisplay(holder.getSurface());
        mediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mr, int what, int extra) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;
                recorder.setEnabled(true);
                stop.setEnabled(false);
            }
        });
       /* try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            mediaRecorder.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void onDestroy() {
        if (camera != null) {
            holder.removeCallback(this);
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }

        if (mediaRecorder != null && isRecording) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
        super.onDestroy();
    }
}
