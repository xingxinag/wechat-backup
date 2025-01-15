package com.example.myxposedmodule.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class WeChatVersionUtil {
    private static final String WECHAT_PACKAGE = "com.tencent.mm";
    
    public static int getWeChatVersionCode(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(WECHAT_PACKAGE, 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e("Failed to get WeChat version", e);
            return -1;
        }
    }

    public static String getWeChatVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(WECHAT_PACKAGE, 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e("Failed to get WeChat version", e);
            return "";
        }
    }

    public static boolean isWeChatInstalled(Context context) {
        try {
            context.getPackageManager().getPackageInfo(WECHAT_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
} 