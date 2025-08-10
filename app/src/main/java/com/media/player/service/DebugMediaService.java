package com.media.player.service;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.util.Log;
import java.util.List;

/**
 * 디버그용 접근성 서비스 - 실제로 무엇이 감지되는지 확인
 */
public class DebugMediaService extends AccessibilityService {
    private static final String TAG = "DEBUG_KAKAO";

    @Override
    public void onInterrupt() {
        Log.d(TAG, "서비스 중단됨");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        try {
            // 카카오T 드라이버 앱 이벤트만 처리
            if (!"com.kakao.taxi.driver".equals(event.getPackageName().toString())) {
                return;
            }
            
            Log.d(TAG, "=== 이벤트 감지 ===");
            Log.d(TAG, "이벤트 타입: " + event.getEventType());
            Log.d(TAG, "패키지: " + event.getPackageName());
            
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root != null) {
                // v_arrow 찾기 (콜 상세 화면)
                List<AccessibilityNodeInfo> arrows = root.findAccessibilityNodeInfosByViewId(
                    "com.kakao.taxi.driver:id/v_arrow");
                if (arrows != null && !arrows.isEmpty()) {
                    Log.d(TAG, "★★★ 콜 화면 감지됨! v_arrow 발견 ★★★");
                    
                    // 수락 버튼 찾기
                    List<AccessibilityNodeInfo> acceptBtns = root.findAccessibilityNodeInfosByText("수락");
                    if (acceptBtns != null && !acceptBtns.isEmpty()) {
                        Log.d(TAG, "✓ '수락' 버튼 발견: " + acceptBtns.size() + "개");
                        for (AccessibilityNodeInfo btn : acceptBtns) {
                            Log.d(TAG, "  - 클릭 가능: " + btn.isClickable());
                            Log.d(TAG, "  - 활성화: " + btn.isEnabled());
                        }
                    }
                    
                    // 직접결제 수락 찾기
                    List<AccessibilityNodeInfo> directBtns = root.findAccessibilityNodeInfosByText("직접결제 수락");
                    if (directBtns != null && !directBtns.isEmpty()) {
                        Log.d(TAG, "✓ '직접결제 수락' 버튼 발견: " + directBtns.size() + "개");
                    }
                    
                    // 자동결제 수락 찾기  
                    List<AccessibilityNodeInfo> autoBtns = root.findAccessibilityNodeInfosByText("자동결제 수락");
                    if (autoBtns != null && !autoBtns.isEmpty()) {
                        Log.d(TAG, "✓ '자동결제 수락' 버튼 발견: " + autoBtns.size() + "개");
                    }
                    
                    // 도착지 정보
                    List<AccessibilityNodeInfo> dests = root.findAccessibilityNodeInfosByViewId(
                        "com.kakao.taxi.driver:id/tv_destination");
                    if (dests != null && !dests.isEmpty() && dests.get(0).getText() != null) {
                        Log.d(TAG, "도착지: " + dests.get(0).getText());
                    }
                    
                    // 거리 정보
                    List<AccessibilityNodeInfo> distances = root.findAccessibilityNodeInfosByViewId(
                        "com.kakao.taxi.driver:id/tv_origin_label_distance");
                    if (distances != null && !distances.isEmpty() && distances.get(0).getText() != null) {
                        Log.d(TAG, "거리: " + distances.get(0).getText());
                    }
                }
                
                // 콜 리스트 찾기
                List<AccessibilityNodeInfo> lists = root.findAccessibilityNodeInfosByViewId(
                    "com.kakao.taxi.driver:id/lv_call_list");
                if (lists != null && !lists.isEmpty()) {
                    Log.d(TAG, "☆☆☆ 콜 리스트 화면 감지됨! ☆☆☆");
                }
                
                root.recycle();
            }
        } catch (Exception e) {
            Log.e(TAG, "에러 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}