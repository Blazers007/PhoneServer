package com.blazers.app.phoneserver.Util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liang on 2015/5/14.
 */
public class GetApks {

    public ArrayList<APKInfo> getInstalled(Context ctx) {
        ArrayList<APKInfo> apkInfoArrayList = new ArrayList<>();
        PackageManager manager = ctx.getPackageManager();
        /* 调用PackageInfo */
        List<PackageInfo> packages = manager.getInstalledPackages(0);
        for(PackageInfo packageInfo : packages) {
           if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM )== 0) {
               apkInfoArrayList.add(new APKInfo(packageInfo.packageName, packageInfo.versionName, packageInfo.versionCode));
           }
        }
        return apkInfoArrayList;
    }

    public byte[] getInstalledApkIcon(Context ctx, String name) {
        PackageManager manager = ctx.getPackageManager();
        /* 调用PackageInfo */
        List<PackageInfo> packages = manager.getInstalledPackages(0);
        for(PackageInfo packageInfo : packages) {
            if(packageInfo.packageName.equals(name))
                return FormatTools.getInstance().Drawable2Bytes(packageInfo.applicationInfo.loadIcon(manager));
        }
        return null;
    }

    public class APKInfo {

        public APKInfo(String name, String versionName, int versionCode) {
            this.name = name;
            this.versionName = versionName;
            this.versionCode = versionCode;
        }

        public String name;
        public String versionName;
        public int versionCode;
    }
}
