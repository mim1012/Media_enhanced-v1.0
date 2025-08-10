@echo off
echo ===============================================
echo Media Enhanced v1.0 - Local Build
echo ===============================================
echo.

cd /d "C:\Users\samsung\Downloads\Media_enhanced-v1.0"

echo [1/3] Cleaning previous build...
call gradlew.bat clean
if errorlevel 1 (
    echo Error during clean! Check your Gradle setup.
    pause
    exit /b 1
)

echo.
echo [2/3] Building Debug APK...
call gradlew.bat assembleDebug
if errorlevel 1 (
    echo Build failed! Check the error messages above.
    pause
    exit /b 1
)

echo.
echo [3/3] Build Complete!
echo ===============================================
echo APK Location:
echo C:\Users\samsung\Downloads\Media_enhanced-v1.0\app\build\outputs\apk\debug\app-debug.apk
echo ===============================================
echo.

echo Press any key to exit...
pause > nul