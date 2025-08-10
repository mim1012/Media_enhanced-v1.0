@echo off
echo ===================================
echo Media Enhanced v1.0 빌드 및 설치
echo ===================================
echo.

echo [1/4] 프로젝트 클린...
call gradlew clean
if errorlevel 1 goto error

echo.
echo [2/4] Debug APK 빌드 중...
call gradlew assembleDebug
if errorlevel 1 goto error

echo.
echo [3/4] APK 빌드 완료!
echo APK 위치: app\build\outputs\apk\debug\app-debug.apk

echo.
echo [4/4] 디바이스에 설치 중...
adb install -r app\build\outputs\apk\debug\app-debug.apk
if errorlevel 1 goto install_error

echo.
echo ===================================
echo ✓ 빌드 및 설치 완료!
echo ===================================
goto end

:error
echo.
echo ❌ 빌드 실패! 
echo Gradle 또는 JDK 설정을 확인하세요.
goto end

:install_error
echo.
echo ⚠️ 설치 실패!
echo ADB 연결 상태를 확인하세요: adb devices
goto end

:end
pause