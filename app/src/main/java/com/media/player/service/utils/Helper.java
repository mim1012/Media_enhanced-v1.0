package com.media.player.service.utils;

import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityNodeInfo;
import com.media.player.service.model.ItemInfo;
import java.util.List;

/**
 * 헬퍼 유틸리티 클래스
 * 원본 비즈니스 로직 완전 이식
 */
public class Helper {
    
    public static boolean isValidNodes(List<AccessibilityNodeInfo> list) {
        return list != null && list.size() >= 1;
    }

    public static boolean isValidText(List<AccessibilityNodeInfo> list) {
        return (list == null || list.size() < 1 || list.get(0) == null || list.get(0).getText() == null) ? false : true;
    }

    public static boolean isValidText(AccessibilityNodeInfo accessibilityNodeInfo) {
        return (accessibilityNodeInfo == null || accessibilityNodeInfo.getText() == null) ? false : true;
    }

    public static int getRecycleHeight(AccessibilityNodeInfo accessibilityNodeInfo) {
        if (accessibilityNodeInfo != null) {
            Rect rect = new Rect();
            accessibilityNodeInfo.getBoundsInScreen(rect);
            return rect.height();
        }
        return 0;
    }

    public static List<AccessibilityNodeInfo> getNodeListByViewId(AccessibilityNodeInfo accessibilityNodeInfo, String str) {
        return accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.kakao.taxi.driver:id/" + str);
    }

    public static AccessibilityNodeInfo getNodeByViewId(AccessibilityNodeInfo accessibilityNodeInfo, String str) {
        List<AccessibilityNodeInfo> nodeListByViewId = getNodeListByViewId(accessibilityNodeInfo, str);
        if (nodeListByViewId.size() == 0) {
            return null;
        }
        return nodeListByViewId.get(0);
    }

    // 핵심 비즈니스 로직 - 개별 콜 정보 추출
    public static MediaItem getMediaItem(AccessibilityNodeInfo accessibilityNodeInfo) {
        AccessibilityNodeInfo accessibilityNodeInfo2;
        MediaItem item = new MediaItem();
        try {
            List<AccessibilityNodeInfo> nodeListByViewId = getNodeListByViewId(accessibilityNodeInfo, "tv_destination");
            if (isValidNodes(nodeListByViewId)) {
                if (nodeListByViewId.get(0) != null && nodeListByViewId.get(0).getText() != null) {
                    // 도착지 텍스트 정제
                    item.mTarget = nodeListByViewId.get(0).getText().toString().trim()
                        .replaceAll(" ", " ")
                        .replaceAll("[?]", " ")
                        .replaceAll("_ ", "")
                        .replaceAll("_", "")
                        .replaceAll("[-\\[\\]^/,'*:.!><~@#$%+=?|\"\\\\()]+", "");
                    
                    // 수락 버튼 찾기
                    List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText = accessibilityNodeInfo.findAccessibilityNodeInfosByText("수락");
                    if (findAccessibilityNodeInfosByText != null && !findAccessibilityNodeInfosByText.isEmpty() 
                            && findAccessibilityNodeInfosByText.get(0) != null && findAccessibilityNodeInfosByText.get(0).isClickable()) {
                        item.mPlayCtrl = findAccessibilityNodeInfosByText.get(0);
                    }
                    
                    if (item.mPlayCtrl == null) {
                        List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText2 = accessibilityNodeInfo.findAccessibilityNodeInfosByText("직접결제 수락");
                        if (findAccessibilityNodeInfosByText2 != null && !findAccessibilityNodeInfosByText2.isEmpty() 
                                && findAccessibilityNodeInfosByText2.get(0) != null && findAccessibilityNodeInfosByText2.get(0).isClickable()) {
                            item.mPlayCtrl = findAccessibilityNodeInfosByText2.get(0);
                        }
                        
                        List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText3 = accessibilityNodeInfo.findAccessibilityNodeInfosByText("자동결제 수락");
                        if (findAccessibilityNodeInfosByText3 != null && !findAccessibilityNodeInfosByText3.isEmpty() 
                                && findAccessibilityNodeInfosByText3.get(0) != null && findAccessibilityNodeInfosByText3.get(0).isClickable()) {
                            item.mPlayCtrl = findAccessibilityNodeInfosByText3.get(0);
                        }
                    }
                    
                    // 거절 버튼 찾기
                    List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText4 = accessibilityNodeInfo.findAccessibilityNodeInfosByText("거절");
                    if (findAccessibilityNodeInfosByText4 != null && !findAccessibilityNodeInfosByText4.isEmpty() 
                            && findAccessibilityNodeInfosByText4.get(0) != null && findAccessibilityNodeInfosByText4.get(0).isClickable()) {
                        item.mSkipCtrl = findAccessibilityNodeInfosByText4.get(0);
                    }
                    
                    // 거리 정보 추출
                    List<AccessibilityNodeInfo> nodeListByViewId2 = getNodeListByViewId(accessibilityNodeInfo, "tv_origin_label_distance");
                    if (nodeListByViewId2 != null && !nodeListByViewId2.isEmpty() 
                            && (accessibilityNodeInfo2 = nodeListByViewId2.get(0)) != null && accessibilityNodeInfo2.getText() != null) {
                        item.mQuality = accessibilityNodeInfo2.getText().toString().trim();
                    }
                    
                    // 출발지 정보 추출
                    List<AccessibilityNodeInfo> nodeListByViewId3 = getNodeListByViewId(accessibilityNodeInfo, "tv_origin");
                    if (nodeListByViewId3 != null && !nodeListByViewId3.isEmpty() 
                            && nodeListByViewId3.get(0) != null && nodeListByViewId3.get(0).getText() != null) {
                        item.mSource = nodeListByViewId3.get(0).getText().toString().trim()
                            .replaceAll(" ", " ")
                            .replaceAll("[?]", " ")
                            .replaceAll("_ ", "")
                            .replaceAll("_", "")
                            .replaceAll("[-\\[\\]^/,'*:.!><~@#$%+=?|\"\\\\()]+", "");
                    }
                }
                return item;
            }
            return null;
        } catch (Exception unused) {
            return null;
        }
    }

    // getGeneralCall 별칭
    public static MediaItem getGeneralCall(AccessibilityNodeInfo accessibilityNodeInfo) {
        return getMediaItem(accessibilityNodeInfo);
    }

    // 리스트 콜 정보 추출 (3개 파라미터 버전)
    public static MediaItem getListItem(AccessibilityNodeInfo accessibilityNodeInfo, AccessibilityNodeInfo accessibilityNodeInfo2, AccessibilityNodeInfo accessibilityNodeInfo3) {
        try {
            MediaItem item = new MediaItem();
            if (isValidText(accessibilityNodeInfo) && isValidText(accessibilityNodeInfo2)) {
                item.mTarget = accessibilityNodeInfo.getText().toString().trim()
                    .replaceAll(" ", " ")
                    .replaceAll("[?]", " ")
                    .replaceAll("_ ", "")
                    .replaceAll("_", "")
                    .replaceAll("[-\\[\\]^/,'*:.!><~@#$%+=?|\"\\\\()]+", "");
                    
                String obj = accessibilityNodeInfo2.getText().toString();
                if (obj.contains("손님까지")) {
                    obj = obj.substring(obj.indexOf("손님까지") + 5);
                }
                item.mQuality = obj.trim();
                
                if (!accessibilityNodeInfo3.isClickable()) {
                    accessibilityNodeInfo3 = accessibilityNodeInfo3.getParent();
                }
                item.mPlayCtrl = accessibilityNodeInfo3;
                return item;
            }
        } catch (Exception unused) {
        }
        return null;
    }

    // 리스트 콜 정보 추출 (1개 파라미터 버전)
    public static MediaItem getListItem(AccessibilityNodeInfo accessibilityNodeInfo) {
        try {
            MediaItem item = new MediaItem();
            List<AccessibilityNodeInfo> nodeListByViewId = getNodeListByViewId(accessibilityNodeInfo, "tv_destination");
            if (isValidText(nodeListByViewId)) {
                List<AccessibilityNodeInfo> nodeListByViewId2 = getNodeListByViewId(accessibilityNodeInfo, "tv_origin_label_distance");
                if (isValidText(nodeListByViewId2)) {
                    List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText = accessibilityNodeInfo.findAccessibilityNodeInfosByText("수락");
                    if (isValidText(findAccessibilityNodeInfosByText)) {
                        item.mTarget = nodeListByViewId.get(0).getText().toString().trim()
                            .replaceAll(" ", " ")
                            .replaceAll("[?]", " ")
                            .replaceAll("_ ", "")
                            .replaceAll("_", "")
                            .replaceAll("[-\\[\\]^/,'*:.!><~@#$%+=?|\"\\\\()]+", "");
                            
                        String obj = nodeListByViewId2.get(0).getText().toString();
                        if (obj.contains("손님까지")) {
                            obj = obj.substring(obj.indexOf("손님까지") + 5);
                        }
                        item.mQuality = obj.trim();
                        
                        AccessibilityNodeInfo accessibilityNodeInfo2 = findAccessibilityNodeInfosByText.get(0);
                        if (accessibilityNodeInfo2 != null && !accessibilityNodeInfo2.isClickable()) {
                            accessibilityNodeInfo2 = accessibilityNodeInfo2.getParent();
                        }
                        item.mPlayCtrl = accessibilityNodeInfo2;
                        return item;
                    }
                    return null;
                }
                return null;
            }
            return null;
        } catch (Exception unused) {
            return null;
        }
    }

    // getListCall 별칭들
    public static MediaItem getListCall(AccessibilityNodeInfo a1, AccessibilityNodeInfo a2, AccessibilityNodeInfo a3) {
        return getListItem(a1, a2, a3);
    }
    
    public static MediaItem getListCall(AccessibilityNodeInfo accessibilityNodeInfo) {
        return getListItem(accessibilityNodeInfo);
    }

    // 버튼 클릭 실행 - 핵심 비즈니스 로직 (메모리 최적화 포함)
    public static void delegateButtonClick(final AccessibilityNodeInfo accessibilityNodeInfo) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    accessibilityNodeInfo.performAction(16);  // ACTION_CLICK
                    // 메모리 최적화 - 사용 후 즉시 recycle
                    accessibilityNodeInfo.recycle();
                } catch (Exception unused) {
                }
            }
        });
    }
    
    // 정규식 패턴 (원본 그대로)
    private static final String CLEAN_PATTERN = "[-\\[\\]^/,'*:.!><~@#$%+=?|\"\\\\()]+";
    
    // 텍스트 정제 메서드 (정규식 최적화)
    public static String cleanText(String text) {
        if (text == null) return "";
        return text.trim()
            .replaceAll(" ", " ")      // 공백 정규화
            .replaceAll("[?]", " ")     // 물음표 제거
            .replaceAll("_ ", "")       // 언더바+공백 제거
            .replaceAll("_", "")        // 언더바 제거
            .replaceAll(CLEAN_PATTERN, "");  // 특수문자 제거
    }

    // 거리 계산 - 핵심 비즈니스 로직
    public static int getCallDistance(MediaItem item) {
        double parseDouble;
        if (!item.mQuality.equals("")) {
            if (item.mQuality.contains("k")) {
                parseDouble = Double.parseDouble(item.mQuality.substring(0, item.mQuality.indexOf("k")).trim());
            } else if (item.mQuality.contains("K")) {
                parseDouble = Double.parseDouble(item.mQuality.substring(0, item.mQuality.indexOf("K")).trim());
            } else if (item.mQuality.contains("m")) {
                return Integer.parseInt(item.mQuality.replace("m", "").trim());
            } else {
                if (item.mQuality.contains("M")) {
                    return Integer.parseInt(item.mQuality.replace("M", "").trim());
                }
            }
            return (int) (parseDouble * 1000.0d);
        }
        return 9999;
    }

    // 도착지 분석 - 핵심 비즈니스 로직
    public static ItemInfo analyzeDestination(MediaItem item) {
        int i;
        int i2;
        String[] split = item.mTarget.split(" ");
        for (int i5 = 0; i5 < split.length; i5++) {
            String str = split[i5];
            if (str != null && str.length() != 0) {
                if (str.equals("정읍")) {
                    str = "정읍시";
                }
                String substring = str.substring(str.length() - 1);
                if ("읍면동가".contains(substring)) {
                    ItemInfo itemInfo = new ItemInfo();
                    itemInfo.sDongName = str;
                    if (i5 >= 2) {
                        if (split[i5 - 2].endsWith("시")) {
                            if (split[i5 - 1].endsWith("구")) {
                                itemInfo.sSigunguName = split[i5 - 2] + split[i5 - 1];
                            }
                        } else {
                            itemInfo.sSigunguName = split[i5 - 1];
                        }
                    } else if (i5 == 1) {
                        itemInfo.sSigunguName = split[0];
                    }
                    itemInfo.bRoad = false;
                    return itemInfo;
                } else if ("로길".contains(substring)) {
                    ItemInfo itemInfo = new ItemInfo();
                    itemInfo.sDongName = str;
                    if (i5 >= 1) {
                        itemInfo.sSigunguName = split[i5 - 1];
                    }
                    itemInfo.bRoad = true;
                    return itemInfo;
                }
            }
        }
        return null;
    }

    // 두 아이템이 같은지 비교
    public static boolean equalItems(MediaItem item1, MediaItem item2) {
        if (item1 == null || item2 == null) {
            return false;
        }
        
        // 거리 비교
        if (item1.mQuality != null && item2.mQuality != null) {
            if (!item1.mQuality.equals(item2.mQuality)) {
                return false;
            }
        }
        
        // 도착지 비교
        if (item1.mTarget != null && item2.mTarget != null) {
            if (!item1.mTarget.equals(item2.mTarget)) {
                return false;
            }
        }
        
        // 출발지 비교
        if (item1.mSource != null && item2.mSource != null) {
            if (!item1.mSource.equals(item2.mSource)) {
                return false;
            }
        }
        
        return true;
    }
    
    // equalGeneralCalls 별칭
    public static boolean equalGeneralCalls(MediaItem item1, MediaItem item2) {
        return equalItems(item1, item2);
    }
}