# Story AUTH-006: Camera Permission Privacy Disclosure

Status: review

## Story

As a FitLife user,
I want FitLife to explain why it needs camera access before requesting it,
so that I can make an informed privacy decision and still continue with audio-only guidance if I decline.

## Acceptance Criteria

1. No camera permission prompt is shown at app launch, splash, auth, or startup routing.
2. The first time `SESSION-001` or a later session screen actually needs camera access, the app shows an in-app disclosure before the system permission dialog.
3. The disclosure clearly states that the camera is used to analyze form in real time, processing stays on-device, and nothing is uploaded or stored.
4. The camera request uses the Android runtime permission flow with `rememberLauncherForActivityResult` and `ActivityResultContracts.RequestPermission`, and it only launches after the disclosure is acknowledged.
5. If `shouldShowRequestPermissionRationale()` indicates a rationale is needed, the app shows the rationale screen instead of directly launching the system dialog.
6. If the user denies camera permission, the workout continues in audio-only mode and the UI exposes that fallback without blocking the session.
7. If camera permission is already granted, the session proceeds directly without showing the disclosure or prompt.
8. The disclosure and request flow live in `:feature:session:session-ui`; it does not add auth, splash, or app-shell navigation changes.
9. Automated coverage verifies the disclosure gate, granted path, denial fallback, and no-launch-at-start behavior without requiring a physical camera.

## Tasks / Subtasks

- [x] Add the camera privacy disclosure UI and state machine in session-ui. (AC: 2, 3, 5, 8)
  - [x] Create a dedicated rationale/disclosure composable that explains why FitLife needs camera access.
  - [x] Add MVI types for the permission gate flow using the project naming pattern.
  - [x] Provide explicit continue and decline/cancel actions so the flow can proceed or fall back cleanly.
  - [x] Keep the disclosure copy aligned with the UX and Play policy requirements.

- [x] Wire the runtime permission request into the first-session entry path. (AC: 1, 2, 4, 7, 8)
  - [x] Defer the permission request until the user starts a workout session entry owned by `SESSION-001` or its successor that actually needs camera access.
  - [x] Use `rememberLauncherForActivityResult` with `ActivityResultContracts.RequestPermission` rather than legacy permission APIs.
  - [x] Skip the disclosure and permission prompt entirely when the permission is already granted.
  - [x] Keep the permission flow inside session-ui and avoid introducing app-shell or auth navigation changes.
  - [x] Add the `androidx.activity:activity-compose` dependency in `feature/session/session-ui/build.gradle.kts` if it is not already present.

- [x] Preserve the audio-only fallback path. (AC: 6, 9)
  - [x] Route denied permission states into the existing audio-only session experience instead of blocking the workout.
  - [x] Make the fallback visible and user-invoked, not a silent failure.
  - [x] Keep the camera request flow from reappearing in a way that traps the user.

- [x] Add focused verification. (AC: 1-9)
  - [x] Test the gate logic for granted, denied, and rationale-required states.
  - [x] Test that no camera prompt is triggered at app launch or from splash/startup routing.
  - [x] Test that the denial path still allows the workout to continue in audio-only mode.
  - [x] Keep tests offline-safe and avoid requiring a physical camera.

## Dev Notes

### Current State

- `:feature:session:session-ui` already exists as a module but has no main-source implementation yet.
- `:feature:session:session-data` already declares `android.permission.CAMERA` in its manifest and contains benchmark-only permission handling for the pose-detection spike.
- `SESSION-001` owns the actual camera-preview entry point; this story only adds the privacy disclosure and runtime permission gate ahead of that entry.
- The splash/auth path already explicitly avoids requesting camera access; this story should keep that boundary intact.
- The original story source and UX spec both place camera permission disclosure in the session flow, not in auth or onboarding.

### Architecture Compliance

- Follow MVI + Clean Architecture. UI should render immutable state and emit events; permission prompts and fallback selection should be driven by one-time actions, not embedded business logic.
- Keep Android permission APIs in the UI layer. Do not push camera permission concerns into domain or data layers.
- Use the AndroidX Activity Result APIs for permission requests. Do not use legacy `requestPermissions()` or `onRequestPermissionsResult()` in new code.
- Do not move or duplicate the existing `CAMERA` manifest declaration into app shell or auth modules.
- Do not invent a new audio-only architecture here; emit the fallback action/state needed by the existing session experience and let the session flow own the actual fallback UI.
- Do not introduce navigation changes outside the session feature just to support this disclosure.

### Library And Framework Requirements

- Use `androidx.activity:activity-compose` for `rememberLauncherForActivityResult`.
- Use `ActivityResultContracts.RequestPermission` for the camera runtime request.
- If rationale UI is shown, the message must be in-app, context-specific, and immediately precede the permission request.
- Play policy guidance requires an option to decline the flow and a clear explanation of the data use before the runtime prompt.

### File Structure Requirements

Expected new or updated areas:

- `feature/session/session-ui/build.gradle.kts`
- `feature/session/session-ui/build.gradle.kts` must add `androidx.activity:activity-compose` and any Compose test artifacts needed for permission-gate coverage
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/permission/CameraPermissionState.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/permission/CameraPermissionEvent.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/permission/CameraPermissionAction.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/permission/CameraPermissionViewModel.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/permission/CameraPermissionRationaleScreen.kt`
- `feature/session/session-ui/src/main/res/values/strings.xml`
- Matching `src/test` and, if useful, `src/androidTest` coverage for the gate logic and Compose behavior

### Testing Requirements

- Verify the camera request is not launched during app startup.
- Verify granted permission skips the disclosure path.
- Verify rationale-required state shows the disclosure screen before the system prompt.
- Verify denied permission transitions into audio-only mode.
- Verify the story handoff to `SESSION-001` is explicit and no new session entry point is invented.
- Do not require a connected camera, emulator camera feed, or physical device camera for automated tests.
- Keep the tests small and focused on state transitions and permission-gate behavior.

### Previous Story Intelligence

- AUTH-000 established the explicit rule that no camera permission may be requested from splash or startup routing.
- `SESSION-001` is the first camera-using session story; this story should gate that entry, not create a parallel session launcher.
- AUTH-003, AUTH-004, and AUTH-005 are complete enough that this story should not reach back into auth deletion, Firestore rules, or account management.
- The current project direction uses typed Navigation 3 and feature-owned UI boundaries; this story should stay inside the session feature.

### Git Intelligence

- Recent auth work has been landing as narrow, reviewable feature stories with strong separation between UI, domain, and data layers.
- Keep this story similarly focused: disclosure UI, runtime permission request, and audio-only fallback only.

### Latest Technical Information

- Android runtime permission request guide: [https://developer.android.com/training/permissions/requesting](https://developer.android.com/training/permissions/requesting)
- Compose permission requests with Activity Result APIs: [https://developer.android.com/develop/ui/compose/libraries](https://developer.android.com/develop/ui/compose/libraries)
- `rememberLauncherForActivityResult` API reference: [https://developer.android.com/reference/kotlin/androidx/activity/compose/rememberLauncherForActivityResult.composable](https://developer.android.com/reference/kotlin/androidx/activity/compose/rememberLauncherForActivityResult.composable)
- Play policy prominent disclosure guidance: [https://support.google.com/googleplay/android-developer/answer/11150561](https://support.google.com/googleplay/android-developer/answer/11150561)
- Play policy user data and consent/runtimes permissions: [https://support.google.com/googleplay/android-developer/answer/10144311](https://support.google.com/googleplay/android-developer/answer/10144311)

### References

- Story source: `docs/fitlife-stories-v1.md#AUTH-006`
- UX: `docs/fitlife-ux-spec-v1.md#33-Session-Screen`
- UX: `docs/fitlife-ux-spec-v1.md#Accessibility-Checklist`
- Architecture: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#11-Navigation-3-Structure`
- Architecture: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md#7-ML-Kit-Pose-Detection-Pipeline`
- Project context: `_bmad-output/project-context.md`
- Prior auth guardrail: `_bmad-output/implementation-artifacts/auth-000-splash-screen-and-startup-routing.md`
- Session spike context: `feature/session/session-data/src/main/AndroidManifest.xml`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Implemented the session camera permission gate as a dedicated MVI flow with a Compose route, runtime launcher, and rationale screen.
- Added unit coverage for the permission gate ViewModel.
- Added Compose UI coverage for the disclosure screen and audio-only fallback notice.
- Verified the module with `.\gradlew.bat :feature:session:session-ui:testDebugUnitTest --no-daemon --console=plain`.
- Verified the Android-test APK with `.\gradlew.bat :feature:session:session-ui:assembleDebugAndroidTest --no-daemon --console=plain`.
- Verified the host app still assembles with `.\gradlew.bat :app:assembleDebug --no-daemon --console=plain`.
- Connected the gate into the app shell so the first session entry can reach the permission disclosure from the production Home flow.
- Fixed the fallback state handling so `SessionEntered` no longer clears the audio-only notice after a denial.
- Re-verified the host app with `.\gradlew.bat :app:assembleDebug --no-daemon --console=plain` after the review fixes.
- Refined the rationale refresh flow so Android rationale changes update the screen copy without clearing the audio-only fallback.
- Re-verified the session-ui tests with `.\gradlew.bat :feature:session:session-ui:testDebugUnitTest --no-daemon --console=plain` after the rationale-state fix.

### Completion Notes List

- Added `CameraPermissionState`, `CameraPermissionEvent`, `CameraPermissionAction`, and `CameraPermissionViewModel` for the session permission gate.
- Added `CameraPermissionGateRoute` and `CameraPermissionRationaleScreen` using `rememberLauncherForActivityResult` and `ActivityResultContracts.RequestPermission`.
- Added session-ui string resources for the disclosure, rationale, continue, and audio-only copy.
- Added unit tests and Compose UI tests for the permission disclosure experience.
- Added the session-ui Compose and test dependencies required for the new gate implementation.
- Added app-shell wiring for the first session entry path so the camera disclosure is reachable in production without inventing a new navigation destination.
- Preserved the audio-only fallback after denial by keeping `SessionEntered` from resetting the fallback visibility.
- Added a dedicated rationale-status update event so the disclosure copy stays in sync with Android's permission rationale state after denial.

### File List

- `_bmad-output/implementation-artifacts/auth-006-camera-permission-privacy-disclosure.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `app/build.gradle.kts`
- `app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt`
- `feature/session/session-ui/build.gradle.kts`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/permission/CameraPermissionAction.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/permission/CameraPermissionEvent.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/permission/CameraPermissionRationaleScreen.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/permission/CameraPermissionState.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/permission/CameraPermissionViewModel.kt`
- `feature/session/session-ui/src/main/res/values/strings.xml`
- `feature/session/session-ui/src/androidTest/java/com/aml_sakr/fitlife/feature/session/ui/permission/CameraPermissionRationaleScreenTest.kt`
- `feature/session/session-ui/src/test/java/com/aml_sakr/fitlife/feature/session/ui/permission/CameraPermissionViewModelTest.kt`

### Change Log

- 2026-06-17: Implemented the camera permission privacy disclosure gate in session-ui, added tests, and verified the module and host app builds.
- 2026-06-17: Addressed code review findings by wiring the gate into the app shell and stabilizing the audio-only fallback after denial.
- 2026-06-17: Refined rationale refresh handling so the disclosure text updates after denial without losing the audio-only fallback state.
