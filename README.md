# Puff AI — Android (Kotlin)

[![Android CI](https://github.com/blitzlabx/puff-android/actions/workflows/android.yml/badge.svg)](https://github.com/blitzlabx/puff-android/actions/workflows/android.yml)

Native Android client for **Puff AI by Blitz**.

- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34
- **Language:** Kotlin
- **UI:** Material 3, Telegram-style message animations, collapsible thoughts

## Features

- Landing screen with animated Puff avatar
- Chat with message limit (50)
- Collapsible “Thought for Xs” (→ / ↓)
- Clean final answers (reasoning separated)
- Smooth enter animations on messages
- Created by Blitz (blitzlabx)

## Build

1. Open the project in **Android Studio** (Hedgehog or newer recommended)
2. Sync Gradle
3. Run on an emulator or device (API 24+)

```bash
./gradlew assembleDebug
```

APK will be at `app/build/outputs/apk/debug/app-debug.apk`

## Notes

- Uses Prexzy `/ai/aiw3` with conversation history
- System prompt identifies Puff as created by Blitz
- No authentication required

## GitHub Actions (CI)

A workflow is included at `.github/workflows/android.yml`.

### What it does
- **On every push** → builds Debug APK and uploads it as artifact
- **Manual run (Actions tab → Run workflow)** → choose `debug`, `release`, or `both`
- Artifacts are kept for 7–14 days

### How to use
1. Push this repo to GitHub
2. Go to **Actions** tab
3. Select **Android CI** → **Run workflow**
4. Pick build type and run
5. Download the APK from the **Artifacts** section of the completed run

No signing config is required for debug. For release signing, add your keystore as a GitHub secret and extend the workflow.
