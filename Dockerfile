# Android Gradle 빌드를 위한 베이스 이미지
FROM openjdk:17-jdk

# 작업 디렉토리 설정
WORKDIR /app

# Android SDK 설정
ENV ANDROID_HOME=/opt/android-sdk
ENV PATH=${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools

# Android SDK 설치
RUN apt-get update && apt-get install -y wget unzip && \
    mkdir -p ${ANDROID_HOME}/cmdline-tools && \
    cd ${ANDROID_HOME}/cmdline-tools && \
    wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip && \
    unzip commandlinetools-linux-9477386_latest.zip && \
    mv cmdline-tools latest && \
    rm commandlinetools-linux-9477386_latest.zip

# Android SDK components 설치
RUN yes | sdkmanager --licenses && \
    sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

# 프로젝트 파일 복사
COPY . /app/.

# Gradlew 실행 권한 부여
RUN chmod +x gradlew

# 빌드 실행
RUN ./gradlew clean build -x check -x test

# APK 파일을 실행 가능한 형태로 노출
EXPOSE 8080

# 서버 모드로 실행 (서버 디렉토리가 있는 경우)
CMD ["sh", "-c", "if [ -d server ]; then cd server && npm install && npm start; else echo 'APK build completed'; fi"]