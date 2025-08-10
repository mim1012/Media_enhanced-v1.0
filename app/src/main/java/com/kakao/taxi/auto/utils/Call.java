package com.kakao.taxi.auto.utils;

import android.view.accessibility.AccessibilityNodeInfo;

/**
 * 콜 정보 데이터 모델
 * 연구 목적으로 콜 정보를 저장하는 클래스
 */
public class Call {
    public String mDistance = null;       // 고객까지의 거리
    public String mOrigin = null;         // 출발지
    public String mDest = null;           // 도착지
    public AccessibilityNodeInfo mDenyCtrl = null;    // 거절 버튼 노드
    public AccessibilityNodeInfo mAcceptCtrl = null;  // 수락 버튼 노드
    
    // 추가 필드 (개선된 기능용)
    public long mTimestamp = 0;           // 콜 수신 시간
    public String mEstimatedFare = null;  // 예상 요금
    public String mCallType = null;       // 콜 종류 (일반, 예약, 플러스 등)
    
    public Call() {
        this.mTimestamp = System.currentTimeMillis();
    }
    
    @Override
    public String toString() {
        return "Call{" +
                "distance='" + mDistance + '\'' +
                ", origin='" + mOrigin + '\'' +
                ", dest='" + mDest + '\'' +
                ", timestamp=" + mTimestamp +
                ", type='" + mCallType + '\'' +
                '}';
    }
}