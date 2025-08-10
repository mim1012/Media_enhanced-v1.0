package com.kakao.taxi.auto.model;

/**
 * 목적지 정보를 담는 데이터 클래스
 * 주소 분석을 통해 얻은 목적지 정보를 저장합니다.
 */
public class DestItem {
    /** 도로명 주소 여부 */
    public boolean bRoad = false;
    
    /** 시/군/구 이름 */
    public String sSigunguName = null;
    
    /** 동/읍/면 이름 또는 도로명 */
    public String sDongName = null;
}