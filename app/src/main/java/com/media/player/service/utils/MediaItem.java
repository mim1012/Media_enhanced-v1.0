package com.media.player.service.utils;

import android.view.accessibility.AccessibilityNodeInfo;

/**
 * 미디어 아이템 데이터 모델
 */
public class MediaItem {
    public String mQuality = null;        // 품질 정보
    public String mSource = null;         // 소스 정보
    public String mTarget = null;         // 타겟 정보
    public AccessibilityNodeInfo mSkipCtrl = null;    // 스킵 버튼
    public AccessibilityNodeInfo mPlayCtrl = null;    // 재생 버튼
    
    public long mTimestamp = 0;           // 타임스탬프
    public String mMetadata = null;       // 메타데이터
    public String mType = null;           // 타입
    
    public MediaItem() {
        this.mTimestamp = System.currentTimeMillis();
    }
    
    @Override
    public String toString() {
        return "MediaItem{" +
                "quality='" + mQuality + '\'' +
                ", source='" + mSource + '\'' +
                ", target='" + mTarget + '\'' +
                ", timestamp=" + mTimestamp +
                ", type='" + mType + '\'' +
                '}';
    }
}