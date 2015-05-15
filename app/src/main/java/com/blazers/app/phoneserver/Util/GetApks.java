package com.blazers.app.phoneserver.Util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by liang on 2015/5/14.
 */
public class GetApks {

    /* 用Concurrent 会导致获取的时候过慢 而 用HashMap 可能多次请求的时候出现问题 */
    private static HashMap<String, Drawable> nameIconMap = new HashMap<>();

    public ArrayList<APKInfo> getInstalled(Context ctx) {
        ArrayList<APKInfo> apkInfoArrayList = new ArrayList<>();
        PackageManager manager = ctx.getPackageManager();
        /* 调用PackageInfo */
        List<PackageInfo> packages = manager.getInstalledPackages(0);
        for(PackageInfo packageInfo : packages) {
           if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM )== 0) {
               apkInfoArrayList.add(new APKInfo(packageInfo.packageName, packageInfo.versionName, packageInfo.versionCode));
               nameIconMap.put(packageInfo.packageName, packageInfo.applicationInfo.loadIcon(manager));
           }
        }
        return apkInfoArrayList;
    }

    public byte[] getInstalledApkIcon(Context ctx, String name) {
        /* 从缓存里面找 */
        if (nameIconMap.containsKey(name)) {
            return FormatTools.getInstance().Drawable2Bytes(nameIconMap.get(name));
        }
        /* 没有继续重新查找 */
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
