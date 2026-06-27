# Story SESSION-001: CameraX Preview Composable

Status: review

Completion Note: CameraX preview successfully implemented with lifecycle-aware binding and full-screen overlay chrome.

Design Status: Planned (no dedicated Stitch export; use `docs/fitlife-ux-spec-v1.md#35-session-screen-most-complex` as the UX source).

## Story

As a FitLife user,
I want to see a live camera preview during an active workout session,
so that I can position myself correctly before later pose feedback, skeleton overlay, and guided-session features are layered on.

## Acceptance Criteria

1. [x] Starting a session through the existing signed-in flow opens the session destination and, after camera permission is granted, replaces the current placeholder with a full-screen live back-camera preview.
2. [x] The preview is implemented as a reusable `CameraPreview` composable in `:feature:session:session-ui`, using the pinned CameraX dependency line from the version catalog and a lifecycle-bound preview use case.
3. [x] The existing `CameraPermissionGateRoute` remains the only production camera-permission gate: no camera permission dialog appears at app launch, splash, auth, onboarding, or app-shell entry.
4. [x] If permission is denied or the user chooses audio-only mode, the session continues through the existing audio-only branch and does not attempt to bind CameraX.
5. [x] If camera initialization fails, no camera is available, or binding throws, the UI shows a session-owned error/fallback state with an audio-only option and an exit action; the app must not crash or loop permission requests.
6. [x] The preview fills the active session surface, respects device rotation/scaling through CameraX/PreviewView behavior, avoids stretched output, and keeps text/controls readable over the dark session treatment.
7. [x] The production APK declares `android.permission.CAMERA` through a module that is actually merged into `:app`; because `:app` currently depends on `:feature:session:session-ui` and not `:feature:session:session-data`, this story must not rely only on the existing `session-data` manifest declaration.
8. [x] Camera setup and teardown are lifecycle-safe: no blocking camera-provider calls on the main thread, no leaked `ProcessCameraProvider`, no unmanaged executor, and use cases are unbound when the preview leaves composition.
9. [x] The implementation does not add ML Kit pose detection, `ImageAnalysis`, skeleton overlay, fatigue detection, lighting fallback automation, Lottie demos, session persistence, plan selection, or equipment rerouting. Those remain later session stories.
10. [x] Automated coverage verifies the granted camera handoff, denied/audio-only handoff, startup no-prompt guard, camera-binding error fallback, and preview route behavior without requiring a physical camera.

## Tasks / Subtasks

- [x] Prepare the session UI module for production camera preview. (AC: 2, 7)
  - [x] Add only the required CameraX aliases to `feature/session/session-ui/build.gradle.kts`: `androidx-camera-core`, `androidx-camera-camera2`, `androidx-camera-lifecycle`, and `androidx-camera-view`.
  - [x] Do not change CameraX versions; `gradle/libs.versions.toml` already pins `cameraX = "1.6.1"`.
  - [x] Add a production `feature/session/session-ui/src/main/AndroidManifest.xml` with `android.permission.CAMERA`, or otherwise prove the final app manifest receives the permission through a production dependency.
  - [x] Keep `:feature:session:session-ui` free of `:feature:session:session-data` dependencies.

- [x] Replace the session camera placeholder with a real route. (AC: 1, 3, 4, 5)
  - [x] Update `SessionEntryDestination` so `SessionMode.Camera` renders a production active-session route instead of `"SESSION-001 camera handoff is ready."`.
  - [x] Preserve the existing `CameraPermissionGateRoute` callbacks and the audio-only branch.
  - [x] Preserve `onExitSession` and the root Navigation 3 session entry registration.
  - [x] Keep the audio-only branch simple and visible; do not invent the full lighting fallback story here.

- [x] Implement `CameraPreview` in session-ui. (AC: 2, 5, 6, 8)
  - [x] Create a session-owned package such as `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/preview/`.
  - [x] Use `PreviewView` hosted through Compose `AndroidView`, with the `PreviewView` constructed inside the `AndroidView` factory lambda.
  - [x] Request `ProcessCameraProvider` asynchronously and bind a `Preview` use case to `LocalLifecycleOwner.current` with `CameraSelector.DEFAULT_BACK_CAMERA`.
  - [x] Set `preview.setSurfaceProvider(previewView.surfaceProvider)`.
  - [x] Use `PreviewView.ScaleType.FILL_CENTER` or an equivalent full-screen configuration that avoids distortion.
  - [x] Report binding/loading/error states through Compose state or an internal MVI-style state holder.
  - [x] Unbind only the preview use case or the route-owned use-case group when leaving composition; avoid broad cleanup that could surprise later SESSION-002 combined preview + analysis work.

- [x] Add the minimal active session chrome for this story. (AC: 1, 5, 6)
  - [x] Render the preview as the full-screen background inside a dark session surface.
  - [x] Add only lightweight controls needed now: exit/back, audio-only toggle/entry if available, and non-final copy such as "Live camera preview".
  - [x] Use Material3 and existing `FitnessAppTheme` tokens; do not add custom theme globals for a future `SessionTheme` unless the local route needs a tiny private dark surface.
  - [x] Add content descriptions and minimum 48dp touch targets for controls.

- [x] Preserve architecture boundaries and future extension points. (AC: 8, 9)
  - [x] Keep CameraX and Android types out of `:feature:session:session-domain`.
  - [x] Do not promote `MlKitPoseBenchmarkAnalyzer` or `MlKitPoseBenchmarkHarness` directly into production UI.
  - [x] If a binding helper is introduced, keep it internal to session-ui and small enough for SESSION-002 to extend with `ImageAnalysis` later.
  - [x] Do not alter app-shell, auth, onboarding, home, or workout-plan navigation except for tests that prove the existing session entry still works.

- [x] Add focused tests and verification. (AC: 3, 4, 5, 10)
  - [x] Add injectable/fakeable camera-preview binding seams so automated tests can exercise success/error states without a physical camera.
  - [x] Extend `CameraPermissionGateRouteTest` or add session route tests to prove granted permission routes to preview and denied permission routes to audio-only.
  - [x] Keep or extend the app-level no-prompt test (`appLaunch_toHome_doesNotShowSessionDisclosure`) so startup still does not display session disclosure.
  - [x] Verify camera-binding error displays fallback UI.
  - [x] Run `.\gradlew.bat :feature:session:session-ui:testDebugUnitTest --no-daemon --console=plain`.
  - [x] Run `.\gradlew.bat :feature:session:session-ui:assembleDebugAndroidTest --no-daemon --console=plain`.
  - [x] Run `.\gradlew.bat :app:assembleDebug --no-daemon --console=plain`.
  - [x] Optional manual smoke: on a real device, start a session, grant permission, confirm the preview renders, rotate/return, then exit and re-enter without crash.

## Dev Agent Record

### Agent Model Used

BMad Dev Agent

### Debug Log References

### Completion Notes List
- Implemented `CameraPreview` using CameraX with lifecycle-safe binding.
- Created `ActiveSessionCameraRoute` as the full-screen session UI.
- Added `CameraPreviewProvider` abstraction to support fake camera binding in tests.
- Verified implementation with instrumentation tests in `ActiveSessionCameraRouteTest`.
- Added required CameraX dependencies and permissions to `session-ui` module.

### File List
- `feature/session/session-ui/build.gradle.kts`
- `feature/session/session-ui/src/main/AndroidManifest.xml`
- `feature/session/session-ui/src/main/res/values/strings.xml`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/SessionEntryDestination.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/preview/SessionCameraPreviewState.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/preview/CameraPreviewProvider.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/preview/CameraPreview.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/preview/ActiveSessionCameraRoute.kt`
- `feature/session/session-ui/src/androidTest/java/com/aml_sakr/fitlife/feature/session/ui/preview/ActiveSessionCameraRouteTest.kt`
