# Story 4.2: ML Kit PoseDetector Integration

Status: ready-for-dev

## Story

As a FitLife user,
I want real-time pose detection during my workout session,
so that the app can analyze my form and provide feedback.

## Acceptance Criteria

1. [ ] Pose detector is initialized in `:feature:session:session-data` module using ML Kit.
2. [ ] The integration provides key joint coordinates at ≥15 fps on mid-range devices (verified by SETUP-004 spike).
3. [ ] A production `AnalyzePoseUseCase` emits a `PoseData` stream from CameraX `ImageAnalysis` frames.
4. [ ] The implementation supports lifecycle-safe startup and shutdown of the pose detector, ensuring no memory leaks.
5. [ ] Architecture boundaries are respected: ML Kit SDK types (e.g., `Pose`, `PoseLandmark`) stay in `session-data`, while domain models (`PoseData`, `PoseJoint`) are defined in `session-domain`.
6. [ ] Automated tests verify the mapping from ML Kit `Pose` to domain `PoseData` using fake data.

## Tasks / Subtasks

- [ ] Define production pose domain models. (AC: 5)
  - [ ] Create `PoseData.kt` in `:feature:session:session-domain` with joint coordinates and confidence scores.
  - [ ] Define `PoseJoint` enum and `JointCoordinate` data class.
- [ ] Implement production Pose Detector in session-data. (AC: 1, 4, 5)
  - [ ] Add `mlkit-pose-detection` dependency to `:feature:session:session-data`.
  - [ ] Create `MlKitPoseDetector` as an internal implementation of a session-data repository or internal service.
  - [ ] Implement conversion logic from ML Kit `Pose` to domain `PoseData`.
  - [ ] Reuse the optimized configuration from `MlKitPoseBenchmarkHarness` (Stream Mode, base detector).
- [ ] Create AnalyzePoseUseCase. (AC: 3, 5)
  - [ ] Implement `AnalyzePoseUseCase` in `:feature:session:session-domain` that accepts an `ImageProxy` (abstracted if possible) and returns `Flow<PoseData>`.
  - [ ] Ensure the use case does not leak Android/ML Kit types into its API.
- [ ] Integrate with Session UI. (AC: 2, 3, 4)
  - [ ] Update `CameraPreview` or a new analyzer-specific composable to bind `ImageAnalysis` use case.
  - [ ] Connect `AnalyzePoseUseCase` to the `ImageAnalysis` analyzer callback.
  - [ ] Handle detector initialization errors gracefully in the UI.
- [ ] Verification and Testing. (AC: 2, 6)
  - [ ] Add unit tests for `AnalyzePoseUseCase` using a fake detector implementation.
  - [ ] Verify average FPS remains ≥15 on a representative device.
  - [ ] Run `.\gradlew.bat :feature:session:session-data:test :feature:session:session-domain:test`.

## Dev Notes

- Follow MVI + Clean Architecture pattern.
- ML Kit `PoseDetector` must be closed when the session ends or the component is disposed.
- `ImageAnalysis` resolution should be pinned to 640x480 as per the spike success.
- Use `STRATEGY_KEEP_ONLY_LATEST` for `ImageAnalysis` to maintain real-time performance.

### Project Structure Notes

- Module `:feature:session:session-data` will host the ML Kit implementation.
- Module `:feature:session:session-domain` will host the models and use case.
- Module `:feature:session:session-ui` will host the CameraX analyzer integration.

### References

- [Source: _bmad-output/planning-artifacts/fitlife-architecture-v1.md#7-ml-kit-pose-detection-pipeline]
- [Source: _bmad-output/implementation-artifacts/spike-ml-kit-pose-detection-15-fps-report.md]
- [Source: docs/fitlife-stories-v1.md#SESSION-002]

## Dev Agent Record

### Agent Model Used

BMad Dev Agent

### Debug Log References

### Completion Notes List

### File List
