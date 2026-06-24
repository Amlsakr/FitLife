# Story SESSION-001: CameraX Preview Composable

Status: ready-for-dev

Completion Note: Ultimate context engine analysis completed - comprehensive developer guide created.

Design Status: Planned (no dedicated Stitch export; use `docs/fitlife-ux-spec-v1.md#35-session-screen-most-complex` as the UX source).

## Story

As a FitLife user,
I want to see a live camera preview during an active workout session,
so that I can position myself correctly before later pose feedback, skeleton overlay, and guided-session features are layered on.

## Acceptance Criteria

1. Starting a session through the existing signed-in flow opens the session destination and, after camera permission is granted, replaces the current placeholder with a full-screen live back-camera preview.
2. The preview is implemented as a reusable `CameraPreview` composable in `:feature:session:session-ui`, using the pinned CameraX dependency line from the version catalog and a lifecycle-bound preview use case.
3. The existing `CameraPermissionGateRoute` remains the only production camera-permission gate: no camera permission dialog appears at app launch, splash, auth, onboarding, or app-shell entry.
4. If permission is denied or the user chooses audio-only mode, the session continues through the existing audio-only branch and does not attempt to bind CameraX.
5. If camera initialization fails, no camera is available, or binding throws, the UI shows a session-owned error/fallback state with an audio-only option and an exit action; the app must not crash or loop permission requests.
6. The preview fills the active session surface, respects device rotation/scaling through CameraX/PreviewView behavior, avoids stretched output, and keeps text/controls readable over the dark session treatment.
7. The production APK declares `android.permission.CAMERA` through a module that is actually merged into `:app`; because `:app` currently depends on `:feature:session:session-ui` and not `:feature:session:session-data`, this story must not rely only on the existing `session-data` manifest declaration.
8. Camera setup and teardown are lifecycle-safe: no blocking camera-provider calls on the main thread, no leaked `ProcessCameraProvider`, no unmanaged executor, and use cases are unbound when the preview leaves composition.
9. The implementation does not add ML Kit pose detection, `ImageAnalysis`, skeleton overlay, fatigue detection, lighting fallback automation, Lottie demos, session persistence, plan selection, or equipment rerouting. Those remain later session stories.
10. Automated coverage verifies the granted camera handoff, denied/audio-only handoff, startup no-prompt guard, camera-binding error fallback, and preview route behavior without requiring a physical camera.

## Tasks / Subtasks

- [ ] Prepare the session UI module for production camera preview. (AC: 2, 7)
  - [ ] Add only the required CameraX aliases to `feature/session/session-ui/build.gradle.kts`: `androidx-camera-core`, `androidx-camera-camera2`, `androidx-camera-lifecycle`, and `androidx-camera-view`.
  - [ ] Do not change CameraX versions; `gradle/libs.versions.toml` already pins `cameraX = "1.6.1"`.
  - [ ] Add a production `feature/session/session-ui/src/main/AndroidManifest.xml` with `android.permission.CAMERA`, or otherwise prove the final app manifest receives the permission through a production dependency.
  - [ ] Keep `:feature:session:session-ui` free of `:feature:session:session-data` dependencies.

- [ ] Replace the session camera placeholder with a real route. (AC: 1, 3, 4, 5)
  - [ ] Update `SessionEntryDestination` so `SessionMode.Camera` renders a production active-session route instead of `"SESSION-001 camera handoff is ready."`.
  - [ ] Preserve the existing `CameraPermissionGateRoute` callbacks and the audio-only branch.
  - [ ] Preserve `onExitSession` and the root Navigation 3 session entry registration.
  - [ ] Keep the audio-only branch simple and visible; do not invent the full lighting fallback story here.

- [ ] Implement `CameraPreview` in session-ui. (AC: 2, 5, 6, 8)
  - [ ] Create a session-owned package such as `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/preview/`.
  - [ ] Use `PreviewView` hosted through Compose `AndroidView`, with the `PreviewView` constructed inside the `AndroidView` factory lambda.
  - [ ] Request `ProcessCameraProvider` asynchronously and bind a `Preview` use case to `LocalLifecycleOwner.current` with `CameraSelector.DEFAULT_BACK_CAMERA`.
  - [ ] Set `preview.setSurfaceProvider(previewView.surfaceProvider)`.
  - [ ] Use `PreviewView.ScaleType.FILL_CENTER` or an equivalent full-screen configuration that avoids distortion.
  - [ ] Report binding/loading/error states through Compose state or an internal MVI-style state holder.
  - [ ] Unbind only the preview use case or the route-owned use-case group when leaving composition; avoid broad cleanup that could surprise later SESSION-002 combined preview + analysis work.

- [ ] Add the minimal active session chrome for this story. (AC: 1, 5, 6)
  - [ ] Render the preview as the full-screen background inside a dark session surface.
  - [ ] Add only lightweight controls needed now: exit/back, audio-only toggle/entry if available, and non-final copy such as "Live camera preview".
  - [ ] Use Material3 and existing `FitnessAppTheme` tokens; do not add custom theme globals for a future `SessionTheme` unless the local route needs a tiny private dark surface.
  - [ ] Add content descriptions and minimum 48dp touch targets for controls.

- [ ] Preserve architecture boundaries and future extension points. (AC: 8, 9)
  - [ ] Keep CameraX and Android types out of `:feature:session:session-domain`.
  - [ ] Do not promote `MlKitPoseBenchmarkAnalyzer` or `MlKitPoseBenchmarkHarness` directly into production UI.
  - [ ] If a binding helper is introduced, keep it internal to session-ui and small enough for SESSION-002 to extend with `ImageAnalysis` later.
  - [ ] Do not alter app-shell, auth, onboarding, home, or workout-plan navigation except for tests that prove the existing session entry still works.

- [ ] Add focused tests and verification. (AC: 3, 4, 5, 10)
  - [ ] Add injectable/fakeable camera-preview binding seams so automated tests can exercise success/error states without a physical camera.
  - [ ] Extend `CameraPermissionGateRouteTest` or add session route tests to prove granted permission routes to preview and denied permission routes to audio-only.
  - [ ] Keep or extend the app-level no-prompt test (`appLaunch_toHome_doesNotShowSessionDisclosure`) so startup still does not display session disclosure.
  - [ ] Verify camera-binding error displays fallback UI.
  - [ ] Run `.\gradlew.bat :feature:session:session-ui:testDebugUnitTest --no-daemon --console=plain`.
  - [ ] Run `.\gradlew.bat :feature:session:session-ui:assembleDebugAndroidTest --no-daemon --console=plain`.
  - [ ] Run `.\gradlew.bat :app:assembleDebug --no-daemon --console=plain`.
  - [ ] Optional manual smoke: on a real device, start a session, grant permission, confirm the preview renders, rotate/return, then exit and re-enter without crash.

## Dev Notes

### Current State

- `sprint-status.yaml` tracks this story as `session-001-camerax-preview-composable` under `epic-4`. Planning docs have session numbering drift: `_bmad-output/planning-artifacts/epics.md` v1.3 lists Session as Epic 5, while `docs/fitlife-stories-v1.md` and the sprint tracker use Epic 4 for Session. Follow the sprint key; do not renumber sprint entries during implementation.
- `SessionEntryDestination` already exists in `:feature:session:session-ui`. It runs `CameraPermissionGateRoute` first, then shows placeholder text for camera or audio-only mode.
- `CameraPermissionGateRoute`, `CameraPermissionViewModel`, permission state/event/action types, strings, unit tests, and Compose tests already exist from AUTH-006. Reuse them.
- `:app` registers session entries through `registerSessionEntries(onExitSession = { backStack.removeLastOrNull() })`, provides `LocalSessionNavigator`, and the Home placeholder uses `SessionStartButton`.
- `:feature:shell:shell-ui` owns the bottom-navigation container. Do not move session state into the shell.
- `:feature:session:session-data` contains spike-only `MlKitPoseBenchmarkHarness` and `MlKitPoseBenchmarkAnalyzer` and declares `android.permission.CAMERA`, but `:app` does not currently depend on `:feature:session:session-data`.
- `feature/session/session-ui/build.gradle.kts` currently has Compose/activity/navigation dependencies but no production CameraX dependencies.
- `gradle/libs.versions.toml` already includes CameraX aliases for `camera-core`, `camera-camera2`, `camera-lifecycle`, and `camera-view` at `1.6.1`; do not add duplicate coordinates.
- AUTH-006 is still marked `review` in sprint status, but its implementation files exist. Before dev starts, inspect any unresolved review notes so this story does not build on a broken gate.
- The worktree has unrelated uncommitted shell/auth/home/profile changes. Do not revert or normalize files outside this story's scope.

### What This Story Changes

- Turns the existing `SessionMode.Camera` placeholder into a production camera preview route.
- Adds the CameraX production dependencies and manifest permission needed by `session-ui`.
- Introduces a reusable `CameraPreview` composable and small active-session surface that later session stories can layer pose analysis, skeleton overlay, fatigue warnings, guided demos, and persistence onto.
- Adds tests around camera preview routing and fallback behavior without requiring real camera hardware in CI.

### What Must Be Preserved

- Permission is requested only when the user starts a session.
- Denied permission or explicit audio-only selection never blocks the workout.
- Splash/auth/onboarding/app-shell startup must not display the camera disclosure or system permission prompt.
- Navigation remains Navigation 3 with typed serializable `NavKey` destinations, `NavDisplay`, app-owned back stacks, and feature-owned entry registration.
- Domain remains free of Android, CameraX, ML Kit, and Firebase types.
- `MlKitPoseBenchmarkHarness` and `MlKitPoseBenchmarkAnalyzer` remain internal spike code unless a later story deliberately promotes a production analyzer.
- No camera frames, screenshots, health/profile data, or raw image buffers are logged or stored.

### Architecture Compliance

- This is a session UI story. The production preview belongs in `:feature:session:session-ui` because it is a visual surface and permission-gated session route.
- Keep `:feature:session:session-ui -> :feature:session:session-domain -> :core` dependency direction. Do not introduce `session-ui -> session-data`.
- CameraX binding can use Android framework APIs in the UI layer, but any future pose-analysis models must be mapped before crossing into domain.
- If a controller/helper is useful, make it internal and scoped to preview binding. Avoid a large camera abstraction until SESSION-002 proves what the analysis pipeline needs.
- Do not add `NavController`, `NavHost`, string routes, or `navigation-compose`.
- Keep `MainActivity` as the single activity host.

### Library And Framework Requirements

- Current project versions from `gradle/libs.versions.toml`:
  - CameraX: `1.6.1`
  - Activity Compose: `1.13.0`
  - Navigation 3 runtime/UI: `1.1.2`
  - Compose BOM: `2026.02.01`
  - Kotlin: `2.2.10`
  - AGP: `9.2.1`
- Use existing version-catalog aliases. Do not hardcode CameraX coordinates in module build files.
- Default implementation path for this story: `PreviewView` inside Compose `AndroidView`. This aligns with existing spike preview work and the current `androidx-camera-view` alias.
- CameraX `camera-compose` is also listed as stable `1.6.1` in current AndroidX release notes, but do not add it in this story unless the implementation intentionally changes direction and documents why. The `PreviewView` route is sufficient and lower churn for this codebase.
- Use `ProcessCameraProvider.getInstance(context)` with a listener/main executor instead of blocking the main thread.
- Bind `Preview` to the lifecycle owner and set the surface provider from `PreviewView`.
- Prefer the back camera for SESSION-001. Do not add front/back switching unless the user asks later.
- For later SESSION-002 compatibility, structure preview binding so a future `ImageAnalysis` use case can be bound together with preview under one lifecycle.

### File Structure Notes

Likely updates:

- `feature/session/session-ui/build.gradle.kts`
- `feature/session/session-ui/src/main/AndroidManifest.xml`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/SessionEntryDestination.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/preview/CameraPreview.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/preview/SessionCameraPreviewState.kt` if state is separated
- `feature/session/session-ui/src/main/res/values/strings.xml`
- `feature/session/session-ui/src/test/java/com/aml_sakr/fitlife/feature/session/ui/...`
- `feature/session/session-ui/src/androidTest/java/com/aml_sakr/fitlife/feature/session/ui/...`
- `app/src/androidTest/java/com/aml_sakr/fitlife/FitLifeAppNavigationTest.kt` only if app-level no-prompt/session-entry coverage needs extension

Files to read before editing:

- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/SessionEntryDestination.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/permission/CameraPermissionRationaleScreen.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/navigation/SessionNavigation.kt`
- `feature/session/session-ui/build.gradle.kts`
- `feature/session/session-data/src/main/AndroidManifest.xml`
- `feature/session/session-data/src/main/java/com/aml_sakr/fitlife/feature/session/data/pose/MlKitPoseBenchmarkHarness.kt`
- `feature/session/session-data/src/main/java/com/aml_sakr/fitlife/feature/session/data/pose/MlKitPoseBenchmarkAnalyzer.kt`
- `app/src/main/java/com/aml_sakr/fitlife/FitLifeApp.kt`
- `feature/home/home-ui/src/main/java/com/aml_sakr/fitlife/feature/home/ui/navigation/HomeNavigation.kt`
- `app/src/androidTest/java/com/aml_sakr/fitlife/FitLifeAppNavigationTest.kt`
- `gradle/libs.versions.toml`

### Testing Requirements

- Automated tests must not require a physical camera, emulator camera feed, Firebase, Gemini, or network access.
- Inject or fake the camera binder/provider at the route level so tests can simulate preview-ready and preview-error states.
- Continue to test MVI one-time actions separately from persistent UI state where state/action types are added.
- Keep Compose UI tests focused on rendered session states and callbacks, not actual camera hardware.
- App-level tests should verify the camera disclosure still does not appear at app launch/home entry.
- Manual real-device smoke is recommended after automated tests because camera preview rendering can only be fully trusted on-device.

### Previous Story Intelligence

- SETUP-004 proved the CameraX/ML Kit path can meet the 15 fps go/no-go threshold on a physical OPPO `CPH2737` run: 29.05 average FPS over 310 seconds, p50 26 ms, p95 36 ms, zero errors, and 97.6% pose-detected frames.
- SETUP-004 also established the important future analyzer rules: use CameraX lifecycle binding, keep analysis off the main thread, avoid analyzer backlog, and always close `ImageProxy`. SESSION-001 should not implement analysis, but it should avoid preview code that makes SESSION-002 hard.
- AUTH-006 established the privacy disclosure copy and gate behavior: on-device form analysis, nothing uploaded/stored, Activity Result permission request, granted path, denied audio-only fallback, and no permission request at launch.
- SHELL-001 established the signed-in shell and app-owned session navigation. SESSION-001 should reuse that root session entry instead of adding a tab, route string, or feature-level back-stack leak.
- The existing `SessionEntryDestination` placeholder was intentionally left for this story; replace it rather than adding a parallel screen.

### Git Intelligence

- Recent commits emphasize narrow feature boundaries:
  - `c0bb8b7` Merge pull request #11 from `feature/auth-006`
  - `2eee279` adding app shell story
  - `67dd7d5` implement auth-006, auth-007, epic-2, wp-001, wp-002, wp-003
  - `055f0fa` implement auth-006
- Continue that pattern: one session preview story, no navigation redesign, no pose-analysis implementation, no workout-plan integration.

### Latest Technical Information

- AndroidX Camera release notes list `androidx.camera:*` stable release `1.6.1` for `camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view`, and `camera-compose`, last updated May 6, 2026. The project is already pinned to this stable line. Source: https://developer.android.com/jetpack/androidx/releases/camera
- The official CameraX preview guide says `PreviewView` is the view used to display the preview and describes the implementation flow: add `PreviewView`, request `ProcessCameraProvider`, select a camera, and bind lifecycle/use cases. Source: https://developer.android.com/media/camera/camerax/preview
- The CameraX architecture guide describes Preview as the display surface use case, ImageAnalysis as the CPU-accessible ML use case, and lifecycle binding through `bindToLifecycle`. Use only Preview in this story. Source: https://developer.android.com/media/camera/camerax/architecture
- `PreviewView.getSurfaceProvider()` returns the surface provider that lets the camera feed start after the Preview use case is lifecycle-bound. Source: https://developer.android.com/reference/androidx/camera/view/PreviewView#getSurfaceProvider()
- Compose interop docs support hosting Android views with `AndroidView` and recommend constructing the view inside the `factory` lambda instead of remembering a View outside it. Source: https://developer.android.com/develop/ui/compose/migrate/interoperability-apis/views-in-compose

### References

- Story source: `docs/fitlife-stories-v1.md#SESSION-001`
- UX source: `docs/fitlife-ux-spec-v1.md#35-session-screen-most-complex`
- PRD: `_bmad-output/planning-artifacts/fitlife-prd-v1.md#54-pose-detection--form-feedback-ml-kit`
- Architecture: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#7-ml-kit-pose-detection-pipeline`
- Architecture: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#11-navigation-3-structure`
- Project context: `_bmad-output/project-context.md`
- Sprint tracker: `_bmad-output/implementation-artifacts/sprint-status.yaml`
- Permission gate story: `_bmad-output/implementation-artifacts/auth-006-camera-permission-privacy-disclosure.md`
- ML Kit/CameraX spike story: `_bmad-output/implementation-artifacts/setup-004-technical-spike-ml-kit-pose-detection-15-fps.md`
- Spike report: `_bmad-output/implementation-artifacts/spike-ml-kit-pose-detection-15-fps-report.md`
- App shell story: `_bmad-output/implementation-artifacts/shell-001-app-shell-with-bottom-navigation.md`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

### Completion Notes List

### File List
