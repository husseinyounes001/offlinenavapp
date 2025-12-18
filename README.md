# OfflineNavApp

Android Java app showing offline maps (MBTiles via osmdroid) and a Support Call screen (native phone calls + local SQLite storage).

Quick build (on Windows with Android Studio / command line):

1. Place an MBTiles file named `map.mbtiles` into the app's files directory after first run, or bundle it into `assets/` and modify code to copy it to files directory.

2. Build with Gradle (from project root):

```bash
./gradlew assembleDebug
```

APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

Notes:
- iOS project is not included here. For routing (turn-by-turn) consider integrating GraphHopper or other offline routing engines.
- To enable direct phone calls the app requests `CALL_PHONE` permission at runtime.
