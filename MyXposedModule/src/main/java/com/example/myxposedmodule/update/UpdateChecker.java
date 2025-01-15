package com.example.myxposedmodule.update;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.example.myxposedmodule.config.ServerConfig;
import com.example.myxposedmodule.utils.Logger;

public class UpdateChecker {
    public interface UpdateCallback {
        void onUpdateAvailable(VersionInfo newVersion);
        void onNoUpdate();
        void onError(String message);
    }

    public static class VersionInfo {
        public int versionCode;
        public String versionName;
        public String changelog;
        public String downloadUrl;
        public boolean forceUpdate;
    }

    public static void checkUpdate(Context context, UpdateCallback callback) {
        new Thread(() -> {
            try {
                // 获取当前版本
                PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                int currentVersion = pInfo.versionCode;

                // 获取服务器版本
                OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(ServerConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(ServerConfig.READ_TIMEOUT, TimeUnit.SECONDS)
                    .build();

                Request request = new Request.Builder()
                    .url(ServerConfig.VERSION_URL)
                    .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    VersionInfo versionInfo = new Gson().fromJson(
                        response.body().string(),
                        VersionInfo.class
                    );

                    if (versionInfo.versionCode > currentVersion) {
                        callback.onUpdateAvailable(versionInfo);
                    } else {
                        callback.onNoUpdate();
                    }
                }
            } catch (Exception e) {
                Logger.e("Update check failed", e);
                callback.onError("检查更新失败：" + e.getMessage());
            }
        }).start();
    }
} 