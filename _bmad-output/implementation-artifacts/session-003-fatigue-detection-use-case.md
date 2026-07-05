# Story 4.3: Fatigue Detection Use-Case

Status: done

## Story

As a FitLife user,
I want the app to warn me when I’m fatigued based on pose stability,
so that I can prevent injury and maintain workout quality.

## Acceptance Criteria

1. [ ] `DetectFatigueUseCase` implemented in `:feature:session:session-domain` that analyzes `PoseData` stream.
2. [ ] Detects deviation >15° from baseline joint angles over three consecutive reps.
3. [ ] Baseline is established using the average of the first two reps of an exercise.
4. [ ] MVI integration: `ActiveSessionViewModel` handles fatigue detection and updates state with `isFatigued = true`.
5. [ ] UI: `ActiveSessionCameraRoute` shows an animated warning banner using `AnimatedVisibility` when `isFatigued` is true.
6. [ ] UI: "I feel fine — continue" button dismisses the warning.
7. [ ] Dismissal logic: Re-triggers only if fatigue is detected in next 5 reps, not immediately after dismissal.
8. [ ] Analytics: Log `fatigue_detected` and `fatigue_dismissed` events to Firebase.
9. [ ] Audio: A warning alert plays when fatigue is detected, even if the phone screen is locked (requires Foreground Service).
10. [ ] Performance: Detection latency ≤ 2 seconds from the 3rd consecutive bad rep to the UI/Audio warning.

## Tasks / Subtasks

- [x] Implement Fatigue Detection Logic. (AC: 1, 2, 3)
  - [x] Create `DetectFatigueUseCase` in `:feature:session:session-domain`.
  - [x] Implement joint angle calculation logic using `atan2(y, x)`.
  - [x] Implement baseline collection (first 2 reps) and deviation tracking (>15°).
  - [x] Define `FatigueStatus` domain model (Healthy, Fatigued).
- [x] Integrate with ActiveSession MVI. (AC: 4, 7, 8)
  - [x] Add `isFatigued` to `ActiveSessionState`.
  - [x] Add `FatigueDetected` and `DismissFatigue` events to `ActiveSessionEvent`.
  - [x] Implement dismissal cooldown logic (5 reps) in `ActiveSessionViewModel`.
  - [x] Inject `DetectFatigueUseCase` into `ActiveSessionViewModel`.
- [x] Implement UI Warning Banner. (AC: 5, 6)
  - [x] Create `FatigueWarningBanner` composable in `:feature:session:session-ui`.
  - [x] Use `AnimatedVisibility` for smooth entry/exit.
  - [x] Add "I feel fine — continue" button to the banner.
- [x] Implement Audio Alert Service. (AC: 9, 10)
  - [x] Create `SessionAudioService` (Foreground Service) in `:feature:session:session-ui` or `:feature:session:session-data`.
  - [x] Implement audio playback using `MediaPlayer` or `ExoPlayer`.
  - [x] Ensure service lifecycle is tied to the active session.
- [x] Analytics and Testing. (AC: 8)
  - [x] Log events using `AnalyticsRepository`.
  - [x] Add unit tests for `DetectFatigueUseCase` with mocked `PoseData` sequences.
  - [x] Verify UI banner appearance in `ActiveSessionCameraRouteTest`.

## Dev Notes

- **Algorithm Details**: 
  - Compare current joint angles (Shoulders, Elbows, Hips, Knees) against `baselineAngles`.
  - `angle = |atan2(p3.y-p2.y, p3.x-p2.x) - atan2(p1.y-p2.y, p1.x-p2.x)|`.
  - Use `PoseJoint` enum for landmark selection.
- **MVI Pattern**: 
  - `DetectFatigueUseCase` should likely be called from the `ActiveSessionViewModel` as it receives `PoseData` from `AnalyzePoseUseCase`.
- **Foreground Service**: 
  - Use `startForegroundService` and provide a persistent notification to keep the process alive for audio alerts.
- **Anti-Pattern Prevention**:
  - Do NOT hardcode thresholds; use constants or configuration.
  - Do NOT leak ML Kit `Pose` objects; use `PoseData` from `session-domain`.

### Project Structure Notes

- Module `:feature:session:session-domain` owns the business logic (`DetectFatigueUseCase`).
- Module `:feature:session:session-ui` owns the UI components and the Foreground Service.
- Module `:core:core-ui` may host shared `MVI` base classes.

### References

- [Source: _bmad-output/planning-artifacts/fitlife-architecture-v1.md#8-fatigue-detection-algorithm]
- [Source: _bmad-output/planning-artifacts/fitlife-architecture-v1.md#14.6-analytics-events-taxonomy]
- [Source: _bmad-output/implementation-artifacts/session-002-ml-kit-posedetector-integration.md]

### Review Findings

- [x] [Review][Patch] Hardcoded 15.0° deviation threshold [DetectFatigueUseCase.kt:112]
- [x] [Review][Patch] null default notification URI handling [SessionAudioService.kt:54]
- [x] [Review][Patch] 5-rep cooldown logic blocks 6 reps [ActiveSessionViewModel.kt:66]

## Dev Agent Record

### Agent Model Used

BMad Create Story Agent (Gemini 2.0 Flash)

### Debug Log References

### Completion Notes List
- Implemented `DetectFatigueUseCase` with joint angle calculation and baseline establishment.
- Integrated fatigue detection into `ActiveSessionViewModel` with MVI events and cooldown logic.
- Created `FatigueWarningBanner` UI component with `AnimatedVisibility`.
- Implemented `SessionAudioService` (Foreground Service) for audio alerts when the device is locked.
- Moved `AnalyticsLogger` and `CrashReporter` interfaces to `core-domain` for better architectural alignment.
- Added necessary strings and updated module dependencies (Hilt, javax.inject).
- Verified core logic with `DetectFatigueUseCaseTest`.

### File List
- `core/core-domain/src/main/java/com/aml_sakr/fitlife/core/domain/AnalyticsLogger.kt`
- `core/core-domain/src/main/java/com/aml_sakr/fitlife/core/domain/CrashReporter.kt`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/di/FirebaseObservabilityModule.kt`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/observability/FirebaseAnalyticsLogger.kt`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/observability/FirebaseCrashReporter.kt`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/observability/InMemoryAnalyticsLogger.kt`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/observability/InMemoryCrashReporter.kt`
- `feature/session/session-domain/build.gradle.kts`
- `feature/session/session-domain/src/main/java/com/aml_sakr/fitlife/feature/session/domain/pose/FatigueStatus.kt`
- `feature/session/session-domain/src/main/java/com/aml_sakr/fitlife/feature/session/domain/pose/DetectFatigueUseCase.kt`
- `feature/session/session-domain/src/test/java/com/aml_sakr/fitlife/feature/session/domain/pose/DetectFatigueUseCaseTest.kt`
- `feature/session/session-ui/build.gradle.kts`
- `feature/session/session-ui/src/main/AndroidManifest.xml`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/ActiveSessionContracts.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/ActiveSessionViewModel.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/components/FatigueWarningBanner.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/service/SessionAudioService.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/preview/ActiveSessionCameraRoute.kt`
- `feature/session/session-ui/src/main/res/values/strings.xml`
- `feature/auth/auth-data/src/main/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseAuthRepository.kt`
- `feature/auth/auth-data/src/test/java/com/aml_sakr/fitlife/feature/auth/data/repository/FirebaseAuthRepositoryTest.kt`
