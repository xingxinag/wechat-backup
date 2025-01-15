package com.example.myxposedmodule;

import android.content.Context;
import android.content.SharedPreferences;

public class Config {
    private static final String PREF_NAME = "backup_config";
    private static SharedPreferences prefs;

    public static void init(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void setBackupPath(String path) {
        prefs.edit().putString("backup_path", path).apply();
    }

    public static String getBackupPath() {
        return prefs.getString("backup_path", "/storage/emulated/0/WeChat_backup");
    }

    // 添加其他配置项...
} 