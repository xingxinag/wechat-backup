package com.example.myxposedmodule.utils;

import android.util.Log;
import de.robv.android.xposed.XposedBridge;

public class Logger {
    private static final String TAG = "WeChatBackup";
    private static final boolean DEBUG = true;

    public static void d(String message) {
        if (DEBUG) {
            Log.d(TAG, message);
            XposedBridge.log(TAG + ": " + message);
        }
    }

    public static void i(String message) {
        Log.i(TAG, message);
        XposedBridge.log(TAG + ": " + message);
    }

    public static void e(String message, Throwable e) {
        Log.e(TAG, message, e);
        XposedBridge.log(TAG + ": " + message + "\n" + Log.getStackTraceString(e));
    }
} 