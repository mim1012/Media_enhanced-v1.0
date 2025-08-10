package com.media.player.service.utils;

import android.util.Log;

/**
 * 테스트 모드 및 디버깅 시스템
 * 서버 없이 로컬 환경에서 테스트하기 위한 유틸리티
 */
public class TestMode {
    private static final String TAG = "MediaPlayerTest";
    
    // 테스트 모드 설정
    public static final boolean ENABLED = true;  // 테스트 모드 활성화
    public static final boolean SKIP_DELAYS = true;  // 지연 시간 스킵
    public static final boolean VERBOSE_LOGGING = true;  // 상세 로그
    public static final boolean SKIP_SERVER_CHECK = true;  // 서버 인증 스킵
    public static final boolean USE_DUMMY_DATA = true;  // 더미 데이터 사용
    
    /**
     * 테스트 콜 시뮬레이션
     */
    public static void simulateCall(String origin, String destination, String distance) {
        if (!ENABLED) return;
        
        MediaItem testCall = new MediaItem();
        testCall.mSource = origin;
        testCall.mTarget = destination;
        testCall.mQuality = distance;
        
        // 로그 출력
        logCallInfo(testCall);
        
        // 필터 테스트
        testFilterResult(testCall);
    }
    
    /**
     * 콜 정보 로깅
     */
    public static void logCallInfo(MediaItem item) {
        if (!VERBOSE_LOGGING) return;
        
        Log.d(TAG, "=== 콜 정보 ===");
        Log.d(TAG, "출발지: " + item.mSource);
        Log.d(TAG, "도착지: " + item.mTarget);
        Log.d(TAG, "거리: " + item.mQuality);
        Log.d(TAG, "===============");
    }
    
    /**
     * 필터 결과 테스트
     */
    public static void testFilterResult(MediaItem item) {
        if (!ENABLED) return;
        
        // 거리 체크
        int distance = Helper.getCallDistance(item);
        Log.d(TAG, "거리 파싱 결과: " + distance + "m");
        
        // 주소 분석
        ItemInfo destInfo = Helper.analyzeDestination(item);
        if (destInfo != null) {
            Log.d(TAG, "주소 분석 성공:");
            Log.d(TAG, "  시군구: " + destInfo.sSigunguName);
            Log.d(TAG, "  동/로: " + destInfo.sDongName);
            Log.d(TAG, "  도로명주소: " + destInfo.bRoad);
        } else {
            Log.d(TAG, "주소 분석 실패");
        }
        
        // 모드별 처리 결과
        testWorkModes(item);
    }
    
    /**
     * 작업 모드별 테스트
     */
    private static void testWorkModes(MediaItem item) {
        Log.d(TAG, "=== 작업 모드 테스트 ===");
        
        // 전체 모드
        Log.d(TAG, "전체 모드(768): 무조건 수락");
        
        // 도착지 모드
        if (DataStore.nMode == 256) {
            Log.d(TAG, "도착지 모드(256): 선호지 체크");
            boolean matched = false;
            for (String preferred : DataStore.aPlaylistItems) {
                if (item.mTarget.contains(preferred)) {
                    matched = true;
                    Log.d(TAG, "  매칭됨: " + preferred);
                    break;
                }
            }
            if (!matched) {
                Log.d(TAG, "  매칭 안됨: 거절");
            }
        }
        
        // 제외지 모드
        if (DataStore.nMode == 512) {
            Log.d(TAG, "제외지 모드(512): 제외지 체크");
            boolean excluded = false;
            for (String exclusion : DataStore.aExclusionList) {
                if (item.mTarget.contains(exclusion)) {
                    excluded = true;
                    Log.d(TAG, "  제외지 발견: " + exclusion + " -> 거절");
                    break;
                }
            }
            if (!excluded) {
                Log.d(TAG, "  제외지 아님: 수락");
            }
        }
        
        // 장거리 모드
        if (DataStore.nMode == 1024) {
            Log.d(TAG, "장거리 모드(1024): 거리 체크");
            int distance = Helper.getCallDistance(item);
            if (distance >= DataStore.nAdvancedQuality) {
                Log.d(TAG, "  장거리 조건 만족: 수락");
            } else {
                Log.d(TAG, "  장거리 조건 불만족: 거절");
            }
        }
    }
    
    /**
     * UI 감지 테스트
     */
    public static void testUIDetection(String screenType, boolean hasButtons) {
        if (!VERBOSE_LOGGING) return;
        
        Log.d(TAG, "=== UI 감지 ===");
        Log.d(TAG, "화면 타입: " + screenType);
        Log.d(TAG, "버튼 감지: " + hasButtons);
    }
    
    /**
     * 오류 로깅
     */
    public static void logError(String operation, Exception e) {
        Log.e(TAG, "오류 발생 - " + operation, e);
    }
    
    /**
     * 테스트 데이터 생성
     */
    public static class TestData {
        // 테스트용 주소 목록
        public static final String[] TEST_ORIGINS = {
            "서울 강남구 역삼동",
            "서울 서초구 서초동",
            "서울 송파구 잠실동",
            "서울 마포구 합정동",
            "인천 연수구 송도동"
        };
        
        public static final String[] TEST_DESTINATIONS = {
            "서울 강남구 삼성동",
            "서울 종로구 광화문",
            "서울 용산구 이태원동",
            "경기 성남시 분당구 정자동",
            "인천 부평구 부평동"
        };
        
        public static final String[] TEST_DISTANCES = {
            "0.7km",
            "1.2km",
            "2.5km",
            "5.3km",
            "850m",
            "10.5km"
        };
        
        /**
         * 랜덤 테스트 콜 생성
         */
        public static MediaItem generateRandomCall() {
            MediaItem item = new MediaItem();
            item.mSource = TEST_ORIGINS[(int)(Math.random() * TEST_ORIGINS.length)];
            item.mTarget = TEST_DESTINATIONS[(int)(Math.random() * TEST_DESTINATIONS.length)];
            item.mQuality = TEST_DISTANCES[(int)(Math.random() * TEST_DISTANCES.length)];
            return item;
        }
    }
    
    /**
     * 볼륨 컨트롤 테스트
     */
    public static void testVolumeControl(int previousVolume, int currentVolume) {
        if (!VERBOSE_LOGGING) return;
        
        Log.d(TAG, "=== 볼륨 컨트롤 테스트 ===");
        Log.d(TAG, "이전 볼륨: " + previousVolume);
        Log.d(TAG, "현재 볼륨: " + currentVolume);
        
        if (currentVolume > previousVolume) {
            Log.d(TAG, "볼륨 UP -> 자동화 활성화");
        } else if (currentVolume < previousVolume) {
            Log.d(TAG, "볼륨 DOWN -> 자동화 비활성화");
        } else {
            Log.d(TAG, "볼륨 변화 없음");
        }
    }
    
    /**
     * 메모리 사용량 체크
     */
    public static void checkMemoryUsage() {
        if (!VERBOSE_LOGGING) return;
        
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
        long maxMemory = runtime.maxMemory() / 1048576L;
        
        Log.d(TAG, "=== 메모리 사용량 ===");
        Log.d(TAG, "사용중: " + usedMemory + "MB");
        Log.d(TAG, "최대: " + maxMemory + "MB");
        Log.d(TAG, "사용률: " + (usedMemory * 100 / maxMemory) + "%");
    }
    
    /**
     * 스레드 풀 상태 체크
     */
    public static void checkThreadPoolStatus() {
        if (!VERBOSE_LOGGING) return;
        
        Log.d(TAG, "=== 스레드 풀 상태 ===");
        Log.d(TAG, "고정 스레드 수: 30");
        Log.d(TAG, "활성 스레드: " + Thread.activeCount());
    }
}