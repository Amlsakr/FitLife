# Story 4.2: ML Kit PoseDetector Integration

Status: done

## Story

As a FitLife user,
I want real-time pose detection during my workout session,
so that I can provide feedback on my form.

## Acceptance Criteria

1. [x] Pose detector is initialized in `:feature:session:session-data` module using ML Kit.
2. [x] The integration provides key joint coordinates at ≥15 fps on mid-range devices (verified by SETUP-004 spike).
3. [x] A production `AnalyzePoseUseCase` emits a `PoseData` stream from CameraX `ImageAnalysis` frames.
4. [x] The implementation supports lifecycle-safe startup and shutdown of the pose detector, ensuring no memory leaks.
5. [x] Architecture boundaries are respected: ML Kit SDK types (e.g., `Pose`, `PoseLandmark`) stay in `session-data`, while domain models (`PoseData`, `PoseJoint`) are defined in `session-domain`.
6. [x] Automated tests verify the mapping from ML Kit `Pose` to domain `PoseData` using fake data.

## Tasks / Subtasks

- [x] Define production pose domain models. (AC: 5)
  - [x] Create `PoseData.kt` in `:feature:session:session-domain` with joint coordinates and confidence scores.
  - [x] Define `PoseJoint` enum and `JointCoordinate` data class.
- [x] Implement production Pose Detector in session-data. (AC: 1, 4, 5)
  - [x] Add `mlkit-pose-detection` dependency to `:feature:session:session-data`.
  - [x] Create `MlKitPoseDetector` as an internal implementation of a session-data repository or internal service.
  - [x] Implement conversion logic from ML Kit `Pose` to domain `PoseData`.
  - [x] Reuse the optimized configuration from `MlKitPoseBenchmarkHarness` (Stream Mode, base detector).
- [x] Create AnalyzePoseUseCase. (AC: 3, 5)
  - [x] Implement `AnalyzePoseUseCase` in `:feature:session:session-domain` that accepts an `ImageProxy` (abstracted if possible) and returns `Flow<PoseData>`.
  - [x] Ensure the use case does not leak Android/ML Kit types into its API.
- [x] Integrate with Session UI. (AC: 2, 3, 4)
  - [x] Update `CameraPreview` or a new analyzer-specific composable to bind `ImageAnalysis` use case.
  - [x] Connect `AnalyzePoseUseCase` to the `ImageAnalysis` analyzer callback.
  - [x] Handle detector initialization errors gracefully in the UI.
- [x] Verification and Testing. (AC: 2, 6)
  - [x] Add unit tests for `AnalyzePoseUseCase` using a fake detector implementation.
  - [x] Verify average FPS remains ≥15 on a representative device.
  - [x] Run `.\gradlew.bat :feature:session:session-data:test :feature:session:session-domain:test`.

### Review Findings

- [x] [Review][Decision] AC 2 FPS Monitoring — Added production FPS logging every 100 frames.
- [x] [Review][Decision] AC 6 Mapping Test — Implemented `MlKitPoseDetectorMappingTest` using reflection and mocks.
- [x] [Review][Patch] Silent Failure in Detector [MlKitPoseDetector.kt:56]
- [x] [Review][Patch] Concurrent Frame Processing [ActiveSessionCameraRoute.kt:72]
- [x] [Review][Patch] Global Unbind Risk [CameraPreview.kt:127]
- [x] [Review][Patch] Executor Lifecycle Leak [CameraPreview.kt:50]
- [x] [Review][Patch] Shutdown Race Condition [MlKitPoseDetector.kt:51]
- [x] [Review][Patch] Deprecated VolumeOff icon [ActiveSessionCameraRoute.kt:115]

## Dev Notes

- Follow MVI + Clean Architecture pattern.
- ML Kit `PoseDetector` must be closed when the session ends or the component is disposed.
- `ImageAnalysis` resolution should be pinned to 640x480 as per the spike success.
- Use `STRATEGY_KEEP_ONLY_LATEST` for `ImageAnalysis` to maintain real-time performance.

### Project Structure Notes

- Module `:feature:session:session-data` hosts the ML Kit implementation.
- Module `:feature:session:session-domain` hosts the models and use case.
- Module `:feature:session:session-ui` hosts the CameraX analyzer integration.

### References

- [Source: _bmad-output/planning-artifacts/fitlife-architecture-v1.md#7-ml-kit-pose-detection-pipeline]
- [Source: _bmad-output/implementation-artifacts/spike-ml-kit-pose-detection-15-fps-report.md]
- [Source: docs/fitlife-stories-v1.md#SESSION-002]

## Dev Agent Record

### Agent Model Used

BMad Dev Agent

### Debug Log References

### Completion Notes List
- Defined `PoseData`, `JointCoordinate`, and `PoseJoint` models in `:feature:session:session-domain`.
- Implemented `PoseDetector` interface and `AnalyzePoseUseCase` in `:feature:session:session-domain`.
- Implemented `MlKitPoseDetector` in `:feature:session:session-data` with AC 2 FPS monitoring and race condition guards.
- Integrated `ImageAnalysis` with a `Channel`-based decoupled processing loop in `ActiveSessionCameraRoute`.
- Resolved all code review findings (leaks, silent failures, deprecated icons).
- Verified implementation with unit tests and `MlKitPoseDetectorMappingTest`.

### File List
- `feature/session/session-domain/build.gradle.kts`
- `feature/session/session-domain/src/main/java/com/aml_sakr/fitlife/feature/session/domain/pose/PoseData.kt`
- `feature/session/session-domain/src/main/java/com/aml_sakr/fitlife/feature/session/domain/pose/PoseDetector.kt`
- `feature/session/session-domain/src/main/java/com/aml_sakr/fitlife/feature/session/domain/pose/AnalyzePoseUseCase.kt`
- `feature/session/session-data/src/main/java/com/aml_sakr/fitlife/feature/session/data/pose/MlKitPoseDetector.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/ActiveSessionContracts.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/ActiveSessionViewModel.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/preview/CameraPreview.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/preview/CameraPreviewProvider.kt`
- `feature/session/session-ui/src/main/java/com/aml_sakr/fitlife/feature/session/ui/preview/ActiveSessionCameraRoute.kt`
- `feature/session/session-domain/src/test/java/com/aml_sakr/fitlife/feature/session/domain/pose/AnalyzePoseUseCaseTest.kt`
- `feature/session/session-data/src/androidTest/java/com/aml_sakr/fitlife/feature/session/data/pose/MlKitPoseDetectorTest.kt`
- `feature/session/session-ui/src/androidTest/java/com/aml_sakr/fitlife/feature/session/ui/preview/ActiveSessionCameraRouteTest.kt`
