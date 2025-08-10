# Media Player - 프로젝트 요약

## 📌 프로젝트 개요
연구 목적의 미디어 플레이어 앱으로, 고급 접근성 기능을 활용한 서비스입니다.

## 🎯 주요 기능

### 1. 품질 설정
- **세밀한 품질 프리셋**: 0.7km, 1km, 1.5km부터 0.5km 단위로 15km까지
- **최고 품질 모드** 지원
- Material Design 3 Chip 컴포넌트로 구현된 직관적인 UI

### 2. 재생 모드
- **전체 재생 모드**: 모든 미디어 자동 재생
- **선택 재생 모드**: 설정된 조건에 따라 선택적 재생

### 3. 프리셋 시스템
- 최대 5개의 설정 프리셋 저장/불러오기
- 빠른 설정 전환 가능
- JSON 형식으로 저장

### 4. 필터링 기능
- **키워드 필터**: 특정 키워드 포함/제외
- **재생 목록 설정**: 선호 항목 설정
- **제외 목록 설정**: 특정 항목 제외

## 📁 프로젝트 구조

```
KaKao_Auto_Enhanced/
├── app/
│   ├── build.gradle
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/com/media/player/service/
│           │   ├── PlayerActivity.java
│           │   ├── MediaService.java
│           │   ├── worker/
│           │   │   ├── WorkerThread.java
│           │   │   └── ListWorker.java
│           │   ├── utils/
│           │   │   ├── MediaItem.java
│           │   │   ├── Config.java
│           │   │   ├── DataStore.java
│           │   │   ├── Helper.java
│           │   │   ├── Preset.java
│           │   │   └── Logger.java
│           │   └── model/
│           │       └── ItemModel.java
│           └── res/
│               ├── layout/
│               │   ├── activity_player.xml
│               │   └── dialog_save_preset.xml
│               ├── values/
│               │   ├── colors.xml
│               │   ├── strings.xml
│               │   └── themes.xml
│               └── xml/
│                   └── media_service_config.xml
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## 🎨 UI/UX 특징

### Material Design 3
- 파란색 기반 테마 (#2196F3)
- CardView로 구성된 섹션별 레이아웃
- 부드러운 애니메이션과 전환 효과
- 다크 모드 지원 준비

### 접근성
- 큰 터치 타겟 (최소 48dp)
- 명확한 레이블과 설명
- 고대비 색상 조합

## 🔧 기술 스택

- **언어**: Java
- **최소 SDK**: 24 (Android 7.0)
- **타겟 SDK**: 34 (Android 14)
- **주요 라이브러리**:
  - AndroidX
  - Material Components
  - Room Database
  - Coroutines (Java 호환)

## 📱 필요 권한

- `BIND_ACCESSIBILITY_SERVICE` - 접근성 서비스
- `SYSTEM_ALERT_WINDOW` - 오버레이 표시
- `ACCESS_FINE_LOCATION` - 위치 정보
- `FOREGROUND_SERVICE` - 포그라운드 서비스
- `INTERNET` - 네트워크 통신

## 🚀 실행 방법

1. Android Studio에서 프로젝트 열기
2. `local.properties`에 SDK 경로 설정
3. 빌드 및 실행
4. 접근성 서비스 활성화
5. 필요한 권한 승인

## 📝 설정 가이드

### 1. 품질 설정
- 앱 실행 후 품질 칩 선택
- 0.7km부터 15km까지 또는 최고 품질 선택

### 2. 재생 모드
- 전체 재생: 모든 항목 자동 처리
- 선택 재생: 필터 조건에 따라 처리

### 3. 프리셋 관리
- 현재 설정을 프리셋으로 저장
- 저장된 프리셋 빠르게 불러오기

## ⚙️ 고급 설정

### 필터 설정
- 키워드: 쉼표로 구분된 키워드 입력
- 재생 목록: 선호하는 항목 설정
- 제외 목록: 제외할 항목 설정

### 추가 옵션
- 자동 건너뛰기: 조건 미충족시 자동 스킵
- 볼륨 컨트롤: 볼륨 버튼으로 서비스 제어

## 📊 모니터링

- 실시간 서비스 상태 표시
- 처리된 항목 로그
- 성능 통계

## 🔐 보안

- 난독화 적용 가능
- ProGuard 규칙 포함
- 민감한 데이터 암호화

## 📞 지원

연구 목적 관련 문의는 프로젝트 이슈 트래커를 통해 진행해주세요.

## 📄 라이선스

연구 목적으로만 사용 가능합니다.