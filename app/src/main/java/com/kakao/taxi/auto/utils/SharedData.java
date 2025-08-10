package com.kakao.taxi.auto.utils;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 전역 데이터 및 설정 관리 클래스
 */
public class SharedData {
    // 로그 관련
    public static int MIN_UPLOAD_LOG_LENGTH = 300;
    public static String mLogContent = "";
    
    // 작업 모드
    public static int nPrepareMode;
    public static int nWorkMode;
    public static boolean bManagerThreadRunning = false;
    
    // 스레드 풀
    public static ExecutorService tWorkThreadPool;
    
    // 처리된 콜 목록
    public static CopyOnWriteArrayList<String> aProcessedCallList = new CopyOnWriteArrayList<>();
    
    // 사용자 정보
    public static String sPhoneNumber = "";
    public static String sAndroidId = "";
    public static Date dtExpDate = null;
    
    // DB 버전 관리
    public static String sDbFileVersionCode = "1.0.0";
    public static String sNewDbFileVersionCode = "1.0.0";
    public static String sDbFileDownloadUrl = "";
    
    // 자동화 설정
    public static boolean bAuto = false;
    
    // 거리 설정 (개선된 버전)
    public static int nCallDistance = 0;  // 미터 단위
    public static String sDistancePreset = "1km"; // 거리 프리셋 (0.7km, 1km, 1.5km... 15km, 무제한)
    public static int nLongDistance = 0;  // 장거리 모드 킬로미터
    
    // 동작 설정
    public static boolean bAutoDeny = false;
    public static boolean bEnableVolume = false;
    
    // 필터 설정
    public static String sPreferredExclusionPlaces = "";
    public static ArrayList<String> aPreferredExclusionPlaceList = new ArrayList<>();
    public static String sPreferredAcceptPlaces = "";
    public static ArrayList<String> aPreferredAcceptPlaceList = new ArrayList<>();
    
    // 키워드 필터 (새로 추가)
    public static ArrayList<String> aKeywordFilterList = new ArrayList<>();
    
    // 템플릿 관련 (새로 추가)
    public static ArrayList<Template> aTemplateList = new ArrayList<>();
    public static int nCurrentTemplateIndex = -1;
    
    // 콜 모드 (새로 추가)
    public static boolean bFullCallMode = false;  // true: 전체콜, false: 부분콜
    
    // 기타
    public static int bThreadNumber = 0;
    public static String sAllDestDongCodes = "";
    public static String sAllExceptDongCodes = "";
    
    // 액션 상수
    public static String ACTION_PLAY_PAUSE = "action_play_pause";
    public static String ACTION_GO_HOME = "action_go_home";
    
    // 거리 프리셋 목록
    public static final String[] DISTANCE_PRESETS = {
        "0.7km", "1km", "1.5km", "2km", "2.5km", "3km", "3.5km", "4km", "4.5km", "5km",
        "5.5km", "6km", "6.5km", "7km", "7.5km", "8km", "8.5km", "9km", "9.5km", "10km",
        "10.5km", "11km", "11.5km", "12km", "12.5km", "13km", "13.5km", "14km", "14.5km", "15km",
        "무제한"
    };

    public static void loadConfig(Context context) {
        tWorkThreadPool = Executors.newFixedThreadPool(30);
        SharedPreferences sharedPreferences = context.getSharedPreferences("pref", 0);
        
        sDbFileVersionCode = sharedPreferences.getString("DB_VERSION", "1.0.0");
        nCallDistance = sharedPreferences.getInt("callkm", 0);
        sDistancePreset = sharedPreferences.getString("distancePreset", "1km");
        nLongDistance = sharedPreferences.getInt("longkm", 0);
        bAutoDeny = sharedPreferences.getBoolean("autodeny", false);
        bEnableVolume = sharedPreferences.getBoolean("enableVolume", false);
        bFullCallMode = sharedPreferences.getBoolean("fullCallMode", false);
        
        sPreferredExclusionPlaces = FileUtils.loadPreferredExclusionPlacesFileToText();
        sPreferredAcceptPlaces = FileUtils.loadPreferredAcceptPlacesFileToText();
        aPreferredExclusionPlaceList = parseStr2Array(sPreferredExclusionPlaces);
        aPreferredAcceptPlaceList = parseStr2Array(sPreferredAcceptPlaces);
        
        // 키워드 필터 로드
        String keywords = sharedPreferences.getString("keywordFilter", "");
        aKeywordFilterList = parseStr2Array(keywords);
        
        // 템플릿 로드
        loadTemplates(context);
        
        // 거리 프리셋에서 실제 거리 값 설정
        updateDistanceFromPreset();
    }

    public static void saveConfig(Context context) {
        SharedPreferences.Editor edit = context.getSharedPreferences("pref", 0).edit();
        edit.putString("DB_VERSION", sDbFileVersionCode);
        edit.putInt("callkm", nCallDistance);
        edit.putString("distancePreset", sDistancePreset);
        edit.putInt("longkm", nLongDistance);
        edit.putBoolean("autodeny", bAutoDeny);
        edit.putBoolean("enableVolume", bEnableVolume);
        edit.putBoolean("fullCallMode", bFullCallMode);
        edit.putString("keywordFilter", parseArray2Str(aKeywordFilterList));
        edit.apply();
        
        FileUtils.savePreferredExclusionPlacesToFile(sPreferredExclusionPlaces);
        FileUtils.savePreferredAcceptPlacesToFile(sPreferredAcceptPlaces);
        
        // 템플릿 저장
        saveTemplates(context);
    }
    
    private static void updateDistanceFromPreset() {
        if (sDistancePreset.equals("무제한")) {
            nCallDistance = 0;  // 0은 무제한을 의미
        } else {
            String distanceStr = sDistancePreset.replace("km", "").trim();
            try {
                float distance = Float.parseFloat(distanceStr);
                nCallDistance = (int)(distance * 1000);  // km를 m로 변환
            } catch (NumberFormatException e) {
                nCallDistance = 1000;  // 기본값 1km
            }
        }
    }
    
    private static void loadTemplates(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("templates", 0);
        aTemplateList.clear();
        for (int i = 0; i < 5; i++) {
            String templateData = prefs.getString("template_" + i, null);
            if (templateData != null) {
                Template template = Template.fromString(templateData);
                if (template != null) {
                    aTemplateList.add(template);
                }
            }
        }
    }
    
    private static void saveTemplates(Context context) {
        SharedPreferences.Editor edit = context.getSharedPreferences("templates", 0).edit();
        for (int i = 0; i < aTemplateList.size() && i < 5; i++) {
            edit.putString("template_" + i, aTemplateList.get(i).toString());
        }
        edit.apply();
    }

    public static ArrayList<String> parseStr2Array(String str) {
        String[] split;
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            for (String str2 : str.replaceAll(" ", "").split("[\n,]")) {
                if (!str2.equals("")) {
                    arrayList.add(str2);
                }
            }
        } catch (Exception unused) {
        }
        return arrayList;
    }

    public static String parseArray2Str(ArrayList<String> arrayList) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arrayList.size(); i++) {
            try {
                sb.append(arrayList.get(i)).append(", ");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static void addProcessedCallText(String str) {
        if (aProcessedCallList.size() >= 100) {
            aProcessedCallList.remove(0);
        }
        aProcessedCallList.add(str);
    }
}