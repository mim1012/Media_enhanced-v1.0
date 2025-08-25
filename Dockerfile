# Node.js 서버만 배포
FROM node:18-alpine

# 작업 디렉토리 설정
WORKDIR /app

# 서버 디렉토리의 package.json 복사
COPY server/package*.json ./

# 의존성 설치
RUN npm ci --only=production

# 서버 코드 복사
COPY server/ ./

# 포트 노출
EXPOSE 8080

# 환경변수 설정
ENV NODE_ENV=production
ENV PORT=8080

# 서버 실행
CMD ["node", "server.js"]