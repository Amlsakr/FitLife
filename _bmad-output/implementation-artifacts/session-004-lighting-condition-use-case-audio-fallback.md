# Story 4.4: Lighting Condition Use-Case & Audio Fallback

Status: done

## Story

As a FitLife user,
I want the app to automatically switch to audio-only mode in low-light conditions,
so that I can continue my workout safely even when form tracking is unreliable due to poor lighting.

## Acceptance Criteria

1. [x] `LightingUseCase` implemented in `:feature:session:session-domain` that evaluates pose confidence and ambient lux.
2. [x] Automatic Trigger: Switch to audio-only mode if sustained low pose confidence (< 0.6) OR ambient brightness < 10 lux for 2 seconds.
3. [x] Automatic Revert: Return to visual mode if stable brightness > 10 lux AND pose confidence > 0.6 for 3 seconds.
4. [x] Manual Override: User can manually toggle between audio-only and visual modes via a UI button (e.g., in a bottom sheet or on-screen toggle). Manual override disables automatic switching until the session is restarted or manually toggled back.
5. [x] UI Fallback: When in audio-only mode, the camera preview is dimmed or replaced with a minimal "Audio Mode" UI to save battery and reduce distraction.
6. [x] Audio Guidance: Use `TextToSpeech` (TTS) to provide workout instructions and form cues (if any) when in audio-only mode.
7. [x] Analytics: Log `lighting_fallback_triggered` and `lighting_fallback_reverted` events with relevant metadata (lux, average confidence).
8. [ ] Persistence: `SessionEntity` records `audioFallbackUsed = true` if the mode was active at any point during the session. (Deferred: Session persistence layer not yet implemented).

## Tasks / Subtasks

- [x] Implement Lighting Detection Logic. (AC: 1, 2, 3)
  - [x] Create `LightingUseCase` in `:feature:session:session-domain`.
  - [x] Implement `SensorEventListener` for `Sensor.TYPE_LIGHT` to monitor ambient lux.
  - [x] Create a `LightingStatus` flow that combines lux and pose confidence with debounce logic (2s trigger, 3s revert).
- [x] Integrate with ActiveSession MVI. (AC: 4, 8)
  - [x] Update `ActiveSessionState` with `isAudioOnlyMode` and `isManualOverride`.
  - [x] Add `ToggleAudioOnlyMode` event to `ActiveSessionEvent`.
  - [x] Inject `LightingUseCase` into `ActiveSessionViewModel` and observe the status flow.
- [x] Implement Audio-Only UI & TTS. (AC: 5, 6)
  - [x] Create `AudioOnlyOverlay` in `:feature:session:session-ui`.
  - [x] Integrate `android.speech.tts.TextToSpeech` for workout instructions.
  - [x] Update `ActiveSessionCameraRoute` to toggle between `CameraPreview` and `AudioOnlyOverlay`.
- [x] Analytics and Persistence. (AC: 7, 8)
  - [x] Log events using `AnalyticsLogger`.
  - [ ] Update `SessionRepository` to save the `audioFallbackUsed` flag in `SessionEntity`. (Deferred until Session persistence story).
- [x] Testing.
  - [x] Unit tests for `LightingUseCase` using `Turbine` to verify timing/thresholds.
  - [x] Verified build and integration in `SessionEntryDestination`.

## Dev Notes

- **Thresholds**: 
  - Trigger: `lux < 10` or `confidence < 0.6` for 2,000ms.
  - Revert: `lux > 10` and `confidence > 0.6` for 3,000ms.
- **Sensor Handling**: Implemented `AndroidLightSensorProvider` using `Sensor.TYPE_LIGHT` in `:feature:session:session-data`.
- **MVI Pattern**: Used `ActiveSessionAction.Announce` for triggering the TTS announcements.
- **TTS Lifecycle**: Managed via `SessionTtsManager` which is remembered in the `ActiveSessionCameraRoute`.
- **Architecture Note**: Unified `SessionEntryDestination` to use `ActiveSessionCameraRoute` for both camera and initial audio-only sessions (by triggering a toggle event).

### Project Structure Notes

- **Domain Logic**: `LightingUseCase`, `ILightSensorProvider`, and `LightingStatus` added to `:feature:session:session-domain`.
- **Data Implementation**: `AndroidLightSensorProvider` and Hilt binding added to `:feature:session:session-data`.
- **UI Components**: `AudioOnlyOverlay` and `SessionTtsManager` added to `:feature:session:session-ui`.

### References

- [Source: _bmad-output/planning-artifacts/fitlife-architecture-v1.md#9-lighting-detection-trigger]
- [Source: _bmad-output/planning-artifacts/fitlife-prd-v1.md#Section-7.1]
- [Source: _bmad-output/implementation-artifacts/session-003-fatigue-detection-use-case.md]

### Review Findings

- [x] [Review][Decision] Manual Override Persistence — Resolved: Implemented 'Return to Auto' logic on second toggle tap.
- [x] [Review][Patch] `LightingUseCase` init block logic (Race Condition) [LightingUseCase.kt:18]
- [x] [Review][Patch] TTS Initialization Race [SessionTtsManager.kt:18]
- [x] [Review][Patch] Threshold Precision (AC 3) [LightingUseCase.kt:21]

## Dev Agent Record

### Agent Model Used

BMad Developer Agent (Gemini 2.0 Flash)

### Debug Log References

- [Build Status]: Success (`:feature:session:session-ui:assembleDebug`)
- [Test Status]: Success (`LightingUseCaseTest`)

### Completion Notes List
- Implemented `LightingUseCase` with debounce logic for triggering and reverting audio-only mode.
- Added `AndroidLightSensorProvider` to bridge Android's `SensorManager` to the domain.
- Integrated lighting detection into `ActiveSessionViewModel` and updated the MVI state.
- Created `AudioOnlyOverlay` and `SessionTtsManager` for mode feedback.
- Simplified `SessionEntryDestination` to use a unified `ActiveSessionCameraRoute`.
- Added `Turbine` to project dependencies to support advanced Flow testing.
- Deferred database persistence of `audioFallbackUsed` as the session persistence layer (SESSION-008) is not yet implemented.

### File List
- `gradle/libs.versions.toml`
- `feature/session/session-domain/build.gradle.kts`
- `feature/session/session-domain/src/main/java/com/aml_sakr/fitlife/feature/session/domain/pose/LightingStatus.kt`
- `feature/session/session-domain/src/main/java/com/aml_sakr/fitlife/feature/session/domain/pose/ILightSensorProvider.kt`
- `feature/session/session-domain/src/main/java/com/aml_sakr/fitlife/feature/session/domain/pose/LightingUseCase.kt`
- `feature/session/session-domain/src/test/java/com/aml_sakr/fitlife/feature/session/domain/pose/LightingUseCaseTest.kt`
- `feature/session/session-data/src/main/java/com/aml_sakr/fitlife/feature/session/data/pose/AndroidLightSensorProvider.kt`
- `feature/session/session-data/src/main/java/com/aml_sakr/fitlife/feature/session/data/pose/di/PoseModule.kt`
- `feature/session/session-ui/src/main/res/values/strings.xml`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/ActiveSessionContracts.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/ActiveSessionViewModel.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/components/AudioOnlyOverlay.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/service/SessionTtsManager.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/preview/ActiveSessionCameraRoute.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/SessionEntryDestination.kt`
