# 🎵 Media Enhanced Auth Server

## 📋 개요
Media Enhanced 앱용 휴대폰 번호 기반 인증 및 사용자 관리 서버

## 🚀 설치 및 실행

### 1. 의존성 설치
```bash
cd server
npm install
```

### 2. MongoDB 설치 및 실행
```bash
# Windows (MongoDB Community Edition)
mongod --dbpath C:\data\db

# 또는 MongoDB Compass 사용
```

### 3. 환경 변수 설정
`.env` 파일에서 다음 설정:
- `ADMIN_PASSWORD`: 관리자 비밀번호
- `MONGODB_URI`: MongoDB 연결 URL
- `PORT`: 서버 포트 (기본 3000)

### 4. 서버 실행
```bash
npm start
# 또는 개발 모드
npm run dev
```

## 📱 API 엔드포인트

### 인증 API
- `POST /api/auth/verify` - 앱 인증 확인
- `POST /api/auth/stats` - 사용 통계 전송

### 관리자 API
- `POST /admin/register` - 신규 사용자 등록
- `GET /admin/users` - 사용자 목록 조회
- `POST /admin/extend` - 사용자 기간 연장
- `POST /admin/block` - 사용자 차단/해제
- `DELETE /admin/user/:id` - 사용자 삭제

## 🎛️ 관리자 대시보드

### 접속 방법
```
http://localhost:3000/dashboard.html
```

### 기능
1. **신규 사용자 등록**
   - 휴대폰 번호: 010-1234-5678
   - 유형: 체험(7일) / 프리미엄(90일) / 사용자정의
   - 메모: 사용자 식별용

2. **사용자 관리**
   - 실시간 목록 확인
   - 기간 연장
   - 차단/해제
   - 삭제

3. **통계 모니터링**
   - 총 등록 사용자
   - 활성 사용자
   - 만료된 사용자
   - 차단된 사용자

## 🔐 보안 설정

### 관리자 인증
```javascript
// 요청 헤더에 비밀번호 포함
headers: {
    'Authorization': 'your_secure_admin_password_here'
}
```

### 앱 인증 플로우
```
앱 실행 → 휴대폰 번호 추출 → 서버 인증 → 성공/실패 처리
```

## 📊 데이터베이스 스키마

### User Collection
```javascript
{
    phone_number: "010-1234-5678",    // 휴대폰 번호 (고유키)
    device_id: "samsung_s21_abc123",  // 기기 식별자
    type: "premium",                  // "trial" 또는 "premium"
    registered_at: Date,              // 등록일
    expires_at: Date,                 // 만료일
    status: "active",                 // "active" 또는 "blocked"
    memo: "강남 택시기사 김OO",       // 관리자 메모
    last_auth: Date,                  // 마지막 인증 시간
    total_auths: 15,                  // 총 인증 횟수
    stats: {                          // 사용 통계
        total_calls: 1247,
        accepted_calls: 1089,
        last_active: Date,
        regions: ["강남구", "서초구"],
        avg_distance: 3.2
    }
}
```

## 🎯 사용법

### 1. 서버 실행
```bash
npm start
```

### 2. 관리자 페이지 접속
```
http://localhost:3000/dashboard.html
```

### 3. 사용자 등록
- 휴대폰 번호와 기간 입력
- 등록 완료

### 4. 앱 인증
- 등록된 번호의 앱 → 자동 인증 성공
- 미등록 번호의 앱 → 접근 차단

## 🔧 배포 (선택)

### Heroku 배포
```bash
heroku create media-enhanced-server
heroku addons:create mongolab:sandbox
git push heroku main
```

### AWS EC2 배포
```bash
# PM2 사용 권장
npm install -g pm2
pm2 start server.js --name "media-auth"
```