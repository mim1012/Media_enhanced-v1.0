package com.media.player.service.utils;

import android.util.Log;

/**
 * 로거 유틸리티 (원본: LogUtils)
 */
public class Logger {
    private static final String TAG = Config.LOG_TAG;
    private static final boolean DEBUG = Config.DEBUG_MODE;
    
    public static void log(String message) {
        // 디버그 모드 관계없이 항상 로그 출력
        Log.d(TAG, message);
        
        // 로컬 로그만 저장 (서버 업로드 제거)
        DataStore.mLogContent += message + "\n";
        
        // 일정 크기 이상이면 로컬 파일로 저장
        if (DataStore.mLogContent.length() > DataStore.MIN_LOG_LENGTH) {
            saveToLocalFile(DataStore.mLogContent);
            DataStore.mLogContent = "";
        }
    }
    
    public static void log(Exception e) {
        if (DEBUG) {
            Log.e(TAG, "Error", e);
        }
        log("Error: " + e.getMessage());
    }
    
    // 로컬 파일로 로그 저장
    private static void saveToLocalFile(String logContent) {
        // 로컬 파일에 로그 저장 (FileHelper 사용)
        if (DEBUG) {
            Log.d(TAG, "Save log to file: " + logContent.length() + " bytes");
        }
        // FileHelper.saveLogFile(logContent);
    }
    
    // 서버 업로드 메서드 (주석 처리 - 로컬 전용)
    /*
    public static void uploadLog(String logContent) {
        // 서버로 로그 업로드 - 로컬 환경에서는 사용 안함
        if (DEBUG) {
            Log.d(TAG, "Upload log: " + logContent.length() + " bytes");
        }
    }
    
    public static void uploadLog(Exception e) {
        uploadLog("Exception: " + e.toString());
    }
    */
    
    // 호환성 유지용 빈 메서드
    public static void uploadLog(String logContent) { }
    public static void uploadLog(Exception e) { }
}