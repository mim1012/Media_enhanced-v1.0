# 📱 Media Enhanced v1.0 - 최종 테스트 가이드

## 🔧 수정된 핵심 사항

### 1. **DataStore.nMode 초기화 문제 해결** ✅
- **문제**: WorkerThread가 nMode 값이 없어서 아무 동작도 하지 않음
- **해결**: 
  - MainViewModel.loadSettings()에서 초기화
  - MainActivity 시작 버튼 클릭 시 현재 모드에 따른 nMode 설정
  - updateCallMode()에서 모드 변경 시 nMode 업데이트

### 2. **XML 설정 Reference와 완전 동기화** ✅
- `notificationTimeout`: ~~10ms~~ → **200ms** (Reference와 동일)
- `accessibilityFeedbackType`: ~~feedbackGeneric~~ → **feedbackAllMask**
- `packageNames`: 제거 (모든 앱에서 동작)
- `accessibility_service_description`: "꼭 사용함으로 설정해주세요"

### 3. **디버그 로그 추가** ✅
```java
// WorkerThread.java
Logger.log("[WorkerThread] 현재 모드: " + DataStore.nMode);
Logger.log("[WorkerThread] 전체콜 모드 - 자동 수락 버튼 클릭!");
```

## 📋 빌드 및 설치

### Windows 환경
```bash
# 1. 프로젝트 디렉토리로 이동
cd C:\Users\samsung\Downloads\Media_enhanced-v1.0

# 2. 빌드 실행
gradlew.bat assembleDebug

# 3. APK 설치
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### 또는 배치 파일 사용
```bash
# 빌드 + 설치 한번에
build_and_install.bat
```

## 🧪 테스트 시나리오

### 시나리오 1: 전체콜 모드 테스트
1. **설정**
   - 앱 실행 → "전체콜 모드" 선택
   - 거리: 무제한 또는 원하는 값
   
2. **실행**
   - 시작 버튼 클릭
   - 토스트 메시지 확인: "자동 수락 시작 ✅ [모드: 전체콜]"
   
3. **로그 확인**
   ```bash
   adb logcat -c
   adb logcat | findstr "WorkerThread|nMode|수락"
   ```
   
4. **예상 결과**
   - `[WorkerThread] 현재 모드: 768 (ALL=768, DEST=256, NONE=0)`
   - `[WorkerThread] 전체콜 모드 - 자동 수락 버튼 클릭!`
   - 카카오T 앱에서 콜 자동 수락

### 시나리오 2: 거리 제한 테스트
1. **설정**
   - 거리: 3km 설정
   - 전체콜 모드
   
2. **테스트**
   - 3km 이내 콜 → 자동 수락
   - 3km 초과 콜 → 자동 거절
   
3. **로그 확인**
   ```bash
   adb logcat | findstr "거리 체크"
   ```

### 시나리오 3: 서비스 ON/OFF 테스트
1. **시작 버튼** → DataStore.bEnabled = true
2. **중지 버튼** → DataStore.bEnabled = false
3. 각 상태에서 콜 수신 시 동작 확인

## 🔍 실시간 모니터링

### 접근성 서비스 상태 확인
```bash
# 서비스 활성화 여부
adb shell settings get secure enabled_accessibility_services | findstr media.player

# DataStore.bEnabled 값 확인
adb shell "run-as com.media.player.service cat shared_prefs/media_pref.xml" | grep enabled
```

### 실시간 로그 모니터링
```bash
# 전체 동작 로그
adb logcat | findstr "MediaService|WorkerThread|Helper|DataStore"

# 콜 감지 로그
adb logcat | findstr "v_arrow|lv_call_list|수락|직접결제|자동결제"

# 모드 관련 로그
adb logcat | findstr "nMode|MODE_ALL|MODE_DEST|MODE_NONE"
```

## ✅ 체크리스트

### 초기 설정
- [ ] 설정 → 접근성 → Media Player Service 켜기
- [ ] 카카오T 드라이버 앱 설치 및 로그인
- [ ] USB 디버깅 활성화

### 기능 테스트
- [ ] 앱 실행 시 서비스 상태 표시 정상
- [ ] 모드 선택 (전체콜/선택콜/대기) 정상 동작
- [ ] 시작 버튼 → 카카오T 자동 실행
- [ ] 콜 수신 시 자동 감지 (로그 확인)
- [ ] 콜 자동 수락 동작 확인
- [ ] 중지 버튼 → 자동 수락 중지

## 🚨 문제 해결

### "콜이 감지되지 않음"
1. 접근성 서비스 확인
2. 카카오T 앱이 포그라운드에 있는지 확인
3. `adb logcat | grep onAccessibilityEvent` 로 이벤트 수신 확인

### "자동 클릭이 안됨"
1. DataStore.nMode 값 확인
   ```bash
   adb logcat | grep "현재 모드:"
   ```
2. 버튼 감지 확인
   ```bash
   adb logcat | grep "mPlayCtrl"
   ```

### "서비스 상태가 계속 중지됨"
1. 접근성 서비스 설정 재확인
2. 앱 재시작 후 다시 시도

## 📊 Reference(Cypher)와 주요 차이점

| 항목 | Cypher v2.3.0 | Media Enhanced v1.0 |
|------|---------------|---------------------|
| 응답 시간 | 200ms | 200ms (수정됨) |
| 피드백 타입 | feedbackAllMask | feedbackAllMask (수정됨) |
| 패키지 제한 | 없음 | 없음 (수정됨) |
| 서버 연동 | 있음 | 없음 (로컬 전용) |
| 인증 | 전화번호 | 없음 |
| 제어 변수 | SharedData.bAuto | DataStore.bEnabled |
| 모드 변수 | SharedData.nFilter | DataStore.nMode |

## 📝 최종 확인 사항

1. **DataStore.nMode 초기화**: ✅ 해결됨
2. **XML 설정 동기화**: ✅ 완료
3. **로컬 전용 동작**: ✅ 서버 코드 제거
4. **디버그 로그**: ✅ 추가됨

---

**작성일**: 2025-08-10
**버전**: v1.0 Final
**상태**: 테스트 준비 완료