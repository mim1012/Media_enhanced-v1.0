# 🔍 Media Enhanced 서비스 동작 테스트 가이드

## 📊 현재 서비스 상태 구조

### **3단계 제어 시스템**
1. **접근성 서비스** (MediaService) - 시스템 레벨
2. **DataStore.bEnabled** - 자동 수락 ON/OFF 플래그
3. **UI 상태 표시** - ViewModel의 isServiceRunning

### **동작 흐름**
```
접근성 서비스 활성화 (설정에서 켜기)
         ↓
앱 실행 (MainActivity)
         ↓
[시작 버튼] → DataStore.bEnabled = true
         ↓
카카오T 앱 실행
         ↓
콜 감지 시작 (MediaService.onAccessibilityEvent)
         ↓
DataStore.bEnabled 체크
         ↓
true면 자동 수락 처리
```

## 🧪 테스트 방법

### **1. 서비스 상태 확인**
```bash
# 접근성 서비스 활성화 확인
adb shell settings get secure enabled_accessibility_services | findstr media.player

# 실행 결과 예시:
# com.media.player.service/com.media.player.service.MediaService
```

### **2. 실시간 동작 로그**
```bash
# 로그 초기화 후 실시간 모니터링
adb logcat -c
adb logcat | findstr "MediaService|DataStore.bEnabled|v_arrow|수락"
```

### **3. 디버그 로그 확인**
```bash
# DebugMediaService 로그 (상세 정보)
adb logcat | findstr "DEBUG_KAKAO"
```

## 🎮 버튼 동작 설명

### **🚀 시작 버튼**
- `DataStore.bEnabled = true` 설정
- 카카오T 드라이버 앱 자동 실행
- UI 상태: "서비스 실행 중" 표시
- **효과**: 이제부터 콜이 오면 자동 처리

### **⛔ 중지 버튼**
- `DataStore.bEnabled = false` 설정
- UI 상태: "서비스 중지됨" 표시
- **효과**: 콜이 와도 자동 처리 안함
- **주의**: 접근성 서비스는 계속 실행 중 (감지만 하고 액션 안함)

## 📋 체크리스트

### **초기 설정**
- [ ] 설정 → 접근성 → Media Player Service 켜기
- [ ] 카카오T 드라이버 앱 설치됨
- [ ] USB 디버깅 활성화 (로그 확인용)

### **동작 테스트**
1. [ ] 앱 실행 → UI에 "서비스 중지됨" 표시
2. [ ] 시작 버튼 클릭 → "서비스 실행 중" 변경
3. [ ] 카카오T 자동 실행 확인
4. [ ] 콜 수신 시 자동 감지 (로그 확인)
5. [ ] 중지 버튼 클릭 → "서비스 중지됨" 변경
6. [ ] 콜 와도 자동 처리 안함 확인

## 🔧 문제 해결

### **"서비스 중지됨"이 계속 표시될 때**
```bash
# DataStore.bEnabled 값 직접 확인
adb shell "run-as com.media.player.service cat shared_prefs/media_pref.xml" | grep enabled
```

### **콜이 감지되지 않을 때**
```bash
# 카카오T 앱 패키지 확인
adb shell pm list packages | grep kakao

# AccessibilityEvent 로그
adb logcat | grep "onAccessibilityEvent"
```

### **자동 클릭이 안될 때**
```bash
# 버튼 감지 로그
adb logcat | grep "수락|직접결제|자동결제"

# Helper.delegateButtonClick 로그
adb logcat | grep "Button clicked"
```

## 📱 실제 테스트 시나리오

### **시나리오 1: 전체콜 모드**
1. 거리: 무제한
2. 모드: 전체콜
3. 시작 버튼 클릭
4. 콜 수신 → 자동 수락 확인

### **시나리오 2: 거리 제한**
1. 거리: 3km
2. 모드: 전체콜
3. 시작 버튼 클릭
4. 3km 이내 콜 → 자동 수락
5. 3km 초과 콜 → 자동 거절

### **시나리오 3: ON/OFF 테스트**
1. 시작 버튼 → 콜 자동 처리
2. 중지 버튼 → 콜 무시
3. 다시 시작 → 콜 자동 처리

## 💡 Reference(Cypher)와 차이점

| 항목 | Cypher | Media Enhanced |
|------|--------|----------------|
| 제어 변수 | SharedData.bAuto | DataStore.bEnabled |
| 기본값 | false | true |
| 서버 연동 | 있음 | 없음 (로컬) |
| 응답 속도 | 200ms | 10ms |
| 인증 | 전화번호 | 없음 |