package com.media.player.service.model;

/**
 * 아이템 정보 모델 (원본: DestItem)
 */
public class ItemInfo {
    public String sSigunguName;  // 시군구 이름
    public String sDongName;     // 동 이름
    public boolean bRoad;         // 도로명 주소 여부
    
    public ItemInfo() {
        this.sSigunguName = null;
        this.sDongName = null;
        this.bRoad = false;
    }
    
    @Override
    public String toString() {
        return "ItemInfo{" +
                "sigungu='" + sSigunguName + '\'' +
                ", dong='" + sDongName + '\'' +
                ", isRoad=" + bRoad +
                '}';
    }
}