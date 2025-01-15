package com.example.myxposedmodule;

import android.content.Context;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    private static final String TAG = "WeChatBackup";
    private String modulePath;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        modulePath = startupParam.modulePath;
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("com.tencent.mm")) {
            hookWeChat(lpparam);
        } else if (lpparam.packageName.equals(BuildConfig.APPLICATION_ID)) {
            // 用于检测模块是否激活
            hookSelf(lpparam);
        }
    }

    private void hookWeChat(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            int version = WeChatVersionUtil.getWeChatVersion(lpparam.classLoader);
            hookWeChatByVersion(lpparam.classLoader, version);
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": Failed to hook WeChat: " + t.getMessage());
        }
    }

    private void hookSelf(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod(
            BuildConfig.APPLICATION_ID + ".MainActivity",
            lpparam.classLoader,
            "isModuleActive",
            XC_MethodReplacement.returnConstant(true)
        );
    }
} 