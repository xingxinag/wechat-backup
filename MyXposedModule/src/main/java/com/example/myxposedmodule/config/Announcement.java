package com.example.myxposedmodule.config;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Announcement {
    private static final String PREF_NAME = "announcement";
    private static final String KEY_LAST_READ = "last_read_id";
    private static final String ANNOUNCEMENT_URL = "https://your-server.com/announcement.json";
    private static final int DEFAULT_TIMEOUT = 10; // 秒
    
    public static class AnnouncementItem {
        public int id;
        public String title;
        public String content;
        public String date;
        public boolean important;
    }
    
    public static void checkAnnouncement(Context context, AnnouncementCallback callback) {
        new Thread(() -> {
            // 先尝试获取在线公告
            AnnouncementItem onlineAnnouncement = getOnlineAnnouncement(context);
            if (onlineAnnouncement != null) {
                callback.onNewAnnouncement(onlineAnnouncement);
                return;
            }

            // 如果在线获取失败，使用本地公告
            AnnouncementItem localAnnouncement = getLocalAnnouncement(context);
            if (localAnnouncement != null) {
                callback.onNewAnnouncement(localAnnouncement);
            }
        }).start();
    }
    
    private static AnnouncementItem getOnlineAnnouncement(Context context) {
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();

            Request request = new Request.Builder()
                .url(ANNOUNCEMENT_URL)
                .build();
                
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String json = response.body().string();
                List<AnnouncementItem> announcements = new Gson().fromJson(
                    json, 
                    new TypeToken<List<AnnouncementItem>>(){}.getType()
                );
                
                if (!announcements.isEmpty()) {
                    int lastReadId = getLastReadId(context);
                    AnnouncementItem latest = announcements.get(0);
                    
                    if (latest.id > lastReadId) {
                        return latest;
                    }
                }
            }
        } catch (Exception e) {
            Logger.e("Failed to get online announcement", e);
        }
        return null;
    }
    
    private static AnnouncementItem getLocalAnnouncement(Context context) {
        try {
            InputStream is = context.getResources().openRawResource(R.raw.default_announcement);
            String json = new BufferedReader(new InputStreamReader(is))
                .lines().collect(Collectors.joining("\n"));
            
            List<AnnouncementItem> announcements = new Gson().fromJson(
                json,
                new TypeToken<List<AnnouncementItem>>(){}.getType()
            );
            
            if (!announcements.isEmpty()) {
                int lastReadId = getLastReadId(context);
                AnnouncementItem latest = announcements.get(0);
                
                if (latest.id > lastReadId) {
                    return latest;
                }
            }
        } catch (Exception e) {
            Logger.e("Failed to get local announcement", e);
        }
        return null;
    }
    
    public static void markAsRead(Context context, int announcementId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_LAST_READ, announcementId).apply();
    }
    
    private static int getLastReadId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_LAST_READ, 0);
    }
    
    public interface AnnouncementCallback {
        void onNewAnnouncement(AnnouncementItem announcement);
    }
} 