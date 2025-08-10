package com.media.player.service.utils;

/**
 * 설정 상수 클래스
 */
public class Config {
    // 타겟 패키지
    public static final String TARGET_PACKAGE = "com.kakao.taxi.driver";
    
    // 작업 모드 (원본과 동일)
    public static final int MODE_NONE = 0;
    public static final int MODE_DEST = 256;         // 도착지 모드
    public static final int MODE_EXCEPT = 512;       // 제외지 모드
    public static final int MODE_ALL = 768;          // 전체 모드
    public static final int MODE_LONGDISTANCE = 1024; // 장거리 모드
    
    // View ID 상수
    public static final String VIEW_ID_ARROW = "v_arrow";
    public static final String VIEW_ID_LIST = "lv_call_list";
    public static final String VIEW_ID_TARGET = "tv_destination";
    public static final String VIEW_ID_SOURCE = "tv_origin";
    public static final String VIEW_ID_QUALITY = "tv_origin_label_distance";
    public static final String VIEW_ID_DELETE = "tv_btn_delete_completed";
    public static final String VIEW_ID_PLAY = "btn_accept";
    public static final String VIEW_ID_SKIP = "btn_deny";
    
    // 텍스트 상수
    public static final String TEXT_COMPLETED = "배차가 완료된 콜입니다.";
    
    // 설정 키
    public static final String PREF_NAME = "media_pref";
    public static final String PREF_PRESETS = "presets";
    
    // 네트워크 설정 (주석 처리 - 로컬 전용)
    /*
    public static final String SERVER_URL = "https://api.example.com";
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 10000;
    */
    
    // 로그
    public static final String LOG_TAG = "MediaPlayer";
    public static final boolean DEBUG_MODE = true;  // 디버그 모드 활성화
    
    // 파일 경로
    public static final String DB_FILE_NAME = "media.db";
    public static final String EXCLUSION_FILE = "exclusion.txt";
    public static final String PLAYLIST_FILE = "playlist.txt";
    
    // 알림 채널
    public static final String NOTIFICATION_CHANNEL_ID = "media_channel";
    public static final String NOTIFICATION_CHANNEL_NAME = "Media Service";
    public static final int NOTIFICATION_ID = 1001;
    
    // 품질 설정
    public static final int DEFAULT_QUALITY = 1000;  // 기본 품질
    public static final int MAX_QUALITY = 15000;     // 최대 품질
    
    // UI 갱신 간격
    public static final int UI_UPDATE_INTERVAL = 1000;  // 1초
    public static final int LOCATION_UPDATE_INTERVAL = 5000;  // 5초
    
    // 스레드 풀 크기
    public static final int THREAD_POOL_SIZE = 30;
}