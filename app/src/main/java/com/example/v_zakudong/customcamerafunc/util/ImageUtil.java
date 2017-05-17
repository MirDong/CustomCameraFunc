package com.example.v_zakudong.customcamerafunc.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

/**
 * Created by v_zakudong on 2017/5/3.
 */

public class ImageUtil {
    //获取指定尺寸的图片
    public static Bitmap getBitmapBySize(String path, int reWidth, int reHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int bitHeight = options.outHeight;
        int bitWidth = options.outWidth;
        options.inSampleSize = bitWidth / reWidth > bitHeight / reHeight ? (bitWidth / reWidth) : (bitHeight / reHeight);
        options.inJustDecodeBounds = false;
        Bitmap bitmap2 = BitmapFactory.decodeFile(path, options);
        Bitmap bit = ThumbnailUtils.extractThumbnail(bitmap2, reWidth, reHeight, MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
        return bit;
    }
}
