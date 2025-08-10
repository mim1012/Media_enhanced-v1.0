package com.media.player.service.utils;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 통계 및 로그 기록 시스템
 */
public class Statistics {
    
    // 실시간 카운터
    private static final AtomicInteger acceptCount = new AtomicInteger(0);
    private static final AtomicInteger rejectCount = new AtomicInteger(0);
    private static final AtomicInteger totalCallCount = new AtomicInteger(0);
    
    // 일일 통계
    private static long lastResetDate = 0;
    
    /**
     * 수락 카운트 증가
     */
    public static void incrementAccept() {
        acceptCount.incrementAndGet();
        totalCallCount.incrementAndGet();
    }
    
    /**
     * 거절 카운트 증가
     */
    public static void incrementReject() {
        rejectCount.incrementAndGet();
        totalCallCount.incrementAndGet();
    }
    
    /**
     * 현재 통계 가져오기
     */
    public static String getCurrentStats() {
        return String.format(
            "오늘 통계: 총 %d건 | 수락 %d건 | 거절 %d건 | 수락률 %.1f%%",
            totalCallCount.get(),
            acceptCount.get(),
            rejectCount.get(),
            getAcceptRate()
        );
    }
    
    /**
     * 수락률 계산
     */
    public static float getAcceptRate() {
        int total = totalCallCount.get();
        if (total == 0) return 0;
        return (acceptCount.get() * 100.0f) / total;
    }
    
    /**
     * 통계 저장
     */
    public static void saveStats(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("stats", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        editor.putInt("accept_count", acceptCount.get());
        editor.putInt("reject_count", rejectCount.get());
        editor.putInt("total_count", totalCallCount.get());
        editor.putLong("last_reset", System.currentTimeMillis());
        
        editor.apply();
    }
    
    /**
     * 통계 로드
     */
    public static void loadStats(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("stats", Context.MODE_PRIVATE);
        
        acceptCount.set(prefs.getInt("accept_count", 0));
        rejectCount.set(prefs.getInt("reject_count", 0));
        totalCallCount.set(prefs.getInt("total_count", 0));
        lastResetDate = prefs.getLong("last_reset", System.currentTimeMillis());
        
        // 날짜가 바뀌면 리셋
        if (isDifferentDay(lastResetDate)) {
            resetDailyStats(context);
        }
    }
    
    /**
     * 일일 통계 리셋
     */
    public static void resetDailyStats(Context context) {
        acceptCount.set(0);
        rejectCount.set(0);
        totalCallCount.set(0);
        lastResetDate = System.currentTimeMillis();
        saveStats(context);
    }
    
    /**
     * 날짜 비교
     */
    private static boolean isDifferentDay(long timestamp) {
        long currentDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24);
        long lastDay = timestamp / (1000 * 60 * 60 * 24);
        return currentDay != lastDay;
    }
    
    // Getter methods
    public static int getAcceptCount() { return acceptCount.get(); }
    public static int getRejectCount() { return rejectCount.get(); }
    public static int getTotalCount() { return totalCallCount.get(); }
}