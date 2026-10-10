# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

GPS速度插件 (GPS Speed Widget): an Android app for Chinese car head units (车机). It shows GPS speed as home-screen widgets and floating overlay windows, integrates with AMap Auto (高德车机版) navigation broadcasts, runs AMap "aimless mode" cruise (智能巡航 / 电子狗) with TTS alerts, records and uploads driving tracks, shows desktop lyrics, and can act as the device launcher (HOME). UI text is Chinese, much of it hard-coded in Java.

## Build & run

Single Gradle module `app`. Use the wrapper only.

```bash
./gradlew assembleDebug          # app/build/outputs/apk/debug/GPS速度插件_v<versionName>_<versionCode>.apk
./gradlew assembleRelease        # minified + shrunk, signed with the checked-in keystore/GPSWidgetSignKey
./gradlew installDebug
./gradlew lint                   # lintOptions.abortOnError = false, so lint never fails the build
./gradlew clean
```

Toolchain constraints:
- Gradle wrapper 6.7.1 + Android Gradle Plugin 4.2.1. Requires **JDK 8 or 11**; JDK 17+ and newer Gradle will not load AGP 4.2.1. On this machine the default `JAVA_HOME` is JDK 21, so build with `JAVA_HOME=~/.jdks/jdk-11.0.32.1+1 ./gradlew ...`. The IDE is configured to use the wrapper with `gradleJvm=#GRADLE_LOCAL_JAVA_HOME`, which reads `java.home` from the git-ignored `.gradle/config.properties`.
- Android SDK: `local.properties` (git-ignored) points `sdk.dir` at `~/AppData/Local/Android/Sdk`, which has platform 30 and build-tools 30.0.2 (AGP 4.2.1's minimum). compileSdk/targetSdk 30, minSdk 17, only `armeabi-v7a` is packaged (`ndk.abiFilters`).
- Dependency repositories are `google()`, `mavenCentral()`, the Aliyun `public` mirror, and JitPack. JCenter itself is shut down; six dependencies (percent-support-extends, arcprogressstackview, material-intro-screen, recyclerview-animators 2.2.7, autostarter, xcrash) exist only on the Aliyun mirror, so do not remove it. Crashlytics/Fabric artifacts come from `google()`.
- **Legacy `com.android.support` libraries, not AndroidX.** `android.useAndroidX` is unset and there is zero `androidx.*` usage. New code must import `android.support.*`.
- Version bump = `versionCode` / `versionName` in `app/build.gradle`. Commit messages typically reference the versionCode (e.g. "290 ready").
- There are no unit or instrumented tests and no test source sets; test dependencies are commented out in `app/build.gradle`.

Installing on a device: the manifest puts `android:sharedUserId="android.uid.system"` and `android:persistent="true"` on `<application>`, not `<manifest>`. Android only honours these on the root element, so they are currently ignored: the APK installs on any device and does not run as the system UID. Moving `sharedUserId` to `<manifest>` would make the APK installable only with the head unit's platform certificate.

### Track-history server (`server/`)

Plain Node/Express + SQLite service that receives uploaded GPS points. Listens on **8095**.

```bash
cd server && npm install express querystring node-cache sqlite3 moment body-parser && node GPSHistory.js
docker build -t gpshistory server/   # pm2-runtime image, expects server/data and server/release dirs
```

## Architecture

All app code is under `app/src/main/java/com/huivip/gpsspeedwidget/` unless noted.

### Core data flow

- **`GpsUtil`** is the single source of truth for location state: a process-wide singleton that owns the `LocationManager` listener, a 1-second `Timer`, and all derived fields (km/h & mph speed, bearing, altitude, distance, speed limit, camera distance/type, current/next road name, AMap navi/cruise status flags). Everything else reads from it via getters; floating-window services run their own 1-second `Timer` and poll `GpsUtil` rather than subscribing to location events.
- **greenrobot EventBus** is the control backbone. Event classes live in `beans/*Event.java`. The key one is `LaunchEvent(ServiceClass).setToClose(bool).setDelaySeconds(n).setExtentParameters(map)`: post it instead of calling `startService` directly. `BootStartService` is the sole subscriber and performs the actual start/stop.
- **`service/BootStartService`** is the orchestrator. It is started by `listener/BootStartReceiver` (BOOT_COMPLETED, USER_PRESENT, CONNECTIVITY_CHANGE, plus head-unit ACC/power-on actions like `xy.android.acc.on`), by `detection/AppDetectionService`, by `AutoMapBoardReceiver` when AMap starts, and by `MainActivity`. On start it reads every enable flag from `AppSettings`/`PrefUtils` and launches the corresponding services, registers runtime `BroadcastReceiver`s (media, switch, AMap, boot, time tick), and runs as a foreground service on Android O+.

### Service categories (`service/`)

- **Floating windows** (`*FloatingService`): each inflates a `res/layout/floating_*.xml`, adds it to `WindowManager` as an overlay, binds views with ButterKnife, polls `GpsUtil`, and persists its drag position via `PrefUtils`. Convention: a `public static final String EXTRA_CLOSE` intent extra stops the service, and a `TARGET` constant identifies it for delayed relaunch through `Utils.delayLaunchSelf` → `listener/DelayTaskReceiver`. `Utils.startFloatingWindows(ctx, enabled)` decides which speed-window style (default / AMap-style `AutoNaviFloatingService` / `MeterFloatingService`) to show based on settings and whether the user is on the desktop.
- **Widget updaters** (`*WidgetService`, `GpsSpeedMeterService`, `GpsSpeedNumberService`): push `RemoteViews` to the `AppWidgetProvider`s in `widget/`. Tapping a widget toggles its service; the providers' `onEnabled`/`onDisabled` flip `PrefUtils` flags that `BootStartService` later consults.
- **Background workers**: `AutoXunHangService` (AMap `AMapNavi` aimless mode: cameras, speed limits, congestion → `PlayAudioEvent`), `RecordGpsHistoryService` (inserts a point per second into SQLite while moving; `UploadRoadDataReceiver` alarm batches uploads to `Constant.LBSURL`), `NaviTrackService` (AMap Track SDK), `WeatherService`, `RoadLineService`.

### AMap Auto integration

`listener/AutoMapBoardReceiver` implements the AMap Auto standard broadcast protocol (`AUTONAVI_STANDARD_BROADCAST_SEND`, dispatched on the `KEY_TYPE` int extra; 10019 = app state such as foreground/background/navi started). It writes navi state into `GpsUtil` and shows/hides the backend navigation overlay (`NaviFloatingService`, `AutoWidgetFloatingService`). Extra key names for navi info are in `Constant.NaviInfoConstant`. `AutoMapFromHostBoardReceiver` handles the reverse direction.

`listener/SwitchReceiver` is the external toggle API: broadcast `com.huivip.switch.event` with a `TARGET` extra (cruise, map overlay, lyrics, GPS on/off, launch AMap).

### Preferences: two systems coexist

- `util/AppSettings` (preferred): `SharedPreferences` file `"app"`, keys are `R.string.pref_key__*`, edited through `res/xml/preferences_*.xml` + `fragment/Settings*Fragment` under `SettingsActivity`. Built on `net.gsantner.opoc.preference.SharedPreferencesPropertyBackend`.
- `utils/PrefUtils` (legacy, static): string-constant keys in default and named preference files. Still authoritative for widget enabled flags, floating-window positions, device id, FTP settings, and the TTS engine choice.

When adding a feature flag, add it to `AppSettings` and wire it in `BootStartService`.

### Embedded launcher fork

`HomeActivity` (HOME intent filter), `activity/homeparts/Hp*`, `manager/Setup` (service locator initialised with `HpInitSetup` in `HomeActivity` and `MainActivity`), `util/` (`AppManager`, `DatabaseHelper`, `Tool`, `Definitions`), `model/`, `viewutil/`, the launcher views in `widget/` (`Desktop`, `Dock`, `CellContainer`, `AppDrawer*`), and `net.gsantner.opoc.*` are an embedded OpenLauncher-derived launcher. It is gated by `AppSettings.isEnableDesktopFunction()`; `OnBoardActivity` is the LAUNCHER entry and routes to `MainActivity` (GPS control panel) or `HomeActivity`. Note the two parallel utility packages: `util/` is launcher-side, `utils/` is GPS-side.

### Speech (`speech/`)

`TTS` interface → `SpeechFactory.getTTSEngine(type)` returns `BDTTS` (Baidu offline/online), `SBCTTS` (思必驰 DUI-lite), `AliTTS` (AMap SDK voice), or `TextSpeech` (Android built-in). `TTSService` is the base class that queues phrases and plays synthesized audio through the bound `PlayAudioService`. `AudioService` subscribes to `PlayAudioEvent` and handles volume ducking/mute (`AudioTempMuteEvent` while AMap is in the foreground). Offline TTS model files live in `app/src/main/assets/`.

### Lyrics & music (`lyrics/`, `music/`)

LRC/KRC/KSC/HRC parsers and custom lyric views, NetEase lyric fetching (`WangYiYunMusic`), and `MusicRemoteControllerService` (a `NotificationListenerService`). `listener/MediaNotificationReceiver` listens to ~20 music players' `metachanged`-style broadcasts (manifest + runtime registration).

### Third-party binary SDKs

`app/libs/*.jar` (AMap 3D/Navi/Search/Track/Location bundle, Baidu TTS, DUI-lite, Kuwo auto SDK) plus `jniLibs/`. `app/proguard-rules.pro` has extensive keep rules for them; release builds minify, so add keep rules when introducing reflection or new SDK surface. API keys for AMap/DUI are hard-coded in `Constant.java` and `AndroidManifest.xml`.

### Other conventions

- Android O+ service starts go through `Utils.startService(ctx, intent, /*foreground*/ true)`; use `Utils.isServiceRunning(ctx, cls.getName())` before starting to avoid duplicates.
- Overlay services check `PrefUtils.isEnableDrawOverFeature()` and open the system overlay-permission screen if missing.
- Delayed main-thread work uses either `new Handler().postDelayed` or xUtils `x.task().postDelayed`.
- Local track DB: `utils/DBUtil` (`GPSHistory.db`, table `GPS`, `lineId` groups points into a trip). Launcher DB: `util/DatabaseHelper`.
- `res/values/strings.xml` is the default (mostly launcher strings); `values-zh-rCN` overrides. GPS-feature toasts and labels are inline Chinese literals in Java.
