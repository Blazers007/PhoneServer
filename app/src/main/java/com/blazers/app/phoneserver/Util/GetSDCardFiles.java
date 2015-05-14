package com.blazers.app.phoneserver.Util;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by liang on 2015/5/14.
 */
public class GetSDCardFiles {

    public static File[] getFilesByPath(String path) {
        File[] files;
        if (path.equals("")) {
            files = Environment.getExternalStorageDirectory().listFiles();
        } else {
            File file = new File(path);
            files = file.listFiles();
        }
        return files;
    }


}
