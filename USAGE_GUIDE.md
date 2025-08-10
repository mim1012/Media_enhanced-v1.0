# Media Player - 사용법 및 작동원리

## 📖 사용법 가이드

### 1. 초기 설정

#### 1.1 앱 설치
1. APK 파일을 기기에 설치
2. 앱 실행 후 필요한 권한 승인
   - 위치 권한
   - 오버레이 권한
   - 알림 권한

#### 1.2 접근성 서비스 활성화
1. 설정 → 접근성 → Media Player 선택
2. 서비스 활성화 토글 ON
3. "이 앱이 화면 내용을 읽을 수 있습니다" 경고 확인

#### 1.3 기본 설정
1. 앱 메인 화면에서 품질(거리) 설정
   - 0.7km, 1km, 1.5km ~ 15km, 최고품질(무제한)
2. 재생 모드 선택
   - 전체 재생: 모든 항목 자동 처리
   - 선택 재생: 조건에 맞는 항목만 처리

### 2. 고급 설정

#### 2.1 필터 설정
```
키워드 필터: 강남, 서초, 공항
(쉼표로 구분하여 입력)
```

#### 2.2 재생/제외 목록
- 재생 목록: 선호하는 도착지 설정
- 제외 목록: 제외할 도착지 설정

#### 2.3 프리셋 사용
1. 현재 설정을 프리셋으로 저장
2. 상황에 따라 프리셋 빠르게 전환
   - 출근 시간대 프리셋
   - 야간 프리셋
   - 주말 프리셋

### 3. 서비스 작동

#### 3.1 서비스 시작
1. 메인 화면에서 "재생 시작" 버튼 탭
2. 상태바에 "재생 중" 표시 확인
3. KakaoT 드라이버 앱 실행

#### 3.2 볼륨 버튼 제어 (선택사항)
- 볼륨 UP: 서비스 활성화
- 볼륨 DOWN: 서비스 비활성화

#### 3.3 서비스 중지
- 앱에서 "재생 중지" 버튼 탭
- 또는 알림창에서 중지

## ⚙️ 작동 원리

### 1. 핵심 구조

```
[KakaoT Driver App]
        ↓
[AccessibilityEvent 감지]
        ↓
[MediaService (접근성 서비스)]
        ↓
[UI 요소 분석]
        ↓
[조건 판단 로직]
        ↓
[자동 클릭 실행]
```

### 2. 상세 작동 과정

#### 2.1 이벤트 감지
```java
// AccessibilityService가 화면 변화 감지
onAccessibilityEvent(AccessibilityEvent event) {
    // KakaoT 드라이버 앱 패키지 확인
    if (packageName.equals("com.kakao.taxi.driver")) {
        processMedia(rootNode);
    }
}
```

#### 2.2 UI 요소 검색
```java
// View ID로 특정 UI 요소 찾기
List<AccessibilityNodeInfo> nodes = 
    Helper.getNodeListByViewId(root, "v_arrow");  // 개별 콜
    Helper.getNodeListByViewId(root, "lv_call_list");  // 콜 목록
```

#### 2.3 정보 추출
```java
// 콜 정보 파싱
MediaItem item = new MediaItem();
item.mQuality = getTextFromNode("tv_origin_label_distance");  // 거리
item.mSource = getTextFromNode("tv_origin");  // 출발지
item.mTarget = getTextFromNode("tv_destination");  // 도착지
```

#### 2.4 조건 판단 알고리즘

```java
boolean shouldAccept(MediaItem item) {
    // 1. 전체 모드인 경우
    if (DataStore.bFullMode) {
        return true;  // 무조건 수락
    }
    
    // 2. 거리 체크
    int distance = parseDistance(item.mQuality);
    if (distance > DataStore.nQuality && DataStore.nQuality != 0) {
        return false;  // 거리 초과
    }
    
    // 3. 키워드 필터
    for (String keyword : DataStore.aFilterList) {
        if (item.mTarget.contains(keyword)) {
            return false;  // 키워드 매칭시 거절
        }
    }
    
    // 4. 도착지 체크
    if (isInPlaylist(item.mTarget)) {
        return true;  // 선호 도착지
    }
    
    if (isInExclusionList(item.mTarget)) {
        return false;  // 제외 도착지
    }
    
    return true;  // 기본값
}
```

#### 2.5 자동 클릭 실행
```java
// 버튼 클릭 수행
if (shouldAccept) {
    AccessibilityNodeInfo acceptBtn = findNode("btn_accept");
    acceptBtn.performAction(ACTION_CLICK);
} else if (DataStore.bAutoSkip) {
    AccessibilityNodeInfo denyBtn = findNode("btn_deny");
    denyBtn.performAction(ACTION_CLICK);
}
```

### 3. 멀티스레딩 처리

```java
// 30개 스레드 풀로 동시 처리
ExecutorService threadPool = Executors.newFixedThreadPool(30);

// 개별 콜 처리
threadPool.submit(new WorkerThread(mediaItem, service));

// 콜 목록 처리
threadPool.submit(new ListWorker(listNode, service));
```

### 4. 데이터 저장 구조

#### 4.1 SharedPreferences
```java
// 설정값 저장
preferences.putInt("quality", 1000);  // 1km
preferences.putBoolean("fullMode", false);
preferences.putString("filters", "강남,서초");
```

#### 4.2 파일 저장
```
/data/data/com.media.player.service/files/
├── playlist.txt      // 재생 목록
├── exclusion.txt     // 제외 목록
└── presets.json      // 프리셋 데이터
```

### 5. 거리 계산 로직

```java
// 거리 파싱 및 변환
int parseDistance(String distanceText) {
    if (distanceText.contains("km")) {
        // "1.5km" → 1500m
        float km = Float.parseFloat(
            distanceText.replace("km", "").trim()
        );
        return (int)(km * 1000);
    } else if (distanceText.contains("m")) {
        // "500m" → 500m
        return Integer.parseInt(
            distanceText.replace("m", "").trim()
        );
    }
    return 0;  // 파싱 실패
}
```

## 🔒 서버 인증 (현재 미구현)

### 향후 구현 예정 사항:

#### 1. 라이선스 인증
```java
// 서버 인증 API 호출
String deviceId = getDeviceId();
String response = HttpUtils.post(SERVER_URL + "/auth", {
    "device_id": deviceId,
    "package": getPackageName()
});

if (response.status == 200) {
    // 인증 성공 - 만료일 저장
    saveExpireDate(response.expireDate);
} else {
    // 인증 실패 - 서비스 비활성화
    disableService();
}
```

#### 2. 주기적 검증
```java
// 24시간마다 라이선스 체크
ScheduledExecutorService scheduler = 
    Executors.newScheduledThreadPool(1);
    
scheduler.scheduleAtFixedRate(() -> {
    checkLicense();
}, 0, 24, TimeUnit.HOURS);
```

#### 3. 데이터베이스 업데이트
```java
// 지역 정보 DB 다운로드
if (serverVersion > localVersion) {
    downloadDatabase(DB_URL);
    updateLocalDatabase();
}
```

## 📊 성능 최적화

### 1. 이벤트 필터링
- 불필요한 이벤트 무시
- 특정 패키지만 처리

### 2. 메모리 관리
- AccessibilityNodeInfo 재활용
- 처리 완료 후 즉시 recycle()

### 3. 배터리 최적화
- Doze 모드 예외 처리
- WakeLock 최소 사용

## ⚠️ 주의사항

1. **접근성 서비스 권한**
   - 화면 내용을 읽을 수 있는 강력한 권한
   - 신뢰할 수 있는 앱만 허용

2. **배터리 소모**
   - 백그라운드 실행으로 배터리 소모 증가
   - 필요시에만 서비스 활성화

3. **개인정보 보호**
   - 위치 정보 수집
   - 화면 내용 접근

## 🔧 문제 해결

### 서비스가 작동하지 않을 때
1. 접근성 서비스 활성화 확인
2. 앱 권한 모두 허용 확인
3. 배터리 최적화 예외 설정
4. 기기 재시작 후 재시도

### 자동 클릭이 안 될 때
1. KakaoT 앱 버전 확인
2. View ID 변경 여부 확인
3. 로그 확인 후 디버깅

### 거리 필터가 작동하지 않을 때
1. 품질 설정 확인
2. 전체/선택 모드 확인
3. 거리 단위 파싱 오류 체크