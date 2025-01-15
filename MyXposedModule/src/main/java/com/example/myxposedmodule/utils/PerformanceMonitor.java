package com.example.myxposedmodule.utils;

import android.os.SystemClock;
import java.util.HashMap;
import java.util.Map;

public class PerformanceMonitor {
    private static final Map<String, Long> startTimes = new HashMap<>();
    
    public static void start(String operation) {
        startTimes.put(operation, SystemClock.elapsedRealtime());
    }
    
    public static void end(String operation) {
        Long startTime = startTimes.remove(operation);
        if (startTime != null) {
            long duration = SystemClock.elapsedRealtime() - startTime;
            Logger.d(String.format("Operation '%s' took %d ms", operation, duration));
        }
    }
} 