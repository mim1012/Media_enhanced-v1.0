# KaKao Auto Enhanced

KaKao Taxi 자동화 앱 - 향상된 기능을 제공하는 Android 애플리케이션

## 기능

- 카카오택시 호출 요청 자동 접수
- 위치 기반 서비스
- 접근성 서비스를 통한 자동화
- 백그라운드 서비스 실행
- 부팅 시 자동 시작
- 화면 상태 모니터링

## 필요한 권한

- `BIND_ACCESSIBILITY_SERVICE`: 접근성 서비스 바인딩
- `SYSTEM_ALERT_WINDOW`: 화면 위에 그리기
- `ACCESS_FINE_LOCATION`: 정확한 위치 정보
- `INTERNET`: 네트워크 연결
- `FOREGROUND_SERVICE`: 포그라운드 서비스 실행

## 시스템 요구사항

- Android API 24 (Android 7.0) 이상
- 대상 API: 34 (Android 14)

## 빌드 방법

1. Android Studio에서 프로젝트 열기
2. Gradle 동기화 완료 대기
3. 빌드 및 실행

```bash
./gradlew assembleDebug
```

## 설치 방법

1. APK 파일을 Android 기기에 복사
2. 설정 > 보안 > 알 수 없는 소스 허용
3. APK 파일 실행하여 설치

## 사용법

1. 앱 실행
2. 필요한 권한들 허용
3. 접근성 서비스 활성화
4. 자동화 서비스 시작
