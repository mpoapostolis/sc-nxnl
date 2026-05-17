# Skin Coach — Phase 1 (Foundation) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** A buildable, runnable Android app with the Skin Coach design system, a navigation skeleton, and a working CameraX selfie-capture flow that ends on a placeholder result screen.

**Architecture:** Native Android, single-Activity Jetpack Compose, MVVM-ready. Phase 1 is UI/framework scaffolding — there is no business logic yet, so verification is "build + run + visually confirm", not unit tests. Real TDD begins in Phase 2 (Skin Score logic). Screens get a dedicated polish pass with the frontend-design skill during execution.

**Tech Stack:** Kotlin, Jetpack Compose (Material 3), CameraX, Navigation-Compose, Gradle (Kotlin DSL + version catalog). minSdk 24, compileSdk/targetSdk latest stable.

---

## Context Notes

- Phase 1 of 5 — see `docs/superpowers/specs/2026-05-18-skin-coach-design.md`. Phases 2–5 get their own plans.
- Greenfield project built directly on `main` (no existing code to isolate; no worktree needed).
- Toolchain confirmed: JDK 21, Android SDK at `~/Library/Android/sdk`, Android Studio, adb.
- Package: `com.skincoach.app`. App label: "Skin Coach".
- Commit after each task (confirm commit preference with the user before the first commit).

## File Structure

```
settings.gradle.kts          — Gradle settings, module list
build.gradle.kts             — root build file (plugin versions via catalog)
gradle.properties            — JVM args, AndroidX flags
gradle/libs.versions.toml    — version catalog (single source of dependency versions)
local.properties             — sdk.dir (generated; gitignored)
.gitignore                   — standard Android ignores
app/build.gradle.kts         — app module config, dependencies
app/src/main/AndroidManifest.xml
app/src/main/res/            — icons, strings.xml, Android XML theme
app/src/main/java/com/skincoach/app/
  MainActivity.kt            — single Activity, hosts Compose + NavHost
  ui/theme/                  — Color / Type / Shape / Spacing / Theme  (design system)
  ui/navigation/             — Destinations, SkinCoachNavHost
  ui/screens/home/           — HomeScreen
  ui/screens/capture/        — CaptureScreen (CameraX + permission)
  ui/screens/result/         — ResultScreen (placeholder; Phase 2 plugs analysis here)
```

---

## Task 1: Buildable empty app

**Files:** Create `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties` (+ wrapper jar/script via `gradle wrapper` or Android Studio), `.gitignore`, `local.properties`, `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`, `app/src/main/res/values/strings.xml`, `app/src/main/res/values/themes.xml`, `app/src/main/java/com/skincoach/app/MainActivity.kt`.

- [ ] **Step 1** — Create the Gradle scaffold: version catalog with AGP, Kotlin 2.x, Compose BOM, `androidx.activity:activity-compose`, `androidx.core:core-ktx`, `androidx.lifecycle:lifecycle-runtime-ktx`, Material 3. App module: `minSdk 24`, `compileSdk`/`targetSdk` latest stable, `buildFeatures { compose = true }`, Kotlin Compose plugin.
- [ ] **Step 2** — `local.properties` with `sdk.dir=/Users/mpoapostolis/Library/Android/sdk`. `.gitignore` for `/build`, `/.gradle`, `local.properties`, `*.iml`, `.idea/`.
- [ ] **Step 3** — `AndroidManifest.xml`: single `MainActivity` (exported, LAUNCHER intent filter), app label "Skin Coach". `MainActivity.kt`: `ComponentActivity` with `setContent { }` showing a centered "Skin Coach" `Text`.
- [ ] **Step 4** — Build. Run: `./gradlew assembleDebug` — Expected: `BUILD SUCCESSFUL`.
- [ ] **Step 5** — Install & launch on emulator/device: `./gradlew installDebug` then open the app — Expected: a screen with "Skin Coach".
- [ ] **Step 6** — Commit: `feat: buildable Android app scaffold`.

## Task 2: Design system / theme

**Files:** Create `ui/theme/Color.kt`, `Type.kt`, `Shape.kt`, `Spacing.kt`, `Theme.kt`. Add `androidx.compose.ui:ui-text-google-fonts` to the catalog + app deps.

Design tokens (distinctive, premium, calm — NOT default Material purple):
- **Colors:** background `#FBF7F3` (warm cream), surface `#FFFFFF`, surfaceVariant `#F1EAE2` (sand), onBackground/ink `#1F1B16`, muted `#8A8178`, outline `#E4DBD0`, primary `#E5704D` (warm coral), onPrimary `#FFFFFF`, secondary `#5B8A72` (sage = good/progress), error `#C0392B`.
- **Type:** display/headline font "Fraunces" (editorial serif — for the app name and the big Skin Score); body/UI/label font "Inter". Define a Material 3 `Typography` with both via downloadable Google Fonts.
- **Shape:** small 12dp, medium 20dp, large 28dp; buttons are pill-shaped (full rounding).
- **Spacing:** object `Spacing` with `xs=4, sm=8, md=12, lg=16, xl=24, xxl=32` dp.

- [ ] **Step 1** — Create all five theme files with the tokens above. `Theme.kt` exposes `SkinCoachTheme { }` wrapping `MaterialTheme` with a light `colorScheme`, the `Typography`, and `Shapes`. (Dark theme deferred — leave a TODO-free single light scheme.)
- [ ] **Step 2** — Wrap `MainActivity`'s content in `SkinCoachTheme`; set the "Skin Coach" text to the display font.
- [ ] **Step 3** — Add a `@Preview` for a small sample (text + a filled pill button) to confirm the theme renders.
- [ ] **Step 4** — Build: `./gradlew assembleDebug` — Expected: `BUILD SUCCESSFUL`; preview renders with cream background + coral button.
- [ ] **Step 5** — Commit: `feat: Skin Coach design system (color, type, shape, spacing)`.

## Task 3: Navigation skeleton

**Files:** Create `ui/navigation/Destinations.kt`, `ui/navigation/SkinCoachNavHost.kt`. Add `androidx.navigation:navigation-compose` to the catalog + deps.

- [ ] **Step 1** — `Destinations.kt`: a sealed type / route constants for `Home`, `Capture`, `Result` (Result carries a photo URI/path argument).
- [ ] **Step 2** — `SkinCoachNavHost.kt`: a `NavHost` with start destination `Home` and the three routes, each wired to a minimal placeholder composable (a screen title `Text`). `Capture` and `Result` get a back affordance.
- [ ] **Step 3** — `MainActivity` hosts `SkinCoachNavHost()` inside `SkinCoachTheme`.
- [ ] **Step 4** — Build, install, run: navigate Home → Capture → Result → back — Expected: each placeholder screen shows and back works.
- [ ] **Step 5** — Commit: `feat: single-activity navigation skeleton`.

## Task 4: Home screen

**Files:** Create `ui/screens/home/HomeScreen.kt` (replaces the Home placeholder).

- [ ] **Step 1** — Build a polished landing screen: app wordmark "Skin Coach" (display font), a short value line ("Scan your skin. Get your score."), generous whitespace on the cream background, and a large primary pill CTA **"Scan my skin"**. Optional: a small row teasing the 6 concerns analyzed.
- [ ] **Step 2** — The CTA navigates to `Capture`.
- [ ] **Step 3** — Add a `@Preview`. Build, install, run — Expected: a clean, premium-looking home screen; CTA opens Capture.
- [ ] **Step 4** — Commit: `feat: home screen`.

## Task 5: Camera capture screen (CameraX + permission)

**Files:** Create `ui/screens/capture/CaptureScreen.kt` (+ a small `CameraPermission.kt` helper if it keeps the screen focused). Add CameraX to the catalog + deps: `androidx.camera:camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view`. Add `<uses-permission android:name="android.permission.CAMERA"/>` and `<uses-feature android:name="android.hardware.camera.any"/>` to the manifest.

- [ ] **Step 1** — Permission: request `CAMERA` on entry; render a clear rationale + "Grant access" state when not granted, and route to app settings if permanently denied.
- [ ] **Step 2** — Camera preview: `PreviewView` via `AndroidView`, **front camera** by default, bound to the composable lifecycle.
- [ ] **Step 3** — Capture overlay: a face-framing oval guide + a one-line lighting hint, and a large shutter button.
- [ ] **Step 4** — On capture: save the photo to app-internal storage (`filesDir`), then navigate to `Result` passing the saved file path/URI.
- [ ] **Step 5** — Build, install, run: grant permission, see the front-camera preview with the oval guide, tap shutter — Expected: a photo is captured and the app moves to Result.
- [ ] **Step 6** — Commit: `feat: CameraX selfie capture screen`.

## Task 6: Result placeholder screen

**Files:** Create `ui/screens/result/ResultScreen.kt` (replaces the Result placeholder).

- [ ] **Step 1** — Display the captured selfie (load the passed file path). Below it, a placeholder "Skin Score" block — a styled empty score gauge/card with text like "Analysis ready in the next build" — this is the slot where Phase 2's real `SkinAnalysis` result renders.
- [ ] **Step 2** — A "Scan again" action returning to `Home` (or `Capture`).
- [ ] **Step 3** — Build, install, run the full path — Expected: the captured photo shows on Result with the placeholder score block.
- [ ] **Step 4** — Commit: `feat: result screen placeholder`.

## Task 7: End-to-end wiring + smoke test

- [ ] **Step 1** — Verify the full flow on a device/emulator: Home → tap "Scan my skin" → grant camera → capture selfie → Result shows the photo → "Scan again" → Home.
- [ ] **Step 2** — Fix any navigation/argument-passing or lifecycle issues found.
- [ ] **Step 3** — Confirm `./gradlew assembleDebug` and `./gradlew lint` are clean (address real warnings).
- [ ] **Step 4** — Commit: `chore: phase 1 foundation end-to-end smoke test passing`.

---

## Phase 1 Done When

The app builds, installs, and a user can launch it, tap one button, take a guided selfie, and land on a result screen showing their photo — all on the Skin Coach design system. This is the working skeleton that Phase 2 (skin analysis + Skin Score) plugs into at the Result screen.
