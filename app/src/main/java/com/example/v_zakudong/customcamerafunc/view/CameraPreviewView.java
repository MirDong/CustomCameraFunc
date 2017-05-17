package com.example.v_zakudong.customcamerafunc.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;
import com.example.v_zakudong.customcamerafunc.MainActivity;
import com.example.v_zakudong.customcamerafunc.fragment.TakePhotoFragment;
import com.example.v_zakudong.customcamerafunc.instance.CameraInstance;
import com.example.v_zakudong.customcamerafunc.util.CameraUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by v_zakudong on 2017/4/24.
 */

public class CameraPreviewView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder holder;
    public Camera camera;
    private static final String PREVIEW_TAG = "PreviewSize";
    private static final String PICTURE_TAG = "PictureSize";
    private int resolution_width;
    private int resolution_height;
    private byte[] image;
    private static final int SUCCESS = 1;
    private Context mContext;
    private TakeHandler handler = new TakeHandler();
    private String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Camera";
    public static String filePath;

    public CameraPreviewView(Context context) {
        super(context);
        initCamera(context);
    }

    public CameraPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCamera(context);
    }

    //创建相机实例
    public void getCamera() {
        //相机有前置摄像头1与后置摄像头0
//        camera = Camera.open(0);
        if (camera == null) {
            camera = CameraInstance.getInstance();
        }
        camera.setDisplayOrientation(90);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    //初始化相机
    public void initCamera(Context context) {
        mContext = context;
        holder = getHolder();
        holder.addCallback(this);
        if (hasCamera(context)) {
            if (camera == null) {
                //相机有前置摄像头1与后置摄像头0
                getCamera();
            }
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

    //设置相机参数
    public void setParameters(int width, int height) {
        if (camera != null) {
            resolution_width = width;
            resolution_height = height;
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
                            String fileName = DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance()) + "_" + resolution_width + "×" + resolution_height + ".jpg";
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
                                    int rotate = readPictureDegree(file_image.getAbsolutePath());
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

    //旋转图片

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

//读取照片信息的旋转角度

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


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            /*if (camera == null) {
                getCamera();
            }*/
//            camera.lock();
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
       /* if (camera == null) {
            getCamera();
        }*/
        camera.stopPreview();
        camera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera(holder);
    }

    public void releaseCamera(SurfaceHolder holder) {
        if (camera != null) {
            holder.removeCallback(this);
//            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    class TakeHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == SUCCESS) {
                MainActivity activity = (MainActivity) mContext;
                ArrayList<Fragment> fragments = activity.fragments;
                TakePhotoFragment take_fragment = (TakePhotoFragment) fragments.get(0);
                take_fragment.addImage();
                Toast.makeText(mContext, "图片已经保存至" + dir + "文件夹中", Toast.LENGTH_SHORT).show();
            }

            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
