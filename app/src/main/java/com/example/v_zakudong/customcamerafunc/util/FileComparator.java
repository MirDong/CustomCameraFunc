package com.example.v_zakudong.customcamerafunc.util;

import java.io.File;
import java.util.Comparator;

/**
 * Created by v_zakudong on 2017/4/28.
 */

public class FileComparator implements Comparator<File> {

    @Override
    public int compare(File o1, File o2) {
        if (o1.lastModified() < o2.lastModified()) {
            return -1;
        } else {
            return 1;
        }
    }

}
