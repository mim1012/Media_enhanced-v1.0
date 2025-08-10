package com.media.player.service.db;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 테스트용 간단한 주소 데이터베이스
 * 서버 없이 로컬 테스트를 위한 하드코딩된 데이터
 */
public class SimpleAddressDB {
    private static final Map<String, List<String>> AREA_MAP = new HashMap<>();
    
    static {
        // 서울 주요 지역 데이터
        AREA_MAP.put("강남구", Arrays.asList("역삼동", "삼성동", "청담동", "압구정동", "논현동", "개포동", "도곡동", "대치동", "수서동", "일원동", "세곡동"));
        AREA_MAP.put("서초구", Arrays.asList("서초동", "반포동", "잠원동", "방배동", "양재동", "내곡동", "우면동", "원지동", "신원동"));
        AREA_MAP.put("송파구", Arrays.asList("잠실동", "문정동", "가락동", "석촌동", "송파동", "방이동", "오금동", "신천동", "삼전동", "장지동", "거여동", "마천동"));
        AREA_MAP.put("강동구", Arrays.asList("천호동", "성내동", "길동", "둔촌동", "암사동", "명일동", "고덕동", "상일동", "강일동"));
        AREA_MAP.put("강서구", Arrays.asList("화곡동", "등촌동", "염창동", "가양동", "마곡동", "내발산동", "외발산동", "공항동", "방화동"));
        AREA_MAP.put("양천구", Arrays.asList("목동", "신정동", "신월동"));
        AREA_MAP.put("구로구", Arrays.asList("구로동", "가리봉동", "고척동", "개봉동", "오류동", "궁동", "온수동", "천왕동", "항동"));
        AREA_MAP.put("금천구", Arrays.asList("가산동", "독산동", "시흥동"));
        AREA_MAP.put("영등포구", Arrays.asList("영등포동", "여의도동", "당산동", "도림동", "문래동", "양평동", "신길동", "대림동"));
        AREA_MAP.put("동작구", Arrays.asList("노량진동", "상도동", "본동", "흑석동", "동작동", "사당동", "대방동", "신대방동"));
        AREA_MAP.put("관악구", Arrays.asList("봉천동", "신림동", "남현동", "서원동", "신원동", "서림동", "삼성동", "미성동", "난곡동", "난향동", "조원동", "대학동"));
        AREA_MAP.put("종로구", Arrays.asList("청운동", "신교동", "궁정동", "효자동", "창성동", "통의동", "적선동", "통인동", "누상동", "누하동", "옥인동", "체부동"));
        AREA_MAP.put("중구", Arrays.asList("소공동", "회현동", "명동", "필동", "장충동", "광희동", "을지로동", "신당동", "다산동", "약수동", "청구동", "신당동", "동화동"));
        AREA_MAP.put("용산구", Arrays.asList("후암동", "용산동", "남영동", "청파동", "원효로동", "효창동", "도원동", "용문동", "한강로동", "이촌동", "이태원동", "한남동"));
        AREA_MAP.put("성동구", Arrays.asList("왕십리동", "마장동", "사근동", "행당동", "응봉동", "금호동", "옥수동", "성수동", "송정동", "용답동"));
        AREA_MAP.put("광진구", Arrays.asList("화양동", "군자동", "중곡동", "능동", "구의동", "광장동", "자양동"));
        AREA_MAP.put("동대문구", Arrays.asList("신설동", "용두동", "제기동", "전농동", "답십리동", "장안동", "청량리동", "회기동", "휘경동", "이문동"));
        AREA_MAP.put("중랑구", Arrays.asList("면목동", "상봉동", "중화동", "묵동", "망우동", "신내동"));
        AREA_MAP.put("성북구", Arrays.asList("성북동", "삼선동", "동선동", "돈암동", "안암동", "보문동", "정릉동", "길음동", "종암동", "월곡동", "장위동", "석관동"));
        AREA_MAP.put("강북구", Arrays.asList("미아동", "번동", "수유동", "우이동"));
        AREA_MAP.put("도봉구", Arrays.asList("쌍문동", "방학동", "창동", "도봉동"));
        AREA_MAP.put("노원구", Arrays.asList("월계동", "공릉동", "하계동", "중계동", "상계동"));
        AREA_MAP.put("은평구", Arrays.asList("녹번동", "불광동", "갈현동", "구산동", "대조동", "응암동", "역촌동", "신사동", "증산동", "수색동"));
        AREA_MAP.put("서대문구", Arrays.asList("충정로동", "천연동", "북아현동", "신촌동", "연희동", "홍제동", "홍은동", "남가좌동", "북가좌동"));
        AREA_MAP.put("마포구", Arrays.asList("아현동", "공덕동", "신공덕동", "도화동", "용강동", "토정동", "마포동", "대흥동", "염리동", "노고산동", "신수동"));
        
        // 인천 주요 지역
        AREA_MAP.put("인천중구", Arrays.asList("연안동", "신생동", "신포동", "답동", "신흥동", "도원동", "율목동", "동인천동", "북성동", "송월동"));
        AREA_MAP.put("인천동구", Arrays.asList("송림동", "송현동", "금창동", "화수동", "만석동", "화평동"));
        AREA_MAP.put("인천미추홀구", Arrays.asList("숭의동", "용현동", "학익동", "도화동", "주안동", "관교동", "문학동"));
        AREA_MAP.put("인천연수구", Arrays.asList("연수동", "청학동", "동춘동", "선학동", "송도동"));
        AREA_MAP.put("인천남동구", Arrays.asList("구월동", "간석동", "만수동", "장수동", "서창동", "운연동", "남촌동"));
        AREA_MAP.put("인천부평구", Arrays.asList("부평동", "산곡동", "청천동", "갈산동", "삼산동", "부개동", "일신동", "구산동"));
        AREA_MAP.put("인천계양구", Arrays.asList("효성동", "계산동", "작전동", "서운동", "임학동", "동양동"));
        AREA_MAP.put("인천서구", Arrays.asList("검암동", "경서동", "연희동", "심곡동", "공촌동", "백석동", "시천동", "검단동", "원당동", "당하동", "원창동"));
        
        // 경기도 주요 지역
        AREA_MAP.put("수원시", Arrays.asList("팔달구", "영통구", "장안구", "권선구", "매탄동", "원천동", "이의동", "하동", "영화동", "광교동"));
        AREA_MAP.put("성남시", Arrays.asList("수정구", "중원구", "분당구", "정자동", "서현동", "이매동", "야탑동", "판교동", "삼평동"));
        AREA_MAP.put("고양시", Arrays.asList("덕양구", "일산동구", "일산서구", "화정동", "행신동", "주엽동", "대화동", "백석동", "마두동"));
        AREA_MAP.put("용인시", Arrays.asList("처인구", "기흥구", "수지구", "죽전동", "보정동", "구갈동", "상현동", "성복동"));
        AREA_MAP.put("부천시", Arrays.asList("원미구", "소사구", "오정구", "중동", "상동", "심곡동", "부천동", "역곡동", "소사동"));
        AREA_MAP.put("안산시", Arrays.asList("상록구", "단원구", "고잔동", "중앙동", "본오동", "사동", "초지동", "선부동"));
        AREA_MAP.put("안양시", Arrays.asList("만안구", "동안구", "평촌동", "호계동", "범계동", "평안동", "비산동", "관양동"));
        AREA_MAP.put("남양주시", Arrays.asList("와부읍", "진접읍", "진건읍", "오남읍", "별내면", "퇴계원면", "다산동", "호평동", "평내동"));
        AREA_MAP.put("화성시", Arrays.asList("동탄", "병점동", "반송동", "기배동", "화산동", "진안동", "봉담읍", "향남읍"));
        AREA_MAP.put("평택시", Arrays.asList("비전동", "평택동", "합정동", "서정동", "송탄동", "지산동", "송북동", "신장동"));
        AREA_MAP.put("의정부시", Arrays.asList("의정부동", "호원동", "장암동", "신곡동", "용현동", "민락동", "낙양동", "금오동"));
        AREA_MAP.put("시흥시", Arrays.asList("대야동", "신천동", "은행동", "매화동", "목감동", "정왕동", "배곧동"));
        AREA_MAP.put("파주시", Arrays.asList("금촌동", "운정동", "교하동", "문산읍", "법원읍", "파주읍"));
        AREA_MAP.put("김포시", Arrays.asList("장기동", "구래동", "마산동", "운양동", "걸포동", "김포본동", "사우동"));
        AREA_MAP.put("광명시", Arrays.asList("광명동", "철산동", "하안동", "소하동", "일직동", "학온동"));
        AREA_MAP.put("광주시", Arrays.asList("송정동", "역동", "경안동", "초월읍", "곤지암읍", "오포읍"));
        AREA_MAP.put("군포시", Arrays.asList("산본동", "금정동", "군포동", "당동", "당정동", "부곡동", "대야미동"));
        AREA_MAP.put("하남시", Arrays.asList("신장동", "덕풍동", "풍산동", "미사동", "감북동", "위례동"));
        AREA_MAP.put("오산시", Arrays.asList("오산동", "부산동", "대원동", "남촌동", "세마동", "초평동"));
        AREA_MAP.put("이천시", Arrays.asList("증포동", "창전동", "중리동", "관고동", "부발읍", "장호원읍"));
        AREA_MAP.put("구리시", Arrays.asList("인창동", "교문동", "수택동", "토평동", "갈매동"));
        AREA_MAP.put("안성시", Arrays.asList("안성동", "대덕면", "미양면", "공도읍", "보개면"));
        AREA_MAP.put("포천시", Arrays.asList("소흘읍", "신북면", "가산면", "일동면", "영중면"));
        AREA_MAP.put("의왕시", Arrays.asList("고천동", "오전동", "내손동", "포일동", "삼동"));
        AREA_MAP.put("양주시", Arrays.asList("양주동", "회천동", "백석읍", "광적면", "남면"));
        AREA_MAP.put("여주시", Arrays.asList("여주읍", "가남읍", "점동면", "세종대왕면"));
        AREA_MAP.put("동두천시", Arrays.asList("생연동", "중앙동", "보산동", "불현동", "송내동"));
        AREA_MAP.put("과천시", Arrays.asList("중앙동", "갈현동", "별양동", "부림동", "과천동", "문원동"));
        AREA_MAP.put("가평군", Arrays.asList("가평읍", "설악면", "청평면", "상면", "조종면", "북면"));
        AREA_MAP.put("양평군", Arrays.asList("양평읍", "강상면", "강하면", "양서면", "옥천면", "서종면"));
        AREA_MAP.put("연천군", Arrays.asList("연천읍", "전곡읍", "군남면", "미산면", "왕징면"));
    }
    
    /**
     * 지역 매칭 확인
     */
    public static boolean isAreaMatch(String targetArea, String address) {
        if (targetArea == null || address == null) {
            return false;
        }
        
        // 정확한 매칭
        for (Map.Entry<String, List<String>> entry : AREA_MAP.entrySet()) {
            String gu = entry.getKey();
            List<String> dongs = entry.getValue();
            
            // 구 단위 매칭
            if (targetArea.equals(gu) && address.contains(gu)) {
                return true;
            }
            
            // 동 단위 매칭
            for (String dong : dongs) {
                if (targetArea.equals(dong) && address.contains(dong)) {
                    return true;
                }
            }
        }
        
        // 부분 매칭 (contains)
        if (address.contains(targetArea)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 지역 코드 가져오기 (테스트용)
     */
    public static int getAreaCode(String sigungu, String dong) {
        // 테스트용 간단한 코드 생성
        int hash = 0;
        if (sigungu != null) {
            hash = sigungu.hashCode() % 10000;
        }
        if (dong != null) {
            hash = (hash * 31 + dong.hashCode()) % 100000;
        }
        return Math.abs(hash);
    }
    
    /**
     * 좌표 가져오기 (테스트용 더미 데이터)
     */
    public static double[] getCoordinates(String area) {
        // 테스트용 더미 좌표 (서울 중심부)
        double baseLat = 37.5665;
        double baseLng = 126.9780;
        
        // 지역명 해시값으로 약간의 변화 추가
        int hash = area.hashCode();
        double latOffset = (hash % 100) * 0.001;
        double lngOffset = ((hash / 100) % 100) * 0.001;
        
        return new double[]{baseLat + latOffset, baseLng + lngOffset};
    }
    
    /**
     * 거리 계산 (km)
     */
    public static double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371; // 지구 반지름 (km)
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;
        
        return distance;
    }
}