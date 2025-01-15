package com.example.myxposedmodule.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ShellUtils {
    private static final String TAG = "ShellUtils";

    public static void executeScript(Context context, String scriptName) throws IOException {
        // 获取微信版本
        int wechatVersion = WeChatVersionUtil.getWeChatVersionCode(context);
        
        // 根据版本设置环境变量
        Map<String, String> environment = new HashMap<>();
        environment.put("WECHAT_VERSION", String.valueOf(wechatVersion));
        
        // 从assets复制脚本到应用私有目录
        File scriptFile = new File(context.getFilesDir(), scriptName);
        copyAsset(context, scriptName, scriptFile);
        
        // 复制command文件夹
        File commandDir = new File(context.getFilesDir(), "command");
        if (!commandDir.exists()) {
            commandDir.mkdirs();
            copyAssetFolder(context, "command", commandDir.getAbsolutePath());
        }

        // 设置执行权限
        scriptFile.setExecutable(true);
        
        try {
            // 执行脚本时传入环境变量
            ProcessBuilder pb = new ProcessBuilder("su", "-c", scriptFile.getAbsolutePath());
            pb.environment().putAll(environment);
            Process process = pb.start();
            
            // 读取脚本输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                Log.d(TAG, line);
            }
            
            // 等待脚本执行完成
            process.waitFor();
        } catch (Exception e) {
            Logger.e("Script execution failed", e);
            throw new IOException("Script execution failed: " + e.getMessage());
        }
    }

    private static void copyAsset(Context context, String assetName, File outFile) throws IOException {
        try (InputStream in = context.getAssets().open(assetName);
             OutputStream out = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }

    private static void copyAssetFolder(Context context, String assetPath, String outPath) throws IOException {
        String[] assets = context.getAssets().list(assetPath);
        if (assets != null) {
            for (String asset : assets) {
                String fullAssetPath = assetPath + "/" + asset;
                String fullOutPath = outPath + "/" + asset;
                
                if (context.getAssets().list(fullAssetPath).length == 0) {
                    // 是文件
                    copyAsset(context, fullAssetPath, new File(fullOutPath));
                } else {
                    // 是文件夹
                    new File(fullOutPath).mkdirs();
                    copyAssetFolder(context, fullAssetPath, fullOutPath);
                }
            }
        }
    }

    public static void executeScriptAsync(Context context, String scriptName, 
            OnScriptExecutionListener listener) {
        new Thread(() -> {
            try {
                executeScript(context, scriptName);
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (listener != null) listener.onSuccess();
                });
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (listener != null) listener.onError(e);
                });
            }
        }).start();
    }

    public interface OnScriptExecutionListener {
        void onSuccess();
        void onError(Exception e);
    }
} 