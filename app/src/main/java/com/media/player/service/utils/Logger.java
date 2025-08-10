package com.media.player.service.utils;

import android.util.Log;

/**
 * 로거 유틸리티 (원본: LogUtils)
 */
public class Logger {
    private static final String TAG = Config.LOG_TAG;
    private static final boolean DEBUG = Config.DEBUG_MODE;
    
    public static void log(String message) {
        if (DEBUG) {
            Log.d(TAG, message);
        }
        
        // 로그 누적
        DataStore.mLogContent += message + "\n";
        
        // 일정 크기 이상이면 업로드 (현재는 스텁)
        if (DataStore.mLogContent.length() > DataStore.MIN_LOG_LENGTH) {
            uploadLog(DataStore.mLogContent);
            DataStore.mLogContent = "";
        }
    }
    
    public static void log(Exception e) {
        if (DEBUG) {
            Log.e(TAG, "Error", e);
        }
        log("Error: " + e.getMessage());
    }
    
    public static void uploadLog(String logContent) {
        // 서버로 로그 업로드 (현재는 스텁)
        // 실제 구현시 백그라운드 스레드에서 실행
        if (DEBUG) {
            Log.d(TAG, "Upload log: " + logContent.length() + " bytes");
        }
    }
    
    public static void uploadLog(Exception e) {
        uploadLog("Exception: " + e.toString());
    }
}