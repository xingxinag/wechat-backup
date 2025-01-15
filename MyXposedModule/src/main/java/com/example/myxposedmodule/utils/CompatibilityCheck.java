package com.example.myxposedmodule.utils;

import android.content.Context;
import android.content.pm.PackageManager;

public class CompatibilityCheck {
    public static boolean isCompatible(Context context) {
        try {
            int wechatVersion = WeChatVersionUtil.getWeChatVersionCode(context);
            if (wechatVersion == -1) {
                return false;
            }
            
            // 检查Android版本兼容性
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                return false;
            }
            
            // 检查LSPosed版本
            PackageManager pm = context.getPackageManager();
            try {
                pm.getPackageInfo("org.lsposed.manager", 0);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        } catch (Exception e) {
            Logger.e("Compatibility check failed", e);
            return false;
        }
    }
} 