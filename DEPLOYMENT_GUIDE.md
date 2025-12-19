# OfflineNavApp v1.0 - Deployment Guide

## Build Status: ✅ SUCCESS

The OfflineNavApp has been successfully built! The APK file is ready for deployment.

## APK Location

**Debug APK:** [`app/build/outputs/apk/debug/app-debug.apk`](app/build/outputs/apk/debug/app-debug.apk)

## App Information

- **Package Name:** com.example.offlinenav
- **Version:** 1.0 (versionCode 1)
- **Min SDK:** Android 5.0 (API 21)
- **Target SDK:** Android 13 (API 33)
- **Build Type:** Debug (unsigned)

## Features

1. **Map Navigation** - Online map using OSMDroid (OpenStreetMap)
2. **Support Call System** - Emergency calling with call history
3. **SQLite Database** - Local storage for call records
4. **Permissions:**
   - Internet access
   - Phone call capability
   - External storage (read/write)

## Installation Methods

### Method 1: Android Device (USB Debugging)

1. **Enable Developer Options** on your Android phone:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
   
2. **Enable USB Debugging**:
   - Settings → Developer Options → USB Debugging (ON)
   
3. **Connect phone via USB** to your Windows PC

4. **Install the APK**:
   ```cmd
   C:\Android\sdk\platform-tools\adb install app\build\outputs\apk\debug\app-debug.apk
   ```

### Method 2: Android Emulator

1. **Open Android Studio** → Tools → Device Manager

2. **Create/Start an emulator**:
   - Choose a device (e.g., Pixel 5)
   - Select Android API level 21 or higher

3. **Install the APK**:
   ```cmd
   C:\Android\sdk\platform-tools\adb install app\build\outputs\apk\debug\app-debug.apk
   ```

### Method 3: Direct Transfer

1. **Copy APK to phone**:
   - Transfer `app-debug.apk` to your phone via USB, email, or cloud storage

2. **Install on phone**:
   - Open the APK file on your phone
   - Allow "Install from Unknown Sources" if prompted
   - Tap "Install"

### Method 4: Windows Subsystem for Android (WSA) - Windows 11 Only

1. **Install WSA** from Microsoft Store

2. **Enable Developer Mode** in WSA settings

3. **Connect to WSA**:
   ```cmd
   C:\Android\sdk\platform-tools\adb connect 127.0.0.1:58526
   ```

4. **Install the APK**:
   ```cmd
   C:\Android\sdk\platform-tools\adb install app\build\outputs\apk\debug\app-debug.apk
   ```

## First Run

When you launch the app for the first time:

1. **Grant Permissions**: The app will request:
   - Phone call permission (for support call feature)
   - Storage permission (for offline map tiles)

2. **Map Display**: The app will show an online map centered on Paris
   - Zoom level: 12
   - Coordinates: 48.8583°N, 2.2944°E

3. **Support Call Button**: Tap to access the emergency call feature

## Offline Map Support (Optional)

To add offline map support:

1. Obtain an MBTiles file (offline map tiles)
2. Place it in the app's files directory as `map.mbtiles`
3. Restart the app

**Note:** The current version uses online maps by default. Offline MBTiles support can be enhanced in future versions.

## Troubleshooting

### ADB Not Found
If `adb` command is not recognized:
```cmd
set PATH=%PATH%;C:\Android\sdk\platform-tools
```

### Installation Failed
- Ensure USB Debugging is enabled
- Check that the device is connected: `adb devices`
- Try uninstalling previous version: `adb uninstall com.example.offlinenav`

### App Crashes
- Check Android version (must be 5.0 or higher)
- Grant all requested permissions
- Check logcat for errors: `adb logcat | findstr offlinenav`

## Building from Source

To rebuild the app:

```cmd
cd C:\OfflineNavApp
gradle-8.6\bin\gradle.bat assembleDebug
```

The APK will be generated at `app\build\outputs\apk\debug\app-debug.apk`

## Next Steps

- Test all features (map navigation, support calls)
- Add offline map tiles for true offline functionality
- Create a signed release build for production deployment
- Publish to Google Play Store (requires signed release APK)

## Support

For issues or questions, refer to:
- [`README.md`](README.md) - Project overview
- [`README-deploy.md`](README-deploy.md) - Deployment details
- [`README-chat.md`](README-chat.md) - Chat logs

---

**Build Date:** December 18, 2025  
**Build Tool:** Gradle 8.6  
**Android SDK:** API 33
