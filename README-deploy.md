OfflineNavApp — Build & Deploy

This file explains how to build and produce signed artifacts for OfflineNavApp.

Prerequisites
- Java JDK (11+ recommended)
- Android SDK and command-line tools
- Gradle (system) or Gradle Wrapper (recommended)

1) Gradle wrapper
- This repository currently does not include the Gradle wrapper JAR. If you have system Gradle installed, you can run:

```powershell
gradle wrapper
```

This will generate the wrapper files (`gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, etc.).

2) Build debug APK

With wrapper (recommended):

```powershell
.\gradlew.bat assembleDebug
```

Or with system Gradle:

```powershell
gradle assembleDebug
```

3) Create a keystore and sign a release

Generate a keystore (if you don't have one):

```powershell
keytool -genkeypair -v -keystore my-release-key.jks -alias offlineNavKey -keyalg RSA -keysize 2048 -validity 10000
```

Create a `keystore.properties` file at the project root (do NOT commit this file). You can copy `keystore.properties.example` and fill values.

Example `keystore.properties`:

```
storeFile=keystore/my-release-key.jks
storePassword=your_store_password
keyAlias=offlineNavKey
keyPassword=your_key_password
```

4) Build signed release AAB (recommended for Play Store):

```powershell
.\gradlew.bat bundleRelease
```

Or signed release APK:

```powershell
.\gradlew.bat assembleRelease
```

Notes
- `app/build.gradle` was updated to load `keystore.properties` if present and apply a `signingConfig` for `release`.
- `app/proguard-rules.pro` was added (empty) because `build.gradle` references it.
- For Play Store releases prefer building an AAB (`bundleRelease`) and using Play App Signing.
- If you want, I can add the Gradle wrapper files to the repo, but the wrapper JAR is a binary normally generated via `gradle wrapper` on a machine that has Gradle installed.
\n# Last CI trigger: 2025-12-18T15:48:23.1138123+02:00
