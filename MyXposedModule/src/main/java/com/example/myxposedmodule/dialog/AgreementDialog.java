package com.example.myxposedmodule.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.example.myxposedmodule.config.ServerConfig;
import com.example.myxposedmodule.utils.Logger;

public class AgreementDialog {
    private static final String PREF_NAME = "agreement";
    private static final String KEY_AGREED = "has_agreed";
    private static final String KEY_AGREEMENT_VERSION = "agreement_version";
    private static final int CURRENT_AGREEMENT_VERSION = 1;
    
    public static void showIfNeeded(Context context, AgreementCallback callback) {
        if (!needsAgreement(context)) {
            callback.onAgreed();
            return;
        }
        
        // 先尝试获取在线协议
        new Thread(() -> {
            String agreement = getOnlineAgreement();
            if (agreement == null) {
                // 如果获取在线协议失败，使用本地协议
                agreement = getLocalAgreement(context);
            }
            String finalAgreement = agreement;
            
            // 在主线程显示协议
            new Handler(Looper.getMainLooper()).post(() -> 
                showAgreement(context, finalAgreement, callback));
        }).start();
    }
    
    private static String getOnlineAgreement() {
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();
                
            Request request = new Request.Builder()
                .url(ServerConfig.AGREEMENT_URL)
                .build();
                
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            }
        } catch (Exception e) {
            Logger.e("Failed to get online agreement", e);
        }
        return null;
    }
    
    private static String getLocalAgreement(Context context) {
        try {
            InputStream is = context.getResources().openRawResource(R.raw.agreement);
            return new BufferedReader(new InputStreamReader(is))
                .lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            Logger.e("Failed to get local agreement", e);
            return getDefaultAgreement();
        }
    }
    
    private static String getDefaultAgreement() {
        return "使用协议\n\n" +
               "1. 本模块仅供学习交流使用，请勿用于非法用途。\n\n" +
               "2. 使用本模块造成的任何损失由用户自行承担。\n\n" +
               "3. 请在使用前备份重要数据。\n\n" +
               "4. 开发者保留随时修改或终止服务的权利。\n\n" +
               "5. 使用本模块即表示同意本协议的所有条款。";
    }
    
    private static void showAgreement(Context context, String agreementText, AgreementCallback callback) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);
        
        TextView textView = new TextView(context);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setTextSize(14);
        textView.setMaxHeight(context.getResources().getDisplayMetrics().heightPixels / 2);
        textView.setVerticalScrollBarEnabled(true);
        textView.setPadding(0, 10, 0, 10);
        textView.setText(agreementText);
        
        TextView hintText = new TextView(context);
        hintText.setText("请仔细阅读以下协议内容");
        hintText.setTextSize(12);
        hintText.setTextColor(Color.GRAY);
        hintText.setPadding(0, 0, 0, 10);
        
        layout.addView(hintText);
        layout.addView(textView);
        
        new AlertDialog.Builder(context)
            .setTitle("使用协议")
            .setView(layout)
            .setCancelable(false)
            .setPositiveButton("同意", (dialog, which) -> {
                markAsAgreed(context);
                callback.onAgreed();
            })
            .setNegativeButton("不同意", (dialog, which) -> {
                callback.onDisagreed();
            })
            .show();
    }
    
    private static boolean needsAgreement(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int agreedVersion = prefs.getInt(KEY_AGREEMENT_VERSION, 0);
        return agreedVersion < CURRENT_AGREEMENT_VERSION;
    }
    
    private static void markAsAgreed(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
            .putBoolean(KEY_AGREED, true)
            .putInt(KEY_AGREEMENT_VERSION, CURRENT_AGREEMENT_VERSION)
            .apply();
    }
    
    public interface AgreementCallback {
        void onAgreed();
        void onDisagreed();
    }
} 