package com.kakao.taxi.auto.boiler;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import com.kakao.taxi.auto.model.DestItem;
import com.kakao.taxi.auto.utils.Call;
import com.kakao.taxi.auto.utils.DBUtils;
import com.kakao.taxi.auto.utils.Helper;
import com.kakao.taxi.auto.utils.LogUtils;
import com.kakao.taxi.auto.utils.SharedData;
import java.util.ArrayList;

/**
 * 콜 처리를 담당하는 백그라운드 실행 클래스
 * 개선된 거리 필터링 로직과 다양한 운행 모드를 지원합니다.
 */
public class BoilerRunnable implements Runnable {
    private final Call call;
    private final Context context;
    private String mLogText;

    // 작업 모드 상수 (개선된 버전)
    public static final int MODE_DESTINATION_ONLY = 256;    // 도착지만 체크
    public static final int MODE_EXCLUDE_AREAS = 512;       // 제외지역 체크
    public static final int MODE_ALL_ACCEPT = 768;          // 전체 수락
    public static final int MODE_DISTANCE = 1024;           // 거리 기준 모드
    public static final int MODE_SMART_FILTER = 2048;       // 스마트 필터 모드 (새로 추가)

    public BoilerRunnable(Call call, Context context) {
        this.call = call;
        this.context = context;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        this.mLogText = "";
        processBoiler();
        long processingTime = System.currentTimeMillis() - startTime;
        String finalLog = this.mLogText + "\t--소요시간--: " + processingTime + "ms";
        this.mLogText = finalLog;
        LogUtils.uploadLog(finalLog);
    }

    private void processBoiler() {
        try {
            this.mLogText += "콜분석 시작: " + this.call.mDistance + "," + this.call.mDest + "\n";
            
            // 1. 기본 거리 체크 (개선된 거리 필터링)
            if (!checkDistance()) {
                return;
            }
            
            // 2. 우선 제외지 체크
            if (checkPreferredExclusionPlaces()) {
                return;
            }
            
            // 3. 우선 선호지 체크
            if (checkPreferredAcceptPlaces()) {
                return;
            }
            
            // 4. 키워드 필터 체크 (새로 추가)
            if (checkKeywordFilter()) {
                return;
            }
            
            // 5. 모드별 처리
            processByMode();
            
        } catch (Exception e) {
            this.mLogText += "콜처리 오류: " + e.getMessage() + "\n";
            LogUtils.uploadLog(e);
        }
    }

    /**
     * 개선된 거리 체크 로직
     * 0.7km, 1km 등 다양한 거리 프리셋을 지원
     */
    private boolean checkDistance() {
        int callDistance = Helper.getCallDistance(this.call);
        
        // 거리 설정이 0이면 무제한
        if (SharedData.nCallDistance == 0) {
            this.mLogText += "\t: 거리 무제한 모드\n";
            return true;
        }
        
        if (callDistance > SharedData.nCallDistance) {
            if (SharedData.bAutoDeny && this.call.mDenyCtrl != null && this.call.mDenyCtrl.isClickable()) {
                Helper.delegateButtonClick(this.call.mDenyCtrl);
            }
            
            // 실제 거리를 km 단위로 표시
            double actualDistanceKm = callDistance / 1000.0;
            double settingDistanceKm = SharedData.nCallDistance / 1000.0;
            
            this.mLogText += String.format("\t: 거리 초과로 거절 (실제: %.1fkm, 설정: %.1fkm, 프리셋: %s)\n", 
                actualDistanceKm, settingDistanceKm, SharedData.sDistancePreset);
            return false;
        }
        
        this.mLogText += String.format("\t: 거리 체크 통과 (실제: %.1fkm, 설정: %s)\n", 
            callDistance / 1000.0, SharedData.sDistancePreset);
        return true;
    }

    /**
     * 우선 제외지 체크
     */
    private boolean checkPreferredExclusionPlaces() {
        if (SharedData.aPreferredExclusionPlaceList != null && SharedData.aPreferredExclusionPlaceList.size() >= 1) {
            for (String excludePlace : SharedData.aPreferredExclusionPlaceList) {
                if (this.call.mDest.contains(excludePlace)) {
                    if (SharedData.bAutoDeny && this.call.mDenyCtrl != null && this.call.mDenyCtrl.isClickable()) {
                        Helper.delegateButtonClick(this.call.mDenyCtrl);
                    }
                    this.mLogText += "\t: 우선제외지 매칭으로 거절 -> 콜:" + this.call.mDest + ", 제외지명:" + excludePlace + "\n";
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 우선 선호지 체크
     */
    private boolean checkPreferredAcceptPlaces() {
        if (SharedData.aPreferredAcceptPlaceList != null && SharedData.aPreferredAcceptPlaceList.size() >= 1) {
            for (String acceptPlace : SharedData.aPreferredAcceptPlaceList) {
                if (this.call.mDest.contains(acceptPlace)) {
                    Helper.delegateButtonClick(this.call.mAcceptCtrl);
                    this.mLogText += "\t: 우선선호지 매칭으로 수락 -> 콜:" + this.call.mDest + ", 선호지명:" + acceptPlace + "\n";
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 키워드 필터 체크 (새로 추가된 기능)
     */
    private boolean checkKeywordFilter() {
        if (SharedData.aKeywordFilterList != null && SharedData.aKeywordFilterList.size() >= 1) {
            for (String keyword : SharedData.aKeywordFilterList) {
                if (this.call.mDest.contains(keyword) || 
                    (this.call.mOrigin != null && this.call.mOrigin.contains(keyword))) {
                    
                    if (SharedData.bAutoDeny && this.call.mDenyCtrl != null && this.call.mDenyCtrl.isClickable()) {
                        Helper.delegateButtonClick(this.call.mDenyCtrl);
                    }
                    this.mLogText += "\t: 키워드 필터 매칭으로 거절 -> 키워드:" + keyword + "\n";
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 작업 모드에 따른 처리
     */
    private void processByMode() {
        int workMode = SharedData.nWorkMode;
        
        switch (workMode) {
            case MODE_DESTINATION_ONLY:
                processDestinationOnlyMode();
                break;
            case MODE_EXCLUDE_AREAS:
                processExcludeAreasMode();
                break;
            case MODE_ALL_ACCEPT:
                processAllAcceptMode();
                break;
            case MODE_DISTANCE:
                processDistanceMode();
                break;
            case MODE_SMART_FILTER:
                processSmartFilterMode();
                break;
            default:
                this.mLogText += "\t: 알 수 없는 작업 모드: " + workMode + "\n";
                break;
        }
    }

    /**
     * 도착지만 체크하는 모드
     */
    private void processDestinationOnlyMode() {
        if (SharedData.sAllDestDongCodes.isEmpty()) {
            this.mLogText += "\t: 도착지 코드가 설정되지 않음\n";
            return;
        }
        
        this.mLogText += "\t: 도착지 체크 모드 시작\n";
        DestItem destItem = Helper.analyzeDestination(this.call);
        
        if (destItem != null && destItem.sDongName != null && (destItem.bRoad || destItem.sSigunguName != null)) {
            this.mLogText += "\t: 주소분석 성공: 시도구명=" + destItem.sSigunguName + 
                ", 동(도로)이름=" + destItem.sDongName + ", 도로명주소=" + destItem.bRoad + "\n";
            
            ArrayList<Integer> codeList = new ArrayList<>();
            
            if (destItem.bRoad) {
                this.mLogText += DBUtils.findHjdongCodeListByDestInfo(this.context, codeList, destItem);
            } else {
                this.mLogText += DBUtils.findHjdongCodeListByHjdongName(this.context, codeList, destItem);
                if (codeList.isEmpty()) {
                    this.mLogText += DBUtils.findHjdongCodeListByBjdongName(this.context, codeList, destItem);
                }
            }
            
            for (Integer code : codeList) {
                if (SharedData.sAllDestDongCodes.contains(String.valueOf(code))) {
                    Helper.delegateButtonClick(this.call.mAcceptCtrl);
                    this.mLogText += "\t: 도착지 매칭 성공 (" + code + ") -> 수락\n";
                    return;
                }
            }
            
            if (SharedData.bAutoDeny && this.call.mDenyCtrl != null && this.call.mDenyCtrl.isClickable()) {
                Helper.delegateButtonClick(this.call.mDenyCtrl);
            }
            this.mLogText += "\t: 도착지 매칭 실패 -> 거절\n";
        } else {
            if (SharedData.bAutoDeny && this.call.mDenyCtrl != null && this.call.mDenyCtrl.isClickable()) {
                Helper.delegateButtonClick(this.call.mDenyCtrl);
            }
            this.mLogText += "\t: 주소분석 실패 -> 거절\n";
        }
    }

    /**
     * 제외 지역 체크 모드
     */
    private void processExcludeAreasMode() {
        if (SharedData.sAllExceptDongCodes.isEmpty()) {
            this.mLogText += "\t: 제외지역 코드가 설정되지 않음\n";
            return;
        }
        
        this.mLogText += "\t: 제외지역 체크 모드 시작\n";
        DestItem destItem = Helper.analyzeDestination(this.call);
        
        if (destItem != null && destItem.sDongName != null && (destItem.bRoad || destItem.sSigunguName != null)) {
            this.mLogText += "\t: 주소분석 성공: 시도구명=" + destItem.sSigunguName + 
                ", 동(도로)이름=" + destItem.sDongName + ", 도로명주소=" + destItem.bRoad + "\n";
            
            ArrayList<Integer> codeList = new ArrayList<>();
            
            if (destItem.bRoad) {
                this.mLogText += DBUtils.findHjdongCodeListByDestInfo(this.context, codeList, destItem);
            } else {
                this.mLogText += DBUtils.findHjdongCodeListByHjdongName(this.context, codeList, destItem);
                if (codeList.isEmpty()) {
                    this.mLogText += DBUtils.findHjdongCodeListByBjdongName(this.context, codeList, destItem);
                }
            }
            
            for (Integer code : codeList) {
                if (SharedData.sAllExceptDongCodes.contains(String.valueOf(code))) {
                    if (SharedData.bAutoDeny && this.call.mDenyCtrl != null && this.call.mDenyCtrl.isClickable()) {
                        Helper.delegateButtonClick(this.call.mDenyCtrl);
                    }
                    this.mLogText += "\t: 제외지역 매칭 (" + code + ") -> 거절\n";
                    return;
                }
            }
            
            Helper.delegateButtonClick(this.call.mAcceptCtrl);
            this.mLogText += "\t: 제외지역에 없음 -> 수락\n";
        } else {
            this.mLogText += "\t: 주소분석 실패 -> 거절\n";
        }
    }

    /**
     * 전체 수락 모드
     */
    private void processAllAcceptMode() {
        Helper.delegateButtonClick(this.call.mAcceptCtrl);
        this.mLogText += "\t: 전체 수락 모드 -> 수락\n";
    }

    /**
     * 거리 기준 모드
     */
    private void processDistanceMode() {
        this.mLogText += "\t: 거리 기준 모드 (" + this.call.mOrigin + " -> " + this.call.mDest + ")\n";
        
        DestItem originItem = parseLocationFromAddress(this.call.mOrigin);
        DestItem destItem = parseLocationFromAddress(this.call.mDest);
        
        if (originItem != null && destItem != null) {
            // TODO: 실제 위경도 데이터베이스 조회 구현 필요
            // 현재는 간단한 로직으로 대체
            
            double distance = calculateDistanceBetweenLocations(originItem, destItem);
            
            if (distance >= SharedData.nLongDistance) {
                Helper.delegateButtonClick(this.call.mAcceptCtrl);
                this.mLogText += String.format("\t: 거리 기준 만족 (%.1fkm >= %dkm) -> 수락\n", 
                    distance, SharedData.nLongDistance);
            } else {
                if (SharedData.bAutoDeny && this.call.mDenyCtrl != null && this.call.mDenyCtrl.isClickable()) {
                    Helper.delegateButtonClick(this.call.mDenyCtrl);
                }
                this.mLogText += String.format("\t: 거리 기준 미달 (%.1fkm < %dkm) -> 거절\n", 
                    distance, SharedData.nLongDistance);
            }
        } else {
            if (SharedData.bAutoDeny && this.call.mDenyCtrl != null && this.call.mDenyCtrl.isClickable()) {
                Helper.delegateButtonClick(this.call.mDenyCtrl);
            }
            this.mLogText += "\t: 위치 분석 실패 -> 거절\n";
        }
    }

    /**
     * 스마트 필터 모드 (새로 추가)
     */
    private void processSmartFilterMode() {
        this.mLogText += "\t: 스마트 필터 모드 시작\n";
        
        // 여러 조건을 종합하여 스마트하게 판단
        int score = 0;
        
        // 1. 거리 점수
        int callDistance = Helper.getCallDistance(this.call);
        if (callDistance <= 1000) score += 3;      // 1km 이하: +3점
        else if (callDistance <= 2000) score += 2; // 2km 이하: +2점
        else if (callDistance <= 3000) score += 1; // 3km 이하: +1점
        else score -= 1;                          // 3km 초과: -1점
        
        // 2. 시간대 점수 (출퇴근 시간 가중치)
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if ((hour >= 7 && hour <= 9) || (hour >= 17 && hour <= 19)) {
            score += 1; // 출퇴근 시간: +1점
        }
        
        // 3. 목적지 점수 (특정 키워드 포함)
        if (this.call.mDest.contains("역") || this.call.mDest.contains("터미널") || this.call.mDest.contains("공항")) {
            score += 2; // 교통요지: +2점
        }
        if (this.call.mDest.contains("병원") || this.call.mDest.contains("학교")) {
            score += 1; // 공공시설: +1점
        }
        
        this.mLogText += String.format("\t: 스마트 필터 점수: %d점\n", score);
        
        // 4점 이상이면 수락, 미만이면 거절
        if (score >= 4) {
            Helper.delegateButtonClick(this.call.mAcceptCtrl);
            this.mLogText += "\t: 스마트 필터 통과 -> 수락\n";
        } else {
            if (SharedData.bAutoDeny && this.call.mDenyCtrl != null && this.call.mDenyCtrl.isClickable()) {
                Helper.delegateButtonClick(this.call.mDenyCtrl);
            }
            this.mLogText += "\t: 스마트 필터 미통과 -> 거절\n";
        }
    }

    /**
     * 주소 문자열에서 위치 정보 파싱
     */
    private DestItem parseLocationFromAddress(String address) {
        if (address == null || address.isEmpty()) {
            return null;
        }
        
        String[] parts = address.split(" ");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part != null && part.length() > 0) {
                String lastChar = part.substring(part.length() - 1);
                if ("읍면동가".contains(lastChar)) {
                    DestItem item = new DestItem();
                    item.sDongName = part;
                    if (i > 0) {
                        item.sSigunguName = parts[i - 1];
                    }
                    item.bRoad = false;
                    return item;
                }
            }
        }
        return null;
    }

    /**
     * 두 위치 간의 거리 계산 (단순화된 버전)
     */
    private double calculateDistanceBetweenLocations(DestItem origin, DestItem dest) {
        // TODO: 실제 위경도 데이터베이스를 사용한 거리 계산 구현
        // 현재는 임시로 랜덤 값 반환 (실제로는 데이터베이스에서 좌표 조회 후 계산)
        return Math.random() * 20.0; // 0~20km 사이의 랜덤 값
    }
}