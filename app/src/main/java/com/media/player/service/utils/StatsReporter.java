package com.media.player.service.utils;

import android.content.Context;
import android.telephony.TelephonyManager;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;

/**
 * 서버 통계 전송 시스템
 */
public class StatsReporter {
    private static final String STATS_URL = "http://localhost:3000/api/auth/stats";
    
    /**
     * 콜 액션 통계 전송 (비동기)
     */
    public static void reportCallAction(Context context, String action, MediaItem item) {
        new Thread(() -> {
            try {
                String phoneNumber = getPhoneNumber(context);
                if (phoneNumber == null) return;
                
                JSONObject stats = new JSONObject();
                stats.put("phone_number", phoneNumber);
                stats.put("action", action);  // "call_accepted" or "call_rejected"
                stats.put("details", createCallDetails(item));
                stats.put("timestamp", System.currentTimeMillis());
                
                sendStats(stats.toString());
                
                Logger.log("통계 전송: " + action + " - " + item.mTarget);
                
            } catch (Exception e) {
                // 통계 전송 실패해도 앱은 계속 동작
                Logger.log("통계 전송 실패: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * 앱 시작 통계 전송
     */
    public static void reportAppStart(Context context) {
        new Thread(() -> {
            try {
                String phoneNumber = getPhoneNumber(context);
                if (phoneNumber == null) return;
                
                JSONObject stats = new JSONObject();
                stats.put("phone_number", phoneNumber);
                stats.put("action", "app_started");
                stats.put("details", "앱 실행");
                stats.put("timestamp", System.currentTimeMillis());
                
                sendStats(stats.toString());
                
            } catch (Exception e) {
                // 무시
            }
        }).start();
    }
    
    /**
     * 콜 상세 정보 생성 (개인정보 보호)
     */
    private static JSONObject createCallDetails(MediaItem item) {
        try {
            JSONObject details = new JSONObject();
            
            // 개인정보 보호를 위해 해시화
            if (item.mTarget != null) {
                details.put("destination_hash", item.mTarget.hashCode());
            }
            if (item.mSource != null) {
                details.put("origin_hash", item.mSource.hashCode());
            }
            if (item.mQuality != null) {
                details.put("distance", item.mQuality);
            }
            
            details.put("mode", DataStore.nMode);
            details.put("quality_preset", DataStore.sQualityPreset);
            
            return details;
            
        } catch (Exception e) {
            return new JSONObject();
        }
    }
    
    /**
     * 휴대폰 번호 추출
     */
    private static String getPhoneNumber(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                String phoneNumber = telephonyManager.getLine1Number();
                if (phoneNumber != null && phoneNumber.startsWith("+82")) {
                    phoneNumber = phoneNumber.replace("+82", "0");
                    if (phoneNumber.length() == 11) {
                        return phoneNumber.substring(0, 3) + "-" + 
                               phoneNumber.substring(3, 7) + "-" + 
                               phoneNumber.substring(7);
                    }
                }
                return phoneNumber;
            }
        } catch (Exception e) {
            // 권한 없거나 실패 시
        }
        return null;
    }
    
    /**
     * HTTP POST 전송
     */
    private static void sendStats(String jsonData) throws Exception {
        URL url = new URL(STATS_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonData.getBytes("UTF-8"));
        }
        
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("서버 응답 오류: " + responseCode);
        }
    }
}