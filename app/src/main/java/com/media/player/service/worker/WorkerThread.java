package com.media.player.service.worker;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import com.media.player.service.db.DatabaseHelper;
import com.media.player.service.model.ItemInfo;
import com.media.player.service.utils.MediaItem;
import com.media.player.service.utils.DBHelper;
import com.media.player.service.utils.Helper;
import com.media.player.service.utils.Logger;
import com.media.player.service.utils.DataStore;
import com.media.player.service.utils.Config;
import java.util.ArrayList;

/**
 * 워커 스레드 (원본: BoilerRunnable)
 * 핵심 비즈니스 로직 완전 이식
 */
public class WorkerThread implements Runnable {
    MediaItem item;
    Context context;
    String mLogText;

    public WorkerThread(MediaItem item, Context context) {
        this.item = item;
        this.context = context;
    }

    @Override
    public void run() {
        long currentTimeMillis = System.currentTimeMillis();
        this.mLogText = "";
        processWorker();
        String str = this.mLogText + "\t--소요시간--: " + (System.currentTimeMillis() - currentTimeMillis) + "ms";
        this.mLogText = str;
        Logger.log(str);
    }

    private void processWorker() {
        ItemInfo itemInfo;
        ItemInfo itemInfo2;
        double distanceTo;
        
        try {
            this.mLogText += "일반모드 콜분석 시작: " + this.item.mQuality + "," + this.item.mTarget + "\n";
            
            // 1. 거리 체크 - 핵심 비즈니스 로직
            int callDistance = Helper.getCallDistance(this.item);
            if (DataStore.nQuality != 0 && callDistance > DataStore.nQuality) {
                if (DataStore.bAutoSkip && this.item.mSkipCtrl != null && this.item.mSkipCtrl.isClickable()) {
                    Helper.delegateButtonClick(this.item.mSkipCtrl);
                }
                this.mLogText += "\t: 손님거리 체크 실패: 거절(설정: " + DataStore.nQuality + "m)\n";
                return;
            }
            
            // 2. 우선 제외지 체크 - 핵심 비즈니스 로직
            if (DataStore.aExclusionList != null && DataStore.aExclusionList.size() >= 1) {
                for (int i = 0; i < DataStore.aExclusionList.size(); i++) {
                    String str = DataStore.aExclusionList.get(i);
                    if (this.item.mTarget.contains(str)) {
                        this.mLogText += "\t: 우선제외지 -> 콜:" + this.item.mTarget + ", 지명:" + str + "\n";
                        return;
                    }
                }
            }
            
            // 3. 우선 키워드 체크 - 무조건 수락
            if (DataStore.aFilterList != null && DataStore.aFilterList.size() >= 1) {
                for (String keyword : DataStore.aFilterList) {
                    if (this.item.mTarget.contains(keyword)) {
                        this.mLogText += "\t: 우선키워드 매칭 -> 콜:" + this.item.mTarget + ", 키워드:" + keyword + " → 무조건 수락\n";
                        Helper.delegateButtonClick(this.item.mPlayCtrl);
                        return;
                    }
                }
            }
            
            // 4. 우선 선호지 체크 - DB 기반 지역
            if (DataStore.aPlaylistItems != null && DataStore.aPlaylistItems.size() >= 1) {
                for (int i2 = 0; i2 < DataStore.aPlaylistItems.size(); i2++) {
                    String str2 = DataStore.aPlaylistItems.get(i2);
                    if (this.item.mTarget.contains(str2)) {
                        this.mLogText += "\t: 우선선호지 -> 콜:" + this.item.mTarget + ", 지명:" + str2 + " 분석 성공: 접수\n";
                        Helper.delegateButtonClick(this.item.mPlayCtrl);
                        return;
                    }
                }
            }
            
            // 5. 전체콜 모드 체크 - 최우선 처리
            if (DataStore.bFullMode) {
                Helper.delegateButtonClick(this.item.mPlayCtrl);
                this.mLogText += "\t: 전체콜 모드: 무조건 접수\n";
                return;
            }
            
            // 6. 부분콜 모드 - 작업 모드별 처리
            int i3 = DataStore.nMode;
            
            if (i3 == Config.MODE_DEST) {  // 부분콜 모드 (256) - DB기반 + 키워드기반
                this.mLogText += "\t: 부분콜 모드 - DB 및 키워드 체크 시작\n";
                boolean shouldAccept = false;
                
                // 1단계: DB 기반 지역 체크 (sAllPlaylistCodes가 있을 때만)
                if (!DataStore.sAllPlaylistCodes.isEmpty()) {
                    this.mLogText += "\t: DB 기반 도착지 체크\n";
                    ItemInfo analyzeDestination = Helper.analyzeDestination(this.item);
                    
                    if (analyzeDestination != null && analyzeDestination.sDongName != null 
                            && (analyzeDestination.bRoad || analyzeDestination.sSigunguName != null)) {
                        this.mLogText += "\t: 주소분석 성공: 시도구명=" + analyzeDestination.sSigunguName 
                            + ", 동(도로)이름=" + analyzeDestination.sDongName 
                            + ", 도로명주소인가=" + analyzeDestination.bRoad + "\n";
                        
                        ArrayList<Integer> arrayList = new ArrayList();
                        if (analyzeDestination.bRoad) {
                            this.mLogText += DBHelper.findHjdongCodeListByDestInfo(this.context, arrayList, analyzeDestination);
                        } else {
                            this.mLogText += DBHelper.findHjdongCodeListByHjdongName(this.context, arrayList, analyzeDestination);
                            if (arrayList.isEmpty()) {
                                this.mLogText += DBHelper.findHjdongCodeListByBjdongName(this.context, arrayList, analyzeDestination);
                            }
                        }
                        
                        for (Integer num : arrayList) {
                            int intValue = num.intValue();
                            if (DataStore.sAllPlaylistCodes.contains(String.valueOf(intValue))) {
                                this.mLogText += "\t: DB 도착지 매칭 성공: " + intValue + " → 접수\n";
                                shouldAccept = true;
                                break;
                            }
                        }
                    }
                }
                
                // 2단계: 키워드 기반 체크 (DB 매칭 실패 시 또는 DB 코드가 없을 때)
                if (!shouldAccept && DataStore.aFilterList != null && !DataStore.aFilterList.isEmpty()) {
                    this.mLogText += "\t: 키워드 기반 체크\n";
                    for (String filter : DataStore.aFilterList) {
                        if (this.item.mTarget.contains(filter)) {
                            this.mLogText += "\t: 키워드 매칭 성공: " + filter + " → 접수\n";
                            shouldAccept = true;
                            break;
                        }
                    }
                }
                
                // 3단계: 결과 처리
                if (shouldAccept) {
                    Helper.delegateButtonClick(this.item.mPlayCtrl);
                    this.mLogText += "\t: 조건 만족 → 수락\n";
                } else {
                    this.mLogText += "\t: DB 및 키워드 모두 매칭 실패 → 무시\n";
                }
                
            } else if (i3 == Config.MODE_EXCEPT) {  // 제외지 모드 (512)
                if (DataStore.sAllExclusionCodes.isEmpty()) {
                    return;
                }
                this.mLogText += "\t: 제외지 체크 시작\n";
                ItemInfo analyzeDestination2 = Helper.analyzeDestination(this.item);
                
                if (analyzeDestination2 != null && analyzeDestination2.sDongName != null 
                        && (analyzeDestination2.bRoad || analyzeDestination2.sSigunguName != null)) {
                    this.mLogText += "\t: 주소분석 성공: 시도구명=" + analyzeDestination2.sSigunguName 
                        + ", 동(도로)이름=" + analyzeDestination2.sDongName 
                        + ", 도로명주소인가=" + analyzeDestination2.bRoad + "\n";
                    
                    ArrayList arrayList2 = new ArrayList();
                    if (analyzeDestination2.bRoad) {
                        this.mLogText += DBHelper.findHjdongCodeListByDestInfo(this.context, arrayList2, analyzeDestination2);
                    } else {
                        this.mLogText += DBHelper.findHjdongCodeListByHjdongName(this.context, arrayList2, analyzeDestination2);
                        if (arrayList2.isEmpty()) {
                            this.mLogText += DBHelper.findHjdongCodeListByBjdongName(this.context, arrayList2, analyzeDestination2);
                        }
                    }
                    
                    for (int i4 = 0; !arrayList2.isEmpty() && i4 < arrayList2.size(); i4++) {
                        int intValue2 = ((Integer) arrayList2.get(i4)).intValue();
                        if (DataStore.sAllExclusionCodes.contains(String.valueOf(intValue2))) {
                            if (DataStore.bAutoSkip && this.item.mSkipCtrl != null && this.item.mSkipCtrl.isClickable()) {
                                Helper.delegateButtonClick(this.item.mSkipCtrl);
                            }
                            this.mLogText += "\t: 제외지리스트에 있음 : " + intValue2 + " 거절\n";
                            return;
                        }
                    }
                    Helper.delegateButtonClick(this.item.mPlayCtrl);
                    this.mLogText += "\t: 제외지리스트에 없음 : 접수\n";
                    return;
                }
                this.mLogText += "\t: 도착지주소 분석 실패: 거절\n";
                
            } else if (i3 == Config.MODE_ALL) {  // 전체 모드 (768) - 이제 bFullMode로 대체됨
                Helper.delegateButtonClick(this.item.mPlayCtrl);
                this.mLogText += "\t: 전체모드: 접수\n";
                
            } else if (i3 == Config.MODE_LONGDISTANCE) {  // 장거리 모드 (1024)
                this.mLogText += "\t" + this.item.mSource + " -> " + this.item.mTarget + ": 거리모드 체크 시작\n";
                
                // 출발지와 도착지 분석
                String[] split = this.item.mTarget.split(" ");
                ItemInfo destInfo = null;
                ItemInfo originInfo = null;
                
                // 도착지 분석
                for (int i5 = 0; i5 < split.length; i5++) {
                    String str3 = split[i5];
                    if (str3 != null && str3.length() != 0) {
                        String substring = str3.substring(str3.length() - 1);
                        if ("시구군읍면동가로길".contains(substring)) {
                            destInfo = new ItemInfo();
                            destInfo.sDongName = str3;
                            if (i5 >= 1) {
                                destInfo.sSigunguName = split[i5 - 1];
                            }
                            break;
                        }
                    }
                }
                
                // 출발지 분석
                if (this.item.mSource != null) {
                    String[] split2 = this.item.mSource.split(" ");
                    for (int i6 = 0; i6 < split2.length; i6++) {
                        String str4 = split2[i6];
                        if (str4 != null && str4.length() != 0) {
                            String substring2 = str4.substring(str4.length() - 1);
                            if ("시구군읍면동가로길".contains(substring2)) {
                                originInfo = new ItemInfo();
                                originInfo.sDongName = str4;
                                if (i6 >= 1) {
                                    originInfo.sSigunguName = split2[i6 - 1];
                                }
                                break;
                            }
                        }
                    }
                }
                
                // 거리 계산 (DB에서 좌표 조회 후)
                if (destInfo != null && originInfo != null) {
                    double distance = calculateDistance(originInfo, destInfo);
                    this.mLogText += "\t: 계산된 거리: " + String.format("%.1f", distance) + "km\n";
                    
                    if (distance >= (DataStore.nAdvancedQuality / 1000.0)) {  // km 단위로 변환
                        Helper.delegateButtonClick(this.item.mPlayCtrl);
                        this.mLogText += "\t: 장거리 조건 만족 (" + (DataStore.nAdvancedQuality/1000.0) + "km 이상): 접수\n";
                        return;
                    } else {
                        if (DataStore.bAutoSkip && this.item.mSkipCtrl != null && this.item.mSkipCtrl.isClickable()) {
                            Helper.delegateButtonClick(this.item.mSkipCtrl);
                        }
                        this.mLogText += "\t: 장거리 조건 불만족: 거절\n";
                        return;
                    }
                } else {
                    this.mLogText += "\t: 주소 분석 실패로 장거리 계산 불가\n";
                }
                
            } else {  // MODE_NONE 또는 기타 - 기본 키워드 필터 모드
                this.mLogText += "\t: 기본 모드 - 키워드 필터 체크\n";
                
                // 키워드 필터가 있는 경우 체크
                if (DataStore.aFilterList != null && !DataStore.aFilterList.isEmpty()) {
                    boolean matched = false;
                    for (String filter : DataStore.aFilterList) {
                        if (this.item.mTarget.contains(filter)) {
                            matched = true;
                            this.mLogText += "\t: 키워드 매칭됨: " + filter + " → 접수\n";
                            break;
                        }
                    }
                    
                    if (matched) {
                        Helper.delegateButtonClick(this.item.mPlayCtrl);
                    } else {
                        if (DataStore.bAutoSkip && this.item.mSkipCtrl != null && this.item.mSkipCtrl.isClickable()) {
                            Helper.delegateButtonClick(this.item.mSkipCtrl);
                        }
                        this.mLogText += "\t: 키워드 매칭 안됨 → 거절\n";
                    }
                } else {
                    // 키워드 필터가 없으면 기본 수락
                    Helper.delegateButtonClick(this.item.mPlayCtrl);
                    this.mLogText += "\t: 키워드 필터 없음 → 기본 접수\n";
                }
            }
            
        } catch (Exception e) {
            this.mLogText += "\t: 처리 중 오류 발생: " + e.getMessage() + "\n";
            Logger.log(e);
        }
    }
    
    /**
     * 두 지점 간의 직선거리 계산 (Haversine 공식)
     */
    private double calculateDistance(ItemInfo origin, ItemInfo dest) {
        try {
            // DB에서 좌표 조회
            double[] originCoords = DBHelper.getCoordinates(this.context, origin.sDongName, origin.sSigunguName);
            double[] destCoords = DBHelper.getCoordinates(this.context, dest.sDongName, dest.sSigunguName);
            
            // 좌표가 없는 경우
            if (originCoords[0] == 0.0 && originCoords[1] == 0.0) {
                return 0.0;
            }
            if (destCoords[0] == 0.0 && destCoords[1] == 0.0) {
                return 0.0;
            }
            
            // Haversine 공식으로 거리 계산
            return calculateHaversineDistance(originCoords[0], originCoords[1], destCoords[0], destCoords[1]);
            
        } catch (Exception e) {
            Logger.log("거리 계산 실패: " + e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Haversine 공식을 이용한 두 좌표 간 거리 계산 (km 단위)
     */
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 지구 반지름 (km)
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c; // km 단위로 반환
    }
}