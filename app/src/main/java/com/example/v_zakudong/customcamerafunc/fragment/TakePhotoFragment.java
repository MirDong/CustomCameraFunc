package com.example.v_zakudong.customcamerafunc.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.v_zakudong.customcamerafunc.LargeImageActivity;
import com.example.v_zakudong.customcamerafunc.R;
import com.example.v_zakudong.customcamerafunc.util.CameraUtil;
import com.example.v_zakudong.customcamerafunc.util.FileComparator;
import com.example.v_zakudong.customcamerafunc.util.ImageUtil;
import com.example.v_zakudong.customcamerafunc.view.CameraHandFocus;
import com.example.v_zakudong.customcamerafunc.view.MutiShapeView;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class TakePhotoFragment extends Fragment implements SurfaceHolder.Callback {
    private SurfaceHolder holder;
    private ImageView take_picture;
    private SurfaceView preview;
    private SharedPreferences sp_solution;
    private String resolution_height;
    private String resolution_width;
    private RelativeLayout image_container;
    private String rootPath;
    private String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Camera/picture";
    private List<ImageView> list = new ArrayList<>();
    private View view;
    private Camera camera;
    private byte[] image;
    public static String filePath;
    private static final int SUCCESS = 1;
    private TakeHandler handler = new TakeHandler();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d("TAG", "TakePhoto------------onCreateView: ");
        view = inflater.inflate(R.layout.fragment_take_photo, container, false);
        initView(view);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return view;
    }

    private void initView(View view) {
        preview = (SurfaceView) view.findViewById(R.id.preview);
        take_picture = (ImageView) view.findViewById(R.id.take_picture);
        image_container = (RelativeLayout) view.findViewById(R.id.container);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("TAG", "TakePhoto------------onActivityCreated: ");
        sp_solution = getActivity().getSharedPreferences("sp_resolution", Context.MODE_PRIVATE);
        take_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePictures();
            }
        });
        preview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getPointerCount()==1){
                    CameraHandFocus.handleFocus(event,camera);
                }
                return false;
            }
        });

    }
    //获取设置的分辨率
    public void getSolution() {
        String settings = sp_solution.getString("resolution", "480×640");
        String[] strings = settings.split("×");
        resolution_height = strings[0];//高
        resolution_width = strings[1];//宽
        setParameters(Integer.valueOf(resolution_width), Integer.valueOf(resolution_height));
    }

    //添加拍照缩略图
    public void addImage() {
        if (image_container != null) {
            File file_list = new File(rootPath + File.separator + "Camera/picture");
            if (!file_list.exists()) {
                file_list.mkdirs();
            }
            File[] files = file_list.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    String temp = pathname.getName().toLowerCase();
                    if (temp.endsWith(".png")) {
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
                list.clear();
                image_container.setVisibility(View.VISIBLE);
                image_container.removeAllViews();
                final File file = pictures.get(pictures.size() - 1);
                Bitmap bitmap = ImageUtil.getBitmapBySize(file.getPath(), 200, 200);
                /*ImageView image = new ImageView(getActivity());
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                image.setLayoutParams(new RelativeLayout.LayoutParams(200, 200));
                image.setImageBitmap(bitmap);*/
                MutiShapeView shapeView = new MutiShapeView(getActivity());
                shapeView.setImageBitmap(bitmap);
                shapeView.setShape(MutiShapeView.SHAPE_CIRCLE);
                shapeView.setBorderColor(Color.CYAN);
                shapeView.setBorderWidth(2);

                shapeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showLargeImage();
                    }
                });
                image_container.addView(shapeView);
               /* image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showLargeImage();
                    }
                });*/
                /*list.add(image);
                image_container.addView(image);*/
            }else {
                image_container.setVisibility(View.GONE);
            }
        }
    }


    //缩略图点击展示为大图
    public void showLargeImage() {
        Intent intent = new Intent(getActivity(), LargeImageActivity.class);
        intent.putExtra("type", 1);
        startActivity(intent);
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onResume() {
        super.onResume();
        //初始化相机操作
        initCamera(getActivity());
        getSolution();
        addImage();
        Log.d("TAG", "TakePhoto------------onResume: ");
    }

    //初始化相机
    public void initCamera(Context context) {
        holder = preview.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        if (hasCamera(context)) {
            //相机有前置摄像头1与后置摄像头0
            getCamera();
        } else {
            Toast.makeText(context, "没有相机功能", Toast.LENGTH_SHORT).show();
        }
    }

    //判断相机是否存在
    public boolean hasCamera(Context context) {
        if (context.getPackageManager().hasSystemFeature("android.hardware.camera")) {
            return true;
        } else {
            return false;
        }
    }

    //创建相机实例
    public void getCamera() {
        //相机有前置摄像头1与后置摄像头0
//        camera = Camera.open(0);
        if (camera == null) {
            try{
                camera = Camera.open();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (camera!=null){
            camera.setDisplayOrientation(90);
        }
    }

    //设置相机参数
    public void setParameters(int width, int height) {
        if (camera != null) {
            //获取相机参数
            Camera.Parameters parameters = camera.getParameters();
//            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            //获取所有图片可支持的分辨率
            List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
            //如果有相同的分辨率就使用相同的分辨率，否则使用高宽比为4：3的分辨率
            Camera.Size pictureSize = CameraUtil.getProperSize(supportedPictureSizes, width, height);
            //设置图片分辨率
            parameters.setPictureSize(pictureSize.width, pictureSize.height);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            parameters.setJpegQuality(100);
            camera.setParameters(parameters);
        }
    }

    //开始拍照
    public byte[] takePictures() {
        if (camera != null) {
            camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(final byte[] data, Camera camera) {
                    image = data;
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            File file = new File(dir);
                            if (!file.exists()) {
                                file.mkdirs();
                            }
                            String fileName = DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance()) + "_" + resolution_width + "×" + resolution_height + ".png";
                            File file_image = new File(dir + "/" + fileName);
                            filePath = file_image.getAbsolutePath();
                            FileOutputStream outputStream = null;
                            if (!file_image.exists()) {
                                try {
                                    file_image.createNewFile();
                                    outputStream = new FileOutputStream(file_image);
                                    outputStream.write(image);
                                    outputStream.flush();
                                    Bitmap bitmap = BitmapFactory.decodeFile(file_image.getAbsolutePath());
//                                    int rotate = readPictureDegree(file_image.getAbsolutePath());
                                    Bitmap map = rotatePicture(bitmap, 90);
                                    saveFileToSD(map);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } finally {
                                    if (outputStream != null) {
                                        try {
                                            outputStream.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                handler.sendEmptyMessage(SUCCESS);
                            }
                        }
                    });
                    thread.start();
                }
            });
            return image;
        }
        return null;
    }

    /**
     * @param bitmap 传入的原图bitmap
     * @return 旋转后的bitmap
     */
    public Bitmap rotatePicture(final Bitmap bitmap, final int degree) {
        Bitmap resizeBitmap = null;
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (resizeBitmap == null) {
            resizeBitmap = bitmap;
        }
        if (resizeBitmap != null && bitmap != resizeBitmap) {
            bitmap.recycle();
        }
        return resizeBitmap;
    }

    /**
     * 读取照片中的旋转角度
     *
     * @param filePath 文件路径
     * @return 角度
     */

    public static int readPictureDegree(String filePath) {
        int degree = 0;
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (exifInterface == null) {
            return degree;
        }
        int orentation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch (orentation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degree = 270;
                break;
        }
        return degree;
    }

    //保存旋转后的文件到SD卡中
    public static void saveFileToSD(Bitmap bitmap) {
        if (bitmap != null) {
            FileOutputStream outputStream = null;
            if (filePath != null) {
                try {
                    outputStream = new FileOutputStream(filePath);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    try {
                        outputStream.flush();
                        outputStream.close();
                        bitmap.recycle();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class TakeHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == SUCCESS) {
                addImage();
                Toast.makeText(getActivity(), "图片已经保存至" + dir + "文件夹中", Toast.LENGTH_SHORT).show();
            }

            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseCamera(holder);
        Log.d("TAG", "TakePhoto------------onPause: ");
    }

    @Override
    public void onStop() {
        super.onStop();
//        releaseCamera(holder);
        Log.d("TAG", "TakePhoto------------onStop: ");
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        camera.stopPreview();
        camera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        releaseCamera(surfaceHolder);
    }

    public void releaseCamera(SurfaceHolder holder) {
        if (camera != null) {
            holder.removeCallback(this);
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
}
