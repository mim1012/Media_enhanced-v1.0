package com.media.player.service.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 처리된 콜 추적 시스템
 * 중복 처리 방지를 위한 메모리 기반 캐시
 */
public class ProcessedCallTracker {
    
    // 처리된 콜 캐시 (콜 해시 -> 처리 시간)
    private static final ConcurrentHashMap<String, Long> processedCalls = new ConcurrentHashMap<>();
    
    // 캐시 유효 시간 (5분)
    private static final long CACHE_DURATION = TimeUnit.MINUTES.toMillis(5);
    
    /**
     * 콜이 이미 처리되었는지 확인
     */
    public static boolean isAlreadyProcessed(MediaItem item) {
        String callHash = generateCallHash(item);
        Long processedTime = processedCalls.get(callHash);
        
        if (processedTime != null) {
            // 5분 이내에 처리된 콜인지 확인
            if (System.currentTimeMillis() - processedTime < CACHE_DURATION) {
                return true;
            } else {
                // 오래된 엔트리 제거
                processedCalls.remove(callHash);
            }
        }
        return false;
    }
    
    /**
     * 처리된 콜로 마킹
     */
    public static void markAsProcessed(MediaItem item) {
        String callHash = generateCallHash(item);
        processedCalls.put(callHash, System.currentTimeMillis());
        
        // 오래된 엔트리 정리 (100개 이상일 때)
        if (processedCalls.size() > 100) {
            cleanupOldEntries();
        }
    }
    
    /**
     * 콜 고유 해시 생성
     */
    private static String generateCallHash(MediaItem item) {
        StringBuilder sb = new StringBuilder();
        
        if (item.mTarget != null) {
            sb.append(item.mTarget);
        }
        if (item.mSource != null) {
            sb.append("|").append(item.mSource);
        }
        if (item.mQuality != null) {
            sb.append("|").append(item.mQuality);
        }
        
        // 타임스탬프도 고려 (1분 단위로 그룹화)
        long timeGroup = System.currentTimeMillis() / 60000;
        sb.append("|").append(timeGroup);
        
        return sb.toString();
    }
    
    /**
     * 오래된 엔트리 정리
     */
    private static void cleanupOldEntries() {
        long currentTime = System.currentTimeMillis();
        processedCalls.entrySet().removeIf(entry -> 
            currentTime - entry.getValue() > CACHE_DURATION
        );
    }
    
    /**
     * 통계 정보
     */
    public static int getProcessedCount() {
        return processedCalls.size();
    }
    
    /**
     * 캐시 초기화
     */
    public static void clearCache() {
        processedCalls.clear();
    }
}