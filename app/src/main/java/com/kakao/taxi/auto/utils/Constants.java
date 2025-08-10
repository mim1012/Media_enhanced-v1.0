package com.kakao.taxi.auto.utils;

/**
 * 상수 정의 클래스
 */
public class Constants {
    // 패키지명
    public static final String TAXI_DRIVER_APP_PACKAGE = "com.kakao.taxi.driver";
    
    // 작업 모드
    public static final int MODE_NONE = 0;
    public static final int MODE_DEST = 256;        // 도착지 모드
    public static final int MODE_EXCEPT = 512;      // 제외지 모드
    public static final int MODE_ALL = 768;         // 전체콜 모드
    public static final int MODE_LONGDISTANCE = 1024;  // 장거리 모드
    
    // View ID 상수 (KakaoTaxi 앱 UI 요소)
    public static final String VIEW_ID_ARROW = "v_arrow";
    public static final String VIEW_ID_CALL_LIST = "lv_call_list";
    public static final String VIEW_ID_DESTINATION = "tv_destination";
    public static final String VIEW_ID_ORIGIN = "tv_origin";
    public static final String VIEW_ID_DISTANCE = "tv_origin_label_distance";
    public static final String VIEW_ID_DELETE_COMPLETED = "tv_btn_delete_completed";
    public static final String VIEW_ID_ACCEPT_BUTTON = "btn_accept";
    public static final String VIEW_ID_DENY_BUTTON = "btn_deny";
    
    // 텍스트 상수
    public static final String TEXT_COMPLETED_CALL = "배차가 완료된 콜입니다.";
    
    // 설정 키
    public static final String PREF_NAME = "pref";
    public static final String PREF_TEMPLATES = "templates";
    
    // 네트워크
    public static final String SERVER_URL = "https://api.example.com";
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 10000;
    
    // 로그
    public static final String LOG_TAG = "KakaoAuto";
    public static final boolean DEBUG_MODE = true;
    
    // 파일 경로
    public static final String DB_FILE_NAME = "address.db";
    public static final String EXCLUSION_PLACES_FILE = "exclusion_places.txt";
    public static final String ACCEPT_PLACES_FILE = "accept_places.txt";
    
    // 알림 채널
    public static final String NOTIFICATION_CHANNEL_ID = "kakao_auto_channel";
    public static final String NOTIFICATION_CHANNEL_NAME = "KakaoAuto Service";
    public static final int NOTIFICATION_ID = 1001;
    
    // 거리 계산
    public static final int DEFAULT_DISTANCE_LIMIT = 1000;  // 기본 1km (미터 단위)
    public static final int MAX_DISTANCE_LIMIT = 15000;     // 최대 15km
    
    // UI 갱신 간격
    public static final int UI_UPDATE_INTERVAL = 1000;  // 1초
    public static final int LOCATION_UPDATE_INTERVAL = 5000;  // 5초
    
    // 스레드 풀 크기
    public static final int THREAD_POOL_SIZE = 30;
}