package com.media.player.service.utils;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 데이터 저장소
 */
public class DataStore {
    // 로그 관련
    public static int MIN_LOG_LENGTH = 300;
    public static String mLogContent = "";
    
    // 작업 모드
    public static int nPrepareMode;
    public static int nMode;
    public static boolean bWorkerRunning = false;
    
    // 스레드 풀
    public static ExecutorService tThreadPool;
    
    // 처리된 아이템 목록
    public static CopyOnWriteArrayList<String> aProcessedList = new CopyOnWriteArrayList<>();
    
    // 사용자 정보
    public static String sUserId = "";
    public static String sDeviceId = "";
    public static Date dtExpDate = null;
    
    // DB 버전 관리
    public static String sDbVersion = "1.0.0";
    public static String sNewDbVersion = "1.0.0";
    public static String sDbUrl = "";
    
    // 서비스 상태
    public static boolean bEnabled = false;
    
    // 품질 설정
    public static int nQuality = 0;
    public static String sQualityPreset = "1km";
    public static int nAdvancedQuality = 0;
    
    // 동작 설정
    public static boolean bAutoSkip = false;
    public static boolean bVolumeControl = false;
    
    // 필터 설정
    public static String sExclusionList = "";
    public static ArrayList<String> aExclusionList = new ArrayList<>();
    public static String sPlaylist = "";
    public static ArrayList<String> aPlaylistItems = new ArrayList<>();
    
    // 필터 키워드
    public static ArrayList<String> aFilterList = new ArrayList<>();
    
    // 프리셋 관련
    public static ArrayList<Preset> aPresetList = new ArrayList<>();
    public static int nCurrentPresetIndex = -1;
    
    // 재생 모드
    public static boolean bFullMode = false;  // true: 전체, false: 선택
    
    // 기타
    public static int bThreadNumber = 0;
    public static String sAllPlaylistCodes = "";
    public static String sAllExclusionCodes = "";
    
    // 액션 상수
    public static String ACTION_PLAY_PAUSE = "action_play_pause";
    public static String ACTION_GO_HOME = "action_go_home";
    
    // 품질 프리셋 목록
    public static final String[] QUALITY_PRESETS = {
        "0.7km", "1km", "1.5km", "2km", "2.5km", "3km", "3.5km", "4km", "4.5km", "5km",
        "5.5km", "6km", "6.5km", "7km", "7.5km", "8km", "8.5km", "9km", "9.5km", "10km",
        "10.5km", "11km", "11.5km", "12km", "12.5km", "13km", "13.5km", "14km", "14.5km", "15km",
        "최고 품질"
    };

    public static void loadConfig(Context context) {
        // 멀티스레딩 최적화 - 30개 고정 스레드 풀 (원본과 동일)
        tThreadPool = Executors.newFixedThreadPool(30);
        SharedPreferences sharedPreferences = context.getSharedPreferences("media_pref", 0);
        
        sDbVersion = sharedPreferences.getString("DB_VERSION", "1.0.0");
        nQuality = sharedPreferences.getInt("quality", 0);
        sQualityPreset = sharedPreferences.getString("qualityPreset", "1km");
        nAdvancedQuality = sharedPreferences.getInt("advQuality", 0);
        bAutoSkip = sharedPreferences.getBoolean("autoSkip", false);
        bVolumeControl = sharedPreferences.getBoolean("volumeCtrl", false);
        bFullMode = sharedPreferences.getBoolean("fullMode", false);
        
        sExclusionList = FileHelper.loadExclusionFile();
        sPlaylist = FileHelper.loadPlaylistFile();
        aExclusionList = parseStr2Array(sExclusionList);
        aPlaylistItems = parseStr2Array(sPlaylist);
        
        String filters = sharedPreferences.getString("filters", "");
        aFilterList = parseStr2Array(filters);
        
        loadPresets(context);
        updateQualityFromPreset();
    }

    public static void saveConfig(Context context) {
        SharedPreferences.Editor edit = context.getSharedPreferences("media_pref", 0).edit();
        edit.putString("DB_VERSION", sDbVersion);
        edit.putInt("quality", nQuality);
        edit.putString("qualityPreset", sQualityPreset);
        edit.putInt("advQuality", nAdvancedQuality);
        edit.putBoolean("autoSkip", bAutoSkip);
        edit.putBoolean("volumeCtrl", bVolumeControl);
        edit.putBoolean("fullMode", bFullMode);
        edit.putString("filters", parseArray2Str(aFilterList));
        edit.apply();
        
        FileHelper.saveExclusionFile(sExclusionList);
        FileHelper.savePlaylistFile(sPlaylist);
        savePresets(context);
    }
    
    private static void updateQualityFromPreset() {
        if (sQualityPreset.equals("최고 품질")) {
            nQuality = 0;
        } else {
            String qualStr = sQualityPreset.replace("km", "").trim();
            try {
                float quality = Float.parseFloat(qualStr);
                nQuality = (int)(quality * 1000);
            } catch (NumberFormatException e) {
                nQuality = 1000;
            }
        }
    }
    
    private static void loadPresets(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("presets", 0);
        aPresetList.clear();
        for (int i = 0; i < 5; i++) {
            String presetData = prefs.getString("preset_" + i, null);
            if (presetData != null) {
                Preset preset = Preset.fromString(presetData);
                if (preset != null) {
                    aPresetList.add(preset);
                }
            }
        }
    }
    
    private static void savePresets(Context context) {
        SharedPreferences.Editor edit = context.getSharedPreferences("presets", 0).edit();
        for (int i = 0; i < aPresetList.size() && i < 5; i++) {
            edit.putString("preset_" + i, aPresetList.get(i).toString());
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

    public static void addProcessedText(String str) {
        if (aProcessedList.size() >= 100) {
            aProcessedList.remove(0);
        }
        aProcessedList.add(str);
    }
}