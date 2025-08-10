package com.kakao.taxi.auto.utils;

import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityNodeInfo;
import com.kakao.taxi.auto.model.DestItem;
import java.util.List;

/**
 * 접근성 서비스 관련 헬퍼 유틸리티 클래스
 * 카카오택시 앱의 UI 요소들을 분석하고 조작하는 기능을 제공합니다.
 */
public class Helper {
    
    /**
     * 노드 리스트가 유효한지 확인
     */
    public static boolean isValidNodes(List<AccessibilityNodeInfo> list) {
        return list != null && list.size() >= 1;
    }

    /**
     * 노드 리스트의 텍스트가 유효한지 확인
     */
    public static boolean isValidText(List<AccessibilityNodeInfo> list) {
        return (list == null || list.size() < 1 || list.get(0) == null || list.get(0).getText() == null) ? false : true;
    }

    /**
     * 단일 노드의 텍스트가 유효한지 확인
     */
    public static boolean isValidText(AccessibilityNodeInfo accessibilityNodeInfo) {
        return (accessibilityNodeInfo == null || accessibilityNodeInfo.getText() == null) ? false : true;
    }

    /**
     * 리사이클러뷰의 높이 반환
     */
    public static int getRecycleHeight(AccessibilityNodeInfo accessibilityNodeInfo) {
        if (accessibilityNodeInfo != null) {
            Rect rect = new Rect();
            accessibilityNodeInfo.getBoundsInScreen(rect);
            return rect.height();
        }
        return 0;
    }

    /**
     * View ID로 노드 리스트 조회
     */
    public static List<AccessibilityNodeInfo> getNodeListByViewId(AccessibilityNodeInfo accessibilityNodeInfo, String str) {
        return accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.kakao.taxi.driver:id/" + str);
    }

    /**
     * View ID로 첫 번째 노드 조회
     */
    public static AccessibilityNodeInfo getNodeByViewId(AccessibilityNodeInfo accessibilityNodeInfo, String str) {
        List<AccessibilityNodeInfo> nodeListByViewId = getNodeListByViewId(accessibilityNodeInfo, str);
        if (nodeListByViewId.size() == 0) {
            return null;
        }
        return nodeListByViewId.get(0);
    }

    /**
     * 일반 콜 정보를 파싱하여 Call 객체로 반환
     */
    public static Call getKakaoGeneralCall(AccessibilityNodeInfo accessibilityNodeInfo) {
        AccessibilityNodeInfo accessibilityNodeInfo2;
        Call call = new Call();
        try {
            List<AccessibilityNodeInfo> nodeListByViewId = getNodeListByViewId(accessibilityNodeInfo, "tv_destination");
            if (isValidNodes(nodeListByViewId)) {
                if (nodeListByViewId.get(0) != null && nodeListByViewId.get(0).getText() != null) {
                    call.mDest = nodeListByViewId.get(0).getText().toString().trim().replaceAll(" ", " ").replaceAll("[?]", " ").replaceAll("_ ", "").replaceAll("_", "").replaceAll("[-\\[\\]^/,'*:.!><~@#$%+=?|\"\\\\()]+", "");
                    List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText = accessibilityNodeInfo.findAccessibilityNodeInfosByText("수락");
                    if (findAccessibilityNodeInfosByText != null && !findAccessibilityNodeInfosByText.isEmpty() && findAccessibilityNodeInfosByText.get(0) != null && findAccessibilityNodeInfosByText.get(0).isClickable()) {
                        call.mAcceptCtrl = findAccessibilityNodeInfosByText.get(0);
                    }
                    if (call.mAcceptCtrl == null) {
                        List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText2 = accessibilityNodeInfo.findAccessibilityNodeInfosByText("직접결제 수락");
                        if (findAccessibilityNodeInfosByText2 != null && !findAccessibilityNodeInfosByText2.isEmpty() && findAccessibilityNodeInfosByText2.get(0) != null && findAccessibilityNodeInfosByText2.get(0).isClickable()) {
                            call.mAcceptCtrl = findAccessibilityNodeInfosByText2.get(0);
                        }
                        List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText3 = accessibilityNodeInfo.findAccessibilityNodeInfosByText("자동결제 수락");
                        if (findAccessibilityNodeInfosByText3 != null && !findAccessibilityNodeInfosByText3.isEmpty() && findAccessibilityNodeInfosByText3.get(0) != null && findAccessibilityNodeInfosByText3.get(0).isClickable()) {
                            call.mAcceptCtrl = findAccessibilityNodeInfosByText3.get(0);
                        }
                    }
                    List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText4 = accessibilityNodeInfo.findAccessibilityNodeInfosByText("거절");
                    if (findAccessibilityNodeInfosByText4 != null && !findAccessibilityNodeInfosByText4.isEmpty() && findAccessibilityNodeInfosByText4.get(0) != null && findAccessibilityNodeInfosByText4.get(0).isClickable()) {
                        call.mDenyCtrl = findAccessibilityNodeInfosByText4.get(0);
                    }
                    List<AccessibilityNodeInfo> nodeListByViewId2 = getNodeListByViewId(accessibilityNodeInfo, "tv_origin_label_distance");
                    if (nodeListByViewId2 != null && !nodeListByViewId2.isEmpty() && (accessibilityNodeInfo2 = nodeListByViewId2.get(0)) != null && accessibilityNodeInfo2.getText() != null) {
                        call.mDistance = accessibilityNodeInfo2.getText().toString().trim();
                    }
                    List<AccessibilityNodeInfo> nodeListByViewId3 = getNodeListByViewId(accessibilityNodeInfo, "tv_origin");
                    if (nodeListByViewId3 != null && !nodeListByViewId3.isEmpty() && nodeListByViewId3.get(0) != null && nodeListByViewId3.get(0).getText() != null) {
                        call.mOrigin = nodeListByViewId3.get(0).getText().toString().trim().replaceAll(" ", " ").replaceAll("[?]", " ").replaceAll("_ ", "").replaceAll("_", "").replaceAll("[-\\[\\]^/,'*:.!><~@#$%+=?|\"\\\\()]+", "");
                    }
                }
                return call;
            }
            return null;
        } catch (Exception unused) {
            return null;
        }
    }

    /**
     * 리스트 콜 정보를 파싱하여 Call 객체로 반환 (3개 파라미터)
     */
    public static Call getKakaoListCall(AccessibilityNodeInfo accessibilityNodeInfo, AccessibilityNodeInfo accessibilityNodeInfo2, AccessibilityNodeInfo accessibilityNodeInfo3) {
        try {
            Call call = new Call();
            if (isValidText(accessibilityNodeInfo) && isValidText(accessibilityNodeInfo2)) {
                call.mDest = accessibilityNodeInfo.getText().toString().trim().replaceAll(" ", " ").replaceAll("[?]", " ").replaceAll("_ ", "").replaceAll("_", "").replaceAll("[-\\[\\]^/,'*:.!><~@#$%+=?|\"\\\\()]+", "");
                String obj = accessibilityNodeInfo2.getText().toString();
                if (obj.contains("손님까지")) {
                    obj = obj.substring(obj.indexOf("손님까지") + 5);
                }
                call.mDistance = obj.trim();
                if (!accessibilityNodeInfo3.isClickable()) {
                    accessibilityNodeInfo3 = accessibilityNodeInfo3.getParent();
                }
                call.mAcceptCtrl = accessibilityNodeInfo3;
                return call;
            }
        } catch (Exception unused) {
        }
        return null;
    }

    /**
     * 리스트 콜 정보를 파싱하여 Call 객체로 반환 (1개 파라미터)
     */
    public static Call getKakaoListCall(AccessibilityNodeInfo accessibilityNodeInfo) {
        try {
            Call call = new Call();
            List<AccessibilityNodeInfo> nodeListByViewId = getNodeListByViewId(accessibilityNodeInfo, "tv_destination");
            if (isValidText(nodeListByViewId)) {
                List<AccessibilityNodeInfo> nodeListByViewId2 = getNodeListByViewId(accessibilityNodeInfo, "tv_origin_label_distance");
                if (isValidText(nodeListByViewId2)) {
                    List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText = accessibilityNodeInfo.findAccessibilityNodeInfosByText("수락");
                    if (isValidText(findAccessibilityNodeInfosByText)) {
                        call.mDest = nodeListByViewId.get(0).getText().toString().trim().replaceAll(" ", " ").replaceAll("[?]", " ").replaceAll("_ ", "").replaceAll("_", "").replaceAll("[-\\[\\]^/,'*:.!><~@#$%+=?|\"\\\\()]+", "");
                        String obj = nodeListByViewId2.get(0).getText().toString();
                        if (obj.contains("손님까지")) {
                            obj = obj.substring(obj.indexOf("손님까지") + 5);
                        }
                        call.mDistance = obj.trim();
                        AccessibilityNodeInfo accessibilityNodeInfo2 = findAccessibilityNodeInfosByText.get(0);
                        if (accessibilityNodeInfo2 != null && !accessibilityNodeInfo2.isClickable()) {
                            accessibilityNodeInfo2 = accessibilityNodeInfo2.getParent();
                        }
                        call.mAcceptCtrl = accessibilityNodeInfo2;
                        return call;
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

    /**
     * 버튼 클릭을 메인 스레드에서 실행
     */
    public static void delegateButtonClick(final AccessibilityNodeInfo accessibilityNodeInfo) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    accessibilityNodeInfo.performAction(16);
                    accessibilityNodeInfo.recycle();
                } catch (Exception unused) {
                }
            }
        });
    }

    /**
     * Call 객체로부터 거리를 미터 단위로 파싱하여 반환
     * 개선된 거리 필터링 로직 지원
     */
    public static int getCallDistance(Call call) {
        double parseDouble;
        if (!call.mDistance.equals("")) {
            if (call.mDistance.contains("k") || call.mDistance.contains("K")) {
                String distanceStr = call.mDistance.toLowerCase().replace("k", "").replace("m", "").trim();
                parseDouble = Double.parseDouble(distanceStr);
                return (int) (parseDouble * 1000.0d);
            } else if (call.mDistance.contains("m") || call.mDistance.contains("M")) {
                String distanceStr = call.mDistance.toLowerCase().replace("m", "").trim();
                return Integer.parseInt(distanceStr);
            }
        }
        return 9999;
    }

    /**
     * 목적지 주소를 분석하여 DestItem 객체로 반환
     */
    public static DestItem analyzeDestination(Call call) {
        int i;
        int i2;
        int i3;
        int i4;
        String[] split = call.mDest.split(" ");
        for (int i5 = 0; i5 < split.length; i5++) {
            String str = split[i5];
            if (str != null && str.length() != 0) {
                if (str.equals("정읍")) {
                    str = "정읍시";
                }
                String substring = str.substring(str.length() - 1);
                if ("읍면동가".contains(substring)) {
                    DestItem destItem = new DestItem();
                    destItem.sDongName = str;
                    if (i5 >= 2) {
                        if (split[i5 - 2].endsWith("시")) {
                            if (split[i5 - 1].endsWith("구")) {
                                destItem.sSigunguName = split[i5 - 2] + split[i5 - 1];
                            }
                        }
                        destItem.sSigunguName = split[i5 - 1];
                    } else if (i5 == 1) {
                        destItem.sSigunguName = split[0];
                    }
                    destItem.bRoad = false;
                    return destItem;
                } else if ("로길".contains(substring)) {
                    DestItem destItem2 = new DestItem();
                    destItem2.sDongName = str;
                    if (i5 >= 2) {
                        if (split[i5 - 2].endsWith("시")) {
                            if (split[i5 - 1].endsWith("구")) {
                                destItem2.sSigunguName = split[i5 - 2] + split[i5 - 1];
                            }
                        }
                        destItem2.sSigunguName = split[i5 - 1];
                    } else if (i5 == 1) {
                        destItem2.sSigunguName = split[0];
                    }
                    destItem2.bRoad = true;
                    return destItem2;
                }
            }
        }
        return null;
    }

    /**
     * 두 Call 객체가 동일한지 비교
     */
    public static boolean equalGeneralCalls(Call call, Call call2) {
        return call != null && call2 != null && call.mDest.equals(call2.mDest) && call.mDistance.equals(call2.mDistance) && call.mOrigin.equals(call2.mOrigin);
    }

    /**
     * 거리 필터링을 위한 개선된 거리 체크 메서드
     * 0.7km, 1km 등 새로운 거리 프리셋 지원
     */
    public static boolean isWithinDistanceThreshold(Call call, int thresholdMeters) {
        int distance = getCallDistance(call);
        return distance <= thresholdMeters;
    }

    /**
     * 거리 문자열을 보다 정확하게 파싱
     */
    public static double parseDistanceToKm(String distanceStr) {
        if (distanceStr == null || distanceStr.isEmpty()) {
            return 0.0;
        }
        
        try {
            String cleaned = distanceStr.toLowerCase().trim();
            if (cleaned.contains("km")) {
                return Double.parseDouble(cleaned.replace("km", "").trim());
            } else if (cleaned.contains("k")) {
                return Double.parseDouble(cleaned.replace("k", "").trim());
            } else if (cleaned.contains("m")) {
                return Double.parseDouble(cleaned.replace("m", "").trim()) / 1000.0;
            }
            // 단위가 없으면 미터로 가정
            return Double.parseDouble(cleaned) / 1000.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}