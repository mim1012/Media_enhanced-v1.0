# 🔧 Media Enhanced 디버깅 명령어 가이드

## 📱 ADB 기본 명령어

### 1. 디바이스 연결 확인
```bash
# 연결된 디바이스 목록
adb devices

# 무선 ADB 연결 (같은 WiFi 필요)
adb connect 192.168.x.x:5555
```

### 2. APK 설치/제거
```bash
# APK 설치
adb install -r app\build\outputs\apk\debug\app-debug.apk

# 앱 제거
adb uninstall com.media.player.service

# 데이터 유지하며 재설치
adb install -r -g app-debug.apk
```

## 🐛 디버깅 명령어

### 1. Logcat - 실시간 로그 확인
```bash
# 모든 로그 보기
adb logcat

# Media Enhanced 앱 로그만 보기
adb logcat | findstr "com.media.player.service"

# 카카오T 콜 감지 디버그 로그
adb logcat | findstr "DEBUG_KAKAO"

# AccessibilityService 로그
adb logcat | findstr "AccessibilityService"

# 로그 레벨별 필터링
adb logcat *:E  # Error만
adb logcat *:W  # Warning 이상
adb logcat MediaPlayer:V *:S  # MediaPlayer 태그만 Verbose

# 로그 파일로 저장
adb logcat > debug_log.txt
```

### 2. 접근성 서비스 상태 확인
```bash
# 활성화된 접근성 서비스 목록
adb shell settings get secure enabled_accessibility_services

# Media Enhanced 서비스 활성화 확인
adb shell settings get secure enabled_accessibility_services | findstr "media.player"

# 접근성 서비스 강제 활성화 (루트 필요)
adb shell settings put secure enabled_accessibility_services com.media.player.service/.MediaService

# 접근성 서비스 재시작
adb shell am force-stop com.media.player.service
adb shell am start -n com.media.player.service/.MainActivity
```

### 3. 앱 데이터 및 SharedPreferences 확인
```bash
# SharedPreferences 위치 확인
adb shell ls /data/data/com.media.player.service/shared_prefs/

# SharedPreferences 내용 보기 (루트 필요)
adb shell cat /data/data/com.media.player.service/shared_prefs/media_pref.xml

# 앱 데이터 초기화
adb shell pm clear com.media.player.service
```

### 4. 실시간 UI 분석
```bash
# UI Automator Viewer 실행 (Android SDK 필요)
uiautomatorviewer

# 현재 화면 덤프
adb shell uiautomator dump /sdcard/window_dump.xml
adb pull /sdcard/window_dump.xml

# 스크린샷 캡처
adb shell screencap /sdcard/screen.png
adb pull /sdcard/screen.png
```

## 🎯 카카오T 드라이버 앱 디버깅

### 1. 카카오T 패키지 정보
```bash
# 패키지 정보 확인
adb shell dumpsys package com.kakao.taxi.driver

# 실행 중인 액티비티 확인
adb shell dumpsys activity activities | findstr "kakao.taxi"

# 카카오T 앱 강제 종료
adb shell am force-stop com.kakao.taxi.driver

# 카카오T 앱 실행
adb shell am start -n com.kakao.taxi.driver/.MainActivity
```

### 2. 이벤트 시뮬레이션
```bash
# 화면 탭 시뮬레이션
adb shell input tap 500 1000

# 스와이프 시뮬레이션
adb shell input swipe 100 500 100 1000

# 텍스트 입력
adb shell input text "테스트"

# 백 버튼
adb shell input keyevent 4
```

## 🔍 성능 모니터링

### 1. CPU 사용량
```bash
adb shell top | findstr "media.player"
```

### 2. 메모리 사용량
```bash
adb shell dumpsys meminfo com.media.player.service
```

### 3. 배터리 사용량
```bash
adb shell dumpsys batterystats | findstr "media.player"
```

## 📊 데이터베이스 디버깅

### 1. SQLite 데이터베이스 확인
```bash
# DB 파일 위치
adb shell ls /data/data/com.media.player.service/databases/

# DB 파일 추출 (루트 필요)
adb pull /data/data/com.media.player.service/databases/media.db

# SQLite 쿼리 실행
adb shell sqlite3 /data/data/com.media.player.service/databases/media.db "SELECT * FROM presets;"
```

## 🚀 빠른 디버깅 체크리스트

1. **콜 감지 안될 때**
```bash
# 1. 접근성 서비스 확인
adb shell settings get secure enabled_accessibility_services

# 2. 실시간 로그 확인
adb logcat | findstr "v_arrow"

# 3. UI 덤프로 뷰 확인
adb shell uiautomator dump
```

2. **설정값 저장 안될 때**
```bash
# SharedPreferences 확인
adb shell run-as com.media.player.service cat shared_prefs/media_pref.xml
```

3. **앱이 크래시할 때**
```bash
# 크래시 로그 확인
adb logcat | findstr "FATAL EXCEPTION"
```

## 💡 유용한 팁

- `Ctrl+C`: logcat 중지
- `adb logcat -c`: 로그 버퍼 클리어
- `adb logcat -d`: 현재까지의 로그만 출력하고 종료
- `adb logcat -t 100`: 최근 100줄만 출력

## 🔐 개발자 옵션 활성화

1. 설정 → 휴대전화 정보 → 빌드 번호 7번 탭
2. 설정 → 개발자 옵션 → USB 디버깅 ON
3. 설정 → 개발자 옵션 → 무선 디버깅 ON (Android 11+)