package com.kakao.taxi.auto.utils;

import android.util.Log;

/**
 * 로그 유틸리티 클래스
 * 콜 분석 및 처리 과정을 로깅합니다.
 */
public class LogUtils {
    private static final String TAG = "KakaoTaxiAuto";
    
    /**
     * 로그를 업로드하거나 저장
     */
    public static void uploadLog(String logText) {
        if (logText != null && !logText.isEmpty()) {
            Log.d(TAG, logText);
            
            // SharedData에 로그 추가
            if (SharedData.mLogContent.length() > 10000) {
                // 로그가 너무 길면 앞부분 삭제
                SharedData.mLogContent = SharedData.mLogContent.substring(5000);
            }
            SharedData.mLogContent += logText + "\n";
        }
    }
    
    /**
     * 예외 로그
     */
    public static void uploadLog(Exception e) {
        if (e != null) {
            String errorMsg = "Error: " + e.getClass().getSimpleName() + " - " + e.getMessage();
            Log.e(TAG, errorMsg, e);
            uploadLog(errorMsg);
        }
    }
    
    /**
     * 현재 로그 내용 반환
     */
    public static String getCurrentLog() {
        return SharedData.mLogContent;
    }
    
    /**
     * 로그 내용 초기화
     */
    public static void clearLog() {
        SharedData.mLogContent = "";
    }
}