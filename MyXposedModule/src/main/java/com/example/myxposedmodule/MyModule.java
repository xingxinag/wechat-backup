package com.example.myxposedmodule;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MyModule implements IXposedHookLoadPackage {
    private static final String TAG = "WeChatBackup";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.tencent.mm")) {
            return;
        }

        try {
            // 获取微信版本
            int version = getWeChatVersion(lpparam.classLoader);
            hookWeChatByVersion(lpparam.classLoader, version);
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": Failed to hook WeChat: " + t.getMessage());
        }
    }

    private int getWeChatVersion(ClassLoader classLoader) {
        try {
            Class<?> clazz = classLoader.loadClass("com.tencent.mm.protocal.protobuf.Config");
            Object version = XposedHelpers.getStaticObjectField(clazz, "PROTOCOL_VERSION");
            return (int) version;
        } catch (Throwable t) {
            // 如果获取版本失败，尝试其他方式
            return -1;
        }
    }

    private void hookWeChatByVersion(ClassLoader classLoader, int version) {
        // 根据不同版本使用不同的hook方法
        if (version >= 1380) { // WeChat 8.0.0 及以上
            hookNewVersion(classLoader);
        } else if (version >= 1360) { // WeChat 7.0.0 - 7.0.22
            hookOldVersion(classLoader);
        } else { // 更早的版本
            hookLegacyVersion(classLoader);
        }
    }

    private void hookNewVersion(ClassLoader classLoader) {
        // 适配新版本微信的hook逻辑
        try {
            Class<?> storageClass = classLoader.loadClass("com.tencent.mm.storage.MicroMsg");
            XposedHelpers.findAndHookMethod(storageClass, "getFilePath", String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    // 处理新版本的文件路径
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": Failed to hook new version: " + t.getMessage());
        }
    }

    private void hookOldVersion(ClassLoader classLoader) {
        // 适配旧版本微信的hook逻辑
        try {
            Class<?> storageClass = classLoader.loadClass("com.tencent.mm.storage.MicroMsg");
            XposedHelpers.findAndHookMethod(storageClass, "getDataPath", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    // 处理旧版本的文件路径
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": Failed to hook old version: " + t.getMessage());
        }
    }

    private void hookLegacyVersion(ClassLoader classLoader) {
        // 适配更早版本微信的hook逻辑
        try {
            Class<?> storageClass = classLoader.loadClass("com.tencent.mm.d.b");
            XposedHelpers.findAndHookMethod(storageClass, "getPath", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    // 处理早期版本的文件路径
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": Failed to hook legacy version: " + t.getMessage());
        }
    }
} 