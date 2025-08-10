@echo off
echo ===============================================
echo Installing Media Enhanced v1.0
echo ===============================================
echo.

set APK_PATH=app\build\outputs\apk\debug\app-debug.apk

if not exist "%APK_PATH%" (
    echo ERROR: APK file not found!
    echo Expected location: %APK_PATH%
    echo Please build the project first using build_now.bat
    pause
    exit /b 1
)

echo Checking ADB connection...
adb devices
echo.

echo Installing APK...
adb install -r "%APK_PATH%"

if errorlevel 1 (
    echo.
    echo Installation failed! Please check:
    echo - USB debugging is enabled on your device
    echo - Device is properly connected
    echo - You approved the USB debugging prompt on device
) else (
    echo.
    echo ===============================================
    echo Installation Successful!
    echo ===============================================
    echo App Name: Media Player Service
    echo.
    echo Next steps:
    echo 1. Go to Settings - Accessibility
    echo 2. Enable "Media Player Service"
    echo 3. Open the app and configure settings
)

echo.
pause