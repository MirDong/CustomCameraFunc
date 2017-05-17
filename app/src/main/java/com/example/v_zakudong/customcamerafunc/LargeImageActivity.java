package com.example.v_zakudong.customcamerafunc;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.v_zakudong.customcamerafunc.util.FileComparator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class LargeImageActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "TAG";
    private ViewPager view_pager;
    private ArrayList<View> images = new ArrayList<>();
    private String rootPath;
    private boolean isSDOk = false;
    private PictureAdapter adapter;
    private TextView back;
    private int current_position = 0;
    private File file_list;
    private View fir_view;
    private TextView delete;
    //图片标记
    private static final int FROM_IMAGE = 1;
    //视频标记
    private static final int FROM_VIDEO = 2;
    private int type;
    private int screen_width;
    private int screen_height;
    private ProgressBar progress_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_viewpager);
        progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
        view_pager = (ViewPager) findViewById(R.id.view_pager);
        view_pager.setOffscreenPageLimit(0);
        back = (TextView) findViewById(R.id.back);
        delete = (TextView) findViewById(R.id.delete);
        back.setOnClickListener(this);
        delete.setOnClickListener(this);
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Log.d("TAG--------------", t + "uncaughtException: " + e.toString());
            }
        });
        screen_width = getResources().getDisplayMetrics().widthPixels;
        screen_height = getResources().getDisplayMetrics().heightPixels;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            Toast.makeText(this, "请插入SD卡", Toast.LENGTH_SHORT);
        }
        type = getIntent().getIntExtra("type", 1);
        if (rootPath != null) {
            if (type == FROM_IMAGE) {
                file_list = new File(rootPath + File.separator + "Camera/picture");
                if (!file_list.exists()) {
                    file_list.mkdirs();
                }
            } else if (type == FROM_VIDEO) {
                file_list = new File(rootPath + File.separator + "Camera/video");
                if (!file_list.exists()) {
                    file_list.mkdirs();
                }
            }
        }
        adapter = new PictureAdapter();
        view_pager.setAdapter(adapter);
        view_pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                current_position = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        images.clear();
        addView(file_list, images);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void addView(File file_list, ArrayList<View> images) {
        File[] files = null;
        if (type == FROM_IMAGE) {
            files = file_list.listFiles(fileFilter);
        } else if (type == FROM_VIDEO) {
            files = file_list.listFiles(fileFilter_video);
        }
        if (files.length >= 1) {
            //排序
            List<File> asList = Arrays.asList(files);
            Collections.sort(asList, new FileComparator());
            for (int i = asList.size() - 1; i >= 0; i--) {
                View image_show = inflateView(this, files[i].getAbsolutePath());
                images.add(image_show);
            }
            if (adapter == null) {
                adapter = new PictureAdapter();
                view_pager.setAdapter(adapter);
            } else {
                try {
                    adapter.notifyDataSetChanged();
                } catch (IndexOutOfBoundsException e) {
                    Log.d(TAG, "file is deleted");
                }
            }
        }
    }

    //解析布局视图
    public View inflateView(Context context, final String path) {
        View view = null;
        if (type == FROM_IMAGE) {
            view = LayoutInflater.from(context).inflate(R.layout.activity_video, null);
            ImageView image_show = (ImageView) view.findViewById(R.id.image_show);
//            Bitmap bitmap = BitmapFactory.decodeFile(path);
            Bitmap bitmap = readBitmap(context, path);
//            BitmapFactory.decodeStream()
            if (bitmap != null) {
                image_show.setImageBitmap(bitmap);
                /*if (!bitmap.isRecycled()){
                    bitmap.recycle();
                    System.gc();
                }*/
            }
        } else if (type == FROM_VIDEO) {
            view = LayoutInflater.from(context).inflate(R.layout.videoview, null);
            ImageView image_video = (ImageView) view.findViewById(R.id.video_show);
            ImageView play = (ImageView) view.findViewById(R.id.play);
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LargeImageActivity.this, VideoPlayerActivity.class);
                    intent.putExtra("path", path);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    /*Uri uri = Uri.parse(path);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri,"video/mp4");
                    startActivity(intent);*/
                }
            });
            Bitmap bitmap_temp = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);
            Bitmap bitmap = ThumbnailUtils.extractThumbnail(bitmap_temp, screen_width, screen_height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            image_video.setImageBitmap(bitmap);
        }
        return view;
    }

    //设置Bitmap格式为Bitmap.Config.565，避免内存消耗
    public Bitmap readBitmap(Context context, String path) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        FileInputStream inputStream = null;
        BufferedInputStream buffer = null;
        Bitmap bitmap = null;
        try {
            inputStream = new FileInputStream(path);
            buffer = new BufferedInputStream(inputStream);
            bitmap = BitmapFactory.decodeStream(buffer, null, opt);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (buffer != null) {
                try {
                    buffer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return bitmap;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
            case R.id.delete:
                //执行删除文件操作
                File[] files = null;
                if (type == FROM_IMAGE) {
                    files = file_list.listFiles(fileFilter);
                } else if (type == FROM_VIDEO) {
                    files = file_list.listFiles(fileFilter_video);
                }
                List<File> asList = null;
                if (files.length >= 1) {
                    //排序
                    asList = Arrays.asList(files);
                    Collections.sort(asList, new FileComparator());
                    asList.get(asList.size() - 1 - current_position).delete();
                }
                if (images.size() > 0) {
                    images.clear();
                }
                addView(file_list, images);
                int count = 0;
                //切换到上一张图片
                if (adapter != null) {
//                    adapter.notifyDataSetChanged();
                    count = adapter.getCount();
                }
                if (count >= 1) {
                    if (current_position > 0) {
                        view_pager.setCurrentItem(current_position - 1);
                    } else {
                        view_pager.setCurrentItem(current_position);
                    }
                } else {
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
//                deleteUpdatePager();
                break;
        }
    }

    //执行文件删除更新操作，由于onResume()方法中已经加载数据，为了减少消耗，就不再重新加载
    public void deleteUpdatePager() {
        //首先从集合中删除指定位置的View
        /*View view = images.get(current_position);
        if (view!=null){
            deleteItem(images,view);
        }*/
        if (type == FROM_IMAGE) {
            //异步执行删除文件操作
            new Thread(new Runnable() {
                @Override
                public void run() {
                    File[] files = file_list.listFiles(fileFilter);
                    List<File> asList = null;
                    if (files.length > 1) {
                        //排序
                        asList = Arrays.asList(files);
                        Collections.sort(asList, new FileComparator());
                    }
                    asList.get(asList.size() - 1 - current_position).delete();
                    Log.d("TAG", "run: ");
                }
            }).start();
        } else if (type == FROM_VIDEO) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    File[] files = file_list.listFiles(fileFilter_video);
                    List<File> asList = null;
                    if (files.length > 1) {
                        //排序
                        asList = Arrays.asList(files);
                        Collections.sort(asList, new FileComparator());
                    }
                    asList.get(asList.size() - 1 - current_position).delete();
                }
            }).start();
        }
        //更新适配器
        if (adapter != null && images.size() != 0) {
            adapter.notifyDataSetChanged();
        }/*else {
            finish();
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        }*/
        int count = adapter.getCount();
        if (count > 1) {
            //指定删除后要返回的页面
            if (current_position > 0) {
                view_pager.setCurrentItem(current_position - 1);
            } else {
                view_pager.setCurrentItem(current_position);
            }
        } else {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    //从集合中删除指定某个View
    public void deleteItem(ArrayList<View> list, View item) {
        Iterator<View> iterator = list.iterator();
        while (iterator.hasNext()) {
            View view = iterator.next();
            if (view == item) {
                iterator.remove();
            }
        }
    }

    class PictureAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(images.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(images.get(position));
            return images.get(position);
        }

        @Override
        public int getItemPosition(Object object) {
            /*View view = (View) object;
            int position = 0;
            for (int i = 0; i < images.size(); i++) {
                if (images.get(i)==view){
                    position = i;
//                    current_position = i;
                }
            }
            if (position>=0){
                return position;
            }else {

            }*/
            return POSITION_NONE;
        }
    }


    public FileFilter fileFilter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            String temp = pathname.getName().toLowerCase();
            if (temp.endsWith(".png")) {
                return true;
            } else {
                return false;
            }
        }
    };
    public FileFilter fileFilter_video = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            String temp = pathname.getName().toLowerCase();
            if (temp.endsWith(".mp4")) {
                return true;
            } else {
                return false;
            }
        }
    };
}
