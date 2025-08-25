package com.media.player.service.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * 인증 관리자 - 휴대폰 번호 기반 1회 인증
 */
public class AuthManager {
    private static final String AUTH_URL = "http://localhost:3000/api/auth/verify";
    private static final String PREFS_NAME = "media_auth";
    private static final String KEY_AUTHENTICATED = "authenticated";
    private static final String KEY_AUTH_EXPIRY = "auth_expiry";
    private static final String KEY_USER_TYPE = "user_type";
    
    /**
     * 인증 상태 확인 및 실행
     */
    public static void checkAuthentication(Context context, AuthCallback callback) {
        // 로컬 인증 상태 확인
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isAuthenticated = prefs.getBoolean(KEY_AUTHENTICATED, false);
        long authExpiry = prefs.getLong(KEY_AUTH_EXPIRY, 0);
        
        if (isAuthenticated && System.currentTimeMillis() < authExpiry) {
            // 로컬 인증이 유효함
            String userType = prefs.getString(KEY_USER_TYPE, "trial");
            callback.onSuccess(userType, "로컬 인증 유효");
            return;
        }
        
        // 서버 인증 필요
        performServerAuth(context, callback);
    }
    
    /**
     * 서버 인증 수행
     */
    private static void performServerAuth(Context context, AuthCallback callback) {
        new Thread(() -> {
            try {
                // 휴대폰 번호 추출
                String phoneNumber = getPhoneNumber(context);
                if (phoneNumber == null) {
                    callback.onFailure("휴대폰 번호를 가져올 수 없습니다.\n\nSIM 카드를 확인해주세요.");
                    return;
                }
                
                // 기기 ID 생성
                String deviceId = getDeviceId(context);
                
                // 서버 요청
                JSONObject request = new JSONObject();
                request.put("phone_number", phoneNumber);
                request.put("device_id", deviceId);
                request.put("app_version", "2.0.0");
                
                String response = sendHttpPost(AUTH_URL, request.toString());
                JSONObject result = new JSONObject(response);
                
                boolean authorized = result.getBoolean("authorized");
                String message = result.getString("message");
                
                if (authorized) {
                    // 인증 성공 - 로컬에 저장
                    String userType = result.getString("type");
                    long expiryTime = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000); // 30일
                    
                    SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
                    editor.putBoolean(KEY_AUTHENTICATED, true);
                    editor.putLong(KEY_AUTH_EXPIRY, expiryTime);
                    editor.putString(KEY_USER_TYPE, userType);
                    editor.apply();
                    
                    callback.onSuccess(userType, message);
                    
                } else {
                    // 인증 실패
                    callback.onFailure(message);
                }
                
            } catch (Exception e) {
                callback.onFailure("서버 연결에 실패했습니다.\n\n네트워크 상태를 확인해주세요.\n\n📞 문의: 010-0000-0000");
            }
        }).start();
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
                    // +821012345678 → 010-1234-5678 변환
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
            android.util.Log.e("AuthManager", "휴대폰 번호 추출 실패", e);
        }
        return null;
    }
    
    /**
     * 기기 ID 생성
     */
    private static String getDeviceId(Context context) {
        try {
            String androidId = android.provider.Settings.Secure.getString(
                context.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID
            );
            return android.os.Build.MODEL + "_" + androidId.substring(0, Math.min(8, androidId.length()));
        } catch (Exception e) {
            return "unknown_device";
        }
    }
    
    /**
     * HTTP POST 요청
     */
    private static String sendHttpPost(String urlString, String jsonData) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        
        // 요청 데이터 전송
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonData.getBytes("UTF-8"));
        }
        
        // 응답 읽기
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }
        
        return response.toString();
    }
    
    /**
     * 인증 콜백 인터페이스
     */
    public interface AuthCallback {
        void onSuccess(String userType, String message);
        void onFailure(String message);
    }
}