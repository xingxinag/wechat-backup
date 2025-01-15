package com.example.myxposedmodule.backup;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipOutputStream;
import com.example.myxposedmodule.utils.Logger;
import com.example.myxposedmodule.utils.FileUtils;
import com.example.myxposedmodule.utils.ShellUtils;
import com.example.myxposedmodule.dialog.BackupOptionsDialog;
import com.example.myxposedmodule.dialog.RestoreOptionsDialog;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackupManager {
    private static final String BACKUP_FOLDER = "WeChat_Backup";
    private static final String BACKUP_PREFIX = "wechat_backup_";
    private static final SimpleDateFormat DATE_FORMAT = 
        new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
    private static ExecutorService executor = Executors.newFixedThreadPool(2);

    public interface BackupCallback {
        void onProgress(int progress, String message);
        void onSuccess(String path);
        void onError(String error);
    }

    public static void backup(Context context, BackupOptionsDialog.BackupOptions options, BackupCallback callback) {
        executor.execute(() -> {
            try {
                callback.onProgress(0, "准备备份...");
                
                // 创建临时目录
                File tempDir = new File(context.getCacheDir(), "backup_temp");
                if (!tempDir.exists()) tempDir.mkdirs();

                // 执行备份脚本
                callback.onProgress(20, "正在备份微信数据...");
                String backupPath = executeBackupScript(context, tempDir);
                
                // 处理备份文件
                callback.onProgress(50, "正在处理备份文件...");
                String finalPath;
                
                if (options.compressBackup) {
                    // 压缩文件
                    switch (options.encryptionType) {
                        case NONE:
                            finalPath = compressOnly(backupPath, options.customPath);
                            break;
                        case PASSWORD:
                            finalPath = compressWithPassword(backupPath, options.customPath, options.password);
                            break;
                        case AES:
                            finalPath = compressWithAES(backupPath, options.customPath);
                            break;
                        default:
                            throw new IllegalStateException("Unknown encryption type");
                    }
                } else {
                    // 直接复制
                    finalPath = copyBackup(backupPath, options.customPath);
                }
                
                // 清理临时文件
                callback.onProgress(90, "正在清理临时文件...");
                FileUtils.deleteRecursively(tempDir);
                
                callback.onProgress(100, "备份完成");
                callback.onSuccess(finalPath);
            } catch (Exception e) {
                Logger.e("Backup failed", e);
                callback.onError("备份失败: " + e.getMessage());
            }
        });
    }

    public static void restore(Context context, Uri source, RestoreOptionsDialog.RestoreOptions options, BackupCallback callback) {
        new Thread(() -> {
            try {
                callback.onProgress(0, "准备恢复...");
                
                // 创建临时目录
                File tempDir = new File(context.getCacheDir(), "restore_temp");
                if (!tempDir.exists()) tempDir.mkdirs();

                // 解压并解密备份文件
                callback.onProgress(20, "正在处理备份文件...");
                String extractPath;
                
                switch (options.encryptionType) {
                    case NONE:
                        extractPath = extractOnly(source, tempDir);
                        break;
                    case PASSWORD:
                        extractPath = extractWithPassword(source, tempDir, options.password);
                        break;
                    case AES:
                        extractPath = extractWithAES(source, tempDir);
                        break;
                    default:
                        throw new IllegalStateException("Unknown encryption type");
                }
                
                // 执行恢复脚本
                callback.onProgress(50, "正在恢复数据...");
                executeRestoreScript(context, extractPath);
                
                // 清理临时文件
                callback.onProgress(90, "正在清理临时文件...");
                FileUtils.deleteRecursively(tempDir);
                
                callback.onProgress(100, "恢复完成");
                callback.onSuccess("数据已恢复");
            } catch (Exception e) {
                Logger.e("Restore failed", e);
                callback.onError("恢复失败: " + e.getMessage());
            }
        }).start();
    }

    private static String executeBackupScript(Context context, File tempDir) throws Exception {
        // 准备备份脚本
        File scriptFile = ShellUtils.prepareScript(context, "backup.sh");
        
        // 设置环境变量
        String[] envp = {
            "BACKUP_PATH=" + tempDir.getAbsolutePath(),
            "WECHAT_PATH=/data/data/com.tencent.mm"
        };
        
        // 执行脚本
        int result = ShellUtils.executeScript(scriptFile, envp);
        if (result != 0) {
            throw new Exception("Backup script failed with code: " + result);
        }
        
        return tempDir.getAbsolutePath();
    }

    private static String executeRestoreScript(Context context, String sourcePath) throws Exception {
        // 准备恢复脚本
        File scriptFile = ShellUtils.prepareScript(context, "restore.sh");
        
        // 设置环境变量
        String[] envp = {
            "RESTORE_PATH=" + sourcePath,
            "WECHAT_PATH=/data/data/com.tencent.mm"
        };
        
        // 执行脚本
        int result = ShellUtils.executeScript(scriptFile, envp);
        if (result != 0) {
            throw new Exception("Restore script failed with code: " + result);
        }
        
        return "Success";
    }

    private static String compressBackup(String sourcePath, Uri destination) throws Exception {
        String timestamp = DATE_FORMAT.format(new Date());
        String zipName = BACKUP_PREFIX + timestamp + ".zip";
        
        File zipFile;
        if (destination != null) {
            // 使用用户选择的位置
            zipFile = new File(FileUtils.getPathFromUri(destination));
        } else {
            // 使用默认位置
            File backupDir = new File(Environment.getExternalStorageDirectory(), BACKUP_FOLDER);
            if (!backupDir.exists()) backupDir.mkdirs();
            zipFile = new File(backupDir, zipName);
        }
        
        // 压缩文件
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            FileUtils.zipDirectory(new File(sourcePath), "", zos);
        }
        
        return zipFile.getAbsolutePath();
    }

    private static String extractBackup(Uri source, File tempDir) throws Exception {
        File sourceFile = new File(FileUtils.getPathFromUri(source));
        FileUtils.unzip(sourceFile, tempDir);
        return tempDir.getAbsolutePath();
    }

    private static String extractOnly(Uri source, File tempDir) throws Exception {
        File sourceFile = new File(FileUtils.getPathFromUri(source));
        FileUtils.unzip(sourceFile, tempDir);
        return tempDir.getAbsolutePath();
    }

    private static String extractWithPassword(Uri source, File tempDir, String password) throws Exception {
        File sourceFile = new File(FileUtils.getPathFromUri(source));
        
        // 读取salt和iv
        try (FileInputStream fis = new FileInputStream(sourceFile)) {
            byte[] salt = new byte[16];
            byte[] iv = new byte[16];
            fis.read(salt);
            fis.read(iv);
            
            // 生成密钥
            SecretKey key = generateKey(password, salt);
            
            // 创建解密cipher
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
            
            // 解密并解压
            try (CipherInputStream cis = new CipherInputStream(fis, cipher)) {
                FileUtils.unzip(cis, tempDir);
            }
        }
        
        return tempDir.getAbsolutePath();
    }

    private static String extractWithAES(Uri source, File tempDir) throws Exception {
        // 实现AES解密逻辑
        // ...
        return tempDir.getAbsolutePath();
    }
} 