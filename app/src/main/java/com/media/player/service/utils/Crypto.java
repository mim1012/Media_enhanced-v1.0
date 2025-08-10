package com.media.player.service.utils;

import android.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 암호화 유틸리티 - 서버 URL 및 민감 데이터 암호화
 */
public class Crypto {
    // 암호화 키 (난독화됨)
    private static final byte[] KEY = {
        0x4d, 0x65, 0x64, 0x69, 0x61, 0x50, 0x6c, 0x61,
        0x79, 0x65, 0x72, 0x4b, 0x65, 0x79, 0x31, 0x36
    };
    
    // IV (초기화 벡터)
    private static final byte[] IV = {
        0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
        0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f
    };
    
    // 서버 URL (암호화되어 저장)
    private static final String ENCRYPTED_SERVER_URL = "U2FsdGVkX19+5K3H8YW5xQ==";
    private static final String ENCRYPTED_AUTH_URL = "U2FsdGVkX18+7L4I9ZX6yR==";
    
    /**
     * 텍스트 암호화
     */
    public static String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(KEY, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV);
            
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));
            
            return Base64.encodeToString(encrypted, Base64.NO_WRAP);
        } catch (Exception e) {
            return plainText;  // 실패시 원본 반환
        }
    }
    
    /**
     * 텍스트 복호화
     */
    public static String decrypt(String encryptedText) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(KEY, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV);
            
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = Base64.decode(encryptedText, Base64.NO_WRAP);
            byte[] decrypted = cipher.doFinal(encrypted);
            
            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            return "";  // 실패시 빈 문자열
        }
    }
    
    /**
     * 서버 URL 가져오기 (복호화)
     */
    public static String getServerUrl() {
        // 실제로는 암호화된 URL을 복호화
        // return decrypt(ENCRYPTED_SERVER_URL);
        
        // 현재는 하드코딩 (실제 구현시 위 코드 사용)
        return "https://api.example.com";
    }
    
    /**
     * 인증 URL 가져오기
     */
    public static String getAuthUrl() {
        return getServerUrl() + "/auth";
    }
    
    /**
     * 디바이스 ID 난독화
     */
    public static String obfuscateDeviceId(String deviceId) {
        if (deviceId == null || deviceId.isEmpty()) {
            return "";
        }
        
        // 간단한 난독화 (XOR)
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < deviceId.length(); i++) {
            char c = deviceId.charAt(i);
            c ^= KEY[i % KEY.length];
            result.append(String.format("%02x", (int)c));
        }
        return result.toString();
    }
    
    /**
     * 패키지명 체크 (안티 디버깅)
     */
    public static boolean verifyPackage(String packageName) {
        // 올바른 패키지명인지 확인
        String expected = "com.media.player.service";
        return packageName.equals(expected);
    }
}