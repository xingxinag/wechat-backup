package com.example.myxposedmodule.utils;

import android.content.Context;
import java.io.*;

public class ErrorRecovery {
    private static final String BACKUP_EXTENSION = ".bak";
    
    public static void backupFile(String filePath) {
        try {
            File original = new File(filePath);
            if (original.exists()) {
                File backup = new File(filePath + BACKUP_EXTENSION);
                FileUtils.copyFile(original, backup);
            }
        } catch (Exception e) {
            Logger.e("Failed to backup file: " + filePath, e);
        }
    }
    
    public static void restoreFile(String filePath) {
        try {
            File backup = new File(filePath + BACKUP_EXTENSION);
            if (backup.exists()) {
                File original = new File(filePath);
                FileUtils.copyFile(backup, original);
                backup.delete();
            }
        } catch (Exception e) {
            Logger.e("Failed to restore file: " + filePath, e);
        }
    }
} 